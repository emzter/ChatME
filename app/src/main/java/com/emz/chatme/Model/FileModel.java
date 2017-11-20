package com.emz.chatme.Model;

/**
 * Created by AeMzAKuN on 18/10/2559.
 */

public class FileModel {

    private String type;
    private String file_url;
    private String file_name;
    private String file_size;

    public FileModel() {
    }

    public FileModel(String type, String file_url, String file_name, String file_size) {
        this.type = type;
        this.file_url = file_url;
        this.file_name = file_name;
        this.file_size = file_size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_size() {
        return file_size;
    }

    public void setFile_size(String file_size) {
        this.file_size = file_size;
    }
}
