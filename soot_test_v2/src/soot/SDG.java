package soot;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphEdge;
import soot.util.dot.DotGraphNode;

public class SDG {

	private LinkedHashMap<HashMutablePDG, List<SdgEdge>> connections;
	private LinkedHashMap<String, Unit> defUnitMappings;

	public SDG() {
		connections = new LinkedHashMap<HashMutablePDG, List<SdgEdge>>();
		defUnitMappings = new LinkedHashMap<String, Unit>();
	}

	public LinkedHashMap<HashMutablePDG, List<SdgEdge>> getConnections() {
		return connections;
	}

	public void setConnections(LinkedHashMap<HashMutablePDG, List<SdgEdge>> connections) {
		this.connections = connections;
	}

	public LinkedHashMap<String, Unit> getDefUnitMappings() {
		return defUnitMappings;
	}

	public void setDefUnitMappings(LinkedHashMap<String, Unit> defUnitMappings) {
		this.defUnitMappings = defUnitMappings;
	}

	public DotGraph toDotGraph() {
		// DotNamer namer = new DotNamer(1000, 0.7f);
		DotGraph dot = new DotGraph("System Dependence Graph");
		dot.setGraphAttribute("compound", "true");
		int j = 0;
		for (HashMutablePDG pdg : connections.keySet()) {
			SootMethod method = pdg.getCFG().getBody().getMethod();
			String name = method.getSignature();
			DotGraph subGraph = dot.createSubGraph("cluster_" + pdg.hashCode());
			subGraph.setGraphLabel(name);
			subGraph.setGraphAttribute("fontsize", "40");
			subGraph.setGraphAttribute("fontcolor", "blue");

			PDGNode startNode = pdg.GetStartNode();
			Queue<PDGNode> worklist = new LinkedList<PDGNode>();
			worklist.add(startNode);

			DotGraph startSubGraph = subGraph.createSubGraph("cluster_" + startNode.hashCode());
			startSubGraph.setGraphLabel("");
			// DotGraphNode succDotnode =
			// subGraph.drawNode(String.valueOf(succ.hashCode()));
			if (startNode.getType() == PDGNode.Type.REGION) {
				startSubGraph.setGraphAttribute("color", "grey");
			}

			// Add node instead of label of cluster, and use this to add edges between the
			// cfgSubGraph clusters
			DotGraphNode startLabelNode = startSubGraph.drawNode("StartNode" + startNode.hashCode());
			startLabelNode.setLabel(startNode.toShortString());
			startLabelNode.setAttribute("shape", "plaintext");

			if (startNode.getNode() instanceof Block) {
				Block bl = (Block) startNode.getNode();
				Iterator<Unit> it = bl.iterator();
				while (it.hasNext()) {
					Unit u = it.next();
					DotGraphNode uNode = startSubGraph.drawNode(String.valueOf(u.hashCode()));
					uNode.setLabel(u.toString());
				}
			}

			//
			// DotGraphNode dotnode =
			// subGraph.drawNode(String.valueOf(startNode.hashCode()));
			// dotnode.setLabel(startNode.toString().replaceAll("\\r", ""));
			//
			// if (startNode.getType() == PDGNode.Type.REGION) {
			// dotnode.setStyle(DotGraphConstants.NODE_STYLE_FILLED);
			// }
			Set<PDGNode> visited = new HashSet<PDGNode>();

			while (!worklist.isEmpty()) {
				PDGNode node = worklist.poll();
				visited.add(node);

				for (PDGNode succ : node.getDependets()) {

					if (!visited.contains(succ)) {
						DotGraph cfgSubGraph = subGraph.createSubGraph("cluster_" + succ.hashCode());
						cfgSubGraph.setGraphLabel("");
						// DotGraphNode succDotnode =
						// subGraph.drawNode(String.valueOf(succ.hashCode()));
						if (succ.getType() == PDGNode.Type.REGION) {
							cfgSubGraph.setGraphAttribute("color", "grey");
						}

						// Add node instead of label of cluster, and use this to add edges between the
						// cfgSubGraph clusters
						DotGraphNode labelNode = cfgSubGraph.drawNode("StartNode" + succ.hashCode());
						labelNode.setLabel(succ.toShortString());
						labelNode.setAttribute("shape", "plaintext");

						if (succ.getNode() instanceof Block) {
							Block bl = (Block) succ.getNode();
							Iterator<Unit> it = bl.iterator();
							while (it.hasNext()) {
								Unit u = it.next();
								DotGraphNode uNode = cfgSubGraph.drawNode(String.valueOf(u.hashCode()));
								uNode.setLabel(u.toString());

								// Add data dependency edges
								List<ValueBox> useValueBoxes = u.getUseBoxes();
								if (useValueBoxes != null) {
									for (ValueBox vb : useValueBoxes) {
										Value v = vb.getValue();
										if (v instanceof JimpleLocal) {
											String varName = ((JimpleLocal) v).getName();
											Unit edgeToUnit = defUnitMappings.get(name + varName);
											if (edgeToUnit != null)
												subGraph.drawEdge(String.valueOf(edgeToUnit.hashCode()),
														String.valueOf(u.hashCode())).setLabel("D_" + varName);
											;
										}
									}
								}
							}
						}

						// succDotnode.setLabel(succ.toString().replaceAll("\\r", ""));
						worklist.add(succ);
					}
					// Add control flow edge
					DotGraphEdge edge = subGraph.drawEdge("StartNode" + node.hashCode(), "StartNode" + succ.hashCode());
					edge.setLabel("c");
					edge.setAttribute("ltail", "cluster_" + node.hashCode());
					edge.setAttribute("lhead", "cluster_" + succ.hashCode());
				}
			}
		}
		for (List<SdgEdge> edges : connections.values()) {
			for (SdgEdge edge : edges) {
				if (edge.type == EdgeType.RET) {
					DotGraphEdge dotEdge = dot.drawEdge(String.valueOf(edge.from.hashCode()),
							String.valueOf(edge.to.hashCode()));
					dotEdge.setLabel(edge.type.toString());

					// dotEdge.setAttribute("ltail", "cluster_"+edge.pdgFrom.hashCode()); //Head of
					// edge should point to SDG
				} else {
					DotGraphEdge dotEdge = dot.drawEdge(String.valueOf(edge.from.hashCode()),
							"StartNode" + edge.pdgTo.GetStartNode().hashCode());
					dotEdge.setAttribute("lhead", "cluster_" + edge.pdgTo.hashCode()); // Head of edge should point to
																						// SDG
					dotEdge.setLabel(edge.type.toString());

				}
			}
		}

		return dot;
	}
}
