package org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.model.PlayerRank.*;

@Getter
@AllArgsConstructor
public enum TournamentRank {
    RANK_5(
            List.of(P10, P11, P12),
            50,
            Map.of(
                Department.COTE_D_ARMOR, 1,
                Department.FINISTERE, 1,
                Department.ILE_ET_VILAINE, 2,
                Department.MORBIHAN, 1
            )),
    RANK_4(
            List.of(D8, D9),
            50,
            Map.of(
                    Department.COTE_D_ARMOR, 1,
                    Department.FINISTERE, 1,
                    Department.ILE_ET_VILAINE, 2,
                    Department.MORBIHAN, 1
            )),
    RANK_3(
            List.of(R6, D7),
            50,
            Map.of(
                    Department.COTE_D_ARMOR, 1,
                    Department.FINISTERE, 1,
                    Department.ILE_ET_VILAINE, 1,
                    Department.MORBIHAN, 1
            )),
    RANK_2(
            List.of(R4, R5),
            75,
            Map.of(
                    Department.COTE_D_ARMOR, 1,
                    Department.FINISTERE, 1,
                    Department.ILE_ET_VILAINE, 1,
                    Department.MORBIHAN, 1
            )),
    RANK_1(
            List.of(N1, N2, N3),
            100,
            Map.of(
                    Department.COTE_D_ARMOR, 1,
                    Department.FINISTERE, 1,
                    Department.ILE_ET_VILAINE, 1,
                    Department.MORBIHAN, 1
            ));

    private final List<PlayerRank> playerRanks;
    private final int distance;
    private final Map<Department, Integer> max;

    public static List<TournamentRank> getTournamentRankByPlayerRank(List<PlayerRank> playerRanks) {
        return Arrays.stream(values())
                .filter(tournamentRank -> !CollectionUtils.intersection(tournamentRank.playerRanks, playerRanks).isEmpty())
                .distinct()
                .sorted(Comparator.comparingInt(TournamentRank::ordinal).reversed())
                .toList();
    }

    public static TournamentRank getHighestRankInBothLists(List<TournamentRank> a, List<TournamentRank> b) {
        return CollectionUtils.intersection(a, b).stream()
                .sorted(Comparator.comparingInt(TournamentRank::ordinal).reversed())
                .toList()
                .getFirst();
    }

    public static Map<TournamentRank, Integer> getTournamentLimitByDept(Department department) {
        return Arrays.stream(TournamentRank.values())
                .collect(Collectors.toMap(rank -> rank, rank -> rank.getMax().get(department)));
    }
}
