package com.emz.chatme.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.widget.EditText;

/**
 * Created by AeMzAKuN on 18/10/2559.
 */

public class Util {

    public static final String URL_STORAGE_REFERENCE = "gs://mechat-515c9.appspot.com";
    public static final String FOLDER_STORAGE_IMG = "images";
    public static final String FOLDER_STORAGE_PROFILE_IMG = "images/profile-img/";

    @NonNull
    public static String convertString(EditText editText) {
        return editText.getText().toString();
    }

    public static String local(String latitudeFinal,String longitudeFinal){
        return "https://maps.googleapis.com/maps/api/staticmap?center="+latitudeFinal+","+longitudeFinal+"&zoom=18&size=280x280&markers=color:red|"+latitudeFinal+","+longitudeFinal;
    }

    public  static boolean verifyConnectivity(Context context) {
        boolean connection;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connection = connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isAvailable()
                && connectivityManager.getActiveNetworkInfo().isConnected();
        return connection;
    }
}
