package code.mission;

import code.generic.Node;

public class MissionImpossibleNode extends Node<MissionImpossibleState>
{
	public MissionImpossibleNode(MissionImpossibleState state, MissionImpossibleNode parentNode, String operation, int depth, int gCost, int hCost)
	{
		super(state, parentNode, operation, depth, parentNode.gCost + gCost, hCost);
	}
	public MissionImpossibleNode(MissionImpossibleState initialState, int hCost)
	{
		super(initialState, hCost);
	}
	public int compareTo(MissionImpossibleNode o) {
		if(this.fCost == o.fCost) return this.depth - o.depth;
		return this.fCost-o.fCost;
	}
}
