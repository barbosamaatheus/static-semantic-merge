package entrypointManager;

import soot.Scene;
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
                "seu.pacote.MainClass"
        };

        // Executar o Analisador de Ponto de Entrada do Soot
        soot.Main.main(sootArgs);

        // Criar e obter o grafo de chamadas
        CallGraph callGraph = Scene.v().getCallGraph();

        // Exibir as arestas do grafo de chamadas
        displayCallGraph(callGraph);
    }

    private void displayCallGraph(CallGraph callGraph) {
        // Iterar sobre as arestas do grafo de chamadas
        Iterator<Edge> edges = callGraph.iterator();
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

}