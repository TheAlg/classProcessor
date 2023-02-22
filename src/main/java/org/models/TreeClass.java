package org.models;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.visitor.filter.TypeFilter;
import java.util.List;
import java.util.regex.Pattern;

public class TreeClass {
    String classValue;
    CtClass parsedClass;
    CtModel ctModel;
    Launcher launcher;



    public TreeClass(String clazzValue, String filePath) {
        this.classValue = clazzValue;
        this.launcher = new Launcher();
        this.configLauncher(filePath);
        this.ctModel = launcher.buildModel();
        parsedClass = launcher.parseClass(clazzValue);
    }

    private void configLauncher(String filePath) {
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.addInputResource(filePath);
    }

    public String getName(){
        return this.parsedClass.getSimpleName().replaceAll("Json$", "");
    }

    public String getVisibility(){
        return parsedClass.getVisibility().name().toLowerCase();
    }
    public String getImports(){
       /* for (CtImport ctImport : model.getElements(new TypeFilter<>(CtImport.class))){
            System.out.println(ctImport.toString());
        }*/
        // using regex instead of spoon
        var wrapper = new Object(){ StringBuilder value = new StringBuilder(); };
        Pattern imports = Pattern.compile("import (.*?);");
        imports.matcher(classValue)
                .results()
                .map(mr -> mr.group())
                //write all imports
                .forEach( (imp) -> {
                    wrapper.value.append(imp).append(System.lineSeparator());
                });
        return wrapper.value.toString();
    }

    public String getAttributes(){
        StringBuilder results = new StringBuilder();
        for ( Object att : this.parsedClass.getFields()){
            String attribute = att.toString()
                    .replaceAll("@.*", "")
                    .replaceAll("Json", "");
            results.append("\t").append(attribute.trim() + System.lineSeparator());
            System.out.println(results);
        }
        return results.append(System.lineSeparator()).toString();
    }
    public String getEnums(){
        StringBuilder results = new StringBuilder();
        // get the enum declaration inside the class
        List<CtEnum> myEnum = this.parsedClass.getElements((new TypeFilter<>(CtEnum.class)));
        // print the enum name
        if (myEnum.size()>0 ){
            for (CtEnum ctEnum : myEnum) {
                results.append(ctEnum.toString()
                        .replaceAll("@.*", "")
                        .replaceAll("Json", "")
                        .replaceAll("(?m)^", "\t"))
                        .append(System.lineSeparator());
            }
        }
        return  results.toString();
    }

    public String getConstructors(){
        StringBuilder results = new StringBuilder();
        for( Object cons : parsedClass.getConstructors()){
            String constructor = cons.toString()
                    .replaceAll("@.*", "")
                    .replaceAll("Json", "")
                    .replaceAll("(?m)^", "\t");
            //System.out.println("public " + constructor);
            constructor = constructor.contains("public") ?
                        constructor : "public "+constructor;
            results.append("\t"+constructor + System.lineSeparator());
        }
        return results.toString();
    }

    public String getMethods(){
        StringBuilder results = new StringBuilder();
        for ( Object method : parsedClass.getMethods()){
            String func = method.toString()
                    .replaceAll("@.*", "")
                    .replaceAll("Json", "")
                    .replaceAll("(?m)^", "\t");
            results.append( func + System.lineSeparator());
        }

        return results.toString();
    }
}
