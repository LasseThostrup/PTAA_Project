package soot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import soot.Body;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.toolkits.graph.pdg.EnhancedUnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;
import soot.toolkits.graph.pdg.ProgramDependenceGraph;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphConstants;
import soot.util.dot.DotGraphNode;

public class Soot_test {

	public static void main(String[] args) {
		System.out.println(Scene.v().getSootClassPath());
		//Options.v().set_whole_program(true);
		Scene s = Scene.v();
		//s.loadNecessaryClasses();
		SootClass c = s.loadClassAndSupport("test.MyClass");
		Scene.v().setMainClass(c);
//		SootMethod m = c.getMethodByName("foo");
//		Body b = m.retrieveActiveBody();
		
		CHATransformer.v().transform();

		CallGraph cg = s.getCallGraph();
		cg.sourceMethods();
		//printGraph(b, "output");
	}
	
	public static void printGraph(Body body, String dir) {
		try {
			ProgramDependenceGraph graph = new HashMutablePDG(new EnhancedUnitGraph(
					body));
			DotNamer namer = new DotNamer((int) (graph.size() / 0.7f), 0.7f);
			DotGraph dotGraph = initDotGraph(body);
	
			generateGraph(graph, namer, dotGraph);
			saveDot(body, dir, dotGraph);
			
			dotGraph = null;
			namer = null;
			graph = null;
		} catch (Exception e) {
			System.err.println("Error building pdg for method: " + body.getMethod());
			System.err.println("Error: " + e.getMessage());
		}
	}

	private static void generateGraph(ProgramDependenceGraph graph,
			DotNamer namer, DotGraph dotGraph) {
		for (Iterator<PDGNode> nodesIt = graph.iterator(); nodesIt.hasNext(); ) {
		      namer.getName(nodesIt.next().toShortString());
		}
		
		PDGNode startNode = graph.GetStartNode();
		Queue<PDGNode> worklist = new LinkedList<PDGNode>();
		worklist.add(startNode);
		DotGraphNode dotnode = dotGraph.drawNode(namer.getName(startNode.toShortString()));
		dotnode.setLabel(startNode.toString());
		if (startNode.getType() == PDGNode.Type.REGION) {
			dotnode.setStyle(DotGraphConstants.NODE_STYLE_FILLED);
			//dotnode.setAttribute("bgcolor", "#efbaba");
		}
		
		Set<PDGNode> visited = new HashSet<PDGNode>();
		
		
		while (!worklist.isEmpty()) {
			PDGNode node = worklist.poll();
			visited.add(node);
			
			for (PDGNode succ : node.getDependets()) {
				
				if (!visited.contains(succ)) {
					DotGraphNode succDotnode = dotGraph.drawNode(namer.getName(succ.toShortString()));
					if (succ.getType() == PDGNode.Type.REGION) {
						succDotnode.setStyle(DotGraphConstants.NODE_STYLE_FILLED);
					}
					succDotnode.setLabel(succ.toString());
					worklist.add(succ);
				}
				
				dotGraph.drawEdge(namer.getName(node.toShortString()), namer.getName(succ.toShortString()));
 			}
		}
	}

	private static void saveDot(Body body, String dir, DotGraph dotGraph) {
		String filename = dir;
		if (filename.length() > 0) {
			filename = filename + java.io.File.separator;
		}
		filename = filename
				+ body.getMethod().getSubSignature().replace(java.io.File.separatorChar, '.')
				+ DotGraph.DOT_EXTENSION;

		dotGraph.plot(filename);
	}

	private static DotGraph initDotGraph(Body body) {
		String graphname = "cfg";
		if (body != null) {
			graphname = body.getMethod().getSubSignature();
		}
		DotGraph canvas = new DotGraph(graphname);
		canvas.setGraphLabel(graphname);
		canvas.setPageSize(8.5, 11.0);
		canvas.setNodeShape(DotGraphConstants.NODE_SHAPE_BOX);

		return canvas;
	}
	


	@SuppressWarnings({ "serial", "rawtypes" })
	public static class DotNamer extends HashMap {
		private int nodecount = 0;

		public DotNamer(int initialCapacity, float loadFactor) {
			super(initialCapacity, loadFactor);
		}

		@SuppressWarnings("unchecked")
		String getName(Object node) {
			Integer index = (Integer) this.get(node);
			if (index == null) {
				index = new Integer(nodecount++);
				this.put(node, index);
			}
			return index.toString();
		}

		@SuppressWarnings("unchecked")
		int getNumber(Object node) {
			Integer index = (Integer) this.get(node);
			if (index == null) {
				index = new Integer(nodecount++);
				this.put(node, index);
			}
			return index.intValue();
		}
	}

	/**
	 * Comparator used to order a list of nodes by the order in which they were
	 * labeled.
	 */
	@SuppressWarnings("rawtypes")
	static class NodeComparator implements java.util.Comparator {
		private DotNamer namer;

		NodeComparator(DotNamer namer) {
			this.namer = namer;
		}

		public int compare(Object o1, Object o2) {
			return (namer.getNumber(o1) - namer.getNumber(o2));
		}

		public boolean equal(Object o1, Object o2) {
			return (namer.getNumber(o1) == namer.getNumber(o2));
		}
	}
}
