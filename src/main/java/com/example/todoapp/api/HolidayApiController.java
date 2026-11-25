package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolidayApiController {

    private final HolidayClient holidayClient;

    public HolidayApiController(HolidayClient holidayClient) {
        this.holidayClient = holidayClient;
    }

    @GetMapping("/api/holidays")
    public ResponseEntity<Map<String, String>> getHolidays(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        HolidayClient.HolidayResult result = holidayClient.getHolidays();
        Map<String, String> holidays = result.holidays();
        if (from == null && to == null) {
            return response(holidays, result.unavailable());
        }

        LocalDate start = from != null ? from : LocalDate.MIN;
        LocalDate end = to != null ? to : LocalDate.MAX;
        Map<String, String> filtered = new TreeMap<>();
        holidays.forEach((date, name) -> {
            LocalDate holidayDate = LocalDate.parse(date);
            if (!holidayDate.isBefore(start) && !holidayDate.isAfter(end)) {
                filtered.put(date, name);
            }
        });
        return response(filtered, result.unavailable());
    }

    private ResponseEntity<Map<String, String>> response(Map<String, String> holidays, boolean unavailable) {
        if (unavailable) {
            return ResponseEntity.ok().header("X-Holidays-Unavailable", "true").body(holidays);
        }
        return ResponseEntity.ok(holidays);
    }
}
