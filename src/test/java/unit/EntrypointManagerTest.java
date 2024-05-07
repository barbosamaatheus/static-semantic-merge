package unit;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.MergeCommit;
import project.Project;
import services.dataCollectors.modifiedLinesCollector.ModifiedMethod;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import entrypointManager.EntrypointManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class EntrypointManagerTest {

    private final String CLASSPATH = "src/test/java/assets/build.jar";
    private EntrypointManager entrypointManager;

    private static Iterator<Edge> buildCallGraph() {
        SootClass sootClass = Scene.v().loadClassAndSupport("org.example.Main");
        SootMethod mainMethod = sootClass.getMethodByName("main");

        // Criar e obter o grafo de chamadas
        CallGraph callGraph = Scene.v().getCallGraph();

        return callGraph.edgesOutOf(mainMethod);
    }


    @BeforeEach
    public void setup() {
        this.entrypointManager = new EntrypointManager("D:/Documents/development/UFPE/SSM/static-semantic-merge/dependencies/");
        this.entrypointManager.configureSoot(this.CLASSPATH);
    }

    @Test
    public void testConfigureSoot() {

        Options sootOptions = Options.v();
        assertNotNull(sootOptions, "As opções do Soot não devem ser nulas");
        assertEquals("org.example.Main", sootOptions.classes().get(0), "O classpath do Soot deve ser o mesmo que o especificado");
    }

    @Test
    public void testGetCallGraphFromMain() {
        Iterator<Edge> edges = buildCallGraph();
        assertNotNull(edges);
    }

    @Test
    public void testFindCommonAncestorWithEmptySets() {
        Iterator<Edge> edges = buildCallGraph();
        Set<ModifiedMethod> left = new HashSet<>();
        Set<ModifiedMethod> right = new HashSet<>();
        assertThrows(IllegalArgumentException.class, () -> entrypointManager.findCommonAncestor(edges, left, right), "leftChanges and rightChanges cannot be empty");
    }

    @Test
    public void testFindCommonAncestor() {
        Iterator<Edge> edges = buildCallGraph();

        Set<ModifiedMethod> left = new HashSet<>();
        Set<ModifiedMethod> right = new HashSet<>();

        String methodName = "<org.example.Main: void l()>";
        ModifiedMethod modifiedMethod = new ModifiedMethod(methodName);
        left.add(modifiedMethod);

        String methodName2 = "<org.example.Main: void r2()>";
        ModifiedMethod modifiedMethod2 = new ModifiedMethod(methodName2);
        right.add(modifiedMethod2);

        List<ModifiedMethod> commonAncestors = this.entrypointManager.findCommonAncestor(edges, left, right);

        assertNotNull(commonAncestors);
        assertEquals("<org.example.Main: void main(java.lang.String[])>", commonAncestors.get(0).getSignature());
    }

    @Test
    public void testFindCommonAncestorEmpty() {
        Iterator<Edge> edges = buildCallGraph();
        Set<ModifiedMethod> left = new HashSet<>();
        Set<ModifiedMethod> right = new HashSet<>();

        String methodName = "<org.example.Main: void l()>";
        ModifiedMethod modifiedMethod = new ModifiedMethod(methodName);
        left.add(modifiedMethod);

        String methodName2 = "<org.example.Main: void r()>";
        ModifiedMethod modifiedMethod2 = new ModifiedMethod(methodName2);
        right.add(modifiedMethod2);

        assertThrows(RuntimeException.class, () -> entrypointManager.findCommonAncestor(edges, left, right), "No common ancestor found.");
    }

    @Test
    public void testFindCommonAncestorForPairEmpty() {
        Iterator<Edge> edges = buildCallGraph();
        DefaultDirectedGraph<ModifiedMethod, DefaultEdge> graph = this.entrypointManager.createAndInvertedDirectedGraph(edges);
        ModifiedMethod left = new ModifiedMethod("<org.example.Main: void l()>");
        ModifiedMethod right = new ModifiedMethod("<org.example.Main: void r()>");

        ModifiedMethod lcaAlgorithm = this.entrypointManager.findCommonAncestorForPair(graph, left, right);

        assertNull(lcaAlgorithm);
    }

    @Test
    public void testFindCommonAncestorForPair() {
        Iterator<Edge> edges = buildCallGraph();
        DefaultDirectedGraph<ModifiedMethod, DefaultEdge> graph = this.entrypointManager.createAndInvertedDirectedGraph(edges);
        ModifiedMethod left = new ModifiedMethod("<org.example.Main: void l()>");
        ModifiedMethod right = new ModifiedMethod("<org.example.Main: void r2()>");

        ModifiedMethod lcaAlgorithm = this.entrypointManager.findCommonAncestorForPair(graph, left, right);

        assertNotNull(lcaAlgorithm);
        assertEquals("<org.example.Main: void main(java.lang.String[])>", lcaAlgorithm.getSignature());
    }

    /**
     * This test is for running locally only. It should not be run together with others (keep @Ignore).
     * To run locally, remove @Ignore and adjust the project paths and MergeCommit commits.
     */
    @Disabled
    @Test
    public void testEntrypointManagerRun() {
        Project project = new Project("project", "D:/Documents/development/UFPE/SSM/Teste/");
        MergeCommit mergeCommit = new MergeCommit("2199900a069e7bb82654193f001de183e2dfb99b",
                new String[]{"f051b15e85f4d9db61c9c1f87fd2a50e8182081a",
                        "fc789b8bc7d26a4ce9ded885cf68dd9f9567f3bb"},
                "725d6b39edf282e1ab2922b11a66f1c091381ffe");
        List<ModifiedMethod> entrypoints = entrypointManager.run(project, mergeCommit);
        assertEquals(1, entrypoints.size());
    }

}
