package eu.openanalytics.phaedra.measservice.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import eu.openanalytics.phaedra.measservice.model.Measurement;

@Repository
public interface MeasRepository extends CrudRepository<Measurement, Long> {

	@Query("select * from measurement m where m.id in :measIds")
	List<Measurement> findAllByIds(long[] measIds);

	@Query("select * from measurement m where m.created_on >= :date1 and m.created_on <= :date2")
	List<Measurement> findByCreatedOnRange(Date date1, Date date2);

}
