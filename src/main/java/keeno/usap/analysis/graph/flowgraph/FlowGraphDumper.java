

package keeno.usap.analysis.graph.flowgraph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.util.graph.DotAttributes;
import keeno.usap.util.graph.DotDumper;
import keeno.usap.util.graph.Graph;

import java.io.File;

/**
 * Dumper for flow graph.
 */
public class FlowGraphDumper {

    private static final Logger logger = LogManager.getLogger(FlowGraphDumper.class);

    private static final DotDumper<Node> dumper = new DotDumper<Node>()
            .setNodeAttributer(n -> {
                if (n instanceof VarNode) {
                    return DotAttributes.of("shape", "box");
                } else if (n instanceof InstanceFieldNode) {
                    return DotAttributes.of("shape", "box",
                            "style", "rounded", "style", "filled",
                            "fillcolor", "aliceblue");
                } else { // ArrayIndexNode
                    return DotAttributes.of("style", "filled", "fillcolor", "khaki1");
                }
            })
            .setEdgeAttributer(e -> {
                FlowEdge edge = (FlowEdge) e;
                return switch (edge.kind()) {
                    case LOCAL_ASSIGN, CAST -> DotAttributes.of();
                    case THIS_PASSING, PARAMETER_PASSING -> DotAttributes.of("color", "blue");
                    case RETURN -> DotAttributes.of("color", "blue", "style", "dashed");
                    case INSTANCE_STORE, ARRAY_STORE -> DotAttributes.of("color", "red");
                    case INSTANCE_LOAD, ARRAY_LOAD -> DotAttributes.of("color", "red", "style", "dashed");
                    case OTHER -> DotAttributes.of("color", "green3", "style", "dashed");
                    default -> throw new IllegalArgumentException(
                            "Unsupported edge kind: " + edge.kind());
                };
            })
            .setEdgeLabeler(e -> {
                FlowEdge edge = (FlowEdge) e;
                return edge.kind() == FlowKind.OTHER ? e.getClass().getSimpleName() : "";
            });

    public static void dump(Graph<Node> graph, File output) {
        logger.info("Dumping {}", output.getAbsolutePath());
        dumper.dump(graph, output);
    }
}
