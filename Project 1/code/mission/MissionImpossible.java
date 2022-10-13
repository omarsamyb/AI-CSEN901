package code.mission;

import code.generic.Problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Stack;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;

public class MissionImpossible extends Problem<Operator, MissionImpossibleState, MissionImpossibleNode>
{
	int gridWidth;
	int gridHeight;
	Pos submarinePos;

	static boolean [][][]visitedStates;
	static HashSet<MissionImpossibleState> visitedStatesHashset;

	
	MissionImpossible(Operator[] operators, MissionImpossibleState initialState, Object[] environment) {
		super(operators, initialState);
		this.gridWidth = (int) environment[0];
		this.gridHeight = (int) environment[1];
		this.submarinePos = (Pos) environment[3];
		this.expandedNodes = 0;
	}
	public boolean goalTest(MissionImpossibleState state) {
		for(Member m:state.membersList)
			if(!m.isSaved)
				return false;
		return true;
	}
	public MissionImpossibleState stateSpace(MissionImpossibleState state, Operator op) {
		if(op == Operator.UP) 
		{
			if(state.ethanPos.x-1<0)
				return null;
			MissionImpossibleState cloned = new MissionImpossibleState(state.ethanPos, state.membersList, state.remainingSeats);
			cloned.ethanPos.x--;
			cloned.decreaseHealthAll();
			return cloned;
		}
		if(op == Operator.DOWN)
		{
			if(state.ethanPos.x+1>=gridHeight)
				return null;
			MissionImpossibleState cloned = new MissionImpossibleState(state.ethanPos, state.membersList, state.remainingSeats);
			cloned.ethanPos.x++;
			cloned.decreaseHealthAll();
			return cloned;
		}
		if(op == Operator.LEFT)
		{
			if(state.ethanPos.y-1<0)
				return null;
			MissionImpossibleState cloned = new MissionImpossibleState(state.ethanPos, state.membersList, state.remainingSeats);
			cloned.ethanPos.y--;
			cloned.decreaseHealthAll();
			return cloned;
		}
		if(op == Operator.RIGHT)
		{
			if(state.ethanPos.y+1>=gridWidth)
				return null;
			MissionImpossibleState cloned = new MissionImpossibleState(state.ethanPos, state.membersList, state.remainingSeats);
			cloned.ethanPos.y++;
			cloned.decreaseHealthAll();
			return cloned;
		}
		if(op == Operator.CARRY)
		{
			if(state.remainingSeats>0)
			{
				for(int i=0;i<state.membersList.size();i++)
					if(state.membersList.get(i).pos.equals(state.ethanPos) && !state.membersList.get(i).isSaved && !state.membersList.get(i).isCarried)
					{
						MissionImpossibleState cloned = new MissionImpossibleState(state.ethanPos, state.membersList, state.remainingSeats);
						cloned.membersList.get(i).isCarried=true;
						cloned.remainingSeats--;
						cloned.decreaseHealthAll();
						return cloned;
					}
			}
		}
		if(op == Operator.DROP)
		{
			if(state.ethanPos.equals(submarinePos))
			{
				boolean isCarrying = false;
				MissionImpossibleState cloned = new MissionImpossibleState(state.ethanPos, state.membersList, state.remainingSeats);
				for(int i=0;i<state.membersList.size();i++)
					if(state.membersList.get(i).isCarried && !state.membersList.get(i).isSaved)
					{
						isCarrying = true;
						cloned.membersList.get(i).isCarried=false;
						cloned.membersList.get(i).isSaved=true;
						cloned.membersList.get(i).pos= new Pos(submarinePos.x, submarinePos.y);
						cloned.remainingSeats++;
					}
				if(isCarrying) {
					cloned.decreaseHealthAll();
					return cloned;
				}
			}
		}
		return null;
	}
	public int pathCost(MissionImpossibleState currState, MissionImpossibleState nextState) {
		int deathPenalty = 100000;
		int deaths = 0;
		int cost = 0;
		
		for(int i=0;i<nextState.membersList.size();i++) {
			if(!nextState.membersList.get(i).isCarried && !nextState.membersList.get(i).isSaved) {
				int damageIncured = nextState.membersList.get(i).health - currState.membersList.get(i).health;
				if(damageIncured !=0 && nextState.membersList.get(i).health==100) {
					deaths++;
					cost += damageIncured*100;
				}
				else
					if(nextState.membersList.get(i).health != 100)
						cost += damageIncured*100;
			}
			if(!currState.membersList.get(i).isCarried && nextState.membersList.get(i).isCarried) {
				cost+= nextState.membersList.get(i).health;
			}
		}
		
		return cost + (deaths*deathPenalty);
	}
	
	public int heuristicCost1(MissionImpossibleState state)
	{
		int totalCost = 0;
		double minDistance = Double.MAX_VALUE;
		Member closestMember = null;
		int closestMemberIndex = -1;
		Pos myCurPos = new Pos(state.ethanPos.x, state.ethanPos.y);
		boolean [] isRemoved = new boolean[state.membersList.size()];
		
		int[] health = new int[state.membersList.size()];
		for(int i = 0; i<health.length; i++) {
			health[i] = state.membersList.get(i).health;
		}
			
		for(int habd=0;habd<isRemoved.length;habd++)
		{
			for(int i=0;i<state.membersList.size();i++)
			{
				if(state.membersList.get(i).isSaved || state.membersList.get(i).isCarried) isRemoved[i] = true;
				double curDist = state.membersList.get(i).pos.getManhatanDistance(myCurPos);
				if(!isRemoved[i] && curDist<minDistance)
				{
					minDistance = curDist;
					closestMemberIndex = i;
					closestMember = state.membersList.get(i);
				}
			}
			if(minDistance != Double.MAX_VALUE) {
				myCurPos = new Pos(closestMember.pos.x, closestMember.pos.y);
				for(int j=0;j<health.length;j++) {
					if(!isRemoved[j]) {
						int initialHealth = health[j];
						health[j] += minDistance*2;
						if(health[j]>100) health[j]=100;
						totalCost += (health[j] - initialHealth);
					}
				}
				isRemoved[closestMemberIndex] = true;
			}
			minDistance = Double.MAX_VALUE;
		}
		return totalCost;
	}
	
	public int heuristicCost2(MissionImpossibleState state)
	{
		int totalCost = 0;
		Pos myCurPos = new Pos(state.ethanPos.x, state.ethanPos.y);
		
		ArrayList<Member> sorted = new ArrayList<Member>();
		for(Member m:state.membersList)
			if(!m.isCarried && !m.isSaved)
				sorted.add(new Member(new Pos(m.pos.x, m.pos.y), m.health));
		Collections.sort(sorted);
		
		for(int i=0;i<sorted.size();i++) {
			int remainingHealth = 100 - sorted.get(i).health;
			double distToHighestMember = sorted.get(i).pos.getManhatanDistance(myCurPos);
			if(distToHighestMember*2<remainingHealth) {
				myCurPos = new Pos(sorted.get(i).pos.x, sorted.get(i).pos.y);
				totalCost += (int)distToHighestMember*2;
				sorted.remove(i);
				i--;
				for(int j=0;j<sorted.size();j++) {
					int initialHealth = sorted.get(j).health;
					sorted.get(j).health += distToHighestMember*2;
					if(sorted.get(j).health>100) sorted.get(j).health=100;
					totalCost += (sorted.get(j).health - initialHealth);
				}
			}
			else
				continue;	
		}
		
		for(Member m: sorted) {
			if(m.health != 100)
				totalCost += 100 - m.health;
		}
		return totalCost;
	}
	
	public static String genGrid() {
		Random rand = new Random();
		String grid = "";
		int randWidth = rand.nextInt(11)+5;    //range is [5,15]
		int randHeight = rand.nextInt(11)+5;   //range is [5,15]
		
		grid += randWidth + "," + randHeight + ";";
		
		ArrayList<Pos> gridPositions = new ArrayList<>();
		
		for(int i = 0;i<randHeight;i++)
			for(int j = 0;j<randWidth;j++) 
				gridPositions.add(new Pos(i,j));			
		Collections.shuffle(gridPositions);
		
		Pos ethanPos = gridPositions.remove(0);
		grid += ethanPos.x + "," + ethanPos.y + ";";
		
		Pos submarinePos = gridPositions.remove(0);
		grid += submarinePos.x + "," + submarinePos.y + ";";
		
		int numOfMembers = rand.nextInt(6)+5;  //range is [5,10]
		Member[] members = new Member[numOfMembers];
		String membersPositions = "";
		String membersHealth = "";
		for(int i=0;i<members.length;i++) {
			int memberHP = rand.nextInt(99)+1; //range is [1,99]
			Pos memberPos = gridPositions.remove(0);
			membersPositions += memberPos.x + "," + memberPos.y + ",";
			membersHealth += memberHP + ",";
			members[i] = new Member(memberPos,memberHP);
		}

		membersHealth = membersHealth.substring(0,membersHealth.length()-1);
		membersPositions = membersPositions.substring(0,membersPositions.length()-1);

		int truckCapacity = rand.nextInt(numOfMembers)+1;
		
		grid = grid + membersPositions + ";" + membersHealth + ";" + truckCapacity;
		
		return grid;
	}
	
	public static Object[] parseGrid(String grid)
	{
		/*
		 * Returns Object array of length 7:
		 * 0 = gridWidth - Int
		 * 1 = gridHeight - Int
		 * 2 = Ethan's position - Pos
		 * 3 = Submarine position - Pos
		 * 4 = Members' positions - Array
		 * 5 = Members' health - Array
		 * 6 = Truck Capacity - Int
		 */
		Object [] output = new Object [7];
		String [] splitGrid = grid.split(";");
		String [] gridSize = splitGrid[0].split(",");
		String [] ethanPosStr = splitGrid[1].split(",");
		String [] submarinePosStr = splitGrid[2].split(",");
		String [] membersLocationsStr = splitGrid[3].split(",");
		String [] membersHealthStr = splitGrid[4].split(",");
		int truckCapacity = Integer.parseInt(splitGrid[5]);
		Pos [] membersLocations = new Pos[membersLocationsStr.length/2];
		for(int i=0; i<membersLocations.length;i++)
			membersLocations[i] = new Pos(Integer.parseInt(membersLocationsStr[i*2]), Integer.parseInt(membersLocationsStr[(i*2)+1]));
		int [] membersHealth = new int[membersHealthStr.length];
		for(int i=0; i<membersHealthStr.length; i++)
			membersHealth[i] = Integer.parseInt(membersHealthStr[i]);
		output = new Object[]{Integer.parseInt(gridSize[0]), Integer.parseInt(gridSize[1]), 
				Pos.parseStringArr(ethanPosStr), Pos.parseStringArr(submarinePosStr), 
				membersLocations, membersHealth, truckCapacity};
		return output;
	}
	
	public static String solve(String grid, String strategy, boolean visualize) throws IOException {
		Object[] environment= parseGrid(grid);
		Pos ethanPos = (Pos)environment[2];
		Pos [] membersLocations = (Pos[]) environment[4];
		int [] membersHealth = (int[]) environment[5];
		int truckCapacity = (int) environment[6];
		ArrayList<Member> membersList = new ArrayList<Member>();
		for(int i=0;i<membersLocations.length;i++)
			membersList.add(new Member(membersLocations[i], membersHealth[i]));
		
		visitedStates = new boolean[15][15][59049];
		visitedStatesHashset = new HashSet<MissionImpossibleState>();
		
		MissionImpossibleState initialState = new MissionImpossibleState(ethanPos, membersList, truckCapacity);
		MissionImpossible m = new MissionImpossible(Operator.values(), initialState, environment);
		
		MissionImpossibleNode outputNode = null;
		if(strategy.equals("BF"))		outputNode = BFSearch(m);
		else if(strategy.equals("DF"))	outputNode = DFSearch(m);
		else if(strategy.equals("ID"))	outputNode = IDSearch(m);
		else if(strategy.equals("UC"))	outputNode = UCSearch(m);
		else if (strategy.equals("GR1"))outputNode = GreedySearch1(m);
		else if (strategy.equals("GR2"))outputNode = GreedySearch2(m);
		else if (strategy.equals("AS1"))outputNode = AStarSearch1(m);
		else if (strategy.equals("AS2"))outputNode = AStarSearch2(m);
		else System.out.println("Incorrect Strategy!");
		
		String results;
		if(outputNode != null) results = getResultsFromNode(outputNode, m);
		else results = "No Solution was found!";
		
		if(visualize && outputNode != null) visualize(m,outputNode);
		collectTrash();
		
		return results;
	}
	
	private static Stack<MissionImpossibleNode> getPathToNode(MissionImpossibleNode n) {
		Stack<MissionImpossibleNode> arr = new Stack<MissionImpossibleNode>();
		arr.add(n);
		MissionImpossibleNode curNode = n; 
		while(curNode.parentNode != null) {
			arr.push((MissionImpossibleNode) curNode.parentNode);
			curNode = (MissionImpossibleNode) curNode.parentNode;
		}
		return arr;
	}
	
	private static void visualize(MissionImpossible m,MissionImpossibleNode endNode) throws IOException {
		Stack<MissionImpossibleNode> nodesArr = getPathToNode(endNode);
		String outputFile = "";
		FileWriter myWriter = new FileWriter("output.txt");
		while(!nodesArr.isEmpty()) {
			MissionImpossibleNode n = nodesArr.pop();
			MissionImpossibleState curState = n.state;
			String[][] grid = new String[m.gridHeight][m.gridWidth];
			for(Member i : curState.membersList) {
				if(!i.isCarried && !i.isSaved)
					grid[i.pos.x][i.pos.y] += ",F("+i.health+")";
			}
			grid[m.submarinePos.x][m.submarinePos.y] += ",S";
			grid[curState.ethanPos.x][curState.ethanPos.y] += ",E";
			
			outputFile += drawGrid(grid, curState.remainingSeats, n.operation.split(" ")[n.operation.split(" ").length-1], myWriter);
		}
		outputFile += "\n"+endNode.operation;
		myWriter.write(outputFile);
		myWriter.close();
	}
	
	private static String drawGrid(String[][] grid,int remainingSeats,String lastOp,FileWriter myWriter) throws IOException {
		String outputFile ="";
		String row = ""; 
		for(int i=0;i<grid.length;i++) {
			row = "| ";
			String border = "+";
			for(int j=0;j<grid[0].length;j++) {
				border +="----------+";
				if(grid[i][j] != null) {
					String tmp = grid[i][j].replace("null,", "");
					row += padString(tmp,8)+" | ";					
				}
				else
					row += "         | ";
			}
			if(i==0) outputFile+=border+"\n";
			outputFile+=row+"\n"+border+"\n";
//			if(i==0) System.out.println(border);
//			System.out.println(row);
//			System.out.println(border);
		}
		outputFile+= "carry capacity:"+remainingSeats+"\noperation: "+lastOp+"\n---------------------------------------------\n";
//		System.out.println("carry capacity:"+remainingSeats);
//		System.out.println("operation: "+lastOp);
//		System.out.println("---------------------------------------------");
		return outputFile;
	}
	
	private static String padString(String s,int pad) {
		while(s.length()!= pad) {
			s += " ";
		}
		return s;
	}
	
	private static MissionImpossibleNode AStarSearch1(MissionImpossible m) {
		return (MissionImpossibleNode) m.best_first_search("AS1");
	}
	
	private static MissionImpossibleNode AStarSearch2(MissionImpossible m) {
		return (MissionImpossibleNode) m.best_first_search("AS2");
	}
	
	private static MissionImpossibleNode UCSearch(MissionImpossible m) {
		return (MissionImpossibleNode) m.general_search("ORDERED-INSERT");
	}
	
	private static MissionImpossibleNode GreedySearch1(MissionImpossible m) {
		return (MissionImpossibleNode) m.best_first_search("GR1");
	}
	
	private static MissionImpossibleNode GreedySearch2(MissionImpossible m) {
		return (MissionImpossibleNode) m.best_first_search("GR2");
	}

	private static MissionImpossibleNode IDSearch(MissionImpossible m) {
		int maxDepth = 1;
		while(true)
		{
			MissionImpossibleNode answer = (MissionImpossibleNode) m.depth_limited_search(maxDepth);
			if(answer!=null) return answer;
			maxDepth++;
		}
	}
		
	private static MissionImpossibleNode DFSearch(MissionImpossible m) {
		return (MissionImpossibleNode) m.general_search("ENQUEUE-AT-FRONT");
	}
	
	private static MissionImpossibleNode BFSearch(MissionImpossible m) {
		return (MissionImpossibleNode) m.general_search("ENQUEUE-AT-END");
	}
	
	static String getResultsFromNode(MissionImpossibleNode node, MissionImpossible m) throws IOException {
		String plan = Arrays.toString(node.operation.split(" "));
		plan = plan.substring(1, plan.length()-1);
		plan = plan.replace(" ", "");
		plan = plan.toLowerCase();
		
		int deaths = 0;
		int[] remainingHealths = new int[node.state.membersList.size()];
		for(int i = 0;i<remainingHealths.length;i++) {
			if(node.state.membersList.get(i).health>=100)
				deaths++;
			remainingHealths[i] = node.state.membersList.get(i).health;
		}
		
		String healths = Arrays.toString(remainingHealths);
		healths = healths.substring(1, healths.length()-1);
		healths = healths.replace(" ", "");
		
		int nodes = m.expandedNodes;
		
		return plan+";"+deaths+";"+healths+";"+nodes;
	}
	
	static int sum(String x) {
		String[] s = x.split(",");
		int sum = 0;
		for(int i=0;i<s.length;i++) {
			sum+= Integer.parseInt(s[i]);
		}
		return sum;
	}
	
	public static double getMemoryNow() {
		Runtime runtime = Runtime.getRuntime();
		// Run the garbage collector
		//        runtime.gc();
		// Calculate the used memory
		long memory = runtime.totalMemory() - runtime.freeMemory();
		double n = memory/ ((long)(1024L * 1024L)*1.0);
		System.out.println("Used memory in MegaBytes: " + n);
		return n;
	}
	public static void collectTrash() {
		Runtime runtime = Runtime.getRuntime();
		// Run the garbage collector
		runtime.gc();
        
	}
	
	static void checkOptimality() throws IOException {
		int count = 0;
		while(true) {
			String grid = genGrid();
			int sumUC;
			int sumAS1;
			int sumAS2;
			int deathUC;
			int deathAS1;
			int deathAS2;
			String output = solve(grid, "UC", false);
//			calcDamageIncurred(grid.split(";")[grid.split(";").length-2], output.split(";")[output.split(";").length-2]);
			sumUC = sum(output.split(";")[output.split(";").length-2]);
//			sumUC = calcDamageIncurred(grid.split(";")[grid.split(";").length-2], output.split(";")[output.split(";").length-2]);
			deathUC = Integer.parseInt(output.split(";")[output.split(";").length-3]);
			output = solve(grid, "AS1", false);
			sumAS1 = sum(output.split(";")[output.split(";").length-2]);
//			sumAS1 = calcDamageIncurred(grid.split(";")[grid.split(";").length-2], output.split(";")[output.split(";").length-2]);
			deathAS1 = Integer.parseInt(output.split(";")[output.split(";").length-3]);
			output = solve(grid, "AS2", false);
			sumAS2 = sum(output.split(";")[output.split(";").length-2]);
//			sumAS2 = calcDamageIncurred(grid.split(";")[grid.split(";").length-2], output.split(";")[output.split(";").length-2]);
			deathAS2 = Integer.parseInt(output.split(";")[output.split(";").length-3]);
//			Object [] bf = MySolve.mySolver(grid);
//			System.out.println((int)bf[0]%10000);
			if(sumUC!=sumAS1 || sumUC != sumAS2 || deathUC != deathAS1 || deathUC != deathAS2) {
				System.out.println(grid);
				System.out.println(deathUC);
				System.out.println(deathAS1);
				System.out.println(deathAS2);
				System.out.println(sumUC);
				System.out.println(sumAS1);
				System.out.println(sumAS2);
			}
			System.out.println(count++);
		}
	}
	
	public static void main(String[]args) throws IOException {
//		String grid = genGrid();
//		String grid = "13,9;4,6;5,7;3,10,4,4,5,9,6,1,8,8,2,12,7,0;34,39,85,64,3,16,88;2";
//		String grid = "2,2;0,0;1,1;0,1,1,0;100,1;2";
//		String grid = "9,7;3,2;2,6;2,2,4,5,5,2,0,1,2,8,3,4;97,83,40,15,52,49;6";
//		String grid = "13,12;3,11;7,12;6,12,4,2,8,7,2,2,5,6,7,3,7,10;56,39,43,3,39,25,34;5";
//		String grid = "13,13;12,2;0,8;8,1,4,12,12,12,10,12,3,0,0,4,9,5,12,11,5,9,6,5;76,53,16,92,30,20,4,25,72,97;10";
//		String grid = "5,11;4,4;2,0;1,2,2,3,6,2,9,0,1,3;49,31,61,32,2;1";
//		String grid = "3,3;0,0;2,2;0,1,2,0;100,80;2";
		
//		String grid = "5,11;4,2;10,0;8,4,0,1,1,4,1,0,5,0,1,2,9,2,2,2,5,4;57,71,60,13,15,96,27,70,67;4";
//		String grid = "7,8;1,3;2,5;6,5,3,4,3,2,2,1,0,1,4,5,3,6;72,76,42,6,59,24,16;3";
//		String grid = "8,10;0,4;0,3;2,2,0,5,2,0,6,4,2,6;37,94,53,45,87;1";
		
//		String grid = "14,10;6,11;1,1;1,5,7,10,6,3,9,12,5,2,7,9,3,6,0,2,5,7;75,38,3,35,31,26,53,52,51;8"; //gr1
//		String grid = "9,13;3,2;5,3;6,6,1,2,2,4,10,2,10,5,7,7,6,1;79,65,44,50,93,77,36;1";
		
		// String grid = "6,10;0,4;5,0;2,3,6,1,3,3,1,3,4,5,7,2,5,4,2,0,9,3,4,2;59,94,6,29,9,8,76,52,92,22;2";

		 long startTime = System.nanoTime();
//	 	String grid = "12,7;3,7;4,11;3,0,3,3,4,7,0,6,4,5,2,5,6,9,3,1,2,11,2,10;28,2,93,34,36,64,94,74,63,82;8"; // uc 900 as1 888
//		String grid = "6,7;4,4;6,5;4,2,5,3,6,4,5,5,6,3,4,0,1,0,0,4;15,83,38,70,43,81,3,24;2"; // uc 608 as2 600
//		String grid = "5,11;7,2;2,1;4,4,7,1,8,3,4,3,4,1,6,3,8,1,1,0,4,2,8,0;34,22,35,39,32,2,5,43,45,61;5"; // uc 678 as1 670***
//		String grid = "14,14;6,8;0,2;8,1,2,6,8,8,8,0,10,5,7,11,0,0,12,9;95,57,48,14,1,45,39,6;6"; // uc 659 as2 657 $$ optimal 651
//		String grid = "15,10;7,3;8,13;2,7,0,3,4,3,1,12,9,7,3,7;91,30,27,28,57,90;6"; // uc 528 as1 492
//		String grid = "9,6;1,2;2,7;4,2,2,1,4,6,2,2,1,1;22,22,32,39,23;1"; //357
//		String grid = "13,9;4,6;5,7;3,10,4,4,5,9,6,1,8,8,2,12,7,0;34,39,85,64,3,16,88;2"; //552
//		String grid = "13,9;2,5;0,2;0,9,6,10,7,6,8,2,1,10;91,12,42,63,41;2"; //425 2
//		String grid = "6,5;4,3;2,3;1,1,1,2,4,5,0,4,0,5,3,5;35,36,92,24,38,91;1"; // 505 1
		 
		 // TLE UC
		String grid = "11,11;7,7;8,8;9,7,7,4,7,6,9,6,9,5,9,1,4,5,3,10,5,10;14,3,96,89,61,22,17,70,83;5";
//		String grid = "15,15;5,10;14,14;0,0,0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,8;81,13,40,38,52,63,66,36,13;1";
		 
		String output = solve(grid, "UC", true);
		System.out.println(output);
		System.out.println(" "+sum(output.split(";")[output.split(";").length-2]));
		output = solve(grid, "AS1", false);
		System.out.println(output);
		System.out.println(" "+sum(output.split(";")[output.split(";").length-2]));
		output = solve(grid, "AS2", false);
		System.out.println(output);
		System.out.println(" "+sum(output.split(";")[output.split(";").length-2]));
//		 checkOptimality();
		
//		 System.out.print(output.split(";")[output.split(";").length-3]);
//		 System.out.print(" "+sum(output.split(";")[output.split(";").length-2]));
//		 System.out.print(" "+ output.split(";")[output.split(";").length-1]);
//		 System.out.println();
//		 output = solve(grid, "AS1", false);
//		 System.out.println(output);
//		 System.out.print(output.split(";")[output.split(";").length-3]);
//		 System.out.print(" "+sum(output.split(";")[output.split(";").length-2]));
//		 System.out.print(" "+output.split(";")[output.split(";").length-1]);
//		 System.out.println();
//		 output = solve(grid, "AS2", false);
//		 System.out.println(output);
//		 System.out.print(output.split(";")[output.split(";").length-3]);
//		 System.out.print(" "+sum(output.split(";")[output.split(";").length-2]));
//		 System.out.print(" "+output.split(";")[output.split(";").length-1]);
//		 System.out.println();
//		 output = solve(grid, "GR1", false);
//		 System.out.println(output);
//		 System.out.print(output.split(";")[output.split(";").length-3]);
//		 System.out.print(" "+sum(output.split(";")[output.split(";").length-2]));
//		 System.out.print(" "+output.split(";")[output.split(";").length-1]);
//		 System.out.println();
//		 output = solve(grid, "GR2", false);
//		 System.out.println(output);
//		 System.out.print(output.split(";")[output.split(";").length-3]);
//		 System.out.print(" "+sum(output.split(";")[output.split(";").length-2]));
//		 System.out.print(" "+output.split(";")[output.split(";").length-1]);
//		 System.out.println();
//		 output = solve(grid, "DF", false);
//		 System.out.println(output);
//		 System.out.print(output.split(";")[output.split(";").length-3]);
//		 System.out.print(" "+sum(output.split(";")[output.split(";").length-2]));
//		 System.out.print(" "+output.split(";")[output.split(";").length-1]);
//		 System.out.println();
//		 output = solve(grid, "ID", false);
//		 System.out.println(output);
//		 System.out.print(output.split(";")[output.split(";").length-3]);
//		 System.out.print(" "+sum(output.split(";")[output.split(";").length-2]));
//		 System.out.print(" "+output.split(";")[output.split(";").length-1]);
//		 System.out.println();
//		 output = solve(grid, "BF", false);
//		 System.out.println(output);
//		 System.out.print(output.split(";")[output.split(";").length-3]);
//		 System.out.print(" "+sum(output.split(";")[output.split(";").length-2]));
//		 System.out.print(" "+output.split(";")[output.split(";").length-1]);
//		 System.out.println();
		 double totalTime = (double) (System.nanoTime()/1e9) - (startTime/(1e9));
		 System.out.println("Took: "+totalTime+" seconds!");
//		System.out.println(output);
//		System.out.println(output.split(";")[output.split(";").length-1]);
//		output = solve(grid, "AS1", false);
//		System.out.println(output);
//		System.out.println(output.split(";")[output.split(";").length-1]);
//		output = solve(grid, "AS2", false);
//		System.out.println(output);
//		System.out.println(output.split(";")[output.split(";").length-1]);
	}
	
	@Override
	public MissionImpossibleNode createSpecificNode(MissionImpossibleState s, int hCost) {
		s.resetState();
		return new MissionImpossibleNode(s, hCost);
	}
	public MissionImpossibleNode createSpecificNode(MissionImpossibleState state, MissionImpossibleNode parentNode, String operation, int depth, int gCost, int hCost) {
		return new MissionImpossibleNode(state, parentNode, operation, depth, gCost, hCost);
	}
	
}
