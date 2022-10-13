package code.mission;

import java.util.ArrayList;
import java.util.HashSet;

import code.generic.State;

public class MissionImpossibleState extends State{
	Pos ethanPos;
	ArrayList<Member> membersList;
	int remainingSeats;
	
	MissionImpossibleState(Pos ethanPos, ArrayList<Member> membersList, int remainingSeats){
		this.ethanPos = new Pos(ethanPos.x, ethanPos.y);
		this.membersList = deepCloneList(membersList);
		this.remainingSeats = remainingSeats;
	}
	
	ArrayList<Member> deepCloneList(ArrayList<Member> init)
	{
		ArrayList<Member> newList = new ArrayList<Member>();
		for(Member m:init)
			newList.add(new Member(m.pos, m.health, m.isCarried, m.isSaved));
		return newList;
	}
	
	void decreaseHealthAll() 
	{
		for(int i=0;i<membersList.size();i++)
			if(!membersList.get(i).isCarried && !membersList.get(i).isSaved && membersList.get(i).health<100) {
				membersList.get(i).health+=2;
				if(membersList.get(i).health > 100)
					membersList.get(i).health = 100;
			}
	}

	@Override
	public String toString() {
		return "Ethan is located in pos: "+ethanPos+ ", the members information is as follows: " + membersList 
				+ " and there are " + remainingSeats + " remaining seats.";
	}
	public void setStateVisited() {
		MissionImpossible.visitedStatesHashset.add(this);
	}
	public void setStateVisited2() {
		String s="";
		for(Member member: membersList) {
			if(!member.isCarried && !member.isSaved) s+= 0;
			else if(member.isCarried) s+=1;
			else s+=2;
		}
		int position = Integer.parseInt(s, 3);
		MissionImpossible.visitedStates[ethanPos.x][ethanPos.y][position] = true;
	}
	public boolean stateExists() {
		return MissionImpossible.visitedStatesHashset.contains(this);
	}
	public boolean stateExists2() {
		String s="";
		for(Member member: membersList) {
			if(!member.isCarried && !member.isSaved) s+= 0;
			else if(member.isCarried) s+=1;
			else s+=2;
		}
		int position = Integer.parseInt(s, 3);
		
		return MissionImpossible.visitedStates[ethanPos.x][ethanPos.y][position];
	}

	@Override
	public void resetState() {
		MissionImpossible.visitedStates = new boolean[15][15][59049];
		MissionImpossible.visitedStatesHashset = new HashSet<MissionImpossibleState>();
	}
	
	public int hashCode() {
		String hash = "" + ethanPos.x + " " + ethanPos.y+ " ";
		String s = "";
		int health = 0;
		for(Member member :membersList) {
			if(!member.isCarried && !member.isSaved) s+= 0;
			else if(member.isCarried) s+=1;
			else s+=2;
			hash += s + " ";
			if((member.isCarried && !member.isSaved) || (!member.isCarried && member.isSaved)) {
				health += member.health;
			}
		}
		hash += health;
		return hash.hashCode();
	}
	
	public boolean equals(Object o) {
		MissionImpossibleState state = (MissionImpossibleState) o;
		int health1 = 0;
		int health2 = 0;
		if(!state.ethanPos.equals(ethanPos))
			return false;
		for(int i=0;i<state.membersList.size();i++) {
			if(state.membersList.get(i).isCarried != membersList.get(i).isCarried)
				return false;
			if(state.membersList.get(i).isSaved != membersList.get(i).isSaved)
				return false;
			if(((membersList.get(i).isCarried && !membersList.get(i).isSaved) || (!membersList.get(i).isCarried && membersList.get(i).isSaved))) {
				//return false;
				health1 += membersList.get(i).health;
				health2 += state.membersList.get(i).health;
			}
		}
		if(health1 != health2 )
			return false;
		return true;
	}
}
