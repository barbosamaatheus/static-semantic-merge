package entrypointManager;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.LowestCommonAncestorAlgorithm;
import org.jgrapht.alg.lca.NaiveLCAFinder;
import org.jgrapht.alg.lca.TarjanLCAFinder;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import project.MergeCommit;
import project.Project;
import services.dataCollectors.modifiedLinesCollector.ModifiedLinesCollector;
import services.dataCollectors.modifiedLinesCollector.ModifiedMethod;
import services.dataCollectors.modifiedLinesCollector.ModifiedMethodsHelper;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.*;

public class EntrypointManager {

    ModifiedLinesCollector modifiedLinesCollector;

    ModifiedMethodsHelper modifiedMethodsHelper;

    public EntrypointManager () {
        this.modifiedLinesCollector = new ModifiedLinesCollector();
        this.modifiedMethodsHelper = new ModifiedMethodsHelper("diffj.jar");
    }

    public void run(Project project,  MergeCommit mergeCommit){
        Iterator<Edge> edges = getCallGraphFromMain();
        //displayCallGraph(edges);
        Set<String> mutuallyModifiedFiles = this.modifiedLinesCollector.getFilesModifiedByBothParents(project, mergeCommit);

        Set<ModifiedMethod> left = new HashSet<>();
        Set<ModifiedMethod> right = new HashSet<>();
        for (String filePath : mutuallyModifiedFiles) {
             left.addAll(this.modifiedMethodsHelper.getAllModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA()));
             right.addAll(this.modifiedMethodsHelper.getAllModifiedMethods(project, filePath,  mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA()));
        }

        findCommonAncestor(edges, left, right);
    }

    public void configureSoot(String classpath) {
        // Configurar as opções do Soot
        String[] sootArgs = {
                "-cp", classpath,
                "-pp",
                "-f", "J",
                "-w",
                "--allow-phantom-refs",
                "-p", "jb", "use-original-names:true",
                "-p", "cg.spark", "enabled:true",
                "org.example.Main"
        };

        // Executar o Analisador de Ponto de Entrada do Soot
        soot.Main.main(sootArgs);
        Scene.v().loadNecessaryClasses();

    }

    public Iterator<Edge> getCallGraphFromMain(){

        SootClass sootClass = Scene.v().loadClassAndSupport("org.example.Main");
        SootMethod mainMethod = sootClass.getMethodByName("main"); //main // findMainMethod(sootClass);

        // Criar e obter o grafo de chamadas
        CallGraph callGraph = Scene.v().getCallGraph();


        return callGraph.edgesOutOf(mainMethod);

    };

    /**
     * Método para aplicar o algoritmo de busca do ancestral comum mais recente entre as alterações de left e right.

     * Se left e right estiverem vazios, lançar uma exceção.
     * Se left e right tiverem apenas um elemento, buscar o ancestral comum mais recente apenas desse par.
     * Se left ou right tiverem mais de um elemento, buscar o ancestral comum mais recente para todas as combinações de pares.
     * Se nenhum acestral for encontrado, retonra uma exceção.

     * @param edges         Iterador de arestas do grafo de chamadas.
     * @param leftChanges   Conjunto de métodos modificados no lado esquerdo.
     * @param rightChanges  Conjunto de métodos modificados no lado direito.
     * @return O ancestral comum mais recente ou null se nenhum for encontrado.
     * @throws IllegalArgumentException Se leftChanges ou rightChanges estiverem vazios.
     * @throws RuntimeException         Se nenhum ancestral comum for encontrado.
     */
    public List<ModifiedMethod> findCommonAncestor(Iterator<Edge> edges, Set<ModifiedMethod> leftChanges, Set<ModifiedMethod> rightChanges) {
        if (leftChanges.isEmpty() || rightChanges.isEmpty()) {
            throw new IllegalArgumentException("leftChanges and rightChanges cannot be empty");
        }

        List<ModifiedMethod> commonAncestors = new ArrayList<>();

        for (ModifiedMethod leftMethod : leftChanges) {
            for (ModifiedMethod rightMethod : rightChanges) {
                ModifiedMethod ancestorsForPair = findCommonAncestorForPair(edges, leftMethod, rightMethod);
                commonAncestors.add(ancestorsForPair);
            }
        }

        if (commonAncestors.isEmpty()) {
            throw new RuntimeException("No common ancestor found.");
        }

        return commonAncestors;
    }

    /**
     * Método para encontrar o ancestral comum mais recente para um par de modificações.
     *
     * @param edges        Iterador de arestas do grafo de chamadas.
     * @param leftMethod   Método modificado do lado esquerdo do par.
     * @param rightMethod  Método modificado do lado direito do par.
     * @return O ancestral comum mais recente ou null se nenhum for encontrado.
     */
    public ModifiedMethod findCommonAncestorForPair(Iterator<Edge> edges, ModifiedMethod leftMethod, ModifiedMethod rightMethod) {
        DefaultDirectedGraph<ModifiedMethod, DefaultEdge> invertedGraph = createAndInvertedDirectedGraph(edges);

        LowestCommonAncestorAlgorithm<ModifiedMethod> lcaAlgorithm = new NaiveLCAFinder<>(invertedGraph);

        //ModifiedMethod leftModifiedMethod = new ModifiedMethod("<org.example.Main: void l()>");
        //ModifiedMethod rightModifiedMethod = new ModifiedMethod("<org.example.Main: void r2()>");

        ModifiedMethod lca = lcaAlgorithm.getLCA(leftMethod, rightMethod);
        System.out.println(lca);
        return null;

       // return lcaAlgorithm.getLCA(leftMethod, rightMethod);
    }

    private static DefaultDirectedGraph<ModifiedMethod, DefaultEdge> createAndInvertedDirectedGraph(Iterator<Edge> edges) {
        // Criar o grafo direcionado
        DefaultDirectedGraph<ModifiedMethod, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        // Adicionar os métodos e as arestas ao grafo
        while (edges.hasNext()) {
            Edge edge = edges.next();
            SootMethod src = (SootMethod) edge.getSrc();
            SootMethod tgt = (SootMethod) edge.getTgt();

            ModifiedMethod sctModifiedMethod = new ModifiedMethod(src.getSignature());
            ModifiedMethod tgtModifiedMethod = new ModifiedMethod(tgt.getSignature());
            graph.addVertex(sctModifiedMethod);
            graph.addVertex(tgtModifiedMethod);
            graph.addEdge(sctModifiedMethod, tgtModifiedMethod);
        }

        DefaultDirectedGraph<ModifiedMethod, DefaultEdge> invertedGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        convertToDotGraph(graph);
        Graphs.addGraphReversed(invertedGraph, graph);
        convertToDotGraph(invertedGraph);

        return invertedGraph;
    }

    private static void convertToDotGraph(Graph<ModifiedMethod, DefaultEdge> graph) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph {\n");

        // Adicionar vértices
        for (ModifiedMethod vertex : graph.vertexSet()) {
            dot.append("\t").append(vertex.getSignature()).append(";\n");
        }

        // Adicionar arestas
        for (DefaultEdge  edge : graph.edgeSet()) {
            ModifiedMethod source = graph.getEdgeSource(edge);
            ModifiedMethod target = graph.getEdgeTarget(edge);
            dot.append("\t").append(source.getSignature()).append(" -> ").append(target.getSignature()).append(";\n");
        }

        dot.append("}");

        System.out.println(dot.toString());
    }

    private static SootMethod findMainMethod(SootClass sootClass) {
        for (SootMethod method : sootClass.getMethods()) {
            if (isMainMethod(method)) {
                return method;
            }
        }
        return null;
    }

    private static boolean isMainMethod(SootMethod method) {
        return method.getName().equals("main");
    }
}