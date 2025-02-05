// -----------------------------------------------------
// Assignment 2
// Written by: Md Khairul Enam Adib 40211709
// -----------------------------------------------------



package def;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.io.*;

import exception.InvalidISBNException;
import exception.InvalidPriceException;
import exception.InvalidYearException;
import exception.MissingFieldException;
import exception.TooFewFieldsException;
import exception.TooManyFieldsException;
import exception.UnknownGenreException;

public class Driver {
	    private static final String[] GENRE_CODES = {"CCB", "HCB", "MTV", "MRB", "NEB", "OTR", "SSM", "TPA"};
	    private static final String[] GENRE_FILES = {
	        "Cartoons_Comics_Books.csv.txt", "Hobbies_Collectibles_Books.csv.txt", "Movies_TV.csv.txt",
	        "Music_Radio_Books.csv.txt", "Nostalgia_Eclectic_Books.csv.txt", "Old_Time_Radio.csv.txt",
	        "Sports_Sports_Memorabilia.csv.txt", "Trains_Planes_Automobiles.csv.txt"
	    };
	    
	    
	    private static final String SEMANTIC_ERROR_LOG_FILE = "semantic_error_file.txt";
	    private static final String ERROR_LOG_FILE = "syntax_error_file.txt";
	    private static final String FILE_NAME = "part1_input_file_names.txt";
	    private static final int MAX_BOOKS = 100;
	    
	    public static void main(String[] args) {
	        do_part1();
	        do_part2();
	        do_part3();
	    }

	    public static void do_part1() {
	        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                if (line.matches("^[a-zA-Z]+\\d+\\.csv\\.txt$")) {
	                    processBookFile(line);
	                }
	            }
	        } catch (IOException e) {
	            System.err.println("Error reading file: " + e.getMessage());
	        }
	    }
	    
	    public static void do_part2() {
	        for (String genreFile : GENRE_FILES) {
	            Book[] books = new Book[MAX_BOOKS];
	            int bookIndex = 0;
	            try (BufferedReader reader = new BufferedReader(new FileReader(genreFile))) {
	                String line;
	                while ((line = reader.readLine()) != null) {
	                    try {
	                        String[] fields = line.split(",");
	                        if (fields.length != 6) continue; // skip invalid lines

	                        String title = fields[0].replaceAll("\"", "").trim();
	                        String author = fields[1].trim();
	                        double price = validatePrice(fields[2].trim(), line, genreFile);
	                        long isbn = validateISBN(fields[3].trim(), line, genreFile);
	                        String genre = fields[4].trim();
	                        int year = validateYear(fields[5].trim(), line, genreFile);

	                        Book book = new Book(title, author, price, String.valueOf(isbn), genre, year);
	                        books[bookIndex++] = book;

	                        if (bookIndex == MAX_BOOKS) {
	                            serializeBooks(books, genreFile, bookIndex);
	                            bookIndex = 0;
	                        }
	                    } catch (Exception e) {
	                        logSemanticError(e.getMessage(), line, genreFile);
	                    }
	                }
	                // Serialize remaining books in the array if any
	                if (bookIndex > 0) {
	                    serializeBooks(books, genreFile, bookIndex);
	                }
	            } catch (IOException e) {
	                System.err.println("Error processing file: " + genreFile + " - " + e.getMessage());
	            }
	        }
	    }

	    private static void processBookFile(String bookFileName) {
	        try (BufferedReader reader = new BufferedReader(new FileReader(bookFileName))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                try {
	                    String[] fields = line.split(",");
	                    validateFieldsCount(fields, line, bookFileName);
	                    String title = fields[0].replaceAll("\"", "").trim();
	                    String author = fields[1].trim();
	                    double price = validatePrice(fields[2].trim(), line, bookFileName);
	                    long isbn = validateISBN(fields[3].trim(), line, bookFileName);
	                    String genre = validateGenre(fields[4].trim(), line, bookFileName);
	                    int year = validateYear(fields[5].trim(), line, bookFileName);
	                    writeToGenreFile(title, author, price, isbn, genre, year);
	                } catch (Exception e) {
	                    logSyntaxError(e.getMessage(), line, bookFileName);
	                }
	            }
	        } catch (IOException e) {
	            System.err.println("Error processing book file: " + e.getMessage());
	        }
	    }

	    private static void validateFieldsCount(String[] fields, String line, String bookFileName) throws Exception {
	        if (fields.length < 6) throw new TooFewFieldsException("missing field(s)", line, bookFileName);
	        if (fields.length > 6) throw new TooManyFieldsException("too many fields", line, bookFileName);
	    }

	    private static double validatePrice(String priceStr, String line, String bookFileName) throws Exception {
	        if (priceStr.isEmpty()) throw new MissingFieldException("missing price", line, bookFileName);
	        double price = Double.parseDouble(priceStr);
	        if (price < 0) throw new InvalidPriceException("negative price", line, bookFileName);
	        return price;
	    }

	    private static long validateISBN(String isbnStr, String line, String bookFileName) throws Exception {
	        if (isbnStr.isEmpty()) throw new MissingFieldException("missing ISBN", line, bookFileName);
	        long isbn = Long.parseLong(isbnStr);
	        if (isbnStr.length() == 10 && !isValidISBN10(isbnStr)) {
	            throw new InvalidISBNException("invalid ISBN-10", line, bookFileName);
	        } else if (isbnStr.length() == 13 && !isValidISBN13(isbnStr)) {
	            throw new InvalidISBNException("invalid ISBN-13", line, bookFileName);
	        }
	        return isbn;
	    }

	    private static String validateGenre(String genre, String line, String bookFileName) throws Exception {
	        if (genre.isEmpty()) throw new MissingFieldException("missing genre", line, bookFileName);
	        for (String validGenre : GENRE_CODES) {
	            if (genre.equals(validGenre)) return genre;
	        }
	        throw new UnknownGenreException("unknown genre code", line, bookFileName);
	    }

	    private static int validateYear(String yearStr, String line, String bookFileName) throws Exception {
	        if (yearStr.isEmpty()) throw new MissingFieldException("missing year", line, bookFileName);
	        int year = Integer.parseInt(yearStr);
	        if (year < 1995 || year > 2010) throw new InvalidYearException("invalid year", line, bookFileName);
	        return year;
	    }

	    private static boolean isValidISBN10(String isbn) {
	        int sum = 0;
	        for (int i = 0; i < 10; i++) {
	            sum += (10 - i) * (isbn.charAt(i) - '0');
	        }
	        return sum % 11 == 0;
	    }

	    private static boolean isValidISBN13(String isbn) {
	        int sum = 0;
	        for (int i = 0; i < 13; i++) {
	            int digit = isbn.charAt(i) - '0';
	            sum += (i % 2 == 0) ? digit : digit * 3;
	        }
	        return sum % 10 == 0;
	    }

	    private static void writeToGenreFile(String title, String author, double price, long isbn, String genre, int year) {
	        int index = java.util.Arrays.asList(GENRE_CODES).indexOf(genre);
	        if (index != -1) {
	            String fileName = GENRE_FILES[index];
	            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
	                writer.write(String.format("%s,%s,%.2f,%d,%s,%d%n", title, author, price, isbn, genre, year));
	            } catch (IOException e) {
	                System.err.println("Error writing to file: " + e.getMessage());
	            }
	        }
	    }

	    private static void logSyntaxError(String error, String line, String bookFileName) {
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ERROR_LOG_FILE, true))) {
	            writer.write("syntax error in file: " + bookFileName + "\n");
	            writer.write("====================\n");
	            writer.write("Error: " + error + "\n");
	            writer.write("Record: " + line + "\n\n");
	        } catch (IOException e) {
	            System.err.println("Error writing to error log file: " + e.getMessage());
	        }
	    }
	    
	    private static void logSemanticError(String error, String line, String genreFile) {
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SEMANTIC_ERROR_LOG_FILE, true))) {
	            writer.write("semantic error in file: " + genreFile + "\n");
	            writer.write("====================\n");
	            writer.write("Error: " + error + "\n");
	            writer.write("Record: " + line + "\n\n");
	        } catch (IOException e) {
	            System.err.println("Error writing to error log file: " + e.getMessage());
	        }
	    }
	    
	    private static void serializeBooks(Book[] books, String genreFile, int numBooks) {
	        String serFileName = genreFile.replace(".csv.txt", ".csv.ser");
	        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serFileName, true))) {
	            for (int i = 0; i < numBooks; i++) {
	                out.writeObject(books[i]);
	            }
	        } catch (IOException e) {
	            System.err.println("Error serializing books to file: " + serFileName + " - " + e.getMessage());
	        }
	    }
	    	    
	    private static final String[] CSV_FILES = {
	            "Cartoons_Comics_Books.csv.ser", "Hobbies_Collectibles_Books.csv.ser",
	            "Movies_TV.csv.ser", "Music_Radio_Books.csv.ser",
	            "Nostalgia_Eclectic_Books.csv.ser", "Old_Time_Radio.csv.ser",
	            "Sports_Sports_Memorabilia.csv.ser", "Trains_Planes_Automobiles.csv.ser"
	        };

	        private static Book[][] bookArrays = new Book[CSV_FILES.length][];
	        private static int selectedFileIndex = 0;
	        private static int currentIndex = 0;

	        public static void do_part3() {
	            readCSVFiles();
	            displayMainMenu();
	        }

	        private static void readCSVFiles() {
	            for (int i = 0; i < CSV_FILES.length; i++) {
	                String filename = CSV_FILES[i];
	                int lineCount = countLinesInFile(filename);

	                if (lineCount > 0) {
	                    Book[] books = new Book[lineCount];
	                    loadBooksFromFile(filename, books);
	                    bookArrays[i] = books;
	                } else {
	                    bookArrays[i] = new Book[0];
	                }
	            }
	        }

	        private static int countLinesInFile(String filename) {
	            int lineCount = 0;
	            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
	                while (reader.readLine() != null) {
	                    lineCount++;
	                }
	            } catch (IOException e) {
	                System.err.println("Error counting lines in file: " + filename + " - " + e.getMessage());
	            }
	            return lineCount;
	        }

	        private static void loadBooksFromFile(String filename, Book[] books) {
	            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
	                String line;
	                int index = 0;

	                while ((line = reader.readLine()) != null) {
	                    String[] data = line.split(","); // Assuming CSV format "title,author,price,isbn,genre,year"

	                    if (data.length == 6) {
	                        try {
	                            String title = data[0].trim();
	                            String author = data[1].trim();
	                            double price = Double.parseDouble(data[2].trim());
	                            String isbn = data[3].trim();
	                            String genre = data[4].trim();
	                            int year = Integer.parseInt(data[5].trim());

	                            books[index] = new Book(title, author, price, isbn, genre, year);
	                            index++;
	                        } catch (NumberFormatException e) {
	                            System.err.println("Error parsing numeric data in line: " + line);
	                        }
	                    } else {
	                        System.err.println("Invalid line format in file: " + line);
	                    }
	                }
	            } catch (IOException e) {
	                System.err.println("Error reading file: " + filename + " - " + e.getMessage());
	            }
	        }

	        private static void displayMainMenu() {
	            Scanner scanner = new Scanner(System.in);
	            String choice;

	            while (true) {
	                System.out.println("\n-----------------------------");
	                System.out.printf("Main Menu\n-----------------------------\n");
	                System.out.printf("v View the selected file: %s (%d records)\n", CSV_FILES[selectedFileIndex], bookArrays[selectedFileIndex].length);
	                System.out.println("s Select a file to view");
	                System.out.println("x Exit");
	                System.out.println("-----------------------------");
	                System.out.print("Enter Your Choice: ");
	                choice = scanner.nextLine().trim().toLowerCase();

	                switch (choice) {
	                    case "v":
	                        viewSelectedFile(scanner);
	                        break;
	                    case "s":
	                        selectFile(scanner);
	                        break;
	                    case "x":
	                        System.out.println("Exiting...");
	                        return;
	                    default:
	                        System.out.println("Invalid choice, please try again.");
	                }
	            }
	        }

	        private static void selectFile(Scanner scanner) {
	            System.out.println("\n------------------------------");
	            System.out.println("File Sub-Menu");
	            System.out.println("------------------------------");
	            for (int i = 0; i < CSV_FILES.length; i++) {
	                System.out.printf("%d %s (%d records)\n", i + 1, CSV_FILES[i], bookArrays[i].length);
	            }
	            System.out.println("9 Exit");
	            System.out.println("------------------------------");
	            System.out.print("Enter Your Choice: ");
	            
	            int choice = scanner.nextInt();
	            scanner.nextLine(); // consume newline

	            if (choice >= 1 && choice <= CSV_FILES.length) {
	                selectedFileIndex = choice - 1;
	                currentIndex = 0;
	            } else if (choice != 9) {
	                System.out.println("Invalid choice, returning to main menu.");
	            }
	        }

	        private static void viewSelectedFile(Scanner scanner) {
	            Book[] books = bookArrays[selectedFileIndex];
	            if (books.length == 0) {
	                System.out.println("No records in the selected file.");
	                return;
	            }

	            while (true) {
	                System.out.printf("\nCurrently viewing: %s (%d records)\n", CSV_FILES[selectedFileIndex], books.length);
	                System.out.print("Enter range to display (e.g., +3, -2, or 0 to go back): ");
	                
	                int n = scanner.nextInt();
	                scanner.nextLine(); // consume newline

	                if (n == 0) {
	                    System.out.println("Returning to main menu.");
	                    break;
	                }

	                displayRecords(books, n);
	            }
	        }

	        private static void displayRecords(Book[] books, int n) {
	            if (n > 0) { // Moving downwards
	                int end = Math.min(currentIndex + n, books.length);
	                for (int i = currentIndex; i < end; i++) {
	                    System.out.println(books[i]);
	                }
	                if (end == books.length) {
	                    System.out.println("EOF has been reached.");
	                }
	                currentIndex = end - 1;
	            } else { // Moving upwards
	                int start = Math.max(currentIndex + n, 0);
	                for (int i = start; i <= currentIndex; i++) {
	                    System.out.println(books[i]);
	                }
	                if (start == 0) {
	                    System.out.println("BOF has been reached.");
	                }
	                currentIndex = start;
	            }
	        }
}












