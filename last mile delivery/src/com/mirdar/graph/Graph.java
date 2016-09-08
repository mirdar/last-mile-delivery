package com.mirdar.graph;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.mirdar.CKAlgorithm.CK;
import com.mirdar.CKAlgorithm.Dis;
import com.mirdar.GA.Record;
import com.mirdar.O2O.Courier;
import com.mirdar.O2O.DealO2O;
import com.mirdar.O2O.Line;
import com.mirdar.O2O.ReadData;
import com.mirdar.TabuSearch.Code;
import com.mirdar.TabuSearch.TabuSearch;
import com.mirdar.test.Place;

public class Graph
{

	public Map<String, Place> placeMap = new HashMap<String, Place>();
	int branchTime = 0;
	public int count = 0;
	public Map<Integer, String> courier = new HashMap<Integer, String>();

	public static void main(String[] args) throws IOException
	{
		String fileShop = "F:\\ML\\last mile delivery/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery/spot.csv";
		String fileSite = "F:\\ML\\last mile delivery/site.csv";
		String fileOrder = "F:\\ML\\last mile delivery/shop_data.csv";
		String fileCourier = "F:\\ML\\last mile delivery/courier.csv";
		ReadData readData = new ReadData();
		Graph g = new Graph();
		DealO2O o2o = new DealO2O();
		LineCompression lineCompre = new LineCompression();
		Dis dis = new Dis();

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

		String filename = "F:\\ML\\last mile delivery/branch_data2.csv";
		// GA3 ga = new GA3();
		TabuSearch ts = new TabuSearch();
		CK ck = new CK();
		ck.allDataMap = ck.readAllData(filename);
		for (String key1 : ck.allDataMap.keySet())
		{
			ck.dataMap = ck.allDataMap.get(key1);
			ts.max_con_iter = 20; // 重置参数
			ts.max_iter = 1000;
			ts.max_cand_list = 10;
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
			ts.max_con_iter = 20; // 重置参数
			ts.max_iter = 1000;
			ts.max_cand_list = 10;
		}

		int branchSize1 = 0;
		for (String key : ck.lines.keySet())
		{
			for (int i = 0; i < ck.lines.get(key).size(); i++)
				branchSize1 += ck.lines.get(key).get(i).line.size();
		}
		System.out.println("branchSize1: " + branchSize1);
		Map<String, SiteInfo> siteInfo = lineCompre.lineCompression(ck.lines, g.placeMap);
		// lineCompre.printSiteInfo(siteInfo);
		// ArrayList<Couriers> couriers = g.getCouriers(siteInfo);
		// g.printCouriers(couriers);
		// System.out.println("couriers.size: " + couriers.size());
		// System.out.println("branchTime: " + g.branchTime);
		int o2oSize = 0;
		for (int i = 0; i < o2o.lines.size(); i++)
		{
			o2oSize += o2o.lines.get(i).line.size();
		}
		System.out.println("o2oSize: " + o2oSize);
		int branchSize = 0;
		for (String key : siteInfo.keySet())
		{
			for (int i = 0; i < siteInfo.get(key).edges.size(); i++)
			{
				branchSize += siteInfo.get(key).edges.get(i).line.size();
			}
		}
		System.out.println("branchSize: " + branchSize);
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("experiment");
		HSSFRow row1 = sheet.createRow(g.count);
		row1.createCell(0).setCellValue("Courier_id");
		row1.createCell(1).setCellValue("Addr");
		row1.createCell(2).setCellValue("Arrival_time");
		row1.createCell(3).setCellValue("Departure");
		row1.createCell(4).setCellValue("Amount");
		row1.createCell(5).setCellValue("Order_id");

		g.joinBranchO2o(o2o.lines, siteInfo, sheet);
		OutputStream output = new FileOutputStream("F:\\ML\\last mile delivery/newIdeaResult/resultA.xls");
		wb.write(output);
		output.close();
		wb.close();
	}

	public void joinBranchO2o(ArrayList<com.mirdar.O2O.Line> lineO2o, Map<String, SiteInfo> siteInfo, HSSFSheet sheet)
	{
		int spotNull = 0;
		for (int i = 0; i < lineO2o.size(); i++)
		{
			int nearestSpotDis = Integer.MAX_VALUE;
			int longLine = 0;
			String nearestSpot = null;
			String nearestSite = null;
			int nearestLine = 0;
			for (String key : siteInfo.keySet()) // 找到离该商户最近的网点
			{
				ArrayList<Edge> edges = siteInfo.get(key).edges;
				for (int j = 0; j < edges.size(); j++)
				{
					if (edges.get(j).lineTime > longLine && edges.get(j).lineTime + disPlace(edges.get(j).end.place_id,
							lineO2o.get(i).shop_id) <= lineO2o.get(i).earliestTime)
					{
						nearestSpotDis = disPlace(edges.get(j).end.place_id, lineO2o.get(i).shop_id);
						longLine = edges.get(j).lineTime;
						nearestSpot = edges.get(j).end.place_id;
						nearestSite = key;
						nearestLine = j;
					}
				}
			}
			System.out.println("spot_id: " + nearestSpot);
			if (nearestSpot == null)
			{
				spotNull = spotNull + lineO2o.get(i).line.size();
				printO2OBase(lineO2o.get(i), siteInfo, sheet, i);
			} else
			{
				// 偶尔会出现有些商户找不到满足条件的快递员，此时没有另外生成快递员，而是增加迭代此时
				Edge line = siteInfo.get(nearestSite).edges.get(nearestLine);

				int o2oEarliestTime = lineO2o.get(i).earliestTime - nearestSpotDis - line.lineTime; // 背包的大小，减去了最近的一条路径
				siteInfo.get(nearestSite).edges.remove(nearestLine);
				ArrayList<Edge> headLine = lineInO2o(siteInfo, o2oEarliestTime, line.start.place_id);
				int arrvieShopTime = line.lineTime + nearestSpotDis;
				if (o2oEarliestTime < 10) // 剩余的背包大小太小，无法加入
					;
				else
				{
					if (headLine.size() != 0)
						arrvieShopTime += headLine.get(0).lineTime
								+ disPlace(headLine.get(0).end.place_id, line.start.place_id);
				}
				headLine.add(line);
				lineO2o.get(i).arriveTime = arrvieShopTime; // 将该商户快递员之前的信息都加入商户路线中这里，arriveTime应该会小于订单开始时间
				lineO2o.get(i).edgeLine = headLine;
				lineO2o.get(i).spot_id = line.end.place_id;
				// 打印O2o的路线
				printO2o(lineO2o.get(i), sheet, i);
			}
		}

		int courier_id = lineO2o.size();
		ArrayList<Couriers> couriers = getCouriers(siteInfo);
		printCouriers(couriers, courier_id, sheet);
	}

	public ArrayList<Edge> lineInO2o(Map<String, SiteInfo> siteInfo, int o2oEarliestTime, String start)
	{
		ArrayList<Edge> edges = new ArrayList<Edge>();
		// Edge edge = new Edge();

		System.out.println(start);
		int minTime = 0;
		String siteKey = "NO";
		int edgeId = 0;
		for (String key : siteInfo.keySet())
		{
			for (int i = 0; i < siteInfo.get(key).edges.size(); i++)
			{
				if (siteInfo.get(key).edges.get(i).lineTime
						+ disPlace(siteInfo.get(key).edges.get(i).end.place_id, start) <= o2oEarliestTime
						&& siteInfo.get(key).edges.get(i).lineTime > minTime)
				{
					minTime = siteInfo.get(key).edges.get(i).lineTime;
					siteKey = key;
					edgeId = i;
				}
			}
		}
		if (!siteKey.equals("NO"))
		{
			edges.add(siteInfo.get(siteKey).edges.get(edgeId));
			siteInfo.get(siteKey).edges.remove(edgeId);
		}

		return edges;
	}

	public void printO2OBase(Line lineO2o, Map<String, SiteInfo> siteInfo, HSSFSheet sheet, int courier_id)
	{
		int nearestSiteDis = Integer.MAX_VALUE;
		String nearestSite = null;
		for (String key : siteInfo.keySet()) // 找到离该商户最近的网点
		{
			if (disPlace(key, lineO2o.shop_id) < nearestSiteDis)
			{
				nearestSiteDis = disPlace(key, lineO2o.shop_id);
				nearestSite = key;
			}
		}
		System.out.println("site_id: " + nearestSite);
		lineO2o.arriveTime = nearestSiteDis;

		// 打印O2o的路线
		printBaseO2o(lineO2o, sheet, courier_id);
	}

	public void printBaseO2o(com.mirdar.O2O.Line o2oLine, HSSFSheet sheet, int courier_id)
	{
		for (int i = 0; i < o2oLine.line.size(); i++)
		{
			com.mirdar.O2O.Record record = o2oLine.line.get(i);
			if (i == 0)
			{
				count++;
				HSSFRow row1 = sheet.createRow(count);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				row1.createCell(2).setCellValue(o2oLine.arriveTime);
				row1.createCell(3).setCellValue(record.departureTime);
				row1.createCell(4).setCellValue(record.num);
				row1.createCell(5).setCellValue(record.order_id);
			} else
			{
				count++;
				HSSFRow row1 = sheet.createRow(count);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				row1.createCell(2).setCellValue(record.arriveTime);
				row1.createCell(3).setCellValue(record.departureTime);
				row1.createCell(4).setCellValue(record.num);
				row1.createCell(5).setCellValue(record.order_id);
			}
		}
	}

	public void printO2o(com.mirdar.O2O.Line O2oLine, HSSFSheet sheet, int courier_id) // 打印快递员的路线
	{
		ArrayList<Edge> edgeLine = O2oLine.edgeLine;
		int time = 0;
		for (int i = 0; i < edgeLine.size(); i++)
		{
			printRecord(edgeLine.get(i), time, courier_id, sheet);
			if (i < edgeLine.size() - 1)
				time += edgeLine.get(i).lineTime
						+ disPlace(edgeLine.get(i).end.place_id, edgeLine.get(i + 1).start.place_id);
		}

		for (int i = 0; i < O2oLine.line.size(); i++) // 打印o2o路径
		{
			com.mirdar.O2O.Record record = O2oLine.line.get(i);
			if (i == 0)
			{
				count++;
				HSSFRow row1 = sheet.createRow(count);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				row1.createCell(2).setCellValue(O2oLine.arriveTime);
				row1.createCell(3).setCellValue(record.departureTime);
				row1.createCell(4).setCellValue(record.num);
				row1.createCell(5).setCellValue(record.order_id);
			} else
			{
				count++;
				HSSFRow row1 = sheet.createRow(count);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				row1.createCell(2).setCellValue(record.arriveTime);
				row1.createCell(3).setCellValue(record.departureTime);
				row1.createCell(4).setCellValue(record.num);
				row1.createCell(5).setCellValue(record.order_id);
			}
		}

	}

	public void printRecord(Edge edge, int time, int courier_id, HSSFSheet sheet)
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

	// 当只有branch路线时拼接得到快递员
	public ArrayList<Couriers> getCouriers(Map<String, SiteInfo> siteInfo)
	{
		int branchRecord3 = 0;
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for (String key : siteInfo.keySet())
		{
			for (int i = 0; i < siteInfo.get(key).edges.size(); i++)
			{
				edges.add(siteInfo.get(key).edges.get(i));
			}
		}

		quickSort(edges, 0, edges.size() - 1); // 根据dis升序排序
		ArrayList<Couriers> couriersList = new ArrayList<Couriers>();
		int flag = 0;
		int k = 0; // 用来限制拼接的路线数量，这样增加快递数量，但是可以减少cost
		for (int i = 0; i < edges.size(); i++)
		{
			if (edges.get(i).flag == 0) // 没有被访问
			{
				Couriers couriers = new Couriers();
				couriersList.add(couriers);
				edges.get(i).flag = 1;
				couriers.line.add(edges.get(i));
				couriers.currentTime = edges.get(i).lineTime;

				removeEdge(siteInfo, edges.get(i));
				Edge tempEdge = edges.get(i);
				// 为了得到siteInfo中dis最大的元素
				ArrayList<Edge> siteEdges = siteInfo.get(tempEdge.nearSite.place_id).edges;
				if (siteEdges.size() == 0)
					continue;
				quickSort(siteEdges, 0, siteEdges.size() - 1);
				// 当满足条件的时候不断向后面添加路线
				// 加了两个限制条件，1. 连接的路线数据不超过3条，2. 连接的路线直接转移时间不超过40（调整快递员数量）
				while (disPlace(edges.get(i).end.place_id, siteEdges.get(0).start.place_id) < 33
						&& edges.get(i).line.get(edges.get(i).line.size() - 1).departureTime
								- edges.get(i).line.get(edges.get(i).line.size() - 1).arriveTime < 30
						&& couriers.currentTime + tempEdge.dis + siteEdges.get(0).lastArriveTime <= 720)
				{
					markEdge(edges, siteEdges.get(0));
					couriers.line.add(siteEdges.get(0));
					couriers.currentTime += tempEdge.dis + siteEdges.get(0).lineTime;
					tempEdge = siteEdges.get(0);
					siteEdges.remove(0); // 从将访问过的路线map数据中删除
					siteEdges = siteInfo.get(tempEdge.nearSite.place_id).edges;
					if (siteEdges.size() == 0)
						break;
					quickSort(siteEdges, 0, siteEdges.size() - 1);
					k++;
				}
			}
		}

		return couriersList;
	}

	// 将后加入的路线标记
	public void markEdge(ArrayList<Edge> edges, Edge edge)
	{
		for (int i = 0; i < edges.size(); i++)
		{
			if (edges.get(i).end.place_id.equals(edge.end.place_id))
			{
				edges.get(i).flag = 1;
				break;
			}
		}
	}

	// 从map中将起始路线删除
	public void removeEdge(Map<String, SiteInfo> siteInfo, Edge edge)
	{
		for (int i = 0; i < siteInfo.get(edge.start.place_id).edges.size(); i++)
		{
			if (siteInfo.get(edge.start.place_id).edges.get(i).end.place_id.equals(edge.end.place_id))
			{
				siteInfo.get(edge.start.place_id).edges.remove(i);
				break;
			}
		}
	}

	// 快牌，用来找到每一代中最大的fitness的个体
	public void quickSort(ArrayList<Edge> edges, int s, int e)
	{
		if (s < e)
		{
			int m = partition(edges, s, e);
			quickSort(edges, s, m - 1);
			quickSort(edges, m + 1, e);
		}
	}

	public int partition(ArrayList<Edge> edges, int s, int e) // 以第一个元素作为分割元素
	{
		int m = s;
		Edge edge = edges.get(s);
		for (int i = s + 1; i <= e; i++)
		{
			if (edges.get(i).dis <= edge.dis)
			{
				m++;
				Edge temp = edges.get(i);
				edges.set(i, edges.get(m));
				edges.set(m, temp);
			}
		}

		Edge temp = edges.get(m);
		edges.set(m, edges.get(s));
		edges.set(s, temp);

		return m;
	}

	public void printCouriers(ArrayList<Couriers> couriers, int courier_id, HSSFSheet sheet)
	{
		for (int i = 0; i < couriers.size(); i++)
		{
			printCourier(couriers.get(i), courier_id + i, sheet);
			// System.out.println();
		}
	}

	public void printCourier(Couriers courier, int courier_id, HSSFSheet sheet)
	{
		int time = 0;
		for (int i = 0; i < courier.line.size(); i++)
		{
			printRecord(courier.line.get(i), time, courier_id, sheet);
			if (i < courier.line.size() - 1)
				time += courier.line.get(i).lineTime + courier.line.get(i).dis;
			else
				time += courier.line.get(i).lastArriveTime;
		}
		branchTime += time;
	}

	public void printRecord(Record record, int time)
	{
		System.out.println(record.place_id + " " + (record.arriveTime + time) + " " + (record.departureTime + time)
				+ " " + record.num);
	}

	public int disPlace(String name1, String name2) // 两点之间的距离
	{
		int cost = 0;
		cost = (int) (Math
				.round(2 * 6378137
						* Math.asin(
								Math.sqrt(
										Math.pow(Math.sin(
												Math.PI / 180.0 * (placeMap.get(name1).lan - placeMap.get(name2).lan)
														/ 2),
												2)
												+ Math.cos(Math.PI / 180.0 * placeMap.get(name1).lan)
														* Math.cos(
																Math.PI / 180.0 * placeMap.get(name2).lan)
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
