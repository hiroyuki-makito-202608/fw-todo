package com.example.todoapp.api;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HolidayClient {

    private final RestClient restClient;

    public HolidayClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public HolidayResult getHolidays() {
        try {
            Map<String, String> holidays = CompletableFuture.supplyAsync(() -> restClient.get()
                    .uri("https://holidays-jp.github.io/api/v1/date.json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {}))
                    .get(8, TimeUnit.SECONDS);
            return new HolidayResult(holidays, false);
        } catch (TimeoutException e) {
            return new HolidayResult(Map.of(), true);
        } catch (Exception e) {
            return new HolidayResult(Map.of(), true);
        }
    }

    public record HolidayResult(Map<String, String> holidays, boolean unavailable) {
    }
}
