package utilities;

public class Pair<L, R> implements Comparable<Pair<L, R>> {

	private L left;
	private R right;

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	public void setRight(R right) {
		this.right = right;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	@Override
	public int hashCode() {
		return left.hashCode() + right.hashCode() + 31;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair))
			return false;
		Pair<L, R> pairo = (Pair) o;
		return this.left.equals(pairo.getLeft())
				&& this.right.equals(pairo.getRight());
	}

	@Override
	public String toString() {
		return "(" + left.toString() + "," + right.toString() + ")";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int compareTo(Pair o) {

		try {
			int lthis = (int) this.getLeft();
			int lthat = (int) o.getLeft();

			double rthis = (double) this.getRight();
			double rthat = (double) o.getRight();

			if (lthis == lthat && rthis == rthat)
				return 0;
			if (lthis > lthat)
				return 1;
			if (lthis == lthat && rthis > rthat)
				return 1;
			return -1;
		} catch (Exception e) {
			int lthis = (int) this.getLeft();
			int lthat = (int) o.getLeft();

			int rthis = (int) this.getRight();
			int rthat = (int) o.getRight();

			if (lthis == lthat && rthis == rthat)
				return 0;
			if (lthis > lthat)
				return 1;
			if (lthis == lthat && rthis > rthat)
				return 1;
			return -1;
		}

	}
}
