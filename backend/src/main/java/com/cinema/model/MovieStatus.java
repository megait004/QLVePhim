package com.cinema.model;

public enum MovieStatus {
    NOW_SHOWING("Đang chiếu"),
    COMING_SOON("Sắp chiếu"),
    HOT("Phim Hot"),
    ENDED("Đã kết thúc");

    private final String displayName;

    MovieStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}