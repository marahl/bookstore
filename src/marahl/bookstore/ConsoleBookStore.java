package marahl.bookstore;

import marahl.bookstore.books.Book;
import marahl.bookstore.books.BookParser;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;

public class ConsoleBookStore {

    private final String ADD_CART = "add";
    private final String REMOVE_CART = "remove";
    private final String CART = "cart";
    private final String ADD_STOCK = "addstock";
    private final String REMOVE_STOCK = "remstock";
    private final String FIND = "find";
    private final String LIST = "list";
    private final String BUY = "buy";
    private final String EXIT = "exit";
    private final String HELP = "help";
    private final LinkedHashMap<String, String> commandHelpMessages = new LinkedHashMap<>();
    private final LinkedHashMap<String, Consumer<String[]>> commands = new LinkedHashMap<>();
    private Consumer<String[]> helpCommand;

    private BookCart shoppingCart = new BookCart();
    private BookStore store = new BookStore();
    private LinkedList<String> messages = new LinkedList<>();

    public ConsoleBookStore() {
        initCommands();
    }

    private void initCommands() {
        commandHelpMessages.put(ADD_CART, "[id;(quantity)] Add a book to your cart");
        commandHelpMessages.put(REMOVE_CART, "[cartindex] Remove a book from your cart");
        commandHelpMessages.put(ADD_STOCK, "[title;author;price;quantity] Add a new book to the store's stock");
        commandHelpMessages.put(REMOVE_STOCK, "[id] Remove a book from the store's stock");
        commandHelpMessages.put(LIST, "[(searchstring)] List all books with that title or by that author\n" +
                "\t\t\tLists everything if no searchstring is specified");
        commandHelpMessages.put(CART, "Lists all books currently in your shopping cart");
        commandHelpMessages.put(BUY, "Buy contents of your shopping chart");
        commandHelpMessages.put(EXIT, "Exit program");
        commandHelpMessages.put(HELP, "List all available commands");

        helpCommand = (args) -> addCommandHelpMessages();
        commands.put(ADD_CART, this::commandAddToCart);
        commands.put(REMOVE_CART, this::commandRemoveFromCart);
        commands.put(ADD_STOCK, this::commandAddToStock);
        commands.put(REMOVE_STOCK, this::commandRemoveFromStock);
        commands.put(LIST, this::commandList);
        commands.put(CART, this::commandCart);
        commands.put(BUY, this::commandBuy);
        commands.put(EXIT, (args) -> System.exit(0));
        commands.put(HELP, helpCommand);

    }

    public void addStock(String stockString) {
        try {
            store.addBatch(BookParser.parseBooks(stockString));
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }


    public void start() {
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

    private void executeCommand(String line) {
        String[] stringarr = line.split(" ", 2);
        String command = stringarr[0].toLowerCase();
        String[] args = new String[0];
        if (stringarr.length > 1) {
            args = stringarr[1].split(";");
        }
        commands.getOrDefault(command, helpCommand).accept(args);
    }

    private void commandAddToCart(String... args) {
        if (hasArgument(args, 0)) {
            Integer bookId = getPositiveIntegerArgument(args, 0);
            if (bookId == null) return;
            Book book = store.getBook(bookId);
            if (book == null) {
                messages.add(String.format("Couldn't find any book with the id %s", args[0]));
                return;
            }
            Integer quantity = 1;
            if (hasArgument(args, 1)) {
                quantity = getPositiveIntegerArgument(args, 1);
                if (quantity == null) return;
            }
            boolean b = shoppingCart.addToCart(book, quantity);
            if (b) {
                messages.add(String.format("Successfully added %dx %s by %s to cart!", quantity, book.getTitle(), book.getAuthor()));
            } else {
                messages.add(String.format("Failed to add %dx %s by %s to cart!", quantity, book.getTitle(), book.getAuthor()));
            }
        } else {
            messages.add("Too few arguments! Need at least a book title.");
        }
    }

    private void commandRemoveFromCart(String... args) {
        if (hasArgument(args, 0)) {
            Integer cartIndex = getPositiveIntegerArgument(args, 0);
            if (cartIndex != null) {
                try {
                    Book book = shoppingCart.removeFromCart(cartIndex);
                    messages.add(String.format("Successfully removed %s by %s from your cart", book.getTitle(), book.getAuthor()));
                } catch (IndexOutOfBoundsException ex) {
                    messages.add(String.format("No book in cart on index %d", cartIndex));
                }
            }
        } else {
            messages.add("Too few arguments! Need a cart index.");
        }
    }

    private void commandAddToStock(String... args) {
        if (hasArgument(args, 3)) {
            String title = args[0];
            String author = args[1];
            BigDecimal price = getDecimalArgument(args, 2);
            Integer quantity = getPositiveIntegerArgument(args, 3);
            if (price == null || quantity == null) {
                return;
            }
            Book book = new Book(title, author, price);
            store.add(book, quantity);
            messages.add("Added a new book to the store:");
            messages.add(getBookString(book));
        } else {
            messages.add(String.format("Too few arguments! Need 4 arguments but only found %d (title;author;price;quantity)", args.length));
            messages.add(Arrays.toString(args));
        }
    }

    private void commandRemoveFromStock(String... args) {
        if (hasArgument(args, 0)) {
            Integer bookId = getPositiveIntegerArgument(args, 0);
            if (bookId == null) return;
            if (hasArgument(args, 1)) {
                Integer quantity = getPositiveIntegerArgument(args, 1);
                if (quantity == null) return;
                store.reduceQuantity(bookId, quantity);
            } else {
                Map.Entry<Book, Integer> removedBook = store.remove(bookId);
                if (removedBook != null) {
                    messages.add("Removed book from the store:");
                    messages.add(getBookString(removedBook.getKey()));
                } else {
                    messages.add("No books where removed");
                }
            }
        } else {
            messages.add("Too few arguments! Need at least the ID of the book to remove, quantity to remove is optional");
        }
    }


    private void commandList(String... args) {
        String searchString = "";
        if (hasArgument(args, 0)) {
            searchString = args[0];
        }
        Book[] list = store.list(searchString);
        if (list.length > 0) {
            messages.add(getStockHeaderString());
            for (Book book : list) {
                messages.add(getStockBookString(book, store.getQuantity(book)));
            }
        } else {
            messages.add("Couldn't find anything");
        }
    }

    private void commandCart(String... args) {
        Book[] cart = shoppingCart.getCartContent();
        if (cart.length == 0) {
            messages.add("There are no books in your cart");
        } else {
            messages.add(getCartHeaderString());
            int index = 0;
            for (Book book : cart) {
                messages.add(getCartBookString(index, book));
                index++;
            }
        }
    }

    private void commandBuy(String... args) {
        Book[] books = shoppingCart.getCartContent();
        int[] bookStatus = store.buy(books);
        BigDecimal totalPrice = store.getPrice(books, bookStatus);
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
    }

    private static String getCartHeaderString() {
        return String.format("%8s%s", "Index", getHeaderString());
    }

    private static String getStockHeaderString() {
        return String.format("%8s%s%8s", "ID", getHeaderString(), "Qty");
    }

    private static String getHeaderString() {
        String title = "Title";
        String author = "Author";
        String price = "Price";
        return String.format("%24s%24s%16s", title, author, price);
    }

    private String getCartBookString(int cartIndex, Book book) {
        return String.format("%8d%s", cartIndex, getBookString(book));
    }

    private String getStockBookString(Book book, int quantity) {
        return String.format("%8d%s%8d", store.getBookID(book), getBookString(book), quantity);
    }

    private String getBookString(Book book) {
        String title = book.getTitle();
        String author = book.getAuthor();
        BigDecimal price = book.getPrice();
        return String.format("%24s%24s%16.2f", title, author, price);
    }

    private boolean hasArgument(String[] args, int i) {
        return args.length > i;
    }

    private void addCommandHelpMessages() {
        for (Map.Entry<String, String> cmdStrings : commandHelpMessages.entrySet()) {
            messages.add(String.format("%8s - %s", cmdStrings.getKey(), cmdStrings.getValue()));
        }
        messages.add("Written as: \"command arg1;arg2;arg3...\"");
    }

    private BigDecimal getDecimalArgument(String[] args, int index) {
        try {
            return new BigDecimal(args[index]);
        } catch (NumberFormatException ex) {
            messages.add(String.format("Argument number %d isn't a valid decimal number. (%s)", index + 1, args[index]));
        }
        return null;
    }

    private Integer getPositiveIntegerArgument(String[] args, int index) {
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
