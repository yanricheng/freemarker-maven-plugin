package com.oath.maven.plugin.freemarker.replace;

import com.oath.maven.plugin.freemarker.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class ReplaceDes {
    private String sourceFile;
    private List<Replacement> replacemens;

    public ReplaceDes() {

    }

    public ReplaceDes(String sourceFile, List<Replacement> replacemens) {
        this.sourceFile = sourceFile;
        this.replacemens = replacemens;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public List<Replacement> getReplacemens() {
        return replacemens;
    }

    public void setReplacemens(List<Replacement> replacemens) {
        this.replacemens = replacemens;
    }

    public static void main(String[] args) {
        ReplaceDes r = new ReplaceDes("/src/main", new ArrayList<>());
        Replacement r1 = new Replacement("11", "aa");
        Replacement r2 = new Replacement("22", "bb");
        r.getReplacemens().add(r1);
        r.getReplacemens().add(r2);
        System.out.println(JsonUtil.toJson(r));
    }
}
