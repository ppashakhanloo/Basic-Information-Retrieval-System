package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import utilities.Pair;

public class Index {

	private Tokenizer t;
	private static TreeMap<String, TreeSet<Pair<Integer, Integer>>> posIndex = new TreeMap<String, TreeSet<Pair<Integer, Integer>>>();
	private static HashMap<String, TreeSet<String>> bigramIndex = new HashMap<String, TreeSet<String>>();
	private static HashMap<Integer, HashMap<String, Integer>> tfTable = new HashMap<Integer, HashMap<String, Integer>>();

	public Index() {
		t = new Tokenizer(
				"C:\\Users\\Pardis\\workspace\\MIR_SYSTEM\\NPL-v2\\NPL\\Documents\\",
				TokenizationMode.FolderFiles);
	}

	// phrase is ~without~ double quote
	public HashSet<Integer> findDocsIncludingPhrase(String phrase) {
		String[] pt = phrase.split(" ");
		ArrayList<String> phraseTerms = new ArrayList<String>();
		ArrayList<TreeSet<Pair<Integer, Integer>>> weakRelatedPosLists = new ArrayList<TreeSet<Pair<Integer, Integer>>>();
		HashSet<Integer> goodDocs = new HashSet<Integer>();

		for (String s : pt)
			phraseTerms.add(s);

		if (pt.length > 1) {
			for (String s : phraseTerms) {
				weakRelatedPosLists.add(Index.getPosIndex().get(s));
			}

			// find good docs: phrase terms are coming in correct order
			int[] pointers = new int[weakRelatedPosLists.size()];

			WHILE: while (true) {
				if (allReachedEnd(pointers, weakRelatedPosLists)) {
					break WHILE;
				}
				if (allEqual(pointers, weakRelatedPosLists)) {
					// we must check for correctness of positions
					// then add docID if OK
					// then forward all the pointers
					int areDocsCorrect = checkForCorrectPositions(pointers,
							weakRelatedPosLists);

					if (areDocsCorrect != -1) {
						// yay! positions are correct!
						goodDocs.add(areDocsCorrect);
						forwardAllPointers(pointers, weakRelatedPosLists);
					} else {
						int minValPointer = minValPointerIndex(pointers,
								weakRelatedPosLists);

						if (pointers.length > minValPointer) {
							if (pointers[minValPointer] + 1 < weakRelatedPosLists
									.get(minValPointer).size())
								pointers[minValPointer]++;
						}
					}
				} else {
					// there must be a min, forward the min pointer!
					int minPointer = minPointerIndex(pointers,
							weakRelatedPosLists);
					if (pointers[minPointer] + 1 < weakRelatedPosLists.get(
							minPointer).size())
						pointers[minPointer]++;
				}
				if (allReachedEnd(pointers, weakRelatedPosLists)) {
					break;
				}
			}
			return goodDocs;
		} else {

			for (Pair<Integer, Integer> p : Index.getPosIndex().get(pt[0]))
				goodDocs.add(p.getLeft());

			return goodDocs;
		}

	}

	@SuppressWarnings("unchecked")
	private int minValPointerIndex(int[] pointers,
			ArrayList<TreeSet<Pair<Integer, Integer>>> weakRelatedPosLists) {
		if (pointers.length == 1)
			return 0;

		int minVal = Integer.MAX_VALUE;
		int minIndex = -1;
		for (int i = 0; i < pointers.length; i++) {
			if (((Pair<Integer, Integer>) weakRelatedPosLists.get(i).toArray()[pointers[i]])
					.getRight() < minVal) {
				if (weakRelatedPosLists.get(i).size() > pointers[i] + 1) {
					minIndex = i;
					minVal = ((Pair<Integer, Integer>) weakRelatedPosLists.get(
							i).toArray()[pointers[i]]).getRight();
				}
			}
		}
		return minIndex;
	}

	private boolean allReachedEnd(int[] pointers,
			ArrayList<TreeSet<Pair<Integer, Integer>>> weakRelatedPosLists) {
		for (int i = 0; i < pointers.length; i++) {
			if (pointers[i] + 1 < weakRelatedPosLists.get(i).size()) {
				return false;
			}
		}
		return true;
	}

	// forward all pointers if all of them
	// consider end of list
	private void forwardAllPointers(int[] pointers,
			ArrayList<TreeSet<Pair<Integer, Integer>>> weakRelatedPosLists) {
		for (int i = 0; i < pointers.length; i++) {
			if (pointers[i] + 1 < weakRelatedPosLists.get(i).size())
				pointers[i]++;
		}
	}

	@SuppressWarnings("unchecked")
	private int minPointerIndex(int[] pointers,
			ArrayList<TreeSet<Pair<Integer, Integer>>> weakRelatedPosLists) {
		int minVal = Integer.MAX_VALUE;
		int minIndex = -1;
		for (int i = 0; i < pointers.length; i++) {
			if (((Pair<Integer, Integer>) weakRelatedPosLists.get(i).toArray()[pointers[i]])
					.getLeft() < minVal) {
				if (weakRelatedPosLists.get(i).toArray().length > pointers[i] + 1) {
					minIndex = i;
					minVal = ((Pair<Integer, Integer>) weakRelatedPosLists.get(
							i).toArray()[pointers[i]]).getLeft();
				}
			}
		}
		return minIndex;

	}

	// return docID if correct
	// else return -1
	@SuppressWarnings("unchecked")
	private int checkForCorrectPositions(int[] pointers,
			ArrayList<TreeSet<Pair<Integer, Integer>>> weakRelatedPosLists) {
		for (int i = 0; i < pointers.length - 1; i++) {
			if (((Pair<Integer, Integer>) weakRelatedPosLists.get(i).toArray()[pointers[i]])
					.getRight() + 1 != ((Pair<Integer, Integer>) weakRelatedPosLists
					.get(i + 1).toArray()[pointers[i + 1]]).getRight()) {

				return -1;
			}
		}
		return ((Pair<Integer, Integer>) weakRelatedPosLists.get(0).toArray()[pointers[0]])
				.getLeft();
	}

	@SuppressWarnings("unchecked")
	private boolean allEqual(int[] pointers,
			ArrayList<TreeSet<Pair<Integer, Integer>>> weakRelatedPosLists) {

		for (int i = 0; i < pointers.length - 1; i++) {
			int left = ((Pair<Integer, Integer>) weakRelatedPosLists.get(i)
					.toArray()[pointers[i]]).getLeft();
			int leftOther = ((Pair<Integer, Integer>) weakRelatedPosLists.get(
					i + 1).toArray()[pointers[i + 1]]).getLeft();
			if (left != leftOther) {

				return false;
			}
		}

		return true;
	}

	public static TreeMap<String, TreeSet<Pair<Integer, Integer>>> getPosIndex() {
		return posIndex;
	}

	public static Set<String> getDictionary() {
		return posIndex.descendingKeySet();
	}

	public static ArrayList<String> getDictionarySorted() {
		ArrayList<String> al = new ArrayList<String>(
				posIndex.descendingKeySet());
		return al;
	}

	public static HashMap<String, TreeSet<String>> getBigramIndex() {
		return bigramIndex;
	}

	public static HashMap<Integer, HashMap<String, Integer>> getTfTable() {
		return tfTable;
	}

	public void createPosIndex() {
		posIndex.clear();
		t = new Tokenizer(
				"C:\\Users\\Pardis\\workspace\\MIR_SYSTEM\\NPL-v2\\NPL\\Documents\\",
				TokenizationMode.FolderFiles);
		while (true) {
			String nt = t.nextToken();
			if (nt.equals("$")) {
				break;
			} else {
				if (posIndex.containsKey(nt)) {
					posIndex.get(nt).add(
							new Pair<Integer, Integer>(t.getDocNumber(), t
									.getTokenNumber()));
				} else {
					TreeSet<Pair<Integer, Integer>> ts = new TreeSet<Pair<Integer, Integer>>();
					Pair<Integer, Integer> newP = new Pair<Integer, Integer>(
							t.getDocNumber(), t.getTokenNumber());
					ts.add(newP);
					posIndex.put(nt, ts);
				}

				if (tfTable.containsKey(t.getDocNumber())) {
					if (tfTable.get(t.getDocNumber()).containsKey(nt)) {
						int newTF = tfTable.get(t.getDocNumber()).get(nt) + 1;
						tfTable.get(t.getDocNumber()).remove(nt);
						tfTable.get(t.getDocNumber()).put(nt, newTF);
					} else {
						tfTable.get(t.getDocNumber()).put(nt, 1);
					}
				} else {
					HashMap<String, Integer> hm = new HashMap<String, Integer>();
					hm.put(nt, 1);
					tfTable.put(t.getDocNumber(), hm);
				}
			}
		}
	}

	public static void createBigramIndex() {
		bigramIndex.clear();
		// let us create this using PosIndex Dictionary
		ArrayList<String> currBigrams;
		for (String s : posIndex.keySet()) {
			currBigrams = getBigrams(s);
			for (int i = 0; i < currBigrams.size(); i++) {
				if (bigramIndex.containsKey(currBigrams.get(i))) {
					bigramIndex.get(currBigrams.get(i)).add(s);
				} else {
					TreeSet<String> newTS = new TreeSet<String>();
					newTS.add(s);
					bigramIndex.put(currBigrams.get(i), newTS);
				}
			}
		}
	}

	public static void createTfTable() {
		tfTable.clear();
		for (String s : posIndex.keySet()) {
			for (Pair<Integer, Integer> p : posIndex.get(s)) {
				if (tfTable.containsKey(p.getLeft())) {
					if (tfTable.get(p.getLeft()).containsKey(s)) {
						int newVal = 1 + tfTable.get(p.getLeft()).get(s);
						tfTable.get(p.getLeft()).remove(s);
						tfTable.get(p.getLeft()).put(s, newVal);
					} else {
						tfTable.get(p.getLeft()).put(s, 1);
					}
				} else {
					HashMap<String, Integer> innerNew = new HashMap<String, Integer>();
					innerNew.put(s, 1);
					tfTable.put(p.getLeft(), innerNew);
				}
			}
		}
	}

	public static ArrayList<String> getBigrams(String str) {
		ArrayList<String> bigrams = new ArrayList<String>();
		for (int i = 0; i < str.length() - 1; i++)
			bigrams.add(str.substring(i, i + 2));
		return bigrams;
	}

	public void removeDoc(int docID) {
		// Update Positional Index
		ArrayList<Pair<Integer, Integer>> toBeRemoved = new ArrayList<Pair<Integer, Integer>>();
		ArrayList<String> removedTokens = new ArrayList<String>();

		for (Iterator<Map.Entry<String, TreeSet<Pair<Integer, Integer>>>> itOuter = Index
				.getPosIndex().entrySet().iterator(); itOuter.hasNext();) {
			Map.Entry<String, TreeSet<Pair<Integer, Integer>>> outerEntry = itOuter
					.next();

			for (Pair<Integer, Integer> p : outerEntry.getValue()) {
				if (p.getLeft() == docID)
					toBeRemoved.add(p);
			}
			for (Pair<Integer, Integer> p : toBeRemoved) {
				outerEntry.getValue().remove(p);
			}
			if (outerEntry.getValue().size() == 0) {
				removedTokens.add(outerEntry.getKey());
				itOuter.remove();
			}
		}

		// Update Bigram Index
		ArrayList<String> bigrams;
		for (String s : removedTokens) {
			bigrams = getBigrams(s);
			for (int i = 0; i < bigrams.size(); i++) {
				bigramIndex.get(bigrams.get(i)).remove(s);
				if (bigramIndex.get(bigrams.get(i)).size() == 0)
					bigramIndex.remove(bigrams.get(i));
			}
		}

		// Update TF-Table
		Index.getTfTable().remove(docID);
	}

	public void addDoc(int docID) {

		// Update Positional Index
		String pathToDoc = "C:\\Users\\Pardis\\workspace\\MIR_SYSTEM\\NPL-v2\\NPL\\Documents\\"
				+ docID;
		t = new Tokenizer(pathToDoc, TokenizationMode.SingleFile);
		ArrayList<String> addedTokens = new ArrayList<String>();

		while (true) {
			String nt = t.nextToken();
			addedTokens.add(nt);
			if (nt.equals("$")) {
				break;
			} else {
				if (posIndex.containsKey(nt)) {
					posIndex.get(nt).add(
							new Pair<Integer, Integer>(docID, t
									.getTokenNumber()));
				} else {
					TreeSet<Pair<Integer, Integer>> ts = new TreeSet<Pair<Integer, Integer>>();
					Pair<Integer, Integer> newP = new Pair<Integer, Integer>(
							docID, t.getTokenNumber());
					ts.add(newP);
					posIndex.put(nt, ts);
				}

				// Update TF-Table
				if (tfTable.containsKey(docID)) {
					if (tfTable.get(docID).containsKey(nt)) {
						int newTF = tfTable.get(docID).get(nt) + 1;
						tfTable.get(docID).remove(nt);
						tfTable.get(docID).put(nt, newTF);
					} else {
						tfTable.get(docID).put(nt, 1);
					}
				} else {
					HashMap<String, Integer> hm = new HashMap<String, Integer>();
					hm.put(nt, 1);
					tfTable.put(docID, hm);
				}
			}
		}

		// Update Bigram Index
		ArrayList<String> bigrams;
		for (String s : addedTokens) {
			bigrams = getBigrams(s);
			for (int i = 0; i < bigrams.size(); i++) {
				if (bigramIndex.containsKey(bigrams.get(i))) {
					bigramIndex.get(bigrams.get(i)).add(s);
				} else {
					TreeSet<String> newTS = new TreeSet<String>();
					newTS.add(s);
					bigramIndex.put(bigrams.get(i), newTS);
				}
			}
		}
	}

	public void showPostingsList(String term) {
		System.out.println(posIndex.get(term));
	}

	@SuppressWarnings("rawtypes")
	public void showCompleteBigramIndex() {
		Set set = bigramIndex.entrySet();
		Iterator iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry) iterator.next();
			System.out.println(mentry.getKey() + "->" + mentry.getValue());
		}
	}

	// string s is the form word*
	// returns wild-card words with their p-list
	public static SortedMap<String, TreeSet<Pair<Integer, Integer>>> trailingWordsForWildcard(
			String s) {
		s = s.substring(0, s.length() - 1);
		if (s.charAt(s.length() - 1) != 'z') {
			int nextLastChar = s.charAt(s.length() - 1);
			nextLastChar++;
			return posIndex.subMap(s, s.substring(0, s.length() - 1)
					+ (char) nextLastChar);
		} else
			return posIndex.tailMap(s);
	}

	@SuppressWarnings("rawtypes")
	public void showCompletePosIndex() {
		Set set = posIndex.entrySet();
		Iterator iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry) iterator.next();
			System.out.println(mentry.getKey() + "->" + mentry.getValue());
		}
	}

	public void showCompleteTFTable() {
		System.out.println(Index.tfTable);
	}

	// gets a raw word -> tokenize it -> return p-list
	public TreeSet<Pair<Integer, Integer>> getPostingList(String inputStr) {
		t = new Tokenizer(inputStr, TokenizationMode.Query);
		String word = "";
		while (true) {
			String nt = t.nextToken();
			if (nt.equals("$")) {
				break;
			} else {
				word += nt;
			}
		}
		return Index.getPosIndex().get(word);
	}

	public static void setPosIndex(
			TreeMap<String, TreeSet<Pair<Integer, Integer>>> posIndex) {
		Index.posIndex = posIndex;
	}
}
