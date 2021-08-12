package eu.openanalytics.phaedra.measservice.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import eu.openanalytics.phaedra.measservice.model.Measurement;

@Repository
public interface MeasRepository extends CrudRepository<Measurement, Long> {

}
