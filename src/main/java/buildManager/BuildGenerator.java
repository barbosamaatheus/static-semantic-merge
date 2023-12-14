package buildManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class BuildGenerator {

    private String gradlePath;
    private String mavenPath;

    public BuildGenerator(String gradlePath, String mavenPath) {
        this.gradlePath = gradlePath;
        this.mavenPath = mavenPath;
    }

    private String jarPath = "./build/libs";

    public Process generateBuild() throws IOException, InterruptedException {
        System.out.println("==== GENERATING BUILD ====");
        File f = new File("gradlew.bat");
        Process proc = null;
        if(f.exists() && !f.isDirectory()) {
            proc  = Runtime.getRuntime().exec("gradlew.bat build -x test");
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
        File buildJarFolder = new File(this.jarPath);
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
