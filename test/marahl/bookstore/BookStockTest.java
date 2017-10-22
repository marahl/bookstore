package marahl.bookstore;

import javafx.util.Pair;
import marahl.bookstore.books.Book;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class BookStockTest {

    private Pair<Book, Integer>[] testBooks = new Pair[]{
            new Pair<>(new Book("Mastering åäö", "Average Swede", new BigDecimal(762.00)), 15),
            new Pair<>(new Book("How To Spend Money", "Rich Bloke", new BigDecimal(1000000.00)), 1),
            new Pair<>(new Book("Generic Title", "First Author", new BigDecimal(185.50)), 5),
            new Pair<>(new Book("Generic Title", "Second Author", new BigDecimal(1748.00)), 3),
            new Pair<>(new Book("Random Sales", "Cunning Bastard", new BigDecimal(999.00)), 20),
            new Pair<>(new Book("Random Sales", "Cunning Bastard", new BigDecimal(499.00)), 3),
            new Pair<>(new Book("Desired", "Rich Bloke", new BigDecimal(564.50)), 3)};
    private BookStock stock = null;

    @Before
    public void setUp() throws Exception {
        stock = new BookStock();
        for (Pair<Book, Integer> testBook : testBooks) {
            stock.addBook(testBook.getKey(), testBook.getValue());
        }
    }

    @Test
    public void addBook() throws Exception {
        Book newBook = new Book("", "", new BigDecimal(0));
        int quantity = 3;
        stock.addBook(newBook, quantity);
        Book[] completeStock = stock.getStock();

        assertEquals(newBook, completeStock[completeStock.length - 1]);
        assertEquals(quantity, stock.getQuantity(newBook));
    }

    @Test
    public void addBookExisting() throws Exception {
        Book book = stock.getBook(0);
        int quantity = stock.getQuantity(0);
        stock.addBook(book, 1);

        int expected = quantity + 1;
        int actual = stock.getQuantity(book);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addBookNullArg() throws Exception {
        stock.addBook(new Book("", "", ""), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addBookNegativeArg() throws Exception {
        stock.addBook(new Book("", "", new BigDecimal(0)), -1);
    }

    @Test
    public void addBatch() throws Exception {
        stock = new BookStock();
        Book[] books = new Book[testBooks.length];
        int[] quantities = new int[testBooks.length];
        for (int i = 0; i < books.length; i++) {
            books[i] = testBooks[i].getKey();
            quantities[i] = testBooks[i].getValue();
        }

        stock.addBatch(books, quantities);

        Book[] actualBooks = stock.getStock();
        for (int i = 0; i < actualBooks.length; i++) {
            Book expectedBook = books[i];
            int expectedQuantity = quantities[i];
            Book actualBook = actualBooks[i];
            int actualQuantity = stock.getQuantity(actualBook);

            assertEquals(expectedBook, actualBook);
            assertEquals(expectedQuantity, actualQuantity);
        }
    }

    @Test
    public void addBatchToCurrent() throws Exception {
        Book[] books = new Book[]{
                new Book("ABC", "Me", "0"),
                new Book("Hello World", "Someone", "1000"),
                new Book("", "", "50.33"),
                new Book("", "", "99.99")
        };
        int[] quantities = new int[]{0, 1, 2, 3};

        int expectedSize = stock.getStock().length + books.length;
        stock.addBatch(books, quantities);
        int actualSize = stock.getStock().length;
        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void addBatchToCurrentSameBooks() throws Exception {
        Book[] books = new Book[testBooks.length];
        int[] quantities = new int[testBooks.length];
        for (int i = 0; i < books.length; i++) {
            books[i] = testBooks[i].getKey();
            quantities[i] = testBooks[i].getValue();
        }

        int expectedSize = stock.getStock().length;
        stock.addBatch(books, quantities);
        int actualSize = stock.getStock().length;
        assertEquals(expectedSize, actualSize);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addBatchDifferentArraySize() throws Exception {
        stock = new BookStock();
        Book[] books = new Book[1];
        int[] quantities = new int[3];
        stock.addBatch(books, quantities);
    }

    @Test
    public void removeBook() throws Exception {
        stock.removeBook(3);
        assertNull(stock.getBook(3));
    }

    @Test
    public void getStock() throws Exception {
        Book[] bookStock = stock.getStock();
        for (int i = 0; i < bookStock.length; i++) {
            Book actual = bookStock[i];
            Book expected = testBooks[i].getKey();
            assertEquals(expected, actual);
        }
    }

    @Test
    public void getStockAfterRemove() throws Exception {
        Book unexpected = testBooks[5].getKey();
        stock.removeBook(5);
        Book[] bookStock = stock.getStock();
        for (Book actual : bookStock) {
            assertNotEquals(unexpected, actual);
        }
    }

    @Test
    public void getBook() throws Exception {
        Book actual = stock.getBook(5);
        Book expected = testBooks[5].getKey();
        assertEquals(expected, actual);
    }

    @Test
    public void getBookID() throws Exception {
        int actual = stock.getBookID(testBooks[1].getKey());
        int expected = 1;
        assertEquals(expected, actual);
    }

    @Test
    public void getQuantity() throws Exception {
        int actual1 = stock.getQuantity(testBooks[6].getKey());
        int actual2 = stock.getQuantity(6);
        int expected = 3;
        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
    }

    @Test
    public void getQuantityNotFound() throws Exception {
        int actual1 = stock.getQuantity(new Book("", "", "0"));
        int actual2 = stock.getQuantity(-1);
        int expected = -1;
        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
    }

    private String createParseString(Book book, int quantity) {
        return String.format("%s;%s;%f;%d", book.getTitle(), book.getAuthor(), book.getPrice(), quantity);
    }

    @Test
    public void parseBook() throws Exception {
        Book excpectedBook = new Book("Hello World", "", "100.03");
        Integer expectedQuantity = 0;
        String parseString = createParseString(excpectedBook, expectedQuantity);
        Pair<Book, Integer> actualValue = BookStock.parseBook(parseString);
        assertEquals(excpectedBook.getTitle(), actualValue.getKey().getTitle());
        assertEquals(excpectedBook.getAuthor(), actualValue.getKey().getAuthor());
        assertEquals(excpectedBook.getPrice().doubleValue(), actualValue.getKey().getPrice().doubleValue(), .01);
        assertEquals(expectedQuantity, actualValue.getValue());
    }

    @Test
    public void parseAndAddBatch() throws Exception {
        stock = new BookStock();
        Book[] books = new Book[]{
                new Book("ABC", "Me", "0"),
                new Book("Hello World", "Someone", "1000"),
                new Book("", "", "5430.33"),
                new Book("", "", "99.99")
        };
        int[] quantities = new int[]{0, 1, 2, 3};
        String parseString = "";
        for (int i = 0; i < books.length; i++) {
            Book book = books[i];
            int quantity = quantities[i];
            parseString += createParseString(book, quantity) + "\n";
        }

        boolean didSucceed = stock.parseAndAddBatch(parseString);
        assertEquals(true, didSucceed);

        Book[] bookStock = stock.getStock();
        for (int i = 0; i < bookStock.length; i++) {
            Book actual = bookStock[i];
            Book expected = books[i];
            assertEquals(expected.getTitle(), actual.getTitle());
            assertEquals(expected.getAuthor(), actual.getAuthor());
            assertEquals(expected.getPrice().doubleValue(), actual.getPrice().doubleValue(), .01);

            int actualQuantity = stock.getQuantity(actual);
            int expectedQuantity = quantities[i];
            assertEquals(expectedQuantity, actualQuantity);
        }
    }

    @Test
    public void parseAndAddBatchWrongFormatting() throws Exception {
        int expectedBookCount = stock.getStock().length;
        boolean didSucceed1 = stock.parseAndAddBatch(("hello;world;0;0\na;b;c;d"));
        boolean didSucceed2 = stock.parseAndAddBatch(("hello;world;0;0\na;b;0"));
        assertEquals(false, didSucceed1);
        assertEquals(false, didSucceed2);
        int bookCount = stock.getStock().length;
        assertEquals(expectedBookCount, bookCount);
    }

}