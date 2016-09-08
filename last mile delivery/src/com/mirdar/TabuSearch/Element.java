package com.mirdar.TabuSearch;

public class Element {

	public int elementID;
	public String start;
	public String end;
	public int lineTime;
	public com.mirdar.GA.Line line;

	public void init(int elementId, com.mirdar.GA.Line line)
	{
		this.elementID = elementId;
		this.start = line.line.get(0).place_id;
		this.end = line.line.get(line.line.size() - 1).place_id;
		this.line = line;
	}
}
