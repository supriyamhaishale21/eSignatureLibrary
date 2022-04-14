package com.coreco.esignaturelibrary.Model;

public class Coordinate {
    String x, y, w, h;

    public Coordinate(String x, String y, String w, String h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }
}
