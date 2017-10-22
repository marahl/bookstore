package marahl.bookstore;

import javafx.util.Pair;
import marahl.bookstore.books.Book;
import marahl.bookstore.books.BookList;

import java.math.BigDecimal;
import java.util.*;

public class BookStore implements BookList {
    public static final int OK = 0;
    public static final int NOT_IN_STOCK = 1;
    public static final int DOES_NOT_EXIST = 2;
    private static final int PARSE_INDEX_TITLE = 0;
    private static final int PARSE_INDEX_AUTHOR = 1;
    private static final int PARSE_INDEX_PRICE = 2;
    private static final int PARSE_INDEX_QUANTITY = 3;
    private static final int PARSE_INDEX_COUNT = 4;

    private Integer currentId = 0;
    private final Map<Integer, Book> stock = new HashMap<>();
    private final Map<Integer, Integer> bookQuantity = new HashMap<>();
    private final Map<Book, Integer> bookIds = new LinkedHashMap<>();

    public BookStore() {
    }

    public BookStore(String initialStockString) {
        parseAndAddBatch(initialStockString);
    }

    /**
     * Matches the search string agains the beginning of each book's title and author and returns
     * an array of each book with a match in either.
     * Ex. search string="Hell" will match the books "hello world" by "Someone" and "The Story of My Life" by "Hellen Keller"
     *
     * @param searchString String to search from among books in stock. Not case sensitive.
     * @return Array of books matching the search string.
     */
    @Override
    public Book[] list(String searchString) {
        List<Book> foundBooks = new ArrayList<>();
        for (Book book : getStock()) {
            String title = book.getTitle();
            String author = book.getAuthor();
            title = title.substring(0, Math.min(searchString.length(), title.length()));
            author = author.substring(0, Math.min(searchString.length(), author.length()));
            if (title.equalsIgnoreCase(searchString) || author.equalsIgnoreCase(searchString)) foundBooks.add(book);
        }
        return foundBooks.toArray(new Book[foundBooks.size()]);
    }

    /**
     * Adds zero or more book to the stock and gives it an unique ID, if the book already exists
     * it will add the quantity to the current stocked book instead.
     *
     * @param newBook  book to be added, can't be null
     * @param quantity quantity of the books to be added, can't be negative
     * @return <tt>true</tt> if the book and quantity was able to be added
     * @throws NullPointerException     if the book is null
     * @throws IllegalArgumentException if the quantity is negative
     */
    public boolean add(Book newBook, int quantity) {
        if (newBook == null) {
            return false;
        } else if (quantity < 0) {
            return false;
        }
        synchronized (stock) {
            int id;
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
        return true;
    }

    /**
     * Remove a book with the ID provided. All other books with keep their current IDs.
     *
     * @param bookId id of the book to remove
     * @return the book that was removed and its quantity or null if nothing was removed
     */
    public Pair<Book, Integer> remove(int bookId) {
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
     * Remove a book from the stock. All other books with keep their current IDs.
     *
     * @param book the book to remove
     * @return the book that was removed and its quantity or null if nothing was removed
     */
    public Pair<Book, Integer> remove(Book book) {
        Pair<Book, Integer> removedPair = null;
        synchronized (stock) {
            Integer removedBookId = bookIds.remove(book);
            if (removedBookId != null) {
                Integer quantity = bookQuantity.remove(removedBookId);
                Book removedBook = stock.remove(removedBookId);
                removedPair = new Pair<>(removedBook, quantity);
            }
        }
        return removedPair;
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

    /**
     * Return the status of the books in the array against the current stock of books
     * and prints the total price of the books in the array that were available
     * Status can be:
     * <ul>
     * <li>OK - exists in stock</li>
     * <li>NOT_IN_STOCK - exists in stock but not high enough quantity</li>
     * <li>DOES_NOT_EXIST - does not exist in stock</li>
     * </ul>
     *
     * @param books books that should be checked
     * @return an array with the same size as the input array and corresponds to each book's status in order.
     */
    @Override
    public int[] buy(Book... books) {
        Map<Book, Integer> currentlyStockedBooks = new HashMap<>();
        for (Book book : books) {
            if (!currentlyStockedBooks.containsKey(book)) {
                currentlyStockedBooks.put(book, getQuantity(book));
            }
        }
        int[] bookStatus = new int[books.length];
        for (int i = 0; i < books.length; i++) {
            Book book = books[i];
            int quantity = currentlyStockedBooks.get(book);
            if (quantity < 0) {
                bookStatus[i] = DOES_NOT_EXIST;
            } else if (quantity <= 0) {
                bookStatus[i] = NOT_IN_STOCK;
            } else {
                bookStatus[i] = OK;
                currentlyStockedBooks.put(book, quantity - 1);
            }
        }

        return bookStatus;
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
            add(books[i], quanitities[i]);
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
     * Creates a string header to use with <code>getFormattedBookString(Book)</code>
     * Title Author Price
     *
     * @return legend string
     */
    public static String getFormattedHeaderString() {
        String title = "Title";
        String author = "Author";
        String price = "Price";
        return String.format("%24s%24s%16s", title, author, price);
    }

    /**
     * Creates a string header to use with <code>getFormattedBookString(Book,int)</code>
     * Title Author Price Quantity
     *
     * @return legend string
     */
    public static String getFormattedHeaderStringWithQuantity() {
        return getFormattedHeaderString() + String.format("%8s", "Qty");
    }

    /**
     * Creates a string of the content of the book object and formats it in columns
     *
     * @param book contents to be used
     * @return content string
     */
    public static String getFormattedBookString(Book book) {
        String title = book.getTitle();
        String author = book.getAuthor();
        BigDecimal price = book.getPrice();
        return String.format("%24s%24s%16.2f", title, author, price);
    }

    /**
     * Creates a string of the content of the book object and formats it in columns
     *
     * @param book     contents to be used
     * @param quantity quantity of the book
     * @return content string
     */
    public static String getFormattedBookString(Book book, int quantity) {
        return getFormattedBookString(book) + String.format("%8d", quantity);
    }

}
