package unit;

import groovyjarjarantlr4.v4.misc.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private EntrypointManager entrypointManager;
    private final String CLASSPATH = "src/test/java/assets/build.jar";

    //Eu preciso criar um método que simula o que o gelcallgraph faz so que sem depender daquela classe.
    private static Iterator<Edge> buildCallGraph() {
        SootClass sootClass = Scene.v().loadClassAndSupport("assets.Main");
        SootMethod mainMethod = sootClass.getMethodByName("main");

        // Criar e obter o grafo de chamadas
        CallGraph callGraph = Scene.v().getCallGraph();

        return callGraph.edgesOutOf(mainMethod);
    }


    @BeforeEach
    public void setup() {
        this.entrypointManager = new EntrypointManager();
        this.entrypointManager.configureSoot(this.CLASSPATH);

    }

    @Test
    public void testConfigureSoot() {

        // Verifique se as opções do Soot foram configuradas corretamente
        Options sootOptions = Options.v();
        assertNotNull(sootOptions, "As opções do Soot não devem ser nulas");
        assertEquals("org.example.Main", sootOptions.classes().get(0), "O classpath do Soot deve ser o mesmo que o especificado");

        // Adicione mais asserções conforme necessário para verificar outras configurações do Soot
        // Por exemplo, você pode verificar outras opções específicas do Soot que devem ser definidas corretamente
    }

    @Test
    public void testGetCallGraphFromMain() {


        Iterator<Edge> edges = this.entrypointManager.getCallGraphFromMain();
        assertNotNull(edges);


    }
    @Test
    public void testFindCommonAncestorWithEmptySets() {
        Iterator<Edge> edges = EntrypointManagerTest.buildCallGraph();
        Set<ModifiedMethod> left = new HashSet<>();
        Set<ModifiedMethod> right = new HashSet<>();
        assertThrows(IllegalArgumentException.class, () -> entrypointManager.findCommonAncestor(edges, left, right), "leftChanges and rightChanges cannot be empty");

    }

    @Test
    public void testFindCommonAncestor() {
        Iterator<Edge> edges = this.entrypointManager.getCallGraphFromMain();

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
    public void testFindCommonAncestorEmpty(){
        Iterator<Edge> edges = this.entrypointManager.getCallGraphFromMain();
        Set<ModifiedMethod> left = new HashSet<>();
        Set<ModifiedMethod> right = new HashSet<>();

        String methodName = "<org.example.Main: void l()>";
        ModifiedMethod modifiedMethod = new ModifiedMethod(methodName);
        left.add(modifiedMethod);

        String methodName2 = "<org.example.Main: void r()>";
        ModifiedMethod modifiedMethod2 = new ModifiedMethod(methodName2);
        right.add(modifiedMethod2);


        assertThrows(RuntimeException.class,() ->entrypointManager.findCommonAncestor(edges, left, right), "No common ancestor found." );

    }

    @Test
    public void testFindCommonAncestorForPair() {

    }

    @Test
    public void testCreateAndInvertedDirectedGraph() {
        Iterator<Edge> edges = this.entrypointManager.getCallGraphFromMain();
        Iterator<Edge> edges2 = this.entrypointManager.getCallGraphFromMain();

        DefaultDirectedGraph<ModifiedMethod, DefaultEdge> graph = this.entrypointManager.createAndInvertedDirectedGraph(edges);

        int cont = 0;
        while (edges2.hasNext()){
            cont++;
            edges2.next();
        }

        //System.out.println(cont);
        assertNotNull(graph);
        //queria achar uma forma de contar esses vértices mas não pensei em nenhuma variável que eu poderia acessar pra encontrar esse número
        assertEquals(3, graph.vertexSet().size());
        assertEquals(cont, graph.edgeSet().size());
        //outro tipo de teste que pensei era analisar se o grafo finalizou quando realmente não existia mais vértices mas n pensei como fazer



    }




}
