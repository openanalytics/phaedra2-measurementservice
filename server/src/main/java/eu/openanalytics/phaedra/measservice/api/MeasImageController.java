/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
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
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.openanalytics.phaedra.imaging.render.ImageRenderConfig;
import eu.openanalytics.phaedra.imaging.util.ImageRenderConfigUtils;
import eu.openanalytics.phaedra.measservice.service.MeasImageService;

@RestController
public class MeasImageController {

	@Autowired
	private MeasImageService measImageService;
	
	@RequestMapping(value = "/image/{measId}/{wellNr}/{channel}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> renderImage(@PathVariable long measId, @PathVariable int wellNr, @PathVariable String channel,
    		@RequestParam(name="renderConfigId", required=false) Long renderConfigId, HttpServletRequest request) {
    	try {
    		ImageRenderConfig renderConfig = ImageRenderConfigUtils.parseFromParameters(request.getParameterMap());
    		byte[] rendered = measImageService.renderImage(measId, wellNr, channel, renderConfigId, renderConfig);
   			return ResponseEntity.of(Optional.ofNullable(rendered));
    	} catch (IOException e) {
    		throw new RuntimeException("Render failed", e);
    	}
    }
    
	@RequestMapping(value = "/image/{measId}/{wellNr}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> renderImage(@PathVariable long measId, @PathVariable int wellNr,
    		@RequestParam(name="renderConfigId", required=false) Long renderConfigId, HttpServletRequest request) {
    	try {
    		ImageRenderConfig renderConfig = ImageRenderConfigUtils.parseFromParameters(request.getParameterMap());
    		byte[] rendered = measImageService.renderImage(measId, wellNr, renderConfigId, renderConfig);
   			return ResponseEntity.of(Optional.ofNullable(rendered));
    	} catch (IOException e) {
    		throw new RuntimeException("Render failed", e);
    	}
    }
}
