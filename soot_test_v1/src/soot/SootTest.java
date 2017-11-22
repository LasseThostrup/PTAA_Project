package soot;

import java.util.Iterator;

import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;

public class SootTest {

	public static void main(String[] args) {
		//System.out.println(Scene.v().getSootClassPath());
		SootClass c = Scene.v().loadClassAndSupport("test.MyClass");
		c.setApplicationClass();
		// Retrieve the method and its body
		SootMethod m = c.getMethodByName("foo");
		Body b = m.retrieveActiveBody();
		System.out.println(b);
		// Build the CFG and run the analysis
//		UnitGraph g = new ExceptionalUnitGraph(b);
//		HashMutablePDG pdg = new HashMutablePDG(g);
//		// Iterate over the results
//		Iterator i = pdg.iterator();
//		while (i.hasNext()) {
//			PDGNode p = (PDGNode) i.next();
//			System.out.println(p.getBackDependets());
//		}
	}

}
