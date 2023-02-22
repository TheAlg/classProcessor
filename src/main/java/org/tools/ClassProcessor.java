package org.tools;

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.models.TreeClass;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.filter.TypeFilter;

public class ClassProcessor {

    public static void main(String[] args) throws ParseException {

        System.out.println("Start tools");
        CommandLine cmd;
        String srcDir = "";
        String targetDir = "";
        String packageName = "";


        Options options = new Options();

        options.addOption("s", true, "source directory is required");
        options.addOption("t", true, "target directory is required");
        options.addOption("p", true, "package name is required");


        //Create a parser
        CommandLineParser parser = new DefaultParser();
        //parse the options passed as command line arguments
        try {
            cmd = parser.parse( options, args);
            if  (! cmd.hasOption("s")
                    || ! cmd.hasOption("t")
                    || ! cmd.hasOption("p")){
                System.err.println("All options are required");
                return;
            }

        } catch (ParseException e) {
            System.err.println("All options require arguments");
            return;
        }

        //assigning the values;
        srcDir = cmd.getOptionValue("s");
        targetDir = cmd.getOptionValue("t");
        packageName = cmd.getOptionValue("p");
        // configuring file separator
        String fileSeparator = File.separator;
        if ( !targetDir.substring(targetDir.length() - 1).equals(fileSeparator) ){
            targetDir += fileSeparator;
        }
        if ( !srcDir.substring(srcDir.length() - 1).equals(fileSeparator) ){
            srcDir += fileSeparator;
        }
        //filters
        if ( targetDir.toLowerCase().equals(srcDir.toLowerCase())){
            System.err.println("The source file can not be same as the Target file");
            return;
        }

        File srcFile = new File(srcDir);
        if (! srcFile.exists() || ! srcFile.isDirectory()) {
            System.err.println("The source dir " + srcDir + " is not a valid directory");
            return;
        }
        File targetFile = new File(targetDir);
        if (! targetFile.exists() || ! targetFile.isDirectory()) {
            System.err.println("The target dir " + targetDir + " is not a valid directory");
            return;
        }

        System.out.println("Target directory : "+ targetDir);
        System.out.println("Source directory : "+srcDir);

        File[] contents = srcFile.listFiles();
        System.out.println("processing "+ contents.length + " files");

        for (File file : contents) {
            //check if file exists

            if (!file.isFile()){
                System.out.println("Erreur inconnue, file =" + file.toString());
                return;
            }

            try {
                String clazzValue = "";
                //if the file is compiled, decompile it
                if (FilenameUtils.getExtension(file.getPath()).equals("class")){
                    clazzValue = decompile(file.getAbsolutePath());
                }
                //else just put the content in a variable
                else if (FilenameUtils.getExtension(file.getPath()).equals("java")){
                    clazzValue = getFileValue(file.getAbsolutePath());
                }

                //then we write the file
                writeFile(targetDir, clazzValue, packageName, file.getAbsolutePath());

            } catch (IOException e) {

                throw new RuntimeException(e);
            }
        }

    }


    private static String decompile(String path) {
        String clazzValue = "";
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);

        com.strobel.decompiler.Decompiler.decompile(
                path, new com.strobel.decompiler.PlainTextOutput(writer));
        //put the result in a local variable
        clazzValue = out.toString();

        return clazzValue;
    }
    private static String getFileValue(String path) throws IOException {
        String clazzValue = "";
        FileReader javaFile = new FileReader(path);
        BufferedReader reader = new BufferedReader(javaFile);
        String line = reader.readLine();
        while (line != null) {
            clazzValue += line;
            line = reader.readLine();
        }
        return clazzValue;
    }
    private static void writeFile(String targetDir, String clazzValue, String packageName, String filePath)
            throws FileNotFoundException, UnsupportedEncodingException {

        // our parser object
        TreeClass treeClass = new TreeClass(clazzValue, filePath);

        //create a new file write
        PrintWriter javaWriter = new PrintWriter(targetDir + treeClass.getName() +".java", "UTF-8");

        //write the package name
        javaWriter.println("package "+packageName+";" + System.lineSeparator());

        //write the imports
        javaWriter.println(treeClass.getImports());

        //class signature
        javaWriter.println(treeClass.getVisibility() + " class " + treeClass.getName() + " {");

        //write attributes
        javaWriter.println(treeClass.getAttributes());

        //write enums if any
        javaWriter.println(treeClass.getEnums());

        //write constructors
        javaWriter.println(treeClass.getConstructors());

        //write methods
        javaWriter.println(treeClass.getMethods());

        javaWriter.println("}");
        javaWriter.close();

        System.out.println("class " +treeClass.getName() + " has been successfully written in the given directory");

}
}
