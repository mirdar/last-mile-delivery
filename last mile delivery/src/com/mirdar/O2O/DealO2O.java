package com.mirdar.O2O;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * ��ʼ��ѡ������Ķ��������͵����͵���ȹ��ˣ�Ȼ��ѡ������Ķ���,�ظ���ȥ
 * ��������������
 * 1. ÿ��ֻ��һ������
 * 2. ��ʼһ�����Ա���̻����ʱ��ѡ��������̻���
 * 3. ������һ�������󣬴��������͵��ϣ�ѡ��Ӹ����͵�ɴ��������̻���
 * 
 * ����Ҫ�����£�
 * 1. ��֤���Ƿ���ȷ
 * 2. ��·��ת�����ύ�ĸ�ʽ
 * 3. ��o2o·�������·�ߺϲ����ǣ�����ÿ��o2o������·�ߵ����綩��������Щ���Ա���������͵��̶���
 */

public class DealO2O
{

	public Map<String, Shop> shopMap;
	public Map<String, Spot> spotMap;
	public Map<String, ArrayList<Order>> orderMap;

	public Map<String, Integer> shops = new HashMap<String, Integer>(); // ����ÿ������һ�������󣬿��Ա�����е��̻��ľ���
	public ArrayList<InitTime> couriersInitTime = new ArrayList<InitTime>();
	public int allTime = 0;
	public ArrayList<Line> lines = new ArrayList<Line>(); // ��ÿ��·�߱�������

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

		o2o.print(o2o.shopMap, o2o.spotMap, o2o.orderMap); // ���ݶ�ȡ����

		ArrayList<Courier> couriers = o2o.assignmentOrder();
		System.out.println("��Ҫ���Ա��" + couriers.size());
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
			// System.out.println("���Ա"+(i+1)+":"+" ���Ա��ǰʱ�䣺
			// "+(couriers.get(i).current_time-couriers.get(i).last_stay_time));
			// for(int j=0;j<couriers.get(i).list.size();j++)
			// System.out.print(couriers.get(i).list.get(j)+" ");
			// System.out.println();
		}
		System.out.println("�Ѿ���" + orderNum / 2 + "��o2o����ȫ������");
		System.out.println(o2o.couriersInitTime.size());
		// System.out.println("����Ҫ��ʱ��: " + (o2o.allTime - picktime));
		o2o.orderMap = readData.readOrder(fileOrder);
		System.out.println("�����ͷ���ʱ�䣺 " + o2o.o2oTime());
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

		int rest = courierOrder(courier); // ����ÿ�ζ���һ�����Ա�滮һ����·��
		couriers.add(courier);
		while (rest != 0) // ����δ�����꣬��������
		{
			courier = new Courier();
			rest = courierOrder(courier);
			couriers.add(courier);
		}

		return couriers;
	}

	public void assignment(Courier courier, Line line) // ��ĳ�ֹ�����䶩��
	{
		Order order = new Order();
		String earlestShop = null;
		int earlestTime = Integer.MAX_VALUE;
		for (String key : shopMap.keySet()) // ������Ա���ڵ����͵��������̻��ľ���
		{
			shops.put(key, dis(courier.current_place, key));
		}
		int minTime = Integer.MAX_VALUE;
		for (String key : orderMap.keySet()) // ���˵��������������̻�
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
				// �����̻���ʱ������������ʱ��
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
		if (order.order_id == null) // û�ж�����
		{
			return;
		} else if (minTime > 55)
		{
			return;
		} else
		{
			if (courier.current_time + shops.get(earlestShop) + order.time > 720) // ��������ʱ�䳬��720��
			{
				return;
			}
			for (int i = 0; i < orderMap.get(earlestShop).size(); i++)
			{
				if (orderMap.get(earlestShop).get(i).order_id == order.order_id)
				{
					int arriveTime = 0; // �����̻���ʱ��
					int departureTime = 0; // �뿪�̻���ʱ��
					orderMap.get(earlestShop).remove(i); // ȥ�������˵Ķ���
					courier.current_place = order.spot_id; // ���Ա��ǰλ��
					arriveTime = courier.current_time + shops.get(earlestShop); // �����̻���ʱ��
					if (courier.current_time + shops.get(earlestShop) <= order.pickup_time) // �����̻�ʱ��С�ڸö����뿪ʱ��
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
					// System.out.println("���Ա "+order.shop_id+"
					// "+(courier.current_time-order.time-order.stay_time)+" "+
					// (courier.current_time-order.time-order.stay_time)+"
					// "+order.num+" "+order.order_id);
					// System.out.println("���Ա "+order.spot_id+"
					// "+(courier.current_time-order.stay_time)+" "+
					// courier.current_time+" -"+order.num+" "+order.order_id);
				}
			}
			assignment(courier, line);
		}

	}

	public int dis(String spot_id, String shop_id) // ����֮��ľ���
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

	// ���ص����̻������������͵������
	public int courierOrder(Courier courier)
	{
		// String[] shopName = new String[3];
		Line line = new Line();
		lines.add(line);
		Order order = new Order();
		int earliestTime = Integer.MAX_VALUE;
		for (String key : orderMap.keySet()) // ��ʼ��ѡ��������̻�����
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
				orderMap.get(order.shop_id).remove(i); // һ���������ȥ���ö���
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
				// System.out.println("���Ա "+order.shop_id+"
				// "+order.pickup_time+" "+order.pickup_time+" "+
				// order.num+" "+order.order_id);
				// System.out.println("���Ա "+order.spot_id+"
				// "+(order.pickup_time+order.time)+" "+
				// (order.pickup_time+order.time+order.stay_time)+"
				// -"+order.num+" "+order.order_id);
			}
		}

		assignment(courier, line);
		line.lastTime = 720 - courier.current_time + line.earliestTime;
		// System.out.println();
		int restOrder = 0; // ��ʣ���ٶ���
		for (String key : orderMap.keySet()) // ��ʼ��ѡ��������̻�����
		{
			restOrder += orderMap.get(key).size();
		}
		return restOrder;
	}

	// ����ѹ�� 1. ��ͬһ���̻���ͬһ�����͵ص�Ķ�����������Ķ�����������ʱ��������������Ķ���ʱ��<90min��
	// ��Ҫ���¶�����num��ʱ�����
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
		System.out.println("·���ܳ���time1 : " + time1);
		System.out.println("·����ȱʱ��time2 : " + time2);
		System.out.println("�ͷ�ʱ��time3 : " + time3);
		System.out.println("�ȴ�ʱ��time4(������·���ܳ�����) : " + time4);
		System.out.println("spot��ʱ�ͷ���"+time5);
		System.out.println("�ܹ�ʱ��time: " + (time1 + time2 + time3 + time5));
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
