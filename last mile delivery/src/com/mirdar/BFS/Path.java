package com.mirdar.BFS;

public class Path implements Comparable<Path>{

	public Vertex dest;
	public int cost;
	public int transformTime;
	
	public Path(Vertex d,int c)
	{
		dest = d;
		cost = c;
	}
	
	public Path(Vertex d,int c,int tranformTime)
	{
		dest = d;
		cost = c;
		this.transformTime = tranformTime;
	}
	
	public int compareTo(Path rhs)
	{
		int otherCost = rhs.transformTime;
		
		return transformTime < otherCost ? -1 : transformTime > otherCost ? 1 : 0;
	}
}
