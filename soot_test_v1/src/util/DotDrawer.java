package util;

import java.util.Iterator;

import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

public class DotDrawer {

	public static void drawMethodDependenceGraph(SootMethod entryMethod, String path) {
		Body body = entryMethod.retrieveActiveBody();
		ExceptionalUnitGraph exceptionalUnitGraph = new ExceptionalUnitGraph(body);

		CFGToDotGraph cfgForMethod = new CFGToDotGraph();
		cfgForMethod.drawCFG(exceptionalUnitGraph);
		DotGraph cfgDot = cfgForMethod.drawCFG(exceptionalUnitGraph);
		cfgDot.plot(path);
	}

	public static void drawProcedureDependenceGraph(SootMethod entryMethod, String path) {
		Body body = entryMethod.retrieveActiveBody();
		ExceptionalUnitGraph exceptionalUnitGraph = new ExceptionalUnitGraph(body);
		HashMutablePDG hashMutablePDG = new HashMutablePDG(exceptionalUnitGraph);
		CFGToDotGraph pdgForMethod = new CFGToDotGraph();
		DotGraph pdgDot = pdgForMethod.drawCFG(hashMutablePDG, body);
		pdgDot.plot(path);
	}

	@SuppressWarnings("unchecked")
	public static void drawCallGraph(CallGraph callGraph, String path) {
		DotGraph dot = new DotGraph("callgraph");
		@SuppressWarnings("rawtypes")
		Iterator iteratorEdges = ((DirectedGraph<Unit>) callGraph).iterator();

		int i = 0;
		System.out.println("Call Graph size : " + callGraph.size());
		while (iteratorEdges.hasNext()) {
			Edge edge = (Edge) iteratorEdges.next();
			String node_src = edge.getSrc().toString();
			String node_tgt = edge.getTgt().toString();

			dot.drawEdge(node_src, node_tgt);
			System.out.println(i++);
		}

		dot.plot(path);
	}
}
