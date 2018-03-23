package com.ds.tire.dao;

/**
 * Created by developer on 2018/3/17.
 */

public class Advertisement {
    private String advertisement;
    private int imageId;

    public Advertisement(String advertisement, int imageId) {
        super();
        this.advertisement = advertisement;
        this.imageId = imageId;


    }

    public String getAdvertisement() {
        return advertisement;
    }

    public void setAdvertisement(String advertisement) {
        this.advertisement = advertisement;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
