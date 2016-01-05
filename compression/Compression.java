package compression;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import core.Index;
import utilities.Pair;

public class Compression {

	public static void main(String[] args) {
		System.out.println(decimalToVB(100000));
	}

	private static String decimalToVB(int decimalInput) {

		String toBinary = Integer.toString(decimalInput, 2);
		ArrayList<String> substrs = new ArrayList<String>();

		String zeros = "";
		for (int i = 0; i < 7 * (toBinary.length() / 7 + 1) - toBinary.length(); i++) {
			zeros += "0";
		}
		toBinary = zeros + toBinary;

		for (int i = 0; i < toBinary.length() / 7; i++) {
			substrs.add(toBinary.substring(i * 7, (i + 1) * 7));
		}
		String finalString = "";
		for (int i = 0; i < substrs.size(); i++) {
			if (i == (substrs.size() - 1))
				finalString += "1" + substrs.get(i);
			else
				finalString += "0" + substrs.get(i);
		}
		return finalString;
	}

	private static String decimalToGamma(int decimalInput) {

		String toBinary = Integer.toString(decimalInput, 2);

		if (toBinary.charAt(0) == '1')
			toBinary = toBinary.substring(1);

		String finalEncodedString = "";
		for (int i = 0; i < toBinary.length(); i++) {
			finalEncodedString += "1";
		}
		finalEncodedString += "0";
		finalEncodedString += toBinary;
		return finalEncodedString;
	}

	public static void compressIndex(String filename) throws IOException {
		FileWriter fileWriter = new FileWriter(filename);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		// compression here!
		for (String term : Index.getDictionarySorted()) {
			bufferedWriter.write(term);
			bufferedWriter.newLine();

			ArrayList<Pair<Integer, Integer>> tempAL = new ArrayList<Pair<Integer, Integer>>(
					Index.getPosIndex().get(term));
			bufferedWriter.write(decimalToVB(tempAL.get(0).getLeft()));

			int currDocID = tempAL.get(0).getLeft();
			for (int i = 1; i < tempAL.size(); i++) {
				if (currDocID != tempAL.get(i).getLeft()) {
					bufferedWriter.write(decimalToVB(tempAL.get(i).getLeft()
							- currDocID));
				}
				currDocID = tempAL.get(i).getLeft();
			}
			bufferedWriter.write(decimalToVB(100000));

			bufferedWriter.write(decimalToGamma(tempAL.get(0).getRight()));

			currDocID = tempAL.get(0).getLeft();
			int currPosID = tempAL.get(0).getRight();
			for (int i = 1; i < tempAL.size(); i++) {
				if (tempAL.get(i).getLeft().equals(currDocID)) {
					bufferedWriter.write(decimalToGamma(tempAL.get(i)
							.getRight() - currPosID));
				} else {
					bufferedWriter.write(decimalToGamma(100000));

					bufferedWriter.write(decimalToGamma(tempAL.get(i)
							.getRight()));

				}
				currPosID = tempAL.get(i).getRight();
				currDocID = tempAL.get(i).getLeft();
			}
			bufferedWriter.write(decimalToGamma(100000));

			bufferedWriter.newLine();
		}

		bufferedWriter.close();
	}
}
