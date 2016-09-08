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
 * 任务：
 * 1. 验证在bind后，每条路径的time，endToStart是否正确，是否与这条路径最后的point离开+首尾距离一致 (完成)
 * 2. 修改bind过程中加入的o2o操作，当惩罚达到某种程度时，就不加入，在后面拼接的时候，在前面加入branch路径
 *  a. 尝试改变o2o本身的绑定顺序，可能会产生不同结果，先尝试将其根据pickup的时间升序排序（时间足够，直接搜索，或者贪心迭代）
 *  b. 尝试检验绑定，当惩罚值太大的时间（1.设置阈值，2.建立一个函数将该订单在没有绑定o2o订单的路径集合中
 *     进行路线构建，产生最优路线生成的惩罚函数相比较），绑定到一个没有o2o订单的路线上
 */

public class Idea {

	Map<String, Place> placeMap = new HashMap<String, Place>(); //所有地点信息
	public Map<String, Order>  orderMap; //所有电商订单信息,key是订单id
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
		
		//电商订单处理
		String filename = "F:/ML/last mile delivery/branch_data2.csv";
		GA3 ga = new GA3();
		int cars = 0;
		ga.allDataMap = ga.readAllData(filename);
//		System.out.println(ga.allDataMap.size()+"  "+ga.len.size());
//		int orderSize = 0;
		for(String key1 : ga.allDataMap.keySet())
		{
//			System.out.println("网点为："+key1);
//			System.out.println("染色体长度："+ga.len.get(key1));
			ga.dataMap = ga.allDataMap.get(key1);
			ga.length = ga.len.get(key1);
			ga.bestChomo = new ArrayList<Chomo>();
			ArrayList<Chomo> pop = ga.getInitPop(); //初始群体生成成功
			
			ArrayList<Chomo> newPop = new ArrayList<Chomo>();
			for(int i=0;i<ga.maxGen;i++)
			{
				newPop = ga.select(pop);
				newPop = ga.crossAndVar(newPop);
				pop = newPop;
			}
//			orderSize+= ga.bestChomo.get(ga.bestChomo.size()-1).chomo.length; //染色体长度正常
			cars+= ga.printChomo(ga.bestChomo.get(ga.bestChomo.size()-1)); //保存了至今出现最后的染色体
		}
//		System.out.println(test.orderMap.size());
		Map<String,ArrayList<Line>> branchlines = test.branchLineToZero( ga.lines);
//		Map<String,ArrayList<Line>> branchAndO2oLines = test.bindO2OtoBranch(branchlines, test.orderMap);
//		System.out.println(branchAndO2oLines.size());
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("experiment");
		HSSFSheet sheet2 = wb.createSheet("experiment2");
		HSSFRow row1=sheet.createRow(test.count);
		//创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
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
	
	//将加入了o2o订单的路线进行标记，或者o2o订单会产生很大的惩罚项
	public Map<String,ArrayList<Line>> bindO2OtoBranch(Map<String,ArrayList<Line>> branchLines,Map<String,Order> orders)
	{
		Map<String,Order> orderMap = new HashMap<String,Order>();
		//这里还需要修改，因为有些o2o加入后，会导致很大的惩罚项,尤其是加在一个o2o订单的前面
//		Map<String,Line> newLines = new HashMap<String,Line>();
		int orderCount = 0;
		for(String order_key : orders.keySet()) //shop
		{
			int extraTime = Integer.MAX_VALUE;
			int extraPlus = 0;
			String bestLineKey = null;
			int bestLineId = 0;
			int bestRecordId = 0;
			//为每一个order找到最适合绑定的路线
			for(String branch_key : branchLines.keySet()) //site
			{
				ArrayList<Line> lines = branchLines.get(branch_key);
				for(int i=0;i<lines.size();i++) //line
				{
					ArrayList<Record> records = lines.get(i).line;
					
					//从records.size()/2开始，因为前一半都是网点
					for(int j=records.size()/2;j<records.size();j++) //record
					{
						/*几个条件
						 * 
						 * 1. 将o2o订单插入配送点j后，产生的额外时间最小
						 * 2. 当线路插入o2o订单后，这条线路新的总时间要小于720
						 */
						if(j < records.size()-1)
						{
							int extra = 0;
							int trueExtra = 0;
							int arriveShop = records.get(j).departureTime+
									disPlace(records.get(j).place_id, orders.get(order_key).shop_id);
							//晚到要惩罚，早到就等待
							if(arriveShop > orders.get(order_key).pickup_time)
							{
								extra = extra + 5*Math.abs(arriveShop - orders.get(order_key).pickup_time);
								trueExtra = trueExtra + disPlace(records.get(j).place_id, orders.get(order_key).shop_id)
									+ orders.get(order_key).time + orders.get(order_key).stay_time
									+ disPlace(orders.get(order_key).spot_id, records.get(j+1).place_id)
									- (records.get(j+1).arriveTime-records.get(j).departureTime);
								//到o2o配送点的时间要晚于规定时间,惩罚
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
							//使得加入到某条路径后，产生的额外时间开销最小
							extra = extra + disPlace(records.get(j).place_id, orders.get(order_key).shop_id)
								+ orders.get(order_key).time + orders.get(order_key).stay_time
								+ disPlace(orders.get(order_key).spot_id, records.get(j+1).place_id)
								- (records.get(j+1).arriveTime-records.get(j).departureTime);
							
							//这里是处理插入后导致后面O2o订单产生的惩罚，且不管插入在那种地点后面
							int k=j+1;
							int plus = extraPlus;
							while( k < records.size())
							{
								if(records.get(k).order_id.substring(0, 1).equals("E") && //遇到的是商户
										records.get(k).place_id.substring(0, 1).equals("S"))
								{   //因为商户离开时间必然大于等于规定时间
									if(records.get(k).arriveTime + plus > records.get(k).departureTime) //加入后时间超过
									{
										plus = records.get(k).arriveTime + plus-records.get(k).departureTime;
										extra = extra + 5*Math.abs(plus);
									}
									else //或者对后面都不造成影响
										break;
								}
								else if(records.get(k).order_id.substring(0, 1).equals("E") && //遇到商户对应的spot
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
							
							
							//这里也的加入限制，不能让o2o订单加入后导致惩罚太大，这里先尽量往里面加!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
							//超过720，加入惩罚，可以再优化，将加入该路线产生的惩罚与重新生成一个新的快递员，产生的惩罚比较，从而选择小的
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
		
			//先相比extraTime与order从最近网点到达产生的惩罚，再比较与其构建路径后产生的惩罚！！！！！！！
			//找到的路线,以及插入点
			Line line = branchLines.get(bestLineKey).get(bestLineId);
			
			ArrayList<Record> last = new ArrayList<Record>();
			for(int i=bestRecordId+1;i<line.line.size();i++)
			{
				last.add(line.line.get(i));
			}
			//ArrayList的循环删除需要注意
			while(line.line.size() > bestRecordId+1 )
			{
				line.line.remove(line.line.size()-1);
			}
			int arriveTime = line.line.get(bestRecordId).departureTime;
			//加入o2o到电商路线的操作
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
			
			//加载路线中间
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
//				System.out.println("last.arriveTime: "+0+" 直接加在路径最后");
//			}
			//可能出现在这里，当在S前面加入o2o订单时，本来S是需要等待才可以走，
			//但是因为o2o的订单加入，超过了pickup，可以直接走，但是这里却没有更新
			int plus = extraPlus;
			for(int i=0;i<last.size();i++)
			{
				if(last.get(i).place_id.substring(0, 1).equals("S"))
				{
					if(last.get(i).arriveTime < last.get(i).departureTime) //注意的地方
					{
						//当加入o2o订单，到达shop的时间晚于pickup时间
						if(last.get(i).arriveTime + plus >= last.get(i).departureTime)
						{
							last.get(i).arriveTime= last.get(i).arriveTime + plus;
							plus = last.get(i).arriveTime - last.get(i).departureTime;
							last.get(i).departureTime=last.get(i).arriveTime;
							line.line.add(last.get(i));
						}
						else //如果还是要早于的话，那么后面的时间不变
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
			else //这时直接在路径后面增加o2o订单路线
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
				//每条线路的初始时间
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
	//需要将路径拼接起来得到个快递员的路径，拼接的时候需要将加入了o2o订单的路线作为初始路线，或者利用背包原理在前面加路线
	public void printRecord(Map<String,ArrayList<Line>> branchAndO2oLines,HSSFSheet sheet)
	{
		//将路线集合分成两个集合，一个必定是首发路线，另一个是可调节集合
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
		/*//检查首发路线前是否可以再加其他路线
		for(int i=0;i<firstLines.size();i++)
		{
			Line line = firstLines.get(i);
			ArrayList<Record> lineCopy = new ArrayList<Record>(); //Line.line的复杂
			for(int j=0;j<line.line.size();j++)
			{
				lineCopy.add(line.line.get(j));
			}
			for(int j=0;j<line.line.size();j++)
			{
				if(line.line.get(j).place_id.substring(0, 1).equals("S"))
				{
					int diffTime = orderMap.get(line.line.get(j).order_id).pickup_time-line.line.get(j).arriveTime;
					if(diffTime <= 10) ; //可以先检查所有路线中最短路线的时间开销，这里就可以提前跳出
					else 
					{
						//每条ajust路线到这条路线出发点的时间
						for(int k=0;k<adjustLines.size();k++)
						{
							adjustLines.get(k).tempTime = adjustLines.get(k).time - adjustLines.get(k).endToStart
										+disPlace(adjustLines.get(k).line.get(adjustLines.get(k).line.size()-1).place_id,
												line.line.get(0).place_id);
						}
						Line lineIn = bagFun(adjustLines,diffTime); //这里只加入一条路线
						if(lineIn.time == 0) ; //无法加入背包
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
		
		//先不考虑在首发路线前加路线
		
		//拼接过程 ，需优化
		int courier_id = -1;
		int time = 0;
		for(int i=0;i<firstLines.size();i++)
		{
			time = 0;
			//当这条路线已经大于720时，直接生产路线
			if(firstLines.get(i).time-firstLines.get(i).endToStart >= 720)
			{
				courier_id++;
				printBaseBranch(firstLines.get(i), sheet, courier_id,0);
			}
			else //只考虑在后面加一条路径
			{
				time = time + firstLines.get(i).time - firstLines.get(i).endToStart;
				int bestRouteId = -1;
				int maxPlus = 0;
				for(int j=0;j<adjustLines.size();j++)
				{
					adjustLines.get(j).tempTime = adjustLines.get(j).time - adjustLines.get(j).endToStart
							+ disPlace(firstLines.get(i).line.get(firstLines.get(i).line.size()-1).place_id,
									adjustLines.get(j).line.get(0).place_id);
					//再试验adjustLines.get(j).time - adjustLines.get(j).endToStart>maxPlus
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
					for(int j =0;j<adjustLines.get(bestRouteId).line.size();j++) //添加路径
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
		//这里有的路线>720，会导致快递员的变化不连续递增
		Map<String,Line> results = new HashMap<String, Line>();
		Line line = new Line();
		int lineSize = 0;
		for(String key : branchAndO2oLines.keySet())
		{ 
			courier_id++;
			line = new Line();
			results.put(courier.get(courier_id), line);
			time=0;
			for(int i=0;i<branchAndO2oLines.get(key).size();i++) //电商订单的每一路线第一个记录都是网点
			{
//				System.out.println(time+" +"+branchAndO2oLines.get(key).get(i).time+" -"+branchAndO2oLines.get(key).get(i).endToStart);
				if(time+branchAndO2oLines.get(key).get(i).time-branchAndO2oLines.get(key).get(i).endToStart > 720) //此时就应该增加一个快递员
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
				else //该快递员继续路线
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
		int maxCap = 0; //最大容量
		int k = -1; //最大容量的lineid;
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
				//创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
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
				//创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
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
				//创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
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
	public int disPlace(String name1,String name2) //两点之间的距离
	{
		int cost = 0;
		cost = (int)(Math.round( 2*6378137*Math.asin(Math.sqrt(Math.pow(Math.sin(Math.PI/180.0*(placeMap.get(name1).lan-placeMap.get(name2).lan)/2),2)+
				Math.cos(Math.PI/180.0*placeMap.get(name1).lan)*Math.cos(Math.PI/180.0*placeMap.get(name2).lan)*
				Math.pow(Math.sin(Math.PI/180.0*(placeMap.get(name1).lon-placeMap.get(name2).lon)/2),2)))/250));
		
		return cost;
	}
	
}
