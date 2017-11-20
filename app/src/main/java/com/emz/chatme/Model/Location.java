package com.emz.chatme.Model;

/**
 * Created by AeMzAKuN on 18/10/2559.
 */

public class Location {
    private String latitude;
    private String longitude;

    public Location() {
    }

    public Location(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
