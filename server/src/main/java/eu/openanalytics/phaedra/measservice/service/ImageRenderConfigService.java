package eu.openanalytics.phaedra.measservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.measservice.model.NamedImageRenderConfig;
import eu.openanalytics.phaedra.measservice.repository.ImageRenderConfigRepository;

@Service
public class ImageRenderConfigService {

	private final ImageRenderConfigRepository repo;

	public ImageRenderConfigService(ImageRenderConfigRepository repo) {
		this.repo = repo;
	}
	
	public NamedImageRenderConfig createConfig(NamedImageRenderConfig config) {
		return repo.save(config);
	}
	
	public Optional<NamedImageRenderConfig> getConfigById(long id) {
		return repo.findById(id);
	}
	
	@Cacheable
	public Optional<NamedImageRenderConfig> getConfigByName(String name) {
		return repo.findByName(name).stream().findAny();
	}
	
	public boolean configExists(long id) {
		return repo.existsById(id);
	}
	
	public NamedImageRenderConfig updateConfig(NamedImageRenderConfig config) {
		return repo.save(config);
	}
	
	public List<NamedImageRenderConfig> getAllConfigs() {
		return (List<NamedImageRenderConfig>) repo.findAll();
	}
	
	public void deleteConfig(long id) {
		repo.deleteById(id);
	}
}
