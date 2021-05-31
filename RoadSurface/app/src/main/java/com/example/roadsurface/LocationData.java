package com.example.roadsurface;

public class LocationData {
    public double lat;
    public double lon;
    public double alt;
    public long time;

    public LocationData(double lat, double lon, double alt, long time) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.time = time;
    }

    public LocationData(double lat, double lon, double alt) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.time = 0;
    }

    private double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    public double distanceInKmBetweenEarthCoordinates(LocationData destination) {
        double earthRadiusKm = 6371;

        double dLat = degreesToRadians(destination.lat - this.lat);
        double dLon = degreesToRadians(destination.lon - this.lon);

        double lat1 = degreesToRadians(this.lat);
        double lat2 = degreesToRadians(destination.lat);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadiusKm * c;
    }



    public long getDuration(LocationData destination) {
        return destination.time - this.time;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getAlt() {
        return alt;
    }

    public long getTime() {
        return time;
    }
}
