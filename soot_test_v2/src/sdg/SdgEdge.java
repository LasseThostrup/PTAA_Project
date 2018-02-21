package sdg;

import soot.Unit;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;

public class SdgEdge {
	Unit from;
	Unit to;
	EdgeType type;
	HashMutablePDG pdgFrom;
	HashMutablePDG pdgTo;
	
	public SdgEdge(Unit from, Unit to, EdgeType type, HashMutablePDG pdgFrom, HashMutablePDG pdgTo) {
		this.from = from;
		this.to = to;
		this.type = type;
		this.pdgFrom = pdgFrom;
		this.pdgTo = pdgTo;
	}

	@Override
	public String toString() {
		return from.toString() + " ============> " + (to != null ? to.toString() : "");
	}	
	
	
}
