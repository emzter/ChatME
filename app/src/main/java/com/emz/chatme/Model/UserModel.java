package com.emz.chatme.Model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by AeMzAKuN on 18/10/2559.
 */

public class UserModel {

    public String provider;
    public String id;
    public String email;
    public String name;
    public String profilePic;

    public UserModel(){}

    public UserModel(String id) {
        this.id = id;
    }

    public UserModel(String provider, String id, String email, String name, String profilePic) {
        this.provider = provider;
        this.id = id;
        this.email = email;
        this.name = name;
        this.profilePic = profilePic;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("id", id);
        result.put("name", name);
        result.put("profilePic", profilePic);
        result.put("provider", provider);
        return result;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    @Override
    public String toString() {
        return "users{" +
                ", provider" + provider +
                ", id=" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", profilePic=" + profilePic +
                '}';
    }
}
