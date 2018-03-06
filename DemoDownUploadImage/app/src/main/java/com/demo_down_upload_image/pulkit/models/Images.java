package com.demo_down_upload_image.pulkit.models;

import java.io.Serializable;

/**
 * Created by agicon06 on 11/10/17.
 */

public class Images implements Serializable {

    private String imagePath;

    public Images(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

}
