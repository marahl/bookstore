package marahl.bookstore;

import marahl.bookstore.books.Book;

import java.util.ArrayList;
import java.util.List;

public class BookCart {
    private List<Book> cart = new ArrayList<>();

    /**
     * Adds a number of books to the shopping cart
     *
     * @param book     the book to add to the cart, can't be null
     * @param quantity how many books to be added to the cart
     * @return true if something was added to the cart otherwise false
     */
    public boolean addToCart(Book book, int quantity) {
        if (book == null) return false;
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

}
