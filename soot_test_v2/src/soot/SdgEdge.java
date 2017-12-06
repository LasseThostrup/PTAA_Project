package soot;

import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;

public class SdgEdge {
	PDGNode from;
	PDGNode to;
	EdgeType type;
	HashMutablePDG pdgFrom;
	HashMutablePDG pdgTo;
	
	public SdgEdge(PDGNode from, PDGNode to, EdgeType type, HashMutablePDG pdgFrom, HashMutablePDG pdgTo) {
		this.from = from;
		this.to = to;
		this.type = type;
		this.pdgFrom = pdgFrom;
		this.pdgTo = pdgTo;
	}

	@Override
	public String toString() {
		return from.toShortString() + " ============> " + to.toShortString();
	}	
	
	
}
