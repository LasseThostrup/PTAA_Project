package sdg;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.internal.JRetStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;

public class SDGTransformer extends SceneTransformer {

	private SDG sdg;

	private CallGraph cg;

	public SDGTransformer() {
		sdg = null;
	}

	@Override
	protected void internalTransform(String arg0, Map arg1) {
		sdg = new SDG();
		cg = Scene.v().getCallGraph();
		Scene.v().getEntryPoints();
		SootMethod main = Scene.v().getMainMethod();
		Body body = main.retrieveActiveBody();
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		HashMutablePDG hashMutablePDG = new HashMutablePDG(unitGraph);
		iteratePdg(hashMutablePDG);
	}

	private void addDefinitionToUnitMapping(String name, Unit u) {
		// Create mapping between the potential definition of variable to the unit
		List<ValueBox> defBoxes = u.getDefBoxes();
		if (defBoxes == null)
			return;
		for (ValueBox vb : defBoxes) {
			Value v = vb.getValue();
			if (v instanceof JimpleLocal)
				sdg.getDefUnitMappings().put(name + ((JimpleLocal) v).getName(), u);
			else if (v instanceof FieldRef) {
				SootField sf = ((FieldRef) v).getField();
				if (!sdg.getSootFields().contains(sf)) sdg.getSootFields().add(sf);
				sdg.getDefUnitMappings().put(sf.toString(), u);
			}
		}

	}

	@SuppressWarnings("rawtypes")
	private void iteratePdg(HashMutablePDG pdg) {
		// Base case
		if (sdg.getConnections().containsKey(pdg))
			return;

		// Recursive case
		LinkedList<SdgEdge> sdgEdgeList = new LinkedList<SdgEdge>();
		sdg.getConnections().put(pdg, sdgEdgeList);

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
		LinkedHashMap<HashMutablePDG, List<SdgEdge>> connections = sdg.getConnections();
		for (HashMutablePDG pdgCmp : connections.keySet()) {
			if (pdgCmp.toString().equals(pdg.toString()))
				existingPdg = pdgCmp; // TODO: Not ideal to do such a long string comparison
		}
		return existingPdg;
	}

	@SuppressWarnings("rawtypes")
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

	/**
	 * Returns the created SDG if {@iteratePdg} was called and else null
	 * 
	 * @return SDG or null
	 */

	public SDG getSDG() {
		return sdg;
	}

}
