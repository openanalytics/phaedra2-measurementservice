package eu.openanalytics.phaedra.measurementservice.client;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measurementservice.client.exception.MeasUnresolvableException;

import java.util.List;

public interface MeasurementServiceClient {

    float[] getWellData(long measId, String columnName) throws MeasUnresolvableException;

    List<MeasurementDTO> getMeasurementsByMeasIds(long ...measId);

}
