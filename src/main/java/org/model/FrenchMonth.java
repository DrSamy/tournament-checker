package org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FrenchMonth {
    JANVIER(1),
    FEVRIER(2),
    MARS(3),
    AVRIL(4),
    MAI(5),
    JUIN(6),
    JUILLET(7),
    AOUT(8),
    SEPTEMBRE(9),
    OCTOBRE(10),
    NOVEMBRE(11),
    DECEMBRE(12);

    private final int monthNumber;
}

