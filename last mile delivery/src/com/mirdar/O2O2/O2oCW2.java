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
public class O2oCW2 {
	
	int save;
	public Map<String, Vertex> vertexMap = new HashMap<String, Vertex>();
	public Map<String,Order> orders = new HashMap<String,Order>();
	public ArrayList<Line> lines = new ArrayList<Line>(); // 将每条路线保存下来
	
	public static  void main(String[] args)
	{
		O2oCW2 o2oCW = new O2oCW2();
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
		o2oCW.orders = orders;
		o2oCW.graph(orders, dis);
		ArrayList<Vertex> edges = o2oCW.bindOperate(dis);
		o2oCW.edgeNum(edges);
		o2oCW.printLines();
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
	
	//最小生成树算法
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
			if(e.dest.scratch == 1 || e.dest.scratch == 3 || e.dest.scratch == 4) continue;
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
					
				if(lineTime(v, dis) > 720 )
				{
					if(orders.get(v.orderName).time+orders.get(v.orderName).pickup_time > 720)
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
		vertexToLine(leaveV);
		System.out.println("edges.size: "+edges.size());
		System.out.println("leaveV.size: "+leaveV.size());
		return edges;
	}
	
	public void vertexToLine(ArrayList<String> leaveV)
	{
		
		for(int i=0;i<leaveV.size();i++)
		{
			Line line = new Line();
			lines.add(line);
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
				line.shop_id = orders.get(orderNames.get(i)).shop_id;
				Record record1 = new Record();
				record1.order_id = orderNames.get(i);
				record1.place_id = orders.get(orderNames.get(i)).shop_id;
				record1.num = orders.get(orderNames.get(i)).num;
				record1.arriveTime = orders.get(orderNames.get(i)).pickup_time;
				record1.departureTime = orders.get(orderNames.get(i)).pickup_time;
				
				Record record2 = new Record();
				record2.order_id = orderNames.get(i);
				record2.place_id = orders.get(orderNames.get(i)).spot_id;
				record2.num = - orders.get(orderNames.get(i)).num;
				record2.arriveTime = orders.get(orderNames.get(i)).pickup_time+orders.get(orderNames.get(i)).time;
				record2.departureTime = orders.get(orderNames.get(i)).pickup_time+orders.get(orderNames.get(i)).time
										+orders.get(orderNames.get(i)).stay_time;
				line.line.add(record1);
				line.line.add(record2);
				
				arriveTime = orders.get(orderNames.get(i)).pickup_time+orders.get(orderNames.get(i)).time+orders.get(orderNames.get(i)).stay_time;
				System.out.print("orderName: "+orderNames.get(i)+" pickup_time: "+orders.get(orderNames.get(i)).pickup_time
						+" time: "+orders.get(orderNames.get(i)).time +" -> ");
				
				
			}
			if(i < orderNames.size()-1)
			{
				if(arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) 
							< orders.get(orderNames.get(i)).pickup_time)
				{
					Record record1 = new Record();
					record1.order_id = orderNames.get(i);
					record1.place_id = orders.get(orderNames.get(i)).shop_id;
					record1.num = orders.get(orderNames.get(i)).num;
					record1.arriveTime = arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) ;
					record1.departureTime = orders.get(orderNames.get(i)).pickup_time;
					
					Record record2 = new Record();
					record2.order_id = orderNames.get(i);
					record2.place_id = orders.get(orderNames.get(i)).spot_id;
					record2.num = - orders.get(orderNames.get(i)).num;
					record2.arriveTime = orders.get(orderNames.get(i)).pickup_time+orders.get(orderNames.get(i)).time;
					record2.departureTime = orders.get(orderNames.get(i)).pickup_time+orders.get(orderNames.get(i)).time
											+orders.get(orderNames.get(i)).stay_time;
					line.line.add(record1);
					line.line.add(record2);
					
					System.out.print("orderName: "+orderNames.get(i)+" pickup_time: "+orders.get(orderNames.get(i)).pickup_time
							+" arriveShop_time: "+(arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id))
							+" time: "+orders.get(orderNames.get(i)).time+" -> ");
					arriveTime = orders.get(orderNames.get(i)).pickup_time + orders.get(orderNames.get(i)).time + orders.get(orderNames.get(i)).stay_time;
				}
				else
				{
					Record record1 = new Record();
					record1.order_id = orderNames.get(i);
					record1.place_id = orders.get(orderNames.get(i)).shop_id;
					record1.num = orders.get(orderNames.get(i)).num;
					record1.arriveTime = arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) ;
					record1.departureTime = arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id);
					
					Record record2 = new Record();
					record2.order_id = orderNames.get(i);
					record2.place_id = orders.get(orderNames.get(i)).spot_id;
					record2.num = - orders.get(orderNames.get(i)).num;
					record2.arriveTime = arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id)+orders.get(orderNames.get(i)).time;
					record2.departureTime = arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id)+orders.get(orderNames.get(i)).time
											+orders.get(orderNames.get(i)).stay_time;
					line.line.add(record1);
					line.line.add(record2);
					
					System.out.print("orderName: "+orderNames.get(i)+" pickup_time: "+orders.get(orderNames.get(i)).pickup_time
							+" arriveShop_time: "+arriveTime+" time: "+orders.get(orderNames.get(i)).time+" -> ");
					arriveTime +=dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) 
								+ orders.get(orderNames.get(i)).time + orders.get(orderNames.get(i)).stay_time;
				}
			}
		}
	}
	
	public int lineTime(Vertex v,Dis dis)
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
				arriveTime = orders.get(orderNames.get(i)).pickup_time+orders.get(orderNames.get(i)).time+orders.get(orderNames.get(i)).stay_time;
			if(i < orderNames.size()-1)
			{
				if(arriveTime + dis.disPlace(orders.get(orderNames.get(i+1)).spot_id, orders.get(orderNames.get(i)).shop_id) 
							< orders.get(orderNames.get(i)).pickup_time)
				{
					arriveTime = orders.get(orderNames.get(i)).pickup_time + orders.get(orderNames.get(i)).time+orders.get(orderNames.get(i)).stay_time;
				}
				else
				{
					if(i != 0)
						arriveTime += orders.get(orderNames.get(i)).time+orders.get(orderNames.get(i)).stay_time;
					else
						arriveTime += orders.get(orderNames.get(i)).time;
				}
			}
		}
		return arriveTime;
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
				
				int time = orders.get(orderName1).pickup_time + orders.get(orderName1).time+
						orders.get(orderName1).stay_time+dis.disPlace(orders.get(orderName1).spot_id, orders.get(orderName2).shop_id);
				if(time < orders.get(orderName2).pickup_time)
				{
					time = orders.get(orderName2).pickup_time - orders.get(orderName1).pickup_time
							-orders.get(orderName1).time-orders.get(orderName1).stay_time;
				}
				else
				{
					time = dis.disPlace(orders.get(orderName1).spot_id, orders.get(orderName2).shop_id) + 
							5*Math.abs(orders.get(orderName2).pickup_time - time);
					if(orders.get(orderName2).time >= 90)
						time += 5*Math.abs(orders.get(orderName2).pickup_time - time);
				}
				
				if(time > 95) continue;
				
				Vertex v = getVertex(orderName1);
				Vertex w = getVertex(orderName2);
				v.adj.add(new Edge(v,w, time,0)); //result[1]为flag
				
			}
		}
		System.out.println("vertexMap.size: "+vertexMap.size());
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
	
	public void printLines()
	{
		int orderNum = 0;
		for(int i=0;i<lines.size();i++)
		{
			orderNum += lines.get(i).line.size();
			for(int j=0;j<lines.get(i).line.size();j+=2)
			{
				System.out.print("shop_id: "+lines.get(i).line.get(j).place_id 
						+ " pickup_time: "+orders.get(lines.get(i).line.get(j).order_id).pickup_time
						+ " arrive_time: "+lines.get(i).line.get(j).arriveTime + " -> ");
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

				if (j % 2 == 0)
				{
					if (j == 0)
						time2 += lines.get(i).line.get(j).arriveTime; 
					else
					{
						if(lines.get(i).line.get(j).arriveTime > orders.get(lines.get(i).line.get(j).order_id).pickup_time)
						{
							time3 += 5*Math.abs(lines.get(i).line.get(j).arriveTime -
									orders.get(lines.get(i).line.get(j).order_id).pickup_time);
							System.out.println(lines.get(i).line.get(j).order_id+": "+"shop_id: "+lines.get(i).line.get(j).place_id 
									+ " pickup_time: "+orders.get(lines.get(i).line.get(j).order_id).pickup_time
									+ " arrive_time: "+lines.get(i).line.get(j).arriveTime 
									+ " stay_time: " +orders.get(lines.get(i).line.get(j).order_id).stay_time
									+ " time: "+orders.get(lines.get(i).line.get(j).order_id).time);
						}
						else
						{
							time4 += Math.abs(lines.get(i).line.get(j).arriveTime -
									orders.get(lines.get(i).line.get(j).order_id).pickup_time);
						}
					}
				}
				if(j % 2 == 1)
				{
					if(lines.get(i).line.get(j).arriveTime > orders.get(lines.get(i).line.get(j).order_id).delivery_time)
					{
						time5 += 5*Math.abs(lines.get(i).line.get(j).arriveTime -
								orders.get(lines.get(i).line.get(j).order_id).delivery_time);
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
//			if(lines.get(i).line.size() > line.line.size())
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
