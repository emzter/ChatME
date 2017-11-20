package com.emz.chatme.Model;

/**
 * Created by AeMzAKuN on 18/10/2559.
 */

public class ChatModel {

    public String id;
    private UserModel user;
    private String message;
    private String timeStamp;
    private FileModel file;
    private Location mapModel;

    public ChatModel() {}

    public ChatModel(UserModel user, String message, String timeStamp, FileModel file) {
        this.user = user;
        this.message = message;
        this.timeStamp = timeStamp;
        this.file = file;
    }

    public ChatModel(UserModel user, String timeStamp, Location mapModel) {
        this.user = user;
        this.timeStamp = timeStamp;
        this.mapModel = mapModel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public FileModel getFile() {
        return file;
    }

    public void setFile(FileModel file) {
        this.file = file;
    }

    public Location getMapModel() {
        return mapModel;
    }

    public void setMapModel(Location mapModel) {
        this.mapModel = mapModel;
    }

    @Override
    public String toString() {
        return "ChatMessege{" +
                "mapModel=" + mapModel +
                ", file=" + file +
                ", timeStamp='" + timeStamp + '\'' +
                ", message='" + message + '\'' +
                ", user=" + user +
                '}';
    }
}
