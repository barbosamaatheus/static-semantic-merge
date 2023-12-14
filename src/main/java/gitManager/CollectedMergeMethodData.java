package gitManager;

import project.MergeCommit;
import project.Project;

import java.util.HashSet;
import java.util.Set;

public class CollectedMergeMethodData {
    private Project project;
    private MergeCommit mergeCommit;
    private String className;
    private String methodSignature;
    private Set<Integer> leftAddedLines;
    private Set<Integer> leftDeletedLines;
    private Set<Integer> rightAddedLines;
    private Set<Integer> rightDeletedLines;

    public CollectedMergeMethodData(Project project, MergeCommit mergeCommit, String className, String methodSignature,
                                    Set<Integer> leftAddedLines, Set<Integer> leftDeletedLines,
                                    Set<Integer> rightAddedLines, Set<Integer> rightDeletedLines){
        this.mergeCommit = mergeCommit;
        this.className = className;
        this.methodSignature = methodSignature;
        this.leftAddedLines = leftAddedLines;
        this.leftDeletedLines = leftDeletedLines;
        this.rightAddedLines = rightAddedLines;
        this.rightDeletedLines = rightDeletedLines;
        this.project = project;
    }

    public void setMergeCommit(MergeCommit mergeCommit){
        this.mergeCommit = mergeCommit;
    }

    public MergeCommit getMergeCommit() {
        return mergeCommit;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setLeftAddedLines(Set<Integer> leftAddedLines) {
        this.leftAddedLines = leftAddedLines;
    }

    public Set<Integer> getLeftAddedLines() {
        return leftAddedLines;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    public Set<Integer> getLeftDeletedLines() {
        return leftDeletedLines;
    }

    public void setLeftDeletedLines(Set<Integer> leftDeletedLines) {
        this.leftDeletedLines = leftDeletedLines;
    }

    public Set<Integer> getRightAddedLines() {
        return rightAddedLines;
    }

    public void setRightDeletedLines(Set<Integer> rightDeletedLines) {
        this.rightDeletedLines = rightDeletedLines;
    }

    public void setRightAddedLines(Set<Integer> rightAddedLines) {
        this.rightAddedLines = rightAddedLines;
    }

    public Set<Integer> getRightDeletedLines() {
        return rightDeletedLines;
    }

    @Override
    public String toString() {
        return "CollectedMergeMethodData{" +
                "project=" + project +
                ", mergeCommit=" + mergeCommit +
                ", className='" + className + '\'' +
                ", methodSignature='" + methodSignature + '\'' +
                ", leftAddedLines=" + leftAddedLines +
                ", leftDeletedLines=" + leftDeletedLines +
                ", rightAddedLines=" + rightAddedLines +
                ", rightDeletedLines=" + rightDeletedLines +
                '}';
    }
}
