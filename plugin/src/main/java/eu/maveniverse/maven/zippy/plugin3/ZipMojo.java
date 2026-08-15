/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.zippy.plugin3;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Map;
import javax.inject.Inject;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.util.DefaultFileSet;

@Mojo(
        name = "zip",
        defaultPhase = LifecyclePhase.PACKAGE,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.NONE)
public class ZipMojo extends AbstractMojo {
    private static final String[] DEFAULT_EXCLUDES = new String[] {"**/package.html"};
    private static final String[] DEFAULT_INCLUDES = new String[] {"**/**"};
    private static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    /**
     * List of files to include. Specified as fileset patterns which are relative to the input directory whose contents
     * is being packaged into the JAR.
     */
    @Parameter
    private String[] includes;

    /**
     * List of files to exclude. Specified as fileset patterns which are relative to the input directory whose contents
     * is being packaged into the JAR.
     */
    @Parameter
    private String[] excludes;

    /**
     * Directory containing the generated JAR.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    /**
     * Name of the generated JAR.
     */
    @Parameter(defaultValue = "${project.build.finalName}", readonly = true)
    private String finalName;

    /**
     * Require the jar plugin to build a new JAR even if none of the contents appear to have changed. By default, this
     * plugin looks to see if the output jar exists and inputs have not changed. If these conditions are true, the
     * plugin skips creation of the jar. This does not work when other plugins, like the maven-shade-plugin, are
     * configured to post-process the jar. This plugin can not detect the post-processing, and so leaves the
     * post-processed jar in place. This can lead to failures when those plugins do not expect to find their own output
     * as an input. Set this parameter to <tt>true</tt> to avoid these problems by forcing this plugin to recreate the
     * jar every time.<br/>
     * Starting with <b>3.0.0</b> the property has been renamed from <code>jar.forceCreation</code> to
     * <code>maven.jar.forceCreation</code>.
     */
    @Parameter(property = "maven.jar.forceCreation", defaultValue = "false")
    private boolean forceCreation;

    /**
     * Skip creating empty archives.
     */
    @Parameter(defaultValue = "false")
    private boolean skipIfEmpty;

    /**
     * Timestamp for reproducible output archive entries, either formatted as ISO 8601 extended offset date-time
     * (e.g. in UTC such as '2011-12-03T10:15:30Z' or with an offset '2019-10-05T20:37:42+06:00'),
     * or as an int representing seconds since the epoch
     * (like <a href="https://reproducible-builds.org/docs/source-date-epoch/">SOURCE_DATE_EPOCH</a>).
     */
    @Parameter(defaultValue = "${project.build.outputTimestamp}")
    private String outputTimestamp;

    /**
     * If set to {@code false}, the files and directories that by default are excluded from the resulting archive,
     * like {@code .gitignore}, {@code .cvsignore} etc. will be included.
     * This means all files like the following will be included.
     * <ul>
     * <li>Misc: &#42;&#42;/&#42;~, &#42;&#42;/#&#42;#, &#42;&#42;/.#&#42;, &#42;&#42;/%&#42;%, &#42;&#42;/._&#42;</li>
     * <li>CVS: &#42;&#42;/CVS, &#42;&#42;/CVS/&#42;&#42;, &#42;&#42;/.cvsignore</li>
     * <li>RCS: &#42;&#42;/RCS, &#42;&#42;/RCS/&#42;&#42;</li>
     * <li>SCCS: &#42;&#42;/SCCS, &#42;&#42;/SCCS/&#42;&#42;</li>
     * <li>VSSercer: &#42;&#42;/vssver.scc</li>
     * <li>MKS: &#42;&#42;/project.pj</li>
     * <li>SVN: &#42;&#42;/.svn, &#42;&#42;/.svn/&#42;&#42;</li>
     * <li>GNU: &#42;&#42;/.arch-ids, &#42;&#42;/.arch-ids/&#42;&#42;</li>
     * <li>Bazaar: &#42;&#42;/.bzr, &#42;&#42;/.bzr/&#42;&#42;</li>
     * <li>SurroundSCM: &#42;&#42;/.MySCMServerInfo</li>
     * <li>Mac: &#42;&#42;/.DS_Store</li>
     * <li>Serena Dimension: &#42;&#42;/.metadata, &#42;&#42;/.metadata/&#42;&#42;</li>
     * <li>Mercurial: &#42;&#42;/.hg, &#42;&#42;/.hg/&#42;&#42;</li>
     * <li>Git: &#42;&#42;/.git, &#42;&#42;/.git/&#42;&#42;</li>
     * <li>Bitkeeper: &#42;&#42;/BitKeeper, &#42;&#42;/BitKeeper/&#42;&#42;, &#42;&#42;/ChangeSet,
     * &#42;&#42;/ChangeSet/&#42;&#42;</li>
     * <li>Darcs: &#42;&#42;/_darcs, &#42;&#42;/_darcs/&#42;&#42;, &#42;&#42;/.darcsrepo,
     * &#42;&#42;/.darcsrepo/&#42;&#42;&#42;&#42;/-darcs-backup&#42;, &#42;&#42;/.darcs-temp-mail
     * </ul>
     *
     * @see <a href="https://codehaus-plexus.github.io/plexus-utils/apidocs/org/codehaus/plexus/util/AbstractScanner.html#DEFAULTEXCLUDES">DEFAULTEXCLUDES</a>
     */
    @Parameter(defaultValue = "true")
    private boolean addDefaultExcludes;

    /**
     * Specifies whether to attach the jar to the project
     */
    @Parameter(property = "maven.jar.attach", defaultValue = "true")
    protected boolean attach;

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

    private final MavenProject project;
    private final MavenSession session;
    private final Map<String, Archiver> archivers;
    private final MavenProjectHelper projectHelper;

    @Inject
    public ZipMojo(
            MavenProject project,
            MavenSession session,
            Map<String, Archiver> archivers,
            MavenProjectHelper projectHelper) {
        this.project = project;
        this.session = session;
        this.archivers = archivers;
        this.projectHelper = projectHelper;
    }

    /**
     * Return the {@link #project MavenProject}
     *
     * @return the MavenProject.
     */
    protected final MavenProject getProject() {
        return project;
    }

    /**
     * {@inheritDoc}
     */
    protected String getClassifier() {
        return classifier;
    }

    /**
     * {@inheritDoc}
     */
    protected String getType() {
        return "zip";
    }

    /**
     * {@inheritDoc}
     */
    protected File getClassesDirectory() {
        return classesDirectory;
    }

    protected File getJarFile(File basedir, String resultFinalName, String classifier) {
        if (basedir == null) {
            throw new IllegalArgumentException("basedir is not allowed to be null");
        }
        if (resultFinalName == null) {
            throw new IllegalArgumentException("finalName is not allowed to be null");
        }

        String fileName = resultFinalName + (hasClassifier() ? "-" + classifier : "") + ".zip";

        return basedir.toPath().resolve(fileName).toFile();
    }

    /**
     * Generates the JAR.
     * @return The instance of File for the created archive file.
     * @throws MojoExecutionException in case of an error.
     */
    public File createArchive() throws MojoExecutionException {
        File jarFile = getJarFile(outputDirectory, finalName, getClassifier());

        FileSetManager fileSetManager = new FileSetManager();
        FileSet jarContentFileSet = new FileSet();
        jarContentFileSet.setDirectory(getClassesDirectory().getAbsolutePath());
        jarContentFileSet.setIncludes(Arrays.asList(getIncludes()));
        jarContentFileSet.setExcludes(Arrays.asList(getExcludes()));

        String[] includedFiles = fileSetManager.getIncludedFiles(jarContentFileSet);

        MavenArchiver archiver = new MavenArchiver();
        archiver.setCreatedBy("Maveniverse Zippy Plugin", "eu.maveniverse.maven.plugins", "zippy");
        archiver.setArchiver((JarArchiver) archivers.get("jar"));
        archiver.setOutputFile(jarFile);

        MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

        // configure for Reproducible Builds based on outputTimestamp value
        archiver.configureReproducibleBuild(outputTimestamp);

        archive.setForced(forceCreation);

        try {
            File contentDirectory = getClassesDirectory();
            if (!contentDirectory.exists()) {
                if (!forceCreation) {
                    getLog().warn("ZIP will be empty - no content was marked for inclusion!");
                }
            } else {
                archiver.getArchiver().addFileSet(getFileSet(contentDirectory));
            }

            archiver.createArchive(session, project, archive);

            return jarFile;
        } catch (Exception e) {
            // TODO: improve error handling
            throw new MojoExecutionException("Error assembling ZIP", e);
        }
    }

    /**
     * Generates the JAR.
     * @throws MojoExecutionException in case of an error.
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (skipIfEmpty
                && (!getClassesDirectory().exists() || getClassesDirectory().list().length < 1)) {
            getLog().info("Skipping packaging of the " + getType());
        } else {
            File jarFile = createArchive();

            if (attach) {
                if (hasClassifier()) {
                    projectHelper.attachArtifact(getProject(), getType(), getClassifier(), jarFile);
                } else {
                    if (projectHasAlreadySetAnArtifact()) {
                        throw new MojoExecutionException("You have to use a classifier "
                                + "to attach supplemental artifacts to the project instead of replacing them.");
                    }
                    getProject().getArtifact().setFile(jarFile);
                }
            } else {
                getLog().debug("Skipping attachment of the " + getType() + " artifact to the project.");
            }
        }
    }

    private boolean projectHasAlreadySetAnArtifact() {
        if (getProject().getArtifact().getFile() == null) {
            return false;
        }

        return getProject().getArtifact().getFile().isFile();
    }

    /**
     * Return {@code true} in case where the classifier is not {@code null} and contains something else than white spaces.
     *
     * @return {@code true} if the classifier is set.
     */
    protected boolean hasClassifier() {
        return getClassifier() != null && !getClassifier().trim().isEmpty();
    }

    private String[] getIncludes() {
        if (includes != null && includes.length > 0) {
            return includes;
        }
        return DEFAULT_INCLUDES;
    }

    private String[] getExcludes() {
        if (excludes != null && excludes.length > 0) {
            return excludes;
        }
        return DEFAULT_EXCLUDES;
    }

    private org.codehaus.plexus.archiver.FileSet getFileSet(File contentDirectory) {
        DefaultFileSet fileSet = DefaultFileSet.fileSet(contentDirectory)
                .prefixed("")
                .includeExclude(getIncludes(), getExcludes())
                .includeEmptyDirs(true);

        fileSet.setUsingDefaultExcludes(addDefaultExcludes);
        return fileSet;
    }
}
