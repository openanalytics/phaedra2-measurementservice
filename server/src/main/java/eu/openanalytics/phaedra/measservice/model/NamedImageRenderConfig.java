package eu.openanalytics.phaedra.measservice.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.annotation.Id;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.phaedra.imaging.render.ImageRenderConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

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
