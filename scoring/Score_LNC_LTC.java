package scoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import core.Index;
import utilities.Pair;
import utilities.ScoringUtility;

public class Score_LNC_LTC implements VectorScorer {

	@Override
	public Map<Integer, Double> computeScoreBySpecifiedMethod(
			ArrayList<String> queryTerms) {
		Map<Integer, Double> dotProductResults = new TreeMap<Integer, Double>();

		double[][] dvs = getDocVector(queryTerms);
		double[] qv = getQueryVector(queryTerms);

		for (int i = 0; i < 11500; i++) {
			double temp = ScoringUtility.computeDocWiseScore(dvs[i], qv);

			if (temp > 0) {
				dotProductResults.put(i, temp);
			}
		}
		return dotProductResults;
	}

	@Override
	// just call this and this will call all other methods!
	public SortedSet<Map.Entry<Integer, Double>> getDocsByScore(
			ArrayList<String> queryTerms, ArrayList<Integer> onlyDocs) {
		Map<Integer, Double> rawResult = computeScoreBySpecifiedMethod(queryTerms);
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		if (onlyDocs != null) {
			for (Integer i : rawResult.keySet()) {
				if (!onlyDocs.contains(i))
					toRemove.add(i);
			}
			for (Integer i : toRemove) {
				rawResult.remove(i);
			}
		}
		return ScoringUtility.entriesSortedByValues(rawResult);
	}

	@Override
	public double[][] getDocVector(ArrayList<String> queryTerms) {
		HashSet<String> qt = new HashSet<String>(queryTerms);
		queryTerms = new ArrayList<String>(qt);

		// each row is a doc
		// each column is a term
		double[][] docScores = new double[11500][Index.getDictionary().size()];

		// we must get docs in union of postings lists of query terms
		HashSet<Integer> relatedDocs = new HashSet<Integer>();
		for (String s : queryTerms) {
			if (!s.trim().equals(""))
				for (Pair<Integer, Integer> p : Index.getPosIndex().get(s)) {
					relatedDocs.add((int) p.getLeft());
				}
		}

		for (Integer doc : relatedDocs) {
			for (String s : queryTerms) {
				if (Index.getTfTable().get(doc).get(s) != null) {
					docScores[doc][Index.getDictionarySorted().indexOf(s)] += 1 + Math
							.log10(Index.getTfTable().get(doc).get(s));
				}
			}
		}

		// normalize
		for (int i = 0; i < docScores.length; i++) {

			double norm = 0;
			for (int j = 0; j < docScores[i].length; j++) {
				if (docScores[i][j] != 0)
					norm += docScores[i][j] * docScores[i][j];
			}
			norm = Math.sqrt(norm);
			if (norm != 0)
				for (int j = 0; j < docScores[i].length; j++) {
					docScores[i][j] /= norm;
				}
		}

		return docScores;
	}

	@Override
	public double[] getQueryVector(ArrayList<String> queryTerms) {
		double[] queryScore = new double[Index.getDictionary().size()];
		// First, we have to count number of each query term
		HashMap<String, Integer> queryFrequency = new HashMap<String, Integer>();

		for (String s : queryTerms) {
			if (!queryFrequency.containsKey(s)) {
				queryFrequency.put(s, 1);
			} else {
				int newQF = queryFrequency.get(s) + 1;
				queryFrequency.remove(s);
				queryFrequency.put(s, newQF);
			}
		}
		// (1+log10(tf))*(log10(N/df))*normalize
		// Second, we have to calculate the number of docs = N
		int N = Index.getTfTable().size();
		// for each term of query: calculate ltn = [1+log10(tf)]*log10(N/df)
		for (int i = 0; i < queryTerms.size(); i++) {
			// Third, we have to calculate df for each term
			queryScore[Index.getDictionarySorted().indexOf(queryTerms.get(i))] = (1 + Math
					.log10(queryFrequency.get(queryTerms.get(i))))
					* (Math.log10(N * 1.0
							/ ScoringUtility.df(queryTerms.get(i))));
		}

		// normalize
		double norm = 0;
		for (int i = 0; i < queryScore.length; i++) {
			norm += queryScore[i] * queryScore[i];
		}
		norm = Math.sqrt(norm);
		for (int i = 0; i < queryScore.length; i++) {
			queryScore[i] /= norm;
		}

		return queryScore;
	}
}
