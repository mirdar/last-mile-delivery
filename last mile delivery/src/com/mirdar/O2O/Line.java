package com.mirdar.O2O;

import java.util.ArrayList;

import com.mirdar.graph.Edge;

public class Line {

	public ArrayList<Record> line = new ArrayList<Record>();
	public int earliestTime;
	public int lastTime; // o2o路线最晚可以什么时候到，这里不对，这个变量不使用
	public int arriveTime; // 快递员什么时候到达
	public ArrayList<com.mirdar.GA.Line> branchToO2oLine = new ArrayList<com.mirdar.GA.Line>(); // 快递员的电商路线
	public ArrayList<Edge> edgeLine = new ArrayList<Edge>();
	public String shop_id; // 初始网点名称
	public String spot_id; // 记录该商户快递员电商路线的终点
}
