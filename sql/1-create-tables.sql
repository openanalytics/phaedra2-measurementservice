create schema measservice;

create table measservice.measurement (
	id					bigserial primary key,
	name				text not null,
	barcode				text not null,
	description			text,
	rows				int not null,
	columns				int not null,
	created_on			timestamp not null,
	created_by			text not null,
	well_columns		text[],
	subwell_columns		text[],
	image_channels		text[]
);

create table measservice.welldata (
	meas_id					bigint not null references measurement(id) on delete cascade,
	column_name				text not null,
	values					float[],
	primary key (meas_id, column_name)
);