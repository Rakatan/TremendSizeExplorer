package com.rakatan.tremendsizeexplorer.models;

import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Created by rakatan on 11.09.2016.
 */

public class FileModel {
    private File file;
    private long size;

    public FileModel(File file){
        this.file = file;

        if(file.isDirectory())
            size = FileUtils.sizeOfDirectory(file);
        else if(file.isFile())
            size = file.length();
    }

    public FileModel(File file, long size) {
        this.file = file;
        this.size = size;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
