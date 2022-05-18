package com.coreco.esignatureapp.Model;

import java.util.ArrayList;

public class PageDetails {
    String page;
    ArrayList<Coordinates> coordinates;

    public PageDetails(String page,  ArrayList<Coordinates> coordinates) {
        this.page = page;
       this.coordinates=coordinates;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public ArrayList<Coordinates> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(ArrayList<Coordinates> coordinates) {
        this.coordinates = coordinates;
    }
}
