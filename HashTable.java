import java.util.*;

class HashTable {

	// the internal array for the hash table
	private List<Integer>[] table;

	@SuppressWarnings("unchecked")
	public HashTable(int size) {
		table = new ArrayList[size];
	}

	public void put(int value, int hashcode) {
		// if the position in the array is empty create a new array list
		// for that position
		if (table[hashcode] == null) {
			table[hashcode] = new ArrayList<Integer>();
		}
		// store the itemset in the arraylist of that position
		table[hashcode].add(value);
	}

	public List<Integer> get(int hashcode){
		if (table[hashcode]!=null)
		   return table[hashcode];
		else return new ArrayList<Integer>();
	}

}
