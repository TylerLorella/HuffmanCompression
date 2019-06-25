/*
 * Tyler Lorella - Compressed Literature 2
 * TCSS 342 - Spring 2019
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MyHashTable<K, V> {

	private class Probogram {

		private final ArrayList<Integer> list;

		private int maxProbe = 0;

		public Probogram() {
			list = new ArrayList<Integer>();
		}

		public void incrementBucketValue(Integer bucketSize) {
			while (list.size() < bucketSize) {
				list.add(0);
				maxProbe++;
			}
			list.set(bucketSize - 1, list.get(bucketSize - 1) + 1);


		}

		public int getMaxProbe() {
			return maxProbe;
		}

		public double getAverageLinearProb() {
			int total = 0;
			int probes = 0;
			for (int i = 0; i < maxProbe; i++) {
				probes = probes + list.get(i);
				total = total + (list.get(i)) * (i+1);
			}
			return ((double) total) / ((double) probes);
		}

		public String toString() {
			return list.toString();
		}

	}

	private class KeyValuePair {

		private K key;

		private V value;

		public KeyValuePair(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return this.key;
		}

		public V getValue() {
			return this.value;
		}

		public String toString() {
			return key.toString() + "=" + value.toString();
		}
	}

	public final static int DEFAULT_CAPACITY = 32768;

	private final ArrayList<KeyValuePair> table;

	private final Probogram histogram;

	private final int capacity;

	private int size = 0;

	public MyHashTable(int capacity) {
		this.capacity = capacity;
		table = new ArrayList<KeyValuePair>(capacity);
		for(int i = 0; i < capacity; i++) {
			table.add(null);
		}
		histogram = new Probogram();
	}

	public void put(K searchKey, V newValue) {
		Objects.requireNonNull(searchKey);
		Objects.requireNonNull(newValue);


		boolean linearProbing = true;
		int hash = this.hash(searchKey);
		KeyValuePair entry = new KeyValuePair(searchKey, newValue);

		if (table.get(hash) == null) {
			table.set(hash, entry);
			this.size++;
		} else if (table.get(hash).getKey().equals(searchKey)) {
			table.set(hash, entry);
		} else if (linearProbing) {
			//int probeCounter = 0;
			hash++;
			for (int i = 0; i < this.capacity; i++) {
				//probeCounter++;
				if (hash >= this.capacity) hash = 0;
				if (table.get(hash) == null) {
					table.set(hash, entry);
					this.size++;
					break;
				} else if (table.get(hash).getKey().equals(searchKey)) {
					table.set(hash, entry);
					break;
				}
				hash++;
			}
		}

	}

	public V get(K searchKey) {
		Objects.requireNonNull(searchKey);
		int hash = this.hash(searchKey);

		if (table.get(hash) == null) {
			return null;
		} else if (table.get(hash).getKey() != null && table.get(hash).getKey().equals(searchKey)) {
			return table.get(hash).getValue();
		}

		int index = this.probeSearch(searchKey);
		if (index != -1) {
			return table.get(index).getValue();
		}

		return null;
	}

	public V getOrDefault(K searchKey, V defaultValue) {
		if (this.get(searchKey) == null) {
			return defaultValue;
		}
		return this.get(searchKey);
	}

	public boolean containsKey(K searchKey) {
		Objects.requireNonNull(searchKey);

		int hash = this.hash(searchKey);

		if (table.get(hash) == null || table.get(hash).getKey() == null) {
			return false;
		} else if (table.get(hash).getKey().equals(searchKey)) {
			return true;
		} else if (this.probeSearch(searchKey) != -1) {
			return true;
		}

		return false;
	}

	public Set<K> keySet() {
		final Set<K> keySet = new HashSet<K>();
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i) != null && table.get(i).getKey() != null) {
				keySet.add(table.get(i).getKey());
			}
		}
		return keySet;
	}

	public void remove(K searchKey) {
		int hash = this.hash(searchKey);
		if (table.get(hash) == null) return;

		else if (this.table.get(hash).getKey().equals(searchKey)) {
			table.set(hash, new KeyValuePair(null, null));
		} else {
			int index = probeSearch(searchKey);
			if (index == -1) return;
			table.set(index, new KeyValuePair(null, null));
		}

		//didn't fully remove the element, so a flag is left behind.
	}

	public void stats() {
		System.out.println("Number of Entries: " + this.size);
		System.out.println("Number of Buckets: " + this.capacity);
		System.out.println("Histogram of Probes: " + this.histogram.toString());
		System.out.println("Fill Percentage: " + (((double) this.size) / ((double) this.capacity)) * 100 + "%");
		System.out.println("Max Linear Probe: " + this.histogram.getMaxProbe());
		System.out.println("Average Linear Probe: " + this.histogram.getAverageLinearProb());
	}

	public int size() {
		return this.size;
	}

	public String toString() {
		//return table.toString();
		if (this.size == 0) return "";
		
		StringBuilder string = new StringBuilder();
		string.append("{");
		
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i) != null && table.get(i).getKey() != null) {
				string.append(table.get(i).toString());
				string.append(", ");
			}
		}
		
		string.replace(string.lastIndexOf(", "), string.length(), "}");
		return string.toString();
	}

	private int hash(K key) {
		//return Math.abs((key.hashCode()%this.capacity));	
		int hash = key.hashCode();
		int classHash = key.getClass().hashCode();
		return Math.abs((hash + classHash))%this.capacity;			
	}


	//returns the index of the searchKey or returns -1 if it's not present.
	private int probeSearch(K searchKey) {
		Objects.requireNonNull(searchKey);
		return probeSearchLine(searchKey);
	}

	private int probeSearchLine(K searchKey) {
		int toReturn = -1;
		int hash = this.hash(searchKey);
		int probeCounter = 0;

		//hash was already checked...
		int indexToCheck = hash + 1;
		if (indexToCheck >= this.capacity) indexToCheck = 0;

		while (true) {
			probeCounter++;
			if (table.get(indexToCheck) == null || hash == indexToCheck) {
				break;
			} else if (table.get(indexToCheck).getKey() != null && table.get(indexToCheck).getKey().equals(searchKey)) {
				toReturn = indexToCheck;
				break;
			}
			indexToCheck++;
			if (indexToCheck >= this.capacity) indexToCheck = 0;
		}
		histogram.incrementBucketValue(probeCounter);
		return toReturn;
	}


}
