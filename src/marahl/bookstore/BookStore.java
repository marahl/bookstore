package marahl.bookstore;

import marahl.bookstore.books.Book;

import java.math.BigDecimal;
import java.util.*;

public class BookStore implements marahl.bookstore.books.BookList {
    public static final int OK = 0;
    public static final int NOT_IN_STOCK = 1;
    public static final int DOES_NOT_EXIST = 2;

    private Integer currentId = 0;
    private final Map<Integer, Book> stock = new HashMap<>();
    private final Map<Integer, Integer> bookQuantity = new HashMap<>();
    private final Map<Book, Integer> bookIds = new LinkedHashMap<>();

    /**
     * Matches the search string against the beginning of each book's title and author and returns
     * an array of each book with a match in either.
     * Ex. search string="Hell" will match the books "<b>hell</b>o world" by "Someone" and "The Story of My Life" by "<b>Hell</b>en Keller"
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
     * @return an entry containing the book that was removed as key and its quantity as value or null and 0 if nothing was removed
     */
    public Map.Entry<Book, Integer> remove(int bookId) {
        Map.Entry<Book, Integer> removedEntry = createNewEntry(null, 0);
        synchronized (stock) {
            Book book = stock.get(bookId);
            if (book != null) {
                stock.remove(bookId);
                bookIds.remove(book);
                Integer quantity = bookQuantity.remove(bookId);
                removedEntry = createNewEntry(book, quantity);
            }
        }
        return removedEntry;
    }

    /**
     * Remove a book from the stock. All other books with keep their current IDs.
     *
     * @param book the book to remove
     * @return an entry containing the book that was removed as key and its quantity as value or null and 0 if nothing was removed
     */
    public Map.Entry<Book, Integer> remove(Book book) {
        Map.Entry<Book, Integer> removedEntry = createNewEntry(null, 0);
        synchronized (stock) {
            Integer bookId = bookIds.get(book);
            if (bookId != null) {
                stock.remove(bookId);
                bookIds.remove(book);
                Integer quantity = bookQuantity.remove(bookId);
                removedEntry = createNewEntry(book, quantity);
            }
        }
        return removedEntry;
    }


    /**
     * Reduces the quantity of a book from the stock. Won't remove more of the book than available.
     *
     * @param id  the id of the book to reduce
     * @param qty the quantity of books to be removed, has to be greater than 0
     * @return an entry containing the book that was removed as key and the quantity removed as value or null and 0 if nothing was removed
     */
    public Map.Entry<Book, Integer> reduceQuantity(int id, int qty) {
        Map.Entry<Book, Integer> reducedEntry = createNewEntry(null, 0);
        if (qty >= 0) {
            synchronized (stock) {
                Book book = getBook(id);
                if (book != null) {
                    Integer currentQuantity = bookQuantity.get(id);
                    int newQuantity = Math.max(currentQuantity - qty, 0);
                    int decreasedQuantity = currentQuantity - newQuantity;
                    bookQuantity.put(id, newQuantity);
                    reducedEntry = createNewEntry(book, decreasedQuantity);
                }
            }
        }
        return reducedEntry;
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
        return 0;
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
        return 0;
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
            currentlyStockedBooks.put(book, getQuantity(book));
        }
        int[] bookStatus = new int[books.length];
        for (int i = 0; i < books.length; i++) {
            Book book = books[i];
            Integer quantity = currentlyStockedBooks.get(book);
            if (quantity == null) {
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
     * @param books books to be added
     */
    public void addBatch(Map.Entry<Book, Integer>[] books) {
        for (Map.Entry<Book, Integer> book : books) {
            add(book.getKey(), book.getValue());
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
     * Sums up price of all book with an OK status
     *
     * @param books books to calculate the prices from
     * @return the price of all books with the OK status
     * @throws IllegalArgumentException if the arrays differ in size
     */
    public BigDecimal getPrice(Book... books) {
        return getPrice(books, buy(books));
    }

    /**
     * Sums up price of all book with an OK status
     *
     * @param books      books to calculate the prices from
     * @param bookStatus status of books to be added
     * @return the price of all books with the OK status
     * @throws IllegalArgumentException if the arrays differ in size
     */
    public BigDecimal getPrice(Book[] books, int[] bookStatus) {
        if (books.length != bookStatus.length) {
            throw new IllegalArgumentException(String.format("The array of books and the array of statuses differ in size (%d != %d)", books.length, bookStatus.length));
        }
        BigDecimal totalPrice = new BigDecimal(0);
        for (int i = 0; i < books.length; i++) {
            int status = bookStatus[i];
            if (status == OK) {
                totalPrice = totalPrice.add(books[i].getPrice());
            }
        }
        return totalPrice;
    }

    private static Map.Entry<Book, Integer> createNewEntry(Book book, int quantity) {
        return new AbstractMap.SimpleImmutableEntry<>(book, quantity);
    }
}
