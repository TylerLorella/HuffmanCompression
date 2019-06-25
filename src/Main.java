/*
 * Tyler Lorella - Compressed Literature 2
 * TCSS 342 - Spring 2019
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {
		//testCodingTree();
		//testMyPriorityQueue();
		//testMyHashTable();

		//final String filePath = "./src/Test.txt";
		final String filePath = "./src/WarAndPeace.txt";

		System.out.println("Beginning encoding...");
		long startTime = System.nanoTime();

		String toEncode = readTextFileBuffered(filePath);		
		CodingTree encode = new CodingTree(toEncode.toString());

		writeByteFile("./src/compressed.txt", encode.bits);
		writeTextBufferedFile("./src/codes.txt", encode.codes.toString());
		//writeTextBufferedFile("./src/frequencyMap.txt", encode.frequencyMap.toString());

		long endTime = System.nanoTime();
		int timeMS = (int) ((endTime - startTime) / 1_000_000);

		System.out.println("Encoding complete, runtime: " + timeMS + " milliseconds");
		int ogSize = toEncode.toString().getBytes().length/1024 + 1;
		int compSize = encode.bits.size()/1024 + 1;
		double ratio = (((double) compSize / (double) ogSize) * 100);
		System.out.println("Original File Size: " + ogSize + 
				" KBs, Compressed Size: " + compSize + " KBs \n" + "Compression Ratio: " + String.format("%.2f", ratio) + "%.");


		System.out.println("\nFrequency Map Stats:");
		encode.frequencyMap.stats();
		System.out.println("\nCodes Map Stats:");
		encode.codes.stats();
		System.out.println();


		System.out.println("Beginning decoding...");
		startTime = System.nanoTime();

		byte[] byteList = readByteFile("./src/compressed.txt");
		String codesInput = readTextFileBuffered("./src/codes.txt");

		MyHashTable<String, String> codes = new MyHashTable<String, String>(MyHashTable.DEFAULT_CAPACITY);

		//remove the first {, and last }
		String codesTrim = codesInput.substring(1, codesInput.length() - 1);

		for (String mapping: codesTrim.split(", ")) {
			int lastEqual = mapping.lastIndexOf("=");
			String word = mapping.substring(0, lastEqual);
			String theCode = mapping.substring(lastEqual + 1);
			codes.put(word, theCode);
		}

		//Converting byteList into a string of binary values.
		StringBuilder toDecode = new StringBuilder();
		for (byte aByte: byteList) {
			toDecode.append(byte2String(aByte));
		}


		String toPrint = CodingTree.decode(toDecode.toString(), codes); //reading from text files

		writeTextBufferedFile("./src/Decode.txt", toPrint);
		endTime = System.nanoTime();
		timeMS = (int) ((endTime - startTime) / 1_000_000);
		System.out.println("Decoding complete, runtime: " + timeMS + " milliseconds.");

		//System.out.println("\nDoes original equal the decode?: " + (toEncode.equals(toPrint)));
	}

	/**
	 * Reads a text file containing text characters and returns the a string representation of the content.
	 * @param theFilePath The file path of the file to read.
	 * @return String representation of the content
	 */
	private static String readTextFileBuffered(final String theFilePath) {
		final StringBuilder toEncode = new StringBuilder();
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(theFilePath));
			while (true) {
				int characterNumber = reader.read();
				if (characterNumber == -1) break;
				toEncode.append((char) characterNumber);
			}
			reader.close();
		} catch (final FileNotFoundException e) {
			System.out.println("File not Found");
		} catch (final IOException e) {
			System.out.println("IO exception");
			e.printStackTrace();
		}
		return toEncode.toString();
	}

	/**
	 * Writes a text file containing text characters.
	 * @param theFilePath The file path for the file to be created.
	 * @param toWrite The string to write to the file.
	 * @return returns true if the write was successful.
	 */
	private static boolean writeTextBufferedFile(final String theFilePath, final String toWrite) {
		boolean succesfulWrite = false;
		try {
			final BufferedWriter writer = new BufferedWriter(new FileWriter(theFilePath));
			writer.write(toWrite);
			writer.close();
			succesfulWrite = true;
		} catch (final Exception e) {
			System.out.println("Buffered writer error...");
		}
		return succesfulWrite;
	}

	/**
	 * Writes a text file containing pure binary data that is the encoding for the huffman compression. 
	 * @param theFilePath The file path for the file to be created.
	 * @param theBytes The binary data to write to the file.
	 * @return if the write was successful, true is returned.
	 */
	private static boolean writeByteFile(final String theFilePath, final ArrayList<Byte> theBytes) {
		boolean succesfulWrite = false;
		try {
			FileOutputStream output = new FileOutputStream(new File(theFilePath));

			BufferedOutputStream toFile = new BufferedOutputStream(output);
			byte[] bytes = new byte[theBytes.size()];
			for (int index = 0; index < theBytes.size(); index++) {
				bytes[index] = theBytes.get(index);
			}
			toFile.write(bytes);

			toFile.close();
			output.close();
			succesfulWrite = true;
		} catch (final FileNotFoundException e) {
			System.out.println("Encoded file writting error...");
			e.printStackTrace();	
		} catch (final IOException e) {
			System.out.println("Byte IO error...");
			e.printStackTrace();
		}
		return succesfulWrite;
	}

	/**
	 * Reads a text file containing binary data, returns the data in the form of a byte array.
	 * @param theFilePath The file path of the file to read.
	 * @return byte array representation of the content.
	 */
	private static byte[] readByteFile(final String theFilePath) {
		byte[] byteList;
		try {
			FileInputStream byteReader = new FileInputStream(theFilePath);
			byteList = byteReader.readAllBytes();
			byteReader.close();
			return byteList;
		} catch (final FileNotFoundException e) {
			System.out.println("compressed file not found...");
			e.printStackTrace();
		} catch (final IOException e) {
			System.out.println("IOException, on compressed file...");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Converts a byte into the binary string representation of that byte.
	 * @param theByte The byte to be converted into a string.
	 * @return String representation of a byte, using binary.
	 */
	private static String byte2String(final byte theByte) {
		StringBuilder stringBinary = new StringBuilder();
		for (int mask = 1; mask <= 128; mask = mask * 2) {
			byte byteMask = (byte) ((0b10000000) / mask);
			int result = (theByte & byteMask);
			if (result == 0) {
				stringBinary.append('0');
			} else {
				stringBinary.append('1');
			}
		}
		return stringBinary.toString();
	}

	/**
	 * Basic testing method for the coding tree implementation.
	 */
	@SuppressWarnings("unused")
	private static void testCodingTree() {
		System.out.println("TESTING CODINGTREE");
		String test1 = "1000101";
		Byte test2 = Byte.parseByte(test1, 2);
		String test3 = Integer.toString(test2, 2);
		System.out.println("String test: " + test1 +  " from " + test3);

		String test = "10010011";
		//byte test2 = Byte.parseByte(test);
		byte tes = (byte) Integer.parseInt(test);
		System.out.println(tes);

		CodingTree code = new CodingTree("ANNA HAS A BANANA IN A BANDANA");
		System.out.println("Bytes: " + code.bits + " String Rep: " + code.stringRep + " Codes: " + code.codes);

		System.out.println("Testing String2Binary");
		String value = "01111111";
		System.out.println(CodingTree.string2Byte(value));
		System.out.println(byte2String(CodingTree.string2Byte(value)));

		System.out.println("is A a word character: " + CodingTree.isWordChar('A'));
		System.out.println("is z a word character: " + CodingTree.isWordChar('z'));
		System.out.println("is 1 a word character: " + CodingTree.isWordChar('1'));
		System.out.println("is - a word character: " + CodingTree.isWordChar('-'));
		System.out.println("is ' a word character: " + CodingTree.isWordChar('\''));
		System.out.println("is   a word character: " + CodingTree.isWordChar(' '));
		System.out.println("is @ a word character: " + CodingTree.isWordChar('@'));
		System.out.println("is _ a word character: " + CodingTree.isWordChar('_'));
		System.out.println("TESTING COMPLETE...\n");
	}

	/**
	 * Basic testing method for my priority queue implementation.
	 */
	@SuppressWarnings("unused")
	private static void testMyPriorityQueue() {
		MyPriorityQueue<Integer> test = new MyPriorityQueue<Integer>();
		System.out.println("Size of heap: " + test.size());
		test.add(10);
		System.out.println("Size of heap: " + test.size() + " First element: " + test.peek());
		test.add(9);
		System.out.println("Size of heap: " + test.size() + " First element: " + test.peek());
		for (int i = 8; i >= 0; i--) {
			test.add(i);
		}
		System.out.println("Size of heap: " + test.size() + " First element: " + test.peek());

		for (int i = test.size()-1; i >= 0; i--) {
			System.out.println("Removing current root: " + test.poll() + " Size is: " + test.size());
		}

		System.out.println("Size of heap: " + test.size() + " First ele: " + test.peek());
	}

	/**
	 * Basic testing method for my hash map implementation.
	 */
	@SuppressWarnings("unused")
	private static void testMyHashTable() {
		System.out.println("TESTING MYHASHTABLE\n");
		MyHashTable<String, Integer> test = new MyHashTable<String, Integer>(MyHashTable.DEFAULT_CAPACITY);
		System.out.println("putting stuff into test");
		for (int i = 0; i < 30_000; i++) {
			test.put(("pow" + i), i);
		}
		System.out.println("finished putting stuff into test: " + test.size());
		//System.out.println("Test toString: " + test.toString());
		//System.out.println("Test keySet toString: " + test.keySet().toString());
		System.out.println("Looking for all keys");
		for (String stuff: test.keySet()) {
			//System.out.println("Key: " + stuff + " Mapped to: " + test.get(stuff));
			test.get(stuff);
		}
		System.out.println("found all keys in test.keySet\n");

		MyHashTable<String, Integer> test2 = new MyHashTable<String, Integer>(MyHashTable.DEFAULT_CAPACITY);
		System.out.println("Size of test2: " + test2.size());
		test2.put("po", 320);
		System.out.println("Size of test2, after 1 add: " + test2.size());

		System.out.println("Looking for: pow1, returned: " + test.get("pow1"));
		System.out.println("Contains: NO?, returned: " + test.containsKey("NO"));
		System.out.println("getting: NO, returned: " + test.get("NO"));
		System.out.println("DONE MYHASHTABLE TESTING\n");
		//System.out.println("Hashing: poo2 \n Result: " + test.hash(poo2));

	}

}
