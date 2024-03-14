package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.dataCollectors.modifiedLinesCollector.ModifiedMethod;
import soot.Scene;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import entrypointManager.EntrypointManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class EntrypointManagerTest {

    private EntrypointManager entrypointManager;
    private final String CLASSPATH = "src/test/java/assets/build.jar";

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
        Iterator<Edge> edges = this.entrypointManager.getCallGraphFromMain();
        Set<ModifiedMethod> left = new HashSet<>();
        Set<ModifiedMethod> right = new HashSet<>();
        //List<ModifiedMethod> modified = this.entrypointManager.findCommonAncestor(edges, left, right);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            entrypointManager.findCommonAncestor(edges, left, right);
        });
        assertEquals("leftChanges and rightChanges cannot be empty", exception.getMessage());
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
//O esperado pra essa lista seria que retornasse o método main? To tentando pensar em uma forma sem depender dos resultados do mining
        assertNotNull(commonAncestors);
        assertEquals("org.example.Main: void main(java.lang.String)", commonAncestors.get(0));

    }

    @Test
    public void testFindCommonAncestorEmpty(){
        Iterator<Edge> edges = this.entrypointManager.getCallGraphFromMain();
        Set<ModifiedMethod> left = new HashSet<>();
        Set<ModifiedMethod> right = new HashSet<>();

        String methodName = "<org.example.Main: void l()>";
        ModifiedMethod modifiedMethod = new ModifiedMethod(methodName);
        left.add(modifiedMethod);

        String methodName2 = "<org.example.Main: void r2()>";
        ModifiedMethod modifiedMethod2 = new ModifiedMethod(methodName2);
        right.add(modifiedMethod2);

        List<ModifiedMethod> commonAncestors = new ArrayList<>();
        RuntimeException exception = assertThrows(RuntimeException.class,() ->{entrypointManager.findCommonAncestor(edges, left, right);});


    }
}
