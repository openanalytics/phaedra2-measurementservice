package eu.openanalytics.phaedra.measservice.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.measservice.model.NamedImageRenderConfig;
import eu.openanalytics.phaedra.measservice.repository.ImageRenderConfigRepository;
import eu.openanalytics.phaedra.util.auth.IAuthorizationService;

@Service
public class ImageRenderConfigService {

	private final ImageRenderConfigRepository repo;
	private final IAuthorizationService authService;
	private final ModelMapper modelMapper = new ModelMapper();
	
	public ImageRenderConfigService(ImageRenderConfigRepository repo, IAuthorizationService authService) {
		this.repo = repo;
		this.authService = authService;
	}
	
	public NamedImageRenderConfig createConfig(NamedImageRenderConfig config) {
		config.setCreatedBy(authService.getCurrentPrincipalName());
		config.setCreatedOn(new Date());
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
		NamedImageRenderConfig existingConfig = repo.findById(config.getId()).orElse(null);
		if (existingConfig != null) {
			authService.performOwnershipCheck(existingConfig.getCreatedBy());
			modelMapper.typeMap(NamedImageRenderConfig.class, NamedImageRenderConfig.class)
				.setPropertyCondition(Conditions.isNotNull())
				.map(config, existingConfig);
			existingConfig = repo.save(existingConfig);
		}
		return existingConfig;
	}
	
	public List<NamedImageRenderConfig> getAllConfigs() {
		return (List<NamedImageRenderConfig>) repo.findAll();
	}
	
	public void deleteConfig(long id) {
		repo.deleteById(id);
	}
}
