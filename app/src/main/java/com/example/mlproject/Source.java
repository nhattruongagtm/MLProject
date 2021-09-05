package com.example.mlproject;

public class Source {

    private static Source source;
    String data;
    boolean isHaveImage;

    private MainActivity mainActivity;

    private Source() {

    }

    public static Source getInstance() {
        if (source == null) {
            source = new Source();
        }
        return source;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}
