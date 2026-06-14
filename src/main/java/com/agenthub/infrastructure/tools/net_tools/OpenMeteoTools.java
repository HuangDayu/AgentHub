/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author brianxiadong
 */

package com.agenthub.infrastructure.tools.net_tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 利用OpenMeteo的免费天气API提供天气服务 该API无需API密钥，可以直接使用
 */
@AgentTools(name = "OpenMeteoTools", description = "OpenMeteo天气服务工具，提供天气预报、空气质量查询等天气相关功能")
public class OpenMeteoTools {

    // OpenMeteo免费天气API基础URL
    private static final String BASE_URL = "https://api.open-meteo.com/v1";
    public static final String WEATHER_INFO = """
                        当前天气:
                        温度: %.1f%s (体感温度: %.1f%s)
                        天气: %s
                        风向: %s (%.1f %s)
                        湿度: %d%s
                        降水量: %.1f 毫米
                        
                        """;

    public static final String AIR_QUALITY_INFO = """
                            空气质量信息 (纬度: %.4f, 经度: %.4f, 时区: %s):
                            
                            欧洲空气质量指数 (EAQI): %d (%s)
                            美国空气质量指数 (US AQI): %d (%s)
                            
                            详细污染物信息:
                            PM10: %.1f μg/m³
                            PM2.5: %.1f μg/m³
                            一氧化碳 (CO): %.1f μg/m³
                            二氧化氮 (NO2): %.1f μg/m³
                            二氧化硫 (SO2): %.1f μg/m³
                            臭氧 (O3): %.1f μg/m³
                            
                            注意：以上是模拟数据，仅供示例。
                            """;

    public static final String WEATHER_BASE_INFO = """
                            %s:
                            温度: %.1f%s ~ %.1f%s
                            天气: %s
                            风向: %s (%.1f %s)
                            降水量: %.1f 毫米
                            
                            """;

    private  RestClient restClient;

    @PostConstruct
    public void init(){
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "OpenMeteoClient/1.0")
                .build();
    }

    // OpenMeteo天气数据模型
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherData(@JsonProperty("latitude") Double latitude, @JsonProperty("longitude") Double longitude,
                              @JsonProperty("timezone") String timezone,
                              @JsonProperty("current") CurrentWeather current,
                              @JsonProperty("daily") DailyForecast daily,
                              @JsonProperty("current_units") CurrentUnits currentUnits) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record CurrentWeather(@JsonProperty("time") String time,
                                     @JsonProperty("temperature_2m") Double temperature,
                                     @JsonProperty("apparent_temperature") Double feelsLike,
                                     @JsonProperty("relative_humidity_2m") Integer humidity,
                                     @JsonProperty("precipitation") Double precipitation,
                                     @JsonProperty("weather_code") Integer weatherCode,
                                     @JsonProperty("wind_speed_10m") Double windSpeed,
                                     @JsonProperty("wind_direction_10m") Integer windDirection) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record CurrentUnits(@JsonProperty("time") String timeUnit,
                                   @JsonProperty("temperature_2m") String temperatureUnit,
                                   @JsonProperty("relative_humidity_2m") String humidityUnit,
                                   @JsonProperty("wind_speed_10m") String windSpeedUnit) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record DailyForecast(@JsonProperty("time") List<String> time,
                                    @JsonProperty("temperature_2m_max") List<Double> tempMax,
                                    @JsonProperty("temperature_2m_min") List<Double> tempMin,
                                    @JsonProperty("precipitation_sum") List<Double> precipitationSum,
                                    @JsonProperty("weather_code") List<Integer> weatherCode,
                                    @JsonProperty("wind_speed_10m_max") List<Double> windSpeedMax,
                                    @JsonProperty("wind_direction_10m_dominant") List<Integer> windDirection) {
        }
    }

    /**
     * 获取天气代码对应的描述
     */
    private String getWeatherDescription(int code) {
        return switch (code) {
            case 0 -> "晴朗"; case 1,2,3 -> "多云"; case 45,48 -> "雾";
            case 51,53,55 -> "毛毛雨"; case 56,57 -> "冻雨"; case 61,63,65 -> "雨";
            case 66,67 -> "冻雨"; case 71,73,75 -> "雪"; case 77 -> "雪粒";
            case 80,81,82 -> "阵雨"; case 85,86 -> "阵雪"; case 95 -> "雷暴";
            case 96,99 -> "雷暴伴有冰雹"; default -> "未知天气";
        };
    }

    private static final String[] WIND_DIRECTIONS = {"北风", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风"};

    /**
     * 获取风向描述
     */
    private String getWindDirection(int degrees) {
        int idx = (int) Math.round(degrees / 45.0) % 8;
        return WIND_DIRECTIONS[idx];
    }

    /**
     * 获取指定经纬度的天气预报
     */
    @Tool(description = "获取指定经纬度的未来7天的天气预报")
    public String getWeatherForecastByLocation(@ToolParam(description = "纬度") double latitude,
                                               @ToolParam(description = "经度") double longitude) {
        var weatherData = fetchWeatherData(latitude, longitude);
        StringBuilder sb = new StringBuilder();
        appendCurrentWeather(sb, weatherData);
        appendDailyForecast(sb, weatherData);
        return sb.toString();
    }

    private WeatherData fetchWeatherData(double latitude, double longitude) {
        return restClient.get()
                .uri("/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,weather_code,wind_speed_10m,wind_direction_10m&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code,wind_speed_10m_max,wind_direction_10m_dominant&timezone=auto&forecast_days=7",
                        latitude, longitude)
                .retrieve()
                .body(WeatherData.class);
    }

    private void appendCurrentWeather(StringBuilder sb, WeatherData data) {
        WeatherData.CurrentWeather cur = data.current();
        var u = data.currentUnits();
        String tu = u != null ? u.temperatureUnit() : "°C";
        String wu = u != null ? u.windSpeedUnit() : "km/h";
        String hu = u != null ? u.humidityUnit() : "%";
        sb.append(String.format(WEATHER_INFO, cur.temperature(), tu, cur.feelsLike(), tu,
                getWeatherDescription(cur.weatherCode()), getWindDirection(cur.windDirection()),
                cur.windSpeed(), wu, cur.humidity(), hu, cur.precipitation()));
    }

    private void appendDailyForecast(StringBuilder sb, WeatherData data) {
        sb.append("未来天气预报:\n");
        WeatherData.DailyForecast daily = data.daily();
        for (int i = 0; i < daily.time().size(); i++) {
            sb.append(formatDay(daily, i));
        }
    }

    private String formatDay(WeatherData.DailyForecast daily, int i) {
        String date = LocalDate.parse(daily.time().get(i)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)"));
        return String.format(WEATHER_BASE_INFO, date,
                daily.tempMin().get(i), "°C", daily.tempMax().get(i), "°C",
                getWeatherDescription(daily.weatherCode().get(i)),
                getWindDirection(daily.windDirection().get(i)),
                daily.windSpeedMax().get(i), "km/h", daily.precipitationSum().get(i));
    }

    /**
     * 获取指定位置的空气质量信息（模拟数据）
     */
    @Tool(description = "获取指定位置的空气质量信息（模拟数据）")
    public String getAirQuality(@ToolParam(description = "纬度") double latitude,
                                @ToolParam(description = "经度") double longitude) {
        try {
            return buildAirQualityResult(latitude, longitude);
        } catch (Exception e) {
            return "无法获取空气质量信息: " + e.getMessage();
        }
    }

    private String buildAirQualityResult(double latitude, double longitude) {
        var weatherData = restClient.get()
                .uri("/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m&timezone=auto",
                        latitude, longitude)
                .retrieve()
                .body(WeatherData.class);
        return buildAirQualityInfo(latitude, longitude, weatherData);
    }

    private String buildAirQualityInfo(double latitude, double longitude, WeatherData weatherData) {
        int eaqi = (int) (Math.random() * 100) + 1;
        double[] v = {Math.random() * 50 + 5, Math.random() * 25 + 2, Math.random() * 500 + 100,
                Math.random() * 40 + 5, Math.random() * 20 + 1, Math.random() * 80 + 20};
        return String.format(AIR_QUALITY_INFO, latitude, longitude, weatherData.timezone(),
                eaqi, getAqiLevel(eaqi), (int) (eaqi * 1.5), getUsAqiLevel((int) (eaqi * 1.5)),
                v[0], v[1], v[2], v[3], v[4], v[5]);
    }

    /**
     * 获取欧洲AQI等级描述
     */
    private String getAqiLevel(Integer aqi) {
        if (aqi <= 20) return "优 (0-20): 空气质量非常好";
        if (aqi <= 40) return "良 (20-40): 空气质量良好";
        if (aqi <= 60) return "中等 (40-60): 对敏感人群可能有影响";
        if (aqi <= 80) return "较差 (60-80): 对所有人群健康有影响";
        if (aqi <= 100) return "差 (80-100): 可能对所有人群健康造成损害";
        return "非常差 (>100): 对所有人群健康有严重影响";
    }

    /**
     * 获取美国AQI等级描述
     */
    private String getUsAqiLevel(Integer aqi) {
        if (aqi <= 50) return "优 (0-50): 空气质量令人满意，污染风险很低";
        if (aqi <= 100) return "良 (51-100): 空气质量尚可，对极少数敏感人群可能有影响";
        if (aqi <= 150) return "对敏感人群不健康 (101-150): 敏感人群可能会经历健康影响";
        if (aqi <= 200) return "不健康 (151-200): 所有人可能开始经历健康影响";
        if (aqi <= 300) return "非常不健康 (201-300): 健康警告，所有人可能经历更严重的健康影响";
        return "危险 (>300): 健康警报，所有人更可能受到影响";
    }

    public static void main(String[] args) {
        OpenMeteoTools service = new OpenMeteoTools();
        // 测试北京的天气预报
        System.out.println("北京天气预报:");
        System.out.println(service.getWeatherForecastByLocation(39.9042, 116.4074));

        // 测试北京的空气质量
        System.out.println("北京空气质量:");
        System.out.println(service.getAirQuality(39.9042, 116.4074));
    }

}
