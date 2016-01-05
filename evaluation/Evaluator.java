package evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;

public abstract class Evaluator {

	private static TreeMap<Integer, String> queries = new TreeMap<Integer, String>();
	private static HashMap<Integer, LinkedHashSet<Integer>> relevanceAssesment = new HashMap<Integer, LinkedHashSet<Integer>>();

	public Evaluator() throws IOException {
		if (queries.size() == 0 && relevanceAssesment.size() == 0)
			initiateRelevanceAssesment();
	}

	private static void getAllQueries() throws IOException {
		final File folder = new File(
				"C:\\Users\\Pardis\\workspace\\MIR_SYSTEM\\NPL-v2\\NPL\\Queries");

		for (final File fileEntry : folder.listFiles()) {
			for (String line : Files.readAllLines(fileEntry.toPath(),
					Charset.forName("ISO-8859-1"))) {
				if (!queries.containsKey(Integer.parseInt(fileEntry.getName())))
					queries.put(Integer.parseInt(fileEntry.getName()), line);
				else {
					queries.put(Integer.parseInt(fileEntry.getName()),
							queries.get(Integer.parseInt(fileEntry.getName()))
									+ " " + line);
				}
			}
		}
	}

	private static void initiateRelevanceAssesment() throws IOException {
		getAllQueries();
		getAllRevelanceAssesment();
	}

	public static TreeMap<Integer, String> getQueries() {
		return queries;
	}

	public static HashMap<Integer, LinkedHashSet<Integer>> getRelevanceAssesment() {
		return relevanceAssesment;
	}

	private static void getAllRevelanceAssesment() throws IOException {
		final File relAsses = new File(
				"C:\\Users\\Pardis\\workspace\\MIR_SYSTEM\\NPL-v2\\NPL\\RelevanceAssesment\\RelevanceAssesment");

		for (String line : Files.readAllLines(relAsses.toPath(),
				Charset.forName("ISO-8859-1"))) {
			String[] parts = line.split("\\s+");
			LinkedHashSet<Integer> relatedDocs = new LinkedHashSet<Integer>();
			for (int i = 1; i < parts.length; i++)
				relatedDocs.add(Integer.valueOf(parts[i]));
			relevanceAssesment.put(Integer.valueOf(parts[0]), relatedDocs);
		}
	}

	public abstract double evalQuery(Integer[] retResult, int q);

}
