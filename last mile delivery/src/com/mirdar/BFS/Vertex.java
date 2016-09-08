package com.mirdar.BFS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Vertex
{

	public String name;
	public List<Edge> adj;
	public int dist;
	public int cost;
	public Vertex prev;
	// Dijkstra算法中用来检测该该顶点是否被访问过
	public int scratch;
	int flag = 0; // 用来区分网店与配送点
	int transformDis = 0;
	public int bindorNot = 0;
	public ArrayList<String> path = new ArrayList<String>();
	public int arriveTime = 0;

	public Vertex(String nm)
	{
		name = nm;
		adj = new LinkedList<Edge>();
		reset();
	}

	public void reset()
	{
		dist = GraphAlgorithm.INFINITY;
		prev = null;
		scratch = 0;
		transformDis = 0;
		// flag = 0;
	}
}
