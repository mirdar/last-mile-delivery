package com.mirdar.CW;

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
import com.mirdar.GA.Record;
import com.mirdar.O2O.Courier;
import com.mirdar.O2O.DealO2O;
import com.mirdar.O2O.ReadData;
import com.mirdar.TabuSearch.Code;
import com.mirdar.TabuSearch.TabuSearch;
import com.mirdar.graph.Couriers;
import com.mirdar.graph.Graph;
import com.mirdar.graph.LineCompression;
import com.mirdar.graph.SiteInfo;
import com.mirdar.test.Place;

public class CW
{
	Map<String, Place> placeMap = new HashMap<String, Place>();
	public int count = 0;
	Map<Integer, String> courier = new HashMap<Integer, String>();

	public static void main(String[] args) throws IOException
	{
		String fileShop = "E:\\tianchibigdata\\last mile delivery\\part 2/shop.csv";
		String fileSpot = "E:\\tianchibigdata\\last mile delivery\\part 2/spot.csv";
		String fileSite = "E:\\tianchibigdata\\last mile delivery\\part 2/site.csv";
		String fileOrder = "E:\\tianchibigdata\\last mile delivery\\part 2/o2o_data.csv";
		String fileCourier = "E:\\tianchibigdata\\last mile delivery\\part 2/courier.csv";
		ReadData readData = new ReadData();
		CW g = new CW();
		DealO2O o2o = new DealO2O();
		LineCompression lineCompre = new LineCompression();
		Dis dis = new Dis();
		Graph graph = new Graph();

		dis.placeMap = readData.readPlace(fileSite, dis.placeMap);
		dis.placeMap = readData.readPlace(fileSpot, dis.placeMap);
		dis.placeMap = readData.readPlace(fileShop, dis.placeMap);

		g.placeMap = readData.readPlace(fileSite, g.placeMap);
		g.placeMap = readData.readPlace(fileSpot, g.placeMap);
		g.placeMap = readData.readPlace(fileShop, g.placeMap);
		o2o.orderMap = readData.readOrder(fileOrder);
		o2o.shopMap = readData.readShop(fileShop);
		o2o.spotMap = readData.readSpot(fileSpot);
		g.courier = readData.readCourier(fileCourier);

		ArrayList<Courier> courierlist = o2o.assignmentOrder();

		String filename = "E:\\tianchibigdata\\last mile delivery\\part 2/branch_data.csv";
		// GA3 ga = new GA3();
		TabuSearch ts = new TabuSearch();
		CK ck = new CK();
		ck.allDataMap = ck.readAllData(filename);
		for (String key1 : ck.allDataMap.keySet())
		{
			ck.dataMap = ck.allDataMap.get(key1);
			// ts.max_con_iter = 200 + ck.dataMap.size() * 2; // 重置参数
			// ts.max_iter = 10000 + ck.dataMap.size() * 5;
			// ts.max_cand_list = 150 + 2 * ck.dataMap.size() * 1;
			ts.dataMap = ck.dataMap;
			ArrayList<com.mirdar.CKAlgorithm.Line> lineCk = ck.cKAlgorithm(ck.dataMap, dis);
			ArrayList<Integer> codeList = ts.setCode(lineCk);
			Code code = new Code();
			code.init(codeList, ts.codeEva(codeList, dis));
			System.out.println("len: " + ts.codeLength(code.codeList));
			System.out.println("eval: " + ts.codeEva(code.codeList, dis));
			ts.tabuTable = new int[ck.dataMap.size() + 1][ck.dataMap.size() + 1];
			Code newCode = ts.tabuSearch(code, dis);
			ts.printCode(newCode.codeList);
			System.out.println("len: " + ts.codeLength(newCode.codeList));
			System.out.println("eval: " + ts.codeEva(newCode.codeList, dis));
			ArrayList<com.mirdar.CKAlgorithm.Line> lineCk2 = ts.deCode(newCode.codeList);
			ck.printPaths(lineCk2, dis);
			// ts.max_con_iter = 1000; // 重置参数
			// ts.max_iter = 10000;
			// ts.max_cand_list = 150;
		}
		int branchSize1 = 0;
		for (String key : ck.lines.keySet())
		{
			for (int i = 0; i < ck.lines.get(key).size(); i++)
				branchSize1 += ck.lines.get(key).get(i).line.size();
		}
		System.out.println("branchSize1: " + branchSize1);
		Map<String, SiteInfo> siteInfo = lineCompre.lineCompression(ck.lines, g.placeMap);
		GraphAlgorithm graphAlgorithm = g.graphInit(o2o.lines, siteInfo, dis);
		System.out.println("节点数量： " + graphAlgorithm.vertexMap.size());
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("experiment");
		HSSFRow row1 = sheet.createRow(g.count);
		row1.createCell(0).setCellValue("Courier_id");
		row1.createCell(1).setCellValue("Addr");
		row1.createCell(2).setCellValue("Arrival_time");
		row1.createCell(3).setCellValue("Departure");
		row1.createCell(4).setCellValue("Amount");
		row1.createCell(5).setCellValue("Order_id");
		int branchLineTime = 0;
		for (String key : siteInfo.keySet())
		{
			for (int i = 0; i < siteInfo.get(key).edges.size(); i++)
				branchLineTime += siteInfo.get(key).edges.get(i).lineTime;
		}
		int o2oLineTime = 0;
		for (int i = 0; i < o2o.lines.size(); i++)
			o2oLineTime += o2o.lines.get(i).line.get(o2o.lines.get(i).line.size() - 1).arriveTime
					- o2o.lines.get(i).line.get(0).arriveTime;
		int transformTime = 0;
		// 如果这样可行的话，就还有三种优化途径
		// 1. 有branch进行禁忌搜索，局部优化
		// 2. 调整o2o的参数
		// 3. 改变o2o与branch拼接的排列顺序
		ArrayList<ArrayList<String>> newLines = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < o2o.lines.size(); i++)
		{
			transformTime += g.bindO2oBranch(o2o.lines.get(i), graphAlgorithm, siteInfo, dis, newLines, i, sheet);
		}
		System.out.println("branchLineTime: " + branchLineTime);
		System.out.println("o2oLineTime: " + o2oLineTime);
		System.out.println("transformTime: " + transformTime);
		System.out.println("AllTime: " + (branchLineTime + o2oLineTime + transformTime));
		int num = 0;
		for (int i = 0; i < newLines.size(); i++)
		{
			num += (newLines.get(i).size() - 1) / 2;
		}
		for (String key : graphAlgorithm.vertexMap.keySet())
		{
			if (graphAlgorithm.vertexMap.get(key).bindorNot != -1
					&& graphAlgorithm.vertexMap.get(key).name.substring(0, 1).equals("B"))
				num++;
		}
		System.out.println("num: " + num);
		g.getNewSiteInfo(siteInfo, newLines);
		graph.courier = g.courier;
		graph.count = g.count;
		graph.placeMap = g.placeMap;
		ArrayList<Couriers> couriers = graph.getCouriers(siteInfo);
		graph.printCouriers(couriers, o2o.lines.size(), sheet);

		OutputStream output = new FileOutputStream("E:\\tianchibigdata\\last mile delivery\\part 2/result/result1.xls");
		wb.write(output);
		output.close();
		wb.close();
	}

	public void getNewSiteInfo(Map<String, SiteInfo> siteInfo, ArrayList<ArrayList<String>> newLines)
	{
		for (int i = 0; i < newLines.size(); i++)
		{
			for (int j = newLines.get(i).size() - 1; j >= 0; j--)
			{
				if (j % 2 == 0 && j != 0)
				{
					for (int m = 0; m < siteInfo.get(newLines.get(i).get(j)).edges.size(); m++)
					{
						if (siteInfo.get(newLines.get(i).get(j)).edges.get(m).end.place_id
								.equals(newLines.get(i).get(j - 1)))
						{
							siteInfo.get(newLines.get(i).get(j)).edges.remove(m);
							break;
						}
					}
				}
			}
		}
	}

	public void printO2oLine(ArrayList<String> lines, HSSFSheet sheet, Map<String, SiteInfo> siteInfo, int courier_id,
			Dis dis, com.mirdar.O2O.Line line)
	{
		int time = 0;
		for (int i = 0; i < lines.size(); i++)
		{
			if (i % 2 == 0 && i != lines.size() - 1)
			{
				for (int j = 0; j < siteInfo.get(lines.get(i)).edges.size(); j++)
				{
					if (lines.get(i + 1).equals(siteInfo.get(lines.get(i)).edges.get(j).end.place_id))
					{
						printRecord(siteInfo.get(lines.get(i)).edges.get(j), time, courier_id, sheet);
						time += siteInfo.get(lines.get(i)).edges.get(j).lineTime;
						time += dis.disPlace(lines.get(i + 1), lines.get(i + 2));
					}
				}
			}
		}
		printO2o(line, time, courier_id, sheet);
	}

	public void printO2o(com.mirdar.O2O.Line line, int time, int courier_id, HSSFSheet sheet)
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
				row1.createCell(3).setCellValue(record.departureTime + extraTime);
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

	public void printRecord(com.mirdar.graph.Edge edge, int time, int courier_id, HSSFSheet sheet)
	{
		for (int i = 0; i < edge.line.size(); i++)
		{
			Record record = edge.line.get(i);
			count++;
			HSSFRow row1 = sheet.createRow(count);
			row1.createCell(0).setCellValue(courier.get(courier_id));
			row1.createCell(1).setCellValue(record.place_id);
			row1.createCell(2).setCellValue(record.arriveTime + time);
			row1.createCell(3).setCellValue(record.departureTime + time);
			row1.createCell(4).setCellValue(record.num);
			row1.createCell(5).setCellValue(record.order_id);
		}
	}

	// 初始化图，连接site,spot,shop，可以减少不必要的边，从而减少图规模，减少搜索时间
	public GraphAlgorithm graphInit(ArrayList<com.mirdar.O2O.Line> lineO2o, Map<String, SiteInfo> siteInfo, Dis dis)
	{
		GraphAlgorithm graphAlgorithm = new GraphAlgorithm();
		for (String key : siteInfo.keySet())
		{
			for (int j = 0; j < siteInfo.get(key).edges.size(); j++)
			{
				graphAlgorithm.addEdge(siteInfo.get(key).edges.get(j).start.place_id,
						siteInfo.get(key).edges.get(j).end.place_id, siteInfo.get(key).edges.get(j).lineTime);
				for (String key2 : siteInfo.keySet())
				{
					if (dis.disPlace(siteInfo.get(key).edges.get(j).end.place_id, key2) < 100)
						graphAlgorithm.addEdge(siteInfo.get(key).edges.get(j).end.place_id, key2,
								dis.disPlace(siteInfo.get(key).edges.get(j).end.place_id, key2));
				}
			}
		}

		return graphAlgorithm;
	}

	public int bindO2oBranch(com.mirdar.O2O.Line line, GraphAlgorithm graphAlgorithm, Map<String, SiteInfo> siteInfo,
			Dis dis, ArrayList<ArrayList<String>> newLines, int courier_id, HSSFSheet sheet)
	{
		for (String key : siteInfo.keySet())
		{
			for (int j = 0; j < siteInfo.get(key).edges.size(); j++)
			{

				if (siteInfo.get(key).edges.get(j).lineTime + dis.disPlace(siteInfo.get(key).edges.get(j).end.place_id,
						line.shop_id) < line.line.get(0).arriveTime + 10)
					graphAlgorithm.addEdge(siteInfo.get(key).edges.get(j).end.place_id, line.shop_id,
							dis.disPlace(siteInfo.get(key).edges.get(j).end.place_id, line.shop_id));
			}
		}
		int time = 0;
		int minTime = Integer.MAX_VALUE;
		for (String key : siteInfo.keySet())
		{
			minTime = (int) graphAlgorithm.BFS(key, line.shop_id, line.line.get(0).arriveTime, minTime);
		}
		ArrayList<String> o2oLines = new ArrayList<String>();
		// System.out.println("minTime: " + minTime);
		graphAlgorithm.printPath(line.shop_id, siteInfo, dis, newLines, o2oLines);
		// System.out.println(" line.arriveTime: " +
		// line.line.get(0).arriveTime);
		printO2oLine(o2oLines, sheet, siteInfo, courier_id, dis, line);
		return minTime;
	}

}
