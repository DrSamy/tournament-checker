package org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum Department {
    COTE_D_ARMOR(22),
    ILE_ET_VILAINE(35),
    FINISTERE(29),
    MORBIHAN(56);

    private final int code;

    public static List<Integer> getCodeList() {
        return Arrays.stream(values())
                .map(Department::getCode)
                .toList();
    }

    public static Department getByCode(Integer code) {
        return Arrays.stream(values())
                .filter(d -> code != null && d.getCode() == code)
                .findFirst()
                .orElse(null);
    }
}
