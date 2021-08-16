package eu.openanalytics.phaedra.measservice.api.dto;

import java.util.Map;

import org.springframework.data.annotation.Transient;

import eu.openanalytics.phaedra.measservice.model.Measurement;

public class NewMeasurementDTO extends Measurement {

	@Transient
	private Map<String, float[]> welldata;

	public Map<String, float[]> getWelldata() {
		return welldata;
	}
	public void setWelldata(Map<String, float[]> wellData) {
		this.welldata = wellData;
	}

}
