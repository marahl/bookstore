package marahl.bookstore;

import marahl.bookstore.books.Book;
import marahl.bookstore.books.BookList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookStore implements BookList {
    public static final int OK = 0;
    public static final int NOT_IN_STOCK = 1;
    public static final int DOES_NOT_EXIST = 2;

    private BookStock stock;
    private ArrayList<Book> cart = new ArrayList<>();

    public BookStore(BookStock stock) {
        this.stock = stock;
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
        for (Book book : stock.getStock()) {
            String title = book.getTitle();
            String author = book.getAuthor();
            title = title.substring(0, Math.min(searchString.length(), title.length()));
            author = author.substring(0, Math.min(searchString.length(), author.length()));
            if (title.equalsIgnoreCase(searchString) || author.equalsIgnoreCase(searchString)) foundBooks.add(book);
        }
        return foundBooks.toArray(new Book[foundBooks.size()]);
    }

    /**
     * Adds a number of books to the shopping cart
     *
     * @param book     the book to add to the cart, can't be null
     * @param quantity how many books to be added to the cart
     * @return true if something was added to the cart
     * @throws NullPointerException if the book is null
     */
    @Override
    public boolean add(Book book, int quantity) {
        if (book == null) throw new NullPointerException("Book can't be null");
        List<Book> booksToAdd = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            booksToAdd.add(book);
        }
        return cart.addAll(booksToAdd);
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
                currentlyStockedBooks.put(book, stock.getQuantity(book));
            }
        }
        int[] bookStatus = new int[books.length];
        BigDecimal totalPrice = new BigDecimal(0);
        for (int i = 0; i < books.length; i++) {
            Book book = books[i];
            int quantity = currentlyStockedBooks.get(book);
            if (quantity < 0) {
                bookStatus[i] = DOES_NOT_EXIST;
            } else if (quantity <= 0) {
                bookStatus[i] = NOT_IN_STOCK;
            } else {
                bookStatus[i] = OK;
                totalPrice = totalPrice.add(book.getPrice());
                currentlyStockedBooks.put(book, quantity - 1);
            }
        }

        System.out.println("Total price: " + totalPrice);
        return bookStatus;
    }

    /**
     * Sums up price of all book with an OK status
     * Both arrays in need to be the same size and each index in the arrays should correspond to the same book
     * Status can be:
     * <ul>
     * <li>OK - exists in stock</li>
     * <li>NOT_IN_STOCK - exists in stock but not high enough quantity</li>
     * <li>DOES_NOT_EXIST - does not exist in stock</li>
     * </ul>
     *
     * @param books      array of books to sum up
     * @param bookStatus array of statuses for each book
     * @return the price of all books with the OK status
     * @throws IllegalArgumentException if the arrays differ in size
     */
    public BigDecimal getTotalPrice(Book[] books, int[] bookStatus) {
        if (books.length != bookStatus.length) {
            throw new IllegalArgumentException("the arrays need to be the same size");
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

    /**
     * Gets the current quantity of the cart as an array
     *
     * @return array of books in the cart
     */
    public Book[] getCartContent() {
        return cart.toArray(new Book[cart.size()]);
    }

    /**
     * Removes the book on the index from the cart, books after it will be moved forward one index
     *
     * @param i index of the book to be removed from the cart
     * @return the Book that was removed
     */
    public Book removeFromCart(int i) {
        return cart.remove(i);
    }

}
