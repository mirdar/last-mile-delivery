package com.mirdar.GA;

import java.util.ArrayList;

public class Line {

	public ArrayList<Record> line = new ArrayList<Record>();
	public int time; //该线路的时间开销
	public int endToStart; //从终点回到网点的时间
	public int tempTime; //用于背包问题
	public String spot_id; //终点的名称
	public int flag = 0; //当加入了o2o订单后就为1
}
