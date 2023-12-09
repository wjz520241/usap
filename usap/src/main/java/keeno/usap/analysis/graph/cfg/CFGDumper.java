

package keeno.usap.analysis.graph.cfg;

import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;
import keeno.usap.util.Indexer;
import keeno.usap.util.SimpleIndexer;
import keeno.usap.util.graph.DotAttributes;
import keeno.usap.util.graph.DotDumper;

import java.io.File;
import java.util.stream.Collectors;

public class CFGDumper {

    /**
     * Limits length of file name, otherwise it may exceed the max file name
     * length of the underlying file system.
     */
    private static final int FILENAME_LIMIT = 200;

    /**
     * Dumps the given CFG to .dot file.
     */
    static <N> void dumpDotFile(CFG<N> cfg, File dumpDir) {
        Indexer<N> indexer = new SimpleIndexer<>();
        new DotDumper<N>()
                .setNodeToString(n -> Integer.toString(indexer.getIndex(n)))
                .setNodeLabeler(n -> toLabel(n, cfg))
                .setGlobalNodeAttributes(DotAttributes.of("shape", "box",
                        "style", "filled", "color", "\".3 .2 1.0\""))
                .setEdgeLabeler(e -> {
                    CFGEdge<N> edge = (CFGEdge<N>) e;
                    if (edge.isSwitchCase()) {
                        return edge.getKind() +
                                "\n[case " + edge.getCaseValue() + "]";
                    } else if (edge.isExceptional()) {
                        return edge.getKind() + "\n" +
                                edge.getExceptions()
                                        .stream()
                                        .map(t -> t.getJClass().getSimpleName())
                                        .toList();
                    } else {
                        return edge.getKind().toString();
                    }
                })
                .setEdgeAttributer(e -> {
                    if (((CFGEdge<N>) e).isExceptional()) {
                        return DotAttributes.of("color", "red");
                    } else {
                        return DotAttributes.of();
                    }
                })
                .dump(cfg, new File(dumpDir, toDotFileName(cfg)));
    }

    public static <N> String toLabel(N node, CFG<N> cfg) {
        if (cfg.isEntry(node)) {
            return "Entry" + cfg.getMethod();
        } else if (cfg.isExit(node)) {
            return "Exit" + cfg.getMethod();
        } else {
            return node instanceof Stmt ?
                    ((Stmt) node).getIndex() + ": " + node.toString().replace("\"", "\\\"") :
                    node.toString();
        }
    }

    private static String toDotFileName(CFG<?> cfg) {
        JMethod m = cfg.getMethod();
        String fileName = String.valueOf(m.getDeclaringClass()) + '.' +
                m.getName() + '(' +
                m.getParamTypes()
                        .stream()
                        .map(Type::toString)
                        .collect(Collectors.joining(",")) +
                ')';
        if (fileName.length() > FILENAME_LIMIT) {
            fileName = fileName.substring(0, FILENAME_LIMIT) + "...";
        }
        // escape invalid characters in file name
        fileName = fileName.replaceAll("[\\[\\]<>]", "_") + ".dot";
        return fileName;
    }
}
