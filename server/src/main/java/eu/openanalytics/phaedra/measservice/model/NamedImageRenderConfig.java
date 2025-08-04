/**
 * Phaedra II
 *
 * Copyright (C) 2016-2025 Open Analytics
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.imaging.render.ImageRenderConfig;
import eu.openanalytics.phaedra.measservice.record.PropertyRecord;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("image_render_config")
@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class NamedImageRenderConfig {

	@Id
	@Column
	private Long id;

	@Column
	private String name;

	@Column
	private Date createdOn;
	@Column
	private String createdBy;

	@Column
	private ImageRenderConfig config;

	@Transient
	private List<String> tags;
	@Transient
	private List<PropertyRecord> properties;

	@ReadingConverter
	public static class ConfigReadingConverter implements Converter<PGobject, ImageRenderConfig> {

		private ObjectMapper mapper;

		public ConfigReadingConverter(ObjectMapper mapper) {
			this.mapper = mapper;
		}

		@Override
		public ImageRenderConfig convert(PGobject source) {
			try {
				return mapper.readValue(source.getValue(), ImageRenderConfig.class);
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	@WritingConverter
	public static class ConfigWritingConverter implements Converter<ImageRenderConfig, PGobject> {

		private ObjectMapper mapper;

		public ConfigWritingConverter(ObjectMapper mapper) {
			this.mapper = mapper;
		}

		@Override
		public PGobject convert(ImageRenderConfig source) {
			PGobject target = new PGobject();
			target.setType("json");
			try {
				String stringValue = mapper.writeValueAsString(source);
				target.setValue(stringValue);
			} catch (IOException | SQLException e) {
				throw new IllegalArgumentException(e);
			}
			return target;
		}
	}
}
