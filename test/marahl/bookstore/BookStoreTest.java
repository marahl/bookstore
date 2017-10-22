package marahl.bookstore;

import javafx.util.Pair;
import marahl.bookstore.books.Book;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.*;


public class BookStoreTest {

    private Pair<Book, Integer>[] testBooks = new Pair[]{
            new Pair<>(new Book("Mastering åäö", "Average Swede", new BigDecimal(762.00)), 15),
            new Pair<>(new Book("How To Spend Money", "Rich Bloke", new BigDecimal(1000000.00)), 1),
            new Pair<>(new Book("Generic Title", "First Author", new BigDecimal(185.50)), 5),
            new Pair<>(new Book("Generic Title", "Second Author", new BigDecimal(1748.00)), 3),
            new Pair<>(new Book("Random Sales", "Cunning Bastard", new BigDecimal(999.00)), 20),
            new Pair<>(new Book("Random Sales", "Cunning Bastard", new BigDecimal(499.00)), 3),
            new Pair<>(new Book("Desired", "Rich Bloke", new BigDecimal(564.50)), 3)};
    private BookStore store = null;

    @Before
    public void setUp() throws Exception {
        store = new BookStore();
        for (Pair<Book, Integer> testBook : testBooks) {
            store.add(testBook.getKey(), testBook.getValue());
        }
    }


    @Test
    public void add() throws Exception {
        Book newBook = new Book("", "", "");
        int quantity = 3;

        boolean wasSuccess = store.add(newBook, quantity);
        assert (wasSuccess);

        int addedBookID = store.getBookID(newBook);

        assertEquals(newBook, store.getBook(addedBookID));
        assertEquals(quantity, store.getQuantity(addedBookID));
    }

    @Test
    public void addNull() throws Exception {
        boolean wasSuccess = store.add(null, 1);
        assert (!wasSuccess);
    }

    @Test
    public void addExisting() throws Exception {
        Book book = store.getBook(0);
        int quantity = store.getQuantity(0);
        boolean wasSuccess = store.add(book, 1);
        assert (wasSuccess);

        int expected = quantity + 1;
        int actual = store.getQuantity(book);
        assertEquals(expected, actual);
    }

    @Test
    public void addNegativeQuantity() throws Exception {
        boolean wasSuccess = store.add(new Book("", "", ""), -1);
        assert (!wasSuccess);
    }

    @Test
    public void addBatch() throws Exception {
        store = new BookStore();
        Book[] books = new Book[testBooks.length];
        int[] quantities = new int[testBooks.length];
        for (int i = 0; i < books.length; i++) {
            books[i] = testBooks[i].getKey();
            quantities[i] = testBooks[i].getValue();
        }

        store.addBatch(books, quantities);

        Book[] actualBooks = store.getStock();
        for (int i = 0; i < actualBooks.length; i++) {
            Book expectedBook = books[i];
            int expectedQuantity = quantities[i];
            Book actualBook = actualBooks[i];
            int actualQuantity = store.getQuantity(actualBook);

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

        int expectedSize = store.getStock().length + books.length;
        store.addBatch(books, quantities);
        int actualSize = store.getStock().length;
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

        int expectedSize = store.getStock().length;
        store.addBatch(books, quantities);
        int actualSize = store.getStock().length;
        assertEquals(expectedSize, actualSize);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addBatchDifferentArraySize() throws Exception {
        store = new BookStore();
        Book[] books = new Book[1];
        int[] quantities = new int[3];
        store.addBatch(books, quantities);
    }

    @Test
    public void removeBookByID() throws Exception {
        Book expectedBook = store.getBook(3);
        Book removedBook = store.remove(3).getKey();
        assertEquals(expectedBook, removedBook);
        assertNull(store.getBook(3));
    }

    @Test
    public void removeBookByBook() throws Exception {
        Book book = testBooks[3].getKey();
        Book removedBook = store.remove(book).getKey();
        assertEquals(book, removedBook);
        assertEquals(-1, store.getBookID(book));
        assertEquals(-1, store.getQuantity(book));
    }

    @Test
    public void getStock() throws Exception {
        Book[] bookStock = store.getStock();
        for (int i = 0; i < bookStock.length; i++) {
            Book actual = bookStock[i];
            Book expected = testBooks[i].getKey();
            assertEquals(expected, actual);
        }
    }

    @Test
    public void getStockAfterRemove() throws Exception {
        Book unexpected = testBooks[5].getKey();
        store.remove(5);
        Book[] bookStock = store.getStock();
        for (Book actual : bookStock) {
            assertNotEquals(unexpected, actual);
        }
    }

    @Test
    public void getBook() throws Exception {
        Book actual = store.getBook(5);
        Book expected = testBooks[5].getKey();
        assertEquals(expected, actual);
    }

    @Test
    public void getBookID() throws Exception {
        int actual = store.getBookID(testBooks[1].getKey());
        int expected = 1;
        assertEquals(expected, actual);
    }

    @Test
    public void getQuantity() throws Exception {
        int actual1 = store.getQuantity(testBooks[6].getKey());
        int actual2 = store.getQuantity(6);
        int expected = 3;
        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
    }

    @Test
    public void getQuantityNotFound() throws Exception {
        int actual1 = store.getQuantity(new Book("", "", "0"));
        int actual2 = store.getQuantity(-1);
        int expected = -1;
        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
    }

    private String createParseString(Book book, int quantity) {
        return String.format("%s;%s;%f;%d", book.getTitle(), book.getAuthor(), book.getPrice(), quantity);
    }

    @Test
    public void parseBook() throws Exception {
        Book expectedBook = new Book("Hello World", "", "100.03");
        Integer expectedQuantity = 0;
        String parseString = createParseString(expectedBook, expectedQuantity);
        Pair<Book, Integer> actualValue = BookStore.parseBook(parseString);
        assertBookEquals(expectedBook, actualValue.getKey());
        assertEquals(expectedQuantity, actualValue.getValue());
    }

    @Test
    public void constructorInitialStorage() {
        Book[] books = new Book[testBooks.length];
        int[] quantities = new int[testBooks.length];

        for (int i = 0; i < testBooks.length; i++) {
            Pair<Book, Integer> testBook = testBooks[i];
            books[i] = testBook.getKey();
            quantities[i] = testBook.getValue();
        }

        String parseString = "";
        for (int i = 0; i < books.length; i++) {
            Book book = books[i];
            int quantity = quantities[i];
            parseString += createParseString(book, quantity) + "\n";
        }

        BookStore newStore = new BookStore(parseString);

        Book[] bookStock = newStore.getStock();
        for (int i = 0; i < bookStock.length; i++) {
            Book actual = bookStock[i];
            Book expected = books[i];
            assertBookEquals(expected, actual);

            int actualQuantity = newStore.getQuantity(actual);
            int expectedQuantity = quantities[i];
            assertEquals(expectedQuantity, actualQuantity);
        }
    }

    @Test
    public void parseAndAddBatch() throws Exception {
        Book[] books = new Book[testBooks.length];
        int[] quantities = new int[testBooks.length];

        for (int i = 0; i < testBooks.length; i++) {
            Pair<Book, Integer> testBook = testBooks[i];
            books[i] = testBook.getKey();
            quantities[i] = testBook.getValue();
        }

        String parseString = "";
        for (int i = 0; i < books.length; i++) {
            Book book = books[i];
            int quantity = quantities[i];
            parseString += createParseString(book, quantity) + "\n";
        }

        BookStore newStore = new BookStore();
        boolean didSucceed = newStore.parseAndAddBatch(parseString);
        assertEquals(true, didSucceed);

        Book[] bookStock = newStore.getStock();
        for (int i = 0; i < bookStock.length; i++) {
            Book actual = bookStock[i];
            Book expected = books[i];
            assertBookEquals(expected, actual);

            int actualQuantity = newStore.getQuantity(actual);
            int expectedQuantity = quantities[i];
            assertEquals(expectedQuantity, actualQuantity);
        }
    }

    @Test
    public void parseAndAddBatchWrongFormatting() throws Exception {
        int expectedBookCount = store.getStock().length;
        boolean didSucceed1 = store.parseAndAddBatch(("hello;world;0;0\na;b;c;d"));
        boolean didSucceed2 = store.parseAndAddBatch(("hello;world;0;0\na;b;0"));
        assertEquals(false, didSucceed1);
        assertEquals(false, didSucceed2);
        int bookCount = store.getStock().length;
        assertEquals(expectedBookCount, bookCount);
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
    public void buyOneOfEach() throws Exception {
        Book[] cart = store.getStock();
        Book removedBook = store.remove(4).getKey();

        int[] status = store.buy(cart);
        assertEquals(cart.length, status.length);

        for (int i = 0; i < cart.length; i++) {
            Book book = cart[i];
            int quantity = store.getQuantity(book);
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
        Book[] cart = new Book[buyQuantity];
        Arrays.fill(cart, book);

        int[] status = store.buy(cart);
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

    private void assertBookEquals(Book expectedBook, Book actualBook) {
        assertEquals(expectedBook.getTitle(), actualBook.getTitle());
        assertEquals(expectedBook.getAuthor(), actualBook.getAuthor());
        assertEquals(expectedBook.getPrice().doubleValue(), actualBook.getPrice().doubleValue(), .01);
    }
}