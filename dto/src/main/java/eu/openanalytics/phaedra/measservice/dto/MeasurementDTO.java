package eu.openanalytics.phaedra.measservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeasurementDTO {

    private long id;
    private String name;
    private String barcode;
    private String description;
    private int rows;
    private int columns;
    private Date createdOn;
    private String createdBy;
    private String[] wellColumns;
    private String[] subWellColumns;
    private String[] imageChannels;
    private long captureJobId;
}
