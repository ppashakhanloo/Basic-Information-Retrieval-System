package ui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import compression.Compression;
import compression.Decompression;
import scoring.Score_LNC_LTC;
import scoring.Score_LNN_LTN;
import scoring.VectorScorer;
import utilities.Pair;
import core.Index;
import core.Query;
import evaluation.Evaluator;
import evaluation.FMeasure;
import evaluation.MAP;
import evaluation.RPrecision;

public class Main {
	public static void showMainMenu() {
		System.out.println("|-----------------------------------------------|");
		System.out.println("|Choose One of the Sections:\t\t\t|");
		System.out.println("|\t1: Part 1 - Indexing and Compression\t|");
		System.out.println("|\t2: Part 2 - Query and Retrieval\t\t|");
		System.out.println("|\t3: Part 3 - System Evaluation\t\t|");
		System.out.println("|\t-1: Quit\t\t\t\t|");
		System.out.println("|-----------------------------------------------|");
		System.out.print("$");
	}

	private static void showMenuPartOne() {
		System.out.println("|------------------------------------------------|");
		System.out.println("|Choose One of the Sub-Sections:\t\t |");
		System.out.println("|\t1: Create Index (Positional and Bigram)\t |");
		System.out.println("|\t2: Add a Document\t\t\t |");
		System.out.println("|\t3: Remove a Document\t\t\t |");
		System.out.println("|\t4: View Postings List of a Word\t\t |");
		System.out.println("|\t5: Compress Index\t\t\t |");
		System.out.println("|\t6: Decompress Index\t\t\t |");
		System.out.println("|\t7: Get all possible words for a wildcard |");
		System.out.println("|\t-1: Back\t\t\t\t |");
		System.out.println("|------------------------------------------------|");
		System.out.print("$");
	}

	private static void showMenuPartTwo() {
		System.out
				.println("|-------------------------------------------------------|");
		System.out.println("|Choose One of the Sub-Sections:\t\t\t|");
		System.out.println("|\t1: Enter Query (wildcard and quote allowed)\t|");
		System.out.println("|\t2: View the Modified Query\t\t\t|");
		System.out.println("|\t3: Choose Scoring Method\t\t\t|");
		System.out.println("|\t4: View Sorted List of Retrieved Documents\t|");
		System.out.println("|\t5: View the Document\t\t\t\t|");
		System.out.println("|\t-1: Back\t\t\t\t\t|");
		System.out
				.println("|-------------------------------------------------------|");
		System.out.print("$");
	}

	private static void showMenuPartThree() {
		System.out.println("|---------------------------------------|");
		System.out.println("|Choose One of the Sub-Sections:\t|");
		System.out.println("|\t1: Choose Scoring Method\t|");
		System.out.println("|\t2: Evaluate One Query\t\t|");
		System.out.println("|\tall: Complete Evaluation\t|");
		System.out.println("|\t-1: Back\t\t\t|");
		System.out.println("|---------------------------------------|");
		System.out.print("$");
	}

	public static void main(String[] args) throws IOException {
		Scanner scan = new Scanner(System.in);
		Index index = new Index();
		Query q = new Query();
		String userQuery = "";
		String modifQ = "";
		VectorScorer scoring = new Score_LNC_LTC();
		TreeSet<Pair<Integer, Double>> total = new TreeSet<Pair<Integer, Double>>();
		showMainMenu();
		String input = "";
		MAIN: while (true) {
			input = scan.nextLine();
			switch (input) {
			case "1":
				showMenuPartOne();
				PARTONE: while (true) {
					input = scan.nextLine();
					switch (input) {
					case "1":
						System.out.println("We're indexing. Please wait...");
						long t1 = System.currentTimeMillis();
						index.createPosIndex();
						long t2 = System.currentTimeMillis();
						System.out.println("Positional index created. It took "
								+ (t2 - t1) / 1000.0 + " seconds.");
						Index.createBigramIndex();
						System.out.println("Bigram index created.");
						break;
					case "2":
						System.out.println("Enter document ID to add:");
						int docIDadd = Integer.parseInt(scan.nextLine());
						index.addDoc(docIDadd);
						System.out.println("Document " + docIDadd + " added.");
						break;
					case "3":
						System.out.println("Enter document ID to remove:");
						int docIDrem = Integer.parseInt(scan.nextLine());
						index.removeDoc(docIDrem);
						System.out
								.println("Document " + docIDrem + " removed.");
						break;
					case "4":
						System.out
								.println("Enter a word to retrieve the posting list:");
						System.out
								.println("\t(No need to enter the tokenized word.)");
						String temp = scan.nextLine();

						System.out.println(temp + " -> "
								+ index.getPostingList(temp));
						break;
					case "5":
						System.out
								.println("Enter filename for compressing the index: ");
						t1 = System.currentTimeMillis();
						Compression.compressIndex(scan.nextLine());
						t2 = System.currentTimeMillis();
						System.out
								.println("Index compressed successfully. It took "
										+ (t2 - t1) / 1000.0 + " seconds.");
						break;
					case "6":
						System.out
								.println("Enter filename for loading the index: ");
						t1 = System.currentTimeMillis();
						Decompression.decompressIndex(scan.nextLine());
						t2 = System.currentTimeMillis();
						System.out
								.println("Index loaded successfully. It took "
										+ (t2 - t1) / 1000.0 + " seconds.");
						break;
					case "7":
						System.out
								.println("Enter a string and a wildcard in the end:");
						String inputWildCard = scan.nextLine();
						System.out.println(Index.trailingWordsForWildcard(
								inputWildCard).keySet());
						break;
					case "-1":
						break PARTONE;
					default:
						System.out.println("Enter a valid input.");
						break;
					}
					showMenuPartOne();
				}
				showMainMenu();
				break;
			case "2":
				showMenuPartTwo();
				PARTTWO: while (true) {
					input = scan.nextLine();
					switch (input) {
					case "1":
						System.out.println("Enter query:");
						userQuery = scan.nextLine();
						modifQ = q.understandQuery(userQuery);
						System.out.println("Query received.");
						break;
					case "2":
						System.out.println("Modified Query: " + modifQ);
						break;
					case "3":
						scoring = chooseScoringMethod(scan, scoring);
						break;
					case "4":
						long startTime = System.currentTimeMillis();
						findRankedResults(index, q, modifQ, scoring, total);
						long stopTime = System.currentTimeMillis();
						System.out
								.println(total.size() + " results in "
										+ (stopTime - startTime) / 1000.0
										+ " seconds:");
						Integer[] res = sortByScore(total);
						for (int i = 0; i < res.length; i++) {
							System.out.print("[" + (i + 1) + ": " + res[i]
									+ "]\t\t\t");
							if ((i + 1) % 5 == 0)
								System.out.println();
						}
						System.out.println();
						break;
					case "5":
						System.out.println("Enter document ID:");
						showDocContent(scan);
						break;
					case "-1":
						break PARTTWO;
					default:
						System.out.println("Enter a valid input.");
						break;
					}
					showMenuPartTwo();
				}
				showMainMenu();
				break;
			case "3":
				showMenuPartThree();
				PARTTHREE: while (true) {
					Evaluator map = new MAP();
					Evaluator rprecision = new RPrecision();
					Evaluator fmeasure = new FMeasure();
					input = scan.nextLine();
					switch (input) {
					case "1":
						scoring = chooseScoringMethod(scan, scoring);
						break;
					case "2":
						// F-Measure MAP R-Precision
						System.out.println("Enter your desired query number: ");
						int queryNum = Integer.parseInt(scan.nextLine());
						modifQ = q.understandQuery(Evaluator.getQueries().get(
								queryNum));
						Integer[] tempRes = findRankedResults(index, q, modifQ,
								scoring, total);

						System.out
								.printf("Q#%2d\t= [RPrecision=%.3f\tF-Measure=%.3f\t\tMAP=%.3f]\n",
										queryNum,
										rprecision.evalQuery(tempRes, queryNum),
										fmeasure.evalQuery(tempRes, queryNum),
										map.evalQuery(tempRes, queryNum));
						break;
					case "all":
						// All measures
						double FMAvg = 0;
						double RPAvg = 0;
						double MAPAvg = 0;
						double FMTemp;
						double RPTemp;
						double MAPTemp;
						for (int qry = 1; qry <= 93; qry++) {
							userQuery = Evaluator.getQueries().get(qry);
							modifQ = q.understandQuery(userQuery);
							tempRes = findRankedResults(index, q, modifQ,
									scoring, total);

							FMTemp = fmeasure.evalQuery(tempRes, qry);

							RPTemp = rprecision.evalQuery(tempRes, qry);

							MAPTemp = map.evalQuery(tempRes, qry);

							System.out
									.printf("Q#%2d\t= [RPrecision=%.3f\tF-Measure=%.3f\t\tMAP=%.3f]\n",
											qry, RPTemp, FMTemp, MAPTemp);

							FMAvg += FMTemp;
							MAPAvg += MAPTemp;
							RPAvg += RPTemp;
						}
						System.out
								.println("=====================================================");
						System.out
								.printf("Avg\t= [RPrecision=%.3f\tF-Measure=%.3f\t\tMAP=%.3f]\n",
										RPAvg / 93.0, FMAvg / 93.0,
										MAPAvg / 93.0);
						break;
					case "-1":
						break PARTTHREE;
					default:
						System.out.println("Enter a valid input.");
						break;
					}
					showMenuPartThree();
				}
				showMainMenu();
				break;
			case "-1":
				break MAIN;
			default:
				System.out.println("Enter a valid input.");
				break;
			}
		}
		scan.close();
	}

	private static Integer[] findRankedResults(Index index, Query q,
			String modifQ, VectorScorer scoring,
			TreeSet<Pair<Integer, Double>> total) {
		total.clear();
		HashSet<Integer> docsRetrievedForPhrases = new HashSet<Integer>();

		for (int queryNum = 0; queryNum < q.getAllDifferentQueries().size(); queryNum++) {
			docsRetrievedForPhrases.clear();
			modifQ = q
					.understandQuery(q.getAllDifferentQueries().get(queryNum));
			if (Query.getPartsExact().size() > 0) {

				// first find docs for exact parts for exact
				// match
				for (int i = 0; i < Query.getPartsExact().size(); i++) {
					if (i == 0) {
						docsRetrievedForPhrases.addAll(index
								.findDocsIncludingPhrase(Query.getPartsExact()
										.get(i)));
					} else {
						docsRetrievedForPhrases.retainAll(index
								.findDocsIncludingPhrase(Query.getPartsExact()
										.get(i)));
					}
				}

				// then, score them using all parts
				String tempQuery = modifQ.replaceAll("\"", "");
				ArrayList<String> queryTerms = new ArrayList<String>();
				queryTerms.addAll(Arrays.asList(tempQuery.split(" ")));
				SortedSet<Entry<Integer, Double>> docsByScore = scoring
						.getDocsByScore(queryTerms, new ArrayList<Integer>(
								docsRetrievedForPhrases));

				for (Entry<Integer, Double> e : docsByScore) {
					double findCorresScore = containsDocGetScore(total,
							new Pair<Integer, Double>((int) e.getKey(),
									(double) e.getValue()));
					if (findCorresScore != -1) {
						if (findCorresScore < (double) e.getValue()) {
							total.remove(new Pair<Integer, Double>((int) e
									.getKey(), findCorresScore));
							total.add(new Pair<Integer, Double>((int) e
									.getKey(), (double) e.getValue()));
						}
					} else {
						total.add(new Pair<Integer, Double>((int) e.getKey(),
								(double) e.getValue()));
					}
				}
				// total is all doc-id that contain all phrase
				// queries
			} else { // no exact part
				ArrayList<String> queryTerms = new ArrayList<String>();
				queryTerms.addAll(Arrays.asList(modifQ.split(" ")));
				SortedSet<Entry<Integer, Double>> docsByScore = scoring
						.getDocsByScore(queryTerms, null);
				for (Entry<Integer, Double> e : docsByScore) {
					double findCorresScore = containsDocGetScore(total,
							new Pair<Integer, Double>((int) e.getKey(),
									(double) e.getValue()));
					if (findCorresScore != -1) {
						if (findCorresScore < (double) e.getValue()) {
							total.remove(new Pair<Integer, Double>((int) e
									.getKey(), findCorresScore));
							total.add(new Pair<Integer, Double>((int) e
									.getKey(), (double) e.getValue()));
						}
					} else {
						total.add(new Pair<Integer, Double>((int) e.getKey(),
								(double) e.getValue()));
					}
				}
			}
		}
		return sortByScore(total);
	}

	private static VectorScorer chooseScoringMethod(Scanner scan,
			VectorScorer scoring) {
		System.out.println("Choose a scoring method:");
		System.out.println("\t1: lnn.ltn");
		System.out.println("\t2: lnc.ltc");
		int scoringMethod = Integer.parseInt(scan.nextLine());
		if (scoringMethod == 1) {
			scoring = new Score_LNN_LTN();
			System.out.println("Scoring method selected: lnn.ltn.");
		} else if (scoringMethod == 2) {
			scoring = new Score_LNC_LTC();
			System.out.println("Scoring method selected: lnc.ltc.");
		}
		return scoring;
	}

	private static void showDocContent(Scanner scan)
			throws FileNotFoundException, IOException {
		int documentID = Integer.parseInt(scan.nextLine());
		String pathToDoc = "C:\\Users\\Pardis\\workspace\\MIR_SYSTEM\\NPL-v2\\NPL\\Documents\\"
				+ documentID;
		BufferedReader in = new BufferedReader(new FileReader(pathToDoc));
		String line;
		while ((line = in.readLine()) != null) {
			System.out.println(line);
		}
		System.out.println();
		in.close();
	}

	@SuppressWarnings("unchecked")
	private static Integer[] sortByScore(TreeSet<Pair<Integer, Double>> total) {
		Pair<Integer, Double>[] pairs = new Pair[total.size()];
		Integer[] finalResult = new Integer[total.size()];

		int i = 0;
		for (Pair<Integer, Double> p : total) {
			pairs[i] = new Pair<Integer, Double>(p.getLeft(), p.getRight());
			i++;
		}
		for (int j = 0; j < pairs.length; j++) {
			for (int j2 = j; j2 < pairs.length; j2++) {
				if ((double) pairs[j].getRight() < (double) pairs[j2]
						.getRight()) {
					// swap!
					int jLeft = (int) pairs[j].getLeft();
					double jRight = (double) pairs[j].getRight();

					pairs[j].setLeft(pairs[j2].getLeft());
					pairs[j].setRight(pairs[j2].getRight());
					pairs[j2].setLeft(jLeft);
					pairs[j2].setRight(jRight);
				}
			}
		}

		for (int j = 0; j < finalResult.length; j++) {
			finalResult[j] = (int) pairs[j].getLeft();
		}

		return finalResult;
	}

	private static double containsDocGetScore(
			TreeSet<Pair<Integer, Double>> total, Pair<Integer, Double> p) {
		for (Pair<Integer, Double> pin : total) {
			int pinLeft = pin.getLeft();
			int pLeft = p.getLeft();
			if (pinLeft == pLeft) {
				return pin.getRight();
			}
		}
		return -1;
	}

}
