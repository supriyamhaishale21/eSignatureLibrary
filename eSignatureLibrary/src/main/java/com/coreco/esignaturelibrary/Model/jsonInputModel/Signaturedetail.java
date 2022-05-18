package com.coreco.esignaturelibrary.Model.jsonInputModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Signaturedetail {

    @SerializedName("page")
    @Expose
    private String page;
    @SerializedName("coordinates")
    @Expose
    private List<Coordinate> coordinates = null;

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

}
