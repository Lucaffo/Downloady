package it.xedoll.downloadylibrary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import it.xedoll.downloady.Downloady;
import it.xedoll.downloady.interfaces.DownloadMultiple;
import it.xedoll.downloady.interfaces.DownloadMultipleParallel;
import it.xedoll.downloady.interfaces.DownloadSingle;

import static it.xedoll.downloady.Downloady.FAILED;
import static it.xedoll.downloady.Downloady.SUCCESS;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    // View objects
    TextView downloadName;
    TextView downloadCounter;
    ProgressBar downloadProgress;
    TextView progressPercentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View objects
        downloadName = findViewById(R.id.downloadName);
        downloadCounter = findViewById(R.id.downloadCounter);
        downloadProgress = findViewById(R.id.downloadProgress);
        progressPercentage = findViewById(R.id.progressPercentage);

        // File to download
        String[] urls = {
                "https://www.google.com/drive/static/images/home/files.png",
                "http://ipv4.download.thinkbroadband.com/5MB.zip",
                "http://ipv4.download.thinkbroadband.com/10MB.zip",
                "http://ipv4.download.thinkbroadband.com/20MB.zip",
                "http://ipv4.download.thinkbroadband.com/2feawfawef0MB.zip",
                //"http://ipv4.download.thinkbroadband.com/50MB.zip",
                //"http://ipv4.download.thinkbroadband.com/100MB.zip",
                //"http://ipv4.download.thinkbroadband.com/200MB.zip"
        };

        while (!checkPermissions());

        startMultipleParallelDownloads(urls);
    }

    private void startSingleDownload(String url)
    {
        // Create an instance of downloady
        Downloady downloady = new Downloady();

        // This is used to test the download time
        long startTime = System.currentTimeMillis();

        downloady.download(url, getDownloadsDirectory(), new DownloadSingle() {

            @Override
            public void onStartDownload(String fileName, String url) {
                Log.v("OnDownload", "Start downloading " + fileName);
                runOnUiThread(() -> {
                    downloadName.setText("Downloading " + fileName + "...");
                    downloadCounter.setText("");
                    progressPercentage.setText("0%");
                });
            }

            @Override
            public void onDownload(String fileName, long progress) {
                Log.v("OnDownload", "Downloading " + fileName + " at " + progress + "%");
                runOnUiThread(() -> {
                    downloadProgress.setProgress((int)progress);
                    progressPercentage.setText(progress + "%");
                });
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
            }

            @Override
            public void onCompleted(int status) {

                long endTime = System.currentTimeMillis();

                switch (status){
                    case SUCCESS:
                        Toast.makeText(MainActivity.this, "Downloaded with success!", Toast.LENGTH_SHORT).show();
                    case FAILED:
                        Toast.makeText(MainActivity.this, "Failed to download!", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(MainActivity.this, "Time " + (endTime - startTime)/1000 + "s", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMultipleDownloads(String[] urls)
    {
        // Create an instance of downloady
        Downloady downloady = new Downloady();

        // This is used to test the download time
        long startTime = System.currentTimeMillis();

        downloady.download(urls, getDownloadsDirectory(), new DownloadMultiple() {
            @Override
            public void onDownload(String fileName, long progress, int currentDownload, int numberOfDownloads) {
                Log.v("OnDownload", "Downloading " + fileName + " " + progress + "% File " + (currentDownload + 1) + "/" + numberOfDownloads);
                runOnUiThread(() -> {
                    downloadName.setText("Downloading " + fileName + "...");
                    downloadProgress.setProgress((int)progress);
                    progressPercentage.setText(progress + "%");
                    downloadCounter.setText((currentDownload + 1) + "/" + numberOfDownloads);
                });
            }

            @Override
            public void onCompleted(String[] successDownloads, String[] failedDownloads) {

                long endTime = System.currentTimeMillis();
                runOnUiThread(() -> {
                    downloadName.setText("Download completed");
                    downloadCounter.setText(successDownloads.length + "/" + urls.length);
                });

                Toast.makeText(MainActivity.this, "Downloaded " + urls.length + "/" + (successDownloads.length + failedDownloads.length) + " files ", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "Time " + (endTime - startTime)/1000 + "s", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMultipleParallelDownloads(String[] urls)
    {
        // Create an instance of downloady
        Downloady downloady = new Downloady();

        // This is used to test the download time
        long startTime = System.currentTimeMillis();

        downloadProgress.setIndeterminate(true);
        downloady.download(urls, getDownloadsDirectory(), new DownloadMultipleParallel() {
            @Override
            public void onDownload(String fileName, long progress, int currentDownload, int numberOfDownloads, int completedDownloads) {
                Log.v("OnDownload", "Downloading " + fileName + " " + progress + "% File " + completedDownloads + "/" + numberOfDownloads);
                runOnUiThread(() -> {
                    downloadName.setText("Downloading " + fileName + "...");
                    downloadProgress.setProgress((int)progress);
                    progressPercentage.setText("");
                    downloadCounter.setText(completedDownloads + "/" + numberOfDownloads);
                });
            }

            @Override
            public void onCompleted(String[] successDownloads, String[] failedDownloads) {

                long endTime = System.currentTimeMillis();

                Toast.makeText(MainActivity.this, "Downloaded with success " + successDownloads.length + "/" + successDownloads.length + failedDownloads.length + " files ", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "Time " + (endTime - startTime)/1000 + "s", Toast.LENGTH_SHORT).show();

                runOnUiThread(() -> {
                    downloadName.setText("Download completed!");
                    downloadProgress.setIndeterminate(false);
                    downloadProgress.setProgress((int)100);
                    downloadCounter.setText(successDownloads.length + "/" + urls.length);
                });
            }
        });
    }


    /**
     * Get the downloading directory
     * @return File Dir
     */
    private File getDownloadsDirectory() {
        // Get the directory for the app's private pictures directory.
        if(!this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).exists()){
            this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).mkdirs();
        }
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    /**
     * Permissions to request
     */
    private final int REQUEST_PERMISSIONS = 0;
    private final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    /**
     * Check the permissions for the AR
     * @return true, if the permissions requests are satisfied
     *         false else the permission requests are not satisfied
     */
    private boolean checkPermissions(){

        boolean res = true;

        //Before starting the ArActivity check for permissions
        for(String permission : permissions){

            //Check if the permission is denied
            boolean check = ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_DENIED;

            //If at least one is denied, res will be always false
            res = res && check;
        }

        //If is denied
        if(!res){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }

        return res;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {

            // If request is cancelled, the result arrays are empty.
            if (grantResults.length == permissions.length) {

                //Loop thought all the permission and check that are granted
                for(int i = 0; i < grantResults.length; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                }
            }
        }
    }
}
