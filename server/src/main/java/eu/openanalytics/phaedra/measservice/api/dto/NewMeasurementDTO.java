/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
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
