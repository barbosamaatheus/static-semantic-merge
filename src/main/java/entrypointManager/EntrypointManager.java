package entrypointManager;

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
        Iterator<Edge> edges = getCallGraphFromMain("main");
        displayCallGraph(edges);
        Set<String> mutuallyModifiedFiles = this.modifiedLinesCollector.getFilesModifiedByBothParents(project, mergeCommit);

        Set<ModifiedMethod> left = new HashSet<>();
        Set<ModifiedMethod> right = new HashSet<>();
        for (String filePath : mutuallyModifiedFiles) {
             left.addAll(this.modifiedMethodsHelper.getAllModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA()));
             right.addAll(this.modifiedMethodsHelper.getAllModifiedMethods(project, filePath,  mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA()));
        }

        //findCommonAncestor(edges, left, right);
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

    private Iterator<Edge> getCallGraphFromMain(String mainClass){

        SootClass sootClass = Scene.v().loadClassAndSupport("org.example.Main");
        SootMethod mainMethod = sootClass.getMethodByName(mainClass); //main // findMainMethod(sootClass);

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
    private List<ModifiedMethod> findCommonAncestor(Iterator<Edge> edges, Set<ModifiedMethod> leftChanges, Set<ModifiedMethod> rightChanges) {
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
    private ModifiedMethod findCommonAncestorForPair(Iterator<Edge> edges, ModifiedMethod leftMethod, ModifiedMethod rightMethod) {
        return null;
    }

    private void displayCallGraph(Iterator<Edge> edges) {

        StringBuilder dotCode = new StringBuilder();
        dotCode.append("digraph CallGraph {\n");

        while (edges.hasNext()) {
            Edge edge = edges.next();
            SootMethod srcMethod = (SootMethod) edge.getSrc();
            SootMethod tgtMethod = (SootMethod) edge.getTgt();
            dotCode.append("  \"").append(srcMethod).append("\" -> \"").append(tgtMethod).append("\";\n");
            // Exibir a relação de chamada
            //System.out.println("Método de origem: " + srcMethod);
            //System.out.println("Método de destino: " + tgtMethod);
            //System.out.println("----");
        }

        dotCode.append("}");
        System.out.println(dotCode.toString());
        System.out.println("----");


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