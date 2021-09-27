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

@Mojo(name = "copy", defaultPhase = LifecyclePhase.INITIALIZE)
public class CopyMojo extends AbstractMojo {

    @Parameter(property = "sourceDirs")
    private List<File> sourceDirs;

    @Parameter(property = "targetDir")
    private File targetDir;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojo;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (sourceDirs != null && !sourceDirs.isEmpty()) {
            for (File file : sourceDirs) {
                try {
                    FileUtils.copyDirectory(file, targetDir);
                } catch (IOException e) {
                    getLog().error(String.format("Failed to process files in generator src dir: %s,target dir:%s,detail:%s", file.getPath(), targetDir.getPath(), e.getMessage()), e);
                }
            }
        }

    }


}
