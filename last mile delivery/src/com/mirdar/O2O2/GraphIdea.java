package com.mirdar.O2O2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import com.mirdar.CKAlgorithm.Dis;
import com.mirdar.O2O.Order;


public class GraphIdea {

	Map<String,Vertex> vertexMap = new HashMap<String,Vertex>();
	public void addEdge(String name1,String name2,int cost,Map<String,Order> orderMap)
	{
		Vertex v = getVertex(name1,orderMap.get(name1).pickup_time);
		Vertex w =getVertex(name2,orderMap.get(name2).pickup_time);
		v.adj2.add(new Edge2(v,w, cost,0));
	}
	//绑定后产生新的order
	public Map<String,Order> bind(Map<String,Order> orderMap,Dis dis)
	{
		clearAll();
		Map<String,Order> orders = new HashMap<String,Order>();
		PriorityQueue<Edge2> pq = new PriorityQueue<Edge2>();
		LinkedList<Vertex> vertexs =new LinkedList<Vertex>();
		//将所有边加入优先队列
		for(String orderName : vertexMap.keySet())
		{
			Vertex v = vertexMap.get(orderName);
			for(int i=0;i<v.adj2.size();i++)
			{
				pq.add(v.adj2.get(i));
			}
		}
		while(!pq.isEmpty())
		{
			//1 -> 3 -> 3 -> 2
			Edge2 e = pq.remove();
			if(e.start.scratch == 1 || e.dest.scratch == 2) continue;
			if(e.start.scratch == 3 || e.dest.scratch == 3) continue;
			//此时加在路段前面
			if(e.dest.scratch == 1)
			{
				if(e.cost < 0 )
					continue;
				Vertex w = null;
				int flag = 0;
				if(e.start.scratch == 0)
				{
					e.dest.prev = e.start;
					e.dest.scratch = 3;
					e.start.scratch = 1;
				}
				else
				{
					w = e.start.prev;
					//检查是否会形成环
					while(w != null)
					{
						if(w.orderName.equals(e.dest.orderName))
						{
							flag = 1;
							break;
						}
						w = w.prev;
					}
					if(flag == 1)
						continue;
					e.start.scratch = 3;
					e.dest.scratch =3;
				}
			}
			else //此时加载路段后面
			{
				if(e.start.scratch == 0)
				{
					e.dest.prev = e.start;
					e.dest.scratch = 2;
					e.start.scratch = 1;
				}
				else
				{
					e.dest.prev = e.start;
					e.dest.scratch = 2;
					e.start.scratch = 3;
				}
			}
		}
		for(String orderName : vertexMap.keySet())
		{
			//重新产生新的order
			if(vertexMap.get(orderName).scratch == 2) //单个点也加入
			{
				Order order = copy(orderMap.get(vertexMap.get(orderName).prev.orderName));
				order.order_id = orderName+1;
				Vertex v = vertexMap.get(orderName);
				ArrayList<String> orderNames = new ArrayList<String>();
				while(v != null)
				{
					orderNames.add(v.orderName);
					v = v.prev;
				}
				for(int i=orderNames.size()-1;i>=0;i--)
				{
					order.list.add(orderNames.get(i));
					System.out.print(orderMap.get(orderNames.get(i)).shop_id +" "+
							orderMap.get(orderNames.get(i)).pickup_time + " -> ");
				}
				order.shop_id = orderMap.get(orderNames.get(orderNames.size()-1)).shop_id;
				order.time = getDeliveryTime(order.list, orderMap, dis);
				for(int i=orderNames.size()-1;i>=0;i--)
				{
					order.stay_time = orderMap.get(orderNames.get(i)).stay_time;
					order.num = orderMap.get(orderNames.get(i)).num;
				}
				
				order.flag = 2;
				orders.put(order.order_id, order);
			}
			System.out.println();
			if(vertexMap.get(orderName).scratch == 0)
			{
				orders.put(orderName, orderMap.get(orderName));
			}
		}
		for(String orderName : orderMap.keySet())
		{
			if(vertexMap.get(orderName) == null)
			{
				orders.put(orderName,orderMap.get(orderName));
			}
		}
		int orderSize = 0;
		for(String orderName : orders.keySet())
		{
			if(orders.get(orderName).flag == 2)
				orderSize += 2;
			else
				orderSize += 1;
			
		}
		System.out.println(orders.size());
		System.out.println(orderSize);
		return orders;
	}
	
	public int getDeliveryTime(ArrayList<String> orders,Map<String,Order> orderMap,Dis dis)
	{
//		System.out.println();
		int time = 0;
		for(int i=0;i<orders.size();i++)
		{
			if(i == 0)
			{
				time = orderMap.get(orders.get(i)).time;
//				System.out.print(orders.get(i)+" -> ");
			}
			else
			{
				if(time + dis.disPlace(orderMap.get(orders.get(i-1)).shop_id, orderMap.get(orders.get(i)).shop_id)
				- orderMap.get(orders.get(i-1)).time < orderMap.get(orders.get(i)).pickup_time)
					time += orderMap.get(orders.get(i)).pickup_time - orderMap.get(orders.get(i-1)).pickup_time
							- orderMap.get(orders.get(i-1)).time;
				else
					time += dis.disPlace(orderMap.get(orders.get(i-1)).shop_id, orderMap.get(orders.get(i)).shop_id)
					- orderMap.get(orders.get(i-1)).time;
				time += orderMap.get(orders.get(i)).time;
//				System.out.print(orders.get(i)+" -> ");
			}
		}
//		System.out.println(time+" ******************************************");
		return time;
	}
	
	public Order copy(Order order)
	{
		Order orderr = new Order();
		orderr.order_id = order.order_id;
		orderr.shop_id = order.shop_id;
		orderr.spot_id = order.spot_id;
		orderr.delivery_time = order.delivery_time;
		orderr.pickup_time = order.pickup_time;
		orderr.last_time = order.last_time;
		orderr.num = order.num;
//		orderr.time = order.time;
//		orderr.stay_time = order.stay_time;
		
		return orderr;
	}
	
	private void clearAll()
	{
		for (Vertex v : vertexMap.values())
		{
			v.reset();
		}
	}
	public Vertex getVertex(String name,int t)
	{
		Vertex v = vertexMap.get(name);
		if(v == null)
		{
			v = new Vertex(name,t);
			vertexMap.put(name, v);
		}
		return v;
	}
}
