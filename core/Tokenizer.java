package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Tokenizer
 *
 * The Tokenizer class tokenizes a signle document or a set of documents which
 * have names in format "doc[i]" where [i] is the document identification
 * number.
 * 
 * @author Amin Nejatbakhsh <nejatbakhsh.amin@gmail.com>
 */

public class Tokenizer {
	private Scanner scanner;
	private int fileNumber = -1;
	private int docNumber = 0;
	private int tokenNumber = 0;
	private File[] listOfFiles;
	private HashMap<String, Boolean> stopWords;
	private Stemmer stemmer;
	private TokenizationMode tokenizationMode;

	private static final String swPath = "C:\\Users\\Pardis\\workspace\\MIR_SYSTEM\\NPL-v2\\NPL\\stopwords";

	// private static String urlRegex =
	// "((((https?|ftp|file)://)|(www\\.))[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";
	private static String wordRegex = "([a-zA-Z]+|[0-9]+(\\.[0-9]+)?)((\\.|[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,}))?";
	// private static String emailRegex =
	// "([_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,}))";

	private Pattern pattern1;

	public int getFileNumber() {
		return fileNumber;
	}

	/**
	 * Returns token number which is being tokenized.
	 * 
	 * @return Token number (int)
	 */
	public int getTokenNumber() {
		return tokenNumber;
	}

	/**
	 * Returns document id which is being tokenized.
	 * 
	 * @return Document id (int)
	 */
	public int getDocNumber() {
		return docNumber;
	}

	/**
	 * @param source
	 *            Folder path in case of using Tokenizer on a number of docs and
	 *            a file path otherwise.
	 * @param allDocs
	 *            True is case of using Tokenizer on a number of docs and false
	 *            otherwise.
	 */
	public Tokenizer(String source, TokenizationMode tokenizationMode) {
		this.tokenizationMode = tokenizationMode;

		stemmer = new Stemmer();
		stopWords = new HashMap<>();
		createStopWordsMap();
		pattern1 = Pattern.compile(wordRegex);

		if (tokenizationMode == TokenizationMode.FolderFiles) {
			File folder = new File(source);
			listOfFiles = folder.listFiles();
			initializeNewFile();
		} else if (tokenizationMode == TokenizationMode.SingleFile) {
			try {
				scanner = new Scanner(new File(source));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			scanner = new Scanner(source);
		}

	}

	private void createStopWordsMap() {
		Scanner stScanner = null;
		try {
			stScanner = new Scanner(new File(swPath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		while (stScanner.hasNext()) {
			if (stScanner.findInLine("#") != null) {
				stScanner.nextLine();
				continue;
			}
			stopWords.put(stScanner.next(), true);
		}

		stScanner.close();
	}

	/**
	 * Returns next token after stemming and checking whether current token is a
	 * stop word or not.
	 * <p>
	 * In case of using Tokenizer on a number of files, when tokenizing one file
	 * finishes automatically continues tokenizing on the next file calling
	 * initilizeNewFile() method.
	 * 
	 * @return Next token (String) when there exist one, or "$" when tokenizing
	 *         finishes.
	 */
	public String nextToken() {
		String result;
		while (true) {
			result = scanner.findInLine(pattern1);
			if (result == null) {
				if (!scanner.hasNext()) {
					if (this.tokenizationMode == TokenizationMode.FolderFiles
							&& initializeNewFile()) {
						return nextToken();
					} else {
						scanner.close();
						tokenNumber++;
						return "$";
					}
				} else {
					scanner.nextLine();
					return nextToken();
				}
			} else {
				break;
			}
		}

		if (result.equals(".") || stopWords.get(result) != null) {
			return nextToken();
		}
		tokenNumber++;
		return filterToken(result);
	}

	private String filterToken(String token) {
		token = token.toLowerCase();
		for (int i = 0; i < token.length(); i++) {
			stemmer.add(token.charAt(i));
		}
		stemmer.stem();
		return stemmer.toString();
	}

	private boolean initializeNewFile() {
		if (scanner != null) {
			scanner.close();
		}
		fileNumber++;
		tokenNumber = 0;

		if (fileNumber >= listOfFiles.length) {
			return false;
		}

		String filePath = listOfFiles[fileNumber].getAbsolutePath();
		docNumber = Integer.parseInt(filePath.substring(filePath
				.lastIndexOf("\\") + 1));
		try {
			scanner = new Scanner(new File(filePath)); // Scanner on whole file
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}

enum TokenizationMode {
	FolderFiles, SingleFile, Query,
}