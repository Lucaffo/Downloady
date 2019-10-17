package it.xedoll.downloady.interfaces;

import android.util.Log;

import static it.xedoll.downloady.Downloady.FAILED;
import static it.xedoll.downloady.Downloady.SUCCESS;

public abstract class DownloadMultipleParallel implements DownloadMultipleParallelCallback{

    @Override
    public void onStartDownload(String fileName, int numberOfDownloads){
        Log.v("OnStartDownload", "Start downloading " + fileName);
    }

    @Override
    public void onDownload(String fileName, long progress, int currentDownload, int numberOfDownloads, int completedDownloads){
        Log.v("OnDownload", "Downloading " + fileName + " " + progress + "% File " + currentDownload + "/" + numberOfDownloads);
    }

    @Override
    public void onError(Exception e, int currentDownload) {
        Log.e("OnError", e.getMessage() + " url at index " + currentDownload);
    }

    @Override
    public void onSingleCompleted(int status, String fileName, int currentDownload, int completedDownloads) {
        switch (status){
            case SUCCESS:
                Log.v("OnSingleCompleted", "Downloaded with success " + fileName + ". Already completed " + completedDownloads);
                break;
            case FAILED:
                Log.e("OnSingleCompleted", "Failed to download " + fileName + ". Already completed " + completedDownloads);
                break;
        }
    }

    @Override
    public void onCompleted(String[] successDownloads, String[] failedDownloads) {
        Log.w("OnAllFileCompleted", "Download completed! Downloaded with success " + successDownloads.length + "/" + successDownloads.length + failedDownloads.length + " files");
    }
}

// Download multiple file interface
interface DownloadMultipleParallelCallback
{
    /**
     * Before starting the downloads
     * @param fileName the name of the file before starting to downloading it
     * @param numberOfDownloads the number of files to download
     */
    void onStartDownload(String fileName, int numberOfDownloads);

    /**
     * Download for multiple file
     * @param fileName of the file to download
     * @param progress in percentage of completing for the current file
     * @param currentDownload index in the list of the url
     * @param numberOfDownloads number of files to download
     */
    void onDownload(String fileName, long progress, int currentDownload, int numberOfDownloads, int completedDownloads);

    /**
     * Inform the user about errors that don't block the download progress
     * @param e Exception of the error
     * @param currentDownload of the urls array
     */
    void onError(Exception e, int currentDownload);

    /**
     * When the single download is complete, return this
     * @param status of the download: SUCCESS or FAILED
     * @param fileName of the file to download
     * @param currentDownload  of the urls array
     */
    void onSingleCompleted(int status, String fileName, int currentDownload, int completedDownloads);

    /**
     * When the single download is complete, return this
     * @param successDownloads of completed downloads
     * @param failedDownloads of failed downloads
     */
    void onCompleted(String[] successDownloads, String[] failedDownloads);
}