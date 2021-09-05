package com.example.mlproject;

import android.graphics.Bitmap;

public class ObjectResult {

    Bitmap bitmap;
    String label;

    public ObjectResult(Bitmap bitmap, String label) {
        this.bitmap = bitmap;
        this.label = label;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
