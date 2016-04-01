package com.svvorf.yandex.musicians.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by ivan on 4/1/16.
 */
public class Utils {

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
