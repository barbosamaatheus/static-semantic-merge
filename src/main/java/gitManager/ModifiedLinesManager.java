package gitManager;

import groovy.lang.Tuple2;
import project.MergeCommit;
import project.Project;
import services.dataCollectors.modifiedLinesCollector.*;
import util.FileManager;
import util.TypeNameHelper;

import java.util.*;

public class ModifiedLinesManager {
    ModifiedLinesCollector modifiedLinesCollector;
    FileManager fileManager;
    ModifiedMethodsHelper modifiedMethodsHelper;

    public ModifiedLinesManager (String dependenciesPath) {
        this.modifiedLinesCollector = new ModifiedLinesCollector(dependenciesPath);
        this.modifiedMethodsHelper = new ModifiedMethodsHelper("diffj.jar", dependenciesPath);
    }

    public List<CollectedMergeMethodData> collectData(Project project, MergeCommit mergeCommit) {
        List<CollectedMergeMethodData> collectedMergeMethodDataList = new ArrayList<>();

        // Get all modified files in the merge commit
        Set<String> allModifiedFiles = this.modifiedLinesCollector.getAllModifiedFiles(project, mergeCommit);

        for (String filePath : allModifiedFiles) {
            System.out.println("Collecting data for file: " + filePath);
            // For each modified file, get the modified methods
            Set<ModifiedMethod> allModifiedMethodsSet = this.modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getSHA());
            Set<ModifiedMethod> leftModifiedMethods = this.modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA());
            Set<ModifiedMethod> rightModifiedMethods = this.modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA());

            // Create a map with all modified methods, with the left and right methods as values
            Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> allModifiedMethods = new HashMap<>();
            for (ModifiedMethod leftMethod : leftModifiedMethods) {
                allModifiedMethods.put(leftMethod.getSignature(), new Tuple2<>(leftMethod, new ModifiedMethod(leftMethod.getSignature())));
            }

            for (ModifiedMethod rightMethod : rightModifiedMethods) {
                if (allModifiedMethods.containsKey(rightMethod.getSignature())) {
                    allModifiedMethods.computeIfPresent(rightMethod.getSignature(), (k, leftTuple) -> new Tuple2<>(leftTuple.getV1(), rightMethod));
                } else {
                    allModifiedMethods.put(rightMethod.getSignature(), new Tuple2<>(new ModifiedMethod(rightMethod.getSignature()), rightMethod));
                }
            }

            // Get the fully qualified class name
            String className = TypeNameHelper.getFullyQualifiedName(project, filePath, mergeCommit.getAncestorSHA());

            // For each modified method, collect the data
            for (ModifiedMethod method : allModifiedMethodsSet) {
                Tuple2<ModifiedMethod, ModifiedMethod> leftAndRightMethods = allModifiedMethods.get(method.getSignature());
                System.out.println("Collecting data for method: " + method.getSignature());
                collectedMergeMethodDataList.add(this.collectMethodData(leftAndRightMethods, method, project, mergeCommit, className));
            }

        }

        System.out.println(project.getName() + " - ModifiedLinesCollector collection finished");
        return collectedMergeMethodDataList;
    }

    private CollectedMergeMethodData collectMethodData(Tuple2<ModifiedMethod, ModifiedMethod> leftAndRightMethods, ModifiedMethod mergeMethod, Project project, MergeCommit mergeCommit, String className) {
        ModifiedMethod leftMethod = leftAndRightMethods.getV1();
        ModifiedMethod rightMethod = leftAndRightMethods.getV2();

        Set<Integer> leftAddedLines = new HashSet<Integer>();
        Set<Integer> leftDeletedLines = new HashSet<Integer>();
        Set<Integer> rightAddedLines = new HashSet<Integer>();
        Set<Integer> rightDeletedLines = new HashSet<Integer>();

        for (ModifiedLine mergeLine : mergeMethod.getModifiedLines()) {
            if (leftMethod.getModifiedLines().contains(mergeLine)) {
                if (mergeLine.getType() == ModifiedLine.ModificationType.Removed) {
                    leftDeletedLines.add(mergeLine.getNumber());
                } else {
                    leftAddedLines.add(mergeLine.getNumber());
                }
            }

            if (rightMethod.getModifiedLines().contains(mergeLine)) {
                if (mergeLine.getType() == ModifiedLine.ModificationType.Removed) {
                    rightDeletedLines.add(mergeLine.getNumber());
                } else {
                    rightAddedLines.add(mergeLine.getNumber());
                }
            }
        }

        CollectedMergeMethodData collectedMergeMethodData = new CollectedMergeMethodData(project, mergeCommit, className, mergeMethod.getSignature(), leftAddedLines, leftDeletedLines, rightAddedLines, rightDeletedLines);
        System.out.println(collectedMergeMethodData.toString());

        return collectedMergeMethodData;
    }
}
