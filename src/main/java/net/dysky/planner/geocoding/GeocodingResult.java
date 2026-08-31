package net.dysky.planner.geocoding;

public record GeocodingResult(
        String name,
        double latitude,
        double longitude,
        String country
) {}