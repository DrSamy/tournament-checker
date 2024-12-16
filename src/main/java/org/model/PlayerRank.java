package org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@AllArgsConstructor
public enum PlayerRank {
    NC,
    P12,
    P11,
    P10,
    D9,
    D8,
    D7,
    R6,
    R5,
    R4,
    N3,
    N2,
    N1;

    private static final String REGEX = "^\\s*([A-Z]{1,2}[0-9]{0,2})\\s*à\\s*([A-Z]{1,2}[0-9]{0,2})\\s*$";
    public static List<PlayerRank> getRangeFromString(String input, boolean isChampionShip) {
        if (isChampionShip) return getRange(N1, D9);
        if (isBlank(input)) return Collections.emptyList();
        val pattern = Pattern.compile(REGEX);
        val matcher = pattern.matcher(input);
        if (!matcher.find()) return Collections.emptyList();
        val max = PlayerRank.valueOf(matcher.group(1));
        val min = PlayerRank.valueOf(matcher.group(2));
        return getRange(max, min);
    }

    public static List<PlayerRank> getRange(PlayerRank max, PlayerRank min) {
        return Arrays.stream(values())
                .filter(rank -> rank.ordinal() >= min.ordinal() && rank.ordinal() <= max.ordinal())
                .sorted(Comparator.comparingInt(PlayerRank::ordinal).reversed())
                .toList();
    }
}
