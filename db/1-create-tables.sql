CREATE SCHEMA IF NOT EXISTS measservice;

DROP TABLE IF EXISTS measservice.measurement;
DROP TABLE IF EXISTS measservice.welldata;

CREATE TABLE measservice.measurement
(
    id              bigserial,
    name            text      NOT NULL,
    barcode         text      NOT NULL,
    description     text,
    rows            int       NOT NULL,
    columns         int       NOT NULL,
    created_on      timestamp NOT NULL,
    created_by      text      NOT NULL,
    well_columns    text[],
    subwell_columns text[],
    image_channels  text[],
    PRIMARY KEY(id)
);

CREATE TABLE measservice.welldata
(
    meas_id     bigint NOT NULL,
    column_name text   NOT NULL,
    values      float[],
    PRIMARY KEY (meas_id, column_name),
    FOREIGN KEY (meas_id) REFERENCES measservice.measurement(id) ON DELETE CASCADE
);