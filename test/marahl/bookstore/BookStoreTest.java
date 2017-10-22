package marahl.bookstore;

import javafx.util.Pair;
import marahl.bookstore.books.Book;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;


public class BookStoreTest {

    private Pair<Book, Integer>[] testBooks = new Pair[]{
            new Pair<>(new Book("Mastering åäö", "Average Swede", new BigDecimal(762.00)), 15),
            new Pair<>(new Book("How To Spend Money", "Rich Bloke", new BigDecimal(1000000.00)), 1),
            new Pair<>(new Book("Generic Title", "First Author", new BigDecimal(185.50)), 5),
            new Pair<>(new Book("Generic Title", "Second Author", new BigDecimal(1748.00)), 3),
            new Pair<>(new Book("Random Sales", "Cunning Bastard", new BigDecimal(999.00)), 20),
            new Pair<>(new Book("Random Sales", "Cunning Bastard", new BigDecimal(499.00)), 3),
            new Pair<>(new Book("Desired", "Rich Bloke", new BigDecimal(564.50)), 3)};
    private BookStock stock = null;
    private BookStore store = null;

    @Before
    public void setUp() throws Exception {
        stock = new BookStock();
        for (Pair<Book, Integer> testBook : testBooks) {
            stock.addBook(testBook.getKey(), testBook.getValue());
        }
        store = new BookStore(stock);
    }

    @Test
    public void getTotalPrice() throws Exception {
        int[] status = new int[testBooks.length];
        Book[] books = new Book[testBooks.length];
        BigDecimal excpectedPrice = new BigDecimal(0);
        for (int i = 0; i < status.length; i++) {
            status[i] = i % 3;
            books[i] = testBooks[i].getKey();
            if (status[i] == BookStore.OK) {
                excpectedPrice = excpectedPrice.add(books[i].getPrice());
            }
        }
        BigDecimal actualPrice = store.getTotalPrice(books, status);
        assertEquals(excpectedPrice, actualPrice);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTotalPriceDifferentSizeArray() throws Exception {
        int[] status = new int[1];
        Book[] books = new Book[2];
        store.getTotalPrice(books, status);
    }

    @Test
    public void getCartContent() throws Exception {
        Book book = testBooks[1].getKey();
        store.add(book, 1);
        assertEquals(book, store.getCartContent()[0]);
    }

    @Test
    public void removeFromCart() throws Exception {
        store.add(stock.getBook(4), 2);
        store.add(stock.getBook(1), 1);
        store.add(stock.getBook(4), 2);
        int total = 5;
        assertEquals(total, store.getCartContent().length);
        store.removeFromCart(2);
        assertEquals(total - 1, store.getCartContent().length);
        for (Book book : store.getCartContent()) {
            assertEquals(testBooks[4].getKey(), book);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void removeFromCartIOOBException() throws Exception {
        store.add(stock.getBook(1), 1);
        store.removeFromCart(1);
    }

    @Test
    public void list() throws Exception {
        Book[] list1 = store.list("Generic Title");
        assertEquals(list1[0], testBooks[2].getKey());
        assertEquals(list1[1], testBooks[3].getKey());
        Book[] list2 = store.list("Rich Bloke");
        assertEquals(list2[0], testBooks[1].getKey());
        assertEquals(list2[1], testBooks[6].getKey());
        Book[] list3 = store.list("Marcus Ahlén");
        assertEquals(0, list3.length);
    }

    @Test
    public void add() throws Exception {
        int quantity = 3;
        store.add(stock.getBook(0), quantity);

        Book[] cartContent = store.getCartContent();
        assertEquals(quantity, cartContent.length);

        Book expectedValue = testBooks[0].getKey();
        Book actualValue = cartContent[2];
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void buySingle() throws Exception {
        for (Pair<Book, Integer> testBook : testBooks) {
            store.add(testBook.getKey(), 1);
        }
        Book removedBook = stock.removeBook(4).getKey();
        int[] status = store.buy(store.getCartContent());
        assertEquals(testBooks.length, status.length);

        for (int i = 0; i < testBooks.length; i++) {
            Book book = testBooks[i].getKey();
            int quantity = testBooks[i].getValue();
            int actual = status[i];
            int expected = -1;
            if (book == removedBook) {
                expected = BookStore.DOES_NOT_EXIST;
            } else if (quantity <= 0) {
                expected = BookStore.NOT_IN_STOCK;
            } else if (quantity > 0) expected = BookStore.OK;

            assertEquals(expected, actual);
        }
    }

    @Test
    public void buyMultiple() throws Exception {
        Pair<Book, Integer> testBook = testBooks[3];
        Book book = testBook.getKey();
        int bookQuantity = testBook.getValue();
        int buyQuantity = 10;
        store.add(book, buyQuantity);
        int[] status = store.buy(store.getCartContent());
        assertEquals(buyQuantity, status.length);

        for (int i = 0; i < testBooks.length; i++) {
            int expected = i < bookQuantity ? BookStore.OK : BookStore.NOT_IN_STOCK;
            int actual = status[i];

            assertEquals(expected, actual);
        }
    }

    @Test
    public void buyNone() throws Exception {
        int[] buy = store.buy();
        assertEquals(0, buy.length);
    }

    @Test
    public void getFormattedBookString() throws Exception {
        Book book = testBooks[4].getKey();
        int quantity = 6;
        String expected = String.format("%24s%24s%16.2f%8d", book.getTitle(), book.getAuthor(), book.getPrice(), quantity);
        String actual = BookStore.getFormattedBookString(book, quantity);
        assertEquals(expected, actual);
    }

    @Test
    public void getFormattedHeaderString() {
        Book book = testBooks[0].getKey();
        int headerStringLength = BookStore.getFormattedHeaderString().length();
        int bookStringLength = BookStore.getFormattedBookString(book).length();
        assertEquals(bookStringLength, headerStringLength);
        int headerStringLength2 = BookStore.getFormattedHeaderStringWithQuantity().length();
        int bookStringLength2 = BookStore.getFormattedBookString(book, 3).length();
        assertEquals(bookStringLength2, headerStringLength2);
    }
}