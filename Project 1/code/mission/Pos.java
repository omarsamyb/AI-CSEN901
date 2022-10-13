package code.mission;

public class Pos 
{
	int x;
	int y;
	Pos(int x, int y)
	{
		this.x=x;
		this.y=y;
	}
	double getEucledianDistance(Pos p2){ return Math.sqrt((p2.x-this.x)*(p2.x-this.x) + (p2.y-this.y)*(p2.y-this.y)); }
	double getManhatanDistance(Pos p2){ return Math.abs((p2.x-this.x)) + Math.abs((p2.y-this.y)); }
	public boolean equals(Pos p2) { return this.x==p2.x && this.y==p2.y; }
	public String toString() { return "["+x+", "+y+"]"; }
	static Pos parseStringArr(String[] posStr) {return new Pos(Integer.parseInt(posStr[0]), Integer.parseInt(posStr[1]));}
}
