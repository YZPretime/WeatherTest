package com.example.retime.weathertest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.retime.weathertest.gson.Forecast;
import com.example.retime.weathertest.gson.Weather;
import com.example.retime.weathertest.service.AutoUpdateService;
import com.example.retime.weathertest.utils.HttpUtils;
import com.example.retime.weathertest.utils.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public String weatherId;

    public DrawerLayout drawerLayout;
    private ImageView ivNav;
    public SwipeRefreshLayout swipeRefresh;
    private ScrollView svWeather;
    private TextView titleTvCity;
    private TextView titleTvUpdateTime;
    private TextView tvDegree;
    private TextView tvWeatherInfo;
    private LinearLayout llForecast;
    private TextView tvAqi;
    private TextView tvPm25;
    private TextView tvComfort;
    private TextView tvCarWash;
    private TextView tvSport;
    private ImageView ivBingPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ivNav = (ImageView) findViewById(R.id.iv_nav);
        svWeather = (ScrollView) findViewById(R.id.sv_weather);
        titleTvCity = (TextView) findViewById(R.id.title_tv_city);
        titleTvUpdateTime = (TextView) findViewById(R.id.title_tv_update_time);
        tvDegree = (TextView) findViewById(R.id.tv_degree);
        tvWeatherInfo = (TextView) findViewById(R.id.tv_weather_info);
        llForecast = (LinearLayout) findViewById(R.id.ll_forecast);
        tvAqi = (TextView) findViewById(R.id.tv_aqi);
        tvPm25 = (TextView) findViewById(R.id.tv_pm25);
        tvComfort = (TextView) findViewById(R.id.tv_comfort);
        tvCarWash = (TextView) findViewById(R.id.tv_car_wash);
        tvSport = (TextView) findViewById(R.id.tv_sport);
        ivBingPic = (ImageView) findViewById(R.id.iv_bing_pic);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sp.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            weatherId = getIntent().getStringExtra("weather_id");
            svWeather.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        ivNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        String bingPic = sp.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(ivBingPic);
        } else {
            loadBingPic();
        }
    }

    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId
                + "&key=4ab78bf8d6f542f1b6720ae33fc878c4";
        HttpUtils.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtils.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(ivBingPic);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        if (weather != null && "ok".equals(weather.status)) {
            String cityName = weather.basic.cityName;
            String updateTime = weather.basic.update.updateTime.split(" ")[1];
            String degree = weather.now.temperature + "°C";
            String weatherInfo = weather.now.more.info;
            titleTvCity.setText(cityName);
            titleTvUpdateTime.setText(updateTime);
            tvDegree.setText(degree);
            tvWeatherInfo.setText(weatherInfo);
            llForecast.removeAllViews();
            for (Forecast forecast :
                    weather.forecastList) {
                View view = LayoutInflater.from(this).inflate(R.layout.item_forecast, llForecast, false);
                TextView tvDate = (TextView) view.findViewById(R.id.tv_date);
                TextView tvInfo = (TextView) view.findViewById(R.id.tv_info);
                TextView tvMax = (TextView) view.findViewById(R.id.tv_max);
                TextView tvMin = (TextView) view.findViewById(R.id.tv_min);
                tvDate.setText(forecast.date);
                tvInfo.setText(forecast.more.info);
                tvMax.setText(forecast.temperature.max);
                tvMin.setText(forecast.temperature.min);
                llForecast.addView(view);
            }
            if (weather.aqi != null) {
                tvAqi.setText(weather.aqi.city.aqi);
                tvPm25.setText(weather.aqi.city.pm25);
            }
            String comfort = "舒适度：" + weather.suggestion.comfort.info;
            String carWash = "洗车指数：" + weather.suggestion.carWash.info;
            String sport = "运动建议：" + weather.suggestion.sport.info;
            tvComfort.setText(comfort);
            tvCarWash.setText(carWash);
            tvSport.setText(sport);
            svWeather.setVisibility(View.VISIBLE);
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        } else {
            Toast.makeText(this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
        }
    }
}
