package eu.openanalytics.phaedra.measservice.service;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.model.Measurement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelMapperTest {
    private ModelMapper modelMapper;

    @BeforeEach
    void before() {
        this.modelMapper = new ModelMapper();
    }

    @Test
    public void contextLoads() {
        assertThat(modelMapper).isNotNull();
    }

    @Test
    public void mapMeasurementToMeasurementDTO() throws ParseException {
        Measurement measurement = new Measurement();
        measurement.setId(1000L);
        measurement.setName("Test measurement");
        measurement.setBarcode("123456789");
        measurement.setDescription("String description");
        measurement.setRows(24);
        measurement.setColumns(32);
        measurement.setWellColumns(new String[]{"wellColumns1", "wellColumns2", "wellColumns3"});
        measurement.setSubWellColumns(new String[]{"subWellColumns1", "subWellColumns2", "subWellColumns3"});
        measurement.setImageChannels(new String[]{"imageChannels1", "imageChannels2", "imageChannels3"});
        measurement.setCreatedOn(new SimpleDateFormat("dd-MM-yyyy").parse("12-01-2022"));
        measurement.setCreatedBy("sberberovic");

        MeasurementDTO measurementDTO = modelMapper.map(measurement);
        assertThat(measurementDTO.getBarcode().equals(measurement.getBarcode()));
        assertThat(measurementDTO.getColumns() == measurement.getColumns());
        assertThat(measurementDTO.getWellColumns().equals(measurement.getWellColumns()));
        assertThat(measurementDTO.getSubWellColumns().equals(measurement.getSubWellColumns()));
        assertThat(measurementDTO.getImageChannels().equals(measurement.getImageChannels()));
        assertThat(measurementDTO.getCreatedOn().equals(measurement.getCreatedOn()));
    }

    @Test
    public void mapMeasurementDTOToMeasurement() throws ParseException {
        MeasurementDTO measurementDTO = new MeasurementDTO();
        measurementDTO.setId(1000L);
        measurementDTO.setName("Test measurement");
        measurementDTO.setBarcode("123456789");
        measurementDTO.setDescription("String description");
        measurementDTO.setRows(24);
        measurementDTO.setColumns(32);
        measurementDTO.setWellColumns(new String[]{"wellColumns1", "wellColumns2", "wellColumns3"});
        measurementDTO.setSubWellColumns(new String[]{"subWellColumns1", "subWellColumns2", "subWellColumns3"});
        measurementDTO.setImageChannels(new String[]{"imageChannels1", "imageChannels2", "imageChannels3"});
        measurementDTO.setCreatedOn(new SimpleDateFormat("dd-MM-yyyy").parse("12-01-2022"));
        measurementDTO.setCreatedBy("sberberovic");

        Measurement measurement = modelMapper.map(measurementDTO);
        assertThat(measurementDTO.getBarcode().equals(measurement.getBarcode()));
        assertThat(measurementDTO.getColumns() == measurement.getColumns());
        assertThat(measurementDTO.getWellColumns().equals(measurement.getWellColumns()));
        assertThat(measurementDTO.getSubWellColumns().equals(measurement.getSubWellColumns()));
        assertThat(measurementDTO.getImageChannels().equals(measurement.getImageChannels()));
        assertThat(measurementDTO.getCreatedOn().equals(measurement.getCreatedOn()));
    }
}
