package eu.openanalytics.phaedra.measservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Table("measurement")
@JsonInclude(Include.NON_NULL)
public class Measurement {

    @Id
    @Column
	private long id;

    @Column
	private String name;
    @Column
	private String barcode;
    @Column
	private String description;
	
    @Column
	private int rows;
    @Column
	private int columns;
	
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
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	public int getColumns() {
		return columns;
	}
	public void setColumns(int columns) {
		this.columns = columns;
	}
	public String[] getWellColumns() {
		return wellColumns;
	}
	public void setWellColumns(String[] wellColumns) {
		this.wellColumns = wellColumns;
	}
	public String[] getSubWellColumns() {
		return subWellColumns;
	}
	public void setSubWellColumns(String[] subWellColumns) {
		this.subWellColumns = subWellColumns;
	}
	public String[] getImageChannels() {
		return imageChannels;
	}
	public void setImageChannels(String[] imageChannels) {
		this.imageChannels = imageChannels;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
}
