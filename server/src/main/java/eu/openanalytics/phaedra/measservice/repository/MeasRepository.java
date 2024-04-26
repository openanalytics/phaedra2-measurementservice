/**
 * Phaedra II
 *
 * Copyright (C) 2016-2024 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.measservice.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import eu.openanalytics.phaedra.measservice.model.Measurement;

@Repository
public interface MeasRepository extends CrudRepository<Measurement, Long> {

	@Query("select * from measurement m where m.id  = any(:measIds)")
	List<Measurement> findAllByIds(long[] measIds);

	@Query("select * from measurement m where m.created_on >= :date1 and m.created_on <= :date2")
	List<Measurement> findByCreatedOnRange(Date date1, Date date2);

	@Query(value = "select distinct trim(unnest(well_columns)) AS column_name FROM measurement ORDER BY column_name ASC")
	List<String> findDistinctWellColumns();

	@Query(value = "select distinct trim(unnest(subwell_columns)) AS column_name FROM measurement ORDER BY column_name ASC")
	List<String> findDistinctSubWellColumns();

}
