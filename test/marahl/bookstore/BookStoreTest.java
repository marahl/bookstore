package marahl.bookstore;

import marahl.bookstore.books.Book;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;


public class BookStoreTest {

    private static final Map.Entry<Book, Integer>[] testBooks = new Map.Entry[]{
            newEntry(new Book("Mastering åäö", "Average Swede", new BigDecimal(762.00)), 15),
            newEntry(new Book("How To Spend Money", "Rich Bloke", new BigDecimal(1000000.00)), 1),
            newEntry(new Book("Generic Title", "First Author", new BigDecimal(185.50)), 5),
            newEntry(new Book("Generic Title", "Second Author", new BigDecimal(1748.00)), 3),
            newEntry(new Book("Random Sales", "Cunning Bastard", new BigDecimal(999.00)), 20),
            newEntry(new Book("Random Sales", "Cunning Bastard", new BigDecimal(499.00)), 3),
            newEntry(new Book("Desired", "Rich Bloke", new BigDecimal(564.50)), 3)};
    private BookStore store = null;

    @Before
    public void setUp() throws Exception {
        store = new BookStore();
        for (Map.Entry<Book, Integer> testBook : testBooks) {
            store.add(testBook.getKey(), testBook.getValue());
        }
    }

    private static Map.Entry<Book, Integer> newEntry(Book book, int i) {
        return new AbstractMap.SimpleImmutableEntry<>(book, i);
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
        Map.Entry<Book, Integer>[] books = testBooks;

        store.addBatch(books);

        Book[] actualBooks = store.getStock();
        for (int i = 0; i < actualBooks.length; i++) {
            Book expectedBook = books[i].getKey();
            int expectedQuantity = books[i].getValue();
            Book actualBook = actualBooks[i];
            int actualQuantity = store.getQuantity(actualBook);

            assertEquals(expectedBook, actualBook);
            assertEquals(expectedQuantity, actualQuantity);
        }
    }

    @Test
    public void addBatchToCurrent() throws Exception {
        Map.Entry<Book, Integer>[] books = new Map.Entry[]{
                newEntry(new Book("ABC", "Me", "0"), 0),
                newEntry(new Book("Hello World", "Someone", "1000"), 1),
                newEntry(new Book("", "", "50.33"), 2),
                newEntry(new Book("", "", "99.99"), 3)
        };
        int[] quantities = new int[]{0, 1, 2, 3};

        int expectedSize = store.getStock().length + books.length;
        store.addBatch(books);
        int actualSize = store.getStock().length;
        assertEquals(expectedSize, actualSize);
    }


    @Test
    public void addBatchToCurrentSameBooks() throws Exception {
        Map.Entry<Book, Integer>[] books = testBooks;

        int expectedSize = store.getStock().length;
        store.addBatch(books);
        int actualSize = store.getStock().length;
        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void removeBookByID() throws Exception {
        int idBefore = 3;
        Book bookBefore = store.getBook(3);

        final Book expectedBookRemoved = bookBefore;
        final int expectedQuantityRemoved = store.getQuantity(bookBefore);
        final Book expectedBookAfterRemoval = null;
        final int expectedQuantityAfterRemoval = 0;

        final Map.Entry<Book, Integer> removed = store.remove(idBefore);


        Book actualBookRemoved = removed.getKey();
        int actualQuantityRemoved = removed.getValue();
        Book actualBookAfterRemoval = store.getBook(idBefore);
        int actualQuantityAfterRemoval = store.getQuantity(idBefore);

        assertEquals(expectedBookRemoved, actualBookRemoved);
        assertEquals(expectedQuantityRemoved, actualQuantityRemoved);
        assertEquals(expectedBookAfterRemoval, actualBookAfterRemoval);
        assertEquals(expectedQuantityAfterRemoval, actualQuantityAfterRemoval);
        assertNull(store.getBook(3));
    }

    @Test
    public void removeBookByBook() throws Exception {
        final int idBefore = 3;
        final Book bookBefore = store.getBook(idBefore);

        final Book expectedBookRemoved = bookBefore;
        final int expectedQuantityRemoved = store.getQuantity(bookBefore);
        final int expectedBookIdAfterRemoval = -1;
        final int expectedQuantityAfterRemoval = 0;

        final Map.Entry<Book, Integer> removed = store.remove(bookBefore);

        final Book actualBookRemoved = removed.getKey();
        final int actualQuantityRemoved = removed.getValue();
        final int actualBookIdAfterRemoval = store.getBookID(actualBookRemoved);
        final int actualQuantityAfterRemoval = store.getQuantity(idBefore);

        assertEquals(expectedBookRemoved, actualBookRemoved);
        assertEquals(expectedQuantityRemoved, actualQuantityRemoved);
        assertEquals(expectedBookIdAfterRemoval, actualBookIdAfterRemoval);
        assertEquals(expectedQuantityAfterRemoval, actualQuantityAfterRemoval);
    }

    @Test
    public void reduceQuantityOfBook() throws Exception {
        final int idBefore = 2;
        final Book bookBefore = store.getBook(idBefore);
        final int quantityBefore = store.getQuantity(idBefore);
        final int quantityToDecreace = 3;

        final Book expectedBookRemoved = bookBefore;
        final int expectedQuantityRemoved = Math.max(0, quantityToDecreace);
        final int expectedBookIdAfterRemoval = idBefore;
        final int expectedQuantityAfterRemoval = Math.max(0, quantityBefore - quantityToDecreace);

        final Map.Entry<Book, Integer> removed = store.reduceQuantity(idBefore, quantityToDecreace);

        final Book actualBookRemoved = removed.getKey();
        final int actualQuantityRemoved = removed.getValue();
        final int actualBookIdAfterRemoval = store.getBookID(actualBookRemoved);
        final int actualQuantityAfterRemoval = store.getQuantity(idBefore);

        assertEquals(expectedBookRemoved, actualBookRemoved);
        assertEquals(expectedQuantityRemoved, actualQuantityRemoved);
        assertEquals(expectedBookIdAfterRemoval, actualBookIdAfterRemoval);
        assertEquals(expectedQuantityAfterRemoval, actualQuantityAfterRemoval);
    }

    @Test
    public void reduceQuantityOfBookMoreThanAvailable() throws Exception {
        final int idBefore = 2;
        final Book bookBefore = store.getBook(idBefore);
        final int quantityBefore = store.getQuantity(idBefore);
        final int quantityToDecreace = quantityBefore * 3;

        final Book expectedBookRemoved = bookBefore;
        final int expectedQuantityRemoved = quantityBefore;
        final int expectedBookIdAfterRemoval = idBefore;
        final int expectedQuantityAfterRemoval = 0;

        final Map.Entry<Book, Integer> removed = store.reduceQuantity(idBefore, quantityToDecreace);

        final Book actualBookRemoved = removed.getKey();
        final int actualQuantityRemoved = removed.getValue();
        final int actualBookIdAfterRemoval = store.getBookID(actualBookRemoved);
        final int actualQuantityAfterRemoval = store.getQuantity(idBefore);

        assertEquals(expectedBookRemoved, actualBookRemoved);
        assertEquals(expectedQuantityRemoved, actualQuantityRemoved);
        assertEquals(expectedBookIdAfterRemoval, actualBookIdAfterRemoval);
        assertEquals(expectedQuantityAfterRemoval, actualQuantityAfterRemoval);
    }

    @Test
    public void reduceQuantityOfBookByNegativeValue() throws Exception {
        final Book expectedBook = null;
        final int expectedQuantity = 0;

        final Map.Entry<Book, Integer> removed = store.reduceQuantity(0, -1);

        assertEquals(expectedBook, removed.getKey());
        assertEquals(expectedQuantity, (int) removed.getValue());
        assertEquals(-1, store.getBookID(expectedBook));
        assertEquals(0, store.getQuantity(expectedBook));
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
        int expected = 0;
        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
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
        Book reducedBook = store.reduceQuantity(3, 100).getKey();

        int[] status = store.buy(cart);
        assertEquals(cart.length, status.length);

        String expectedResult = "";
        String actualResult = "";
        for (int i = 0; i < cart.length; i++) {
            Book book = cart[i];
            actualResult += status[i];
            if (book == removedBook) {
                expectedResult += BookStore.DOES_NOT_EXIST;
            } else if (book == reducedBook) {
                expectedResult += BookStore.NOT_IN_STOCK;
            } else {
                expectedResult += BookStore.OK;
            }
        }
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void buyMultiple() throws Exception {
        Map.Entry<Book, Integer> testBook = testBooks[3];
        Book book = testBook.getKey();
        int bookQuantity = testBook.getValue();
        int buyQuantity = 10;
        Book[] cart = new Book[buyQuantity];
        Arrays.fill(cart, book);

        int[] status = store.buy(cart);
        assertEquals(buyQuantity, status.length);

        String expectedResult = "";
        String actualResult = "";
        for (int i = 0; i < cart.length; i++) {
            expectedResult += i < bookQuantity ? BookStore.OK : BookStore.NOT_IN_STOCK;
            actualResult += status[i];
        }
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void buyNone() throws Exception {
        int[] buy = store.buy();
        assertEquals(0, buy.length);
    }

    @Test
    public void buyFromStore() throws Exception {
        BigDecimal expectedTotalPrice = BigDecimal.ZERO;
        List<Book> bookList = new ArrayList<>();
        for (int i = 0; i < testBooks.length && i < 4; i++) {
            Book book = testBooks[i].getKey();
            int quantity = testBooks[i].getValue();

            for (int j = 0; j < quantity + 5; j++) {
                bookList.add(book);
            }
            expectedTotalPrice = expectedTotalPrice.add(book.getPrice().multiply(new BigDecimal(quantity)));
        }
        bookList.add(new Book("", "", "100000"));

        BigDecimal actualTotalPrice = store.getPrice(bookList.toArray(new Book[bookList.size()]));
        assertEquals(expectedTotalPrice, actualTotalPrice);
    }

}