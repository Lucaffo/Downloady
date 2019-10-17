package it.xedoll.downloady.exceptions;

public class InvalidUrlException extends Exception
{
    public InvalidUrlException(String message) {
        super(message);
    }

    public InvalidUrlException() {
        super("Specify valid downloading urls");
    }
}
