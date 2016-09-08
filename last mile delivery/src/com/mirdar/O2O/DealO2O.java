package com.mirdar.O2O;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * 初始化选择最早的订单，配送到配送点后，先过滤，然后选择最早的订单,重复下去
 * 其中设置条件：
 * 1. 每次只送一个订单
 * 2. 初始一个快递员的商户点的时候，选择最早的商户点
 * 3. 当配送一个订单后，次数在配送点上，选择从该配送点可达的最早的商户点
 * 
 * 还需要做的事：
 * 1. 验证解是否正确
 * 2. 经路线转化成提交的格式
 * 3. 将o2o路线与电商路线合并考虑，根据每条o2o订单的路线的最早订单，将这些快递员先让其配送电商订单
 */

public class DealO2O
{

	public Map<String, Shop> shopMap;
	public Map<String, Spot> spotMap;
	public Map<String, ArrayList<Order>> orderMap;

	public Map<String, Integer> shops = new HashMap<String, Integer>(); // 保存每次配送一个订单后，快递员与所有的商户的距离
	public ArrayList<InitTime> couriersInitTime = new ArrayList<InitTime>();
	public int allTime = 0;
	public ArrayList<Line> lines = new ArrayList<Line>(); // 将每条路线保存下来

	public static void main(String[] args)
	{
		String fileShop = "F:\\ML\\last mile delivery\\part 2/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery\\part 2/spot.csv";
		String fileOrder = "F:\\ML\\last mile delivery\\part 2/o2o_data.csv";
		ReadData readData = new ReadData();

		DealO2O o2o = new DealO2O();
		o2o.shopMap = readData.readShop(fileShop);
		o2o.spotMap = readData.readSpot(fileSpot);
		o2o.orderMap = readData.readOrder(fileOrder);

		o2o.print(o2o.shopMap, o2o.spotMap, o2o.orderMap); // 数据读取正常

		ArrayList<Courier> couriers = o2o.assignmentOrder();
		System.out.println("需要快递员：" + couriers.size());
		int orderNum = 0;
		int picktime = 0;
		System.out.println("o2o.lines: " + o2o.lines.size());
		for (int i = 0; i < o2o.lines.size(); i++)
		{
			picktime += o2o.lines.get(i).line.get(0).arriveTime;
		}
		System.out.println("pickupTime: " + picktime);
		for (int i = 0; i < couriers.size(); i++)
		{
			o2o.allTime += couriers.get(i).current_time;
			orderNum += couriers.get(i).list.size();
			// System.out.println("快递员"+(i+1)+":"+" 快递员当前时间：
			// "+(couriers.get(i).current_time-couriers.get(i).last_stay_time));
			// for(int j=0;j<couriers.get(i).list.size();j++)
			// System.out.print(couriers.get(i).list.get(j)+" ");
			// System.out.println();
		}
		System.out.println("已经将" + orderNum / 2 + "个o2o订单全部配送");
		System.out.println(o2o.couriersInitTime.size());
		// System.out.println("所需要的时间: " + (o2o.allTime - picktime));
		o2o.orderMap = readData.readOrder(fileOrder);
		System.out.println("包括惩罚的时间： " + o2o.o2oTime());
		System.out.println(o2o.lines.size());
		// for (int i = 0; i < 5; i++)
		// {
		// Record record = o2o.lines.get(0).line.get(i);
		// System.out.print(record.place_id + " " + record.arriveTime + " " +
		// record.departureTime + " " + record.num
		// + " " + record.order_id);
		// System.out.println();
		// }
	}

	public ArrayList<Courier> assignmentOrder()
	{
		ArrayList<Courier> couriers = new ArrayList<Courier>();
		Courier courier = new Courier();

		int rest = courierOrder(courier); // 这里每次都对一个快递员规划一条长路径
		couriers.add(courier);
		while (rest != 0) // 订单未分配完，继续分配
		{
			courier = new Courier();
			rest = courierOrder(courier);
			couriers.add(courier);
		}

		return couriers;
	}

	public void assignment(Courier courier, Line line) // 按某种规则分配订单
	{
		Order order = new Order();
		String earlestShop = null;
		int earlestTime = Integer.MAX_VALUE;
		for (String key : shopMap.keySet()) // 计算快递员所在的配送点与所有商户的距离
		{
			shops.put(key, dis(courier.current_place, key));
		}
		int minTime = Integer.MAX_VALUE;
		for (String key : orderMap.keySet()) // 过滤掉不满足条件的商户
		{
			for (int i = 0; i < orderMap.get(key).size(); i++)
			{
				int time = 0;
				if (courier.current_time + shops.get(key) <= orderMap.get(key).get(i).pickup_time)
					time = orderMap.get(key).get(i).pickup_time - courier.current_time;
				else
					time = orderMap.get(key).get(i).pickup_time - courier.current_time + 5
							* Math.abs(courier.current_time + shops.get(key) - orderMap.get(key).get(i).pickup_time);
				// if (time < 100)
				// System.out.println("time: " + time);
				// 到达商户的时间早于最晚到的时间
				if (time < minTime)
				{
					minTime = time;
					order = orderMap.get(key).get(i);
					if (courier.current_time + shops.get(key) <= orderMap.get(key).get(i).pickup_time)
						earlestTime = orderMap.get(key).get(i).pickup_time;
					else
						earlestTime = courier.current_time + shops.get(key);
					earlestShop = key;
				}
			}
		}
		// System.out.println("minTime: " + minTime);
		if (order.order_id == null) // 没有订单了
		{
			return;
		} else if (minTime > 55)
		{
			return;
		} else
		{
			if (courier.current_time + shops.get(earlestShop) + order.time > 720) // 继续配送时间超过720了
			{
				return;
			}
			for (int i = 0; i < orderMap.get(earlestShop).size(); i++)
			{
				if (orderMap.get(earlestShop).get(i).order_id == order.order_id)
				{
					int arriveTime = 0; // 到达商户的时间
					int departureTime = 0; // 离开商户的时间
					orderMap.get(earlestShop).remove(i); // 去除分配了的订单
					courier.current_place = order.spot_id; // 快递员当前位置
					arriveTime = courier.current_time + shops.get(earlestShop); // 到达商户的时间
					if (courier.current_time + shops.get(earlestShop) <= order.pickup_time) // 到达商户时间小于该订单离开时间
					{
						courier.current_time = order.pickup_time + order.time + order.stay_time;
						departureTime = order.pickup_time;
					} else
					{
						courier.current_time = courier.current_time + shops.get(earlestShop) + order.time
								+ order.stay_time;
						departureTime = arriveTime;
					}
					courier.last_stay_time = order.stay_time;
					courier.list.add(order.shop_id);
					courier.list.add(order.spot_id);

					Record record1 = new Record();
					record1.order_id = order.order_id;
					record1.place_id = order.shop_id;
					record1.arriveTime = arriveTime;
					record1.departureTime = departureTime;
					record1.num = order.num;

					Record record2 = new Record();
					record2.order_id = order.order_id;
					record2.place_id = order.spot_id;
					record2.arriveTime = courier.current_time - order.stay_time;
					record2.departureTime = courier.current_time;
					record2.num = -order.num;
					line.line.add(record1);
					line.line.add(record2);
					// System.out.println("快递员 "+order.shop_id+"
					// "+(courier.current_time-order.time-order.stay_time)+" "+
					// (courier.current_time-order.time-order.stay_time)+"
					// "+order.num+" "+order.order_id);
					// System.out.println("快递员 "+order.spot_id+"
					// "+(courier.current_time-order.stay_time)+" "+
					// courier.current_time+" -"+order.num+" "+order.order_id);
				}
			}
			assignment(courier, line);
		}

	}

	public int dis(String spot_id, String shop_id) // 两点之间的距离
	{
		int cost = 0;
		cost = (int) Math.round(2
				* 6378137 * Math
						.asin(Math
								.sqrt(Math
										.pow(Math.sin(
												Math.PI / 180.0 * (spotMap.get(spot_id).lan - shopMap.get(shop_id).lan)
														/ 2),
												2)
										+ Math.cos(Math.PI / 180.0 * spotMap.get(spot_id).lan)
												* Math.cos(
														Math.PI / 180.0 * shopMap.get(shop_id).lan)
												* Math.pow(
														Math.sin(
																Math.PI / 180.0
																		* (spotMap.get(spot_id).lon
																				- shopMap.get(shop_id).lon)
																		/ 2),
														2)))
				/ 250);

		return cost;
	}

	// 返回的是商户，订单，配送点的名字
	public int courierOrder(Courier courier)
	{
		// String[] shopName = new String[3];
		Line line = new Line();
		lines.add(line);
		Order order = new Order();
		int earliestTime = Integer.MAX_VALUE;
		for (String key : orderMap.keySet()) // 初始化选择最早的商户订单
		{
			for (int i = 0; i < orderMap.get(key).size(); i++)
			{
				if (orderMap.get(key).get(i).pickup_time < earliestTime)
				{
					earliestTime = orderMap.get(key).get(i).pickup_time;
					// shopName[0] = key;
					// shopName[1] = orderMap.get(key).get(i).order_id;
					// shopName[2] = orderMap.get(key).get(i).spot_id;
					order = orderMap.get(key).get(i);
				}
			}
		}
		for (int i = 0; i < orderMap.get(order.shop_id).size(); i++)
		{
			if (orderMap.get(order.shop_id).get(i).order_id == order.order_id)
			{
				orderMap.get(order.shop_id).remove(i); // 一旦被分配就去除该订单
				courier.current_place = order.spot_id;
				courier.current_time = order.pickup_time + order.time + order.stay_time;
				courier.last_stay_time = order.stay_time;
				courier.list.add(order.shop_id);
				courier.list.add(order.spot_id);
				InitTime initTime = new InitTime();
				initTime.shop_id = order.shop_id;
				initTime.initTime = order.pickup_time;
				couriersInitTime.add(initTime);
				line.earliestTime = order.pickup_time;

				Record record1 = new Record();
				record1.order_id = order.order_id;
				record1.place_id = order.shop_id;
				record1.arriveTime = order.pickup_time;
				record1.departureTime = order.pickup_time;
				record1.num = order.num;

				Record record2 = new Record();
				record2.order_id = order.order_id;
				record2.place_id = order.spot_id;
				record2.arriveTime = order.pickup_time + order.time;
				record2.departureTime = order.pickup_time + order.time + order.stay_time;
				record2.num = -order.num;
				line.line.add(record1);
				line.line.add(record2);
				line.shop_id = order.shop_id;
				// System.out.println("快递员 "+order.shop_id+"
				// "+order.pickup_time+" "+order.pickup_time+" "+
				// order.num+" "+order.order_id);
				// System.out.println("快递员 "+order.spot_id+"
				// "+(order.pickup_time+order.time)+" "+
				// (order.pickup_time+order.time+order.stay_time)+"
				// -"+order.num+" "+order.order_id);
			}
		}

		assignment(courier, line);
		line.lastTime = 720 - courier.current_time + line.earliestTime;
		// System.out.println();
		int restOrder = 0; // 还剩多少订单
		for (String key : orderMap.keySet()) // 初始化选择最早的商户订单
		{
			restOrder += orderMap.get(key).size();
		}
		return restOrder;
	}

	// 订单压缩 1. 将同一个商户，同一个配送地点的订单（在最晚的订单进行配送时，可以满足最早的订单时间<90min）
	// 需要将新订单的num与时间更改
	public void orderCompress(Map<String, ArrayList<Order>> orderMap)
	{

	}

	public int o2oTime()
	{
		int time1 = 0;
		int time2 = 0;
		int time3 = 0;
		int time4 = 0;
		int time5 = 0;
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
					for (int m = 0; m < orderMap.get(lines.get(i).line.get(j).place_id).size(); m++)
					{

						if (lines.get(i).line.get(j).order_id
								.equals(orderMap.get(lines.get(i).line.get(j).place_id).get(m).order_id))
						{
							if (lines.get(i).line.get(j).arriveTime > orderMap.get(lines.get(i).line.get(j).place_id)
									.get(m).pickup_time)
							{
								time3 += 5 * Math.abs(lines.get(i).line.get(j).arriveTime
										- orderMap.get(lines.get(i).line.get(j).place_id).get(m).pickup_time);
							} else
							{
								time4 += Math.abs(lines.get(i).line.get(j).arriveTime
										- orderMap.get(lines.get(i).line.get(j).place_id).get(m).pickup_time);
							}
						}
					}
				}
				if(j % 2 == 1)
				{
					for (int m = 0; m < orderMap.get(lines.get(i).line.get(j-1).place_id).size(); m++)
					{

						if (lines.get(i).line.get(j).order_id
								.equals(orderMap.get(lines.get(i).line.get(j-1).place_id).get(m).order_id))
						{
							if (lines.get(i).line.get(j).arriveTime > orderMap.get(lines.get(i).line.get(j-1).place_id)
									.get(m).delivery_time)
							{
								time5 += 5 * Math.abs(lines.get(i).line.get(j).arriveTime
										- orderMap.get(lines.get(i).line.get(j-1).place_id).get(m).delivery_time);
							} 
							
						}
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

	public void print(Map<String, Shop> shopMap, Map<String, Spot> spotMap, Map<String, ArrayList<Order>> orderMap)
	{
		System.out.println("shop information");
		int i = 0;
		for (String key : shopMap.keySet())
		{
			Shop shop = shopMap.get(key);
			System.out.println(shop.shop_id + " " + shop.lon + " " + shop.lan);
			if (i >= 5)
			{
				i = 0;
				break;
			}
			i++;
		}
		System.out.println();
		System.out.println("spot information");
		for (String key : spotMap.keySet())
		{
			Spot spot = spotMap.get(key);
			System.out.println(spot.spot_id + " " + spot.lon + " " + spot.lan);
			if (i >= 5)
			{
				i = 0;
				break;
			}
			i++;
		}
		System.out.println();
		System.out.println("order information");
		for (String key : orderMap.keySet())
		{
			ArrayList<Order> order = orderMap.get(key);
			System.out.println(key + " " + order.size());
			if (i >= 5)
			{
				i = 0;
				break;
			}
			i++;
		}
	}
}
