package it.xedoll.downloady;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Utils {


    /**
     * This function check the validity of a directory and return it if it's valid
     * @param pathToDir path string to directory
     * @return null if is not a directory, else return the directory file
     */
    public static File checkDir(String pathToDir)
    {
        //Check if is not null
        if(pathToDir == null) return null;

        // Get the file dir and check if exists
        File savingDir = new File(pathToDir);

        // Check if exists and if is it a valid directory
        if(savingDir.exists() && savingDir.isDirectory()){
            return savingDir;
        }else{
            return null;
        }
    }

    /**
     * This function check the validity of a directory and return it if it's valid
     * @param dir file directory
     * @return null if is not a directory, else return the directory file
     */
    public static File checkDir(File dir)
    {
        //Check if is not null
        if(dir == null) return null;

        // Check if exists and if is it a valid directory
        if(dir.exists() && dir.isDirectory()){
            return dir;
        }else{
            return null;
        }
    }

    /**
     * This function check the validity of a url address and return it if it's valid
     * @param url to check
     * @return null if is not a valid URL, else return the URL
     */
    public static URL checkURL(String url)
    {
        //Check if is not null
        if(url == null) return null;

        // Create the URL Object
        URL urlObject;
        try {
            urlObject = new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }

        // It's a valid URL
        return urlObject;
    }

    public static String getFileNameFromUrl(URL url) {
        String urlString = url.getFile();
        return urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
    }

}
