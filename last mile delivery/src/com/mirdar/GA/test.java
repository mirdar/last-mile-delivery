package com.mirdar.GA;

import java.awt.PageAttributes.PrintQualityType;
import java.util.ArrayList;

import com.mirdar.O2O.ReadData;
import com.mirdar.test.Idea;

/*
 * 可以得到所有电商订单的规划，如果要优化，就得一个个网点进行优化，然后再是处理O2o订单
 * 
 */
public class test {

	public static void main(String[] args)
	{
		/*ArrayList<Integer> a = new ArrayList<Integer>();
		a.add(1);
		a.add(1);
		System.out.println(a.size());
		for(int i=0;i<a.size();i++)
		{
			System.out.println(a.get(i));
		}*/
		String fileShop = "F:\\ML\\last mile delivery/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery/spot.csv";
		String fileSite = "F:\\ML\\last mile delivery/site.csv";
		String fileOrder = "F:\\ML\\last mile delivery/shop_data.csv";
		String fileCourier = "F:\\ML\\last mile delivery/courier.csv";
		String filename = "F:/ML/last mile delivery/branch_data2.csv";
		ReadData readData = new ReadData();
		
		GA test = new GA();
//		test.placeMap = readData.readPlace(fileSite,test.placeMap);
//		test.placeMap = readData.readPlace(fileSpot,test.placeMap);
//		test.placeMap = readData.readPlace(fileShop,test.placeMap);
//		test.orderMap = readData.readOrder(fileOrder);
//		test.courier = readData.readCourier(fileCourier);
//		
//		int cars = 0;
//		test.allDataMap = test.readAllData(filename);
//		test.bind(test.orderMap,test.allDataMap);
//		for(String key : test.allDataMap.keySet())
//		{
//			for(Integer key2 : test.allDataMap.get(key).keySet())
//			{
//				System.out.println("deapr: "+test.allDataMap.get(key).get(key2).deparTime);
//			}
//		}
		System.out.println("-----------------------------------------------------------");
		System.out.println();
		System.out.println(test.allDataMap.size()+"  "+test.len.size());
		for(String key1 : test.allDataMap.keySet())
		{
			System.out.println("网点为："+key1);
			System.out.println("染色体长度："+test.len.get(key1));
			test.dataMap = test.allDataMap.get(key1);
			test.length = test.len.get(key1);
			test.bestChomo = new ArrayList<Chomo>();
//			System.out.println("spot_id  num  lon  lan  ID");
//			for(Integer key : ga.dataMap.keySet()) //打印数据
//			{
//				Point point = ga.dataMap.get(key);
//				System.out.println(point.getPointName()+"  "+point.getGoods_num()+"  "+
//								point.getLon()+"  "+point.getLan()+"  "+point.getPointId());
//			}
			ArrayList<Chomo> pop = test.getInitPop(); //初始群体生成成功
//			System.out.println("ininPop.size: "+pop.size());
//			ga.quickSort(pop, 0, pop.size()-1);
//			for(int i=0;i<pop.size();i++)
//			{
//				System.out.println(pop.get(i).getFit());
//			}
			
			ArrayList<Chomo> newPop = new ArrayList<Chomo>();
			for(int i=0;i<test.maxGen;i++)
			{
				newPop = test.select(pop);
				newPop = test.crossAndVar(newPop);
				pop = newPop;
			}
//			System.out.println("bestChomo.size: "+test.bestChomo.size());
//			System.out.println("bestChomo.length: "+test.bestChomo.get(test.bestChomo.size()-1).getChomo().length);
//			test.printAllocation(test.bestChomo.get(test.bestChomo.size()-1).getChomo());
			
//			cars+= test.printChomo(test.bestChomo.get(test.bestChomo.size()-1));
//			for(int i=0;i<test.bestChomo.size();i++)
//			{
//				System.out.println(test.fitness(test.bestChomo.get(i)));
//			}
//			test.printPath(test.bestChomo.get(test.bestChomo.size()-1));
		}
//		System.out.println("电商订单总共需要快递员数量为： "+cars);
		System.out.println(test.allTime);
	}
}
