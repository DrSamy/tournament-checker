package org;

import api_jours_feries.ApiException;
import api_jours_feries.Configuration;
import api_jours_feries.apis.DefaultApi;
import api_jours_feries.models.Zone;
import api_vacances.apis.DatasetApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.json.JSONObject;
import org.model.FrenchMonth;
import org.model.Season;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DateUtils {

    public static String getLastYear() {
        return String.valueOf(LocalDate.now().minusYears(1).getYear());
    }

    public static int getMonthNumber(String frenchMonthName) {
        val normalizedName = Normalizer.normalize(frenchMonthName.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toUpperCase();
        return FrenchMonth.valueOf(normalizedName.toUpperCase()).getMonthNumber();
    }

    public static List<List<LocalDate>> getNextWeekends() {
        List<List<LocalDate>> weekendDates = new ArrayList<>();

        val startDate = LocalDate.now();
        val endDate = Season.getSeasonDateEnd();

        // Include the end date
        long numOfDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        List<LocalDate> currentWeekend = new ArrayList<>();

        for (int i = 0; i < numOfDays; i++) {
            val date = startDate.plusDays(i);
            val day = date.getDayOfWeek();

            if (day == DayOfWeek.SATURDAY) {
                currentWeekend.add(date);
            } else if (day == DayOfWeek.SUNDAY) {
                currentWeekend.add(date);
                weekendDates.add(currentWeekend);
                currentWeekend = new ArrayList<>();
            }
        }

        return weekendDates;
    }

    public static boolean overlap(List<LocalDate> a, LocalDate b) {
        return overlap(a, Arrays.asList(b, b));
    }

    public static boolean overlap(List<LocalDate> a, List<LocalDate> b) {
        if (a.size() != 2 || b.size() != 2) {
            throw new IllegalArgumentException("Each list must contain exactly two LocalDate objects");
        }
        return (a.getFirst().isBefore(b.getLast()) || a.getFirst().isEqual(b.getLast()))
                && (a.getLast().isAfter(b.getFirst()) || a.getLast().isEqual(b.getFirst()));
    }

    public static List<LocalDate> getPublicHolidays() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        val apiClient = Configuration.getDefaultApiClient();
        val defaultApi = new DefaultApi(apiClient);
        try {
            return defaultApi.zoneJsonGet(Zone.METROPOLE).keySet().stream()
                    .map(key -> LocalDate.parse(key, formatter))
                    .filter(d -> d.isAfter(LocalDate.now()) && d.isBefore(Season.getSeasonDateEnd()))
                    .toList();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<List<LocalDate>> getHolidays() {
        try (val client = HttpClient.newHttpClient()) {
            val request = HttpRequest.newBuilder()
                    .uri(URI.create("https://data.education.gouv.fr/api/explore/v2.1/catalog/datasets/fr-en-calendrier-scolaire/records?select=start_date%2Cend_date&where=start_date%3E%3Ddate%27" + LocalDate.now() + "%27%20AND%20end_date%3C%3Ddate%27" + Season.getSeasonDateEnd() + "%27%20AND%20%20location%3D%27Rennes%27%20AND%20population%3D%27-%27&order_by=start_date&lang=fr"))
                    .build();
            val response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // Parse the JSON response
            val mapper = new ObjectMapper();
            val jsonNode = mapper.readTree(response.body());

            // Extract the list of dates
            List<List<LocalDate>> dates = new ArrayList<>();

            val results = jsonNode.get("results");
            for (val result : results) {
                String startDateString = result.get("start_date").asText();
                String endDateString = result.get("end_date").asText();

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
                val startZdt = ZonedDateTime.parse(startDateString, formatter);
                val endZdt = ZonedDateTime.parse(endDateString, formatter);

                val startDate = startZdt.toLocalDate();
                val endDate = endZdt.toLocalDate();

                dates.add(Arrays.asList(startDate, endDate));
            }

            return dates;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
