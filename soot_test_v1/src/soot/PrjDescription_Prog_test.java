package soot;

import util.DotDrawer;

public class PrjDescription_Prog_test {
	public static void main(String[] args) {
		SootClass c = Scene.v().loadClassAndSupport("test.PrjDescription_Prog");
		Scene.v().loadNecessaryClasses();
		c.setApplicationClass();
		// Retrieve the method and its body
		SootMethod m = c.getMethodByName("foo");
		DotDrawer.drawProcedureDependenceGraph(m, "output/prjDescription_prog_pdg.dot");
	}
}
