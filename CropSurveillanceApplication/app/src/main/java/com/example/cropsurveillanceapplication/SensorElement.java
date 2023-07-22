package com.example.cropsurveillanceapplication;

public class SensorElement {
    private String name;
    private String info;
    private String absolute_value;
    private String tip;
    private String last_updated;

    public SensorElement(String name,String info, String absolute_value, String tip,String last_updated) {
        this.name = name;
        this.info = info;
        this.absolute_value = absolute_value;
        this.tip = tip;
        this.last_updated = last_updated;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public String getAbsolute_value() {
        return absolute_value;
    }

    public String getTip() {
        return tip;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public String display(){
        return String.format("Sensor name: %s | last_updated value: %s | absolute_value: %s |tip: %s ",getName(),getLast_updated(),getAbsolute_value(),getTip());
    }
}
