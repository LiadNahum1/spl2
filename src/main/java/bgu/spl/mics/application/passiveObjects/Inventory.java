package bgu.spl.mics.application.passiveObjects;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Passive data-object representing the store inventory.
 * It holds a collection of {@link BookInventoryInfo} for all the
 * books in the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Inventory {
	private Vector<BookInventoryInfo> books;
	private ConcurrentHashMap<String, Semaphore> semaphores;

	private static class SingletonHolderInv {
		private static Inventory instance = new Inventory();
	}

	private Inventory(){
		this.books = new Vector<>();
		semaphores = new ConcurrentHashMap<>();
	}
	/**
	 * Retrieves the single instance of this class.
	 */
	public static Inventory getInstance() {
		return SingletonHolderInv.instance;

	}

	/**
	 * Initializes the store inventory. This method adds all the items given to the store
	 * inventory.
	 * <p>
	 * @param inventory 	Data structure containing all data necessary for initialization
	 * 						of the inventory.
	 */
	public void load (BookInventoryInfo[ ] inventory ) {
		for (int i = 0; i < inventory.length; i = i + 1) {
			this.books.add(inventory[i]);
			semaphores.put(inventory[i].getBookTitle(), new Semaphore(inventory[i].getAmountInInventory()));
		}
	}

	/**
	 * Attempts to take one book from the store.
	 * <p>
	 * @param book 		Name of the book to take from the store
	 * @return 	an {@link Enum} with options NOT_IN_STOCK and SUCCESSFULLY_TAKEN.
	 * 			The first should not change the state of the inventory while the
	 * 			second should reduce by one the number of books of the desired type.
	 */
	public OrderResult take (String book) {
		if(semaphores.containsKey(book)) {
			if (semaphores.get(book).tryAcquire()) {
				BookInventoryInfo currentBook = null;
				for (BookInventoryInfo bookInfo : books) {
					if (bookInfo.getBookTitle().compareTo(book) == 0)
						currentBook = bookInfo;
				}

				currentBook.reduceAmountInInventory();
				return OrderResult.SUCCESSFULLY_TAKEN;
			}
		}
		return OrderResult.NOT_IN_STOCK;
	}

	/**
	 * Checks if a certain book is available in the inventory.
	 * <p>
	 * @param book 		Name of the book.
	 * @return the price of the book if it is available, -1 otherwise.
	 */
	public int checkAvailabiltyAndGetPrice(String book) {
		BookInventoryInfo currentBook = null;
		for (BookInventoryInfo bookInfo : books) {
			if(bookInfo.getBookTitle().compareTo(book) == 0) {
				currentBook = bookInfo;
			}
		}
		if(currentBook != null) {
				if (currentBook.getAmountInInventory() > 0) {
					return currentBook.getPrice();
				}
		}
		return -1;
	}

	/**
	 *
	 * <p>
	 * Prints to a file name @filename a serialized object HashMap<String,Integer> which is a Map of all the books in the inventory. The keys of the Map (type {@link String})
	 * should be the titles of the books while the values (type {@link Integer}) should be
	 * their respective available amount in the inventory.
	 * This method is called by the main method in order to generate the output.
	 */
	public void printInventoryToFile(String filename){
		HashMap<String, Integer> booksHashMap = new HashMap<>();
		for(BookInventoryInfo bookInfo: books){
			booksHashMap.put(bookInfo.getBookTitle(), bookInfo.getAmountInInventory());
		}

		//TODO erase

		for(BookInventoryInfo bookInfo: books){
			System.out.println(bookInfo.toString());
		}


		try {
			FileOutputStream fileOut = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(booksHashMap);
			out.close();
			fileOut.close();
			System.out.printf("Serialized data is saved in " + filename);
		} catch (IOException i) {
			i.printStackTrace();
		}

	}
}