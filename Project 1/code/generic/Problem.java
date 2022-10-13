package code.generic;

public abstract class Problem<O, S, N> {
	public O[] operators;
	public State initialState;
	public int expandedNodes;
	public Problem(O[] operators, State initialState){
		this.operators = operators; //May cause problems due to lack of deep cloning
		this.initialState = initialState;
	}
	
	public abstract boolean goalTest(S state);
	public abstract int pathCost(S currState, S nextState);
	public abstract State stateSpace(S state, O operator);
	public abstract int heuristicCost1(S state);
	public abstract int heuristicCost2(S state);
	public abstract N createSpecificNode(S state, int hCost);
	public abstract N createSpecificNode(S state, N node, String b, int depth, int gCost, int hCost);
	public N general_search(String searchStrategy)
	{
		QueuingFunction<N,S> queue = new QueuingFunction<N,S>(searchStrategy);
	    N root = createSpecificNode((S) initialState, 0);
	    queue.enque(root);
		while(!queue.isEmpty())
		{
			N preNode = queue.deque();
			Node node = (Node) preNode;
			
			if(goalTest((S) node.state)) return (N) preNode;
			if(searchStrategy != "ORDERED-INSERT") {
				if(((State) node.state).stateExists2()) continue;
				((State) node.state).setStateVisited2();
			}
			else {
				if(((State) node.state).stateExists()) continue;
				((State) node.state).setStateVisited();
			}
	    	expandedNodes++;
	    	
	    	for(O op: operators) {
	    		S nextState = (S) stateSpace((S) node.state, op);
	    		if(nextState != null && ((!((State) nextState).stateExists2() && searchStrategy!="ORDERED-INSERT" ) || (!((State) nextState).stateExists() && searchStrategy=="ORDERED-INSERT" ))) {
	    			int gCost = pathCost((S) node.state, nextState);
	    			N nextNode = createSpecificNode(nextState, preNode, node.operation+op+" ", node.depth+1, gCost, 0);
	    			queue.enque(nextNode);
	    		}
	    	}
		}
		return null;
	}
	public N depth_limited_search(int maxDepth)
	{			
		QueuingFunction<N,S> queue = new QueuingFunction<N,S>("ENQUEUE-AT-FRONT");
	    N root = createSpecificNode((S) initialState, 0);
	    queue.enque(root);
		while(!queue.isEmpty())
		{
			N preNode = queue.deque();
			Node node = (Node) preNode;
			
			if(node.depth > maxDepth) continue;
			
			if(goalTest((S) node.state)) return (N) preNode;
			if(((State) node.state).stateExists2()) continue;
			((State) node.state).setStateVisited2();
	    	expandedNodes++;
	    	
	    	for(O op: operators) {
	    		S nextState = (S) stateSpace((S) node.state, op);
	    		if(nextState != null && !((State) nextState).stateExists2()) {
	    			int gCost = pathCost((S) node.state, nextState);
	    			N nextNode = createSpecificNode(nextState, preNode, node.operation+op+" ", node.depth+1, gCost, 0);
	    			queue.enque(nextNode);
	    		}
	    	}
		}
		return null;
	}
	public N best_first_search(String evalFun)
	{
		int hCost;
		QueuingFunction<N,S> queue = new QueuingFunction<N,S>("ORDERED-INSERT");
		N root = createSpecificNode((S) initialState, 0);
		Node rootNode = (Node) root;
		S rootState = (S) rootNode.state;
		if(evalFun.endsWith("1"))
			hCost = heuristicCost1(rootState);
		else 
			hCost = heuristicCost2(rootState);
		N rootWithCost = createSpecificNode((S) initialState, hCost);
	   	queue.enque(rootWithCost);
		while(!queue.isEmpty())
		{
			N preNode = queue.deque();
			Node node = (Node) preNode;
			
			if(goalTest((S) node.state)) return (N) preNode;
			if(evalFun.startsWith("AS")) {
				if(((State) node.state).stateExists()) continue;
				((State) node.state).setStateVisited();
			}
			else {
				if(((State) node.state).stateExists2()) continue;
				((State) node.state).setStateVisited2();
			}
				
	    	expandedNodes++;
	    	
	    	for(O op: operators) {
	    		S nextState = (S) stateSpace((S) node.state, op);
	    		if(nextState != null && ((!((State) nextState).stateExists() && evalFun.startsWith("AS")) ||  (!((State) nextState).stateExists2() && !evalFun.startsWith("AS")))){
	    			int gCost = pathCost((S) node.state, nextState);
	    			if(evalFun.endsWith("1"))
	    				hCost = heuristicCost1(nextState);
	    			else 
	    				hCost = heuristicCost2(nextState);
	    			N nextNode;
	    			if(evalFun.startsWith("AS"))
	    				nextNode = createSpecificNode(nextState, preNode, node.operation+op+" ", node.depth+1, gCost, hCost);
	    			else
	    				nextNode = createSpecificNode(nextState, preNode, node.operation+op+" ", node.depth+1, 0, hCost);
	    			queue.enque(nextNode);
	    		}
	    	}
		}
		return null;
	}
}
