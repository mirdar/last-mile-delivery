package com.mirdar.O2O2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.mirdar.BFS.GraphAlgorithm;
import com.mirdar.O2O.Record;

public class Vertex implements Comparable<Vertex>{

	public String orderName;
	public List<Edge> adj;
	public List<Edge2> adj2;
	public int dist;
	public Vertex prev;
	public Vertex next;
	public int scratch;
	ArrayList<Record> records = new ArrayList<Record>();
	public int pickup_time = 0;
//	public int flag; //1,2,3
	
	public Vertex(String name)
	{
		orderName = name;
		adj = new LinkedList<Edge>();
		reset();
	}
	
	public Vertex(String name,int t)
	{
		orderName = name;
		adj = new LinkedList<Edge>();
		adj2 = new LinkedList<Edge2>();
		pickup_time = t;
		reset();
	}
	
	public void reset()
	{
		dist = GraphAlgorithm.INFINITY;
		prev = null;
		next = null;
		scratch = 0;
//		flag = 0;
	}
	public int compareTo(Vertex e)
	{
		int otherCost = e.pickup_time;
		
		return pickup_time < otherCost ? -1 : pickup_time > otherCost ? 1 : 0;
	}
}
