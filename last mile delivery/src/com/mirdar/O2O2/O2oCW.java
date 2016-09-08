package com.mirdar.O2O2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import com.mirdar.CKAlgorithm.Dis;
import com.mirdar.O2O.Order;
import com.mirdar.O2O.ReadData;

//合并路线长度为2
public class O2oCW {
	
	int save;
	public Map<String, Vertex> vertexMap = new HashMap<String, Vertex>();
	
	public static  void main(String[] args)
	{
		O2oCW o2oCW = new O2oCW();
		Dis dis = new Dis();
		ReadData readData = new ReadData();
		
		String fileShop = "F:\\ML\\last mile delivery\\part 2/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery\\part 2/spot.csv";
		String fileSite = "F:\\ML\\last mile delivery\\part 2/site.csv";
		String fileOrder = "F:\\ML\\last mile delivery\\part 2/o2o_data.csv";
		
		dis.placeMap = readData.readPlace(fileSite, dis.placeMap);
		dis.placeMap = readData.readPlace(fileSpot, dis.placeMap);
		dis.placeMap = readData.readPlace(fileShop, dis.placeMap);
		
		Map<String,Order> orders = readData.readO2oOrder(fileOrder);
		o2oCW.graph(orders, dis);
		ArrayList<Edge> edges = o2oCW.bindOperate();
		o2oCW.O2oOrder(edges);
		o2oCW.save(orders,dis,edges);
	}
	
	public void save(Map<String,Order> orders,Dis dis,ArrayList<Edge> edges)
	{
		for(int i=0;i<edges.size();i++)
		{
			if(edges.get(i).flag != 1)
			{
				chooseBindWay(orders.get(edges.get(i).start.orderName),
							orders.get(edges.get(i).dest.orderName), dis, 1);
			}
		}
		System.out.println("save： "+save);
	}
	
	public void O2oOrder(ArrayList<Edge> edges)
	{
		int flag1 = 0;
		int flag2 = 0;
		int flag3 = 0;
		int bind = 0;
		for(int i=0;i<edges.size();i++)
		{
			System.out.println("e.cost: "+edges.get(i).cost);
			if(edges.get(i).flag == 1)
			{
				flag1++;
			}
			if(edges.get(i).flag == 2)
			{
				flag2++;
			}
			if(edges.get(i).flag == 3)
			{
				flag3++;
			}
		}
		int single = 0;
		for(String orderName : vertexMap.keySet())
		{
			if(vertexMap.get(orderName).scratch == 0)
				single++;
		}
		System.out.println(flag1); //623
		System.out.println(flag2); //298
		System.out.println(flag3); //634
		System.out.println(single); //295+12
	}
	
	//绑定操作
	public ArrayList<Edge> bindOperate()
	{
		PriorityQueue<Edge> pq = new PriorityQueue<Edge>();
		ArrayList<Edge> edges = new ArrayList<Edge>();
//		Map<String,Order> out = new HashMap<String,Order>();
		//将所有边加入优先队列
		for(String orderName : vertexMap.keySet())
		{
			Vertex v = vertexMap.get(orderName);
			for(int i=0;i<v.adj.size();i++)
			{
				pq.add(v.adj.get(i));
			}
		}
		System.out.println(pq.size());
		while(!pq.isEmpty())
		{
			Edge e = pq.remove();
			if(e.start.scratch == 1 || e.dest.scratch == 1) continue;
			else
			{
				e.isLeave = 1; //这条边保留
				e.start.scratch = 1;
				e.dest.scratch = 1;
				edges.add(e);
			}
		}
		System.out.println("edges.size: "+edges.size());
		return edges;
	}
	public void createPath(Map<String, Vertex> vertexMaps)
	{
		PriorityQueue<Edge> pq = new PriorityQueue<Edge>();
		ArrayList<Edge> edges = new ArrayList<Edge>();
//		Map<String,Order> out = new HashMap<String,Order>();
		//将所有边加入优先队列
		for(String orderName : vertexMaps.keySet())
		{
			Vertex v = vertexMaps.get(orderName);
			for(int i=0;i<v.adj.size();i++)
			{
				pq.add(v.adj.get(i));
			}
		}
		if(!pq.isEmpty())
		{
			Edge e = pq.remove();
		}
	}
	
	//生成图
	public void graph(Map<String,Order> orders,Dis dis)
	{
		System.out.println("order.size: "+orders.size());
		
		for(String orderName1 : orders.keySet())
		{
			if(orders.get(orderName1).time > 90) continue; //shop -> spot时间>90，不进行配送
			for(String orderName2 : orders.keySet())
			{
				//有向边从时间小的指向大的
				if(orders.get(orderName1).pickup_time < orders.get(orderName2).pickup_time) continue;
				if(orderName1.equals(orderName2)) continue;
				double d = dis.disPlace(orders.get(orderName1).shop_id, orders.get(orderName2).shop_id);
				//shop之间距离太远 ，pass
				if(d > 30) continue;
				//order之间pickup_time间隔时间太久， pass
				if(orders.get(orderName2).pickup_time-orders.get(orderName1).pickup_time > 30) continue;
				//仿照graph生成图，且每一条边有一个flag，取值1,2,3
				int[] result = chooseBindWay(orders.get(orderName1), orders.get(orderName2), dis,0);
				
				Vertex v = getVertex(orderName1);
				Vertex w = getVertex(orderName2);
				v.adj.add(new Edge(v,w, result[0],result[1])); //result[1]为flag
				
			}
		}
		System.out.println("vertexMap.size: "+vertexMap.size());
	}
	
	//选择两个order的bind方式（3中），A->B的边的最小权值
	//返回一个2个元素的数值，第一个为cost，第二个为边的类型
	public int[] chooseBindWay(Order order1, Order order2,Dis dis,int flag)
	{
		int cost1 = 0;
		int cost2 = 0;
		int cost3 = 0;
		int[] result = new int[2];
		cost1 += order1.time + dis.disPlace(order1.spot_id, order2.shop_id);
		if(cost1 + order1.pickup_time < order2.pickup_time)
			cost1 = order2.pickup_time - order1.pickup_time + order2.time;
		else
		{
			cost1 = cost1 + 5*Math.abs(order2.pickup_time-(order1.pickup_time+cost1)) + order2.time;
			if(flag == 1)
				System.out.println("penaty");
		}
		
		cost2 += dis.disPlace(order1.shop_id, order2.shop_id);
		if(cost2 + order1.pickup_time < order2.pickup_time)
			cost2 = order2.pickup_time - order1.pickup_time + 
					dis.disPlace(order2.shop_id, order1.spot_id)
					+ dis.disPlace(order1.spot_id, order2.spot_id);
		else
		{
			cost2 = cost2 + 5*Math.abs(order2.pickup_time-(order1.pickup_time+cost2))+ 
							dis.disPlace(order2.shop_id, order1.spot_id)
							+ dis.disPlace(order1.spot_id, order2.spot_id);
			if(flag == 1)
				System.out.println("penaty");
		}
		
		cost3 += dis.disPlace(order1.shop_id, order2.shop_id);
		if(cost3 + order1.pickup_time < order2.pickup_time)
			cost3 = order2.pickup_time - order1.pickup_time + 
					order2.time
					+ dis.disPlace(order2.spot_id,order1.spot_id);
		else
		{
			cost3 = cost3 + 5*Math.abs(order2.pickup_time-(order1.pickup_time+cost3))+ 
							order2.time
							+ dis.disPlace(order2.spot_id,order1.spot_id);
			if(flag == 1)
				System.out.println("penaty");
		}
		
		if(cost1 < cost2)
			if(cost1 < cost3)
			{
				result[0] = cost1;
				result[1] = 1;
				return result;
			}
			else
			{
				result[0] = cost3;
				result[1] = 3;
				if(flag == 1)
					save += cost1 - cost3;
				return result;
			}
		else if(cost2 < cost3)
		{
			result[0] = cost2;
			result[1] = 2;
			if(flag == 1)
				save += cost1 - cost2;
			return result;
		}
		else 
		{
			result[0] = cost3;
			result[1] = 3;
			if(flag == 1)
				save += cost1 - cost3;
			return result;
		}
	}
	
	private Vertex getVertex(String vertexName)
	{
		Vertex v = vertexMap.get(vertexName);
		if (v == null)
		{
			v = new Vertex(vertexName);
			vertexMap.put(vertexName, v);
		}

		return v;
	}
}
