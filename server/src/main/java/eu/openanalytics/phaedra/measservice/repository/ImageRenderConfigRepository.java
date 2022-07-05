package eu.openanalytics.phaedra.measservice.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import eu.openanalytics.phaedra.measservice.model.NamedImageRenderConfig;

@Repository
public interface ImageRenderConfigRepository extends CrudRepository<NamedImageRenderConfig, Long> {

	List<NamedImageRenderConfig> findByName(String name);

}

