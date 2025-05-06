package com.example.token.model;

import java.util.Date;

public class ImageHistoryEntry {
    private String imageUrl;
    private Date timestamp;

    public ImageHistoryEntry() {}

    public ImageHistoryEntry(String imageUrl, Date timestamp) {
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
