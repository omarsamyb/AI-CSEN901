package code.generic;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
public class QueuingFunction<N, S>
{
	Stack<N> a;
	Queue<N> b;
	public QueuingFunction(String queuingStrategy)
	{
		if(queuingStrategy.equals("ENQUEUE-AT-END")) b = new LinkedList<N>();
		else if(queuingStrategy.equals("ORDERED-INSERT")) b = new PriorityQueue<N>();
		else if(queuingStrategy.equals("ENQUEUE-AT-FRONT"))	a = new Stack<N>();
	}
	void enque(N p)
	{
		if(a!=null) a.push(p);
		else if(b!=null) b.add(p);
	}
	N deque()
	{
		if(a!=null) return a.pop();
		else return b.remove();
	}
	public boolean isEmpty()
	{
		if(a!=null) return a.isEmpty();
		else return b.isEmpty();
	}
}
