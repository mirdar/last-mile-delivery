package com.mirdar.TabuSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.mirdar.CKAlgorithm.CK;
import com.mirdar.CKAlgorithm.Dis;
import com.mirdar.CKAlgorithm.Line;
import com.mirdar.GA.Point;
import com.mirdar.O2O.ReadData;
import com.mirdar.test.Place;

public class TabuSearch
{

	public Map<String, Place> placeMap = new HashMap<String, Place>();
	public int count = 0;
	public Map<Integer, String> courier = new HashMap<Integer, String>();
	public Map<Integer, Point> dataMap;
	public Map<String, ArrayList<com.mirdar.GA.Line>> lines;

	public int[][] tabuTable = null;
	public int max_con_iter = 50;
	public int max_iter = 2000;
	public int max_cand_list = 10;

	public static void main(String[] args) throws IOException
	{
		String fileShop = "F:\\ML\\last mile delivery/shop.csv";
		String fileSpot = "F:\\ML\\last mile delivery/spot.csv";
		String fileSite = "F:\\ML\\last mile delivery/site.csv";
		ReadData readData = new ReadData();
		Dis dis = new Dis();

		dis.placeMap = readData.readPlace(fileSite, dis.placeMap);
		dis.placeMap = readData.readPlace(fileSpot, dis.placeMap);
		dis.placeMap = readData.readPlace(fileShop, dis.placeMap);

		String filename = "F:\\ML\\last mile delivery/branch_data2.csv";
		CK ck = new CK();
		TabuSearch ts = new TabuSearch();
		ck.allDataMap = ck.readAllData(filename);
		for (String key1 : ck.allDataMap.keySet())
		{
			ck.dataMap = ck.allDataMap.get(key1);
			// ts.max_con_iter = 500 + ck.dataMap.size() * 10; // 重置参数
			// ts.max_iter = 1000 + ck.dataMap.size() * 30;
			// ts.max_cand_list = 100 + 2 * ck.dataMap.size() * 2;
			ts.dataMap = ck.dataMap;
			ArrayList<com.mirdar.CKAlgorithm.Line> lineCk = ck.cKAlgorithm(ck.dataMap, dis);
			ArrayList<Integer> codeList = ts.setCode(lineCk);
			Code code = new Code();
			code.init(codeList, ts.codeEva(codeList, dis));
			System.out.println("len: " + ts.codeLength(code.codeList));
			System.out.println("eval: " + ts.codeEva(code.codeList, dis));
			ts.tabuTable = new int[ck.dataMap.size()][ck.dataMap.size()];
			Code newCode = ts.tabuSearch(code, dis);
			System.out.println("len: " + ts.codeLength(newCode.codeList));
			System.out.println("eval: " + ts.codeEva(newCode.codeList, dis));
			// ts.printCode(newCode.codeList);
			ArrayList<Line> lineCk2 = ts.deCode(newCode.codeList);
			ck.printPaths(lineCk2, dis);
			// ts.max_con_iter = 200; // 重置参数
			// ts.max_iter = 1000;
			// ts.max_cand_list = 150;
		}
	}

	public Code tabuSearch(Code code, Dis dis)
	{

		int iter = 0;
		int cons_iter = 0;
		Code bestCode = code; // 目前为止最后解
		Code currentCode = code; // 当前解
		while (iter <= max_iter && cons_iter <= max_con_iter)
		{
			// System.out.println("theBestOne.eval: " + theBestOne.eval);
			int cand_list = 0;
			ArrayList<Code> codes = new ArrayList<Code>(); // 用来存放候选集
			ArrayList<Code> tcodes = new ArrayList<Code>(); // 用来存放禁忌的对象
			while (cand_list <= max_cand_list)
			{
				Code newCode = exchange(currentCode, dis);
				if (tabuTable[newCode.i1][newCode.i2] > 0)
					tcodes.add(newCode);
				else
					codes.add(newCode);
				cand_list++;
			}
			int eval = Integer.MAX_VALUE;
			for (int i = 0; i < codes.size(); i++) // 找到候选集中最好的code
			{
				if (codes.get(i).eval < eval)
				{
					currentCode = codes.get(i);
					eval = codes.get(i).eval;
				}
			}
			for (int i = 0; i < tcodes.size(); i++)
			{
				if (tcodes.get(i).eval < currentCode.eval && tcodes.get(i).eval < bestCode.eval)
				{
					currentCode = tcodes.get(i);
					// tabuTable[tcodes.get(i).i1][tcodes.get(i).i2] = 0;
				}
			}
			for (int i = 0; i < tabuTable.length; i++) // 禁忌表中禁忌状态减一
			{
				for (int j = i; j < tabuTable.length; j++)
				{
					if (tabuTable[i][j] > 0)
						tabuTable[i][j]--;
				}
			}
			tabuTable[currentCode.i2][currentCode.i1]++; // 记录频率
			tabuTable[currentCode.i1][currentCode.i2] = (int) Math // 记录禁忌长度
					.sqrt(dataMap.size()) + 2 * tabuTable[currentCode.i2][currentCode.i1];
			iter++;
			if (bestCode.eval > currentCode.eval)
			{
				cons_iter = 0;
				bestCode = currentCode;
			} else
				cons_iter++;
		}

		return bestCode;
	}

	public Code exchange(Code code, Dis dis)
	{
		Code newCode = new Code();
		Random rand = new Random();
		int i = rand.nextInt(3);
		int i1 = rand.nextInt(code.codeList.size());
		int i2 = rand.nextInt(code.codeList.size());
		while (i1 == i2 || i1 == 0 || i2 == 0 || (code.codeList.get(i1) == 0 && code.codeList.get(i2) == 0)) // 不选择同一个点，且都不能移动code的第一个元素
		{
			i1 = rand.nextInt(code.codeList.size());
			i2 = rand.nextInt(code.codeList.size());
		}
		if (i1 > i2)
		{
			int temp = i1;
			i1 = i2;
			i2 = temp;
		}
		if (i == 0) // 顶点重新分配
		{
			for (int j = 0; j < code.codeList.size(); j++)
			{
				if (j == i1)
					continue;
				else if (j == i2)
				{
					newCode.codeList.add(code.codeList.get(i1));
					newCode.codeList.add(code.codeList.get(i2));
				} else
					newCode.codeList.add(code.codeList.get(j));
			}
		} else if (i == 1) // 顶点交换
		{
			for (int j = 0; j < code.codeList.size(); j++)
			{
				if (j == i1)
					newCode.codeList.add(code.codeList.get(i2));
				else if (j == i2)
				{
					newCode.codeList.add(code.codeList.get(i1));
				} else
					newCode.codeList.add(code.codeList.get(j));
			}
		}
		// else if (i == 2 && (i1 - i2) == 5) //
		// 分裂操作，自己加的操作，为了使线路可以分裂组合，增加多样性,但是分裂的几率很小
		// {
		// for (int j = 0; j < code.codeList.size(); j++)
		// {
		// if (j == i1)
		// {
		// newCode.codeList.add(0);
		// newCode.codeList.add(code.codeList.get(i1));
		// }
		// else
		// newCode.codeList.add(code.codeList.get(j));
		// }
		//
		// }
		else // 2-opt or 尾巴交换，一个在同一条线路内交换，一个在不同线路间交换
		{
			int flag = 0; // 0表示路线内，1表示路线间
			for (int j = i1 + 1; j <= i2; j++)
			{
				if (code.codeList.get(j) == 0)
					flag = 1;
			}
			if (flag == 0) // 2-opt
			{
				for (int j = 0; j < code.codeList.size(); j++)
				{
					if (j == i1)
					{
						for (int m = i2; m >= i1; m--)
						{
							newCode.codeList.add(code.codeList.get(m));
							j++;
						}
						j--;
					} else
					{
						newCode.codeList.add(code.codeList.get(j));
					}
				}
			} else
			{
				ArrayList<Integer> temp1 = new ArrayList<Integer>();
				ArrayList<Integer> temp2 = new ArrayList<Integer>();
				for (int j = i1; j < code.codeList.size(); j++)
				{
					temp1.add(code.codeList.get(j));
					if (code.codeList.get(j + 1) == 0)
						break;
				}
				for (int j = i2; j < code.codeList.size(); j++)
				{
					temp2.add(code.codeList.get(j));
					if (j != code.codeList.size() - 1 && code.codeList.get(j + 1) == 0)
						break;
				}
				for (int j = 0; j < code.codeList.size(); j++)
				{
					if (j < i1)
					{
						newCode.codeList.add(code.codeList.get(j));
					} else if (j < i1 + temp1.size() - 1)
					{
						newCode.codeList.add(code.codeList.get(j));
					} else if (j < i2)
						newCode.codeList.add(code.codeList.get(j));
					else if (j < i2 + temp2.size() - 1)
						newCode.codeList.add(code.codeList.get(j));
					else
						newCode.codeList.add(code.codeList.get(j));
				}
			}

		}
		for (int j = 0; j < newCode.codeList.size(); j++) // 去除00这种情况
		{
			if (j < newCode.codeList.size() - 1 && newCode.codeList.get(j) == 0 && newCode.codeList.get(j + 1) == 0)
			{
				newCode.codeList.remove(j + 1);
			}
			if (j == newCode.codeList.size() - 1 && newCode.codeList.get(j) == 0)
			{
				newCode.codeList.remove(j);
			}
		}
		if (newCode.codeList.size() != 0)
		{
			newCode.init(newCode.codeList, codeEva(newCode.codeList, dis));
			newCode.i1 = code.codeList.get(i1);
			newCode.i2 = code.codeList.get(i2);
		}

		return newCode;
	}

	public int codeEva(ArrayList<Integer> code, Dis dis)
	{
		int eval = 0;
		int weight = 0;
		int time = 0;
		for (int i = 0; i < code.size(); i++)
		{
			if (i == code.size() - 1)
			{
				if (weight > 140 || time > 720)
				{
					eval += 1000000;
				}
			} else if (code.get(i + 1) == 0)
			{
				if (weight > 140 || time > 720)
				{
					eval += 1000000;
					break;
				}
				weight = 0;
				time = 0;
			} else
			{
				eval += dis.disPlace(dataMap.get(code.get(i + 1)).pointName, dataMap.get(code.get(i)).pointName)
						+ Math.round(3 * Math.sqrt(dataMap.get(code.get(i + 1)).goods_num) + 5);
				if (code.get(i + 1) != 0)
					time += dis.disPlace(dataMap.get(code.get(i + 1)).pointName, dataMap.get(code.get(i)).pointName)
							+ Math.round(3 * Math.sqrt(dataMap.get(code.get(i + 1)).goods_num) + 5);
				weight += dataMap.get(code.get(i + 1)).goods_num;
			}
		}

		return eval;
	}

	public ArrayList<Line> deCode(ArrayList<Integer> code)
	{
		ArrayList<Line> lines = new ArrayList<Line>();
		Line line = new Line();
		int flag = 0;
		for (int i = 1; i < code.size(); i++)
		{
			if (code.get(i) == 0)
			{
				lines.add(line);
				if (i == code.size() - 1)
					flag = 1;
				line = new Line();
			} else
			{
				line.transitPoint.add(code.get(i));
			}
		}
		if (flag == 0)
			lines.add(line);
		return lines;
	}

	public ArrayList<Integer> setCode(ArrayList<Line> lineCk)
	{
		ArrayList<Integer> code = new ArrayList<Integer>();
		for (int i = 0; i < lineCk.size(); i++)
		{
			code.add(0);
			for (int j = 0; j < lineCk.get(i).transitPoint.size(); j++)
			{
				code.add(lineCk.get(i).transitPoint.get(j));
			}
		}

		return code;
	}

	public void printCode(ArrayList<Integer> code)
	{
		for (int i = 0; i < code.size(); i++)
		{
			System.out.print(code.get(i) + "  ");
		}
		System.out.println();
	}

	public int codeLength(ArrayList<Integer> code)
	{
		int length = 0;
		for (int i = 0; i < code.size(); i++)
		{
			if (code.get(i) != 0)
				length++;
		}

		return length;
	}

}
