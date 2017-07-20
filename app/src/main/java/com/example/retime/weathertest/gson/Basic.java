package com.example.retime.weathertest.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by retime on 2017/7/20.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;
    }
}
