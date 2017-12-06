package soot;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;
import soot.util.dot.DotGraph;
public class SDG {

	LinkedHashMap<HashMutablePDG, List<SdgEdge>> connections;
	CallGraph cg;
	
	//Ctor
	//What to pass here? path to bytecode dir?
	public SDG(String entryMethod) {
		Scene s = Scene.v();
		SootClass c = s.loadClassAndSupport("test.MyClass");
		c.setApplicationClass();
		Scene.v().setMainClass(c);

		SootMethod m = c.getMethodByName("foo");
		Body b = m.retrieveActiveBody();

		UnitGraph g = new ExceptionalUnitGraph(b);
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
								Object toPDGNode = tempPdg.getNodes().get(0);
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
		throw new java.lang.UnsupportedOperationException();
	}
}
