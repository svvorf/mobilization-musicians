package com.svvorf.yandex.musicians.models;

import java.util.ArrayList;
import java.util.List;

/**
 * A model for api response.
 */
public class ApiResponse {
    private List<Musician> musicians;

    public List<Musician> getMusicians() {
        return musicians;
    }

    public void setMusicians(List<Musician> musicians) {
        this.musicians = musicians;
    }
}
