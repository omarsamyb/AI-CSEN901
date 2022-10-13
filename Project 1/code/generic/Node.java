package code.generic;

public class Node<S> implements Comparable<Node>
{
	public S state;
	public Node<S> parentNode;
	public String operation;
	public int depth;
	public int fCost;
	public int gCost;
	public int hCost;
	
	public Node(S state, Node<S> parentNode, String operation, int depth, int gCost, int hCost)
	{
		this.state = state;
		this.parentNode = parentNode;
		this.operation = operation;
		this.depth = depth;
		this.gCost = gCost;
		this.hCost = hCost;
		this.fCost = gCost + hCost;
	}
	public Node(S initialState, int hCost)
	{
		this.state = initialState;
		this.operation = "";
		this.hCost = hCost;
		this.fCost = hCost;
	}
	@Override
	public int compareTo(Node o) {
		if(this.fCost == o.fCost) return this.depth-o.depth;
		return this.fCost-o.fCost;
	}
}
