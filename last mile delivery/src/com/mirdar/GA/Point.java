package com.mirdar.GA;

import com.mirdar.O2O.Order;

public class Point {

	public String pointName;//���͵����֣��붩��id��Ӧ
	public double lon;
	public double lan;
	public int pointId; //���͵����
	public int goods_num; //��Ʒ����
	public String order_id;
	
//	public int dis = 0;
//	public int arriTime = 0;
//	public int deparTime = 0;
//	public int stayTime = (int)Math.round(3*Math.sqrt(goods_num)+5);
//	public Order o2o_order;
//	public int flag = 0; //��Ϊ1ʱ����������͵����o2o����
//	
//	public void addO2o(Order order,int dis)
//	{
//		this.o2o_order = order;
//		flag = 1;
//		arriTime = order.pickup_time - dis-(int)Math.round(3*Math.sqrt(goods_num)+5);
//		deparTime = order.pickup_time+order.time+order.stay_time;
//		goods_num = goods_num + order.num;
//		this.dis = dis;
//		stayTime = deparTime - arriTime;
//	}
	public String getPointName()
	{
		return pointName;
	}
	public void setPointName(String pointName)
	{
		this.pointName = pointName;
	}
	public double getLon()
	{
		return lon;
	}
	public void setLon(double lon)
	{
		this.lon = lon;
	}
	public double getLan()
	{
		return lan;
	}
	public void setLan(double lan)
	{
		this.lan = lan;
	}
	public int getPointId()
	{
		return pointId;
	}
	public void setPointId(int pointId)
	{
		this.pointId = pointId;
	}
	public int getGoods_num()
	{
		return goods_num;
	}
	public void setGoods_num(int goods_num)
	{
		this.goods_num = goods_num;
	}
	
}
