package compression;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import core.Index;
import utilities.Pair;

public class Decompression {

	public static void decompressIndex(String filename) throws IOException {
		FileReader fileReader = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		TreeMap<String, TreeSet<Pair<Integer, Integer>>> newPosIndex = new TreeMap<String, TreeSet<Pair<Integer, Integer>>>();

		String term = "-1";
		// one term!
		while (true) {
			term = bufferedReader.readLine();

			if (term == null)
				break;

			// read8
			char[] read8 = new char[8];
			String accSoFar = "";

			// first gap gap gap ...
			ArrayList<Integer> docGapNumbers = new ArrayList<Integer>();

			// verified!
			READ_DOC_GAPS: while (true) {
				READ_ONE_DOC: while (true) {
					bufferedReader.read(read8);
					accSoFar += (new String(read8)).substring(1);
					if ((new String(read8)).startsWith("1")) {
						if (!accSoFar.equals("000011000011010100000"))
							docGapNumbers.add(Integer.parseInt(accSoFar, 2));
						break READ_ONE_DOC;
					}
				}
				// we have a VB!
				if (accSoFar.equals("000011000011010100000")) {
					break READ_DOC_GAPS;
				}
				accSoFar = "";
				read8 = new char[8];
			}

			// convert <<first gap gap...>> to <<first second...>>
			for (int i = 1; i < docGapNumbers.size(); i++) {
				docGapNumbers.set(i,
						docGapNumbers.get(i - 1) + docGapNumbers.get(i));
			}

			// here: we have finished docs here!
			// we have to read gammas!
			int whichDoc = 0; // index in docGapNumbers
			TreeSet<Pair<Integer, Integer>> posForDoc = new TreeSet<Pair<Integer, Integer>>();
			int currPos = 0;
			while (whichDoc < docGapNumbers.size()) {
				int numToMove = 0;
				char[] bit = new char[1];
				bufferedReader.read(bit);

				while (!(new String(bit)).equals("0")) {
					numToMove++;
					bufferedReader.read(bit);
				}

				// here: we have reached "0"!
				String posSoFar = "";
				for (int i = 0; i < numToMove; i++) {
					bufferedReader.read(bit);
					posSoFar += new String(bit);
				}

				// we have a gamma number here!!
				// add the msb 1!
				posSoFar = "1" + posSoFar;

				if (posSoFar.equals("11000011010100000")) {
					currPos = 0;
					whichDoc++;
				} else { // for the same doc!

					posForDoc.add(new Pair<Integer, Integer>(docGapNumbers
							.get(whichDoc), Integer.parseInt(posSoFar, 2)
							+ currPos));
					currPos = Integer.parseInt(posSoFar, 2);
				}

			}

			newPosIndex.put(term, posForDoc);
			bufferedReader.readLine();
		}
		Index.setPosIndex(newPosIndex);
		Index.createBigramIndex();
		Index.createTfTable();
		bufferedReader.close();
	}
}
