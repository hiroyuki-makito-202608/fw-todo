package com.example.todoapp;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month, Model model) {
        LocalDate today = LocalDate.now();
        YearMonth selectedMonth = YearMonth.of(year != null ? year : today.getYear(),
                month != null ? month : today.getMonthValue());
        LocalDate firstDay = selectedMonth.atDay(1);
        LocalDate lastDay = selectedMonth.atEndOfMonth();
        LocalDate gridStart = firstDay.minusDays(firstDay.getDayOfWeek().getValue() % 7);

        List<CalendarDay> days = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            LocalDate date = gridStart.plusDays(i);
            days.add(new CalendarDay(date.getMonthValue() == selectedMonth.getMonthValue() ? date : null));
        }

        YearMonth previousMonth = selectedMonth.minusMonths(1);
        YearMonth nextMonth = selectedMonth.plusMonths(1);
        model.addAttribute("year", selectedMonth.getYear());
        model.addAttribute("month", selectedMonth.getMonthValue());
        model.addAttribute("firstDay", firstDay);
        model.addAttribute("lastDay", lastDay);
        model.addAttribute("days", days);
        model.addAttribute("previousYear", previousMonth.getYear());
        model.addAttribute("previousMonth", previousMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        return "calendar";
    }

    public record CalendarDay(LocalDate date) {
    }
}
