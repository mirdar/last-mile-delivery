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
import com.mirdar.GA.GA3;
import com.mirdar.GA.Line;
import com.mirdar.GA.Record;
import com.mirdar.O2O.Order;
import com.mirdar.O2O.ReadData;

/*
 * ����
 * 1. ��֤��bind��ÿ��·����time��endToStart�Ƿ���ȷ���Ƿ�������·������point�뿪+��β����һ�� (���)
 * 2. �޸�bind�����м����o2o���������ͷ��ﵽĳ�̶ֳ�ʱ���Ͳ����룬�ں���ƴ�ӵ�ʱ����ǰ�����branch·��
 *  a. ���Ըı�o2o����İ�˳�򣬿��ܻ������ͬ������ȳ��Խ������pickup��ʱ����������ʱ���㹻��ֱ������������̰�ĵ�����
 *  b. ���Լ���󶨣����ͷ�ֵ̫���ʱ�䣨1.������ֵ��2.����һ���������ö�����û�а�o2o������·��������
 *     ����·�߹�������������·�����ɵĳͷ�������Ƚϣ����󶨵�һ��û��o2o������·����
 */

public class Idea {

	Map<String, Place> placeMap = new HashMap<String, Place>(); //���еص���Ϣ
	public Map<String, Order>  orderMap; //���е��̶�����Ϣ,key�Ƕ���id
	int count = 0;
	int count2 = 0;
	Map<Integer,String> courier = new  HashMap<Integer,String>();
	
	public int allTime = 0;
	public static void main(String[] args) throws IOException
	{
		
		String fileShop = "F:\\ML\\last mile delivery/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery/spot.csv";
		String fileSite = "F:\\ML\\last mile delivery/site.csv";
		String fileOrder = "F:\\ML\\last mile delivery/shop_data.csv";
		String fileCourier = "F:\\ML\\last mile delivery/courier.csv";
		ReadData readData = new ReadData();
		
		Idea test = new Idea();
		test.placeMap = readData.readPlace(fileSite,test.placeMap);
		test.placeMap = readData.readPlace(fileSpot,test.placeMap);
		test.placeMap = readData.readPlace(fileShop,test.placeMap);
		test.orderMap = readData.readO2oOrder(fileOrder);
		test.courier = readData.readCourier(fileCourier);
		
		//���̶�������
		String filename = "F:/ML/last mile delivery/branch_data2.csv";
		GA3 ga = new GA3();
		int cars = 0;
		ga.allDataMap = ga.readAllData(filename);
//		System.out.println(ga.allDataMap.size()+"  "+ga.len.size());
//		int orderSize = 0;
		for(String key1 : ga.allDataMap.keySet())
		{
//			System.out.println("����Ϊ��"+key1);
//			System.out.println("Ⱦɫ�峤�ȣ�"+ga.len.get(key1));
			ga.dataMap = ga.allDataMap.get(key1);
			ga.length = ga.len.get(key1);
			ga.bestChomo = new ArrayList<Chomo>();
			ArrayList<Chomo> pop = ga.getInitPop(); //��ʼȺ�����ɳɹ�
			
			ArrayList<Chomo> newPop = new ArrayList<Chomo>();
			for(int i=0;i<ga.maxGen;i++)
			{
				newPop = ga.select(pop);
				newPop = ga.crossAndVar(newPop);
				pop = newPop;
			}
//			orderSize+= ga.bestChomo.get(ga.bestChomo.size()-1).chomo.length; //Ⱦɫ�峤������
			cars+= ga.printChomo(ga.bestChomo.get(ga.bestChomo.size()-1)); //�����������������Ⱦɫ��
		}
//		System.out.println(test.orderMap.size());
		Map<String,ArrayList<Line>> branchlines = test.branchLineToZero( ga.lines);
//		Map<String,ArrayList<Line>> branchAndO2oLines = test.bindO2OtoBranch(branchlines, test.orderMap);
//		System.out.println(branchAndO2oLines.size());
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("experiment");
		HSSFSheet sheet2 = wb.createSheet("experiment2");
		HSSFRow row1=sheet.createRow(test.count);
		//������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
//		HSSFCell cell=row1.createCell(0);
			row1.createCell(0).setCellValue("Courier_id");
			row1.createCell(1).setCellValue("Addr");
			row1.createCell(2).setCellValue("Arrival_time");
			row1.createCell(3).setCellValue("Departure");
			row1.createCell(4).setCellValue("Amount");
			row1.createCell(5).setCellValue("Order_id");
			row1.createCell(6).setCellValue("pickUpTime");
		
		OutputStream output=new FileOutputStream("F:/ML/last mile delivery/branchPlusO2O.xls");
		wb.write(output);
		output.close();
		wb.close();
	}

	
	/*public ArrayList<BranchLines> generateBranchLines(Map<String,ArrayList<Line>> branchLines)
	{
		ArrayList<BranchLines> lines = new ArrayList<BranchLines>();
		
		
		return lines;
	}*/
	
	//��������o2o������·�߽��б�ǣ�����o2o����������ܴ�ĳͷ���
	public Map<String,ArrayList<Line>> bindO2OtoBranch(Map<String,ArrayList<Line>> branchLines,Map<String,Order> orders)
	{
		Map<String,Order> orderMap = new HashMap<String,Order>();
		//���ﻹ��Ҫ�޸ģ���Ϊ��Щo2o����󣬻ᵼ�ºܴ�ĳͷ���,�����Ǽ���һ��o2o������ǰ��
//		Map<String,Line> newLines = new HashMap<String,Line>();
		int orderCount = 0;
		for(String order_key : orders.keySet()) //shop
		{
			int extraTime = Integer.MAX_VALUE;
			int extraPlus = 0;
			String bestLineKey = null;
			int bestLineId = 0;
			int bestRecordId = 0;
			//Ϊÿһ��order�ҵ����ʺϰ󶨵�·��
			for(String branch_key : branchLines.keySet()) //site
			{
				ArrayList<Line> lines = branchLines.get(branch_key);
				for(int i=0;i<lines.size();i++) //line
				{
					ArrayList<Record> records = lines.get(i).line;
					
					//��records.size()/2��ʼ����Ϊǰһ�붼������
					for(int j=records.size()/2;j<records.size();j++) //record
					{
						/*��������
						 * 
						 * 1. ��o2o�����������͵�j�󣬲����Ķ���ʱ����С
						 * 2. ����·����o2o������������·�µ���ʱ��ҪС��720
						 */
						if(j < records.size()-1)
						{
							int extra = 0;
							int trueExtra = 0;
							int arriveShop = records.get(j).departureTime+
									disPlace(records.get(j).place_id, orders.get(order_key).shop_id);
							//��Ҫ�ͷ����絽�͵ȴ�
							if(arriveShop > orders.get(order_key).pickup_time)
							{
								extra = extra + 5*Math.abs(arriveShop - orders.get(order_key).pickup_time);
								trueExtra = trueExtra + disPlace(records.get(j).place_id, orders.get(order_key).shop_id)
									+ orders.get(order_key).time + orders.get(order_key).stay_time
									+ disPlace(orders.get(order_key).spot_id, records.get(j+1).place_id)
									- (records.get(j+1).arriveTime-records.get(j).departureTime);
								//��o2o���͵��ʱ��Ҫ���ڹ涨ʱ��,�ͷ�
								if(arriveShop+orders.get(order_key).time > orders.get(order_key).delivery_time)
								{
									extra = extra + 5*Math.abs(arriveShop+orders.get(order_key).time 
											- orders.get(order_key).delivery_time);
								}
							}
							else 
							{
								
								extra = extra + Math.abs(arriveShop - orders.get(order_key).pickup_time);
								trueExtra = trueExtra + disPlace(records.get(j).place_id, orders.get(order_key).shop_id)
									+ orders.get(order_key).time + orders.get(order_key).stay_time
									+ disPlace(orders.get(order_key).spot_id, records.get(j+1).place_id)
									- (records.get(j+1).arriveTime-records.get(j).departureTime)
									+orders.get(order_key).pickup_time-arriveShop;
							}
							//ʹ�ü��뵽ĳ��·���󣬲����Ķ���ʱ�俪����С
							extra = extra + disPlace(records.get(j).place_id, orders.get(order_key).shop_id)
								+ orders.get(order_key).time + orders.get(order_key).stay_time
								+ disPlace(orders.get(order_key).spot_id, records.get(j+1).place_id)
								- (records.get(j+1).arriveTime-records.get(j).departureTime);
							
							//�����Ǵ��������º���O2o���������ĳͷ����Ҳ��ܲ��������ֵص����
							int k=j+1;
							int plus = extraPlus;
							while( k < records.size())
							{
								if(records.get(k).order_id.substring(0, 1).equals("E") && //���������̻�
										records.get(k).place_id.substring(0, 1).equals("S"))
								{   //��Ϊ�̻��뿪ʱ���Ȼ���ڵ��ڹ涨ʱ��
									if(records.get(k).arriveTime + plus > records.get(k).departureTime) //�����ʱ�䳬��
									{
										plus = records.get(k).arriveTime + plus-records.get(k).departureTime;
										extra = extra + 5*Math.abs(plus);
									}
									else //���߶Ժ��涼�����Ӱ��
										break;
								}
								else if(records.get(k).order_id.substring(0, 1).equals("E") && //�����̻���Ӧ��spot
											records.get(k).place_id.substring(0, 1).equals("B"))
								{
									if(records.get(k).arriveTime + plus > orders.get(records.get(k).order_id).delivery_time 
										&& records.get(k).arriveTime <= orders.get(records.get(k).order_id).delivery_time )
									{
										extra = extra + 5*Math.abs(records.get(k).arriveTime + plus-orders.get(records.get(k).order_id).delivery_time );
									}
									else if(records.get(k).arriveTime + plus > orders.get(records.get(k).order_id).delivery_time 
											&& records.get(k).arriveTime > orders.get(records.get(k).order_id).delivery_time )
									{
										extra = extra + 5*Math.abs(plus);
									}
								}
								k++;
							}
							
							
							//����Ҳ�ļ������ƣ�������o2o����������³ͷ�̫�������Ⱦ����������!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
							//����720������ͷ����������Ż����������·�߲����ĳͷ�����������һ���µĿ��Ա�������ĳͷ��Ƚϣ��Ӷ�ѡ��С��
							if(records.get(records.size()-1).arriveTime+trueExtra > 720)
								extra = extra + 10*Math.abs(records.get(records.size()-1).arriveTime+trueExtra-720);
							
							if(extra < extraTime)
							{
								extraTime = extra;
								extraPlus = trueExtra;
								bestLineKey = branch_key;
								bestLineId = i;
								bestRecordId = j;
							}
						} 
						else
						{
							int extra = 0;
							int trueExtra = 0;
							int arriveShop = records.get(j).departureTime+
									disPlace(records.get(j).place_id, orders.get(order_key).shop_id);
							if(arriveShop > orders.get(order_key).pickup_time)
							{
								extra = extra + 5*Math.abs(arriveShop - orders.get(order_key).pickup_time);
								trueExtra = trueExtra + disPlace(records.get(j).place_id, orders.get(order_key).shop_id)
									+ orders.get(order_key).time;
							}
							else 
							{
								extra = extra + Math.abs(arriveShop - orders.get(order_key).pickup_time);
								trueExtra = trueExtra + disPlace(records.get(j).place_id, orders.get(order_key).shop_id)
									+ orders.get(order_key).time + orders.get(order_key).pickup_time-arriveShop;
							}
							extra = extra + orders.get(order_key).time + 
									disPlace(records.get(j).place_id, orders.get(order_key).shop_id)+
									records.get(j).departureTime-records.get(j).arriveTime;
							if(records.get(records.size()-1).departureTime+ trueExtra > 720)
								extra = extra + 10*Math.abs(records.get(records.size()-1).departureTime+ trueExtra-720);
							if(extra < extraTime)
							{
								extraTime = extra;
								extraPlus = trueExtra;
								bestLineKey = branch_key;
								bestLineId = i;
								bestRecordId = j;
							}
						}
					}
				}
			}
//			System.out.println("order_id: "+order_key);
//			System.out.println("orderCount: "+orderCount);
//			System.out.println("extraTime: "+extraTime);
//			System.out.println("extraPlus: "+extraPlus);
//			System.out.println("line.line.size(1) "+branchLines.get(bestLineKey).get(bestLineId).line.size());
		
			//�����extraTime��order��������㵽������ĳͷ����ٱȽ����乹��·��������ĳͷ���������������
			//�ҵ���·��,�Լ������
			Line line = branchLines.get(bestLineKey).get(bestLineId);
			
			ArrayList<Record> last = new ArrayList<Record>();
			for(int i=bestRecordId+1;i<line.line.size();i++)
			{
				last.add(line.line.get(i));
			}
			//ArrayList��ѭ��ɾ����Ҫע��
			while(line.line.size() > bestRecordId+1 )
			{
				line.line.remove(line.line.size()-1);
			}
			int arriveTime = line.line.get(bestRecordId).departureTime;
			//����o2o������·�ߵĲ���
				Record record = new Record();
				record.place_id = orders.get(order_key).shop_id;
				arriveTime= arriveTime + disPlace(line.line.get(bestRecordId).place_id, orders.get(order_key).shop_id);
				record.arriveTime = arriveTime;
				if(arriveTime <= orders.get(order_key).pickup_time)
					arriveTime = record.departureTime = orders.get(order_key).pickup_time;
				else
					record.departureTime = arriveTime;
				record.num = orders.get(order_key).num;
				record.order_id = orders.get(order_key).order_id;
				line.line.add(record);
				
				Record record2 = new Record();
				record2.place_id = orders.get(order_key).spot_id;
				arriveTime= arriveTime + orders.get(order_key).time;
				record2.arriveTime = arriveTime;
				arriveTime= arriveTime + orders.get(order_key).stay_time;
				record2.departureTime = arriveTime;
				record2.num = -orders.get(order_key).num;
				record2.order_id = orders.get(order_key).order_id;
				
				
				line.line.add(record2);
			
			//����·���м�
//			if(last.size() != 0) 
//			{
//				System.out.println((arriveTime+disPlace(last.get(0).place_id, orders.get(order_key).spot_id)
//										- line.line.get(bestRecordId).departureTime-
//										(last.get(0).arriveTime-line.line.get(bestRecordId).departureTime))+" == "+extraPlus);
//				System.out.println("last.arriveTime: "+last.get(last.size()-1).arriveTime);
//			}
//			else
//			{
//				System.out.println(arriveTime-orders.get(order_key).stay_time - line.line.get(bestRecordId).departureTime+" == "+extraPlus);
//				System.out.println("last.arriveTime: "+0+" ֱ�Ӽ���·�����");
//			}
			//���ܳ������������Sǰ�����o2o����ʱ������S����Ҫ�ȴ��ſ����ߣ�
			//������Ϊo2o�Ķ������룬������pickup������ֱ���ߣ���������ȴû�и���
			int plus = extraPlus;
			for(int i=0;i<last.size();i++)
			{
				if(last.get(i).place_id.substring(0, 1).equals("S"))
				{
					if(last.get(i).arriveTime < last.get(i).departureTime) //ע��ĵط�
					{
						//������o2o����������shop��ʱ������pickupʱ��
						if(last.get(i).arriveTime + plus >= last.get(i).departureTime)
						{
							last.get(i).arriveTime= last.get(i).arriveTime + plus;
							plus = last.get(i).arriveTime - last.get(i).departureTime;
							last.get(i).departureTime=last.get(i).arriveTime;
							line.line.add(last.get(i));
						}
						else //�������Ҫ���ڵĻ�����ô�����ʱ�䲻��
						{
							last.get(i).arriveTime= last.get(i).arriveTime + plus;
							line.line.add(last.get(i));
							int k = i+1;
							while(k<last.size())
							{
								line.line.add(last.get(k));
								k++;
							}
							plus = 0;
							break;
						}
					}
					else
					{
						last.get(i).arriveTime= last.get(i).arriveTime + plus;
						last.get(i).departureTime= last.get(i).departureTime + plus;
						line.line.add(last.get(i));
					}
				}
				else
				{
					last.get(i).arriveTime= last.get(i).arriveTime + plus;
					last.get(i).departureTime= last.get(i).departureTime + plus;
					line.line.add(last.get(i));
				}
			}
//			System.out.println("line.time1: "+line.time);
			if(last.size() != 0)
				line.time = line.time+plus;
			else //��ʱֱ����·����������o2o����·��
			{
				line.time = line.time-line.endToStart+extraPlus+orders.get(order_key).stay_time
						+disPlace(orders.get(order_key).spot_id, line.line.get(0).place_id);
				line.endToStart = disPlace(line.line.get(line.line.size()-1).place_id, line.line.get(0).place_id);
			}
			line.flag = 1;
//			System.out.println("line.line.size(2) "+branchLines.get(bestLineKey).get(bestLineId).line.size());
//			if(line.line.get(line.line.size()-1).arriveTime > 1000)
//				System.out.println("----------------------------------------------------------------");
			
			
//			System.out.println("line.line.get(line.line.size()-1).arriveTime: "+line.line.get(line.line.size()-1).arriveTime);
//			System.out.println("line.time2: "+line.time);
//			System.out.println();
			orderCount++;
		}
		
		return branchLines;
	}
	
	public Map<String,ArrayList<Line>> branchLineToZero(Map<String,ArrayList<Line>> branchLines)
	{
		for(String key : branchLines.keySet())
		{
			for(int i=0;i<branchLines.get(key).size();i++)
			{
				//ÿ����·�ĳ�ʼʱ��
				int firstTime = branchLines.get(key).get(i).line.get(0).arriveTime;
				for(int j=0;j<branchLines.get(key).get(i).line.size();j++)
				{
					branchLines.get(key).get(i).line.get(j).arriveTime = branchLines.get(key).get(i).line.get(j).arriveTime
																					- firstTime;
					branchLines.get(key).get(i).line.get(j).departureTime = branchLines.get(key).get(i).line.get(j).departureTime
							- firstTime;
				}
			}
		}
		
		return branchLines;
	}
	//��Ҫ��·��ƴ�������õ������Ա��·����ƴ�ӵ�ʱ����Ҫ��������o2o������·����Ϊ��ʼ·�ߣ��������ñ���ԭ����ǰ���·��
	public void printRecord(Map<String,ArrayList<Line>> branchAndO2oLines,HSSFSheet sheet)
	{
		//��·�߼��Ϸֳ��������ϣ�һ���ض����׷�·�ߣ���һ���ǿɵ��ڼ���
		ArrayList<Line> firstLines = new ArrayList<Line>();
		ArrayList<Line> adjustLines = new ArrayList<Line>();
		
		int recordSize = 0;
		for(String key : branchAndO2oLines.keySet())
		{
			for(int i=0;i<branchAndO2oLines.get(key).size();i++)
			{
				if(branchAndO2oLines.get(key).get(i).flag == 1)
					firstLines.add(branchAndO2oLines.get(key).get(i));
				else
					adjustLines.add(branchAndO2oLines.get(key).get(i));
				recordSize = recordSize + branchAndO2oLines.get(key).get(i).line.size();
			}
		}
		
		System.out.println("recordSize: "+recordSize);
		System.out.println("firstLines.size(1): "+firstLines.size());
		System.out.println("adjustLines.size(1): "+adjustLines.size());
		/*//����׷�·��ǰ�Ƿ�����ټ�����·��
		for(int i=0;i<firstLines.size();i++)
		{
			Line line = firstLines.get(i);
			ArrayList<Record> lineCopy = new ArrayList<Record>(); //Line.line�ĸ���
			for(int j=0;j<line.line.size();j++)
			{
				lineCopy.add(line.line.get(j));
			}
			for(int j=0;j<line.line.size();j++)
			{
				if(line.line.get(j).place_id.substring(0, 1).equals("S"))
				{
					int diffTime = orderMap.get(line.line.get(j).order_id).pickup_time-line.line.get(j).arriveTime;
					if(diffTime <= 10) ; //�����ȼ������·�������·�ߵ�ʱ�俪��������Ϳ�����ǰ����
					else 
					{
						//ÿ��ajust·�ߵ�����·�߳������ʱ��
						for(int k=0;k<adjustLines.size();k++)
						{
							adjustLines.get(k).tempTime = adjustLines.get(k).time - adjustLines.get(k).endToStart
										+disPlace(adjustLines.get(k).line.get(adjustLines.get(k).line.size()-1).place_id,
												line.line.get(0).place_id);
						}
						Line lineIn = bagFun(adjustLines,diffTime); //����ֻ����һ��·��
						if(lineIn.time == 0) ; //�޷����뱳��
						else
						{
							int arriveTime = 0;
							line.line = lineIn.line;
							arriveTime = arriveTime + lineIn.tempTime;
							line.time = line.time + lineIn.tempTime;
							for(int k=0;k<lineCopy.size();k++)
							{
								lineCopy.get(k).arriveTime = lineCopy.get(k).arriveTime+arriveTime;
								lineCopy.get(k).departureTime = lineCopy.get(k).arriveTime+arriveTime;
								line.line.add(lineCopy.get(k));
							}
						}
					}
				}
			}
		}
		System.out.println("firstLines.size(2): "+firstLines.size());
		System.out.println("adjustLines.size(2): "+adjustLines.size());*/
		
		//�Ȳ��������׷�·��ǰ��·��
		
		//ƴ�ӹ��� �����Ż�
		int courier_id = -1;
		int time = 0;
		for(int i=0;i<firstLines.size();i++)
		{
			time = 0;
			//������·���Ѿ�����720ʱ��ֱ������·��
			if(firstLines.get(i).time-firstLines.get(i).endToStart >= 720)
			{
				courier_id++;
				printBaseBranch(firstLines.get(i), sheet, courier_id,0);
			}
			else //ֻ�����ں����һ��·��
			{
				time = time + firstLines.get(i).time - firstLines.get(i).endToStart;
				int bestRouteId = -1;
				int maxPlus = 0;
				for(int j=0;j<adjustLines.size();j++)
				{
					adjustLines.get(j).tempTime = adjustLines.get(j).time - adjustLines.get(j).endToStart
							+ disPlace(firstLines.get(i).line.get(firstLines.get(i).line.size()-1).place_id,
									adjustLines.get(j).line.get(0).place_id);
					//������adjustLines.get(j).time - adjustLines.get(j).endToStart>maxPlus
					if(time+adjustLines.get(j).tempTime <= 720 && adjustLines.get(j).tempTime > maxPlus)
					{
						maxPlus= adjustLines.get(j).tempTime;
						bestRouteId = j;
					}
				}
				if(bestRouteId != -1)
				{
					time = time + disPlace(firstLines.get(i).line.get(firstLines.get(i).line.size()-1).place_id,
							adjustLines.get(bestRouteId).line.get(0).place_id);
					for(int j =0;j<adjustLines.get(bestRouteId).line.size();j++) //���·��
					{
						Record record = adjustLines.get(bestRouteId).line.get(j);
						record.arriveTime = time+record.arriveTime;
						record.departureTime = time+record.departureTime;
						firstLines.get(i).line.add(record);					
					}
					firstLines.get(i).time = firstLines.get(i).line.get(firstLines.get(i).line.size()-1).departureTime
												+disPlace(firstLines.get(i).line.get(firstLines.get(i).line.size()-1).place_id,
														firstLines.get(i).line.get(0).place_id);
					firstLines.get(i).endToStart = disPlace(firstLines.get(i).line.get(firstLines.get(i).line.size()-1).place_id,
							firstLines.get(i).line.get(0).place_id);
					adjustLines.remove(bestRouteId);
					i--;
				}
				else
				{
					courier_id++;
					printBaseBranch(firstLines.get(i), sheet, courier_id,0);
				}
			}
		}
		
		
		
		/*int courier_id = -1;
		int time = 0;
		//�����е�·��>720���ᵼ�¿��Ա�ı仯����������
		Map<String,Line> results = new HashMap<String, Line>();
		Line line = new Line();
		int lineSize = 0;
		for(String key : branchAndO2oLines.keySet())
		{ 
			courier_id++;
			line = new Line();
			results.put(courier.get(courier_id), line);
			time=0;
			for(int i=0;i<branchAndO2oLines.get(key).size();i++) //���̶�����ÿһ·�ߵ�һ����¼��������
			{
//				System.out.println(time+" +"+branchAndO2oLines.get(key).get(i).time+" -"+branchAndO2oLines.get(key).get(i).endToStart);
				if(time+branchAndO2oLines.get(key).get(i).time-branchAndO2oLines.get(key).get(i).endToStart > 720) //��ʱ��Ӧ������һ�����Ա
				{
					line.time = time;
					if(i == 0)
						line.endToStart = branchAndO2oLines.get(key).get(i).endToStart;
					else
						line.endToStart = branchAndO2oLines.get(key).get(i-1).endToStart;
					courier_id++;
					line = new Line();
					results.put(courier.get(courier_id), line);
					time = 0;
					printBaseBranch2(branchAndO2oLines.get(key).get(i), sheet, courier_id,time,line);
					time+= branchAndO2oLines.get(key).get(i).time;
				}
				else //�ÿ��Ա����·��
				{
					printBaseBranch2(branchAndO2oLines.get(key).get(i), sheet, courier_id,time,line);
					time+= branchAndO2oLines.get(key).get(i).time;
				}
			}
			
		}*/
	}
	
	public Line bagFun(ArrayList<Line> lines,int bagWeight)
	{
		Line line = new Line();
		int maxCap = 0; //�������
		int k = -1; //���������lineid;
		for(int i=0;i<lines.size();i++)
		{
			if(lines.get(i).tempTime < bagWeight && lines.get(i).tempTime > maxCap)
			{
				maxCap = lines.get(i).tempTime ;
				k = i;
			}
		}
		if(k!=-1)
		{
			line = lines.get(k);
			lines.remove(k);
		}
		
		return line;
	}
	
	
	
	
	public void printBaseBranch(Line line,HSSFSheet sheet,int courier_id,int time)
	{
		for(int i=0;i<line.line.size();i++)
		{
			Record record = line.line.get(i);
				count2++;
				HSSFRow row1=sheet.createRow(count2);
				//������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
//				HSSFCell cell=row1.createCell(0);
					row1.createCell(0).setCellValue(courier.get(courier_id));
					row1.createCell(1).setCellValue(record.place_id);
					row1.createCell(2).setCellValue(record.arriveTime);
					row1.createCell(3).setCellValue(record.departureTime);
					row1.createCell(4).setCellValue(record.num);
					row1.createCell(5).setCellValue(record.order_id);
//					if(record.order_id.substring(0,1).equals("E") && record.place_id.substring(0,1).equals("S"))
//						row1.createCell(6).setCellValue(orderMap.get(record.order_id).pickup_time);
//					else if(record.order_id.substring(0,1).equals("E") && record.place_id.substring(0,1).equals("B"))
//						row1.createCell(6).setCellValue(orderMap.get(record.order_id).delivery_time);
//					else
//						row1.createCell(6).setCellValue(0);
//					if(i == line.line.size()-1) 
//						row1.createCell(7).setCellValue(line.time+" -"+line.endToStart);
//					else
//						row1.createCell(7).setCellValue(" -");
//					row1.createCell(7).setCellValue(" -");
		}
	}
	
	/*public void printR(Map<String,Line> lines,HSSFSheet sheet)
	{
		int bindSize = 0;
		for(String key : lines.keySet())
		{
			printBaseBranch(lines.get(key), sheet, key, 0);
			bindSize+= lines.get(key).line.size();
		}
		System.out.println("bindRcords.size: "+bindSize);
	}*/
	
	/*public void printBaseBranch2(Line line,HSSFSheet sheet,int courier_id,int time,Line lines)
	{
		int times = time;
		for(int i=0;i<line.line.size();i++)
		{
			com.mirdar.GA.Record record = line.line.get(i);
			Record newRecord = new Record();
			if(i == 0)
			{
				count++;
				HSSFRow row1=sheet.createRow(count);
				//������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
//				HSSFCell cell=row1.createCell(0);
				row1.createCell(0).setCellValue(courier.get(courier_id));
					row1.createCell(1).setCellValue(record.place_id);
					newRecord.place_id = record.place_id;
					row1.createCell(2).setCellValue(time);
					newRecord.arriveTime = time;
					time+= record.departureTime - record.arriveTime; 
					row1.createCell(3).setCellValue(time);
					newRecord.departureTime = time;
					row1.createCell(4).setCellValue(record.num);
					newRecord.num = record.num;
					row1.createCell(5).setCellValue(record.order_id);
					newRecord.order_id = record.order_id;
					lines.line.add(newRecord);
					
			}
			else
			{
				count++;
				HSSFRow row1=sheet.createRow(count);
				//������Ԫ��excel�ĵ�Ԫ�񣬲���Ϊ��������������0��255֮����κ�һ��
//				HSSFCell cell=row1.createCell(0);
				row1.createCell(0).setCellValue(courier.get(courier_id));
				row1.createCell(1).setCellValue(record.place_id);
				newRecord.place_id = record.place_id;
				time+= record.arriveTime - line.line.get(i-1).departureTime;
				row1.createCell(2).setCellValue(time);
				newRecord.arriveTime = time;
				time+= record.departureTime - record.arriveTime; 
				row1.createCell(3).setCellValue(time);
				newRecord.departureTime = time;
				row1.createCell(4).setCellValue(record.num);
				newRecord.num = record.num;
				row1.createCell(5).setCellValue(record.order_id);
				newRecord.order_id = record.order_id;
				lines.line.add(newRecord);
			}
//			System.out.println();
		}
		time = times;
	}*/
	public int disPlace(String name1,String name2) //����֮��ľ���
	{
		int cost = 0;
		cost = (int)(Math.round( 2*6378137*Math.asin(Math.sqrt(Math.pow(Math.sin(Math.PI/180.0*(placeMap.get(name1).lan-placeMap.get(name2).lan)/2),2)+
				Math.cos(Math.PI/180.0*placeMap.get(name1).lan)*Math.cos(Math.PI/180.0*placeMap.get(name2).lan)*
				Math.pow(Math.sin(Math.PI/180.0*(placeMap.get(name1).lon-placeMap.get(name2).lon)/2),2)))/250));
		
		return cost;
	}
	
}
