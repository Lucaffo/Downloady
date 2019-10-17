package it.xedoll.downloady.interfaces;


import android.util.Log;

import static it.xedoll.downloady.Downloady.FAILED;
import static it.xedoll.downloady.Downloady.SUCCESS;

public abstract class DownloadSingle implements DownloadSingleCallback{

    @Override
    public void onStartDownload(String fileName, String url){
        Log.v("OnDownload", "Downloading " + fileName);
    }

    @Override
    public void onDownload(String fileName, long progress) {
        Log.v("OnDownload", progress + "%");
    }

    @Override
    public void onError(Exception e) {
        Log.e("OnError", e.getMessage());
    }

    @Override
    public void onCompleted(int status) {
        switch (status){
            case FAILED:
                Log.v("OnCompleted", "Failed to download this file!");
            case SUCCESS:
                Log.e("OnCompleted", "File downloaded with success!");
        }
    }
}


// Download single file interface
interface DownloadSingleCallback
{
    /**
     * Before starting the downloads
     * @param fileName downloading file name
     */
    void onStartDownload(String fileName, String url);

    /**
     * Download for a single file
     * @param progress progress in percentage to complete
     */
    void onDownload(String fileName, long progress);

    /**
     * Inform the user about errors that don't block the download progress
     * @param e Exception of the error
     */
    void onError(Exception e);

    /**
     * When the download is complete, return this
     */
    void onCompleted(int status);
}