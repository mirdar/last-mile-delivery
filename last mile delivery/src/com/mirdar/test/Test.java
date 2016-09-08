package com.mirdar.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.mirdar.GA.Chomo;
import com.mirdar.GA.GA;
import com.mirdar.O2O.Courier;
import com.mirdar.O2O.DealO2O;
import com.mirdar.O2O.Line;
import com.mirdar.O2O.Order;
import com.mirdar.O2O.ReadData;
import com.mirdar.O2O.Record;
import com.mirdar.O2O.Shop;
import com.mirdar.O2O.Spot;

public class Test
{

	public Map<String, Shop> shopMap;
	public Map<String, Spot> spotMap;
	public Map<String, Site> siteMap;
	Map<String, Place> placeMap = new HashMap<String, Place>();
	Map<String, ArrayList<RRecord>> recordMap;
	public int count = 0; // o2o快递员编号
	public int branchCount = 0; // 电商快递员
	Map<Integer, String> courier = new HashMap<Integer, String>();

	public static void main(String[] args) throws IOException
	{
		String fileShop = "F:\\ML\\last mile delivery/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery/spot.csv";
		String fileSite = "F:\\ML\\last mile delivery/site.csv";
		String fileOrder = "F:\\ML\\last mile delivery/shop_data.csv";
		String fileCourier = "F:\\ML\\last mile delivery/courier.csv";
		String fileResult = "F:\\ML\\last mile delivery/baseResult.csv";
		ReadData readData = new ReadData();

		Test test = new Test();
		DealO2O o2o = new DealO2O();
		test.shopMap = o2o.shopMap = readData.readShop(fileShop);
		test.spotMap = o2o.spotMap = readData.readSpot(fileSpot);
		test.siteMap = readData.readSite(fileSite);
		test.placeMap = readData.readPlace(fileSite, test.placeMap);
		test.placeMap = readData.readPlace(fileSpot, test.placeMap);
		test.placeMap = readData.readPlace(fileShop, test.placeMap);
		test.recordMap = readData.readRRecord(fileResult);
		o2o.orderMap = readData.readOrder(fileOrder);
		test.courier = readData.readCourier(fileCourier);

		o2o.print(o2o.shopMap, o2o.spotMap, o2o.orderMap); // 数据读取正常

		ArrayList<Courier> couriers = o2o.assignmentOrder();
		System.out.println("需要快递员：" + couriers.size());
		int orderNum = 0;
		for (int i = 0; i < couriers.size(); i++)
		{
			o2o.allTime += couriers.get(i).current_time;
			orderNum += couriers.get(i).list.size();
			System.out.println("快递员" + (i + 1) + ":" + " 快递员当前时间： "
					+ (couriers.get(i).current_time - couriers.get(i).last_stay_time));
			for (int j = 0; j < couriers.get(i).list.size(); j++)
				System.out.print(couriers.get(i).list.get(j) + " ");
			System.out.println();
		}

		String filename = "F:/ML/last mile delivery/branch_data2.csv";
		GA ga = new GA();
		int cars = 0;
		ga.allDataMap = ga.readAllData(filename);
		// System.out.println(ga.allDataMap.size()+" "+ga.len.size());
		int orderSize = 0;
		for (String key1 : ga.allDataMap.keySet())
		{
			// System.out.println("网点为："+key1);
			// System.out.println("染色体长度："+ga.len.get(key1));
			ga.dataMap = ga.allDataMap.get(key1);
			ga.length = ga.len.get(key1);
			ga.bestChomo = new ArrayList<Chomo>();
			ArrayList<Chomo> pop = ga.getInitPop(); // 初始群体生成成功

			ArrayList<Chomo> newPop = new ArrayList<Chomo>();
			for (int i = 0; i < ga.maxGen; i++)
			{
				newPop = ga.select(pop);
				newPop = ga.crossAndVar(newPop);
				pop = newPop;
			}
			orderSize += ga.bestChomo.get(ga.bestChomo.size() - 1).chomo.length; // 染色体长度正常
			cars += ga.printChomo(ga.bestChomo.get(ga.bestChomo.size() - 1)); // 保存了至今出现最后的染色体
		}
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("experiment");
		HSSFSheet sheet2 = wb.createSheet("experiment2");
		HSSFRow row1 = sheet.createRow(test.count);
		// 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
		// HSSFCell cell=row1.createCell(0);
		row1.createCell(0).setCellValue("Courier_id");
		row1.createCell(1).setCellValue("Addr");
		row1.createCell(2).setCellValue("Arrival_time");
		row1.createCell(3).setCellValue("Departure");
		row1.createCell(4).setCellValue("Amount");
		row1.createCell(5).setCellValue("Order_id");

		// HSSFRow row2=sheet2.createRow(test.branchCount);
		// //创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
		//// HSSFCell cell=row1.createCell(0);
		// row2.createCell(0).setCellValue("Courier_id");
		// row2.createCell(1).setCellValue("Addr");
		// row2.createCell(2).setCellValue("Arrival_time");
		// row2.createCell(3).setCellValue("Departure");
		// row2.createCell(4).setCellValue("Amount");
		// row1.createCell(5).setCellValue("Order_id");

		/*
		 * test.writeInFile(o2o.lines, ga.lines,sheet,sheet2); OutputStream
		 * output=new FileOutputStream("F:/ML/last mile delivery/o2o.xls");
		 * wb.write(output); output.close(); wb.close();
		 */
		test.connectO2oAndBranch(o2o.lines, ga.lines, sheet);
		OutputStream output = new FileOutputStream("F:/ML/last mile delivery/O2O.xls");
		wb.write(output);
		output.close();
		wb.close();

		System.out.println("已经将" + orderNum / 2 + "个o2o订单全部配送");
		System.out.println(o2o.couriersInitTime.size());
		System.out.println(o2o.allTime - 210 * 224);
		System.out.println(o2o.lines.size());
		System.out.println("电商订单总共需要快递员数量为： " + cars);
		System.out.println(ga.allTime);
		System.out.println(test.siteMap.size());
		System.out.println(test.siteMap.get("A116").site_id);
		System.out.println("ga.lines.size: " + ga.lines.size());
		System.out.println("o2o.lines.size: " + o2o.lines.size());
		System.out.println("检验输出的订单是否全  recordSize: " + ga.recordSize);
		System.out.println("这时所有染色体的长度和，应与上面订单数相等  orderSize: " + orderSize);

		// System.out.println("cost: "+test.valid(test.recordMap));

	}

	// 最Base的结果
	public void writeInFile(ArrayList<Line> lineO2o, Map<String, ArrayList<com.mirdar.GA.Line>> lineBranch,
			HSSFSheet sheet, HSSFSheet sheet2)
	{
		for (int i = 0; i < lineO2o.size(); i++) // 所有的o2o路线
		{
			int nearestSiteDis = Integer.MAX_VALUE;
			String nearestSite = null;
			for (String key : lineBranch.keySet()) // 找到离该商户最近的网点
			{
				for (int j = 0; j < lineBranch.get(key).size(); j++)
				{
					// 有些o2o订单无法满足从某配送点到达还要小于最小时间,这里选择配送的快递员的时候有问题
					// 需要修改，如何为o2o商户选择快递员呢，以及该快递员的路径
					if (disSite_Shop(key, lineO2o.get(i).shop_id) < nearestSiteDis)
					{
						nearestSiteDis = disSite_Shop(key, lineO2o.get(i).shop_id);
						nearestSite = key;
					}
				}
			}
			System.out.println("site_id: " + nearestSite);
			lineO2o.get(i).arriveTime = nearestSiteDis;

			// 打印O2o的路线
			printBaseO2o(lineO2o.get(i), sheet, i);
			System.out.println();
		}

		int branchCourierId = lineO2o.size() - 1; // 快递员产生了重叠ID
		for (String key : lineBranch.keySet())
		{
			branchCourierId++;
			int time = 0;
			for (int i = 0; i < lineBranch.get(key).size(); i++) // 电商订单的每一路线第一个记录都是网点
			{
				System.out.println(
						time + " +" + lineBranch.get(key).get(i).time + " -" + lineBranch.get(key).get(i).endToStart);
				if (time + lineBranch.get(key).get(i).time - lineBranch.get(key).get(i).endToStart > 720) // 此时就应该增加一个快递员
				{
					branchCourierId++;
					time = 0;
					printBaseBranch(lineBranch.get(key).get(i), sheet, branchCourierId, time);
					time += lineBranch.get(key).get(i).time;
				} else // 该快递员继续路线
				{
					printBaseBranch(lineBranch.get(key).get(i), sheet, branchCourierId, time);
					time += lineBranch.get(key).get(i).time;
				}
			}

		}
	}

	public void printBaseBranch(com.mirdar.GA.Line line, HSSFSheet sheet, int courier_id, int time)
	{
		int times = time;
		for (int i = 0; i < line.line.size(); i++)
		{
			com.mirdar.GA.Record record = line.line.get(i);
			if (i == 0)
			{
				count++;
				HSSFRow row1 = sheet.createRow(count);
				// 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
				// HSSFCell cell=row1.createCell(0);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				row1.createCell(2).setCellValue(time);
				time += record.departureTime - record.arriveTime;
				row1.createCell(3).setCellValue(time);
				row1.createCell(4).setCellValue(record.num);
				row1.createCell(5).setCellValue(record.order_id);
			} else
			{
				count++;
				HSSFRow row1 = sheet.createRow(count);
				// 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
				// HSSFCell cell=row1.createCell(0);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				time += record.arriveTime - line.line.get(i - 1).departureTime;
				row1.createCell(2).setCellValue(time);
				time += record.departureTime - record.arriveTime;
				row1.createCell(3).setCellValue(time);
				row1.createCell(4).setCellValue(record.num);
				row1.createCell(5).setCellValue(record.order_id);
			}
			// System.out.println();
		}
		time = times;
	}

	public void printBaseO2o(Line o2oLine, HSSFSheet sheet, int courier_id)
	{
		for (int i = 0; i < o2oLine.line.size(); i++)
		{
			Record record = o2oLine.line.get(i);
			if (i == 0)
			{
				count++;
				HSSFRow row1 = sheet.createRow(count);
				// 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
				// HSSFCell cell=row1.createCell(0);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				row1.createCell(2).setCellValue(o2oLine.arriveTime);
				row1.createCell(3).setCellValue(record.departureTime);
				row1.createCell(4).setCellValue(record.num);
				row1.createCell(5).setCellValue(record.order_id);
				// System.out.print(record.place_id+" "+o2oLine.arriveTime+"
				// "+record.departureTime+" "+
				// record.num+" "+record.order_id);
			} else
			{
				count++;
				HSSFRow row1 = sheet.createRow(count);
				// 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
				// HSSFCell cell=row1.createCell(0);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				row1.createCell(2).setCellValue(record.arriveTime);
				row1.createCell(3).setCellValue(record.departureTime);
				row1.createCell(4).setCellValue(record.num);
				row1.createCell(5).setCellValue(record.order_id);
				// System.out.print(record.place_id+" "+record.arriveTime+"
				// "+record.departureTime+" "+
				// record.num+" "+record.order_id);
			}
			// System.out.println();
		}
	}

	// ------------------------------------------------------------以下是将o2o订单与电商订单一起考虑

	// 当o2o路径无法加如其他电商路径时，此时，快递员从最近的网点出发到达
	public void printO2OBase(Line lineO2o, Map<String, ArrayList<com.mirdar.GA.Line>> lineBranch, HSSFSheet sheet,
			int courier_id)
	{
		int nearestSiteDis = Integer.MAX_VALUE;
		String nearestSite = null;
		for (String key : lineBranch.keySet()) // 找到离该商户最近的网点
		{
			for (int j = 0; j < lineBranch.get(key).size(); j++)
			{
				// 有些o2o订单无法满足从某配送点到达还要小于最小时间,这里选择配送的快递员的时候有问题
				// 需要修改，如何为o2o商户选择快递员呢，以及该快递员的路径
				if (disSite_Shop(key, lineO2o.shop_id) < nearestSiteDis)
				{
					nearestSiteDis = disSite_Shop(key, lineO2o.shop_id);
					nearestSite = key;
				}
			}
		}
		System.out.println("site_id: " + nearestSite);
		lineO2o.arriveTime = nearestSiteDis;

		// 打印O2o的路线
		printBaseO2o(lineO2o, sheet, courier_id);
	}

	// 将O2o订单与电商订单连接,只要出现两个订单连着的，就会出现错误
	public void connectO2oAndBranch(ArrayList<Line> lineO2o, Map<String, ArrayList<com.mirdar.GA.Line>> lineBranch,
			HSSFSheet sheet)
	{
		int spotNull = 0;
		for (int i = 0; i < lineO2o.size(); i++) // 所有的o2o路线
		{

			// 改变逻辑
			int nearestSpotDis = Integer.MAX_VALUE;
			int longLine = 0;
			String nearestSpot = null;
			String nearestSite = null;
			int nearestLine = 0;
			for (String key : lineBranch.keySet()) // 找到离该商户最近的网点
			{
				for (int j = 0; j < lineBranch.get(key).size(); j++)
				{
					// 有些o2o订单无法满足从某配送点到达还要小于最小时间,这里选择配送的快递员的时候有问题
					// 需要修改，如何为o2o商户选择快递员呢，以及该快递员的路径
					if (lineBranch.get(key).get(j).time - lineBranch.get(key).get(j).endToStart > longLine
							&& lineBranch.get(key).get(j).time - lineBranch.get(key).get(j).endToStart
									+ disSpot_Shop(lineBranch.get(key).get(j).spot_id,
											lineO2o.get(i).shop_id) <= lineO2o.get(i).earliestTime)
					{
						nearestSpotDis = disSpot_Shop(lineBranch.get(key).get(j).spot_id, lineO2o.get(i).shop_id);
						longLine = lineBranch.get(key).get(j).time - lineBranch.get(key).get(j).endToStart;
						nearestSpot = lineBranch.get(key).get(j).spot_id;
						nearestSite = key;
						nearestLine = j;
					}
				}
				// System.out.println("lineBranch.get("+key+").size():
				// "+lineBranch.get(key).size());
			}
			System.out.println("spot_id: " + nearestSpot);
			if (nearestSpot == null)
			{
				spotNull = spotNull + lineO2o.get(i).line.size();
				printO2OBase(lineO2o.get(i), lineBranch, sheet, i);
			} else
			{
				// 偶尔会出现有些商户找不到满足条件的快递员，此时没有另外生成快递员，而是增加迭代此时
				com.mirdar.GA.Line line = lineBranch.get(nearestSite).get(nearestLine);
				ArrayList<com.mirdar.GA.Line> branchToO2oLine = new ArrayList<com.mirdar.GA.Line>();
				System.out.println("lineO2o.get(i).earliestTime: " + lineO2o.get(i).earliestTime);
				System.out.println("nearestSpotDis: " + nearestSpotDis);
				System.out.println("line.time: " + line.time);
				System.out.println("line.endToStart： " + line.endToStart);

				int o2oEarliestTime = lineO2o.get(i).earliestTime - nearestSpotDis - line.time + line.endToStart; // 背包的大小，减去了最近的一条路径
				lineBranch.get(nearestSite).remove(nearestLine);
				System.out.println("o2oEarliestTime: " + o2oEarliestTime);
				System.out.println("lineBranch.get(nearestSite).size: " + lineBranch.get(nearestSite).size());

				int arrvieShopTime = line.time + nearestSpotDis - line.endToStart; // 该快递员什么时候到商户，
				if (o2oEarliestTime < 10) // 剩余的背包大小太小，无法加入
					;
				else
				{
					branchToO2oLine = lineInO2o(lineBranch.get(nearestSite), o2oEarliestTime); // 该商户快递员之前走过的路线
					// branchToO2oLine.add(line);

					for (int k = 0; k < branchToO2oLine.size(); k++)
					{
						arrvieShopTime += branchToO2oLine.get(k).time;
					}
				}
				branchToO2oLine.add(line);
				lineO2o.get(i).arriveTime = arrvieShopTime; // 将该商户快递员之前的信息都加入商户路线中这里，arriveTime应该会小于订单开始时间
				lineO2o.get(i).branchToO2oLine = branchToO2oLine;
				lineO2o.get(i).spot_id = line.spot_id;

				// 打印O2o的路线
				printO2o(lineO2o.get(i), sheet, i);
			}
		}

		int branchCourierId = lineO2o.size() - 1; // 快递员产生了重叠ID
		for (String key : lineBranch.keySet())
		{
			branchCourierId++;
			int time = 0;
			for (int i = 0; i < lineBranch.get(key).size(); i++) // 电商订单的每一路线第一个记录都是网点
			{
				// System.out.println(time+"
				// +"+lineBranch.get(key).get(i).time+"
				// -"+lineBranch.get(key).get(i).endToStart);
				if (time + lineBranch.get(key).get(i).time - lineBranch.get(key).get(i).endToStart > 720) // 此时就应该增加一个快递员
				{
					branchCourierId++;
					time = 0;
					printBaseBranch(lineBranch.get(key).get(i), sheet, branchCourierId, time);
					time += lineBranch.get(key).get(i).time;
				} else // 该快递员继续路线
				{
					printBaseBranch(lineBranch.get(key).get(i), sheet, branchCourierId, time);
					time += lineBranch.get(key).get(i).time;
				}
			}

		}
		System.out.println();
		System.out.println("没有加branchLine 的 O2O订单数： " + spotNull
				+ "-----------------------------------------------------------------");
	}

	// 利用背包问题的求解方法，但是要保证一定是在o2oEarliestTime前到达，这时可能会产生无效解
	// 还是利用贪心方法，以离商户最近的配送点作为快递员最后的路线，然后向前匹配，或者思考其他方法
	public ArrayList<com.mirdar.GA.Line> lineInO2o(ArrayList<com.mirdar.GA.Line> lines, int o2oEarliestTime)
	{
		// 背包问题有错误
		ArrayList<com.mirdar.GA.Line> lineInO2o = new ArrayList<com.mirdar.GA.Line>(); // 用来保存o2o快递员之前配送的电商路线（只保存了终点）
		int[][] capacity = new int[o2oEarliestTime + 1][lines.size() + 1]; // 用二维数组，是为了将最终挑选了那些路线打印出来
		int[] flag = new int[lines.size()];

		for (int i = 0; i < o2oEarliestTime + 1; i++)
		{
			for (int j = 0; j < lines.size() + 1; j++)
				capacity[i][j] = 0;
		}
		for (int i = 0; i < lines.size(); i++) // 背包求解
		{
			for (int j = o2oEarliestTime; j >= 0; j--)
			{
				if (j - lines.get(i).time <= 0) // 越界
					break;
				if (capacity[j][i] < capacity[j - lines.get(i).time][i] + lines.get(i).time)
				{
					System.out.println("lines.get(" + i + ").time: " + lines.get(i).time);
					capacity[j][i + 1] = capacity[j - lines.get(i).time][i] + lines.get(i).time;
				} else
					capacity[j][i + 1] = capacity[j][i];
			}
		}
		int n = o2oEarliestTime;
		for (int i = lines.size(); i > 0; i--)
		{
			if (n <= 0)
				break;
			// 此时说明第i件商品未加入
			if (capacity[n][i] == capacity[n][i - 1])
			{
				flag[i - 1] = 0;
			} else // 第i件商品加入，需要减去第i件商品的重量
			{
				if (i == lines.size()) // 最后一条路径
				{
					flag[i - 1] = 1;
					System.out.println("o2oEarliestTime: " + o2oEarliestTime + " lines.get(i-1).time: "
							+ lines.get(i - 1).time + "-----------------------------------------------------");
					n = n - lines.get(i - 1).time;
				} else // 中间路径 如 0 1 0 ，需要首先0 !=1 ，所以直接i-1会得到0，而不是1
				{
					flag[i - 2] = 1;
					System.out.println("o2oEarliestTime: " + o2oEarliestTime + " lines.get(i-2).time: "
							+ lines.get(i - 2).time + "-----------------------------------------------------");
					n = n - lines.get(i - 2).time;
				}

			}
		}

		for (int i = 0; i < flag.length; i++) // 将加入背包的路线记录
		{
			if (flag[i] == 1)
			{
				lineInO2o.add(lines.get(i));
			}
			System.out.print(lines.get(i).time + "  ");
		}

		int m = 0;
		for (int i = 0; i < flag.length; i++) // 去除商户快递员的路线,当有多条路线时，这里也会出先删除错误
		{
			if (flag[i] == 1)
			{
				lines.remove(i - m);
				m++;
			}
		}

		return lineInO2o;
	}

	public void printO2o(Line O2oLine, HSSFSheet sheet, int courier_id) // 打印快递员的路线
	{
		ArrayList<com.mirdar.GA.Line> branchToO2oLine = O2oLine.branchToO2oLine;
		int arriveTime = 0;
		int oldArriveTime = 0;
		for (int i = 0; i < branchToO2oLine.size(); i++)
		{
			if (branchToO2oLine.get(i).spot_id != O2oLine.spot_id) // 打印前面几个branch路径
			{
				for (int j = 0; j < branchToO2oLine.get(i).line.size(); j++)
				{
					if (j == 0)
						oldArriveTime = branchToO2oLine.get(i).line.get(j).arriveTime;
					branchToO2oLine.get(i).line.get(j).arriveTime = branchToO2oLine.get(i).line.get(j).arriveTime
							- oldArriveTime + arriveTime;
					branchToO2oLine.get(i).line.get(j).departureTime = branchToO2oLine.get(i).line.get(j).departureTime
							- oldArriveTime + arriveTime;
					// System.out.println(branchToO2oLine.get(i).line.get(j).place_id+"
					// "+branchToO2oLine.get(i).line.get(j).arriveTime+" "+
					// branchToO2oLine.get(i).line.get(j).departureTime+"
					// "+branchToO2oLine.get(i).line.get(j).num);
					count++;
					HSSFRow row1 = sheet.createRow(count);
					// 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
					// HSSFCell cell=row1.createCell(0);
					row1.createCell(0).setCellValue(courier.get(courier_id));
					row1.createCell(1).setCellValue(branchToO2oLine.get(i).line.get(j).place_id);
					row1.createCell(2).setCellValue(branchToO2oLine.get(i).line.get(j).arriveTime);
					row1.createCell(3).setCellValue(branchToO2oLine.get(i).line.get(j).departureTime);
					row1.createCell(4).setCellValue(branchToO2oLine.get(i).line.get(j).num);
					row1.createCell(5).setCellValue(branchToO2oLine.get(i).line.get(j).order_id);
				}
				arriveTime += branchToO2oLine.get(i).time;
			}
		}

		for (int i = 0; i < branchToO2oLine.size(); i++) // 打印最后一条路径
		{
			if (branchToO2oLine.get(i).spot_id == O2oLine.spot_id)
			{
				for (int j = 0; j < branchToO2oLine.get(i).line.size(); j++)
				{
					if (j == 0)
						oldArriveTime = branchToO2oLine.get(i).line.get(j).arriveTime;
					branchToO2oLine.get(i).line.get(j).arriveTime = branchToO2oLine.get(i).line.get(j).arriveTime
							- oldArriveTime + arriveTime;
					branchToO2oLine.get(i).line.get(j).departureTime = branchToO2oLine.get(i).line.get(j).departureTime
							- oldArriveTime + arriveTime;
					// System.out.println(branchToO2oLine.get(i).line.get(j).place_id+"
					// "+branchToO2oLine.get(i).line.get(j).arriveTime+" "+
					// branchToO2oLine.get(i).line.get(j).departureTime+"
					// "+branchToO2oLine.get(i).line.get(j).num);
					count++;
					HSSFRow row1 = sheet.createRow(count);
					// 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
					// HSSFCell cell=row1.createCell(0);
					row1.createCell(0).setCellValue(courier.get(courier_id));
					row1.createCell(1).setCellValue(branchToO2oLine.get(i).line.get(j).place_id);
					row1.createCell(2).setCellValue(branchToO2oLine.get(i).line.get(j).arriveTime);
					row1.createCell(3).setCellValue(branchToO2oLine.get(i).line.get(j).departureTime);
					row1.createCell(4).setCellValue(branchToO2oLine.get(i).line.get(j).num);
					row1.createCell(5).setCellValue(branchToO2oLine.get(i).line.get(j).order_id);
				}
			}
		}

		for (int i = 0; i < O2oLine.line.size(); i++) // 打印o2o路径
		{
			Record record = O2oLine.line.get(i);
			if (i == 0)
			{
				// System.out.print(record.place_id+" "+O2oLine.arriveTime+"
				// "+record.departureTime+" "+
				// record.num+" "+record.order_id);
				count++;
				HSSFRow row1 = sheet.createRow(count);
				// 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
				// HSSFCell cell=row1.createCell(0);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				row1.createCell(2).setCellValue(O2oLine.arriveTime);
				row1.createCell(3).setCellValue(record.departureTime);
				row1.createCell(4).setCellValue(record.num);
				row1.createCell(5).setCellValue(record.order_id);
			} else
			{
				// System.out.print(record.place_id+" "+record.arriveTime+"
				// "+record.departureTime+" "+
				// record.num+" "+record.order_id);
				count++;
				HSSFRow row1 = sheet.createRow(count);
				// 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
				// HSSFCell cell=row1.createCell(0);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				row1.createCell(2).setCellValue(record.arriveTime);
				row1.createCell(3).setCellValue(record.departureTime);
				row1.createCell(4).setCellValue(record.num);
				row1.createCell(5).setCellValue(record.order_id);
			}
			// System.out.println();
		}

	}

	public int disSpot_Shop(String spot_id, String shop_id) // 两点之间的距离
	{
		int cost = 0;
		cost = (int) (Math.round(2
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
				/ 250));

		return cost;
	}

	public int disSite_Shop(String site_id, String shop_id) // 两点之间的距离
	{
		int cost = 0;
		cost = (int) (Math.round(2
				* 6378137 * Math
						.asin(Math
								.sqrt(Math
										.pow(Math.sin(
												Math.PI / 180.0 * (siteMap.get(site_id).lan - shopMap.get(shop_id).lan)
														/ 2),
												2)
										+ Math.cos(Math.PI / 180.0 * siteMap.get(site_id).lan)
												* Math.cos(
														Math.PI / 180.0 * shopMap.get(shop_id).lan)
												* Math.pow(
														Math.sin(
																Math.PI / 180.0
																		* (siteMap.get(site_id).lon
																				- shopMap.get(shop_id).lon)
																		/ 2),
														2)))
				/ 250));

		return cost;
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

	// 评分函数
	public int valid(Map<String, ArrayList<RRecord>> recordsMap, Map<String, Order> orders)
	{
		int eval = 0;
		int fault1 = 0;
		int fault2 = 0;
		int fault3 = 0;
		int fault4 = 0;
		int fault5 = 0;
		int fault6 = 0;
		int fault7 = 0;
		int fault8 = 0;
		int punish1 = 0;
		int punish2 = 0;
		int punish3 = 0;
		int punish4 = 0;
		int punish5 = 0;
		int punish6 = 0;
		int punish7 = 0;
		int punish8 = 0;
		int punish = 0;
		for (String key : recordsMap.keySet()) // 遍历每一个快递员
		{
			ArrayList<RRecord> records = recordsMap.get(key);
			// System.out.println(key+" .size(): "+records.size());
			for (int i = 0; i < records.size(); i++)
			{
				if (records.get(i).place_id.substring(0, 1).equals("A"))
				{
					if (records.get(i).arriveTime != records.get(i).departureTime) // A...停留时间应该为0
						System.out.println("A...离开时间与到达时间不一致");
					// A之前的点为B（只可能为A或B）时才需要验证
					if (i != 0 && Math.abs(disPlace(records.get(i - 1).place_id, records.get(i).place_id)
							- (records.get(i).arriveTime - records.get(i - 1).departureTime)) != 0)
					{
						// System.out.println(records.get(i).place_id + " 1: " +
						// records.get(i).order_id);
						eval = eval + 10 * Math.abs(disPlace(records.get(i - 1).place_id, records.get(i).place_id)
								- (records.get(i).arriveTime - records.get(i - 1).departureTime));
						fault1++;
						punish1 = punish1 + 10 * Math.abs(disPlace(records.get(i - 1).place_id, records.get(i).place_id)
								- (records.get(i).arriveTime - records.get(i - 1).departureTime));
					}
				} else if (records.get(i).place_id.substring(0, 1).equals("B")) // 两个点之间的距离是否正确,以及停留时间是否正确
				{
					if (Math.abs(disPlace(records.get(i - 1).place_id, records.get(i).place_id)
							- (records.get(i).arriveTime - records.get(i - 1).departureTime)) != 0)
					{
						// System.out.println(records.get(i).place_id + " 2: " +
						// records.get(i).order_id);
						eval = eval + 10 * Math.abs(disPlace(records.get(i - 1).place_id, records.get(i).place_id)
								- (records.get(i).arriveTime - records.get(i - 1).departureTime));
						fault2++;
						punish2 = punish2 + 10 * Math.abs(disPlace(records.get(i - 1).place_id, records.get(i).place_id)
								- (records.get(i).arriveTime - records.get(i - 1).departureTime));
					}
					if (Math.abs(Math.round(3 * Math.sqrt(Math.abs(records.get(i).num)) + 5)
							- (records.get(i).departureTime - records.get(i).arriveTime)) != 0)
					{
						// System.out.println(records.get(i).place_id + " num: "
						// + records.get(i).num + " 3: "
						// + records.get(i).order_id + " "
						// + (int) Math.round(3 * Math.sqrt(records.get(i).num)
						// + 5) + " != "
						// + (records.get(i).departureTime -
						// records.get(i).arriveTime));
						eval = eval + 10 * Math.abs((int) Math.round(3 * Math.sqrt(records.get(i).num) + 5)
								- (records.get(i).departureTime - records.get(i).arriveTime));
						fault3++;
						punish3 = punish3 + 10 * Math.abs((int) Math.round(3 * Math.sqrt(records.get(i).num) + 5)
								- (records.get(i).departureTime - records.get(i).arriveTime));
					}

					if (records.get(i).order_id.substring(0, 1).equals("E"))
					{
						// 到达o2o订单配送点的时间要晚于规定时间
						if (orders.get(records.get(i).order_id).delivery_time < records.get(i).arriveTime)
						{
							System.out.println(records.get(i).place_id + " 4" + records.get(i).order_id);
							eval = eval + 5 * Math
									.abs(orders.get(records.get(i).order_id).delivery_time - records.get(i).arriveTime);
							fault4++;
							punish4 = punish4 + 5 * Math
									.abs(orders.get(records.get(i).order_id).delivery_time - records.get(i).arriveTime);
						}

					}
					if (records.get(i).order_id.substring(0, 1).equals("F"))
					{
						if (720 < records.get(i).arriveTime)
						{
							// System.out.println(records.get(i).place_id + " 5
							// " + records.get(i).order_id);
							eval = eval + 5 * Math.abs(records.get(i).arriveTime - 720);
							fault5++;
							punish5 = punish5 + 5 * Math.abs(records.get(i).arriveTime - 720);
						}
					}

				} else if (records.get(i).place_id.substring(0, 1).equals("S"))
				{
					if (i != 0)
					{
						if (Math.abs(disPlace(records.get(i - 1).place_id, records.get(i).place_id)
								- (records.get(i).arriveTime - records.get(i - 1).departureTime)) != 0)
						{
							fault8++;
							eval = eval + 10 * Math.abs(disPlace(records.get(i - 1).place_id, records.get(i).place_id)
									- (records.get(i).arriveTime - records.get(i - 1).departureTime));
							punish8 = punish8
									+ 10 * Math.abs(disPlace(records.get(i - 1).place_id, records.get(i).place_id)
											- (records.get(i).arriveTime - records.get(i - 1).departureTime));
						}
						// 到达o2o商户的时间要晚于规定时间
						if (orders.get(records.get(i).order_id).pickup_time < records.get(i).arriveTime)
						{
							// System.out.println(records.get(i).place_id + " 6
							// " + records.get(i).order_id);
							eval = eval + 5 * Math
									.abs(orders.get(records.get(i).order_id).pickup_time - records.get(i).arriveTime);
							fault6++;
							punish6 = punish6 + 5 * Math
									.abs(orders.get(records.get(i).order_id).pickup_time - records.get(i).arriveTime);
						}
						else
						{
							punish7 = punish7 + Math
									.abs(orders.get(records.get(i).order_id).pickup_time - records.get(i).arriveTime);
						}

					}

				}
				if (i == records.size() - 1) // 加上每个快递员到达最后一个配送点的时间
				{
					eval = eval + records.get(i).departureTime;
				}
			}
		}
		System.out.println();
		System.out.println("fault1: " + fault1);
		System.out.println("fault2: " + fault2);
		System.out.println("fault3: " + fault3);
		System.out.println("fault4: " + fault4);
		System.out.println("fault5: " + fault5);
		System.out.println("fault6: " + fault6);
		System.out.println("fault7: " + fault7);
		System.out.println("fault8: " + fault8);
		System.out.println("punish1: " + punish1);
		System.out.println("punish2: " + punish2);
		System.out.println("punish3: " + punish3);
		System.out.println("punish4: " + punish4);
		System.out.println("punish5: " + punish5);
		System.out.println("punish6: " + punish6);
		System.out.println("punish7: " + punish7);
		System.out.println("punish8: " + punish8);
		punish = punish1 + punish2 + punish3 + punish4 + punish5 + punish6 + 0 + punish8;
		System.out.println("punish: " + punish);
		return eval;
	}

}
