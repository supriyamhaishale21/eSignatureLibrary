package com.coreco.esignaturelibrary.Model;

import java.util.ArrayList;

public class SignatureDetails {
    String page;
    ArrayList<Coordinate> scoordinates;

    public SignatureDetails(String page, ArrayList<Coordinate> scoordinates) {
        this.page = page;
        this.scoordinates = scoordinates;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public ArrayList<Coordinate> getScoordinates() {
        return scoordinates;
    }

    public void setScoordinates(ArrayList<Coordinate> scoordinates) {
        this.scoordinates = scoordinates;
    }
}
