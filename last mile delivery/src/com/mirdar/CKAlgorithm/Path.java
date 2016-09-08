package com.mirdar.CKAlgorithm;

import java.util.Map;

import com.mirdar.GA.Point;

public class Path
{
	public int start;
	public int end;

	public int cost;
	public int flag = 0; // 分为0,1，0代表可以继续访问，1代表不能访问

	public void init(int i, int j, Dis dis, Map<Integer, Point> dataMap)
	{
		start = i;
		end = j;
		// cost = (int) (0.2 * dis.disPlace(dataMap.get(0).pointName,
		// dataMap.get(i).pointName)
		// + 0.3 * dis.disPlace(dataMap.get(0).pointName,
		// dataMap.get(j).pointName)
		// - 0.5 * dis.disPlace(dataMap.get(i).pointName,
		// dataMap.get(j).pointName));

		cost = dis.disPlace(dataMap.get(0).pointName, dataMap.get(i).pointName)
				+ dis.disPlace(dataMap.get(0).pointName, dataMap.get(j).pointName)
				- dis.disPlace(dataMap.get(i).pointName, dataMap.get(j).pointName);

		// cost = dis.disPlace(dataMap.get(0).pointName,
		// dataMap.get(j).pointName)
		// - dis.disPlace(dataMap.get(i).pointName,
		// dataMap.get(j).pointName);
	}

}
