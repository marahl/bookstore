package marahl.bookstore;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String DEFAULT_BOOKS = "Mastering åäö;Average Swede;762.00;15\n" +
            "How To Spend Money;Rich Bloke;1,000,000.00;1\n" +
            "Generic Title;First Author;185.50;5\n" +
            "Generic Title;Second Author;1,748.00;3\n" +
            "Random Sales;Cunning Bastard;999.00;20\n" +
            "Random Sales;Cunning Bastard;499.50;3\n" +
            "Desired;Rich Bloke;564.50;0";
    private static final ConsoleBookStore consoleBookStore = new ConsoleBookStore();

    public static void main(String[] args) {
        String argument;
        if (args.length > 0) {
            argument = args[0];
        } else {
            argument = DEFAULT_BOOKS;
        }
        String bookString;
        if ((bookString = getStringFromURL(argument)).isEmpty()) {
            bookString = getStringFromFile(argument);
        }
        consoleBookStore.addStock(bookString);
        consoleBookStore.start();
    }


    private static String getStringFromInput(InputStream in) {
        Scanner sc = new Scanner(in);
        List<String> bookStrings = new ArrayList<>();
        while (true) {
            String line = sc.nextLine();
            if (line.isEmpty()) {
                break;
            }
            bookStrings.add(line);
        }
        return String.join("\n", bookStrings);
    }

    private static String getStringFromFile(String filename) {
        String string = "";
        try {
            string = String.join("\n", Files.readAllLines(new File(filename).toPath()));
        } catch (Exception e) {
        }
        return string;
    }

    private static String getStringFromURL(String urlString) {
        StringBuilder stringBuilder = new StringBuilder();
        if (urlString != null) {
            try (Scanner sc = new Scanner((new URL(urlString)).openStream())) {
                while (sc.hasNextLine()) {
                    stringBuilder.append(sc.nextLine()).append('\n');
                }
            } catch (IOException e) {
            }
        }
        return stringBuilder.toString();
    }
}

