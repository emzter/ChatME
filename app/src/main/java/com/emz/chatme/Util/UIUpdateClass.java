package com.emz.chatme.Util;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.emz.chatme.R;

/**
 * Created by AeMzAKuN on 17/10/2559.
 */

public class UIUpdateClass {

    static ProgressDialog progressDialog;

    public static void createProgressDialog(Context context, int theme, String string) {
        progressDialog = new ProgressDialog(context, theme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(string);
        progressDialog.show();
    }

    public static void createProgressDialog(Context context, String string) {
        progressDialog = new ProgressDialog(context, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(string);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public static void createSnackbar(View view, String string){
        Snackbar.make(view, string, Snackbar.LENGTH_LONG).show();
    }
}
