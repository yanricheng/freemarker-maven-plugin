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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Mojo(name = "replace", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ReplaceMojo extends AbstractMojo {

    /**
     * FreeMarker version string used to build FreeMarker Configuration instance.
     */
    @Parameter
    private String freeMarkerVersion;

    @Parameter(property = "sourceDirectory", defaultValue = "src/main/java/cn/com/servyou/finance/ecs/facade/form/convert")
    private File sourceDirectory;

    @Parameter(property = "replaceMapStrig", defaultValue = "")
    private String replaceMapStrig;

    @Parameter(property = "subfix", defaultValue = ".java")
    private String subfix;


    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojo;


    public void write(File file, OutputStream os) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int length;
            while ((length = fis.read(b)) > 0) {
                os.write(b, 0, length);
            }
        } finally {
            if (os != null) {
                os.close();

            }
            if (fis != null) {
                fis.close();
            }
        }
    }

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

//        if (freeMarkerVersion == null || freeMarkerVersion.length() == 0) {
//            throw new MojoExecutionException("freeMarkerVersion is required");
//        }

        if (!sourceDirectory.exists()) {
            getLog().info(String.format("===> replace sourceDirectory:not exists", sourceDirectory.getAbsolutePath()));
            return;
        }


        if (!sourceDirectory.isDirectory()) {
            sourceDirectory.mkdirs();
            if (!sourceDirectory.isDirectory()) {
                throw new MojoExecutionException("Required directory does not exist: " + sourceDirectory);
            }
        }
        getLog().info(String.format("===> replace sourceDirectory:%s", sourceDirectory.getAbsolutePath()));

        File[] filelist = sourceDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(subfix);
            }
        });

        if (replaceMapStrig != null && !replaceMapStrig.isEmpty()) {
            String[] keyVals = replaceMapStrig.split(",");
            for (File file : filelist) {
                try {
                    String src = read(file);

                    for (String keyVal : keyVals) {
                        String[] kv = keyVal.split("\\|");
                        src = src.replaceAll(kv[0], kv[1]);
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
                    getLog().error(e);
                }
            }
        }
    }


}
