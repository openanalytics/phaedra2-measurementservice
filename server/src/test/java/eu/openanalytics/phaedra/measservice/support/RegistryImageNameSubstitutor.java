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
package eu.openanalytics.phaedra.measservice.support;

import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ImageNameSubstitutor;

public class RegistryImageNameSubstitutor extends ImageNameSubstitutor {

    private static final String REGISTRY_NAME = "registry.openanalytics.eu";

    @Override
    public DockerImageName apply(DockerImageName original) {
        String newName = original.asCanonicalNameString();
        if (newName.toLowerCase().startsWith(REGISTRY_NAME)) return original;

        String[] nameParts = newName.split("/");
        int slashCount = nameParts.length - 1;
        if (slashCount == 0) {
            newName = REGISTRY_NAME + "/proxy/library/" + newName;
        } else {
            String firstPart = nameParts[0];
            boolean firstPartIsReg = firstPart.contains(".");

            if (firstPartIsReg) {
                newName = newName.substring(firstPart.length() + 1);
                slashCount--;
            }

            if (slashCount == 0) {
                newName = REGISTRY_NAME + "/proxy/library/" + newName;
            } else {
                newName = REGISTRY_NAME + "/proxy/" + newName;
            }
        }

        return DockerImageName.parse(newName);
    }

    @Override
    protected String getDescription() {
        return String.format("Modifies image names to include %s", REGISTRY_NAME);
    }

}
