package marahl.bookstore.books;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

/**
 * Created by madgr_000 on 22/10/2017.
 */
public class BookTest {

    @Test
    public void constructor() {
        String title = "A";
        String author = "B";
        BigDecimal price = BigDecimal.ZERO;
        Book book = new Book(title, author, price);
        assertEquals(title, book.getTitle());
        assertEquals(author, book.getAuthor());
        assertEquals(price.doubleValue(), book.getPrice().doubleValue(), 0.01);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNull() {
        new Book(null, null, "");
    }

    @Test
    public void constructorEmptyPrice() {
        Book book = new Book("", "", "");
        assertEquals(BigDecimal.ZERO.doubleValue(), book.getPrice().doubleValue(), 0.01);
    }

    @Test
    public void constructorPriceString() {
        double value = 1000.01;
        String price = "" + value;
        Book book = new Book("", "", price);
        assertEquals(value, book.getPrice().doubleValue(), 0.01);
    }

}