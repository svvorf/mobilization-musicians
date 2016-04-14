package com.svvorf.yandex.musicians.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * The class provides useful static methods.
 */
public class Utils {

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
