package eu.openanalytics.phaedra.measservice.api.dto;

import eu.openanalytics.phaedra.measservice.model.Measurement;
import org.springframework.data.annotation.Transient;

import java.util.Map;

public class NewMeasurementDTO extends Measurement {

	@Transient
	private Map<String, float[]> welldata;

	public Map<String, float[]> getWelldata() {
		return welldata;
	}
	public void setWelldata(Map<String, float[]> wellData) {
		this.welldata = wellData;
	}

	public Measurement asMeasurement() {
		Measurement meas = new Measurement();
		meas.setId(getId());
		meas.setBarcode(getBarcode());
		meas.setDescription(getDescription());
		meas.setName(getName());
		meas.setRows(getRows());
		meas.setColumns(getColumns());
		meas.setCreatedBy(getCreatedBy());
		meas.setCreatedOn(getCreatedOn());
		meas.setWellColumns(getSubWellColumns());
		meas.setSubWellColumns(getSubWellColumns());
		meas.setImageChannels(getImageChannels());
		return meas;
	}
}
