package marahl.bookstore;


import javafx.util.Pair;
import marahl.bookstore.books.Book;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Main {
    private static final String defaultURL = "https://raw.githubusercontent.com/contribe/contribe/dev/bookstoredata/bookstoredata.txt";

    private static final String ADD = "add";
    private static final String REMOVE = "remove";
    private static final String ADD_STOCK = "addstock";
    private static final String REMOVE_STOCK = "remstock";
    private static final String FIND = "find";
    private static final String LIST = "list";
    private static final String CART = "cart";
    private static final String BUY = "buy";
    private static final String EXIT = "exit";
    private static final String HELP = "help";
    private static final LinkedHashMap<String, String> commandHelp = new LinkedHashMap<>();

    private static BookStock stock;
    private static BookStore store;
    private static LinkedList<String> messages = new LinkedList<>();

    public static void main(String[] args) {
        initCommandHelpStrings();
        String storeUrl = defaultURL;
        if (args.length > 0) {
            storeUrl = args[0];
        }
        try {
            stock = new BookStock();
            if (!updateStock(stock, new URL(storeUrl))) {
                System.out.println("Error while trying to load books from the URL: " + storeUrl + "\nContinuing with an empty stock");
            }
        } catch (MalformedURLException e) {
            System.out.println(defaultURL + " is no valid URL. BookStock will remain empty");
        }
        store = new BookStore(stock);

        storeLoop();
    }

    private static void storeLoop() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Hello and welcome to our store!");
        System.out.println("To list available commands, type help");
        while (true) {
            System.out.print(">>");
            executeCommand(sc.nextLine());
            while (!messages.isEmpty()) {
                System.out.println(messages.pop());
            }
        }
    }

    private static boolean updateStock(BookStock stock, URL storeURL) {
        if (storeURL != null) {
            String stockString = "";
            try (Scanner sc = new Scanner(storeURL.openStream())) {
                while (sc.hasNextLine()) {
                    stockString += sc.nextLine() + "\n";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return stock.parseAndAddBatch(stockString);
        } else {
            return false;
        }
    }

    private static void initCommandHelpStrings() {
        commandHelp.put(ADD, "[id;(quantity)] Add a book to your cart");
        commandHelp.put(REMOVE, "[cartindex] Remove a book from your cart");
        commandHelp.put(ADD_STOCK, "[title;author;price;quantity] Add a book to the store's stock");
        commandHelp.put(REMOVE_STOCK, "[id] Remove a book from the store's stock");
        commandHelp.put(FIND, "[title or author] List all books with that title or by that author");
        commandHelp.put(LIST, "Lists all books currently in stock");
        commandHelp.put(CART, "Lists all books currently in your shopping cart");
        commandHelp.put(BUY, "Buy contents of your shopping chart");
        commandHelp.put(HELP, "List all available commands");
    }

    private static void executeCommand(String line) {
        String[] stringarr = line.split(" ", 2);
        String command = stringarr[0].toLowerCase();
        String[] args = new String[0];
        if (stringarr.length > 1) {
            args = stringarr[1].split(";");
        }
        switch (command) {
            case ADD:
                commandAdd(args);
                break;
            case REMOVE:
                commandRemove(args);
                break;
            case ADD_STOCK:
                commandAddStock(args);
                break;
            case REMOVE_STOCK:
                commandRemoveStock(args);
                break;
            case FIND:
                commandFind(args);
                break;
            case LIST:
                commandList();
                break;
            case CART:
                commandCart();
                break;
            case BUY:
                commandBuy();
                break;
            case EXIT:
                System.exit(0);
            case HELP:
            default:
                addCommandListMessages();
                break;
        }
    }

    private static boolean commandAdd(String[] args) {
        if (hasArgument(args, 0)) {
            Integer bookId = getPositiveIntegerArgument(args, 0);
            if (bookId == null) return false;
            Book book = stock.getBook(bookId);
            if (book == null) {
                messages.add(String.format("Couldn't find any book with the id %s", args[0]));
                return false;
            }
            Integer quantity = 1;
            if (hasArgument(args, 1)) {
                quantity = getPositiveIntegerArgument(args, 1);
                if (quantity == null) return false;
            }
            boolean b = store.add(book, quantity);
            if (b) {
                messages.add(String.format("Successfully added %dx %s by %s to cart!", quantity, book.getTitle(), book.getAuthor()));
            } else {
                messages.add(String.format("Failed to add %dx %s by %s to cart!", quantity, book.getTitle(), book.getAuthor()));
            }
        } else {
            messages.add("Too few arguments! Need at least a book title.");
        }
        return false;
    }

    private static Book commandRemove(String[] args) {
        if (hasArgument(args, 0)) {
            Integer cartIndex = getPositiveIntegerArgument(args, 0);
            if (cartIndex != null) {
                try {
                    Book book = store.removeFromCart(cartIndex);
                    messages.add(String.format("Successfully removed %s by %s from your cart", book.getTitle(), book.getAuthor()));
                    return book;
                } catch (NumberFormatException ex) {
                    messages.add(String.format("No book in cart on index %d", cartIndex));
                }
            }
        } else {
            messages.add("Too few arguments! Need a cart index.");
        }
        return null;
    }

    private static boolean commandAddStock(String[] args) {
        if (hasArgument(args, 3)) {
            String title = args[0];
            String author = args[1];
            BigDecimal price = getDecimalArgument(args, 2);
            int quantity = getPositiveIntegerArgument(args, 3);
            Book book = new Book(title, author, price);
            stock.addBook(book, quantity);
            messages.add("Added a new book to the store:");
            messages.add(BookStore.getFormattedBookString(book, quantity));
            return true;
        } else {
            messages.add(String.format("Too few arguments! Need 4 arguments but only found %d (title;author;price;quantity)", args.length));
            messages.add(Arrays.toString(args));
        }
        return false;
    }

    private static Pair<Book, Integer> commandRemoveStock(String[] args) {
        if (hasArgument(args, 0)) {
            Integer bookId = getPositiveIntegerArgument(args, 0);
            if (bookId == null) return null;
            Pair<Book, Integer> removedBook = stock.removeBook(bookId);
            if (removedBook != null) {
                messages.add("Book from the store:");
                messages.add(BookStore.getFormattedBookString(removedBook.getKey(), removedBook.getValue()));
            } else {
                messages.add("No books where removed");
            }
            return removedBook;
        } else {
            messages.add("Too few arguments! Need a book ID and a quantity to remove");
        }
        return null;
    }


    private static Book[] commandFind(String[] args) {
        String searchString = "";
        if (hasArgument(args, 0)) {
            searchString = args[0];
        }
        Book[] list = store.list(searchString);
        if (list.length > 0) {
            messages.add(getStockLegendString());
            for (Book book : list) {
                messages.add(getStockBookString(book, stock.getQuantity(book)));
            }
        } else {
            messages.add(String.format("Couldn't find anything matching \"%s\"...", searchString));
        }
        return list;
    }

    private static Book[] commandList() {
        Book[] books = Main.stock.getStock();
        if (books.length == 0) {
            messages.add("There are no books in the store");
        } else {
            messages.add(getStockLegendString());
            for (Book book : books) {
                int quantity = Main.stock.getQuantity(book);
                messages.add(getStockBookString(book, quantity));
            }
        }
        return books;
    }

    private static Book[] commandCart() {
        Book[] cart = store.getCartContent();
        if (cart.length == 0) {
            messages.add("There are no books in your cart");
        } else {
            messages.add(getCartLegendString());
            int index = 0;
            for (Book book : cart) {
                messages.add(getCartBookString(index, book));
                index++;
            }
        }
        return cart;
    }

    private static String getCartBookString(int index, Book book) {
        return String.format("%8d %s", index, BookStore.getFormattedBookString(book));
    }

    private static String getStockBookString(Book book, int quantity) {
        return String.format("%8d %s", stock.getBookID(book), BookStore.getFormattedBookString(book, quantity));
    }

    private static String getCartLegendString() {
        return String.format("%8s %s", "Index", BookStore.getFormattedHeaderString());
    }

    private static String getStockLegendString() {
        return String.format("%8s %s", "ID", BookStore.getFormattedLegendStringWithQuantity());
    }

    private static BigDecimal commandBuy() {
        Book[] books = store.getCartContent();
        int[] bookStatus = store.buy(books);
        BigDecimal totalPrice = store.getTotalPrice(store.getCartContent(), bookStatus);
        for (int i = 0; i < bookStatus.length; i++) {
            int status = bookStatus[i];
            Book book = books[i];
            String secondColumn;
            switch (status) {
                case BookStore.OK:
                    secondColumn = String.format("%.2f", book.getPrice());
                    break;
                case BookStore.NOT_IN_STOCK:
                    secondColumn = "NOT IN STOCK";
                    break;
                case BookStore.DOES_NOT_EXIST:
                    secondColumn = "DOES NOT EXIST";
                    break;
                default:
                    secondColumn = "ERROR";
                    break;
            }
            messages.add(String.format("%24s%24s%16s", book.getTitle(), book.getAuthor(), secondColumn));
        }
        messages.add(String.format("%48s%16.2f", "TOTAL", totalPrice));
        return totalPrice;
    }

    private static boolean hasArgument(String[] args, int i) {
        return args.length > i;
    }

    private static void addCommandListMessages() {
        for (Map.Entry<String, String> cmdStrings : commandHelp.entrySet()) {
            messages.add(String.format("%8s - %s", cmdStrings.getKey(), cmdStrings.getValue()));
        }
        messages.add("Written as: \"command arg1;arg2;arg3...\"");
    }

    private static BigDecimal getDecimalArgument(String[] args, int index) {
        try {
            return new BigDecimal(args[2]);
        } catch (NumberFormatException ex) {
            messages.add(String.format("Argument number %d isn't a valid decimal number. (%s)", index + 1, args[index]));
        }
        return null;
    }

    private static Integer getPositiveIntegerArgument(String[] args, int index) {
        if (args != null) {
            try {
                Integer i = Integer.parseInt(args[index]);
                if (i < 0) {
                    throw new NumberFormatException();
                }
                return i;
            } catch (NumberFormatException ex) {
                messages.add(String.format("Argument number %d isn't a valid positive integer. (%s)", index + 1, args[index]));
            }
        }
        return null;
    }
}
