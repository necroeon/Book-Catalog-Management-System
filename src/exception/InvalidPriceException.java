// -----------------------------------------------------
// Assignment 2
// Written by: Md Khairul Enam Adib 40211709
// -----------------------------------------------------


package exception;

public class InvalidPriceException extends Exception {
    public InvalidPriceException(String message, String line, String fileName) {
        super(message + " | Record: " + line + " | File: " + fileName);
    }
}
