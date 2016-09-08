package com.mirdar.CKAlgorithm;

import java.util.ArrayList;

public class Line
{
	public int start;
	public int end;
	public ArrayList<Integer> transitPoint = new ArrayList<Integer>();

	public void init()
	{
		transitPoint = new ArrayList<Integer>();
		transitPoint.add(start);
	}
}
