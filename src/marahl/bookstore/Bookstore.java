package marahl.bookstore;

import marahl.bookstore.books.Book;
import marahl.bookstore.books.BookList;

public class Bookstore implements BookList {
    public static final int OK = 0;
    public static final int NOT_IN_STOCK = 1;
    public static final int DOES_NOT_EXIST = 2;

    @Override
    public Book[] list(String searchString) {
        throw new UnsupportedOperationException("list(String)>Book[] not implemented");
    }

    @Override
    public boolean add(Book book, int quantity) {
        throw new UnsupportedOperationException("add(Book,int)>boolean not implemented");
    }

    @Override
    public int[] buy(Book... books) {
        throw new UnsupportedOperationException("buy(Book[])>int[] not implemented");
    }
}
