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
package eu.openanalytics.phaedra.measservice.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.phaedra.measservice.model.NamedImageRenderConfig;
import eu.openanalytics.phaedra.measservice.service.ImageRenderConfigService;

@RestController
@RequestMapping(value = "/renderconfigurations")
public class ImageRenderConfigController {

	@Autowired
	private ImageRenderConfigService service;

    @PostMapping
    public ResponseEntity<?> createConfig(@RequestBody String renderConfigString, ObjectMapper objectMapper) {
        try {
        	NamedImageRenderConfig config = objectMapper.readValue(renderConfigString, NamedImageRenderConfig.class);
        	config = service.createConfig(config);
        	return new ResponseEntity<>(config, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<?> updateConfig(@PathVariable long id, @RequestBody NamedImageRenderConfig renderConfig) {
    	renderConfig.setId(id);
        return new ResponseEntity<>(service.updateConfig(renderConfig), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<NamedImageRenderConfig>> getAllConfigs() {
        List<NamedImageRenderConfig> configs = service.getAllConfigs();
        return ResponseEntity.ok(configs);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<NamedImageRenderConfig> getConfig(@PathVariable long id) {
        return ResponseEntity.of(service.getConfigById(id));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable long id) {
        if (!service.configExists(id)) return ResponseEntity.notFound().build();
        service.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }
}
