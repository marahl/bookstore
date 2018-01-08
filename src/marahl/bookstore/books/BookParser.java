package marahl.bookstore.books;

import java.text.ParseException;
import java.util.*;

public class BookParser {
    private static final int PARSE_INDEX_TITLE = 0;
    private static final int PARSE_INDEX_AUTHOR = 1;
    private static final int PARSE_INDEX_PRICE = 2;
    private static final int PARSE_INDEX_QUANTITY = 3;
    private static final int PARSE_INDEX_ARGUMENT_COUNT = 4;

    /**
     * Parses the string and adds the books found to the stock
     * Each row in the string should contain exactly one book
     * Each book should be written as four values separated by ';'
     * The values are in order: title;author;price;quantity
     *
     * @param stockString sting to be parsed
     * @return an array of entries containing the books and quantities represented by the string
     * @throws ParseException if something went wrong while parsing
     */
    public static Map.Entry<Book, Integer>[] parseBooks(String stockString) throws ParseException {
        List<Map.Entry<Book, Integer>> books = new ArrayList<>();
        Scanner sc = new Scanner(stockString);
        String line;
        int lineNumber = 0;
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            lineNumber++;
            if (line.isEmpty()) continue;
            try {
                Map.Entry<Book, Integer> bookValues = parseBook(line);
                if (bookValues != null) {
                    books.add(bookValues);
                }
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                String message = String.format("While parsing row: %s\n%s %s", line, e.getClass().getName(), e.getMessage());

                ParseException pex = new ParseException(message, lineNumber);
                pex.setStackTrace(e.getStackTrace());
                throw pex;
            }
        }
        return books.toArray(new Map.Entry[books.size()]);
    }

    /**
     * Parses the string and returns a bock and a quantity as a Pair
     * The string should be written as four values separated by ';'
     * The values are in order: title;author;price;quantity
     *
     * @param bookString string to be parsed
     * @return an Entry containing the book as key and quantity as value
     * @throws IndexOutOfBoundsException if the string contains to few arguments
     * @throws NumberFormatException     if the price isn't a decimal number or quantity isn't an integer
     */
    public static Map.Entry<Book, Integer> parseBook(String bookString) throws IndexOutOfBoundsException, NumberFormatException {
        if (bookString.isEmpty()) return null;
        String[] result = bookString.split(";");
        String title = result[PARSE_INDEX_TITLE];
        String author = result[PARSE_INDEX_AUTHOR];
        String price = result[PARSE_INDEX_PRICE].replace(",", "");

        int quanity = Integer.parseInt(result[PARSE_INDEX_QUANTITY]);
        Book book = new Book(title, author, price);
        return new AbstractMap.SimpleImmutableEntry<>(book, quanity);
    }
}
