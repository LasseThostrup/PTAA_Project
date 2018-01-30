package soot;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import soot.jimple.internal.JRetStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphConstants;
import soot.util.dot.DotGraphEdge;
import soot.util.dot.DotGraphNode;

public class SDG {

	LinkedHashMap<HashMutablePDG, List<SdgEdge>> connections;
	CallGraph cg;
	Body body;

	public SDG(List<String> process_dirs, String class_, String entryMethod) {
		String classPath = Scene.v().getSootClassPath();

		for (String s : process_dirs)
			classPath += ":" + s + "/bin";

		Scene.v().setSootClassPath(classPath);

		Options.v().no_bodies_for_excluded();

		Options.v().set_whole_program(true);

		Scene.v().addBasicClass("java.lang.Object", 3);

		Scene s = Scene.v();

		SootClass c = s.loadClassAndSupport(class_);

		c.setApplicationClass();

		for (SootClass sc : Scene.v().getClasses()) {
			if (sc.declaresMethodByName("main")) {
				Scene.v().setMainClass(sc);
				break;
			}
		}

		Scene.v().loadNecessaryClasses();

		SootMethod m = c.getMethodByName(entryMethod);

		body = m.retrieveActiveBody();

		CHATransformer.v().transform();

		UnitGraph g = new ExceptionalUnitGraph(body);

		HashMutablePDG pdg = new HashMutablePDG(g);

		cg = s.getCallGraph();

		connections = new LinkedHashMap<>();

		iteratePdg(pdg);
	}

	// Ctor
	// What to pass here? path to bytecode dir?
	public SDG(String entryMethod) {
		// Options.v().set_whole_program(true);

		// Scene.v().addBasicClass("test.MyClass");
		Scene s = Scene.v();
		// s.loadNecessaryClasses();
		SootClass c = s.loadClassAndSupport("test.MyClass");
		c.setApplicationClass();
		Scene.v().setMainClass(c);

		SootMethod m = c.getMethodByName("foo");
		body = m.retrieveActiveBody();

		UnitGraph g = new ExceptionalUnitGraph(body);
		HashMutablePDG pdg = new HashMutablePDG(g);

		CHATransformer.v().transform();

		cg = s.getCallGraph();
		// System.out.println(cg);

		connections = new LinkedHashMap<>();

		iteratePdg(pdg);

		for (Map.Entry<HashMutablePDG, List<SdgEdge>> entry : connections.entrySet()) {
			HashMutablePDG pdgEntry = entry.getKey();
			List<SdgEdge> SdgEdgeList = entry.getValue();
			for (SdgEdge edge : SdgEdgeList) {
				System.out.println(edge);
			}

		}
	}

	private void iteratePdg(HashMutablePDG pdg) {
		// Base case
		if (connections.containsKey(pdg))
			return;

		// Recursive case
		LinkedList<SdgEdge> sdgEdgeList = new LinkedList<SdgEdge>();
		connections.put(pdg, sdgEdgeList);

		Iterator i = pdg.iterator();
		while (i.hasNext()) {
			PDGNode node = (PDGNode) i.next();
			if (node.getNode() instanceof Block) {
				Block bl = (Block) node.getNode();
				Iterator<Unit> it = bl.iterator();
				while (it.hasNext()) {
					Unit u = (Unit) it.next();
					Iterator<Edge> outGoingEdges = cg.edgesOutOf(u);
					while (outGoingEdges.hasNext()) {
						Edge edge = outGoingEdges.next();
						if (!edge.isClinit()) {
							if (!edge.tgt().isConcrete())
								continue;
							Body body = edge.tgt().retrieveActiveBody();
							UnitGraph cfg = new ExceptionalUnitGraph(body);
							HashMutablePDG tempPdg = new HashMutablePDG(cfg);

							// Check if there already is a matching pdg, and take the existing one if it is
							// the case.
							HashMutablePDG existingPdg = getExistingPdg(tempPdg);
							if (existingPdg != null)
								tempPdg = existingPdg;

							if (tempPdg.getNodes().size() > 1) {
								Object toPDGNode = tempPdg.GetStartNode();
								if (toPDGNode instanceof PDGNode) {

									int parameterCount = tempPdg.getCFG().getBody().getMethod().getParameterCount();
									EdgeType edgeType = parameterCount > 0 ? EdgeType.PARAM : EdgeType.CALL;

									sdgEdgeList.add(new SdgEdge(node, (PDGNode) toPDGNode, edgeType, pdg, tempPdg));

									sdgEdgeList.addAll(findReturnEdges(tempPdg, pdg, node));

									iteratePdg(tempPdg);
								}
							}
						}
					}
				}
			}
		}
	}

	/// If there is a match, it returns the already existing pdg. If not match, null
	/// is returned!
	private HashMutablePDG getExistingPdg(HashMutablePDG pdg) {
		HashMutablePDG existingPdg = null;
		for (HashMutablePDG pdgCmp : connections.keySet()) {
			if (pdgCmp.toString().equals(pdg.toString()))
				existingPdg = pdgCmp; // TODO: Not ideal to do such a long string comparison
		}
		return existingPdg;
	}

	private List<SdgEdge> findReturnEdges(HashMutablePDG fromPdg, HashMutablePDG toPdg, PDGNode callNode) {
		List<SdgEdge> sdgEdges = new LinkedList<SdgEdge>();
		Iterator i = fromPdg.iterator();
		while (i.hasNext()) {
			PDGNode node = (PDGNode) i.next();
			if (node.getNode() instanceof Block) {
				Block bl = (Block) node.getNode();
				Iterator<Unit> it = bl.iterator();
				while (it.hasNext()) {
					Unit u = (Unit) it.next();
					if (u instanceof JRetStmt || u instanceof JReturnStmt) {
						System.out.println("Return statement: " + u);
						sdgEdges.add(new SdgEdge(node, callNode, EdgeType.RET, fromPdg, toPdg));
					}
				}
			}
		}
		return sdgEdges;
	}

	public DotGraph toDotGraph() {
		// DotNamer namer = new DotNamer(1000, 0.7f);
		DotGraph dot = new DotGraph("System Dependence Graph");
		int j = 0;
		for (HashMutablePDG pdg : connections.keySet()) {
			SootMethod method = pdg.getCFG().getBody().getMethod();
			String name = method.getSignature();
			DotGraph subGraph = dot.createSubGraph("cluster_" + name);
			subGraph.setGraphLabel(name);
			subGraph.setGraphAttribute("fontsize", "40");
			subGraph.setGraphAttribute("fontcolor", "blue");

			PDGNode startNode = pdg.GetStartNode();
			Queue<PDGNode> worklist = new LinkedList<PDGNode>();
			worklist.add(startNode);

			DotGraphNode dotnode = subGraph.drawNode(String.valueOf(startNode.hashCode()));
			dotnode.setLabel(startNode.toString().replaceAll("\\r", ""));

			if (startNode.getType() == PDGNode.Type.REGION) {
				dotnode.setStyle(DotGraphConstants.NODE_STYLE_FILLED);
			}
			Set<PDGNode> visited = new HashSet<PDGNode>();

			while (!worklist.isEmpty()) {
				PDGNode node = worklist.poll();
				visited.add(node);

				for (PDGNode succ : node.getDependets()) {

					if (!visited.contains(succ)) {
						DotGraphNode succDotnode = subGraph.drawNode(String.valueOf(succ.hashCode()));
						if (succ.getType() == PDGNode.Type.REGION) {
							succDotnode.setStyle(DotGraphConstants.NODE_STYLE_FILLED);
						}
						succDotnode.setLabel(succ.toString().replaceAll("\\r", ""));
						worklist.add(succ);
					}

					subGraph.drawEdge(String.valueOf(node.hashCode()), String.valueOf(succ.hashCode()));
				}
			}
		}
		for (List<SdgEdge> edges : connections.values()) {
			for (SdgEdge edge : edges) {
				DotGraphEdge dotEdge = dot.drawEdge(String.valueOf(edge.from.hashCode()),
						String.valueOf(edge.to.hashCode()));
				dotEdge.setLabel(edge.type.toString());
			}
		}

		return dot;
	}
}
