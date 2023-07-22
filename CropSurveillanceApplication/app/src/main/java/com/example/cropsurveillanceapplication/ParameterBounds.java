package com.example.cropsurveillanceapplication;

public class ParameterBounds {
    private  String name;
    private int lowerBound;
    private int upperBound;

    public ParameterBounds(String name, int lowerBound, int upperBound) {
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public String getName() {
        return name;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }
}
