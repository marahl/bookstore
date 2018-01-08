package marahl.bookstore.books;

import org.junit.Test;

import java.text.ParseException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BookParserTest {

    private static String createParseString(Book book, int quantity) {
        return String.format("%s;%s;%f;%d", book.getTitle(), book.getAuthor(), book.getPrice(), quantity);
    }

    @Test
    public void parseBook() throws Exception {
        Book expectedBook = new Book("Hello World", "", "100.03");
        Integer expectedQuantity = 0;
        String parseString = createParseString(expectedBook, expectedQuantity);
        Map.Entry<Book, Integer> actualValue = BookParser.parseBook(parseString);
        assertBookEquals(expectedBook, actualValue.getKey());
        assertEquals(expectedQuantity, actualValue.getValue());
    }

    @Test(expected = ParseException.class)
    public void parseWrongFormattingNonnumericalValue() throws Exception {
        BookParser.parseBooks("hello;world;0;0\na;b;c;d");
    }

    @Test(expected = ParseException.class)
    public void parseWrongFormattingToFewArguments() throws Exception {
        BookParser.parseBooks("hello;world;0;0\na;b;0");
    }

    private static void assertBookEquals(Book expectedBook, Book actualBook) {
        assertEquals(expectedBook.getTitle(), actualBook.getTitle());
        assertEquals(expectedBook.getAuthor(), actualBook.getAuthor());
        assertEquals(expectedBook.getPrice().doubleValue(), actualBook.getPrice().doubleValue(), .01);
    }
}