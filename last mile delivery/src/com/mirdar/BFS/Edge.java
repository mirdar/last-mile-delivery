package com.mirdar.BFS;

public class Edge {

	public Vertex dest;
	public int cost;
	public int tranformTime;
	public Edge(Vertex d, int c)
	{
		dest = d;
		cost = c;
	}
	public Edge(Vertex d, int c,int tranformTime)
	{
		dest = d;
		cost = c;
		this.tranformTime = tranformTime;
	}
}
