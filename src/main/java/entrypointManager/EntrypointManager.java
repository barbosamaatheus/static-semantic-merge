package entrypointManager;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.Iterator;

public class EntrypointManager {

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
        // Criar e obter o grafo de chamadas
        CallGraph callGraph = Scene.v().getCallGraph();

        // Exibir as arestas do grafo de chamadas
        displayCallGraph(callGraph);
    }

    private void displayCallGraph(CallGraph callGraph) {
        SootClass sootClass = Scene.v().loadClassAndSupport("org.example.Main");
        SootMethod mainMethod = sootClass.getMethodByName("main"); // findMainMethod(sootClass);

        //Iterator<Edge> edges = callGraph.iterator();
        Iterator<Edge> edges = callGraph.edgesInto(mainMethod);

        while (edges.hasNext()) {
            Edge edge = edges.next();
            SootMethod srcMethod = (SootMethod) edge.getSrc();
            SootMethod tgtMethod = (SootMethod) edge.getTgt();

            // Exibir a relação de chamada
            System.out.println("Método de origem: " + srcMethod);
            System.out.println("Método de destino: " + tgtMethod);
            System.out.println("----");
        }
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