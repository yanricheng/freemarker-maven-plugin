// Copyright 2018, Oath Inc.
// Licensed under the terms of the Apache 2.0 license. See the LICENSE file in the project root for terms.

package com.oath.maven.plugin.freemarker;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

@Mojo(name = "mkdir", defaultPhase = LifecyclePhase.INITIALIZE)
public class MakeDirMojo extends AbstractMojo {

    @Parameter(property = "includeDirs")
    private List<File> includeDirs;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojo;

    @Parameter
    private String freeMarkerVersion;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File basedir = session.getCurrentProject().getBasedir();
        if (includeDirs != null && !includeDirs.isEmpty()) {
            for (File file : includeDirs) {
                file.mkdirs();
            }
        }

    }


}
