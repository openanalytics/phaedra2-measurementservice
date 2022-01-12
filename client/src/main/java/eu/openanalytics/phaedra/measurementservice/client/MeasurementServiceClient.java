package eu.openanalytics.phaedra.measurementservice.client;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measurementservice.client.exception.MeasUnresolvableException;

public interface MeasurementServiceClient {

    float[] getWellData(long measId, String columnName) throws MeasUnresolvableException;

    MeasurementDTO[] getMeasurements(long ...measId);

}
