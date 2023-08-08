package src.algorithms.alg2;
import java.util.List;

/**
 * This class represents an itemset (a set of items) implemented as an array of integers
 * with a variable to store the utility count of the itemset.
 * It is designed as an immutable class.
 *
 * <p>Example usage:</p>
 * <pre>
 * Itemset itemset = new Itemset(1, 2, 3);
 * double utility = itemset.getUtility();
 * </pre>
 *
 * @author Philippe Fournier-Viger
 */
public class Itemset {
	/** The array of items */
	private final int[] itemset;

	/** The utility of this itemset */
	private final double utility;

	/**
	 * Constructor
	 */
	public Itemset() {
		itemset = new int[]{};
		utility = 0;
	}

	/**
	 * Constructor
	 *
	 * @param item an item that should be added to the new itemset
	 */
	public Itemset(int item) {
		itemset = new int[]{item};
		utility = 0;
	}

	/**
	 * Constructor
	 *
	 * @param items an array of items that should be added to the new itemset
	 */
	public Itemset(int[] items) {
		this.itemset = items;
		utility = 0;
	}

	/**
	 * Constructor
	 *
	 * @param itemset list of items that should be added to the new itemset
	 * @param utility the utility of the itemset
	 */
	public Itemset(List<Integer> itemset, double utility) {
		this.itemset = new int[itemset.size()];
		int i = 0;
		for (Integer item : itemset) {
			this.itemset[i++] = item;
		}
		this.utility = utility;
	}

	/**
	 * Constructor
	 *
	 * @param itemset array of items that should be added to the new itemset
	 * @param utility the utility of the itemset
	 */
	public Itemset(int[] itemset, double utility) {
		this.itemset = itemset;
		this.utility = utility;
	}

	/**
	 * Get the utility of this itemset
	 *
	 * @return the utility
	 */
	public double getUtility() {
		return utility;
	}

	/**
	 * Get the size of this itemset
	 *
	 * @return the size
	 */
	public int size() {
		return itemset.length;
	}

	/**
	 * Get the item at a given position in this itemset
	 *
	 * @param position the position of the item
	 * @return the item
	 */
	public Integer get(int position) {
		return itemset[position];
	}

	/**
	 * Get the items as an array.
	 * @return the items array
	 */
	public int[] getItems() {
		return itemset;
	}

	/**
	 * Make a copy of this itemset but exclude a given item
	 *
	 * @param itemToRemove the given item
	 * @return the copy
	 */
	public Itemset cloneItemSetMinusOneItem(Integer itemToRemove) {
		// create the new itemset
		int[] newItemset = new int[itemset.length - 1];
		int i = 0;
		// for each item in this itemset
		for (int k : itemset) {
			// copy the item except if it is the item that should be excluded
			if (k != itemToRemove) {
				newItemset[i++] = k;
			}
		}
		return new Itemset(newItemset, utility); // return the copy
	}

	/**
	 * Get a string representation of this transaction
	 *
	 * @return a string
	 */
	public String toString() {
		// use a string buffer for more efficiency
		StringBuilder r = new StringBuilder();
		// for each item, append it to the string buffer
		for (int i = 0; i < size(); i++) {
			r.append(get(i));
			r.append(' ');
		}
		return r.toString(); // return the string
	}
}
