/**
 * Phaedra II
 *
 * Copyright (C) 2016-2025 Open Analytics
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
package eu.openanalytics.phaedra.measservice.service;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import eu.openanalytics.phaedra.measservice.model.NamedImageRenderConfig;
import eu.openanalytics.phaedra.measservice.record.PropertyRecord;
import eu.openanalytics.phaedra.measservice.repository.ImageRenderConfigRepository;
import eu.openanalytics.phaedra.metadataservice.client.MetadataServiceGraphQlClient;
import eu.openanalytics.phaedra.metadataservice.dto.MetadataDTO;
import eu.openanalytics.phaedra.metadataservice.dto.PropertyDTO;
import eu.openanalytics.phaedra.metadataservice.dto.TagDTO;
import eu.openanalytics.phaedra.metadataservice.enumeration.ObjectClass;
import eu.openanalytics.phaedra.util.auth.IAuthorizationService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ImageRenderConfigService {

  private final ImageRenderConfigRepository repo;
  private final IAuthorizationService authService;
  private final ModelMapper modelMapper = new ModelMapper();
  private final MetadataServiceGraphQlClient metadataServiceGraphQlClient;

  public ImageRenderConfigService(ImageRenderConfigRepository repo,
      IAuthorizationService authService,
      MetadataServiceGraphQlClient metadataServiceGraphQlClient) {
    this.repo = repo;
    this.authService = authService;
    this.metadataServiceGraphQlClient = metadataServiceGraphQlClient;
  }

  public NamedImageRenderConfig createConfig(NamedImageRenderConfig config) {
    config.setCreatedBy(authService.getCurrentPrincipalName());
    config.setCreatedOn(new Date());
    return repo.save(config);
  }

  public Optional<NamedImageRenderConfig> getConfigById(long id) {
    return enrichWithMetadata(repo.findById(id));
  }

  @Cacheable
  public Optional<NamedImageRenderConfig> getConfigByName(String name) {
    return enrichWithMetadata(repo.findByName(name).stream().findAny());
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
    List<NamedImageRenderConfig> renderConfigList = (List<NamedImageRenderConfig>) repo.findAll();
    enrichWithMetadata(renderConfigList);
    return renderConfigList;
  }

  public void deleteConfig(long id) {
    repo.deleteById(id);
  }

  private List<PropertyRecord> convertToPropertyRecords(List<PropertyDTO> properties) {
    if (isEmpty(properties)) {
      return new ArrayList<>(); // or return null, depending on requirements
    }

    return properties.stream()
        .map(property -> new PropertyRecord(
            property.getPropertyName(),
            property.getPropertyValue()))
        .collect(toList());
  }

  private Optional<NamedImageRenderConfig> enrichWithMetadata(Optional<NamedImageRenderConfig> renderConfig) {
    if (renderConfig.isPresent()) {
      enrichWithMetadata(List.of(renderConfig.get()));
    }
    return renderConfig;
  }

  private void enrichWithMetadata(List<NamedImageRenderConfig> renderConfigs) {
    if (isEmpty(renderConfigs)) {
      return;
    }

    Map<Long, NamedImageRenderConfig> renderConfigIdMap = new HashMap<>();
    for (NamedImageRenderConfig namedImageRenderConfig : renderConfigs) {
      renderConfigIdMap.put(namedImageRenderConfig.getId(), namedImageRenderConfig);
    }
    List<Long> renderConfigIds = new ArrayList<>(renderConfigIdMap.keySet());

    List<MetadataDTO> renderConfigMetadataList = metadataServiceGraphQlClient.getMetadata(renderConfigIds, ObjectClass.RENDER_CONFIG);

    for (MetadataDTO metadata : renderConfigMetadataList) {
      NamedImageRenderConfig renderConfig = renderConfigIdMap.get(metadata.getObjectId());
      if (renderConfig != null) {
        renderConfig.setTags(metadata.getTags().stream().map(TagDTO::getTag).toList());
        renderConfig.setProperties(convertToPropertyRecords(metadata.getProperties()));
      }
    }
  }
}
