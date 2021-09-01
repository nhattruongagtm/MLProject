package com.example.mlproject;

public class Source {

    private static Source source;
    String data;
    boolean isHaveImage;

    private Source(){

    }

    public static Source getInstance(){
        if(source == null){
            source = new Source();
        }
        return source;
     }



}
