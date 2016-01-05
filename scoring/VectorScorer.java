package scoring;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedSet;

public interface VectorScorer {
	public SortedSet<Map.Entry<Integer, Double>> getDocsByScore(
			ArrayList<String> queryTerms, ArrayList<Integer> docsToDelete);

	public double[][] getDocVector(ArrayList<String> queryTerms);

	public double[] getQueryVector(ArrayList<String> queryTerms);

	public Map<Integer, Double> computeScoreBySpecifiedMethod(
			ArrayList<String> queryTerms);
}
