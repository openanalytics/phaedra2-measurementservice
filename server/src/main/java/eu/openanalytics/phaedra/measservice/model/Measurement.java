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
package eu.openanalytics.phaedra.measservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Table("measurement")
@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Measurement {

	@Id
	@Column
	private Long id;

	@Column
	private String name;
	@Column
	private String barcode;
	@Column
	private String description;

	@Column
	private Integer rows;
	@Column
	private Integer columns;

	@Column
	@JsonIgnore
	private Date createdOn;
	@Column
	private String createdBy;

	@Column
	@JsonIgnore
	private Date updatedOn;
	@Column
	private String updatedBy;

	@Column("well_columns")
	private String[] wellColumns;
	@Column("subwell_columns")
	private String[] subWellColumns;
	@Column("image_channels")
	private String[] imageChannels;

	@Column("capture_job_id")
	private Long captureJobId;
}
