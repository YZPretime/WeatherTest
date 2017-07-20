package com.example.retime.weathertest.gson;

/**
 * Created by retime on 2017/7/20.
 */

public class AQI {

    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
