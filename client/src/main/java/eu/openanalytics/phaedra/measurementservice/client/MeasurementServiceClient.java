package eu.openanalytics.phaedra.measurementservice.client;

import eu.openanalytics.phaedra.measurementservice.client.exception.MeasUnresolvableException;

public interface MeasurementServiceClient {

    float[] getWellData(long measId, String columnName) throws MeasUnresolvableException;

}
