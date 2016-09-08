package com.mirdar.CKAlgorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mirdar.GA.Point;
import com.mirdar.GA.Record;
import com.mirdar.O2O.ReadData;

public class CK
{
	public Map<Integer, Point> dataMap;
	public Map<String, ArrayList<com.mirdar.GA.Line>> lines = new HashMap<String, ArrayList<com.mirdar.GA.Line>>();
	int allTime = 0;
	public Map<String, Integer> len = new HashMap<String, Integer>();
	public Map<String, Map<Integer, Point>> allDataMap;

	// 用来使用禁忌搜索的参数
	public int[][] tabuTable = null;
	int max_con_iter = 2000;
	int max_iter = 10000;
	int max_cand_list = 150;

	public static void main(String[] args)
	{
		String fileShop = "E:\\tianchibigdata\\last mile delivery/part 2/shop.csv";
		String fileSpot = "E:\\tianchibigdata\\last mile delivery/part 2/spot.csv";
		String fileSite = "E:\\tianchibigdata\\last mile delivery/part 2/site.csv";
		String filename = "E:\\tianchibigdata\\last mile delivery/part 2/A007.csv";
		ReadData readData = new ReadData();
		CK ck = new CK();
		Dis dis = new Dis();

		dis.placeMap = readData.readPlace(fileSite, dis.placeMap);
		dis.placeMap = readData.readPlace(fileSpot, dis.placeMap);
		dis.placeMap = readData.readPlace(fileShop, dis.placeMap);
		ck.dataMap = ck.readData(filename);
		System.out.println("dataMap.size: " + ck.dataMap.size());
		// for (int i = 0; i < ck.dataMap.size(); i++)
		// {
		// System.out.println(ck.dataMap.get(i).pointName);
		// }
		ArrayList<Line> lineCk = ck.cKAlgorithm(ck.dataMap, dis);
		// ArrayList<Integer> codeList = ck.setCode(lineCk);
		// ck.printCode(codeList); // 编码正确
		// Code code = new Code();
		// code.init(codeList, ck.codeEva(codeList, dis));
		// System.out.println("len: " + ck.codeLength(code.codeList));
		// System.out.println(ck.codeEva(code.codeList, dis));
		// ck.tabuTable = new int[ck.dataMap.size() + 1][ck.dataMap.size() + 1];
		//
		// // for (int i = 0; i < 10; i++) //测试
		// // ck.exchange(code, dis);
		//
		// Code newCode = ck.tabuSearch(code, dis);
		// ck.printCode(newCode.codeList);
		// System.out.println(ck.codeEva(newCode.codeList, dis));
		// System.out.println("len: " + ck.codeLength(newCode.codeList));
		//
		// ArrayList<Line> lineCk2 = ck.deCode(newCode.codeList);

		System.out.println("lineCK: " + lineCk.size());
		ck.printPaths(lineCk, dis);
		System.out.println("lines.size: " + ck.lines.size());
		System.out.println("allTime: " + ck.allTime);
	}

	public ArrayList<Line> cKAlgorithm(Map<Integer, Point> dataMap, Dis dis)
	{
		ArrayList<Line> lines = new ArrayList<Line>();
		ArrayList<Path> paths = new ArrayList<Path>();
		if (dataMap.size() == 2)
		{
			Line line = new Line();
			line.start = 1;
			line.transitPoint.add(1);
			lines.add(line);
			return lines;
		}
		for (Integer i : dataMap.keySet())
		{
			if (i == 0)
				continue;
			for (Integer j : dataMap.keySet())
			{
				if (j == 0 || j == i)
					continue;
				Path path = new Path();
				path.init(i, j, dis, dataMap);
				paths.add(path);
			}
			Line line = new Line();
			line.start = i;
			// line.init();
			lines.add(line);
		}
		// System.out.println("lines.size: " + lines.size());
		// System.out.println("paths.size: " + paths.size());
		quickSort(paths, 0, paths.size() - 1);
		// System.out.println("sortpaths.size: " + paths.size());
		int times = 0;
		for (int k = paths.size() - 1; k >= 0; k--)
		{
			if (paths.get(k).flag == 0)
			{
				// System.out.println("paths.start " + paths.get(k).start + "
				// ->end " + paths.get(k).end);
				times++;
				merge(paths.get(k).start, paths.get(k).end, paths, dis, lines);
				paths.get(k).flag = 1;
			}
		}
		// System.out.println("times: " + times);
		// System.out.println("lines.size: " + lines.size());
		int size = 0;
		int time = 0;
		for (int i = 0; i < lines.size(); i++)
		{
			ArrayList<Integer> transitPoints = new ArrayList<Integer>();
			if (lines.get(i).end != 0)
				size += lines.get(i).transitPoint.size() + 2;
			else
				size += lines.get(i).transitPoint.size() + 1;
			// System.out.println();
			// System.out.print(lines.get(i).start + " ");
			transitPoints.add(lines.get(i).start);
			for (int j = 0; j < lines.get(i).transitPoint.size(); j++)
			{
				transitPoints.add(lines.get(i).transitPoint.get(j));
				// System.out.print(lines.get(i).transitPoint.get(j) + " ");
			}
			// System.out.print(lines.get(i).end + " ");
			if (lines.get(i).end != 0)
				transitPoints.add(lines.get(i).end);
			time += getTime(transitPoints, dataMap, dis);
			lines.get(i).transitPoint = transitPoints;
			// System.out.println(
			// "time: " + getTime(transitPoints, dataMap, dis) + " weight: " +
			// getWeight(transitPoints, dataMap));
		}
		// System.out.println();
		System.out.println("size: " + size);
		System.out.println("time: " + time);

		return lines;
	}

	public void remove(int i, int j, ArrayList<Path> paths)
	{
		for (int k = 0; k < paths.size(); k++)
		{
			// 去除以i开头，j结尾的其他路线
			if ((paths.get(k).start == i && paths.get(k).end != j)
					|| (paths.get(k).start != i && paths.get(k).end == j))
			{
				paths.get(k).flag = 1;
			}
			if (paths.get(k).start == j && paths.get(k).end == i)
				paths.get(k).flag = 1;
		}
	}

	// 将两条路线合并
	public void merge(int i, int j, ArrayList<Path> paths, Dis dis, ArrayList<Line> lines)
	{
		ArrayList<Integer> transitPoints = new ArrayList<Integer>();
		int iId = 0;
		int jId = 0;
		int flagi = 0;
		int flagj = 0;
		for (int k = 0; k < lines.size(); k++)
		{

			if (lines.get(k).start == i)
			{
				iId = k;
				flagi = 0;
				transitPoints.add(i);
			} else if (lines.get(k).end == i)
			{
				transitPoints.add(lines.get(k).start);
				for (int m = 0; m < lines.get(k).transitPoint.size(); m++)
				{
					transitPoints.add(lines.get(k).transitPoint.get(m));
				}
				transitPoints.add(i);
				iId = k;
				flagi = 1;
			}

			if (lines.get(k).start == j && lines.get(k).end == 0)
			{
				jId = k;
				flagj = 0;
			} else if (lines.get(k).start == j && lines.get(k).end != 0)
			{
				jId = k;
				flagj = 1;
			}

		}
		// 这里有错误
		if (flagi == 0 && flagj == 0)
		{
			// System.out.println("lines1.size: " + lines.size());
			transitPoints.add(j);
			// System.out.println("lines.get(k).size: " + transitPoints.size());
			// System.out.println("getTime(transitPoints, dataMap: " +
			// getTime(transitPoints, dataMap, dis));
			// for (int m = 0; m < transitPoints.size(); m++)
			// System.out.print(transitPoints.get(m) + " ");
			// System.out.println("-------------");
			if (getTime(transitPoints, dataMap, dis) <= 720 && getWeight(transitPoints, dataMap) <= 140)
			{
				remove(i, j, paths);
				lines.get(jId).start = i;
				lines.get(jId).end = j;
				// System.out.println(lines.get(jId).start + " ->" +
				// lines.get(jId).end);
				for (int k = 0; k < paths.size(); k++)
				{
					// 去除以i开头，j结尾的其他路线
					if (paths.get(k).start == lines.get(jId).end && paths.get(k).end == lines.get(jId).start)
					{
						paths.get(k).flag = 1;
					}
				}
				lines.remove(iId);
			} else
				remove(lines.get(iId).end, lines.get(iId).start, paths);
		} else if (flagi != 0 && flagj == 0)
		{
			// System.out.println("lines2.size: " + lines.size());
			transitPoints.add(j);
			// System.out.println("lines.get(k).size: " + transitPoints.size());
			// System.out.println("getTime(transitPoints, dataMap: " +
			// getTime(transitPoints, dataMap, dis));
			// for (int m = 0; m < transitPoints.size(); m++)
			// System.out.print(transitPoints.get(m) + " ");
			// System.out.println("-------------");
			if (getTime(transitPoints, dataMap, dis) <= 720 && getWeight(transitPoints, dataMap) <= 140)
			{
				remove(i, j, paths);
				lines.get(jId).start = lines.get(iId).start;
				lines.get(jId).end = j;
				// System.out.println(lines.get(jId).start + " ->" +
				// lines.get(jId).end);
				transitPoints.remove(0);
				transitPoints.remove(transitPoints.size() - 1);
				lines.get(jId).transitPoint = transitPoints;
				for (int k = 0; k < paths.size(); k++)
				{
					// 去除以i开头，j结尾的其他路线
					if (paths.get(k).start == lines.get(jId).end && paths.get(k).end == lines.get(jId).start)
					{
						paths.get(k).flag = 1;
					}
				}
				lines.remove(iId);
			} else
				remove(lines.get(iId).end, lines.get(iId).start, paths);
		} else
		{
			// System.out.println("lines3.size: " + lines.size());
			transitPoints.add(j);
			for (int m = 0; m < lines.get(jId).transitPoint.size(); m++)
			{
				transitPoints.add(lines.get(jId).transitPoint.get(m));
			}
			transitPoints.add(lines.get(jId).end);
			// System.out.println("lines.get(k).size: " + transitPoints.size());
			// System.out.println("getTime(transitPoints, dataMap: " +
			// getTime(transitPoints, dataMap, dis));
			// for (int m = 0; m < transitPoints.size(); m++)
			// System.out.print(transitPoints.get(m) + " ");
			// System.out.println("-------------");
			if (getTime(transitPoints, dataMap, dis) <= 720 && getWeight(transitPoints, dataMap) <= 140)
			{
				remove(i, j, paths);
				lines.get(jId).start = lines.get(iId).start;
				// System.out.println(lines.get(jId).start + " ->" +
				// lines.get(jId).end);
				transitPoints.remove(0);
				transitPoints.remove(transitPoints.size() - 1);
				lines.get(jId).transitPoint = transitPoints;
				for (int k = 0; k < paths.size(); k++)
				{
					// 去除以i开头，j结尾的其他路线
					if (paths.get(k).start == lines.get(jId).end && paths.get(k).end == lines.get(jId).start)
					{
						paths.get(k).flag = 1;
					}
				}
				lines.remove(iId);

			} else
				remove(lines.get(iId).end, lines.get(iId).start, paths);
		}
		/*
		 * for (int m = 0; m < lines.size(); m++) {
		 * System.out.print(lines.get(m).start + " "); for (int n = 0; n <
		 * lines.get(m).transitPoint.size(); n++)
		 * System.out.print(lines.get(m).transitPoint.get(n) + " ");
		 * System.out.print(lines.get(m).end); System.out.println(); }
		 */
	}

	public int getTime(ArrayList<Integer> path, Map<Integer, Point> dataMap, Dis dis)
	{
		int time = 0;
		for (int i = 0; i < path.size(); i++)
		{
			if (i == 0)
			{
				time += dis.disPlace(dataMap.get(0).pointName, dataMap.get(path.get(i)).pointName);
				time += Math.round(3 * Math.sqrt(dataMap.get(path.get(i)).goods_num) + 5);
			} else
			{
				time += dis.disPlace(dataMap.get(path.get(i - 1)).pointName, dataMap.get(path.get(i)).pointName);
				// if (i < path.size() - 1)
				time += Math.round(3 * Math.sqrt(dataMap.get(path.get(i)).goods_num) + 5);
			}
		}

		return time;
	}

	public int getWeight(ArrayList<Integer> path, Map<Integer, Point> dataMap)
	{
		int weight = 0;
		for (int i = 0; i < path.size(); i++)
		{
			weight += dataMap.get(path.get(i)).goods_num;
		}

		return weight;
	}

	public void quickSort(ArrayList<Path> paths, int s, int e)
	{
		if (s < e)
		{
			int m = partition(paths, s, e);
			quickSort(paths, s, m - 1);
			quickSort(paths, m + 1, e);
		}
	}

	public int partition(ArrayList<Path> paths, int s, int e) // 以第一个元素作为分割元素
	{
		int m = s;
		Path path = paths.get(s);
		for (int i = s + 1; i <= e; i++)
		{
			if (paths.get(i).cost <= path.cost)
			{
				m++;
				Path temp = paths.get(i);
				paths.set(i, paths.get(m));
				paths.set(m, temp);
			}
		}

		Path temp = paths.get(m);
		paths.set(m, paths.get(s));
		paths.set(s, temp);

		return m;
	}

	public int printPaths(ArrayList<Line> lineCk, Dis dis)
	{
		ArrayList<com.mirdar.GA.Line> lineList = new ArrayList<com.mirdar.GA.Line>();
		lines.put(dataMap.get(0).pointName, lineList); // 一个网点的所有路线

		for (int i = 0; i < lineCk.size(); i++)
		{
			int arriveTime = 0; // 记录每条线路的离开到达时间
			com.mirdar.GA.Line line = new com.mirdar.GA.Line(); // 一条路线
			Record record = new Record(); // 路线中的一条记录
			Line linec = lineCk.get(i);
			for (int j = 0; j < linec.transitPoint.size(); j++) // 网点
			{
				record = new Record();
				record.place_id = dataMap.get(0).pointName;
				record.arriveTime = 0;
				record.departureTime = 0;
				record.num = dataMap.get(linec.transitPoint.get(j)).goods_num;
				record.order_id = dataMap.get(linec.transitPoint.get(j)).order_id;
				line.line.add(record);
				// printRecord(record);
			}
			for (int j = 0; j < linec.transitPoint.size(); j++)
			{
				if (j == 0)
				{

					arriveTime += dis.disPlace(dataMap.get(0).pointName,
							dataMap.get(linec.transitPoint.get(j)).pointName);
					record = new Record();
					record.place_id = dataMap.get(linec.transitPoint.get(j)).pointName;
					record.arriveTime = arriveTime;
					arriveTime += Math.round(3 * Math.sqrt(dataMap.get(linec.transitPoint.get(j)).goods_num) + 5);
					record.departureTime = arriveTime;
					record.num = -dataMap.get(linec.transitPoint.get(j)).goods_num;
					record.order_id = dataMap.get(linec.transitPoint.get(j)).order_id;
					// printRecord(record);
					if (j == linec.transitPoint.size() - 1) // 路线中只有一个点
					{
						// arriveTime+=
						// dis(dataMap.get(0),dataMap.get(p.path.get(j))); //回网点
						line.endToStart = dis.disPlace(dataMap.get(0).pointName,
								dataMap.get(linec.transitPoint.get(j)).pointName);
						line.spot_id = dataMap.get(linec.transitPoint.get(j)).pointName;
						line.time = arriveTime; // 每条线路的总开销，不包括回网点
					}
					line.line.add(record);
				} else
				{
					arriveTime += dis.disPlace(dataMap.get(linec.transitPoint.get(j - 1)).pointName,
							dataMap.get(linec.transitPoint.get(j)).pointName);
					record = new Record();
					record.place_id = dataMap.get(linec.transitPoint.get(j)).pointName;
					record.arriveTime = arriveTime;
					arriveTime += Math.round(3 * Math.sqrt(dataMap.get(linec.transitPoint.get(j)).goods_num) + 5);
					record.departureTime = arriveTime;
					record.num = -dataMap.get(linec.transitPoint.get(j)).goods_num;
					record.order_id = dataMap.get(linec.transitPoint.get(j)).order_id;
					line.line.add(record);
					// printRecord(record);
					if (j == linec.transitPoint.size() - 1)
					{
						line.endToStart = dis.disPlace(dataMap.get(0).pointName,
								dataMap.get(linec.transitPoint.get(j)).pointName);
						line.spot_id = dataMap.get(linec.transitPoint.get(j)).pointName;
						// arriveTime+=
						// dis(dataMap.get(0),dataMap.get(linec.transitPoint.get(j)));
						// //回网点
						line.time = arriveTime; // 每条线路的总开销
					}
				}
			}
			// System.out.println();
			allTime += arriveTime;
			lineList.add(line);
		}
		System.out.println();
		return (1 + allTime / 720);
	}

	public void printRecord(Record record)
	{
		System.out.println(record.place_id + " " + record.arriveTime + " " + record.departureTime + " " + record.num);
	}

	public Map<Integer, Point> readData(String filename)
	{
		Map<Integer, Point> map = new HashMap<Integer, Point>();

		File file = new File(filename);
		BufferedReader reader = null;
		int i = 0; // 网点的id为0
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String content = null;
			while ((content = reader.readLine()) != null)
			{
				String[] strings = content.split(",");
				Point point = new Point();
				point.setPointName(strings[0]);
				point.setGoods_num(Integer.parseInt(strings[1]));
				point.setLon(Double.parseDouble(strings[2]));
				point.setLan(Double.parseDouble(strings[3]));
				point.setPointId(i); // 因为每个配送点只有一个，所以这里直接映射id
				map.put(point.getPointId(), point);
				i++;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return map;
	}

	public Map<String, Map<Integer, Point>> readAllData(String filename)
	{
		Map<String, Map<Integer, Point>> mapAll = new HashMap<String, Map<Integer, Point>>();
		Map<Integer, Point> map = null;
		File file = new File(filename);
		BufferedReader reader = null;
		String siteName = null;
		String lastName = "rasd";
		int i = 0; // 网点的id为0
		int j = 0;
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String content = null;
			while ((content = reader.readLine()) != null)
			{
				String[] strings = content.split(",");
				Point point = new Point();
				siteName = strings[0];
				if (!lastName.equals(siteName) && !lastName.equals("rasd"))
					len.put(lastName, i - 1);
				if (!mapAll.containsKey(siteName))
				{
					map = new HashMap<Integer, Point>();
					mapAll.put(siteName, map);
					i = 0;
					j++;
				}
				lastName = siteName;
				point.setPointName(strings[1]);
				point.setGoods_num(Integer.parseInt(strings[2]));
				point.setLon(Double.parseDouble(strings[3]));
				point.setLan(Double.parseDouble(strings[4]));
				point.order_id = strings[5];
				point.setPointId(i); // 因为每个配送点只有一个，所以这里直接映射id

				map.put(point.getPointId(), point);
				i++;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		len.put(siteName, i - 1);
		return mapAll;
	}

}
