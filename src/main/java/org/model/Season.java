package org.model;

import lombok.val;

import java.time.LocalDate;

public class Season {

    public static int getSeasonYearStart() {
        val currentMonth = LocalDate.now().getMonthValue();
        val currentYear = LocalDate.now().getYear();
        return currentMonth >= 9 ? currentYear : currentYear - 1;
    }

    public static int getSeasonYearEnd() {
        val currentMonth = LocalDate.now().getMonthValue();
        val currentYear = LocalDate.now().getYear();
        return currentMonth >= 9 ? currentYear + 1 : currentYear;
    }

    public static int getSeasonYearByMonth(int month) {
        return month >= 9 ? getSeasonYearStart() : getSeasonYearEnd();
    }

    public static LocalDate getSeasonDateStart() {
        return LocalDate.of(getSeasonYearStart(), 9, 1);
    }

    public static LocalDate getSeasonDateEnd() {
        return LocalDate.of(getSeasonYearEnd(), 8, 31);
    }

    public static String asString() {
        return getSeasonYearStart() + "-" + getSeasonYearEnd();
    }
}
