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
import soot.jimple.internal.JimpleLocal;
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
import soot.util.dot.DotGraphEdge;
import soot.util.dot.DotGraphNode;

public class SDG {

	LinkedHashMap<HashMutablePDG, List<SdgEdge>> connections;
	LinkedHashMap<String, Unit> defUnitMappings;
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
		
		/*
		 * args = "-p cg enabled:true -w -no-bodies-for-excluded -full-resolver -process-dir PATH_TO_BIN"
		 * 
		 * create transformer
		 * 			create your own class blabla that extends SceneTransformer
		 * 				Scene.v().getClasses
		 * 							class.getmethods
		 * 				
		Options.v().set_process_dir(setting);
		 * 			
		 * add trans to pack manager
		 * PackManager.getPack("cg/wjtp").add(new Transfomer("name", blabla))
		 *  soot.Main.main(args.split(" "))"
		 * 
		 * 
			if the user should set the entry points:	Scene.v().setEntryPoints();
		 * */

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
		defUnitMappings = new LinkedHashMap<>();

		iteratePdg(pdg);

		for (Map.Entry<HashMutablePDG, List<SdgEdge>> entry : connections.entrySet()) {
			HashMutablePDG pdgEntry = entry.getKey();
			List<SdgEdge> SdgEdgeList = entry.getValue();
			for (SdgEdge edge : SdgEdgeList) {
				System.out.println(edge);
			}

		}
	}

	private void addDefinitionToUnitMapping(String name, Unit u) {
		// Create mapping between the potential definition of variable to the unit
		List<ValueBox> defBoxes = u.getDefBoxes();
		if (defBoxes == null)
			return;
		for (ValueBox vb : defBoxes) {
			Value v = vb.getValue();
			if (v instanceof JimpleLocal)
				defUnitMappings.put(name + ((JimpleLocal) v).getName(), u);
		}

	}

	private void iteratePdg(HashMutablePDG pdg) {
		// Base case
		if (connections.containsKey(pdg))
			return;

		// Recursive case
		LinkedList<SdgEdge> sdgEdgeList = new LinkedList<SdgEdge>();
		connections.put(pdg, sdgEdgeList);

		SootMethod method = pdg.getCFG().getBody().getMethod();
		String name = method.getSignature();

		Iterator i = pdg.iterator();
		while (i.hasNext()) {
			PDGNode node = (PDGNode) i.next();
			if (node.getNode() instanceof Block) {
				Block bl = (Block) node.getNode();
				Iterator<Unit> it = bl.iterator();
				while (it.hasNext()) {
					Unit u = (Unit) it.next();

					addDefinitionToUnitMapping(name, u);

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
								int parameterCount = tempPdg.getCFG().getBody().getMethod().getParameterCount();
								EdgeType edgeType = parameterCount > 0 ? EdgeType.PARAM : EdgeType.CALL;

								sdgEdgeList.add(new SdgEdge(u, null, edgeType, pdg, tempPdg));

								sdgEdgeList.addAll(findReturnEdges(tempPdg, pdg, u));

								iteratePdg(tempPdg);
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

	private List<SdgEdge> findReturnEdges(HashMutablePDG fromPdg, HashMutablePDG toPdg, Unit callUnit) {
		List<SdgEdge> sdgEdges = new LinkedList<SdgEdge>();
		Iterator i = fromPdg.iterator();
		while (i.hasNext()) {
			PDGNode node = (PDGNode) i.next();
			if (node.getNode() instanceof Block) {
				Block bl = (Block) node.getNode();
				Iterator<Unit> it = bl.iterator();
				while (it.hasNext()) {
					Unit u = (Unit) it.next();

					if (u instanceof JRetStmt || u instanceof JReturnStmt)
						sdgEdges.add(new SdgEdge(u, callUnit, EdgeType.RET, fromPdg, toPdg));
				}
			}
		}
		return sdgEdges;
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
