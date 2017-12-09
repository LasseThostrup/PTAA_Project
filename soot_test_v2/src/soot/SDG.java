package soot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphConstants;
import soot.util.dot.DotGraphEdge;
import soot.util.dot.DotGraphNode;

public class SDG {

	LinkedHashMap<HashMutablePDG, List<SdgEdge>> connections;
	CallGraph cg;
	Body body;
	
	//Ctor
	//What to pass here? path to bytecode dir?
	public SDG(String entryMethod) {
		Scene s = Scene.v();
		SootClass c = s.loadClassAndSupport("test.MyClass");
		c.setApplicationClass();
		Scene.v().setMainClass(c);

		SootMethod m = c.getMethodByName("foo");
		body = m.retrieveActiveBody();

		UnitGraph g = new ExceptionalUnitGraph(body);
		HashMutablePDG pdg = new HashMutablePDG(g);

		CHATransformer.v().transform();

		cg = s.getCallGraph();
		//System.out.println(cg);

		connections = new LinkedHashMap<>();
		
		iteratePdg(pdg);
		
		for (Map.Entry<HashMutablePDG, List<SdgEdge>> entry: connections.entrySet()) {
			HashMutablePDG pdgEntry = entry.getKey();
		    List<SdgEdge> SdgEdgeList = entry.getValue();
		    for (SdgEdge edge : SdgEdgeList) {
		    	System.out.println(edge);
		    }
		    
		}
		
//				
//		for (PDGNode pdgNode : nodeToPDGmapping.keySet()) {
//			for (HashMutablePDG lpdg : nodeToPDGmapping.get(pdgNode)) {
//				System.out.println(pdgNode.toShortString() + "------->" + lpdg.toString());
//			}
//			System.out.println();
//		}
	}	
	
	private void iteratePdg(HashMutablePDG pdg) {
		//Base case
		if (connections.containsKey(pdg)) return;
		
		//Recursive case		
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
							Body body = edge.tgt().retrieveActiveBody();
							UnitGraph cfg = new ExceptionalUnitGraph(body);
							HashMutablePDG tempPdg = new HashMutablePDG(cfg);
							
							if (tempPdg.getNodes().size() > 1) {
								Object toPDGNode = tempPdg.GetStartNode();
								if (toPDGNode instanceof PDGNode) {
									
									EdgeType edgeType = edge.passesParameters() ? EdgeType.PARAM : EdgeType.CALL;									
									
									sdgEdgeList.add(new SdgEdge(node, (PDGNode)toPDGNode, edgeType, pdg, tempPdg));
									
									//Try to create a return edge if it applies
									// ...
									
									iteratePdg(tempPdg);
								}
							}
						}
					}
				}
			}
		}
	}

	public DotGraph toDotGraph() {
		//DotNamer namer = new DotNamer(1000, 0.7f);
		DotGraph dot = new DotGraph("System Dependence Graph");
		int j = 0;
		for (HashMutablePDG pdg: connections.keySet()) {
			String name = pdg.getCFG().getBody().getMethod().getName();
			DotGraph subGraph = dot.createSubGraph("cluster_"+name);
			subGraph.setGraphLabel(name);

			PDGNode startNode = pdg.GetStartNode();
			Queue<PDGNode> worklist = new LinkedList<PDGNode>();
			worklist.add(startNode);			
			
			DotGraphNode dotnode = subGraph.drawNode(String.valueOf(startNode.hashCode()));
			dotnode.setLabel("Method: "+name + " " + startNode.toString().replaceAll("\\r", ""));
			
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
						succDotnode.setLabel("Method: "+name + " " + succ.toString().replaceAll("\\r", ""));
						worklist.add(succ);
					}
					
					subGraph.drawEdge(String.valueOf(node.hashCode()), String.valueOf(succ.hashCode()));
	 			}
			}
		}
		for (List<SdgEdge> edges: connections.values()) {
			for(SdgEdge edge : edges) {
				DotGraphEdge dotEdge = dot.drawEdge(String.valueOf(edge.from.hashCode()), String.valueOf(edge.to.hashCode()));
				dotEdge.setLabel(edge.type.toString());
			}
		}
		
		return dot;
	}
}
