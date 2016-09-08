package com.mirdar.CW;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.mirdar.BFS.GraphAlgorithm;
import com.mirdar.BFS.Vertex;
import com.mirdar.CKAlgorithm.CK;
import com.mirdar.CKAlgorithm.Dis;
import com.mirdar.GA.Point;
import com.mirdar.GA.Record;
import com.mirdar.O2O.Order;
import com.mirdar.O2O.ReadData;
import com.mirdar.O2O2.O2oCW2;
import com.mirdar.TabuSearch.Code;
import com.mirdar.TabuSearch.TabuSearch;
import com.mirdar.graph.Couriers;
import com.mirdar.graph.Edge;
import com.mirdar.graph.Graph;
import com.mirdar.graph.LineCompression;
import com.mirdar.graph.SiteInfo;
import com.mirdar.test.Place;
import com.mirdar.test.Valid;

//o2o有148985的cost是必然的，因为配送送时间一定会超过规定时间

public class CW2
{

	public Map<String, Place> placeMap = new HashMap<String, Place>();
	int count;
	Map<Integer, String> courier = new HashMap<Integer, String>();
	Map<String, Point> spots = new HashMap<String, Point>();
	int courier_id = 0;
	Map<String, Order> o2oOrder = new HashMap<String, Order>();

	public static void main(String[] args) throws IOException
	{
		String fileShop = "F:\\ML\\last mile delivery\\part 2/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery\\part 2/spot.csv";
		String fileSite = "F:\\ML\\last mile delivery\\part 2/site.csv";
		String fileOrder = "F:\\ML\\last mile delivery\\part 2/o2o_data2.csv";
		String fileCourier = "F:\\ML\\last mile delivery\\part 2/courier.csv";
		ReadData readData = new ReadData();
		CW2 g = new CW2();
//		DealO2O o2o = new DealO2O();
		O2oCW2 o2oCW = new O2oCW2();
//		O2oCW3 o2oCW = new O2oCW3();
		LineCompression lineCompre = new LineCompression();
		Dis dis = new Dis();
		Graph graph = new Graph();

		dis.placeMap = readData.readPlace(fileSite, dis.placeMap);
		dis.placeMap = readData.readPlace(fileSpot, dis.placeMap);
		dis.placeMap = readData.readPlace(fileShop, dis.placeMap);

		g.placeMap = dis.placeMap;
//		o2o.orderMap = readData.readOrder(fileOrder);
//		o2o.shopMap = readData.readShop(fileShop);
//		o2o.spotMap = readData.readSpot(fileSpot);
		g.courier = readData.readCourier(fileCourier);
		g.o2oOrder = readData.readO2oOrder(fileOrder);

//		ArrayList<Courier> courierlist = o2o.assignmentOrder();
		Map<String,Order> orders = readData.readO2oOrder(fileOrder);
		o2oCW.orders = orders;
		o2oCW.graph(orders, dis);
		ArrayList<com.mirdar.O2O2.Vertex> edges = o2oCW.bindOperate(dis);
		o2oCW.edgeNum(edges);
		o2oCW.printLines();
		System.out.println("lines.size: "+o2oCW.lines.size());
		o2oCW.o2oTime();
		o2oCW.quickSort(o2oCW.lines,0,o2oCW.lines.size()-1);
		System.out.println("lines.size: "+o2oCW.lines.size());

//		Map<String,Order> orders = readData.readO2oOrder(fileOrder);
//		o2oCW.orderss = orders;
//		O2OIdea o2o = new O2OIdea();
//		Map<String,Order> orderss = readData.readO2oOrder(fileOrder);
//		o2o.orderMap = orderss;
//		o2o.orders = o2o.readOrder(fileOrder);
//		Map<String,Order> orderM = o2o.bind(dis);
//		o2oCW.orders = orderM;
//		System.out.println(orderM.size());
//		o2oCW.graph(orderM, dis);
//		ArrayList<com.mirdar.O2O2.Vertex> edges = o2oCW.bindOperate(dis);
//		o2oCW.edgeNum(edges);
//		o2oCW.printLines();
//		System.out.println("lines.size: "+o2oCW.lines.size());
//		o2oCW.o2oTime();
		String filename = "F:\\ML\\last mile delivery\\part 2/branch_data.csv";
		// GA3 ga = new GA3();
		TabuSearch ts = new TabuSearch();
		CK ck = new CK();
		ck.allDataMap = ck.readAllData(filename);
		g.setSpots(ck.allDataMap);
		for (String key1 : ck.allDataMap.keySet())
		{
			ck.dataMap = ck.allDataMap.get(key1);
			 ts.max_con_iter = 500 + 5 * ck.dataMap.size();
			 ts.max_iter = 20000 + 10 * ck.dataMap.size();
			 ts.max_cand_list = 150 + 2 * ck.dataMap.size();
			ts.dataMap = ck.dataMap;
			ArrayList<com.mirdar.CKAlgorithm.Line> lineCk = ck.cKAlgorithm(ck.dataMap, dis);
			ArrayList<Integer> codeList = ts.setCode(lineCk);
			Code code = new Code();
			code.init(codeList, ts.codeEva(codeList, dis));
			System.out.println("len: " + ts.codeLength(code.codeList));
			System.out.println("eval: " + ts.codeEva(code.codeList, dis));
			ts.tabuTable = new int[ck.dataMap.size() + 1][ck.dataMap.size() + 1];
			int k = 0;
			if (ts.codeLength(code.codeList) == 1)
				ck.printPaths(lineCk, dis);
			else
			{
				Code newCode = ts.tabuSearch(code, dis);
				// System.out.println("len: " +
				// ts.codeLength(newCode.codeList));
				// System.out.println("eval: " + ts.codeEva(newCode.codeList,
				// dis));
				// while(k<1) //迭代三次
				// {
				// ts.tabuTable = new int[ck.dataMap.size() +
				// 1][ck.dataMap.size()+ 1];
				// code = newCode;
				// newCode = ts.tabuSearch(code, dis);
				// k++;
				// }
				ts.printCode(newCode.codeList);
				System.out.println("len: " + ts.codeLength(newCode.codeList));
				System.out.println("eval: " + ts.codeEva(newCode.codeList, dis));
				ArrayList<com.mirdar.CKAlgorithm.Line> lineCk2 = ts.deCode(newCode.codeList);
				ck.printPaths(lineCk2, dis);
			}
		}
		System.out.println("CW+TabuSearch 1 is over. ");
		Valid valid = new Valid();
		int result = Integer.MAX_VALUE;
		ArrayList<String> outputs = new ArrayList<String>();
		String bestOut = null;
		int k = 0;
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("experiment");
		HSSFRow row1 = sheet.createRow(g.count);
		row1.createCell(0).setCellValue("Courier_id");
		row1.createCell(1).setCellValue("Addr");
		row1.createCell(2).setCellValue("Arrival_time");
		row1.createCell(3).setCellValue("Departure");
		row1.createCell(4).setCellValue("Amount");
		row1.createCell(5).setCellValue("Order_id");
		int[] permutation = g.getPermutation(o2oCW.lines.size());
		ArrayList<com.mirdar.O2O.Line> lines = new ArrayList<com.mirdar.O2O.Line>();
		System.out.println("o2olines.size: " + o2oCW.lines.size());
		System.out.println("ck.lines.size: " + ck.lines.size());
		Map<String, SiteInfo> siteInfo = lineCompre.lineCompression(ck.lines, g.placeMap);
		System.out.println("siteInfo: " + siteInfo.size());
		Map<String, Represent> represents = g.edgeCompression(siteInfo);
		System.out.println("represents.size: " + represents.size());
		GraphAlgorithm graphAlgorithm = g.graphInit(represents);
		System.out.println("graphInit 1 is over. ");
		for (int i = 0; i < permutation.length; i++)
		{
			g.graphAddO2o(o2oCW.lines.get(i), graphAlgorithm, represents, dis, sheet, lines, siteInfo);
		}
		System.out.println("graphAddO2o is over.");
		System.out.println("前面无法加入branch路线的o2o路线数量： " + lines.size());
		System.out.println("节点数量： " + graphAlgorithm.vertexMap.size());

		g.getNewSiteInfo(siteInfo, graphAlgorithm, represents);
		Map<String, ArrayList<String>> maps = g.getSiteSpot(siteInfo);
		System.out.println("siteInfo.size(): " + siteInfo.size());
		System.out.println("maps.size: " + maps.size());
		g.insertO2oInBranch2(maps, lines, sheet);
		System.out.println("insertO2oInBranch2 is over.");

		Map<String, Map<Integer, Point>> allDataMap = g.setAllDataMap(maps);
		System.out.println("剩余的site  allDataMap.size: " + allDataMap.size());

		ts = new TabuSearch();
		ck = new CK();
		for (String key2 : allDataMap.keySet())
		{
			ck.dataMap = allDataMap.get(key2);
			 ts.max_con_iter = 500 + 5 * ck.dataMap.size();
			 ts.max_iter = 20000 + 10 * ck.dataMap.size();
			 ts.max_cand_list = 150 + 2 * ck.dataMap.size();
			ts.dataMap = ck.dataMap;
			ArrayList<com.mirdar.CKAlgorithm.Line> lineCk = ck.cKAlgorithm(ck.dataMap, dis);
			ArrayList<Integer> codeList = ts.setCode(lineCk);
			Code code = new Code();
			code.init(codeList, ts.codeEva(codeList, dis));
			// System.out.println("len: " + ts.codeLength(code.codeList));
			// System.out.println("eval: " + ts.codeEva(code.codeList, dis));
			ts.tabuTable = new int[ck.dataMap.size() + 1][ck.dataMap.size() + 1];
			if (ts.codeLength(code.codeList) == 1)
				ck.printPaths(lineCk, dis);
			else
			{
				Code newCode = ts.tabuSearch(code, dis);
				ts.printCode(newCode.codeList);
				System.out.println("len: " + ts.codeLength(newCode.codeList));
				System.out.println("eval: " + ts.codeEva(newCode.codeList, dis));
				ArrayList<com.mirdar.CKAlgorithm.Line> lineCk2 = ts.deCode(newCode.codeList);
				ck.printPaths(lineCk2, dis);
			}

		}
		System.out.println("CW+TabuSearch 2 is over. ");
		System.out.println("ck.lines.size: " + ck.lines.size());
		Map<String, SiteInfo> siteInfo2 = lineCompre.lineCompression(ck.lines, g.placeMap);
		graph.courier = g.courier;
		graph.count = g.count;
		graph.placeMap = g.placeMap;
		ArrayList<Couriers> couriers = graph.getCouriers(siteInfo2);
		graph.printCouriers(couriers, g.courier_id, sheet);
		System.out.println("printCouriers is over.");
		String outPutFile = "F:\\ML\\last mile delivery\\part 2/result/result" + 12 + ".xls";
		System.out.println("rows: " + g.count);
		OutputStream output = new FileOutputStream(outPutFile);
		wb.write(output);
		output.close();
		wb.close();
	}

	public int[] getPermutation(int max) // 排列
	{
		int[] permutation = new int[max];
		for (int i = 0; i < max; i++)
			permutation[i] = i;
		Random rand = new Random();
		for (int k = 0; k < max / 4; k++)
		{
			int i1 = rand.nextInt(max);
			int i2 = rand.nextInt(max);
			int temp = permutation[i1];
			permutation[i1] = permutation[i2];
			permutation[i2] = temp;
		}

		return permutation;
	}

	// 这里大概可以减少3000cost
	public void insertO2oInBranch(Map<String, SiteInfo> siteInfo, ArrayList<com.mirdar.O2O.Line> lines)
	{

		System.out.println("lines.size: " + lines.size());
		System.out.println("siteInfo.size: " + siteInfo.size());
		for (int i = 0; i < lines.size(); i++)
		{
			int bestTime = Integer.MAX_VALUE;
			String bestSiteName = null;
			ArrayList<com.mirdar.GA.Record> bestRecord = new ArrayList<com.mirdar.GA.Record>();
			for (String key : siteInfo.keySet())
			{
				for (int j = 0; j < siteInfo.get(key).edges.size(); j++)
				{
					int t = lines.get(i).line.get(0).arriveTime;
					ArrayList<com.mirdar.GA.Record> record = new ArrayList<com.mirdar.GA.Record>();
					Edge edge = siteInfo.get(key).edges.get(j);
					int lineTime = 0;
					t = t - disPlace(lines.get(i).shop_id, edge.end.place_id);
					for (int k = edge.line.size() - 1; k >= 0; k--)
					{
						if (k == edge.line.size() - 1)
						{
							lineTime = edge.line.get(k).departureTime - edge.line.get(k).arriveTime;
							if (lineTime + disPlace(key, edge.line.get(k).place_id) > t)
								break;
							record.add(edge.line.get(k));
						} else
						{
							lineTime = edge.line.get(edge.line.size() - 1).departureTime - edge.line.get(k).arriveTime;
							if (lineTime + disPlace(key, edge.line.get(k).place_id) > t)
							{
								if (t - record.get(0).departureTime + record.get(record.size() - 1).arriveTime
										+ disPlace(lines.get(i).shop_id, edge.end.place_id) < bestTime)
								{
									bestSiteName = key;
									bestRecord = record;
									bestTime = t - record.get(0).departureTime
											+ record.get(record.size() - 1).arriveTime
											+ disPlace(lines.get(i).shop_id, edge.end.place_id);
								}
								break;
							}
							record.add(edge.line.get(k));
						}
					}
				}
			}

			// 先将record从branch中移除，注意site记录也要移除
			// 在打印record，并在后面附加o2oline （利用record，depar与arri的差值计算转移时间）
			// 最后打印siteInfo
			System.out.println("line.arriveTime: " + lines.get(i).line.get(0).arriveTime);
			System.out.println("附加在o2oline前的branch数量： " + bestRecord.size() + " siteName: " + bestSiteName
					+ " bestTime: " + bestTime);
			System.out.println(bestSiteName + " -> " + bestRecord.get(bestRecord.size() - 1).place_id + ": "
					+ disPlace(bestSiteName, bestRecord.get(bestRecord.size() - 1).place_id));
			System.out.println(bestRecord.get(bestRecord.size() - 1).place_id + " -> " + bestRecord.get(0).place_id
					+ ": " + (bestRecord.get(0).departureTime - bestRecord.get(bestRecord.size() - 1).arriveTime));
			System.out.println(bestRecord.get(0).place_id + " -> " + lines.get(i).shop_id + ": "
					+ disPlace(lines.get(i).shop_id, bestRecord.get(0).place_id));
			System.out.println("-----------------------------------------");
		}

	}

	// 改变搜索策略
	public void insertO2oInBranch2(Map<String, ArrayList<String>> siteSpots, ArrayList<com.mirdar.O2O.Line> lines,
			HSSFSheet sheet)
	{
		System.out.println("lines.size: " + lines.size());
		System.out.println("siteInfo.size: " + siteSpots.size());
		for (int i = 0; i < lines.size(); i++)
		{
			String nnSite = null;
			String nnSpot = null;
			int spotJ = -1;
			int dis = Integer.MAX_VALUE;
			for (String key : siteSpots.keySet())
			{
				for (int j = 0; j < siteSpots.get(key).size(); j++)
				{
					// 这里选择site剩余的spot大于10的作为前缀，需要调整
					//
					if (siteSpots.get(key).size() < 6)
						break;
					if (disPlace(lines.get(i).shop_id, siteSpots.get(key).get(j)) < dis)
					{
						dis = disPlace(lines.get(i).shop_id, siteSpots.get(key).get(j));
						nnSite = key;
						nnSpot = siteSpots.get(key).get(j);
						spotJ = j;
					}
				}
			}
			ArrayList<String> records = new ArrayList<String>();
			records.add(nnSpot);
			if (nnSpot != null)
			{
				siteSpots.get(nnSite).remove(spotJ);
				int lineTime = disPlace(nnSpot, lines.get(i).shop_id);
				int nSpotJ = -1;
				// int m = 0;
				// while(m < siteSpots.get(nnSite).size()) //将所有的spot都加入
				// {
				// String nSpot = null;
				// int ndis = Integer.MAX_VALUE;
				// for(int j=0;j<siteSpots.get(nnSite).size();j++)
				// {
				// if(disPlace(records.get(records.size()-1),
				// siteSpots.get(nnSite).get(j)) < ndis)
				// {
				// nSpot = siteSpots.get(nnSite).get(j);
				// ndis = disPlace(records.get(records.size()-1),
				// siteSpots.get(nnSite).get(j));
				// nSpotJ = j;
				// }
				// }
				// records.add(nSpot);
				// m++;
				// }
				// //不断去除spot
				// while(getTime(records,nnSite)+lineTime >
				// lines.get(i).line.get(0).arriveTime ||
				// getWeigth(records) > 140)
				// {
				// records.remove(records.size()-1);
				// }
				// System.out.println("records.size: "+records.size());
				// for(int k=0;k<records.size();k++)
				// {
				// for(int j=0;j<siteSpots.get(nnSite).size();j++)
				// {
				// if(siteSpots.get(nnSite).get(j).equals(records.get(k)))
				// {
				// siteSpots.get(nnSite).remove(j);
				// break;
				// }
				// }
				// }

				while (getTime(records, nnSite) + lineTime < lines.get(i).line.get(0).arriveTime
						&& getWeigth(records) <= 140)
				{
					if (nSpotJ != -1)
						siteSpots.get(nnSite).remove(nSpotJ);
					String nSpot = null;
					int ndis = Integer.MAX_VALUE;

					for (int j = 0; j < siteSpots.get(nnSite).size(); j++)
					{
						if (disPlace(records.get(records.size() - 1), siteSpots.get(nnSite).get(j)) < ndis)
						{
							nSpot = siteSpots.get(nnSite).get(j);
							ndis = disPlace(records.get(records.size() - 1), siteSpots.get(nnSite).get(j));
							nSpotJ = j;
						}
					}
					if (nSpot != null)
						records.add(nSpot);
					else
						break;
				}
			}
			records.remove(records.size() - 1);

			System.out.println("siteName: " + nnSite);
			System.out.println("records.size: " + records.size());
			System.out.println("lines.size: " + lines.get(i).shop_id);
			printBindLine(nnSite, records, lines.get(i), courier_id, sheet);
			courier_id++;
		}

	}

	public void printBindLine(String siteName, ArrayList<String> records, com.mirdar.O2O.Line line, int courier_id,
			HSSFSheet sheet)
	{
		int time = 0;
		int arriveTime = 0;
		ArrayList<com.mirdar.GA.Record> recordList = new ArrayList<Record>();
		com.mirdar.GA.Record record = new com.mirdar.GA.Record();
		for (int i = records.size() - 1; i >= 0; i--)
		{
			record = new com.mirdar.GA.Record();
			record.place_id = siteName;
			record.arriveTime = arriveTime;
			record.departureTime = arriveTime;
			record.num = spots.get(records.get(i)).goods_num;
			record.order_id = spots.get(records.get(i)).order_id;
			recordList.add(record);
		}
		for (int j = records.size() - 1; j >= 0; j--)
		{
			if (j == records.size() - 1)
			{

				arriveTime += disPlace(siteName, records.get(j));
				record = new Record();
				record.place_id = records.get(j);
				record.arriveTime = arriveTime;
				arriveTime += Math.round(3 * Math.sqrt(spots.get(records.get(j)).goods_num) + 5);
				record.departureTime = arriveTime;
				record.num = -spots.get(records.get(j)).goods_num;
				record.order_id = spots.get(records.get(j)).order_id;
				recordList.add(record);
			} else
			{
				arriveTime += disPlace(records.get(j + 1), records.get(j));
				record = new Record();
				record.place_id = records.get(j);
				record.arriveTime = arriveTime;
				arriveTime += Math.round(3 * Math.sqrt(spots.get(records.get(j)).goods_num) + 5);
				record.departureTime = arriveTime;
				record.num = -spots.get(records.get(j)).goods_num;
				record.order_id = spots.get(records.get(j)).order_id;
				recordList.add(record);
			}
		}

		// 下面就是打印recordList，
		// 在将剩下的point进行ＣＫ算法
		if (records.size() != 0)
		{
			printRecord(recordList, time, sheet);
			time += recordList.get(recordList.size() - 1).departureTime - recordList.get(0).arriveTime; // 路径时间
//			time += disPlace(recordList.get(recordList.size() - 1).place_id, line.shop_id); // 转移时间
			time += disPlace(recordList.get(recordList.size() - 1).place_id, line.line.get(0).place_id); // 转移时间
		} else
		{
			time = line.line.get(0).arriveTime - 20;
		}

		printO2o(line, time, sheet);
	}

	public int getWeigth(ArrayList<String> records)
	{
		int weight = 0;
		for (int i = 0; i < records.size(); i++)
		{
			weight += spots.get(records.get(i)).goods_num;
		}

		return weight;
	}

	public int getTime(ArrayList<String> records, String site)
	{
		int time = 0;
		for (int i = 0; i < records.size(); i++)
		{
			time += Math.round(3 * Math.sqrt(spots.get(records.get(i)).goods_num)) + 5;
			if (i == records.size() - 1)
			{
				time += disPlace(records.get(i), site);
			} else
			{
				System.out.println(records.get(i) + "  " + records.get(i + 1));
				time += disPlace(records.get(i), records.get(i + 1));
			}
		}

		return time;
	}

	// 将branch line 打散
	public Map<String, ArrayList<String>> getSiteSpot(Map<String, SiteInfo> siteInfo)
	{
		Map<String, ArrayList<String>> maps = new HashMap<String, ArrayList<String>>();
		ArrayList<String> spots = null;
		for (String key : siteInfo.keySet())
		{
			spots = new ArrayList<String>();
			for (int i = 0; i < siteInfo.get(key).edges.size(); i++)
			{
				for (int j = 0; j < siteInfo.get(key).edges.get(i).line.size(); j++)
				{
					if (!siteInfo.get(key).edges.get(i).line.get(j).place_id.equals(key))
						spots.add(siteInfo.get(key).edges.get(i).line.get(j).place_id);
				}
			}
			maps.put(key, spots);
		}

		return maps;
	}

	// 去除绑定到o2oLine上的路线
	public void getNewSiteInfo(Map<String, SiteInfo> siteInfo, GraphAlgorithm graphAlgorithm,
			Map<String, Represent> represents)
	{
		for (String key : graphAlgorithm.vertexMap.keySet())
		{
			if (graphAlgorithm.vertexMap.get(key).bindorNot == -1) // 说明已经被绑定到o2oline上
			{
				for (int i = 0; i < siteInfo.get(represents.get(key).siteName).edges.size(); i++)
				{
					if (siteInfo.get(represents.get(key).siteName).edges.get(i).end.place_id.equals(key))
					{
						siteInfo.get(represents.get(key).siteName).edges.remove(i);
					}
				}
			}

		}
	}

	public void printRecord(ArrayList<Record> line, int time, HSSFSheet sheet)
	{
		for (int i = 0; i < line.size(); i++)
		{
			Record record = line.get(i);
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

	public void printO2oLine(ArrayList<String> bindLine, com.mirdar.O2O.Line line, HSSFSheet sheet,
			Map<String, Represent> represents)
	{
		int time = 0;
		for (int i = 0; i < bindLine.size(); i++)
		{
			printRecord(represents.get(bindLine.get(i)).line, time, sheet);
			time += represents.get(bindLine.get(i)).lineTime; // 路径时间
			if (i < bindLine.size() - 1)
				time += disPlace(bindLine.get(i), represents.get(bindLine.get(i + 1)).siteName); // 转移时间
		}
		time += disPlace(bindLine.get(bindLine.size() - 1), line.line.get(0).place_id);
		
		//应该是shop_id混乱了
		printO2o(line, time, sheet);
	}

	public void printO2o(com.mirdar.O2O.Line line, int time, HSSFSheet sheet)
	{
		// if(flag == 1)
		// {
		//// String nkey = null;
		// for(String key : siteInfo.keySet())
		// {
		// if(disPlace(key, line.shop_id) < time)
		// {
		// time = disPlace(key, line.shop_id);
		// }
		// }
		//
		// }
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
				row1.createCell(6).setCellValue(record.arriveTime);
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

	// 将o2o路线加入图中
	public void graphAddO2o(com.mirdar.O2O.Line line, GraphAlgorithm graphAlgorithm, Map<String, Represent> represents,
			Dis dis, HSSFSheet sheet, ArrayList<com.mirdar.O2O.Line> lines, Map<String, SiteInfo> siteInfo)
	{
		for (String key : represents.keySet())
		{
			int cost = 0;
			cost = disPlace(represents.get(key).spotName, line.shop_id);
			if (cost < 80)
				graphAlgorithm.addEdge(line.shop_id, represents.get(key).spotName, cost, cost);
		}
		System.out.println(line.shop_id + ": ");
		// 起始点为shop
		ArrayList<String> bindLine = new ArrayList<String>();
		Vertex v = graphAlgorithm.dijkstra(line.shop_id, represents, dis, line.line.get(0).arriveTime, bindLine, line,
				lines, o2oOrder);
		if (v != null)
		{
			printO2oLine(bindLine, line, sheet, represents);
			courier_id++;
		}
		// else
		// {
		// printO2o(line, line.line.get(0).arriveTime, courier_id,
		// sheet,1,siteInfo);
		// }
		System.out.println();
	}

	public GraphAlgorithm graphInit(Map<String, Represent> represents)
	{
		GraphAlgorithm graphAlgorithm = new GraphAlgorithm();
		int i = 0;
		for (String key : represents.keySet())
		{
			for (String key2 : represents.keySet())
			{
				int cost = 0;
				if (key.equals(key2)) // 不与自身相连
					continue;
				else if (represents.get(key).siteName.equals(represents.get(key2).siteName)) // 与共享一个site的spot相连
				{
					cost = represents.get(key).lineTime
							+ disPlace(represents.get(key).spotName, represents.get(key).siteName)
							+ represents.get(key2).lineTime;
					graphAlgorithm.addEdge(represents.get(key2).spotName, represents.get(key).spotName, cost,
							disPlace(represents.get(key).spotName, represents.get(key).siteName));
					// System.out.println("i: " + i);
					i++;
				} else // 与其他site的spot相连
				{
					cost = represents.get(key).lineTime
							+ disPlace(represents.get(key).spotName, represents.get(key2).siteName)
							+ represents.get(key2).lineTime;
					// 这里设置<100的时候，会出现内存不足，应该是因为spot与site之间距离都比较近
					if (disPlace(represents.get(key).spotName, represents.get(key2).siteName) < 65)
						graphAlgorithm.addEdge(represents.get(key2).spotName, represents.get(key).spotName, cost,
								disPlace(represents.get(key).spotName, represents.get(key2).siteName));
					// System.out.println("i: " + i);
					i++;
				}
			}
		}
		System.out.println("i: " + i);
		return graphAlgorithm;
	}

	public Map<String, Map<Integer, Point>> setAllDataMap(Map<String, ArrayList<String>> maps)
	{
		Map<String, Map<Integer, Point>> allDataMap = new HashMap<String, Map<Integer, Point>>();
		Map<Integer, Point> dataMap = null;
		for (String key : maps.keySet())
		{
			if (maps.get(key).size() == 0)
				continue;
			System.out.println("key: " + key + "---------------------------");
			dataMap = new HashMap<Integer, Point>();
			allDataMap.put(key, dataMap);
			Point point = new Point();
			point.pointName = key;
			point.lon = placeMap.get(key).lon;
			point.lan = placeMap.get(key).lan;
			point.order_id = null;
			dataMap.put(0, point);
			for (int i = 0; i < maps.get(key).size(); i++)
			{
				point = spots.get(maps.get(key).get(i));
				dataMap.put(i + 1, point);
			}
		}

		return allDataMap;
	}

	public void setSpots(Map<String, Map<Integer, Point>> allDataMap)
	{
		for (String key : allDataMap.keySet())
		{
			for (Integer key2 : allDataMap.get(key).keySet())
			{
				if (key2 != 0)
					spots.put(allDataMap.get(key).get(key2).pointName, allDataMap.get(key).get(key2));
			}
		}
	}

	// 将branch的线路压缩到spot上，因为spot唯一
	public Map<String, Represent> edgeCompression(Map<String, SiteInfo> siteInfo)
	{
		Map<String, Represent> represents = new HashMap<String, Represent>();
		int size = 0;
		for (String key : siteInfo.keySet())
		{
			size += siteInfo.get(key).edges.size();
			for (int i = 0; i < siteInfo.get(key).edges.size(); i++)
			{
				Represent represent = new Represent();
				represent.siteName = key;
				represent.spotName = siteInfo.get(key).edges.get(i).end.place_id;
				represent.line = siteInfo.get(key).edges.get(i).line;
				represent.lineTime = siteInfo.get(key).edges.get(i).lineTime;
				represents.put(represent.spotName, represent);
			}
		}
		System.out.println("size: " + size);
		return represents;
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
