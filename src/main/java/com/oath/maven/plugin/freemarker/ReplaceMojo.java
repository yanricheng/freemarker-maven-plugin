// Copyright 2018, Oath Inc.
// Licensed under the terms of the Apache 2.0 license. See the LICENSE file in the project root for terms.

package com.oath.maven.plugin.freemarker;

import com.google.gson.reflect.TypeToken;
import com.oath.maven.plugin.freemarker.replace.ReplaceDes;
import com.oath.maven.plugin.freemarker.replace.Replacement;
import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Mojo(name = "replace", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ReplaceMojo extends AbstractMojo {

    @Parameter(property = "replaceJsonConfig")
    protected File replaceJsonConfig;
    @Parameter(defaultValue = "${project.baseDir}")
    protected File projectDirectory;
    @Parameter(property = "suffix", defaultValue = ".java")
    private String suffix;
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;
    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojo;

    public String read(File file) throws IOException {
        if (file.isFile()) {
            BufferedReader bufferedReader = null;
            FileReader fileReader = null;
            StringBuilder sbd = new StringBuilder();
            try {
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    sbd.append(line).append("\n");
                    line = bufferedReader.readLine();

                }
            } finally {
                fileReader.close();
                bufferedReader.close();
            }
            return sbd.toString();
        }
        return null;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!replaceJsonConfig.exists()) {
            getLog().info(String.format("===> replace sourceDirectory:not exists", replaceJsonConfig.getAbsolutePath()));
            return;
        }

        if (projectDirectory == null) {
            projectDirectory = session.getCurrentProject().getBasedir();
        }

        String json = null;
        try {
            json = FileUtils.readFileToString(replaceJsonConfig, StandardCharsets.UTF_8);
        } catch (IOException e) {
            getLog().error(e);
        }

        List<ReplaceDes> replaceDesList = JsonUtil.fromJson(json, new TypeToken<List<ReplaceDes>>() {
        }.getType());

        for (ReplaceDes r : replaceDesList) {
            File sourceDirectory = new File(projectDirectory, r.getSourceFile());
            getLog().info(String.format("===> replace sourceDirectory:%s", sourceDirectory.getAbsolutePath()));

            File[] filelist = sourceDirectory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(suffix);
                }
            });

            for (File file : filelist) {
                try {
                    String src = read(file);
                    for (Replacement replacement : r.getReplacemens()) {
                        src = src.replaceAll(replacement.getSource(), replacement.getTarget());
                    }

                    String path = file.getAbsolutePath();
                    file.delete();
                    File f = new File(path);
                    if (!f.exists()) {
                        f.createNewFile();
                    }
                    f.setExecutable(true);
                    f.setReadable(true);
                    f.setWritable(true);

                    byte[] bytes = src.getBytes(StandardCharsets.UTF_8);
                    OutputStream out = new FileOutputStream(f);
                    out.write(bytes, 0, bytes.length);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    getLog().error(e.getMessage(), e);
                }
            }
        }
    }
}
