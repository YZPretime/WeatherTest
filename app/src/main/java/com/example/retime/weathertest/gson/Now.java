package com.example.retime.weathertest.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by retime on 2017/7/20.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }
}
