package soot;

import java.util.Iterator;

import soot.jimple.internal.JInvokeStmt;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;

public class SootTest {

	public static void main(String[] args) {
		// System.out.println(Scene.v().getSootClassPath());
		SootClass c = Scene.v().loadClassAndSupport("test.PrjDescription_Prog");
		c.setApplicationClass();
		// Retrieve the method and its body
		SootMethod m = c.getMethodByName("foo");
		Body b = m.retrieveActiveBody();
		// System.out.println(b);
		// Build the CFG and run the analysis
		UnitGraph g = new ExceptionalUnitGraph(b);
		HashMutablePDG pdg = new HashMutablePDG(g);
		// Iterate over the results
		Iterator i = pdg.iterator();
		while (i.hasNext()) {
//			System.out.println(((PDGNode)i.next()).getNode().getClass());
			PDGNode node = (PDGNode) i.next();
			if (node.getNode() instanceof Block) {
				Block bl = (Block) node.getNode();
				System.out.println(bl);
				Iterator it = bl.iterator();
				while (it.hasNext()) {
					Unit u = (Unit) it.next();
					if (u instanceof JInvokeStmt) {
						JInvokeStmt jInvokeStmt = (JInvokeStmt) u;
						System.out.println(jInvokeStmt.getInvokeExpr().getMethod().getName());
					}

				}
				System.out.println();
			}
		}
	}

}
