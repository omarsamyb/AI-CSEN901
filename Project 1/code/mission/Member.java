package code.mission;

public class Member implements Comparable<Member>
{
	public Pos pos;
	public int health;
	public boolean isCarried;
	public boolean isSaved;
	public Member(Pos pos, int health, boolean isCarried, boolean isSaved) 
	{
		this.pos = new Pos(pos.x,pos.y);
		this.health = health;
		this.isCarried = isCarried;
		this.isSaved = isSaved;
	}
	public Member(Pos pos, int health) 
	{
		this.pos = new Pos(pos.x,pos.y);
		this.health = health;
		this.isCarried = false;
		this.isSaved = false;
	}
	@Override
	public int compareTo(Member o) {
		return o.health - health;
	}
}
