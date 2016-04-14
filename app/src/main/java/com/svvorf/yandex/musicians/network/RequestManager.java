package com.svvorf.yandex.musicians.network;

import android.content.Context;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.svvorf.yandex.musicians.models.ApiResponse;

import io.realm.RealmObject;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * A singleton class for managing network operations.
 */
public class RequestManager {
    private OkHttpClient client;
    private Picasso picasso;

    private static final String MUSICIANS_URL = "http://download.cdn.yandex.net/mobilization-2016/artists.json";

    private Request loadMusiciansRequest;
    private Gson gson;



    private static class Holder {
        private static final RequestManager INSTANCE = new RequestManager();
    }

    public static RequestManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initialize(Context context) {
        client = new OkHttpClient();
        picasso = new Picasso.Builder(context).downloader(new OkHttp3Downloader(client)).build();

        gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(ApiResponse.class, new Deserializer())
                .create();

        createRequests();
    }

    private void createRequests() {
        loadMusiciansRequest = new Request.Builder().url(MUSICIANS_URL).build();
    }

    public Picasso getPicasso() {
        return picasso;
    }

    public Gson getGson() {
        return gson;
    }

    public void loadMusicians(Callback callback) {
        client.newCall(loadMusiciansRequest).enqueue(callback);
    }

}
