// Copyright 2018, Oath Inc.
// Licensed under the terms of the Apache 2.0 license. See the LICENSE file in the project root for terms.

package com.oath.maven.plugin.freemarker;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class JsonPropertiesProvider implements OutputGeneratorPropertiesProvider {
    private final File dataDir;
    private final File templateDir;
    private final File outputDir;
    private File basedir;
    private Map<String, Object> baseModole;

    private JsonPropertiesProvider(File dataDir, File templateDir, File outputDir) {
        this.dataDir = dataDir;
        this.templateDir = templateDir;
        this.outputDir = outputDir;
    }

    public static JsonPropertiesProvider create(File dataDir, File templateDir, File outputDir) {
        return new JsonPropertiesProvider(dataDir, templateDir, outputDir);
    }

    public void setBaseModole(Map<String, Object> baseModole) {
        this.baseModole = baseModole;
    }

    public void setBasedir(File basedir) {
        this.basedir = basedir;
    }

    @Override
    public void providePropertiesFromFile(Path path, OutputGenerator.OutputGeneratorBuilder builder) {
        File jsonDataFile = path.toFile();
        Map<String, Object> data = JsonUtil.parseJson(jsonDataFile);

        Object obj = data.get("dataModel");
        if (obj != null) {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.putAll(baseModole);
            dataModel.putAll((Map<String, Object>) obj);
            builder.addDataModel(dataModel);
        } else {
            builder.addDataModel(baseModole);
        }

        obj = data.get("templateName");
        if (obj == null) {
            throw new RuntimeException("Require json data property not found: templateName");
        }
        builder.addTemplateLocation(templateDir.toPath().resolve(obj.toString()));

        String dataDirName = dataDir.getAbsolutePath();
        String jsonFileName = jsonDataFile.getAbsolutePath();
        if (!jsonFileName.startsWith(dataDirName)) {
            throw new IllegalStateException("visitFile() given file not in sourceDirectory: " + jsonDataFile);
        }

        String outputFileName = jsonFileName.substring(dataDirName.length() + 1);
        outputFileName = outputFileName.substring(0, outputFileName.length() - 5);

        Object ftlOutputDir = data.get("outputDir");

        Path outputPath = null;
        if (ftlOutputDir != null) {
            String subPath = ftlOutputDir.toString();
            if (subPath.trim().length() > 0) {
                outputPath = new File(basedir.getAbsolutePath() + "/" + subPath.trim()).toPath();
            } else {
                outputPath = basedir.toPath();
            }
        }

        if (outputPath == null) {
            outputPath = outputDir.toPath();
        }
        Path resolved = outputPath.resolve(outputFileName);
        builder.addOutputLocation(resolved);
    }


}
