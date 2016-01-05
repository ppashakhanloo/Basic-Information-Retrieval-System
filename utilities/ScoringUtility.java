package utilities;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import core.Index;

public class ScoringUtility {
	public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
			Map<K, V> map) {
		SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
				new Comparator<Map.Entry<K, V>>() {
					@Override
					public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
						int res = e2.getValue().compareTo(e1.getValue());
						return res != 0 ? res : 1;
					}
				});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}

	public static int df(String s) {
		TreeSet<Pair<Integer, Integer>> tsp = Index.getPosIndex().get(s);
		HashSet<Integer> docIDs = new HashSet<Integer>();

		for (Pair<Integer, Integer> p : tsp)
			docIDs.add((Integer) p.getLeft());

		return docIDs.size();
	}

	public static double computeDocWiseScore(double[] doc, double[] query) {
		double score = 0;
		for (int i = 0; i < query.length; i++) {
			score += doc[i] * query[i];
		}
		return score;
	}

}
