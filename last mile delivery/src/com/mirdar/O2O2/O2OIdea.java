package com.mirdar.O2O2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mirdar.CKAlgorithm.Dis;
import com.mirdar.O2O.Order;
import com.mirdar.O2O.ReadData;

public class O2OIdea {

	public Map<String, ArrayList<Order>> orders =new HashMap<String, ArrayList<Order>>();
	public Map<String,Order> orderMap = new HashMap<String,Order>();
	
	public static void main(String[] args)
	{
		O2OIdea o2o = new O2OIdea();
		Dis dis = new Dis();
		ReadData readData = new ReadData();
		String fileShop = "F:\\ML\\last mile delivery\\part 2/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery\\part 2/spot.csv";
		String fileSite = "F:\\ML\\last mile delivery\\part 2/site.csv";
		String fileOrder = "F:\\ML\\last mile delivery\\part 2/o2o_data2.csv";
		
		dis.placeMap = readData.readPlace(fileSite, dis.placeMap);
		dis.placeMap = readData.readPlace(fileSpot, dis.placeMap);
		dis.placeMap = readData.readPlace(fileShop, dis.placeMap);
		Map<String,Order> orderss = readData.readO2oOrder(fileOrder);
		o2o.orderMap = orderss;
		
		o2o.orders = o2o.readOrder(fileOrder);
		System.out.println("------------------------------------------------");
		System.out.println("spot.size: "+o2o.orders.size());
		int o2oOrderSize = 0;
//		for(String spot_id : o2o.orders.keySet())
//		{
//			o2oOrderSize += o2o.orders.get(spot_id).size();
//			if(o2o.orders.get(spot_id).size() >= 0)
//			{
//				System.out.println("spot_id: "+spot_id+" size: "+o2o.orders.get(spot_id).size());
//				for(int i=0;i<o2o.orders.get(spot_id).size();i++)
//					System.out.println("       orders:"+o2o.orders.get(spot_id).get(i).order_id);
//			}
//		}
		System.out.println("o2oOrderSize: "+o2oOrderSize);
		System.out.println();
		o2o.bind(dis);
	}
	
	public Map<String,Order> bind(Dis dis)
	{
		Map<String,Order> orderAll = new HashMap<String,Order>();
		try {
		      File file =new File("F:\\ML\\last mile delivery\\part 2/result/rengong.txt");

		      // if file doesnt exists, then create it
		      if (!file.exists()) 
		      {
		    	  file.createNewFile();
		      }

		      FileWriter fw = new FileWriter(file.getAbsoluteFile());
		      BufferedWriter bw = new BufferedWriter(fw);
		int saveApro = 0;
		for(String spot_id : orders.keySet())
		{
			GraphIdea graph = new GraphIdea();
			ArrayList<Order> orderList = orders.get(spot_id);
			Map<String,Order> orderM = new HashMap<String,Order>();
			for(int i=0;i<orderList.size();i++)
				orderM.put(orderList.get(i).order_id, orderList.get(i));
			quickSort(orderList, 0, orderList.size()-1);
			int count = 0;
			for(int i=0;i<orderList.size();i++)
			{
				for(int j=0;j<orderList.size();j++)
				{
					//不与自身相连
					if(orderList.get(i).order_id.equals(orderList.get(j).order_id)) continue;
					//从小向大连接
					if(orderList.get(i).pickup_time > orderList.get(j).pickup_time) continue;
					int cost1 = 0;
					int save1 = 0;
					String shop1 = orderList.get(i).shop_id;
					String shop2 = orderList.get(j).shop_id;
					int p1 = orderList.get(i).pickup_time;
					int p2 = orderList.get(j).pickup_time;
					if(orderList.get(i).time >= 90)
					{
						if((p2-p1) == 0 && dis.disPlace(shop1,shop2) == 0)
							save1 = 1;
						else
							continue;
					}
					else
					{
						if(dis.disPlace(shop1,shop2) < (p2-p1))
						{
							if(orderList.get(j).time + (p2 - p1) > 90)
								continue;
							else
							{
								save1 = orderList.get(i).time;
								cost1 = orderList.get(j).time + (p2 - p1);
							}
						}
						else
						{
							if(orderList.get(j).time + dis.disPlace(shop1,shop2) > 90
									|| orderList.get(j).time + dis.disPlace(shop1,shop2) - (p2-p1) > 90)
								continue;
							else
							{
								save1 = orderList.get(i).time;
								cost1 = orderList.get(j).time + dis.disPlace(shop1,shop2)
										+ 5*(dis.disPlace(shop1,shop2) - (p2-p1));
							}
						}
					}
					if(save1-cost1 >= -1)
					{
						System.out.println(
										orderList.get(i).order_id +" pick: "+p1+" time: "+orderList.get(i).time
										+" -> "+orderList.get(j).order_id +" pick: "+p2+" time: "+orderList.get(j).time
										+" 转移时间: "+dis.disPlace(shop1,shop2) + " cost: "+cost1+ " save1: "+save1 + "save: "+(save1-cost1));
						bw.write(orderList.get(i).order_id +" pick: "+p1+" time: "+orderList.get(i).time
								+" -> "+orderList.get(j).order_id +" pick: "+p2+" time: "+orderList.get(j).time
								+" 转移时间: "+dis.disPlace(shop1,shop2) + " cost: "+cost1+ " save1: "+save1 + "save: "+(save1-cost1) +" \r\n");
						saveApro += (save1-cost1);
						count++;
						graph.addEdge(orderList.get(i).order_id, orderList.get(j).order_id, (save1-cost1),orderMap);
					}
				}
			}
			if(count > 0)
			{
				System.out.println(orderList.size());
//				//当路线被修改了
				orderM = graph.bind(orderM,dis);
				System.out.println("----------");
				bw.write("spot_id: "+spot_id+"\r\n");
				bw.write("-----------"+"\r\n");
			}
			for(String orderName : orderM.keySet())
			{
				orderAll.put(orderName, orderM.get(orderName));
			}
		}
		System.out.println("大概可以节约： "+saveApro/2);
		System.out.println(orderAll.size());
		
		 bw.close();
	}catch(IOException e)
		{
			e.printStackTrace();
		}
	return orderAll;
}
	
	//String -> spot_id，ArrayList<Order> -> o2oOrders
	public Map<String, ArrayList<Order>> readOrder(String filename)
	{
		Map<String, ArrayList<Order>> map = new HashMap<String, ArrayList<Order>>();
		ArrayList<Order> orders = new ArrayList<Order>();

		File file = new File(filename);
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String content = null;
			while ((content = reader.readLine()) != null)
			{
				String[] strings = content.split(",");
				Order order = new Order();
				order.spot_id = strings[1];
				if (!map.containsKey(order.spot_id))
				{
					orders = new ArrayList<Order>();
					map.put(order.spot_id, orders);
				}
				order.shop_id = strings[0];
				order.order_id = strings[2];
				order.pickup_time = Integer.parseInt(strings[3]);
				order.delivery_time = Integer.parseInt(strings[4]);
				order.num = Integer.parseInt(strings[5]);
				order.time = Integer.parseInt(strings[10]);
				order.stay_time = Integer.parseInt(strings[11]);
				order.last_time = order.delivery_time-order.time;
				orders.add(order);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		return (map);
	}
	
	public void quickSort(ArrayList<Order> orders,int p,int q)
	{
		if(p < q)
		{
			int s = partition(orders, p, q);
			quickSort(orders, p, s-1);
			quickSort(orders, s+1, q);
		}
	}
	public int partition(ArrayList<Order> orders,int p,int q)
	{
		int s = p;
		Order order = orders.get(p);
		for(int i=p+1;i<=q;i++)
		{
			if(orders.get(i).pickup_time < order.pickup_time)
			{
				s++;
				Order temp = orders.get(i);
				orders.set(i, orders.get(s));
				orders.set(s, temp);
			}
		}
		
		Order temp = orders.get(s);
		orders.set(s, orders.get(p));
		orders.set(p, temp);
		
		return s;
	}
}
