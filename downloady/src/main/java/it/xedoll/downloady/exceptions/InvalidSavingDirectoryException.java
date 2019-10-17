package it.xedoll.downloady.exceptions;

public class InvalidSavingDirectoryException extends Exception {

    public InvalidSavingDirectoryException(String message) {
        super(message);
    }

    public InvalidSavingDirectoryException() {
        super("Specify a valid saving directory");
    }
}
