package com.mirdar.test;

import java.io.IOException;
import java.util.Map;

import com.mirdar.O2O.Order;
import com.mirdar.O2O.ReadData;

public class Valid
{

	// 评分类,跟我的评分相差大概12000
	public static void main(String[] args) throws IOException
	{
		String fileShop = "E:\\tianchibigdata\\last mile delivery/part 2/shop.csv";
		String fileSpot = "E:\\tianchibigdata\\last mile delivery/part 2/spot.csv";
		String fileSite = "E:\\tianchibigdata\\last mile delivery/part 2/site.csv";
		String fileResult = "E:\\tianchibigdata\\last mile delivery\\part 2/result/result6.xls";
		String fileOrder = "E:\\tianchibigdata\\last mile delivery/part 2/o2o_data.csv";
		ReadData readData = new ReadData();

		Test test = new Test();
		test.placeMap = readData.readPlace(fileSite, test.placeMap);
		test.placeMap = readData.readPlace(fileSpot, test.placeMap);
		test.placeMap = readData.readPlace(fileShop, test.placeMap);
		test.recordMap = readData.readRRecord2(fileResult);
		Map<String, Order> orders = readData.readO2oOrder(fileOrder);

		System.out.println("placeSize: " + test.placeMap.size());
		System.out.println("records.size : " + test.recordMap.size());
		System.out.println("orders.size: " + orders.size());
		System.out.println("cost: " + test.valid(test.recordMap, orders));
	}

	public int valid(String filename) throws IOException
	{
		String fileShop = "F:\\ML\\last mile delivery/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery/spot.csv";
		String fileSite = "F:\\ML\\last mile delivery/site.csv";
		String fileResult = filename;
		String fileOrder = "F:\\ML\\last mile delivery/shop_data.csv";
		ReadData readData = new ReadData();

		Test test = new Test();
		test.placeMap = readData.readPlace(fileSite, test.placeMap);
		test.placeMap = readData.readPlace(fileSpot, test.placeMap);
		test.placeMap = readData.readPlace(fileShop, test.placeMap);
		test.recordMap = readData.readRRecord2(fileResult);
		Map<String, Order> orders = readData.readO2oOrder(fileOrder);

		System.out.println("placeSize: " + test.placeMap.size());
		System.out.println("records.size : " + test.recordMap.size());
		System.out.println("orders.size: " + orders.size());
		System.out.println("cost: " + test.valid(test.recordMap, orders));

		return test.valid(test.recordMap, orders);
	}
}
