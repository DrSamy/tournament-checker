package org;

import lombok.val;
import org.json.JSONObject;
import org.model.Coordinates;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.model.Department.getCodeList;

public class MapUtils {
    public static double getRoadDistanceBetween(String src, String dest) {
        try (val client = HttpClient.newHttpClient()) {
            val request = HttpRequest.newBuilder()
                    .uri(URI.create("https://router.project-osrm.org/route/v1/driving/" + getCoordinates(src) + ";" + getCoordinates(dest) + "?overview=false"))
                    .build();
            val response = client.send(request, HttpResponse.BodyHandlers.ofString());
            val jsonObject = new JSONObject(response.body());
            val routes = jsonObject.getJSONArray("routes");
            val firstRoute = routes.getJSONObject(0);
            val legs = firstRoute.getJSONArray("legs");
            val firstLeg = legs.getJSONObject(0);
            return firstLeg.getDouble("distance")/1000;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Coordinates getCoordinates(String search) {
        try (val client = HttpClient.newHttpClient()) {
            val encoded = URLEncoder.encode(search, StandardCharsets.UTF_8);
            val request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api-adresse.data.gouv.fr/search/?q=" + encoded + "&type=municipality&limit=1"))
                    .build();
            val response = client.send(request, HttpResponse.BodyHandlers.ofString());
            val jsonObject = new JSONObject(response.body());
            val features = jsonObject.getJSONArray("features");

            return features.toList().stream()
                    .map(f -> new JSONObject((Map<?, ?>) f))
                    .filter(f -> {
                        val prop = f.getJSONObject("properties");
                        return getCodeList().contains(Integer.parseInt(prop.getString("postcode").substring(0, 2)));
                    })
                    .findFirst()
                    .map(f -> f.getJSONObject("geometry").getJSONArray("coordinates"))
                    .map(c -> Coordinates.builder()
                            .longitude(c.getDouble(0))
                            .latitude(c.getDouble(1))
                            .build())
                    .orElse(null);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
