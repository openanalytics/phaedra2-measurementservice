package eu.openanalytics.phaedra.measservice.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.openanalytics.phaedra.measservice.model.Measurement;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewMeasurementDTO {

	private Long id;
	private String name;
	private String barcode;
	private String description;
	private Integer rows;
	private Integer columns;
	private Date createdOn;
	private String createdBy;
	private String[] wellColumns;
	private String[] subWellColumns;
	private String[] imageChannels;
	private Long captureJobId;
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
		meas.setCaptureJobId(getCaptureJobId());
		return meas;
	}
}
