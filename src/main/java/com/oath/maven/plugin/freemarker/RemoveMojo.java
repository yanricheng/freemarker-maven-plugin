// Copyright 2018, Oath Inc.
// Licensed under the terms of the Apache 2.0 license. See the LICENSE file in the project root for terms.

package com.oath.maven.plugin.freemarker;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Mojo(name = "remove", defaultPhase = LifecyclePhase.INITIALIZE)
public class RemoveMojo extends AbstractMojo {

    @Parameter
    private String freeMarkerVersion;

    @Parameter(property = "includeDirs")
    private List<File> includeDirs;

    @Parameter(property = "excludeDirs")
    private List<File> excludeDirs;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojo;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (includeDirs != null && !includeDirs.isEmpty()) {
            for (File file : includeDirs) {
                if (file != null && file.exists() && file.isDirectory()) {
                    try {
                        FileUtils.deleteDirectory(file);
                    } catch (IOException e) {
                        getLog().error(String.format("Failed to delete dir dir: %s,msg:%s", file, e.getMessage()), e);
                    }
                }
                if (file != null && file.exists() && file.isFile()) {
                    file.delete();
                }
            }
        }

    }


}
