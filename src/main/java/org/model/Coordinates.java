package org.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Coordinates {
    private double latitude;
    private double longitude;

    @Override
    public String toString() {
        return longitude + "," + latitude;
    }
}
