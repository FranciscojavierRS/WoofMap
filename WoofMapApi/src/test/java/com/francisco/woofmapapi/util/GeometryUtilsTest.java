package com.francisco.woofmapapi.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeometryUtilsTest {

    @Test
    @DisplayName("Debe crear un Point con el SRID 4326 y coordenadas correctas")
    void debeCrearPointConSRIDyCoordenadas() {
        double lat = 40.4168; // Madrid
        double lon = -3.7038;

        Point point = GeometryUtils.createPoint(lat, lon);

        assertThat(point).isNotNull();
        assertThat(point.getSRID()).isEqualTo(4326);
        assertThat(GeometryUtils.getLat(point)).isEqualTo(lat);
        assertThat(GeometryUtils.getLon(point)).isEqualTo(lon);
    }

    @Test
    @DisplayName("Debe calcular correctamente la distancia Haversine entre dos puntos")
    void debeCalcularDistanciaHaversine() {
        // Madrid (Puerta del Sol)
        double lat1 = 40.4169;
        double lon1 = -3.7036;

        // Madrid (Parque del Retiro) ~ 1.5 km al este
        double lat2 = 40.4153;
        double lon2 = -3.6844;

        double distancia = GeometryUtils.haversineDistance(lat1, lon1, lat2, lon2);

        // La distancia aproximada es ~1630 metros
        assertThat(distancia).isCloseTo(1630.0, within(100.0));
    }
}
