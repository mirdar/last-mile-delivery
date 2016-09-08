package com.mirdar.O2O2;


public class Edge2 implements Comparable<Edge2>{

	public Vertex start;
	public Vertex dest;
	public int cost;
	public int flag;
	public int isLeave; //用来判断该边是否保留
//	public int tranformTime;
	public Edge2(Vertex s, Vertex d, int c,int flag)
	{
		start = s;
		dest = d;
		cost = c;
		this.flag = flag;
	}
	public Edge2(Vertex d, int c)
	{
		dest = d;
		cost = c;
	}
	
	/*public int compareTo(Edge e)
	{
		int otherCost = e.cost;
		
		return cost < otherCost ? -1 : cost > otherCost ? 1 : 0;
	}*/
	public int compareTo(Edge2 e)
	{
		int otherCost = e.cost;
		
		return cost < otherCost ? 1 : cost > otherCost ? -1 : 0;
	}
	
//	public Edge(Vertex d, int c,int tranformTime)
//	{
//		dest = d;
//		cost = c;
//		this.tranformTime = tranformTime;
//	}
}
