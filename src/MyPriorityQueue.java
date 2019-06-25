/*
 * Tyler Lorella - Compressed Literature 2
 * TCSS 342 - Spring 2019
 */

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;

public class MyPriorityQueue<T extends Comparable<T>> extends AbstractQueue<T>{

	private class Node{

		public T data;

		public Node (final T theData) {
			data = theData;
		}

	}
	
	private class myIterator implements Iterator<T> {

		public myIterator(MyPriorityQueue<T> toIterate) {
			structure = toIterate;
			cursor = toIterate.size();
		}
		
		private final MyPriorityQueue<T> structure;
		
		private int cursor;
		
		@Override
		public boolean hasNext() {
			return (cursor < structure.size());
		}

		@Override
		public T next() {
			return structure.heap.get(cursor++).data;
		}
		
	}

	private final ArrayList<Node> heap;

	public MyPriorityQueue() {
		heap = new ArrayList<Node>();
	}

	@Override
	public boolean offer(final T element) {
		if (element == null) {
			throw new NullPointerException();
		}
		//need to add to the end of the list and then bubbleup
		heap.add(new Node(element));
		bubble(heap.size()-1);

		return true;
	}

	@Override
	public T poll() {
		if (heap.isEmpty()) return null;
		//1st - swap first node and very last node, remove old root node.
		Node toReturn = heap.get(0);
		heap.set(0, heap.get(heap.size()-1));
		heap.remove(heap.size()-1);
		//3rd - sink our new root down, by swapping with the smallest child
		this.sink(0);
		
		return toReturn.data;
	}

	@Override
	public T peek() {
		if (heap.isEmpty()) return null;
		return heap.get(0).data;
	}

	@Override
	public Iterator<T> iterator() {
		return new myIterator(this);
	}

	@Override
	public int size() {
		return heap.size();
	}

	private void bubble(final int toBubbleIndex) {
		int parentIndex = (int) Math.floor( ((double) (toBubbleIndex-1)) / 2);
		if (parentIndex >= 0) { //a parent exists
			//if child is smaller then parent, swap, check if we need to bubble again
			if (heap.get(toBubbleIndex).data.compareTo(heap.get(parentIndex).data) == -1) {
				Node tempNode = heap.get(parentIndex);
				heap.set(parentIndex, heap.get(toBubbleIndex));
				heap.set(toBubbleIndex, tempNode);
				this.bubble(parentIndex);
			}
		}
	}

	private void sink(final int toSinkIndex) {
		if (this.isEmpty()) {
			return;
		}
		int child1Index = (2 * toSinkIndex) + 1;
		boolean oneExists = false;
		int child2Index = (2 * toSinkIndex) + 2;
		boolean twoExists = false;
		int smallestChildIndex;

		//find which of the two children are smaller
		//three compare cases: 1) left smaller, 2) right smaller, 3) they're the same
		//four exists cases: 1) only left exists, 2) only right exists, 3) they neither exist, 4) they both exists

		//Real nasty code, TODO: please come up with something else.
		if (child1Index < heap.size()) {
			oneExists = true;
		}
		if (child2Index < heap.size()) {
			twoExists = true;
		}

		if (!oneExists && !twoExists) {
			smallestChildIndex = toSinkIndex;
		} else if (oneExists && !twoExists) {
			smallestChildIndex = child1Index;
		} else if (!oneExists && twoExists) {
			smallestChildIndex = child2Index;
		} else { //(oneExists && twoExists)
			int comparision = heap.get(child1Index).data.compareTo(heap.get(child2Index).data);
			if (comparision == -1) {
				smallestChildIndex = child1Index;
			} else { //if it's 0 or 1
				smallestChildIndex = child2Index;
			}
		}

		//System.out.println("smallest: " + smallestChildIndex + " This: " + toSinkIndex + " value this: " + heap.toString());
		if (heap.get(toSinkIndex).data.compareTo(heap.get(smallestChildIndex).data) <= 0) { //we already have the min value.
			return;
		} else {
			Node tempNode = heap.get(toSinkIndex);
			heap.set(toSinkIndex, heap.get(smallestChildIndex));
			heap.set(smallestChildIndex, tempNode);
			this.sink(smallestChildIndex);
		}

	}







}
