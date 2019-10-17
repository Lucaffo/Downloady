package it.xedoll.downloady.exceptions;

/**
 * Missing downloaded file name, this means that the Header of response does not
 * contain the name of the file and the last part of the url is not a file name.
 *
 * Correct Examples:
 *
 *      No Content-Disposition in header, but filename in the url
 *      https://www.google.com/drive/static/images/home/files.png
 *
 *      Content-Disposition in header, but filename not in the url
 *      https://www.google.com/downloadthebestfile
 *
 *      Content-Disposition in header and file name in the url, in
 *      this case, Downloady takes the first one.
 *      https://www.google.com/drive/static/images/home/files.png
 */
public class MissingFileNameException extends Exception
{
    public MissingFileNameException(String message) {
        super(message);
    }

    public MissingFileNameException() {
        super("Missing Content-Disposition or could not retrieve the downloading file name from url");
    }
}
