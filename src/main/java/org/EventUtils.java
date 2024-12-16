package org;

import api_vacances.ApiException;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.DateUtils.*;
import static org.ExcelUtils.getEvents;
import static org.MapUtils.getRoadDistanceBetween;
import static org.model.PlayerRank.getRange;
import static org.model.TournamentRank.*;

public class EventUtils {
    public static void dateAvailableFinder(PlayerRank max, PlayerRank min, String postalCode, String file) throws IOException, InterruptedException {
        val events = getEvents(file);
        for (List<LocalDate> weekend : getNextWeekends()) {
            dateAvailableChecker(max, min, weekend.getFirst(), weekend.getLast(), postalCode, true, events, file);
        }
    }

    public static void dateAvailableChecker(PlayerRank max, PlayerRank min, LocalDate dateStart, LocalDate dateEnd, String postalCode, boolean displayOnlyAvailable, List<Event> events, String file) throws IOException, InterruptedException {
        val range = getRange(max, min);
        val currentEvent = Event.builder()
                .title(postalCode)
                .department(Department.getByCode(Integer.valueOf(postalCode.substring(0,2))))
                .dateStart(dateStart)
                .dateEnd(dateEnd)
                .playerRanks(range)
                .tournamentRank(getTournamentRankByPlayerRank(range))
                .build();

        events = events == null ? getEvents(file) : events;

        val concurrence = events.stream()
                .filter(e -> EventType.TOURNAMENT.equals(e.getType()) || EventType.CHAMPIONSHIP.equals(e.getType()))
                .filter(e -> (e.getDateStart().isBefore(currentEvent.getDateEnd()) || e.getDateStart().isEqual(currentEvent.getDateEnd()))
                        && (e.getDateEnd().isAfter(currentEvent.getDateStart()) || e.getDateEnd().isEqual(currentEvent.getDateStart())))
                .filter(e -> !CollectionUtils.intersection(e.getPlayerRanks(), currentEvent.getPlayerRanks()).isEmpty())
                .toList();

        if (concurrence.stream().anyMatch(e -> EventType.CHAMPIONSHIP.equals(e.getType()))) {
            if (!displayOnlyAvailable) System.out.println("Championnat déjà prévu : " + concurrence.stream()
                    .filter(e -> EventType.CHAMPIONSHIP.equals(e.getType()))
                    .map(Event::getTitle)
                    .collect(Collectors.joining(", ")));
            return;
        }

        if (maxReachedInDepartment(concurrence, currentEvent.getTournamentRank(), currentEvent.getDepartment())) {
            if (!displayOnlyAvailable) System.out.println("Maximum atteint dans le département : " + concurrence.stream()
                    .map(Event::getTitle)
                    .collect(Collectors.joining(", ")));
            return;
        }

        for (Event e : concurrence) {
            if (tournamentsTooClose(e, currentEvent)) {
                if (!displayOnlyAvailable) System.out.println("Trop proche : " + e.getTitle());
                return;
            }
        }

        val interclub = events.stream()
                .filter(e -> EventType.ICN.equals(e.getType()) || EventType.ICR.equals(e.getType()))
                .filter(e -> overlap(e.getDateRange(), currentEvent.getDateRange()))
                        .toList();

        val pending = events.stream()
                .filter(e -> EventType.TOURNAMENT_PENDING.equals(e.getType()))
                .filter(e -> overlap(e.getDateRange(), currentEvent.getDateRange()))
                .toList();

        val holidays = getPublicHolidays().stream().anyMatch(h -> overlap(Arrays.asList(dateStart, dateEnd), h)) ||
                        getHolidays().stream().anyMatch(h -> overlap(Arrays.asList(dateStart, dateEnd), h));

        val sideEvent = CollectionUtils.isNotEmpty(interclub);
        val pendingEvent = CollectionUtils.isNotEmpty(pending);

        var symbol = "(✓)";
        if (sideEvent) {
            symbol = "/!\\";
        } else if (pendingEvent) {
            symbol = "(?)";
        } else if (holidays) {
            symbol = "(-)";
        }

        System.out.println(symbol + " La date " + dateStart + " / " + dateEnd + " est disponible");
        if (sideEvent) {
            System.out.println("\tÉvénements en parallèle : " + interclub.stream()
                    .map(Event::getTitle)
                    .collect(Collectors.joining(", ")));
        }
        if (pendingEvent) {
            System.out.println("\tDemandes en attente : " + pending.stream()
                    .map(Event::getTitle)
                    .collect(Collectors.joining(", ")));
        }
        if (holidays) {
            System.out.println("\tDes vacances sont prévues sur la période.");
        }
    }

    private static boolean maxReachedInDepartment(List<Event> events, List<TournamentRank> tournamentRanks, Department department) {
        val map = getTournamentLimitByDept(department);

        events.stream()
                .filter(e -> e.getDepartment() != null && e.getDepartment().equals(department))
                .flatMap(e -> e.getTournamentRank().stream())
                .forEach(t -> map.computeIfPresent(t, (key, value) -> value - 1));

        return tournamentRanks.stream().anyMatch(t -> map.get(t) <= 0);
    }

    private static boolean tournamentsTooClose(Event a, Event b) {
        val maxTournamentRank = getHighestRankInBothLists(a.getTournamentRank(), b.getTournamentRank());
        return getRoadDistanceBetween(a.getTitle(), b.getTitle()) < maxTournamentRank.getDistance();
    }
}
