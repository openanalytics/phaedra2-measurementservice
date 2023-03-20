package eu.openanalytics.phaedra.measservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubwellDataDTO {

    private long measurementId;
    private int wellId;
    private Map<String, float[]> data;

}
