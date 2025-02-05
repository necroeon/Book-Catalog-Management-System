// -----------------------------------------------------
// Assignment 2
// Written by: Md Khairul Enam Adib 40211709
// -----------------------------------------------------


package def;

import java.io.Serializable;

class Book implements Serializable {
    private String title;
    private String author;
    private double price;
    private String isbn;
    private String genre;
    private int year;

    public Book(String title, String author, double price, String isbn, String genre, int year) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.isbn = isbn;
        this.genre = genre;
        this.year = year;
    }

    public Book(String string, String string2) {
		
	}

	@Override
    public String toString() {
        return title + ", " + author + ", " + price + ", " + isbn + ", " + genre + ", " + year;
    }
}
