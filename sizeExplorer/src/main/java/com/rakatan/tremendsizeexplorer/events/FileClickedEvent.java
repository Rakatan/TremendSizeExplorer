package com.rakatan.tremendsizeexplorer.events;

import com.rakatan.tremendsizeexplorer.models.FileModel;

/**
 * Created by rakatan on 11.09.2016.
 */

public class FileClickedEvent {
    private FileModel fileModel;

    public FileClickedEvent(FileModel fileModel) {
        this.fileModel = fileModel;
    }

    public FileModel getFileModel() {
        return fileModel;
    }

    public void setFileModel(FileModel fileModel) {
        this.fileModel = fileModel;
    }
}
