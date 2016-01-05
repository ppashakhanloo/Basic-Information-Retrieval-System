package core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utilities.Sets;

public class Query {

	private static ArrayList<String> partsExact = new ArrayList<String>();
	private static ArrayList<String> partsNotExactNotStar = new ArrayList<String>();
	private static ArrayList<String> partsStar = new ArrayList<String>();

	public static ArrayList<String> getPartsExact() {
		return partsExact;
	}

	public static ArrayList<String> getPartsNotExactNotStar() {
		return partsNotExactNotStar;
	}

	public static ArrayList<String> getPartsStar() {
		return partsStar;
	}

	// get a raw query and understand its parts!
	// returns a string without " and with *
	// it fills partsExact, partsStar, partsNotExactNotStar correctly
	public String understandQuery(String query) {
		Tokenizer t;
		query = query.replace(".", "");
		query = query.toLowerCase();

		// get different parts of the query
		partsExact = getBetweenQuotes(query);

		String tempQuery = new String(query);
		tempQuery = tempQuery.replaceAll("\"", "");

		for (int i = 0; i < partsExact.size(); i++) {
			tempQuery = tempQuery.replaceFirst(partsExact.get(i), "");
		}

		tempQuery = tempQuery.trim();
		String[] partsNew = tempQuery.split(" ");

		partsNotExactNotStar = new ArrayList<String>();
		partsStar = new ArrayList<String>();

		for (int i = 0; i < partsNew.length; i++) {
			if (partsNew[i].endsWith("*"))
				partsStar.add(partsNew[i].trim());
			else {
				partsNotExactNotStar.add(partsNew[i].trim());
			}
		}

		// first, tokenize and spell correct the exact phrases
		String enhancedExactPartsOfQuery = "";
		for (String s : partsExact) {
			t = new Tokenizer(s, TokenizationMode.Query);
			String thisPart = "";
			while (true) {
				String nt = t.nextToken();
				if (nt.equals("$")) {
					break;
				} else {
					if (!Index.getDictionary().contains(nt)) {
						thisPart += spellCorrectionWord(nt) + " ";
					} else {
						thisPart += nt + " ";
					}
				}
			}
			enhancedExactPartsOfQuery += "\"" + thisPart + "\"" + " ";
		}

		// update partsExact
		partsExact.clear();
		partsExact = getBetweenQuotes(enhancedExactPartsOfQuery);
		enhancedExactPartsOfQuery = "";
		for (int i = 0; i < partsExact.size(); i++) {
			enhancedExactPartsOfQuery += "\"" + partsExact.get(i) + "\" ";
		}

		// second, tokenize and spell correct the other words
		String enhancedNotExactNotStarPartsOfQuery = "";
		for (String s : partsNotExactNotStar) {
			t = new Tokenizer(s, TokenizationMode.Query);
			String thisPart = "";
			while (true) {
				String nt = t.nextToken();
				if (nt.equals("$")) {
					break;
				} else {
					if (!Index.getDictionary().contains(nt)) {
						thisPart += spellCorrectionWord(nt) + " ";
					} else {
						thisPart += nt + " ";
					}
				}
			}
			enhancedNotExactNotStarPartsOfQuery += thisPart + " ";
		}
		// Update partsNotExactNotStar
		partsNotExactNotStar.clear();
		String[] partsNew2 = enhancedNotExactNotStarPartsOfQuery.split(" ");
		for (int i = 0; i < partsNew2.length; i++) {
			if (!partsNew2[i].trim().equals("")) {
				partsNotExactNotStar.add(partsNew2[i].trim());
			}
		}

		String newQuery = "";
		newQuery = (newQuery.trim() + " " + enhancedExactPartsOfQuery.trim()
				+ " " + enhancedNotExactNotStarPartsOfQuery).trim();
		t = new Tokenizer(newQuery, TokenizationMode.Query);
		String result = "";
		for (String s : partsStar)
			result += s.trim() + " ";
		for (String s : partsExact)
			result += "\"" + s.trim() + "\" ";
		for (String s : partsNotExactNotStar)
			result += s.trim() + " ";

		return result.trim();
	}

	// get all substrings that are between "
	private ArrayList<String> getBetweenQuotes(String s) {
		Pattern regex = Pattern.compile("\"([^\"]*)\"");
		ArrayList<String> allMatches = new ArrayList<String>();
		Matcher matcher = regex.matcher(s);
		while (matcher.find()) {
			allMatches.add(matcher.group(1).trim());
		}
		return allMatches;
	}

	// get a word and return the correct spelling of
	private String spellCorrectionWord(String s) {
		double max = -1.0;
		String nearest = "";
		for (String term : commonBigramWords(s)) {
			double j = jaccard(term, s);
			if (j > max) {
				max = j;
				nearest = term;
			}
		}
		return nearest;
	}

	// get a string and return words with common bi-grams
	private HashSet<String> commonBigramWords(String s) {
		ArrayList<String> bi = Index.getBigrams(s);
		HashSet<String> commons = new HashSet<String>();
		for (String b : bi) {
			if (Index.getBigramIndex().containsKey(b)) {
				commons.addAll(Index.getBigramIndex().get(b));
			}
		}
		return commons;
	}

	// get two strings and calculate Jaccard coefficient
	private double jaccard(String s1, String s2) {
		ArrayList<String> bi1 = Index.getBigrams(s1);
		ArrayList<String> bi2 = Index.getBigrams(s2);

		Set<String> bi1set = new HashSet<String>(bi1);
		Set<String> bi2set = new HashSet<String>(bi2);
		double m = bi1set.size() + bi2set.size();

		bi1set.retainAll(bi2set);

		return bi1set.size() * 1.0 / (m - bi1set.size());
	}

	// return all different queries possible with understood query parts!
	public ArrayList<String> getAllDifferentQueries() {

		ArrayList<String> result = new ArrayList<String>();
		if (partsStar.size() > 0) {
			// there must be some sets from words
			ArrayList<HashSet<String>> possibleWordsForEachStar = new ArrayList<HashSet<String>>();
			for (int i = 0; i < partsStar.size(); i++) {
				HashSet<String> hs = new HashSet<String>(Index
						.trailingWordsForWildcard(partsStar.get(i)).keySet());
				possibleWordsForEachStar.add(hs);
			}
			String toAdd = "";
			for (int i = 0; i < partsExact.size(); i++) {
				toAdd += "\"" + partsExact.get(i).trim() + "\"" + " ";
			}
			for (int i = 0; i < partsNotExactNotStar.size(); i++) {
				toAdd += partsNotExactNotStar.get(i).trim() + " ";
			}
			toAdd = toAdd.trim();
			String temp = "";
			for (java.util.List<String> list : Sets
					.cartesianProduct(possibleWordsForEachStar)) {
				temp = "";
				for (String str : list) {
					temp += str + " ";
				}
				result.add((toAdd + " " + temp).trim());
			}
		} else {
			String toAdd = "";
			for (int i = 0; i < partsExact.size(); i++) {
				toAdd += "\"" + partsExact.get(i).trim() + "\"" + " ";
			}
			for (int i = 0; i < partsNotExactNotStar.size(); i++) {
				toAdd += partsNotExactNotStar.get(i).trim() + " ";
			}
			toAdd = toAdd.trim();
			result.add(toAdd);
		}
		return result;
	}
}