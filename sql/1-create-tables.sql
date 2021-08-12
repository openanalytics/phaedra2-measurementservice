create schema measservice;

create table measservice.measurement (
	id					bigserial primary key,
	name				text not null,
	description			text,
	well_columns		text[],
	subwell_columns		text[],
	image_channels		text[]
);