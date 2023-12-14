package gitManager;

import project.MergeCommit;

public class CommitManager {

    private String[] args;
    private String base;
    private String[] parents;
    private String head;

    public CommitManager(String[] args) {
        this.args = args;
        this.head = args[0];
        this.parents = new String[]{args[1], args[2]};
        this.base = args[3];
    }

    public MergeCommit buildMergeCommit() {
        return new MergeCommit(this.head, this.parents, this.base);
    }




}
