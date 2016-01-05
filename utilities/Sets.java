package utilities;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class Sets {
	private Sets() {
	}

	public static <B> Set<List<B>> cartesianProduct(
			List<? extends Set<? extends B>> sets) {
		CartesianSet<B> cartesianSet = new CartesianSet<B>(sets);
		// empty set
		ArrayList<B> emptyList = new ArrayList<B>(0);
		HashSet<List<B>> resSet = new HashSet<List<B>>(1);
		resSet.add(emptyList);
		return cartesianSet.isEmpty() ? resSet : cartesianSet;
	}

	public static class ImmutableList<TEMPB> extends ArrayList<TEMPB> {

		public static final class Builder<E> { // implements Collection<E>,
												// Serializable {
			private final ImmutableList<E> contents = new ImmutableList<E>();

			public Builder<E> add(E element) {
				contents.add(element);
				return this;
			}

			public ImmutableList<E> build() {
				return copyOf(contents);
			}
		}

		private static final long serialVersionUID = -7348009608473104985L;

		public static <E> Builder<E> builder() {
			return new Builder<E>();
		}

		public static <TEMPB> ImmutableList<TEMPB> copyOf(TEMPB[] tuple) {
			return new ImmutableList<TEMPB>(Arrays.asList(Arrays.copyOf(tuple,
					tuple.length)));
		}

		public static <TEMPB> ImmutableList<TEMPB> copyOf(List<TEMPB> list) {
			return new ImmutableList<TEMPB>(list);
		}

		public ImmutableList(Collection<? extends TEMPB> list) {
			super(list);
		}

		public ImmutableList() {
			super();
		}

	}

	public static class ImmutableSet<TEMPC> extends HashSet<TEMPC> {
		public ImmutableSet(Set<TEMPC> set) {
			super(set);
		}

		public ImmutableSet() {
			super();
		}

		public static <TEMPC> ImmutableSet<TEMPC> copyOf(Set<TEMPC> set) {
			return new ImmutableSet<TEMPC>(set);
		}

		private static final long serialVersionUID = -918893583599428519L;

		public ImmutableList<TEMPC> asList() {
			return new ImmutableList<TEMPC>(this);
		}
	}

	private static class CartesianSet<B> extends AbstractSet<List<B>> {
		final ImmutableList<Axis> axes;
		final int size;

		CartesianSet(List<? extends Set<? extends B>> sets) {
			long dividend = 1;
			ImmutableList.Builder<Axis> builder = ImmutableList.builder();
			for (Set<? extends B> set : sets) {
				Axis axis = new Axis(set, (int) dividend); // check overflow at
															// end
				builder.add(axis);
				dividend *= axis.size();
			}
			this.axes = builder.build();
			size = (int) (0xFFFFFFFF & dividend); // removed the checked cast
		}

		@Override
		public int size() {
			return size;
		}

		private static interface UnmodifiableIterator<TEMPA> extends
				Iterator<TEMPA> {

		}

		@Override
		public UnmodifiableIterator<List<B>> iterator() {
			return new UnmodifiableIterator<List<B>>() {
				int index;

				public boolean hasNext() {
					return index < size;
				}

				public List<B> next() {
					if (!hasNext()) {
						return null;
					}

					Object[] tuple = new Object[axes.size()];
					for (int i = 0; i < tuple.length; i++) {
						tuple[i] = axes.get(i).getForIndex(index);
					}
					index++;

					@SuppressWarnings("unchecked")
					List<B> result = (ImmutableList<B>) ImmutableList
							.copyOf(tuple);
					return result;
				}

				@Override
				public void remove() {
				}
			};
		}

		@Override
		public boolean contains(Object element) {
			if (!(element instanceof List<?>)) {
				return false;
			}
			List<?> tuple = (List<?>) element;
			int dimensions = axes.size();
			if (tuple.size() != dimensions) {
				return false;
			}
			for (int i = 0; i < dimensions; i++) {
				if (!axes.get(i).contains(tuple.get(i))) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean equals(Object object) {
			if (object instanceof CartesianSet) {
				CartesianSet<?> that = (CartesianSet<?>) object;
				return this.axes.equals(that.axes);
			}
			return super.equals(object);
		}

		@Override
		public int hashCode() {
			int adjust = size - 1;
			for (int i = 0; i < axes.size(); i++) {
				adjust *= 31;
			}
			return axes.hashCode() + adjust;
		}

		private class Axis {
			final ImmutableSet<? extends B> choices;
			final ImmutableList<? extends B> choicesList;
			final int dividend;

			Axis(Set<? extends B> set, int dividend) {
				choices = ImmutableSet.copyOf(set);
				choicesList = choices.asList();
				this.dividend = dividend;
			}

			int size() {
				return choices.size();
			}

			B getForIndex(int index) {
				return choicesList.get(index / dividend % size());
			}

			boolean contains(Object target) {
				return choices.contains(target);
			}

			@Override
			public boolean equals(Object obj) {
				if (obj instanceof CartesianSet.Axis) {
					@SuppressWarnings("rawtypes")
					CartesianSet.Axis that = (CartesianSet.Axis) obj;
					return this.choices.equals(that.choices);
				}
				return false;
			}

			@Override
			public int hashCode() {
				return size / choices.size() * choices.hashCode();
			}
		}
	}
}