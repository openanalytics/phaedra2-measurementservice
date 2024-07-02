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
package eu.openanalytics.phaedra.measservice.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.openanalytics.phaedra.imaging.render.ImageRenderConfig;
import eu.openanalytics.phaedra.imaging.util.ImageRenderConfigUtils;
import eu.openanalytics.phaedra.measservice.service.MeasImageService;

@RestController
public class MeasImageController {

	@Autowired
	private MeasImageService measImageService;
	
	@GetMapping(value = "/measurements/{measurementId}/images/{wellNr}/{channel}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> renderImage(@PathVariable long measurementId, @PathVariable int wellNr, @PathVariable String channel,
    		@RequestParam(name="renderConfigId", required=false) Long renderConfigId, HttpServletRequest request) {
    	try {
    		ImageRenderConfig renderConfig = ImageRenderConfigUtils.parseFromParameters(request.getParameterMap());
    		
    		byte[] rendered = null;
    		if (channel.contains(",")) {
    			List<String> channels = Arrays.stream(channel.split(",")).collect(Collectors.toList());
    			rendered = measImageService.renderImage(measurementId, wellNr, channels, renderConfigId, renderConfig);
    		} else {
    			rendered = measImageService.renderImage(measurementId, wellNr, channel, renderConfigId, renderConfig);
    		}
   			
    		return ResponseEntity.of(Optional.ofNullable(rendered));
    	} catch (IOException e) {
    		throw new RuntimeException("Image render failed", e);
    	}
    }
    
	@GetMapping(value = "/measurements/{measurementId}/images/{wellNr}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> renderImage(@PathVariable long measurementId, @PathVariable int wellNr,
    		@RequestParam(name="renderConfigId", required=false) Long renderConfigId, HttpServletRequest request) {
    	try {
    		ImageRenderConfig renderConfig = ImageRenderConfigUtils.parseFromParameters(request.getParameterMap());
    		byte[] rendered = measImageService.renderImage(measurementId, wellNr, renderConfigId, renderConfig);
   			return ResponseEntity.of(Optional.ofNullable(rendered));
    	} catch (IOException e) {
    		throw new RuntimeException("Image render failed", e);
    	}
    }
}
