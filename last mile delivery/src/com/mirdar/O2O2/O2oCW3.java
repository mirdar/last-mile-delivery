package com.mirdar.O2O2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import com.mirdar.CKAlgorithm.Dis;
import com.mirdar.O2O.Line;
import com.mirdar.O2O.Order;
import com.mirdar.O2O.ReadData;
import com.mirdar.O2O.Record;

//合并路线长度为2
public class O2oCW3 {
	
	int save;
	public Map<String, Vertex> vertexMap = new HashMap<String, Vertex>();
	public Map<String,Order> orders = new HashMap<String,Order>();
	public Map<String,Order> orderss = new HashMap<String,Order>();
	public ArrayList<Line> lines = new ArrayList<Line>(); // 将每条路线保存下来
	
	public static  void main(String[] args)
	{
		O2oCW3 o2oCW = new O2oCW3();
		Dis dis = new Dis();
		ReadData readData = new ReadData();
		
		String fileShop = "F:\\ML\\last mile delivery\\part 2/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery\\part 2/spot.csv";
		String fileSite = "F:\\ML\\last mile delivery\\part 2/site.csv";
		String fileOrder = "F:\\ML\\last mile delivery\\part 2/o2o_data2.csv";
		
		dis.placeMap = readData.readPlace(fileSite, dis.placeMap);
		dis.placeMap = readData.readPlace(fileSpot, dis.placeMap);
		dis.placeMap = readData.readPlace(fileShop, dis.placeMap);
		
		Map<String,Order> orders = readData.readO2oOrder(fileOrder);
		o2oCW.orderss = orders;
		O2OIdea o2o = new O2OIdea();
		o2o.orderMap = orders;
		o2o.orders = o2o.readOrder(fileOrder);
		Map<String,Order> orderM = o2o.bind(dis);
		o2oCW.orders = orderM;
		System.out.println(orderM.size());
		o2oCW.graph(orderM, dis);
		ArrayList<Vertex> edges = o2oCW.bindOperate(dis);
		o2oCW.edgeNum(edges);
		o2oCW.printLines(dis);
		System.out.println("lines.size: "+o2oCW.lines.size());
		o2oCW.o2oTime();
	}
	
	public void edgeNum(ArrayList<Vertex> vs)
	{
		int num = 0;
		for(int i=0;i<vs.size();i++)
		{
			Vertex v = vs.get(i);
			while(v != null)
			{
				num++;
				v = v.prev;
			}
		}
		System.out.println("num: "+num);
	}
	//v为dest，t为lineTime(start)+dis(start,dest)
	public int haveOverNinty(Vertex v,Dis dis,int flag,int t)
	{
		ArrayList<String> orderNames = new ArrayList<String>();
		while(v.next != null)
		{
			v = v.next;
		}
		while(v != null)
		{
			orderNames.add(v.orderName);
			v = v.prev;
		}
		int time = 0;
		int arriveTime = t;
		for(int i=orderNames.size()-1;i>=0;i--)
		{
			if(i == orderNames.size()-1)
			{
				if(orders.get(orderNames.get(i)).flag == 2)
				{
//					line.shop_id = orders.get(orderNames.get(i)).shop_id;
					ArrayList<String> list = orders.get(orderNames.get(i)).list;
					for(int j=0;j<list.size();j++)
					{
						if(j == 0)
						{
							if(arriveTime < orderss.get(list.get(j)).pickup_time)
								return time;
							else
							{
								time += 5 * Math.abs(arriveTime - orderss.get(list.get(j)).pickup_time);
								if(orderss.get(list.get(j)).time >= 90)
									time *= 2;
							}
						}
						else
						{
							if(arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id) < orderss.get(list.get(j)).pickup_time)
								return time;
							else
							{
								time += 5 * Math.abs(arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id) - orderss.get(list.get(j)).pickup_time);
								arriveTime = arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id);
								if(orderss.get(list.get(j)).time >= 90)
									time *= 2;
							}
						}
					}
					for(int j=0;j<list.size();j++)
					{
						if(j == 0)
							arriveTime = arriveTime + orderss.get(list.get(list.size()-1)).time;
						arriveTime += orderss.get(list.get(j)).stay_time;
					}
				}
				else
				{
					if(arriveTime < orders.get(orderNames.get(i)).pickup_time)
						return 0;
					else
					{
						time += 5 * Math.abs(arriveTime - orders.get(orderNames.get(i)).pickup_time);
						if(orderss.get(orderNames.get(i)).time >= 90)
							time *= 2;
					}
				} 
			}
			if(i < orderNames.size()-1)
			{
				if(orders.get(orderNames.get(i)).flag == 2)
				{
//					line.shop_id = orders.get(orderNames.get(i)).shop_id;
					ArrayList<String> list = orders.get(orderNames.get(i)).list;
					for(int j=0;j<list.size();j++)
					{
						if(j == 0)
						{
							if(arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) 
											< orderss.get(list.get(j)).pickup_time )
								return time;
							else
							{
								time += 5 * Math.abs(arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) 
								- orderss.get(list.get(j)).pickup_time );
								arriveTime += dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) ;
								if(orderss.get(list.get(j)).time >= 90)
									time *= 2;
							}
						}
						else
						{
							if(arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id) < orderss.get(list.get(j)).pickup_time)
								return time;
							else
							{
								time += 5 * Math.abs(arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id) - orderss.get(list.get(j)).pickup_time);
								arriveTime = arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id);
								if(orderss.get(list.get(j)).time >= 90)
									time *= 2;
							}
						}
					}
					for(int j=0;j<list.size();j++)
					{
						if(j == 0)
							arriveTime = arriveTime + orderss.get(list.get(list.size()-1)).time;
						if( i != 0 )
							arriveTime += orderss.get(list.get(j)).stay_time;
						//两种选择，1，表示要加入最后spot的停留时间
						if(flag == 1)
							arriveTime += orderss.get(list.get(j)).stay_time;
					}
				}
				else if(arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) 
							< orders.get(orderNames.get(i)).pickup_time)
				{
					return time;
				}
				else
				{
					time += 5*Math.abs(arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) 
					- orders.get(orderNames.get(i)).pickup_time);
					if( i != 0 )
						arriveTime += orders.get(orderNames.get(i)).time+orders.get(orderNames.get(i)).stay_time;
					else
						arriveTime += orders.get(orderNames.get(i)).time;
					if(flag == 1)
						arriveTime += orders.get(orderNames.get(i)).stay_time;
					if(orderss.get(orderNames.get(i)).time >= 90)
						time *= 2;
				}
			}
		}
		return time;
	}
	
	//类似最小生成树算法
	public ArrayList<Vertex> bindOperate(Dis dis)
	{
		PriorityQueue<Edge> pq = new PriorityQueue<Edge>();
		ArrayList<Vertex> edges = new ArrayList<Vertex>();
		ArrayList<LinkedList<Vertex>> vs =new ArrayList<LinkedList<Vertex>>();
		LinkedList<Vertex> vertexs =new LinkedList<Vertex>();
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
		System.out.println("pq.size: "+pq.size());
		//这里逻辑不对
		
		while(!pq.isEmpty())
		{
			Edge e = pq.remove();
			if(e.start.scratch > 1) continue;
			else if(e.dest.scratch == 1 || e.dest.scratch == 3 || e.dest.scratch == 4) continue;
			else if(orders.get(e.dest.orderName).flag == 2 && lineTime(e.start, dis, 1) > orders.get(e.dest.orderName).pickup_time) continue;
			else if(orders.get(e.dest.orderName).time > 90 && lineTime(e.start, dis, 1) > orders.get(e.dest.orderName).pickup_time) continue;
			if(haveOverNinty(e.dest, dis, 1, lineTime(e.start, dis, 1)+dis.disPlace(orders.get(e.start.orderName).spot_id, orders.get(e.dest.orderName).shop_id)) > 30)
				continue;
			else
			{
				Vertex v = null;
				int flag = 0;
				if(e.dest.scratch == 2 && (e.start.scratch == 0 || e.start.scratch == 1))
				{
					Vertex w = null;
					if(e.start.scratch == 1)
					{
						w = e.start.prev;
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
					else
					{
						e.start.scratch = 2;
						e.dest.scratch = 3;
					}
					e.dest.prev = e.start;
					e.start.next = e.dest;
					v = e.dest;
				}
				if(e.dest.scratch == 0 && (e.start.scratch == 0 || e.start.scratch == 1))
				{
					vertexs =new LinkedList<Vertex>();
					if(e.start.scratch == 0)
						e.start.scratch = 2;
					else
						e.start.scratch = 3;
					e.dest.scratch = 1;
					e.dest.prev = e.start;
					e.start.next = e.dest;
					v = e.dest;
				}
				while(v.next != null)
				{
					v = v.next;
				}
					
				if(lineTime(v, dis, 0) > 720 )
				{
					if(orders.get(v.orderName).time+orders.get(v.orderName).pickup_time > 720 && orders.get(v.orderName).flag == 1)
					{
						
					}
					else
					{
						v.prev.scratch = 4;
						v.scratch = 0;
						v.prev.next = null;
						v.prev = null;
					}
				}
			}
		}
		for(String orderName : vertexMap.keySet())
		{
			Vertex v = vertexMap.get(orderName);
			for(int i=0;i<v.adj.size();i++)
			{
				if(v.adj.get(i).start.scratch == 2 && v.adj.get(i).dest.scratch == 1
						|| (v.adj.get(i).start.scratch == 3 && v.adj.get(i).dest.scratch == 1))
					v.adj.get(i).dest.scratch = 4;
				
			}
		}
		ArrayList<String> leaveV = new ArrayList<String>();
		for(String orderName : vertexMap.keySet())
		{
			if(vertexMap.get(orderName).scratch == 4) //单个点也加入
			{
				edges.add(vertexMap.get(orderName));
				printPath(vertexMap.get(orderName),dis);
				System.out.println();
//				if(orders.get(orderName).time+orders.get(orderName).pickup_time > 720)
//				{
//					printPath(vertexMap.get(orderName),dis);
//					System.out.println();
//				}
			}
			if(vertexMap.get(orderName).scratch == 0)
			{
				leaveV.add(orderName);
			}
		}
		for(String orderName : orders.keySet())
		{
			if(vertexMap.get(orderName) == null)
			{
				leaveV.add(orderName);
			}
		}
		vertexToLine(leaveV,dis);
		System.out.println("edges.size: "+edges.size());
		System.out.println("leaveV.size: "+leaveV.size());
		return edges;
	}
	
	public int lineTime(Vertex v,Dis dis,int flag)
	{
		ArrayList<String> orderNames = new ArrayList<String>();
		while(v != null)
		{
			orderNames.add(v.orderName);
			v = v.prev;
		}
		int arriveTime = 0;
		for(int i=orderNames.size()-1;i>=0;i--)
		{
			if(i == orderNames.size()-1)
			{
				if(orders.get(orderNames.get(i)).flag == 2)
				{
//					line.shop_id = orders.get(orderNames.get(i)).shop_id;
					ArrayList<String> list = orders.get(orderNames.get(i)).list;
					for(int j=0;j<list.size();j++)
					{
						if(j == 0)
						{
							arriveTime =  orderss.get(list.get(j)).pickup_time;
						}
						else
						{
							if(arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id) < orderss.get(list.get(j)).pickup_time)
								arriveTime = orderss.get(list.get(j)).pickup_time;
							else
								arriveTime = arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id);
						}
					}
					for(int j=0;j<list.size();j++)
					{
						if(j == 0)
							arriveTime = arriveTime + orderss.get(list.get(list.size()-1)).time;
						arriveTime += orderss.get(list.get(j)).stay_time;
					}
				}
				else
				{
					arriveTime = orders.get(orderNames.get(i)).pickup_time+orders.get(orderNames.get(i)).time+orders.get(orderNames.get(i)).stay_time;
				} 
			}
			if(i < orderNames.size()-1)
			{
				if(orders.get(orderNames.get(i)).flag == 2)
				{
//					line.shop_id = orders.get(orderNames.get(i)).shop_id;
					ArrayList<String> list = orders.get(orderNames.get(i)).list;
					for(int j=0;j<list.size();j++)
					{
						if(j == 0)
						{
							if(arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) 
											< orderss.get(list.get(j)).pickup_time )
								arriveTime =  orderss.get(list.get(j)).pickup_time;
							else
								arriveTime += dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) ;
						}
						else
						{
							if(arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id) < orderss.get(list.get(j)).pickup_time)
								arriveTime = orderss.get(list.get(j)).pickup_time;
							else
								arriveTime = arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id);
						}
					}
					for(int j=0;j<list.size();j++)
					{
						if(j == 0)
							arriveTime = arriveTime + orderss.get(list.get(list.size()-1)).time;
						if( i != 0 )
							arriveTime += orderss.get(list.get(j)).stay_time;
						//两种选择，1，表示要加入最后spot的停留时间
						if(flag == 1)
							arriveTime += orderss.get(list.get(j)).stay_time;
					}
				}
				else if(arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) 
							< orders.get(orderNames.get(i)).pickup_time)
				{
					if(i == 0)
						arriveTime = orders.get(orderNames.get(i)).pickup_time + orders.get(orderNames.get(i)).time;
					else
						arriveTime = orders.get(orderNames.get(i)).pickup_time + orders.get(orderNames.get(i)).time+orders.get(orderNames.get(i)).stay_time;
					if(flag == 1)
						arriveTime += orders.get(orderNames.get(i)).stay_time;
				}
				else
				{
					if( i != 0 )
						arriveTime += orders.get(orderNames.get(i)).time+orders.get(orderNames.get(i)).stay_time;
					else
						arriveTime += orders.get(orderNames.get(i)).time;
					if(flag == 1)
						arriveTime += orders.get(orderNames.get(i)).stay_time;
				}
			}
		}
		return arriveTime;
	}
	
	public void vertexToLine(ArrayList<String> leaveV,Dis dis)
	{
		
		for(int i=0;i<leaveV.size();i++)
		{
			Line line = new Line();
			lines.add(line);
			int arriveTime = 0;
			if(orders.get(leaveV.get(i)).flag == 2)
			{
//				line.shop_id = orders.get(leaveV.get(i)).shop_id;
				ArrayList<String> list = orders.get(leaveV.get(i)).list;
				for(int j=0;j<list.size();j++)
				{
					if(j == 0)
					{
						line.shop_id = orderss.get(list.get(j)).shop_id;
						Record record1 = new Record();
						record1.order_id = list.get(j);
						record1.place_id = orderss.get(list.get(j)).shop_id;
						record1.num = orderss.get(list.get(j)).num;
						record1.arriveTime = orderss.get(list.get(j)).pickup_time;
						record1.departureTime = orderss.get(list.get(j)).pickup_time;
						arriveTime = record1.departureTime;
						line.line.add(record1);
					}
					else
					{
						Record record1 = new Record();
						record1.order_id = list.get(j);
						record1.place_id = orderss.get(list.get(j)).shop_id;
						record1.num = orderss.get(list.get(j)).num;
						record1.arriveTime = arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id);
						if(record1.arriveTime < orderss.get(list.get(j)).pickup_time)
							record1.departureTime = orderss.get(list.get(j)).pickup_time;
						else
							record1.departureTime = record1.arriveTime;
						arriveTime = record1.departureTime;
						line.line.add(record1);
					}
				}
				for(int j=0;j<list.size();j++)
				{
					Record record1 = new Record();
					record1.order_id = list.get(j);
					record1.place_id = orderss.get(list.get(j)).spot_id;
					record1.num =- orderss.get(list.get(j)).num;
					if(j == 0)
						record1.arriveTime = arriveTime + orderss.get(list.get(list.size()-1)).time;
					else
						record1.arriveTime = arriveTime;
					record1.departureTime = record1.arriveTime + orderss.get(list.get(j)).stay_time;
					arriveTime = record1.departureTime;
					line.line.add(record1);
				}
			}
			else
			{
				line.shop_id = orders.get(leaveV.get(i)).shop_id;
				Record record1 = new Record();
				record1.order_id = leaveV.get(i);
				record1.place_id = orders.get(leaveV.get(i)).shop_id;
				record1.num = orders.get(leaveV.get(i)).num;
				record1.arriveTime = orders.get(leaveV.get(i)).pickup_time;
				record1.departureTime = orders.get(leaveV.get(i)).pickup_time;
				
				Record record2 = new Record();
				record2.order_id = leaveV.get(i);
				record2.place_id = orders.get(leaveV.get(i)).spot_id;
				record2.num = - orders.get(leaveV.get(i)).num;
				record2.arriveTime = orders.get(leaveV.get(i)).pickup_time+orders.get(leaveV.get(i)).time;
				record2.departureTime = orders.get(leaveV.get(i)).pickup_time+orders.get(leaveV.get(i)).time
										+orders.get(leaveV.get(i)).stay_time;
				line.line.add(record1);
				line.line.add(record2);
			}
		}
	}
	
	public void printPath(Vertex v,Dis dis)
	{
		Line line = new Line();
		lines.add(line);
		int arriveTime = 0;
		ArrayList<String> orderNames = new ArrayList<String>();
		while(v != null)
		{
			orderNames.add(v.orderName);
			v = v.prev;
		}
		for(int i=orderNames.size()-1;i>=0;i--)
		{
			if(i == orderNames.size()-1)
			{
				int times = 0;
				if(orders.get(orderNames.get(i)).flag == 2)
				{
//					line.shop_id = orders.get(orderNames.get(i)).shop_id;
					ArrayList<String> list = orders.get(orderNames.get(i)).list;
					for(int j=0;j<list.size();j++)
					{
						if(j == 0)
						{
							line.shop_id = orderss.get(list.get(j)).shop_id;
							Record record1 = new Record();
							record1.order_id = list.get(j);
							record1.place_id = orderss.get(list.get(j)).shop_id;
							record1.num = orderss.get(list.get(j)).num;
							record1.arriveTime = orderss.get(list.get(j)).pickup_time;
							record1.departureTime = orderss.get(list.get(j)).pickup_time;
							arriveTime = record1.departureTime;
							line.line.add(record1);
						}
						else
						{
							Record record1 = new Record();
							record1.order_id = list.get(j);
							record1.place_id = orderss.get(list.get(j)).shop_id;
							record1.num = orderss.get(list.get(j)).num;
							record1.arriveTime = arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id);
							if(record1.arriveTime < orderss.get(list.get(j)).pickup_time)
								record1.departureTime = orderss.get(list.get(j)).pickup_time;
							else
								record1.departureTime = record1.arriveTime;
							arriveTime = record1.departureTime;
							line.line.add(record1);
						}
					}
					for(int j=0;j<list.size();j++)
					{
						Record record1 = new Record();
						record1.order_id = list.get(j);
						record1.place_id = orderss.get(list.get(j)).spot_id;
						record1.num =- orderss.get(list.get(j)).num;
						if(j == 0)
							record1.arriveTime = arriveTime + orderss.get(list.get(list.size()-1)).time;
						else
							record1.arriveTime = arriveTime;
						record1.departureTime = record1.arriveTime + orderss.get(list.get(j)).stay_time;
						arriveTime = record1.departureTime;
						line.line.add(record1);
					}
				}
				else
				{
					line.shop_id = orders.get(orderNames.get(i)).shop_id;
					Record record1 = new Record();
					record1.order_id = orderNames.get(i);
					record1.place_id = orders.get(orderNames.get(i)).shop_id;
					record1.num = orders.get(orderNames.get(i)).num;
					record1.arriveTime = orders.get(orderNames.get(i)).pickup_time;
					
					record1.departureTime = orders.get(orderNames.get(i)).pickup_time;
					arriveTime = record1.departureTime;
					
					Record record2 = new Record();
					record2.order_id = orderNames.get(i);
					record2.place_id = orders.get(orderNames.get(i)).spot_id;
					record2.num = - orders.get(orderNames.get(i)).num;
					record2.arriveTime = arriveTime+orders.get(orderNames.get(i)).time;
					record2.departureTime = arriveTime+orders.get(orderNames.get(i)).time
											+orders.get(orderNames.get(i)).stay_time;
					line.line.add(record1);
					line.line.add(record2);
					
					arriveTime = record2.departureTime;
//					System.out.print("orderName: "+orderNames.get(i)+" pickup_time: "+orders.get(orderNames.get(i)).pickup_time
//							+" time: "+orders.get(orderNames.get(i)).time +" -> ");
				}
				
			}
			if(i < orderNames.size()-1)
			{
				if(orders.get(orderNames.get(i)).flag == 2)
				{
//					line.shop_id = orders.get(orderNames.get(i)).shop_id;
					ArrayList<String> list = orders.get(orderNames.get(i)).list;
					for(int j=0;j<list.size();j++)
					{
						if(j == 0)
						{
							line.shop_id = orderss.get(list.get(j)).shop_id;
							Record record1 = new Record();
							record1.order_id = list.get(j);
							record1.place_id = orderss.get(list.get(j)).shop_id;
							record1.num = orderss.get(list.get(j)).num;
							if(orders.get(orderNames.get(i+1)).flag == 2)
								record1.arriveTime = arriveTime + dis.disPlace(orderss.get(orders.get(orderNames.get(i+1)).list.get(orders.get(orderNames.get(i+1)).list.size()-1)).spot_id
													, orders.get(orderNames.get(i)).shop_id) ;
							else
								record1.arriveTime = arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) ;
							if(record1.arriveTime < orderss.get(list.get(j)).pickup_time)
								record1.departureTime = orderss.get(list.get(j)).pickup_time;
							else
								record1.departureTime = record1.arriveTime;
							arriveTime = record1.departureTime;
							line.line.add(record1);
						}
						else
						{
							Record record1 = new Record();
							record1.order_id = list.get(j);
							record1.place_id = orderss.get(list.get(j)).shop_id;
							record1.num = orderss.get(list.get(j)).num;
							record1.arriveTime = arriveTime + dis.disPlace(orderss.get(list.get(j-1)).shop_id, orderss.get(list.get(j)).shop_id);
							if(record1.arriveTime < orderss.get(list.get(j)).pickup_time)
								record1.departureTime = orderss.get(list.get(j)).pickup_time;
							else
								record1.departureTime = record1.arriveTime;
							arriveTime = record1.departureTime;
							line.line.add(record1);
						}
					}
					for(int j=0;j<list.size();j++)
					{
						Record record1 = new Record();
						record1.order_id = list.get(j);
						record1.place_id = orderss.get(list.get(j)).spot_id;
						record1.num =- orderss.get(list.get(j)).num;
						if(j == 0)
							record1.arriveTime = arriveTime + orderss.get(list.get(list.size()-1)).time;
						else
							record1.arriveTime = arriveTime;
						record1.departureTime = record1.arriveTime + orderss.get(list.get(j)).stay_time;
						arriveTime = record1.departureTime;
						line.line.add(record1);
					}
				}
				else
				{
					Record record1 = new Record();
					record1.order_id = orderNames.get(i);
					record1.place_id = orders.get(orderNames.get(i)).shop_id;
					record1.num = orders.get(orderNames.get(i)).num;
					if(orders.get(orderNames.get(i+1)).flag == 2)
						record1.arriveTime = arriveTime + dis.disPlace(orderss.get(orders.get(orderNames.get(i+1)).list.get(orders.get(orderNames.get(i+1)).list.size()-1)).spot_id
											, orders.get(orderNames.get(i)).shop_id) ;
					else
						record1.arriveTime = arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) ;
					if(record1.arriveTime < orders.get(orderNames.get(i)).pickup_time)
						record1.departureTime = orders.get(orderNames.get(i)).pickup_time;
					else
						record1.departureTime = record1.arriveTime;
					arriveTime = record1.departureTime;
					
					Record record2 = new Record();
					record2.order_id = orderNames.get(i);
					record2.place_id = orders.get(orderNames.get(i)).spot_id;
					record2.num = - orders.get(orderNames.get(i)).num;
					record2.arriveTime = arriveTime+orders.get(orderNames.get(i)).time;
					record2.departureTime = arriveTime+orders.get(orderNames.get(i)).time
											+orders.get(orderNames.get(i)).stay_time;
					line.line.add(record1);
					line.line.add(record2);
					arriveTime = record2.departureTime;
					
//					System.out.print("orderName: "+orderNames.get(i)+" pickup_time: "+orders.get(orderNames.get(i)).pickup_time
//							+" arriveShop_time: "+(arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id))
//							+" time: "+orders.get(orderNames.get(i)).time+" -> ");
//					arriveTime = orders.get(orderNames.get(i)).pickup_time + orders.get(orderNames.get(i)).time + orders.get(orderNames.get(i)).stay_time;
					
				}
			}
		}
	}
	
	
	//生成图
	public void graph(Map<String,Order> orders,Dis dis)
	{
		System.out.println("order.size: "+orders.size());
		
		for(String orderName1 : orders.keySet())
		{
//			if(orders.get(orderName1).time > 90) continue; //shop -> spot时间>90，不进行配送
			for(String orderName2 : orders.keySet())
			{
				//有向边从时间小的指向大的
				if(orders.get(orderName1).pickup_time > orders.get(orderName2).pickup_time+5) continue;
				if(orderName1.equals(orderName2)) continue;
				double d = dis.disPlace(orders.get(orderName1).shop_id, orders.get(orderName2).shop_id);
				//shop之间距离太远 ，pass
				if(d > 60) continue;
				//order之间pickup_time间隔时间太久， pass
				if(orders.get(orderName2).pickup_time-orders.get(orderName1).pickup_time > 100) continue;
				
				int time = 0;
				
				time = orders.get(orderName1).pickup_time + orders.get(orderName1).time+
						orders.get(orderName1).stay_time+dis.disPlace(orders.get(orderName1).spot_id, orders.get(orderName2).shop_id);
				if(time <= orders.get(orderName2).pickup_time)
				{
					time = orders.get(orderName2).pickup_time - orders.get(orderName1).pickup_time
							-orders.get(orderName1).time-orders.get(orderName1).stay_time;
				}
				else
				{
					if(orders.get(orderName2).flag == 2)
						time = getPenatyTime(orderName2,time,dis);
					time = dis.disPlace(orders.get(orderName1).spot_id, orders.get(orderName2).shop_id) + 
							5*Math.abs(orders.get(orderName2).pickup_time - time);
					if(orders.get(orderName2).time >= 90)
						time += 5*Math.abs(orders.get(orderName2).pickup_time - time);
				}
				
				if(time > 100) continue;
				
				Vertex v = getVertex(orderName1);
				Vertex w = getVertex(orderName2);
				v.adj.add(new Edge(v,w, time,0)); //result[1]为flag
				
			}
		}
		System.out.println("vertexMap.size: "+vertexMap.size());
	}
	
	public int getPenatyTime(String orderName,int time,Dis dis)
	{
		ArrayList<String> list = orders.get(orderName).list;
		int cost = 0;
		for(int i=0;i<list.size();i++)
		{
			if(i == 0)
			{
				cost += 5*Math.abs(time-orderss.get(list.get(i)).pickup_time);
			}
			else
			{
				time += dis.disPlace(orderss.get(list.get(i-1)).shop_id,orderss.get(list.get(i)).shop_id);
				if(time < orderss.get(list.get(i)).pickup_time)
				{
					cost -=time - dis.disPlace(orderss.get(list.get(i-1)).shop_id,orderss.get(list.get(i)).shop_id)
								- orderss.get(list.get(i-1)).pickup_time;
					time = orderss.get(list.get(i)).pickup_time;
				}
				else
					cost += 5*Math.abs(time-orderss.get(list.get(i)).pickup_time);
			}
		}
		return cost;
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
	
	public void printLines(Dis dis)
	{
		int orderNum = 0;
		for(int i=0;i<lines.size();i++)
		{
			orderNum += lines.get(i).line.size();
			for(int j=0;j<lines.get(i).line.size();j+=1)
			{
				if(lines.get(i).line.get(j).place_id.substring(0, 1).equals("S"))
					System.out.print(lines.get(i).line.get(j).order_id+": "+"shop_id: "+lines.get(i).line.get(j).place_id 
						+ " pickup_time: "+orderss.get(lines.get(i).line.get(j).order_id).pickup_time
						+ " arrive_time: "+lines.get(i).line.get(j).arriveTime 
						+ "	stay_time: " +orderss.get(lines.get(i).line.get(j).order_id).stay_time+" -" );
				else
					System.out.print(lines.get(i).line.get(j).order_id+": "+"spot_id: "+lines.get(i).line.get(j).place_id 
							+ " arrive_time: "+lines.get(i).line.get(j).arriveTime 
							+ "	stay_time: " +orderss.get(lines.get(i).line.get(j).order_id).stay_time+" -" );
				if(j + 1 <lines.get(i).line.size())
					System.out.print(dis.disPlace(lines.get(i).line.get(j).place_id, lines.get(i).line.get(j+1).place_id)+"> ");
			}
			System.out.println();
		}
		System.out.println("orderNum: "+orderNum/2);
	}
	
	public int o2oTime()
	{
		int time1 = 0; //路径总长度
		int time2 = 0; //路径前空缺时间
		int time3 = 0; //惩罚的时间
		int time4 = 0; //等待的时间
		int time5 = 0; //spot超时惩罚
		for (int i = 0; i < lines.size(); i++)
		{
			time1 += lines.get(i).line.get(lines.get(i).line.size() - 1).arriveTime 
					- lines.get(i).line.get(0).arriveTime;
			for (int j = 0; j < lines.get(i).line.size(); j++)
			{

				if (lines.get(i).line.get(j).place_id.substring(0, 1).equals("S"))
				{
					if (j == 0)
						time2 += lines.get(i).line.get(j).arriveTime; 
					else
					{
						if(lines.get(i).line.get(j).arriveTime > orderss.get(lines.get(i).line.get(j).order_id).pickup_time)
						{
							time3 += 5*Math.abs(lines.get(i).line.get(j).arriveTime -
									orderss.get(lines.get(i).line.get(j).order_id).pickup_time);
							System.out.println(lines.get(i).line.get(j).order_id+": "+"shop_id: "+lines.get(i).line.get(j).place_id 
									+ " pickup_time: "+orderss.get(lines.get(i).line.get(j).order_id).pickup_time
									+ " arrive_time: "+lines.get(i).line.get(j).arriveTime 
									+ " stay_time: " +orderss.get(lines.get(i).line.get(j).order_id).stay_time
									+ " time: "+orderss.get(lines.get(i).line.get(j).order_id).time);
						}
						else
						{
							time4 += Math.abs(lines.get(i).line.get(j).arriveTime -
									orderss.get(lines.get(i).line.get(j).order_id).pickup_time);
						}
					}
				}
				else
				{
					if(lines.get(i).line.get(j).arriveTime > orderss.get(lines.get(i).line.get(j).order_id).delivery_time)
					{
						time5 += 5*Math.abs(lines.get(i).line.get(j).arriveTime -
								orderss.get(lines.get(i).line.get(j).order_id).delivery_time);
					}
				}
			}
		}
		System.out.println("路径总长度time1 : " + time1);
		System.out.println("路径空缺时间time2 : " + time2);
		System.out.println("惩罚时间time3 : " + time3);
		System.out.println("等待时间time4(包括在路径总长度里) : " + time4);
		System.out.println("spot超时惩罚："+time5);
		System.out.println("总共时间time: " + (time1 + time2 + time3 + time5));
		return (time1 + time2 + time3 + time5);
	}
	//将lines根据初始shop的pickup时间从小到大排序
	public void quickSort(ArrayList<Line> lines,int p,int q)
	{
		if(p<q)
		{
			int s = partition(lines, p, q);
			quickSort(lines, p, s-1);
			quickSort(lines, s+1, q);
		}
	}
	public int partition(ArrayList<Line> lines,int p,int q)
	{
		int s = p;
		Line line = lines.get(p);
		
		for(int i=p+1;i<=q;i++)
		{
			if(lines.get(i).line.get(0).arriveTime < line.line.get(0).arriveTime)
			{
				s++;
				Line temp = lines.get(i);
				lines.set(i, lines.get(s));
				lines.set(s, temp);
			}
		}
		Line temp = lines.get(p);
		lines.set(p, lines.get(s));
		lines.set(s, temp);
		
		return s;
	}
}
