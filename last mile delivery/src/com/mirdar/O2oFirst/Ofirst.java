package com.mirdar.O2oFirst;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.mirdar.BFS.GraphAlgorithm;
import com.mirdar.CKAlgorithm.CK;
import com.mirdar.CKAlgorithm.Dis;
import com.mirdar.GA.Point;
import com.mirdar.O2O.DealO2O;
import com.mirdar.O2O.ReadData;
import com.mirdar.TabuSearch.Code;
import com.mirdar.TabuSearch.TabuSearch;
import com.mirdar.graph.Couriers;
import com.mirdar.graph.Graph;
import com.mirdar.graph.LineCompression;
import com.mirdar.graph.SiteInfo;
import com.mirdar.test.Place;

public class Ofirst {
	public Map<String, Place> placeMap = new HashMap<String, Place>();
	public Map<String, Point> spots = new HashMap<String, Point>();
	public Map<String, ArrayList<Point>> siteSpot = new HashMap<String, ArrayList<Point>>();
	ArrayList<String> bigShop = new ArrayList<String>();
	Map<Integer, String> courier = new HashMap<Integer, String>();
	
	
	int count = 0;
	
	public static void main(String[] args) throws IOException
	{
		String fileShop = "F:\\ML\\last mile delivery/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery/spot.csv";
		String fileSite = "F:\\ML\\last mile delivery/site.csv";
		String fileOrder = "F:\\ML\\last mile delivery/shop_data.csv";
		String filename = "F:\\ML\\last mile delivery/branch_data2.csv";
		String fileCourier = "F:\\ML\\last mile delivery/courier.csv";
		
		ReadData readData = new ReadData();
		DealO2O o2o = new DealO2O();
		Dis dis = new Dis();
		Ofirst ofirst = new Ofirst();
		dis.placeMap = readData.readPlace(fileSite, dis.placeMap);
		dis.placeMap = readData.readPlace(fileSpot, dis.placeMap);
		dis.placeMap = readData.readPlace(fileShop, dis.placeMap);
		ofirst.placeMap = dis.placeMap;
		o2o.orderMap = readData.readOrder(fileOrder);
		o2o.shopMap = readData.readShop(fileShop);
		o2o.spotMap = readData.readSpot(fileSpot);
		ofirst.siteSpot = readData.readAllData2(filename);
		ofirst.spots = readData.readAllData3(filename);
		ofirst.courier = readData.readCourier(fileCourier);
		
		System.out.println("site.size: "+ofirst.siteSpot.size());
		System.out.println("spot.size: "+ofirst.spots.size());
		
		o2o.assignmentOrder(); //生成o2o路线
		
		GraphAlgorithm graphAlgorithm = ofirst.graphInit(ofirst.siteSpot);
		System.out.println("graph.vector.size: "+graphAlgorithm.vertexMap.size());
		//用来记录哪些branch订单已经被派送
		ArrayList<ArrayList<String>> newLines = new ArrayList<ArrayList<String>>();
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("experiment");
		HSSFRow row1 = sheet.createRow(ofirst.count);
		row1.createCell(0).setCellValue("Courier_id");
		row1.createCell(1).setCellValue("Addr");
		row1.createCell(2).setCellValue("Arrival_time");
		row1.createCell(3).setCellValue("Departure");
		row1.createCell(4).setCellValue("Amount");
		row1.createCell(5).setCellValue("Order_id");
		int courier_id = 0;
		for(int i=o2o.lines.size()-1;i>=0;i--)
		{
			ofirst.bindO2oBranch(ofirst.siteSpot, dis, o2o.lines.get(i), 
					ofirst.spots, graphAlgorithm,newLines,sheet,courier_id);
			courier_id++;
		}
		System.out.println("没有前缀的o2o路线的数量： "+ofirst.bigShop.size());
		
		TabuSearch ts = new TabuSearch();
		CK ck = new CK();
		ck.allDataMap = ofirst.removeSpotFromBranch(newLines);
		System.out.println("ck.allDataMap.size: "+ck.allDataMap.size());
		int spotNum = 0;
		for(int i=0;i<ck.allDataMap.size();i++)
			spotNum += ck.allDataMap.get(i).size()-1;
		System.out.println("spotNum: "+spotNum);
		for (String key1 : ck.allDataMap.keySet())
		{
			ck.dataMap = ck.allDataMap.get(key1);
			ts.max_con_iter = 200 + ck.dataMap.size() * 2; // 重置参数
			ts.max_iter = 10000 + ck.dataMap.size() * 5;
			ts.max_cand_list = 150 + 2 * ck.dataMap.size() * 1;
			ts.dataMap = ck.dataMap;
			ArrayList<com.mirdar.CKAlgorithm.Line> lineCk = ck
					.cKAlgorithm(ck.dataMap, dis);
			ArrayList<Integer> codeList = ts.setCode(lineCk);
			Code code = new Code();
			code.init(codeList, ts.codeEva(codeList, dis));
			System.out.println("len: " + ts.codeLength(code.codeList));
			System.out.println("eval: " + ts.codeEva(code.codeList, dis));
			ts.tabuTable = new int[ck.dataMap.size() + 1][ck.dataMap.size()
					+ 1];
			Code newCode = ts.tabuSearch(code, dis);
			ts.printCode(newCode.codeList);
			System.out.println("len: " + ts.codeLength(newCode.codeList));
			System.out.println("eval: " + ts.codeEva(newCode.codeList, dis));
			ArrayList<com.mirdar.CKAlgorithm.Line> lineCk2 = ts
					.deCode(newCode.codeList);
			ck.printPaths(lineCk2, dis);
			ts.max_con_iter = 500; // 重置参数
			ts.max_iter = 10000;
			ts.max_cand_list = 150;
		}
		System.out.println("ck.lines.size: "+ck.lines.size());
		LineCompression lineCompre = new LineCompression();
		Graph graph = new Graph();

		Map<String, SiteInfo> siteInfo = lineCompre.lineCompression(ck.lines,
				ofirst.placeMap);
		graph.courier = ofirst.courier;
		graph.count = ofirst.count;
		graph.placeMap = ofirst.placeMap;
		ArrayList<Couriers> couriers = graph.getCouriers(siteInfo);
		graph.printCouriers(couriers,courier_id, sheet);
		
		OutputStream output = new FileOutputStream(
				"F:\\ML\\last mile delivery/CW/result4.xls");
		wb.write(output);
		output.close();
		wb.close();
		
	}
	
	public GraphAlgorithm graphInit(Map<String, ArrayList<Point>> siteSpot)
	{
		GraphAlgorithm graphAlgorithm = new GraphAlgorithm();
		for (String key : siteSpot.keySet())
		{
			for (int j = 0; j < siteSpot.get(key).size(); j++)
			{
				
				for (int m = 0; m < siteSpot.get(key).size(); m++) //为每一个site点形成一个完全图,这里是双向边
				{
					if (j == m)
						continue;
					graphAlgorithm.addEdge(siteSpot.get(key).get(j).pointName,
									siteSpot.get(key).get(m).pointName,
									disPlace(siteSpot.get(key).get(j).pointName,
									siteSpot.get(key).get(m).pointName),2);
				}
				for (String key2 : siteSpot.keySet())//这里用flag来标识是否为双向吧，只有spot对应的site才为双向边
				{
					if(key.equals(key2))
						graphAlgorithm.addEdge(siteSpot.get(key).get(j).pointName,
								key2,
								disPlace(siteSpot.get(key).get(j).pointName,
								key2),2);
					else
						if(disPlace(siteSpot.get(key).get(j).pointName,
								key2) < 50)
							graphAlgorithm.addEdge(siteSpot.get(key).get(j).pointName,
								key2,
								disPlace(siteSpot.get(key).get(j).pointName,
								key2),1);
				}
			}
		}

		return graphAlgorithm;
	}

	public int bindO2oBranch(Map<String, ArrayList<Point>> siteSpot, Dis dis,
			com.mirdar.O2O.Line line,Map<String, Point> spots,
			GraphAlgorithm graphAlgorithm,ArrayList<ArrayList<String>> newLines
			,HSSFSheet sheet, int courier_id)
	{
		for (String key : siteSpot.keySet()) //将所有的spot与o2o路线连接
		{
			for (int j = 0; j < siteSpot.get(key).size(); j++)
			{

				if (disPlace(siteSpot.get(key).get(j).pointName,line.shop_id) < 50) //这里参数可调。为了减少边数
					graphAlgorithm.addEdge(siteSpot.get(key).get(j).pointName,
							line.shop_id,
							disPlace(siteSpot.get(key).get(j).pointName,line.shop_id),1);
			}
		}
		System.out.println("graph.vector.size: "+graphAlgorithm.vertexMap.size());
		int minTime = Integer.MAX_VALUE;
		System.out.println("shop: "+line.shop_id);
		for (String key : siteSpot.keySet())
		{
			minTime = (int) graphAlgorithm.BFS2(key, line.shop_id,
					line.line.get(0).arriveTime, minTime,spots);
		}
		System.out.println("minTime: " + minTime);
		if(minTime > 1000000)
			bigShop.add(line.shop_id);
		ArrayList<String> o2oLines = new ArrayList<String>();
		graphAlgorithm.printPath(line.shop_id, siteSpot,dis,
				line,spots,newLines,o2oLines);
		System.out.println();
		printO2oLine(o2oLines, courier_id,line,sheet);
		return minTime;
	}

	//去除在o2o中使用的spot
	public Map<String, Map<Integer, Point>> removeSpotFromBranch(ArrayList<ArrayList<String>> newLines)
	{
		Map<String, Map<Integer, Point>> allDataMap = new HashMap<String, Map<Integer,Point>>();
		for(int i=0;i<newLines.size();i++) 
		{
			for(int j=newLines.get(i).size()-1;j>0;j--)//o2o路线
			{
				String siteName = null;
				if(newLines.get(i).get(j).substring(0, 1).equals("A"))
				{
					siteName = newLines.get(i).get(j);
				}
				else{
					for(int m=0;m<siteSpot.get(siteName).size();m++) //从branch信息中出去已经出现在o2o路线中的spot
					{
						if(newLines.get(i).get(j).equals(siteSpot.get(siteName).get(m).pointName))
						{
							siteSpot.get(siteName).remove(m);
							break;
						}
					}
				}
			}
		}
		for(String key : siteSpot.keySet())
		{
			if(siteSpot.get(key).size() != 0)
			{
				int k = 0;
				Map<Integer, Point> dataMap = new HashMap<Integer, Point>();
				Point point = new Point();
				point.pointName = key;
				point.lan = placeMap.get(key).lan;
				point.lon = placeMap.get(key).lon;
				dataMap.put(k, point);
				for(int i=0;i<siteSpot.get(key).size();i++)
				{
					k++;
					point = siteSpot.get(key).get(i);
					dataMap.put(k, point);
				}
				allDataMap.put(key, dataMap);
			}
		}
		
		return allDataMap;
	}
	
	
	public void printO2oLine(ArrayList<String> lines
			,int courier_id, com.mirdar.O2O.Line line, HSSFSheet sheet)
	{
		int time = 0;
		ArrayList<Orecord> records = o2oSplit(lines);
		for (int i = 0; i < records.size(); i++)
		{
			if(records.get(i).placeName.substring(0, 1).equals("A"))
			{
				if(i == 0)
					printOrecord(records.get(i),time,courier_id,sheet);
				else
				{
					time += disPlace(records.get(i-1).placeName, records.get(i).placeName);
					printOrecord(records.get(i),time,courier_id,sheet);
				}
			}
			else
			{
				time += disPlace(records.get(i-1).placeName, records.get(i).placeName);
				printOrecord(records.get(i),time,courier_id,sheet);
				time += Math.round(3*Math.sqrt(records.get(i).num) + 5);
			}
			if(i == records.size()-1)
			{
				time += disPlace(records.get(i).placeName, line.line.get(0).place_id);
			}
		}
		printO2o(line, time, courier_id, sheet);
	}
	
	public ArrayList<Orecord> o2oSplit(ArrayList<String> lines)
	{
		ArrayList<Orecord> records = new ArrayList<Orecord>();
		ArrayList<String> tempLine = new ArrayList<String>();
		for(int i=0;i<lines.size();i++)
		{
			if(lines.get(i).substring(0, 1).equals("A"))
			{
				if(i != 0)
					constructLine(tempLine, records);
				tempLine = new ArrayList<String>();
				tempLine.add(lines.get(i));
			}
			else if(lines.get(i).substring(0, 1).equals("S"))
			{
				constructLine(tempLine, records);
			}
			else
			{
				tempLine.add(lines.get(i));
			}
		}
		
		return records;
	}
	
	public void constructLine(ArrayList<String> line,ArrayList<Orecord> lines) //将A B B -> A A B B
	{
		for(int i=1;i<line.size();i++)
		{
			Orecord record = new Orecord();
			record.placeName = line.get(0);
			record.num = spots.get(line.get(i)).goods_num;
			record.orderName = spots.get(line.get(i)).order_id;
			lines.add(record);
		}
		for(int i=1;i<line.size();i++)
		{
			Orecord record = new Orecord();
			record.placeName = line.get(i);
			record.num = spots.get(line.get(i)).goods_num;
			record.orderName = spots.get(line.get(i)).order_id;
			lines.add(record);
		}
	}
	
	public void printO2o(com.mirdar.O2O.Line line, int time, int courier_id,
			HSSFSheet sheet)
	{
		int extraTime = 0;
		if (time > line.line.get(0).arriveTime)
			extraTime = time - line.line.get(0).arriveTime;
		for (int i = 0; i < line.line.size(); i++)
		{
			if (i == 0)
			{
				com.mirdar.O2O.Record record = line.line.get(i);
				count++;
				HSSFRow row1 = sheet.createRow(count);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				row1.createCell(2).setCellValue(time);
				row1.createCell(3)
						.setCellValue(record.departureTime + extraTime);
				row1.createCell(4).setCellValue(record.num);
				row1.createCell(5).setCellValue(record.order_id);
				continue;
			}
			com.mirdar.O2O.Record record = line.line.get(i);
			count++;
			HSSFRow row1 = sheet.createRow(count);
			row1.createCell(0).setCellValue(courier.get(courier_id));
			row1.createCell(1).setCellValue(record.place_id);
			row1.createCell(2).setCellValue(record.arriveTime + extraTime);
			row1.createCell(3).setCellValue(record.departureTime + extraTime);
			row1.createCell(4).setCellValue(record.num);
			row1.createCell(5).setCellValue(record.order_id);
		}
	}
	
	public void printOrecord(Orecord record, int time,
			int courier_id, HSSFSheet sheet)
	{
		count++;
		HSSFRow row1 = sheet.createRow(count);
		row1.createCell(0).setCellValue(courier.get(courier_id));
		row1.createCell(1).setCellValue(record.placeName);
		row1.createCell(2).setCellValue(time);
		if(record.placeName.substring(0,1).equals("A"))
		{
			row1.createCell(3).setCellValue(time);
			row1.createCell(4).setCellValue(record.num);
		}
		else
		{
			row1.createCell(3).setCellValue(Math.round(3*Math.sqrt(record.num) + 5) + time);
			row1.createCell(4).setCellValue(-record.num);
		}
		row1.createCell(5).setCellValue(record.orderName);
	}
	
	
	public int disPlace(String name1, String name2) // 两点之间的距离
	{
		int cost = 0;
		cost = (int) (Math.round(2 * 6378137
				* Math.asin(Math.sqrt(Math
						.pow(Math.sin(Math.PI / 180.0
								* (placeMap.get(name1).lan
										- placeMap.get(name2).lan)
								/ 2), 2)
				+ Math.cos(Math.PI / 180.0 * placeMap.get(name1).lan)
						* Math.cos(Math.PI / 180.0 * placeMap.get(name2).lan)
						* Math.pow(
								Math.sin(Math.PI / 180.0
										* (placeMap.get(name1).lon
												- placeMap.get(name2).lon)
										/ 2),
								2)))
				/ 250));

		return cost;
	}
}
