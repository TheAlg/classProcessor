package org.models;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.Scanner;
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
        var wrapper = new Object(){ String value = ""; };
        Pattern imports = Pattern.compile("import (.*?);");
        imports.matcher(classValue)
                .results()
                .map(mr -> mr.group())
                //write all imports
                .forEach( (imp) -> wrapper.value += imp + System.lineSeparator());
        return wrapper.value;
    }

    public String getAttributes(){
        String results = "";
        for ( Object att : this.parsedClass.getFields()){
            String attribute = att.toString()
                    .replaceAll("@.*", "")
                    .replaceAll("Json", "");
            //System.out.println(attribute);
            results += attribute;
        }
        return results + System.lineSeparator();
    }
    public String getEnums(){
        String results = "";
        // get the enum declaration inside the class
        List<CtEnum> myEnum = this.parsedClass.getElements((new TypeFilter<>(CtEnum.class)));
        // print the enum name
        if (myEnum.size()>0 ){
            for (CtEnum ctEnum : myEnum) {
                results += ctEnum.toString()
                        .replaceAll("@.*", "")
                        .replaceAll("Json", "")
                        +System.lineSeparator();
            }
        }
        return results;
    }

    public String getConstructors(){
        String results = "";
        for( Object cons : parsedClass.getConstructors()){
            String constructor = cons.toString()
                    .replaceAll("@.*", "")
                    .replaceAll("Json", "");
            //System.out.println("public " + constructor);
            constructor = constructor.contains("public") ?
                        constructor : "public "+constructor;
            results += constructor;
        }
        return results;
    }

    public String getMethods(){
        String results = "";
        for ( Object method : parsedClass.getMethods()){
            String func = method.toString()
                    .replaceAll("@.*", "")
                    .replaceAll("Json", "");
            results += func + System.lineSeparator();
        }

        return results;
    }
}
