package it.xedoll.downloady;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import it.xedoll.downloady.exceptions.InvalidSavingDirectoryException;
import it.xedoll.downloady.exceptions.InvalidUrlException;
import it.xedoll.downloady.exceptions.MissingFileNameException;
import it.xedoll.downloady.interfaces.DownloadMultiple;
import it.xedoll.downloady.interfaces.DownloadMultipleParallel;
import it.xedoll.downloady.interfaces.DownloadSingle;

/**
 * Downloady allow the download of single or multiple files.
 *
 * Functionality:
 * - Return a callback for all downloads
 * - Specify a scheduled time for the download.
 * - Statistics for your downloads, can be specified the suffix, if should be runtime or ever.
 */
public class Downloady
{

    /**
     * Class Constants
     */
    public static final int SUCCESS = 0;
    public static final int FAILED = -1;

    //Urls to download
    private int urlsNumber = 0;

    /**
     * Download single file using a string url, a string path where the file will be downloaded and
     * a callback to get results or errors.
     *
     * @param url to download the file
     * @param savingPath where the file will be saved
     * @param callback to return results or errors
    */
    public void download(String url, String savingPath, DownloadSingle callback)
    {
        // Check saving directory or object
        URL urlObject = Utils.checkURL(url);
        File savingDir = Utils.checkDir(savingPath);

        // Use the already written function
        download(urlObject, savingDir, callback);
    }

    public void download(URL url, String savingPath, DownloadSingle callback)
    {
        // Check saving directory or object
        File savingDir = Utils.checkDir(savingPath);

        // Use the already written function
        download(url, savingDir, callback);
    }

    public void download(String url, File savingDir, DownloadSingle callback)
    {
        // Check saving directory or object and create the urlObject
        URL urlObject = Utils.checkURL(url);

        // Use the already written function
        download(urlObject, savingDir, callback);
    }

    public void download(URL url, File savingDir, DownloadSingle callback)
    {
        // Check saving directory or object
        File dir = Utils.checkDir(savingDir);

        // Quick check url and saving path/dir
        if(url == null) {
            callback.onError(new InvalidUrlException());
            return;
        }
        if(dir == null) {
            callback.onError(new InvalidSavingDirectoryException());
            return;
        }

        // Create a new Downloady Task
        DownloadyTask downloadyTask = new DownloadyTask();
        downloadyTask.downloadSingle(callback, url, dir);

        // Start this task
        downloadyTask.execute(url);
    }

    /**
     * Download multiple file async (But in sync order) using strings urls, a string path where
     * the file will be downloaded and a callback to get results or errors.
     * @param urls
     * @param callback
     */
    public void download(String[] urls, String savingPath, DownloadMultiple callback)
    {
        // Check saving directory or object
        File savingDir = Utils.checkDir(savingPath);

        download(urls, savingDir, callback);
    }

    public void download(String[] urls, File savingDir, DownloadMultiple callback)
    {

        // Create an array of URL
        URL[] urlsObjects = new URL[urls.length];

        // Populate the array of URL
        for(int i = 0; i < urls.length; i++)
        {
            URL f = Utils.checkURL(urls[i]);

            // While creating the array, check for malformed or invalid urls
            if(f == null){
                callback.onError(new MalformedURLException(), i);
                return;
            }

            // Assign the url to the urls array
            urlsObjects[i] = f;
        }

        // Check saving directory or object
        if(savingDir == null || !savingDir.exists())
        {
            callback.onError(new InvalidSavingDirectoryException(), -1);
            return;
        }

        // Create a new Downloady Task
        DownloadyTask downloadyTask = new DownloadyTask();
        downloadyTask.downloadMultiple(callback, urlsObjects, savingDir);

        // Start this task
        downloadyTask.execute(urlsObjects);
    }

    public void download(String[] urls, File savingDir, DownloadMultipleParallel callback)
    {

        // Create an array of URL
        URL[] urlsObjects = new URL[urls.length];

        // Populate the array of URL
        for(int i = 0; i < urls.length; i++)
        {
            URL f = Utils.checkURL(urls[i]);

            // While creating the array, check for malformed or invalid urls
            if(f == null){
                callback.onError(new MalformedURLException(), i);
                return;
            }

            // Assign the url to the urls array
            urlsObjects[i] = f;
        }

        // Check saving directory or object
        if(savingDir == null || !savingDir.exists())
        {
            callback.onError(new InvalidSavingDirectoryException(), -1);
            return;
        }

        for(int i = 0; i < urls.length; i++){

            // Create a new Downloady Task
            DownloadyTask downloadyTask = new DownloadyTask();
            downloadyTask.downloadMultipleParallel(callback, urlsObjects[i], savingDir, urls.length);

            // Execute in parallel
            downloadyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urlsObjects[i]);
        }
    }

    // Failed and completed downloads
    private List<String> failedDownloads = new ArrayList<>();
    private List<String> successDownloads = new ArrayList<>();

    // Downloady AsyncTask
    class DownloadyTask extends AsyncTask<URL, Integer, Void> {

        private URL[] urls;
        private int urlsNumber = 0;

        private File dir;

        // Callbacks types
        private Object downloadCallback;

        // Current index in urls to download
        int currentFileIndex;

        // Download multiple files with callback
        void downloadMultiple(DownloadMultiple callback, URL[] urls, File savingDir)
        {
            // Quick check url and saving path/dir
            if(urls == null) {
                callback.onError(new InvalidUrlException(), -1);
                return;
            }
            if(savingDir == null || !savingDir.exists() || !savingDir.isDirectory()) {
                callback.onError(new InvalidSavingDirectoryException(), -1);
                return;
            }

            // Set the download multiple callback
            this.downloadCallback = callback;

            // Set the number of files to download in serial
            this.urlsNumber = urls.length;

            // Set the urls, to download the files
            this.urls = urls;

            // Set the directory where the file will be saved
            this.dir = savingDir;
        }

        // Download multiple files with callback
        void downloadMultipleParallel(DownloadMultipleParallel callback, URL url, File savingDir, int urlsNumber)
        {
            // Quick check url and saving path/dir
            if(url == null) {
                callback.onError(new InvalidUrlException(), -1);
                return;
            }
            if(savingDir == null || !savingDir.exists() || !savingDir.isDirectory()) {
                callback.onError(new InvalidSavingDirectoryException(), -1);
                return;
            }

            // Set the download multiple callback
            this.downloadCallback = callback;

            // Set the number of files to download in parallel
            this.urlsNumber = urlsNumber;

            // Set the urls, to download the files
            this.urls = new URL[]{url};

            // Set the directory where the file will be saved
            this.dir = savingDir;
        }

        // Download single file with callback
        void downloadSingle(DownloadSingle callback, URL url, File savingDir)
        {
            // Quick check url and saving path/dir
            if(url == null) {
                callback.onError(new InvalidUrlException());
                return;
            }
            if(savingDir == null || !savingDir.exists() || !savingDir.isDirectory()) {
                callback.onError(new InvalidSavingDirectoryException());
                return;
            }

            // Set tge download single callback
            this.downloadCallback = callback;

            // Set the urls, to download the files
            this.urls = new URL[]{url};

            // Set the directory where the file will be saved
            this.dir = savingDir;
        }

        // Do the task in background/non UI thread
        protected Void doInBackground(URL...urls)
        {
            int count = urls.length;
            HttpURLConnection connection = null;

            // Loop through the urls
            for(int i = 0; i < count; i++)
            {
                // Update the currentFileIndex
                this.currentFileIndex = i;

                // Get the current url
                URL currentURL = urls[i];

                // If is not a valid url
                if(currentURL == null){
                    if(downloadCallback instanceof DownloadSingle){
                        ((DownloadSingle) downloadCallback).onError(new MalformedURLException());
                    }
                    if(downloadCallback instanceof DownloadMultiple){
                        ((DownloadMultiple) downloadCallback).onError(new MalformedURLException(), i);
                    }
                    if(downloadCallback instanceof DownloadMultipleParallel){
                        ((DownloadMultipleParallel) downloadCallback).onError(new MalformedURLException(), i);
                    }
                    continue;
                }

                // So download the file from the url
                try{

                    // Check the if the url use ssl
                    if(currentURL.getProtocol().toLowerCase().equals("https")){
                        // Initialize the https url connection
                        connection = (HttpsURLConnection) currentURL.openConnection();
                    }else{
                        // Initialize the http url connection
                        connection = (HttpURLConnection) currentURL.openConnection();
                    }

                    // Connect
                    connection.connect();

                    // Get the input stream from http url connection
                    InputStream inputStream = connection.getInputStream();

                    // Try to get the file name from the header
                    String contentDisposition = connection.getHeaderField("Content-Disposition");
                    String savingFileName;

                    if(contentDisposition != null && contentDisposition.contains("=")){
                        savingFileName = contentDisposition.split("=")[1];
                    }else{
                        // Could not retrieve the file name from header, try to extract from the url
                        savingFileName = Utils.getFileNameFromUrl(currentURL);

                        if(savingFileName.isEmpty()){
                            if(downloadCallback instanceof DownloadSingle){
                                ((DownloadSingle) downloadCallback).onError(new MissingFileNameException());
                            }
                            if(downloadCallback instanceof DownloadMultiple){
                                ((DownloadMultiple) downloadCallback).onError(new MissingFileNameException(), i);
                            }
                            if(downloadCallback instanceof DownloadMultipleParallel){
                                ((DownloadMultipleParallel) downloadCallback).onError(new MissingFileNameException(), i);
                            }
                            continue;
                        }
                    }

                    // Before start downloading
                    if(downloadCallback instanceof DownloadSingle){
                        ((DownloadSingle) downloadCallback).onStartDownload(savingFileName, currentURL.toString());
                    }
                    if(downloadCallback instanceof DownloadMultiple){
                        ((DownloadMultiple) downloadCallback).onStartDownload(savingFileName, urlsNumber);
                    }
                    if(downloadCallback instanceof DownloadMultipleParallel){
                        ((DownloadMultipleParallel) downloadCallback).onStartDownload(savingFileName, urlsNumber);
                    }

                    // Get the file length, might be -1 if server did not report the length
                    int fileLength = connection.getContentLength();

                    // Try to open the BufferedInputStream and the FileOutputStream
                    try (BufferedInputStream in = new BufferedInputStream(inputStream);
                         FileOutputStream fileOutputStream = new FileOutputStream(new File(dir, savingFileName)))
                    {
                        byte []dataBuffer = new byte[1024];
                        int bytesRead;
                        long total = 0;

                        // Get the first 1024 bytes
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {

                            // Total bytes read
                            if(bytesRead > 0) total += bytesRead;

                            if(fileLength != -1){
                                if(downloadCallback instanceof DownloadSingle){
                                    ((DownloadSingle) downloadCallback).onDownload(savingFileName, (total * 100 / fileLength));
                                }
                                if(downloadCallback instanceof DownloadMultiple){
                                    ((DownloadMultiple) downloadCallback).onDownload(savingFileName, (total*100 / fileLength), currentFileIndex, urlsNumber);
                                }
                                if(downloadCallback instanceof DownloadMultipleParallel){
                                    ((DownloadMultipleParallel) downloadCallback).onDownload(savingFileName, (total*100 / fileLength), currentFileIndex, urlsNumber,failedDownloads.size() + successDownloads.size());
                                }
                            }else{
                                if(downloadCallback instanceof DownloadSingle){
                                    ((DownloadSingle) downloadCallback).onError(new MissingFileNameException());
                                }
                                if(downloadCallback instanceof DownloadMultiple){
                                    ((DownloadMultiple) downloadCallback).onError(new MissingFileNameException(), i);
                                }
                                if(downloadCallback instanceof DownloadMultipleParallel){
                                    ((DownloadMultipleParallel) downloadCallback).onError(new MissingFileNameException(), i);
                                }
                                continue;
                            }

                            // Write into the file
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                        }

                        // Single file completed, report the callback
                        if(downloadCallback instanceof DownloadMultiple){
                            ((DownloadMultiple) downloadCallback).onSingleCompleted(SUCCESS, savingFileName, currentFileIndex);
                        }
                        // Single file completed, report the callback
                        if(downloadCallback instanceof DownloadMultipleParallel){
                            ((DownloadMultipleParallel) downloadCallback).onSingleCompleted(SUCCESS, savingFileName, currentFileIndex, failedDownloads.size() + successDownloads.size());
                        }

                        // Close the streams
                        fileOutputStream.flush();
                        fileOutputStream.close();

                        // File written successfully
                        successDownloads.add(dir.getPath());

                    } catch (IOException e) {
                        // Some errors while reading/writing the file, report the callback
                        if(downloadCallback instanceof DownloadSingle){
                            ((DownloadSingle) downloadCallback).onError(e);
                        }
                        if(downloadCallback instanceof DownloadMultiple){
                            ((DownloadMultiple) downloadCallback).onError(e, currentFileIndex);
                            ((DownloadMultiple) downloadCallback).onSingleCompleted(FAILED, currentURL.getPath(), currentFileIndex);
                        }
                        // Single file completed, report the callback
                        if(downloadCallback instanceof DownloadMultipleParallel){
                            ((DownloadMultipleParallel) downloadCallback).onError(e, currentFileIndex);
                            ((DownloadMultipleParallel) downloadCallback).onSingleCompleted(FAILED, currentURL.getPath(), currentFileIndex, failedDownloads.size() + successDownloads.size());
                        }

                        // Add to the failed downloads
                        failedDownloads.add(currentURL.getPath());
                    }

                }catch(Exception e){

                    // Next file, some errors in BufferedInputStream or FileOutputStream
                    if(downloadCallback instanceof DownloadSingle){
                        ((DownloadSingle) downloadCallback).onError(e);
                    }
                    if(downloadCallback instanceof DownloadMultiple){
                        ((DownloadMultiple) downloadCallback).onError(e, currentFileIndex);
                    }
                    if(downloadCallback instanceof DownloadMultipleParallel){
                        ((DownloadMultipleParallel) downloadCallback).onError(e, currentFileIndex);
                    }

                    // Add to the failed downloads
                    failedDownloads.add(currentURL.getPath());
                }finally{
                    // Disconnect the http url connection
                    connection.disconnect();
                }
            }

            // All files are processed
            return null;
        }

        // On AsyncTask cancelled
        protected void onCancelled(){

        }

        // When all async task done
        protected void onPostExecute(Void result){

            // Single download completed
            if(downloadCallback instanceof DownloadSingle){
                if(failedDownloads.size() >= 1){
                    ((DownloadSingle) downloadCallback).onCompleted(FAILED);
                }else{
                    ((DownloadSingle) downloadCallback).onCompleted(SUCCESS);
                }
            }

            // Multiple downloads completed
            if(downloadCallback instanceof DownloadMultiple){
                ((DownloadMultiple) downloadCallback).onCompleted(
                        successDownloads.toArray(new String[0]),
                        failedDownloads.toArray(new String[0])
                );
            }

            // Multiple downloads completed in Parallel
            if(downloadCallback instanceof DownloadMultipleParallel){

                // When all the download are completed
                if(urlsNumber == successDownloads.size() + failedDownloads.size()){
                    ((DownloadMultipleParallel) downloadCallback).onCompleted(
                            successDownloads.toArray(new String[0]),
                            failedDownloads.toArray(new String[0])
                    );
                }
            }
        }
    }

}
