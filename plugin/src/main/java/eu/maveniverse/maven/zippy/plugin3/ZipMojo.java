/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.zippy.plugin3;

import java.io.File;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.jar.AbstractJarMojo;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;

@SuppressModernizer
@Mojo(
        name = "zip",
        defaultPhase = LifecyclePhase.PACKAGE,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ZipMojo extends AbstractJarMojo {
    /**
     * Directory containing the resource files that should be packaged into the ZIP file.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File classesDirectory;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be attached
     * as a supplemental artifact.
     * If not given this will create the main artifact which is the default behavior.
     * If you try to do that a second time without using a classifier the build will fail.
     */
    @Parameter
    private String classifier;

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClassifier() {
        return classifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getType() {
        return "zip";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File getClassesDirectory() {
        return classesDirectory;
    }

    @Override
    protected File getJarFile(File basedir, String resultFinalName, String classifier) {
        if (basedir == null) {
            throw new IllegalArgumentException("basedir is not allowed to be null");
        }
        if (resultFinalName == null) {
            throw new IllegalArgumentException("finalName is not allowed to be null");
        }

        String fileName = resultFinalName + (hasClassifier() ? "-" + classifier : "") + ".zip";

        return new File(basedir, fileName);
    }
}
