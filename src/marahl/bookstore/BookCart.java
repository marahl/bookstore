package marahl.bookstore;

import marahl.bookstore.books.Book;
import marahl.bookstore.books.BookList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BookCart {
    private ArrayList<Book> cart = new ArrayList<>();


    /**
     * Adds a number of books to the shopping cart
     *
     * @param book     the book to add to the cart, can't be null
     * @param quantity how many books to be added to the cart
     * @return true if something was added to the cart
     * @throws NullPointerException if the book is null
     */
    public boolean addToCart(Book book, int quantity) {
        if (book == null) throw new NullPointerException("Book can't be null");
        List<Book> booksToAdd = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            booksToAdd.add(book);
        }
        return cart.addAll(booksToAdd);
    }

    /**
     * Adds a book to the shopping cart
     *
     * @param book the book to add to the cart, can't be null
     * @return true if something was added to the cart
     * @throws NullPointerException if the book is null
     */
    public boolean addToCart(Book book) {
        return addToCart(book, 1);
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
     * @throws IndexOutOfBoundsException if index is outside the bounds of the cart
     */
    public Book removeFromCart(int i) {
        return cart.remove(i);
    }

    /**
     * Sums up price of all book in the cart with an OK status
     * at the designated store
     *
     * @param store store to get availability from
     * @return the price of all books with the OK status
     * @throws IllegalArgumentException if the arrays differ in size
     */
    public BigDecimal buyFromStore(BookList store) {
        Book[] bookCart = getCartContent();
        int[] bookStatus = store.buy(getCartContent());
        BigDecimal totalPrice = new BigDecimal(0);
        for (int i = 0; i < bookCart.length; i++) {
            int status = bookStatus[i];
            if (status == BookStore.OK) {
                totalPrice = totalPrice.add(bookCart[i].getPrice());
            }
        }
        return totalPrice;
    }
}
