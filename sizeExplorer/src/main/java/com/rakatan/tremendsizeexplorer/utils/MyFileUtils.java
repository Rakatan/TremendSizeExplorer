package com.rakatan.tremendsizeexplorer.utils;

import android.os.Environment;

import com.rakatan.tremendsizeexplorer.models.FileModel;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by rakatan on 11.09.2016.
 */

public class MyFileUtils {
    public static File getSystemRoot() {
        return Environment.getExternalStorageDirectory();
    }

    /**
     * @return A sorted list (large to small) of the files in the system root.
     */
    public static ArrayList<FileModel> getRootFiles() {
        File rootFile = getSystemRoot();

        ArrayList<File> rootFiles = new ArrayList<>(Arrays.asList(rootFile.listFiles()));
        ArrayList<FileModel> fileModels = new ArrayList<>();

        for(File file: rootFiles){
            fileModels.add(new FileModel(file));
        }

        Collections.sort(fileModels, new SizeComparator());

        return fileModels;
    }

    /**
     * @param directory The directory from which we will extract the files.
     * @return A sorted list (large to small) of the files in the system root.
     */
    public static ArrayList<FileModel> getFilesFromDirectory(File directory){
        ArrayList<File> rootFiles = new ArrayList<>(Arrays.asList(directory.listFiles()));

        if(rootFiles.size() != 0) {
            ArrayList<FileModel> fileModels = new ArrayList<>();

            for (File file : rootFiles) {
                fileModels.add(new FileModel(file));
            }

            Collections.sort(fileModels, new SizeComparator());

            return fileModels;
        }

        else return null;
    }

    /**
     * Compares file models based on their size attribute, returns Large to Small
     */
    private static class SizeComparator implements Comparator<FileModel> {

        @Override
        public int compare(FileModel fileModel1, FileModel fileModel2) {

            if (fileModel1.getSize() < fileModel2.getSize())
                return 1;
            else if (fileModel1.getSize() == fileModel2.getSize())
                return 0;
            else if (fileModel1.getSize() > fileModel2.getSize())
                return -1;

            return 0;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }

    //Credit goes to: http://stackoverflow.com/a/3758880/3738533
    /**
     * Transforms a size represented as Long into a human readable format.
     * @param bytes File / folder size
     * @param si Bytes or bits toggle
     * @return A string containing the human readable representation of a file's size.
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
