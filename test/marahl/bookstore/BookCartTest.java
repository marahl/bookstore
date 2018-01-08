package marahl.bookstore;

import marahl.bookstore.books.Book;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BookCartTest {

    private static final Map.Entry<Book, Integer>[] testBooks = new Map.Entry[]{
            newEntry(new Book("Mastering åäö", "Average Swede", new BigDecimal(762.00)), 15),
            newEntry(new Book("How To Spend Money", "Rich Bloke", new BigDecimal(1000000.00)), 1),
            newEntry(new Book("Generic Title", "First Author", new BigDecimal(185.50)), 5),
            newEntry(new Book("Generic Title", "Second Author", new BigDecimal(1748.00)), 3),
            newEntry(new Book("Random Sales", "Cunning Bastard", new BigDecimal(999.00)), 20),
            newEntry(new Book("Random Sales", "Cunning Bastard", new BigDecimal(499.00)), 3),
            newEntry(new Book("Desired", "Rich Bloke", new BigDecimal(564.50)), 3)};

    private BookCart cart = null;

    @Before
    public void setUp() throws Exception {
        cart = new BookCart();
    }


    private static Map.Entry<Book, Integer> newEntry(Book book, int i) {
        return new AbstractMap.SimpleImmutableEntry<>(book, i);
    }

    @Test
    public void getCartContent() throws Exception {
        Book book = testBooks[1].getKey();
        cart.addToCart(book, 1);
        assertEquals(book, cart.getCartContent()[0]);
    }

    @Test
    public void removeFromCart() throws Exception {
        cart.addToCart(testBooks[4].getKey(), 2);
        cart.addToCart(testBooks[1].getKey(), 1);
        cart.addToCart(testBooks[4].getKey(), 2);
        int total = 5;
        assertEquals(total, cart.getCartContent().length);
        cart.removeFromCart(2);
        assertEquals(total - 1, cart.getCartContent().length);
        for (Book book : cart.getCartContent()) {
            assertEquals(testBooks[4].getKey(), book);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void removeFromCartIOOBException() throws Exception {
        cart.addToCart(testBooks[0].getKey());
        cart.removeFromCart(1);
    }

    @Test
    public void add() throws Exception {
        int quantity = 3;
        cart.addToCart(testBooks[0].getKey(), quantity);

        Book[] cartContent = cart.getCartContent();
        assertEquals(quantity, cartContent.length);

        Book expectedValue = testBooks[0].getKey();
        Book actualValue = cartContent[2];
        assertEquals(expectedValue, actualValue);
    }

}