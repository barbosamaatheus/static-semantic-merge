package main;

import buildManager.BuildGenerator;
import csvManager.CsvManager;
import entrypointManager.EntrypointManager;
import gitManager.CollectedMergeMethodData;
import gitManager.CommitManager;
import gitManager.MergeManager;
import gitManager.ModifiedLinesManager;
import org.apache.commons.io.FileUtils;
import project.MergeCommit;
import project.Project;
import services.dataCollectors.modifiedLinesCollector.ModifiedMethod;
import services.outputProcessors.GenerateSootInputFilesOutputProcessor;
import services.outputProcessors.soot.RunSootAnalysisOutputProcessor;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class StaticAnalysisMerge {

    private final Arguments args;

    StaticAnalysisMerge(Arguments args){
        this.args = args;
    }

    public void run() {
        //DependenciesManager dependenciesManager = new DependenciesManager();
        MergeManager mergeManager = new MergeManager();
        BuildGenerator buildGenerator = new BuildGenerator(this.args.getGradlePath(), this.args.getMavenPath(), this.args.getTargetProjectRoot());
        CommitManager commitManager = new CommitManager(this.args.getHead(), this.args.getParents(), this.args.getBase());
        Project project = new Project("project", this.args.getTargetProjectRoot());
        ModifiedLinesManager modifiedLinesManager = new ModifiedLinesManager(this.args.getSsmDependenciesPath());
        EntrypointManager entrypointManager = new EntrypointManager(this.args.getSsmDependenciesPath());

        try {
            //dependenciesManager.copyAuxFilesToProject(this.args.getSsmPath());

            MergeCommit mergeCommit = commitManager.buildMergeCommit();

            Process buildGeneration = buildGenerator.generateBuild();
            buildGeneration.waitFor();

            if(buildGeneration.exitValue() != 0) {
                System.out.println("Could not generate a valid build");
                mergeManager.revertCommit(mergeCommit.getLeftSHA());
                return;
            }

            File buildJar = buildGenerator.getBuildJar();

            File dest = new File("files/project/" + mergeCommit.getSHA() + "/original-without-dependencies/merge/build.jar");
            FileUtils.copyFile(buildJar, dest);
            entrypointManager.configureSoot(dest.getPath(), this.args.getClassName());
            List<ModifiedMethod> entrypoints = entrypointManager.run(project, mergeCommit, this.args.getClassName(), this.args.getMainMethod());

            List<CollectedMergeMethodData> collectedMergeMethodDataList = modifiedLinesManager.collectData(project, mergeCommit);
            CsvManager csvManager = new CsvManager();
            csvManager.transformCollectedDataIntoCsv(collectedMergeMethodDataList, entrypoints, ".");
            csvManager.trimSpacesAndSpecialChars(new File("data/results-with-build-information.csv"));


            GenerateSootInputFilesOutputProcessor generateSootInputFilesOutputProcessor = new GenerateSootInputFilesOutputProcessor(this.args.getScriptsPath());
            generateSootInputFilesOutputProcessor.convertToSootScript(".");

            for(CollectedMergeMethodData data : collectedMergeMethodDataList) {
                String path = "files/"+ data.getProject().getName() + "/" + mergeCommit.getSHA() + "/changed-methods/" + data.getClassName() +"/" + data.getMethodSignature();
                path = path.replaceAll(" ", "");
                path = path.replaceAll("[+^?<>|]*", "");
                File left = new File( path + "/left-right-lines.csv");
                File right = new File(path + "/right-left-lines.csv");

                csvManager.trimBlankLines(left);
                csvManager.trimBlankLines(right);
            }


            RunSootAnalysisOutputProcessor runSootAnalysisOutputProcessor = new RunSootAnalysisOutputProcessor(this.args.getSsmDependenciesPath());
            runSootAnalysisOutputProcessor.executeAnalyses(".");

            File results = new File("./data/soot-results.csv");

            if(csvManager.hasConflict(results)){
               // mergeManager.revertCommit(mergeCommit.getLeftSHA());
            }

            //dependenciesManager.deleteAuxFiles(this.args.getSsmPath());

        } catch (IOException | InterruptedException /*| InterruptedException e*/e) {
            e.printStackTrace();
        }
    }
}
