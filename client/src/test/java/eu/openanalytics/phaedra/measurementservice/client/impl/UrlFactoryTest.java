/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.measurementservice.client.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlFactoryTest {

    @Test
    public void getMeasurementsByMeasIds() {
        String url = UrlFactory.getMeasurementsByMeasIds(1, 2, 3, 4);
        assertThat(url).isEqualTo("http://phaedra-measurement-service/phaedra/measurement-service/meas?measIds=1,2,3,4");
    }

    @Test
    public void getAllMeasurements() {
        String url = UrlFactory.getAllMeasurements();
        assertThat(url).isEqualTo("http://phaedra-measurement-service/phaedra/measurement-service/meas");
    }

}
