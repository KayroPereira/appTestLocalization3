package com.example.apptestlocalization3;

public class Localizacao {

    private double longitude;
    private double latitude;
    private String provider;
    private long time;
    private float accuracy;
    private double altitude;
    private float speed;

    public Localizacao(){}

    public Localizacao(double longitude, double latitude, String provider, long time, float accuracy, double altitude, float speed) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.provider = provider;
        this.time = time;
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.speed = speed;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
