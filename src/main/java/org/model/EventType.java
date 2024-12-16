package org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
    TOURNAMENT,
    TOURNAMENT_PENDING,
    CHAMPIONSHIP,
    ICR,
    ICN;

    public static boolean isChampionShip(EventType type) {
        return CHAMPIONSHIP.equals(type);
    }
}
