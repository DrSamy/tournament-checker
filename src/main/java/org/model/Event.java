package org.model;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
public class Event {
    private String title;
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private List<PlayerRank> playerRanks;
    private Department department;
    private EventType type;
    private List<TournamentRank> tournamentRank;

    public List<LocalDate> getDateRange() {
        return Arrays.asList(dateStart, dateEnd);
    }
}
