package marahl.bookstore;

import javafx.util.Pair;
import marahl.bookstore.books.Book;

import java.util.*;

public class BookStock {
    private static final int PARSE_INDEX_TITLE = 0;
    private static final int PARSE_INDEX_AUTHOR = 1;
    private static final int PARSE_INDEX_PRICE = 2;
    private static final int PARSE_INDEX_QUANTITY = 3;
    private static final int PARSE_INDEX_COUNT = 4;

    private Integer currentId = 0;
    private final Map<Integer, Book> stock = new HashMap<>();
    private final Map<Integer, Integer> bookQuantity = new HashMap<>();
    private final Map<Book, Integer> bookIds = new LinkedHashMap<>();


    /**
     * Adds zero or more book to the stock and gives it an unique ID, if the book already exists
     * it will add the quantity to the current stocked book instead.
     *
     * @param newBook  book to be added, can't be null
     * @param quantity quantity of the books to be added, can't be negative
     * @return the id of the added book
     * @throws NullPointerException     if the book is null
     * @throws IllegalArgumentException if the quantity is negative
     */
    public int addBook(Book newBook, int quantity) {
        if (newBook == null) throw new NullPointerException("Book can't be null");
        if (quantity < 0) throw new IllegalArgumentException("Quantity can't be negative");
        int id;
        synchronized (stock) {
            if (bookIds.containsKey(newBook)) {
                id = bookIds.get(newBook);
                quantity += getQuantity(id);
            } else {
                id = currentId++;
            }
            stock.put(id, newBook);
            bookQuantity.put(id, quantity);
            bookIds.put(newBook, id);
        }
        return id;
    }

    /**
     * Remove a book with the ID provided. All other books with keep their current IDs.
     *
     * @param bookId id of the book to remove
     * @return the book that was removed and its quantity or null if nothing was removed
     */
    public Pair<Book, Integer> removeBook(int bookId) {
        Pair<Book, Integer> removedPair = null;
        synchronized (stock) {
            Book removedBook = stock.remove(bookId);
            if (removedBook != null) {
                Integer quantity = bookQuantity.remove(bookId);
                removedPair = new Pair<>(removedBook, quantity);
                bookIds.remove(removedBook);
            }
        }
        return removedPair;
    }

    /**
     * Adds multiple books and their quantities to the stock. Arrays have to be the same size.
     *
     * @param books       books to be added
     * @param quanitities quantities of the books to be added
     */
    public void addBatch(Book[] books, int[] quanitities) {
        if (books.length != quanitities.length) {
            throw new IllegalArgumentException("Both arrays need to have the same length");
        }
        for (int i = 0; i < books.length; i++) {
            addBook(books[i], quanitities[i]);
        }
    }

    /**
     * Returns all books currently in stock as an array.
     *
     * @return all books
     */
    public Book[] getStock() {
        Book[] books = new Book[stock.size()];
        return stock.values().toArray(books);
    }

    /**
     * Returns the book with the provided ID
     *
     * @param id the id of the book to return
     * @return the book or null if there was no book with that id
     */
    public Book getBook(int id) {
        Book stockedBook = stock.get(id);
        if (stockedBook != null) {
            return stockedBook;
        }
        return null;
    }

    /**
     * Returns the book's ID
     *
     * @param book the book to get the ID of
     * @return the ID of the book or -1 if it wasn't found
     */
    public int getBookID(Book book) {
        Integer id = bookIds.get(book);
        if (id != null) {
            return id;
        }
        return -1;
    }


    /**
     * Returns the quantity of the book with the provided ID
     *
     * @param id the id of the book
     * @return quantity of the book
     */
    public int getQuantity(int id) {
        Integer quantity = bookQuantity.get(id);
        if (quantity != null) {
            return quantity;
        }
        return -1;
    }

    /**
     * Returns the quantity of the book
     *
     * @param book the book
     * @return quantity of the book
     */
    public int getQuantity(Book book) {
        Integer id = getBookID(book);
        if (id >= 0) {
            Integer quantity = bookQuantity.get(id);
            if (quantity != null) {
                return quantity;
            }
        }
        return -1;
    }

    /**
     * Parses the string and returns a bock and a quantity as a Pair
     * The string should be written as four values separated by ';'
     * The values are in order: title;author;price;quantity
     *
     * @param bookString string to be parsed
     * @return a Pair containing the book and quantity
     * @throws IndexOutOfBoundsException if the string contains to few arguments
     * @throws NumberFormatException     if the price isn't a decimal number or quantity isn't an integer
     */
    public static Pair<Book, Integer> parseBook(String bookString) throws IndexOutOfBoundsException, NumberFormatException {
        if (bookString.isEmpty()) return null;
        String[] result = bookString.split(";");
        String title = result[PARSE_INDEX_TITLE];
        String author = result[PARSE_INDEX_AUTHOR];
        String price = result[PARSE_INDEX_PRICE].replace(",", "");

        int quanity = Integer.parseInt(result[PARSE_INDEX_QUANTITY]);
        Book book = new Book(title, author, price);
        return new Pair<>(book, quanity);
    }

    /**
     * Parses the string and adds the books found to the stock
     * Each row in the string should contain exactly one book
     * Each book should be written as four values separated by ';'
     * The values are in order: title;author;price;quantity
     *
     * @param stockString sting to be parsed
     * @return false if something went wrong while parsing, otherwise true
     */
    public boolean parseAndAddBatch(String stockString) {
        List<Book> books = new ArrayList<>();
        List<Integer> bookQuantities = new ArrayList<>();
        Scanner sc = new Scanner(stockString);
        String line;
        int lineNumber = 0;
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            lineNumber++;
            if (line.isEmpty()) continue;
            try {
                Pair<Book, Integer> bookValues = parseBook(line);
                if (bookValues != null) {
                    books.add(bookValues.getKey());
                    bookQuantities.add(bookValues.getValue());
                }
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                System.out.println(String.format("Error while parsing on line %d: %s", lineNumber, line));
                return false;
            }
        }
        Book[] bookArr = books.toArray(new Book[books.size()]);
        int[] qtyArr = bookQuantities.stream().mapToInt(Integer::new).toArray();
        addBatch(bookArr, qtyArr);
        return true;
    }

}
