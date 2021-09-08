drop table if exists measservice.measurement;
drop table if exists measservice.welldata;

create table measservice.measurement
(
    id              bigserial,
    name            text      not null,
    barcode         text      not null,
    description     text,
    rows            int       not null,
    columns         int       not null,
    created_on      timestamp not null,
    created_by      text      not null,
    well_columns    text[],
    subwell_columns text[],
    image_channels  text[],
    primary key(id)
);

create table measservice.welldata
(
    meas_id     bigint not null,
    column_name text   not null,
    values      float[],
    primary key (meas_id, column_name),
    foreign key (meas_id) references measservice.measurement(id) on delete cascade
);