/*
 * Tyler Lorella - Compressed Literature 2
 * TCSS 342 - Spring 2019
 */

import java.util.ArrayList;

public class CodingTree {
	
	private class Element implements Comparable<Element> {

		//public char character;
		public String word;
		
		public String code = "";

		public int weight;

		public Element left;

		public Element right;

		public Element(final String theWord, final int theWeight) {
			this.word = theWord;
			this.weight = theWeight;
		}

		public Element(final int theWeight, final Element theLeft, final Element theRight) {
			this.weight = theWeight;
			this.left = theLeft;
			this.right = theRight;
		}

		public int compareTo(final Element theOtherElement) {
			if (this.weight < theOtherElement.weight) return -1;
			else if (this.weight == theOtherElement.weight) return 0;
			else return 1;
		}
		
		public void setCodes() {
			//sets all the codes
			if (this.left != null) {
				this.left.code = this.code + "0";
				this.left.setCodes();
			}
			if (this.right != null) {
				this.right.code = this.code + "1";
				this.right.setCodes();
			}
		}
	}

	/**
	 * A character that shouldn't appear in normal text files.
	 */
	private static final String IGNORE_CODE = Character.toString(Character.CONTROL);

	/**
	 * Maps a word => string representation of a binary encoding.
	 */
	public final MyHashTable<String, String> codes = new MyHashTable<String, String>(MyHashTable.DEFAULT_CAPACITY);
	
	/**
	 * Maps a word => frequency count of that word.
	 */
	public final MyHashTable<String, Integer> frequencyMap = new MyHashTable<String, Integer>(MyHashTable.DEFAULT_CAPACITY);

	/**
	 * The String representation of the binary encoding.
	 */
	public final String stringRep;

	/**
	 * Byte Array of the binary encoding.
	 */
	public final ArrayList<Byte> bits = new ArrayList<Byte>();


	/**
	 * 
	 * @param message The string to be compressed using huffman-compression.
	 */
	public CodingTree(final String message) {
		
		StringBuilder wordBuilder = new StringBuilder();
		
		for (int index = 0; index < message.length(); index++) {
			char character = message.charAt(index);
			String charString = Character.toString(character);
			
			if (isWordChar(character)) {
				wordBuilder.append(character);
			} else {
				String word = wordBuilder.toString();
				if (!word.equals("")) {
					frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
				}
				frequencyMap.put(charString, frequencyMap.getOrDefault(charString, 0) + 1);
				//frequencyMap.put(Character.toString(character), frequencyMap.getOrDefault(Character.toString(character), 0) + 1);
				wordBuilder = new StringBuilder();
			}
			
			if (index == message.length()-1 && !wordBuilder.toString().equals("")) {
				String word = wordBuilder.toString();
				frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
				wordBuilder = new StringBuilder();
			}
		}
		
		//add our ignore code so when we reach the end of decode there won't be an accidental mapping.
		frequencyMap.put(IGNORE_CODE, 1);

		final MyPriorityQueue<Element> minHeap = new MyPriorityQueue<Element>();
		ArrayList<Element> nodeList = new ArrayList<Element>();
		
		for (String key: frequencyMap.keySet()) {
			Element temp = new Element(key, frequencyMap.get(key));
			minHeap.add(temp);
			nodeList.add(temp);
		}
		
		
		//Condense all nodes in the heap into a single tree O(m). visit every element at least once, then access our
		//condensed node a few times. so at worst it's remove two, add one. so O(m).
		while (minHeap.size() > 1) {
			Element child1 = minHeap.remove();
			Element child2 = minHeap.remove();			
			minHeap.offer(new Element((child1.weight + child2.weight), child1, child2));	
		}

		final Element tree = minHeap.remove();
		tree.setCodes();
		
		for (Element element: nodeList) {
			codes.put(element.word, element.code);
		}

		//O(n)?
		StringBuilder encodedString = new StringBuilder();
		for (int index = 0; index < message.length(); index++) {
			char character = message.charAt(index);
			String charString = Character.toString(character);
			//String character = Character.toString(message.charAt(index));
			
			if (isWordChar(character)) {
				wordBuilder.append(character);
			} else {
				String word = wordBuilder.toString();
				if (!word.equals("")) {
					encodedString.append(codes.get(word));
				}
				encodedString.append(codes.get(charString));
				wordBuilder = new StringBuilder();
			}
			
			if (index == message.length()-1 && !wordBuilder.toString().equals("")) {
				encodedString.append(codes.get(wordBuilder.toString()));
			}
		}
		

		
		encodedString.append(codes.get(IGNORE_CODE));
		

		//we can remove the ignore code from our map, the decode method will encounter a unique binary number that won't
		//represent anything.
		codes.remove(IGNORE_CODE);

		//this is the string representation of the binary encoding.
		stringRep = encodedString.toString();

		//converting stringRep to binary data
		for (int index = 0; index < stringRep.length(); index++) {
			final StringBuilder toByte = new StringBuilder();

			//Append our String bits into the byteString.
			for (int i = 0; i < 8; i++) {
				if (index >= stringRep.length()) {
					//We want to add some 0s at the end to ensure we have a full byte with 0s at the end.
					toByte.append('0');
				} else {
					toByte.append(stringRep.charAt(index));
				}
				index++;
			}
			index--;

			bits.add(string2Byte(toByte.toString()));
		}
	}

	public static String decode(final String bits, final MyHashTable<String, String> codes) {
		//iterator through bits, when we find a character, add it to a code string, every time we add something
		//to the code string we check if it's an entry within the codes, until we have found a match. Then we decode and then repeat
		final MyHashTable<String, String> sedoc = new MyHashTable<String, String>(MyHashTable.DEFAULT_CAPACITY);

		for (String word: codes.keySet()) {
			sedoc.put(codes.get(word), word);
		}

		final StringBuilder decodedString = new StringBuilder();
		StringBuilder codedWord = new StringBuilder();

		for (int index = 0; index < bits.length(); index++) {
			codedWord.append(bits.charAt(index));
			
			if (sedoc.containsKey(codedWord.toString())) {
				String word = sedoc.get(codedWord.toString());
				decodedString.append(word);
				codedWord = new StringBuilder();
			}
		}

		return decodedString.toString();
	}

	public static byte string2Byte(final String toConvert) {
		byte toReturn = (byte) (0b0000000);
		if (toConvert.length() != 8) throw new NumberFormatException(toConvert + " is not 8 digits!");
		for (int index = 0; index < 8; index++) {
			if (toConvert.charAt(index) == '1') {
				int multValue = 8 - index;
				toReturn = (byte) (toReturn +  ((byte) ((0b00000001) * (Math.pow(2, multValue - 1)))));
			}
		}
		return toReturn;
	}
	
	public static boolean isWordChar(char character) {
		//boolean toReturn = false;
		if ((character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z') || (character >= '0' && character <= '9') || character == '-' || character == '\'') {
			return true;
		}
		return false;
	}
}

