package DependenciesManager;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Objects;

public class DependenciesManager {
    private Boolean hasScriptsFolder;
    private Boolean hasDependenciesFolder;

    public DependenciesManager() {
        this.hasDependenciesFolder = false;
        this.hasScriptsFolder = false;
    }

    public void copyAuxFilesToProject(String mergerPath) throws IOException {
        this.copyScriptsToProject(mergerPath);
        this.copySootAndDiffjToProject(mergerPath);
    }

    public void copyScriptsToProject(String mergerPath) throws IOException {
        File scriptsFolder = new File(mergerPath + "/scripts");
        File localFolder = new File(".");
        File[] localScriptsFolder = localFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("scripts");
            }
        });

        assert localScriptsFolder != null;
        if(localScriptsFolder.length == 0) {
            FileUtils.copyDirectory(scriptsFolder, new File("./scripts"));
            this.hasScriptsFolder = false;
        }else{
            this.hasScriptsFolder = true;
            for(File script : Objects.requireNonNull(localScriptsFolder[0].listFiles())) {
                FileUtils.copyFile(script, new File(scriptsFolder + "/" + script.getName()));
            }
        }
    }

    public void copySootAndDiffjToProject(String mergerPath) throws IOException {
        File soot = new File( "./dependencies/soot-analysis-0.2.1-SNAPSHOT-jar-with-dependencies.jar");
        File diffj = new File( "./dependencies/diffj.jar");
        File localFolder = new File(".");
        File[] localDependenciesFolder = localFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("dependencies");
            }
        });

        assert localDependenciesFolder != null;
        this.hasDependenciesFolder = localDependenciesFolder.length != 0;
        FileUtils.copyFile(soot, new File(mergerPath + "./dependencies/" + soot.getName()));
        FileUtils.copyFile(diffj, new File(mergerPath +"./dependencies/" + diffj.getName()));
    }

    public void deleteAuxFiles(String mergerPath) throws IOException {
        if(!this.hasDependenciesFolder) {
            FileUtils.deleteDirectory(new File("./dependencies"));
        }else {
            FileUtils.delete(new File("./dependencies/soot-analysis-0.2.1-SNAPSHOT-jar-with-dependencies.jar"));
            FileUtils.delete(new File("./dependencies/diffj.jar"));
        }

        if(!this.hasScriptsFolder) {
            FileUtils.deleteDirectory(new File("./scripts"));
        }else{
            /*File scriptsFolder = new File(mergerPath + "/scripts");
            for(File script : Objects.requireNonNull(scriptsFolder.listFiles())){
                FileUtils.delete(new File(script.getName()));
            }*/
            FileUtils.delete(new File("./scripts/create_results_csv.py"));
            FileUtils.delete(new File("./scripts/fetch_jars.py"));
            FileUtils.delete(new File("./scripts/parse_to_soot.py"));
            FileUtils.delete(new File("./scripts/fetch_multiple_jar_per_scenario.py"));

        }

        //FileUtils.deleteDirectory(new File("./sootOutput"));
        //FileUtils.deleteDirectory(new File("./data"));
        //FileUtils.deleteDirectory(new File("./files"));
        //FileUtils.delete(new File("./out.txt"));
    }

}
