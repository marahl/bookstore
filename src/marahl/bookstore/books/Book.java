package marahl.bookstore.books;

import java.math.BigDecimal;

public class Book {

    private String title;

    private String author;

    private BigDecimal price;

    public Book(String title, String author, String price) {
        this(title, author, price.isEmpty() ? BigDecimal.ZERO : new BigDecimal(price));
    }

    public Book(String title, String author, BigDecimal price) {
        if (title == null || author == null || price == null) {
            throw new NullPointerException();
        }
        this.title = title;
        this.author = author;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BigDecimal getPrice() {
        return price;
    }
}