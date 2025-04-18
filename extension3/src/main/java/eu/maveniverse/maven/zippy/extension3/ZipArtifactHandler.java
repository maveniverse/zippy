/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.zippy.extension3;

import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.artifact.handler.ArtifactHandler;

/**
 * Zip artifact handler.
 */
@Singleton
@Named("zip")
public class ZipArtifactHandler implements ArtifactHandler {
    @Override
    public String getExtension() {
        return "zip";
    }

    @Override
    public String getDirectory() {
        return getPackaging() + "s";
    }

    @Override
    public String getClassifier() {
        return null;
    }

    @Override
    public String getPackaging() {
        return "zip";
    }

    @Override
    public boolean isIncludesDependencies() {
        return false;
    }

    @Override
    public String getLanguage() {
        return "resources";
    }

    @Override
    public boolean isAddedToClasspath() {
        return Boolean.parseBoolean(System.getProperty("maveniverse.zippy.zip.classpath", "false"));
    }
}
