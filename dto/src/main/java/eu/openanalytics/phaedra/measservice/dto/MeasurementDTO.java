package eu.openanalytics.phaedra.measservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeasurementDTO {

    @JsonIgnore
    private Long id;
    private String name;
    private String barcode;
    @JsonIgnore
    private String description;
    private Integer rows;
    private Integer columns;
    @JsonIgnore
    private Date createdOn;
    @JsonIgnore
    private String createdBy;
    private String[] wellColumns;
    private String[] subWellColumns;
    private String[] imageChannels;
    private Long captureJobId;
}
