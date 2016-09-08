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
	public int count = 0; // o2o���Ա���
	public int branchCount = 0; // ���̿��Ա
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

		o2o.print(o2o.shopMap, o2o.spotMap, o2o.orderMap); // ���ݶ�ȡ����

		ArrayList<Courier> couriers = o2o.assignmentOrder();
		System.out.println("��Ҫ���Ա��" + couriers.size());
		int orderNum = 0;
		for (int i = 0; i < couriers.size(); i++)
		{
			o2o.allTime += couriers.get(i).current_time;
			orderNum += couriers.get(i).list.size();
			System.out.println("���Ա" + (i + 1) + ":" + " ���Ա��ǰʱ�䣺 "
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
			// System.out.println("����Ϊ��"+key1);
			// System.out.println("Ⱦɫ�峤�ȣ�"+ga.len.get(key1));
			ga.dataMap = ga.allDataMap.get(key1);
			ga.length = ga.len.get(key1);
			ga.bestChomo = new ArrayList<Chomo>();
			ArrayList<Chomo> pop = ga.getInitPop(); // ��ʼȺ�����ɳɹ�

			ArrayList<Chomo> newPop = new ArrayList<Chomo>();
			for (int i = 0; i < ga.maxGen; i++)
			{
				newPop = ga.select(pop);
				newPop = ga.crossAndVar(newPop);
				pop = newPop;
			}
			orderSize += ga.bestChomo.get(ga.bestChomo.size() - 1).chomo.length; // Ⱦɫ�峤������
			cars += ga.printChomo(ga.bestChomo.get(ga.bestChomo.size() - 1)); // �����������������Ⱦɫ��
		}
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("experiment");
		HSSFSheet sheet2 = wb.createSheet("experiment2");
		HSSFRow row1 = sheet.createRow(test.count);
		// ������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
		// HSSFCell cell=row1.createCell(0);
		row1.createCell(0).setCellValue("Courier_id");
		row1.createCell(1).setCellValue("Addr");
		row1.createCell(2).setCellValue("Arrival_time");
		row1.createCell(3).setCellValue("Departure");
		row1.createCell(4).setCellValue("Amount");
		row1.createCell(5).setCellValue("Order_id");

		// HSSFRow row2=sheet2.createRow(test.branchCount);
		// //������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
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

		System.out.println("�Ѿ���" + orderNum / 2 + "��o2o����ȫ������");
		System.out.println(o2o.couriersInitTime.size());
		System.out.println(o2o.allTime - 210 * 224);
		System.out.println(o2o.lines.size());
		System.out.println("���̶����ܹ���Ҫ���Ա����Ϊ�� " + cars);
		System.out.println(ga.allTime);
		System.out.println(test.siteMap.size());
		System.out.println(test.siteMap.get("A116").site_id);
		System.out.println("ga.lines.size: " + ga.lines.size());
		System.out.println("o2o.lines.size: " + o2o.lines.size());
		System.out.println("��������Ķ����Ƿ�ȫ  recordSize: " + ga.recordSize);
		System.out.println("��ʱ����Ⱦɫ��ĳ��Ⱥͣ�Ӧ�����涩�������  orderSize: " + orderSize);

		// System.out.println("cost: "+test.valid(test.recordMap));

	}

	// ��Base�Ľ��
	public void writeInFile(ArrayList<Line> lineO2o, Map<String, ArrayList<com.mirdar.GA.Line>> lineBranch,
			HSSFSheet sheet, HSSFSheet sheet2)
	{
		for (int i = 0; i < lineO2o.size(); i++) // ���е�o2o·��
		{
			int nearestSiteDis = Integer.MAX_VALUE;
			String nearestSite = null;
			for (String key : lineBranch.keySet()) // �ҵ�����̻����������
			{
				for (int j = 0; j < lineBranch.get(key).size(); j++)
				{
					// ��Щo2o�����޷������ĳ���͵㵽�ﻹҪС����Сʱ��,����ѡ�����͵Ŀ��Ա��ʱ��������
					// ��Ҫ�޸ģ����Ϊo2o�̻�ѡ����Ա�أ��Լ��ÿ��Ա��·��
					if (disSite_Shop(key, lineO2o.get(i).shop_id) < nearestSiteDis)
					{
						nearestSiteDis = disSite_Shop(key, lineO2o.get(i).shop_id);
						nearestSite = key;
					}
				}
			}
			System.out.println("site_id: " + nearestSite);
			lineO2o.get(i).arriveTime = nearestSiteDis;

			// ��ӡO2o��·��
			printBaseO2o(lineO2o.get(i), sheet, i);
			System.out.println();
		}

		int branchCourierId = lineO2o.size() - 1; // ���Ա�������ص�ID
		for (String key : lineBranch.keySet())
		{
			branchCourierId++;
			int time = 0;
			for (int i = 0; i < lineBranch.get(key).size(); i++) // ���̶�����ÿһ·�ߵ�һ����¼��������
			{
				System.out.println(
						time + " +" + lineBranch.get(key).get(i).time + " -" + lineBranch.get(key).get(i).endToStart);
				if (time + lineBranch.get(key).get(i).time - lineBranch.get(key).get(i).endToStart > 720) // ��ʱ��Ӧ������һ�����Ա
				{
					branchCourierId++;
					time = 0;
					printBaseBranch(lineBranch.get(key).get(i), sheet, branchCourierId, time);
					time += lineBranch.get(key).get(i).time;
				} else // �ÿ��Ա����·��
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
				// ������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
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
				// ������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
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
				// ������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
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
				// ������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
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

	// ------------------------------------------------------------�����ǽ�o2o��������̶���һ����

	// ��o2o·���޷�������������·��ʱ����ʱ�����Ա������������������
	public void printO2OBase(Line lineO2o, Map<String, ArrayList<com.mirdar.GA.Line>> lineBranch, HSSFSheet sheet,
			int courier_id)
	{
		int nearestSiteDis = Integer.MAX_VALUE;
		String nearestSite = null;
		for (String key : lineBranch.keySet()) // �ҵ�����̻����������
		{
			for (int j = 0; j < lineBranch.get(key).size(); j++)
			{
				// ��Щo2o�����޷������ĳ���͵㵽�ﻹҪС����Сʱ��,����ѡ�����͵Ŀ��Ա��ʱ��������
				// ��Ҫ�޸ģ����Ϊo2o�̻�ѡ����Ա�أ��Լ��ÿ��Ա��·��
				if (disSite_Shop(key, lineO2o.shop_id) < nearestSiteDis)
				{
					nearestSiteDis = disSite_Shop(key, lineO2o.shop_id);
					nearestSite = key;
				}
			}
		}
		System.out.println("site_id: " + nearestSite);
		lineO2o.arriveTime = nearestSiteDis;

		// ��ӡO2o��·��
		printBaseO2o(lineO2o, sheet, courier_id);
	}

	// ��O2o��������̶�������,ֻҪ���������������ŵģ��ͻ���ִ���
	public void connectO2oAndBranch(ArrayList<Line> lineO2o, Map<String, ArrayList<com.mirdar.GA.Line>> lineBranch,
			HSSFSheet sheet)
	{
		int spotNull = 0;
		for (int i = 0; i < lineO2o.size(); i++) // ���е�o2o·��
		{

			// �ı��߼�
			int nearestSpotDis = Integer.MAX_VALUE;
			int longLine = 0;
			String nearestSpot = null;
			String nearestSite = null;
			int nearestLine = 0;
			for (String key : lineBranch.keySet()) // �ҵ�����̻����������
			{
				for (int j = 0; j < lineBranch.get(key).size(); j++)
				{
					// ��Щo2o�����޷������ĳ���͵㵽�ﻹҪС����Сʱ��,����ѡ�����͵Ŀ��Ա��ʱ��������
					// ��Ҫ�޸ģ����Ϊo2o�̻�ѡ����Ա�أ��Լ��ÿ��Ա��·��
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
				// ż���������Щ�̻��Ҳ������������Ŀ��Ա����ʱû���������ɿ��Ա���������ӵ�����ʱ
				com.mirdar.GA.Line line = lineBranch.get(nearestSite).get(nearestLine);
				ArrayList<com.mirdar.GA.Line> branchToO2oLine = new ArrayList<com.mirdar.GA.Line>();
				System.out.println("lineO2o.get(i).earliestTime: " + lineO2o.get(i).earliestTime);
				System.out.println("nearestSpotDis: " + nearestSpotDis);
				System.out.println("line.time: " + line.time);
				System.out.println("line.endToStart�� " + line.endToStart);

				int o2oEarliestTime = lineO2o.get(i).earliestTime - nearestSpotDis - line.time + line.endToStart; // �����Ĵ�С����ȥ�������һ��·��
				lineBranch.get(nearestSite).remove(nearestLine);
				System.out.println("o2oEarliestTime: " + o2oEarliestTime);
				System.out.println("lineBranch.get(nearestSite).size: " + lineBranch.get(nearestSite).size());

				int arrvieShopTime = line.time + nearestSpotDis - line.endToStart; // �ÿ��Աʲôʱ���̻���
				if (o2oEarliestTime < 10) // ʣ��ı�����С̫С���޷�����
					;
				else
				{
					branchToO2oLine = lineInO2o(lineBranch.get(nearestSite), o2oEarliestTime); // ���̻����Ա֮ǰ�߹���·��
					// branchToO2oLine.add(line);

					for (int k = 0; k < branchToO2oLine.size(); k++)
					{
						arrvieShopTime += branchToO2oLine.get(k).time;
					}
				}
				branchToO2oLine.add(line);
				lineO2o.get(i).arriveTime = arrvieShopTime; // �����̻����Ա֮ǰ����Ϣ�������̻�·�������arriveTimeӦ�û�С�ڶ�����ʼʱ��
				lineO2o.get(i).branchToO2oLine = branchToO2oLine;
				lineO2o.get(i).spot_id = line.spot_id;

				// ��ӡO2o��·��
				printO2o(lineO2o.get(i), sheet, i);
			}
		}

		int branchCourierId = lineO2o.size() - 1; // ���Ա�������ص�ID
		for (String key : lineBranch.keySet())
		{
			branchCourierId++;
			int time = 0;
			for (int i = 0; i < lineBranch.get(key).size(); i++) // ���̶�����ÿһ·�ߵ�һ����¼��������
			{
				// System.out.println(time+"
				// +"+lineBranch.get(key).get(i).time+"
				// -"+lineBranch.get(key).get(i).endToStart);
				if (time + lineBranch.get(key).get(i).time - lineBranch.get(key).get(i).endToStart > 720) // ��ʱ��Ӧ������һ�����Ա
				{
					branchCourierId++;
					time = 0;
					printBaseBranch(lineBranch.get(key).get(i), sheet, branchCourierId, time);
					time += lineBranch.get(key).get(i).time;
				} else // �ÿ��Ա����·��
				{
					printBaseBranch(lineBranch.get(key).get(i), sheet, branchCourierId, time);
					time += lineBranch.get(key).get(i).time;
				}
			}

		}
		System.out.println();
		System.out.println("û�м�branchLine �� O2O�������� " + spotNull
				+ "-----------------------------------------------------------------");
	}

	// ���ñ����������ⷽ��������Ҫ��֤һ������o2oEarliestTimeǰ�����ʱ���ܻ������Ч��
	// ��������̰�ķ����������̻���������͵���Ϊ���Ա����·�ߣ�Ȼ����ǰƥ�䣬����˼����������
	public ArrayList<com.mirdar.GA.Line> lineInO2o(ArrayList<com.mirdar.GA.Line> lines, int o2oEarliestTime)
	{
		// ���������д���
		ArrayList<com.mirdar.GA.Line> lineInO2o = new ArrayList<com.mirdar.GA.Line>(); // ��������o2o���Ա֮ǰ���͵ĵ���·�ߣ�ֻ�������յ㣩
		int[][] capacity = new int[o2oEarliestTime + 1][lines.size() + 1]; // �ö�ά���飬��Ϊ�˽�������ѡ����Щ·�ߴ�ӡ����
		int[] flag = new int[lines.size()];

		for (int i = 0; i < o2oEarliestTime + 1; i++)
		{
			for (int j = 0; j < lines.size() + 1; j++)
				capacity[i][j] = 0;
		}
		for (int i = 0; i < lines.size(); i++) // �������
		{
			for (int j = o2oEarliestTime; j >= 0; j--)
			{
				if (j - lines.get(i).time <= 0) // Խ��
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
			// ��ʱ˵����i����Ʒδ����
			if (capacity[n][i] == capacity[n][i - 1])
			{
				flag[i - 1] = 0;
			} else // ��i����Ʒ���룬��Ҫ��ȥ��i����Ʒ������
			{
				if (i == lines.size()) // ���һ��·��
				{
					flag[i - 1] = 1;
					System.out.println("o2oEarliestTime: " + o2oEarliestTime + " lines.get(i-1).time: "
							+ lines.get(i - 1).time + "-----------------------------------------------------");
					n = n - lines.get(i - 1).time;
				} else // �м�·�� �� 0 1 0 ����Ҫ����0 !=1 ������ֱ��i-1��õ�0��������1
				{
					flag[i - 2] = 1;
					System.out.println("o2oEarliestTime: " + o2oEarliestTime + " lines.get(i-2).time: "
							+ lines.get(i - 2).time + "-----------------------------------------------------");
					n = n - lines.get(i - 2).time;
				}

			}
		}

		for (int i = 0; i < flag.length; i++) // �����뱳����·�߼�¼
		{
			if (flag[i] == 1)
			{
				lineInO2o.add(lines.get(i));
			}
			System.out.print(lines.get(i).time + "  ");
		}

		int m = 0;
		for (int i = 0; i < flag.length; i++) // ȥ���̻����Ա��·��,���ж���·��ʱ������Ҳ�����ɾ������
		{
			if (flag[i] == 1)
			{
				lines.remove(i - m);
				m++;
			}
		}

		return lineInO2o;
	}

	public void printO2o(Line O2oLine, HSSFSheet sheet, int courier_id) // ��ӡ���Ա��·��
	{
		ArrayList<com.mirdar.GA.Line> branchToO2oLine = O2oLine.branchToO2oLine;
		int arriveTime = 0;
		int oldArriveTime = 0;
		for (int i = 0; i < branchToO2oLine.size(); i++)
		{
			if (branchToO2oLine.get(i).spot_id != O2oLine.spot_id) // ��ӡǰ�漸��branch·��
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
					// ������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
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

		for (int i = 0; i < branchToO2oLine.size(); i++) // ��ӡ���һ��·��
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
					// ������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
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

		for (int i = 0; i < O2oLine.line.size(); i++) // ��ӡo2o·��
		{
			Record record = O2oLine.line.get(i);
			if (i == 0)
			{
				// System.out.print(record.place_id+" "+O2oLine.arriveTime+"
				// "+record.departureTime+" "+
				// record.num+" "+record.order_id);
				count++;
				HSSFRow row1 = sheet.createRow(count);
				// ������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
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
				// ������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
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

	public int disSpot_Shop(String spot_id, String shop_id) // ����֮��ľ���
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

	public int disSite_Shop(String site_id, String shop_id) // ����֮��ľ���
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

	public int disPlace(String name1, String name2) // ����֮��ľ���
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

	// ���ֺ���
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
		for (String key : recordsMap.keySet()) // ����ÿһ�����Ա
		{
			ArrayList<RRecord> records = recordsMap.get(key);
			// System.out.println(key+" .size(): "+records.size());
			for (int i = 0; i < records.size(); i++)
			{
				if (records.get(i).place_id.substring(0, 1).equals("A"))
				{
					if (records.get(i).arriveTime != records.get(i).departureTime) // A...ͣ��ʱ��Ӧ��Ϊ0
						System.out.println("A...�뿪ʱ���뵽��ʱ�䲻һ��");
					// A֮ǰ�ĵ�ΪB��ֻ����ΪA��B��ʱ����Ҫ��֤
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
				} else if (records.get(i).place_id.substring(0, 1).equals("B")) // ������֮��ľ����Ƿ���ȷ,�Լ�ͣ��ʱ���Ƿ���ȷ
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
						// ����o2o�������͵��ʱ��Ҫ���ڹ涨ʱ��
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
						// ����o2o�̻���ʱ��Ҫ���ڹ涨ʱ��
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
				if (i == records.size() - 1) // ����ÿ�����Ա�������һ�����͵��ʱ��
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
