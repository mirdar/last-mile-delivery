package com.mirdar.graph;

import java.util.ArrayList;

import com.mirdar.GA.Record;
import com.mirdar.test.Place;

public class Edge
{

	// 起点终点的信息
	public Place start;
	public Place end;
	public int lineTime; // 路线的长度
	public int flag = 0; // 为0就没有遍历，1就遍历过了
	public int lastArriveTime = 0;
	public ArrayList<Record> line; // 用来保存完整的路线

	public Place nearSite; // 最近的网点
	public int dis; // 这条路线与最近的网点的距离
}
