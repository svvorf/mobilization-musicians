package com.svvorf.yandex.musicians;

import android.app.Application;
import android.util.Log;

import com.svvorf.yandex.musicians.network.RequestManager;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * The main Application subclass.
 */
public class MusiciansApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //Initializing Realm
        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);
        Log.d("dbg", "onCreate() application");
        RequestManager.getInstance().initialize(this);
    }
}
