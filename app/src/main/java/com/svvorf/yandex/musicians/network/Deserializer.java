package com.svvorf.yandex.musicians.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.svvorf.yandex.musicians.models.ApiResponse;
import com.svvorf.yandex.musicians.models.Musician;
import com.svvorf.yandex.musicians.models.RealmString;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

/**
 * A custom deserializer for ApiResponse.
 */
public class Deserializer implements JsonDeserializer<ApiResponse> {
    @Override
    public ApiResponse deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonArray jArray = (JsonArray) json;

        ApiResponse apiResponse = new ApiResponse();
        List<Musician> musicians = new ArrayList<>();
        for (int i = 0; i < jArray.size(); i++) {
            JsonObject jObject = (JsonObject) jArray.get(i);
            Musician musician = new Musician();
            musician.setId(jObject.get("id").getAsInt());
            musician.setName(jObject.get("name").getAsString());
            String description = jObject.get("description").getAsString();
            description = description.substring(0, 1).toUpperCase() + description.substring(1); //capitalize
            musician.setDescription(description);
            JsonElement link = jObject.get("link");
            if (link != null)
                musician.setLink(link.getAsString());
            musician.setAlbums(jObject.get("albums").getAsInt());
            musician.setTracks(jObject.get("tracks").getAsInt());

            JsonArray jGenres = jObject.get("genres").getAsJsonArray();
            RealmList<RealmString> genres = new RealmList<>();
            for (JsonElement jGenre : jGenres) {
                genres.add(new RealmString(jGenre.getAsString()));
            }
            musician.setGenres(genres);

            JsonObject jCovers = (JsonObject) jObject.get("cover");
            musician.setSmallCover(jCovers.get("small").getAsString());
            musician.setBigCover(jCovers.get("big").getAsString());
            musicians.add(musician);
        }
        apiResponse.setMusicians(musicians);

        return apiResponse;
    }
}
