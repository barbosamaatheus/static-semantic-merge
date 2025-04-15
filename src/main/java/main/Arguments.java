package main;

public class Arguments {
    private String hc;
    private String[] pc;
    private String bc;
    private String dp;
    private String tpr;
    private String cn;
    private String m;
    private String gp;
    private String mp;
    private String sp;
    private String[] ep;

    public Arguments(String hc, String[] pc, String bc, String dp, String tpr, String cn, String m, String gp, String mp, String sp, String[] ep) {
        this.hc = hc;
        this.pc = pc;
        this.bc = bc;
        this.dp = dp;
        this.tpr = tpr;
        this.cn = cn;
        this.m = m;
        this.gp = gp;
        this.mp = mp;
        this.sp = sp;
        this.ep = ep;
    }

    // Getters
    public String getHead() { return hc; }
    public String[] getParents() { return pc; }
    public String getBase() { return bc; }
    public String getSsmDependenciesPath() { return dp; }
    public String getTargetProjectRoot() { return tpr; }
    public String getClassName() { return cn; }
    public String getMainMethod() { return m; }
    public String getGradlePath() { return gp; }
    public String getScriptsPath() { return sp; }
    public String getMavenPath() { return mp; }
    public String[] getEntrypoints() { return ep; }
}
