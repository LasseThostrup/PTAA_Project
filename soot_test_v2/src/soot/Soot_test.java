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

public class Soot_test {

	public static void main(String[] args) {
		Scene s = Scene.v();
		SootClass c = s.loadClassAndSupport("test.MyClass");
		c.setApplicationClass();
		Scene.v().setMainClass(c);

		SootMethod m = c.getMethodByName("foo");
		Body b = m.retrieveActiveBody();

		UnitGraph g = new ExceptionalUnitGraph(b);
		HashMutablePDG pdg = new HashMutablePDG(g);

		CHATransformer.v().transform();

		CallGraph cg = s.getCallGraph();
		System.out.println(cg);
		

		Map<PDGNode, List<HashMutablePDG>> nodeToPDGmapping = new LinkedHashMap<PDGNode, List<HashMutablePDG>>();
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
							if (!nodeToPDGmapping.containsKey(node)) {
								List<HashMutablePDG> pdgs = new LinkedList<HashMutablePDG>();
								pdgs.add(tempPdg);
								nodeToPDGmapping.put(node, pdgs);
							} else {
								nodeToPDGmapping.get(node).add(tempPdg);
							}
						}
					}
				}
			}
		}
		for (PDGNode pdgNode : nodeToPDGmapping.keySet()) {
			for (HashMutablePDG lpdg : nodeToPDGmapping.get(pdgNode)) {
				//System.out.println(pdgNode.toShortString() + "------->" + lpdg.toString());
			}
			//System.out.println();
		}

	}

}
