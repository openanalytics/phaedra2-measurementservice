package eu.openanalytics.phaedra.measservice.model;

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
	private Date createdOn;
	@Column
	private String createdBy;

	@Column("well_columns")
	private String[] wellColumns;
	@Column("subwell_columns")
	private String[] subWellColumns;
	@Column("image_channels")
	private String[] imageChannels;

	@Column("capture_job_id")
	private Long captureJobId;
}
