package com.svvorf.yandex.musicians.models;

import io.realm.RealmObject;

/**
 * A model mirroring List<String> for storing in the database as a Musician field.
 */
public class RealmString extends RealmObject {
    private String value;

    public RealmString() {
    }

    public RealmString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
