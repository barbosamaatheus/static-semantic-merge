package buildManager;

import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class BuildGenerator {

    private String gradlePath;
    private String mavenPath;
    private String mergePath;

    public BuildGenerator(String gradlePath, String mavenPath, String mergePath) {
        this.gradlePath = gradlePath;
        this.mavenPath = mavenPath;
        this.mergePath = mergePath;
    }

    private String jarPath = "build/libs";

    public Process generateBuild() throws IOException, InterruptedException {
        System.out.println("==== GENERATING BUILD ====");

        // Define the build file for each OS
        String filename = "gradlew.bat";
        String gradleComm = "";
        if (!SystemUtils.IS_OS_WINDOWS) {
            filename = "gradlew";
            gradleComm = "./";
        }
        gradleComm += filename + " build -x test";

        File f = new File(this.mergePath + filename);
        Process proc = null;
        if(f.exists() && !f.isDirectory()) {
            if (SystemUtils.IS_OS_LINUX) {
                // Give execution permission to the build file
                Runtime.getRuntime().exec("chmod +x " + filename, null, new File(this.mergePath));
            }
            proc  = Runtime.getRuntime().exec(gradleComm, null, new File(this.mergePath));
            this.watchProcess(proc);
        }else{
            proc = Runtime.getRuntime().exec(this.mavenPath + "/mvn.cmd clean compile assembly:single");
            this.jarPath = "./target";
            this.watchProcess(proc);
            /*proc = Runtime.getRuntime().exec(this.gradlePath +"/gradle.bat assemble testClasses");
            this.jarPath = "./core/build/libs";
            this.watchProcess(proc);*/

        }

        return proc;
    }

    public File getBuildJar() {
        File buildJarFolder = new File(this.mergePath + this.jarPath);
        File returnFile = null;
        for(File file : buildJarFolder.listFiles()) {
            if(!file.getName().contains("boot") && !this.getFileExtension(file.getName()).equals("war") &&
                    !file.getName().contains("javadoc") && !file.getName().contains("sources") && this.getFileExtension(file.getName()).equals("jar")){
                returnFile = file;
            }
        }
        return returnFile;
    }

    private String getFileExtension(String fullName) {
        String fileName = new File(fullName).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    private void watchProcess(Process proc) throws IOException {
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

        // Read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // Read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }
}
