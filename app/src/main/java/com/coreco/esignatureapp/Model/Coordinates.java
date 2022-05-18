package com.coreco.esignatureapp.Model;

public class Coordinates {
    String X,Y,W,H;

    public Coordinates(String x, String y, String w, String h) {
        X = x;
        Y = y;
        W = w;
        H = h;
    }

    public String getX() {
        return X;
    }

    public void setX(String x) {
        X = x;
    }

    public String getY() {
        return Y;
    }

    public void setY(String y) {
        Y = y;
    }

    public String getW() {
        return W;
    }

    public void setW(String w) {
        W = w;
    }

    public String getH() {
        return H;
    }

    public void setH(String h) {
        H = h;
    }
}
