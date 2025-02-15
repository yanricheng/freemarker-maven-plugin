// Copyright 2018, Oath Inc.
// Licensed under the terms of the Apache 2.0 license. See the LICENSE file in the project root for terms.

package com.oath.maven.plugin.freemarker;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class FreeMarkerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.baseDir}")
    private File basedir;

    @Parameter(defaultValue = "${project.build.sourceDirectory}",
            required = true,
            readonly = true)
    private File projectSourceDirectory;

    @Parameter(defaultValue = "${project.build.testSourceDirectory}",
            required = true,
            readonly = true)
    private File testSourceDirectory;


    @Parameter(
            defaultValue = "${project.build.directory}",
            required = true,
            readonly = true
    )
    private File targetDirectory;

    @Parameter(
            defaultValue = "${project.build.outputDirectory}",
            required = true,
            readonly = true
    )
    private File classesDirectory;

//    @Parameter(
//            defaultValue = "${project.testOutputDirectory}",
//            required = true,
//            readonly = true
//    )
//    private File testOutputDirectory;

    /**
     * FreeMarker version string used to build FreeMarker Configuration instance.
     */
    @Parameter
    private String freeMarkerVersion;
    @Parameter(defaultValue = "src/main/freemarker")
    private File sourceDirectory;
    @Parameter(defaultValue = "src/main/freemarker/baseModole.json")
    private File baseModoleJson;
    @Parameter(defaultValue = "src/main/freemarker/template")
    private File templateDirectory;
    @Parameter(defaultValue = "src/main/freemarker/generator")
    private File generatorDirectory;
    @Parameter(defaultValue = "target/generated-sources/freemarker")
    private File outputDirectory;
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;
    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojo;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (freeMarkerVersion == null || freeMarkerVersion.length() == 0) {
            throw new MojoExecutionException("freeMarkerVersion is required");
        }

        if (!generatorDirectory.isDirectory()) {
            generatorDirectory.mkdirs();
            if (!generatorDirectory.isDirectory()) {
                throw new MojoExecutionException("Required directory does not exist: " + generatorDirectory);
            }
        }

        Configuration config = FactoryUtil.createConfiguration(freeMarkerVersion);
        config.setDefaultEncoding("UTF-8");

        Map<String, Object> baseModle = null;
        if (baseModoleJson != null && baseModoleJson.exists() && baseModoleJson.isFile()) {
            baseModle = JsonUtil.parseJson(baseModoleJson);
        }
        if (baseModle == null) {
            baseModle = new HashMap<>();
        }

        if (basedir == null) {
            basedir = session.getCurrentProject().getBasedir();
        }
        baseModle.put("projectBaseDir", basedir);
        File projectresourceDirectory = new File(basedir, "src/main/resources");
        if (!projectresourceDirectory.exists()) {
            projectresourceDirectory.mkdirs();
        }
        baseModle.put("projectresourceDirectory", projectresourceDirectory);
        baseModle.put("projectSourceDirectory", projectSourceDirectory);
        baseModle.put("projectBuildDirectory", targetDirectory);
        baseModle.put("projectBuildOutputDirectory", classesDirectory);

        if (!templateDirectory.isDirectory()) {
            throw new MojoExecutionException("Required directory does not exist: " + templateDirectory);
        }
        try {
            config.setTemplateLoader(new FileTemplateLoader(templateDirectory));
        } catch (Throwable t) {
            getLog().error("Could not establish file template loader for directory: " + templateDirectory, t);
            throw new MojoExecutionException("Could not establish file template loader for directory: " + templateDirectory);
        }

        File freeMarkerProps = FactoryUtil.createFile(sourceDirectory, "freemarker.properties");
        if (freeMarkerProps.isFile()) {
            Properties configProperties = new Properties();
            try (InputStream is = FactoryUtil.createFileInputStream(freeMarkerProps)) {
                configProperties.load(is);
            } catch (Throwable t) {
                getLog().error("Failed to load " + freeMarkerProps, t);
                throw new MojoExecutionException("Failed to load " + freeMarkerProps);
            }
            try {
                config.setSettings(configProperties);
            } catch (Throwable t) {
                getLog().error("Invalid setting(s) in " + freeMarkerProps, t);
                throw new MojoExecutionException("Invalid setting(s) in " + freeMarkerProps);
            }
        }

        try {

            if ("generate-sources".equals(mojo.getLifecyclePhase())) {
                session.getCurrentProject().addCompileSourceRoot(outputDirectory.toString());
            } else if ("generate-test-sources".equals(mojo.getLifecyclePhase())) {
                session.getCurrentProject().addTestCompileSourceRoot(outputDirectory.toString());
            }

            JsonPropertiesProvider jsonPropertiesProvider = JsonPropertiesProvider.create(generatorDirectory, templateDirectory, outputDirectory);
            jsonPropertiesProvider.setBasedir(basedir);
            jsonPropertiesProvider.setBaseModole(baseModle);

            Map<String, OutputGeneratorPropertiesProvider> extensionToBuilders = new HashMap<>(1);
            extensionToBuilders.put(".json", jsonPropertiesProvider);

            GeneratingFileVisitor fileVisitor = GeneratingFileVisitor.create(config, session, extensionToBuilders);

            Files.walkFileTree(generatorDirectory.toPath(), fileVisitor);
        } catch (Throwable t) {
            getLog().error(String.format("Failed to process files in generator dir: %s,msg:%s", generatorDirectory, t.getMessage()), t);
            throw new MojoExecutionException("Failed to process files in generator dir: " + generatorDirectory, t);
        }
    }
}
