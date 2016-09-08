package com.mirdar.GA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.mirdar.O2O.Order;

/*
 * 1. ÿ������ֿ�����
 * 2. ÿ��·�����������
 * GAҪ������������·�ߵ�ʱ��Ҫ�涨·�ߵĳ��ȣ���Ȼ�䳬��720��������Ϊ��Ч·��
 * ��Ӌ��fitnessʱ����ν����Ҳ�Ǹ����⣬ѡ�����Ž��뷽ʽ
 */
public class GA
{

	public int length; // Ⱦɫ�峤��
	public int maxNum = 140; // ÿ�����������
	public int popSize = 100; // ��Ⱥ��С
	public double pc = 0.88; // ������
	public double pm = 0.1; // ������
	public int maxGen = 800; // ����Ⱦɫ����Ӧ������Ϊ���Ⱦɫ����ٱ���ֹ
	public ArrayList<Chomo> bestChomo = new ArrayList<Chomo>();// ����������õ�Ⱦɫ��
	public Map<Integer, Point> dataMap;
	public Map<String, ArrayList<Order>> orderMap;

	// ȫ������һ����
	public Map<String, Integer> len = new HashMap<String, Integer>();
	public Map<String, Map<Integer, Point>> allDataMap;

	public Map<String, ArrayList<Line>> lines = new HashMap<String, ArrayList<Line>>(); // ����ֻ��¼��һ������

	public int allTime = 0;

	public int recordSize = 0;

	public static void main(String[] args)
	{

		String filename = "E:\\tianchibigdata\\last mile delivery/part 2/A007.csv";
		GA ga = new GA();
		ga.dataMap = ga.readData(filename);
		System.out.println("spot_id  num  lon  lan  ID");
		for (Integer key : ga.dataMap.keySet()) // ��ӡ����
		{
			Point point = ga.dataMap.get(key);
			System.out.println(point.getPointName() + "  " + point.getGoods_num() + "  " + point.getLon() + "  "
					+ point.getLan() + "  " + point.getPointId());
		}
		ArrayList<Chomo> pop = ga.getInitPop(); // ��ʼȺ�����ɳɹ�
		System.out.println("ininPop.size: " + pop.size());

		ArrayList<Chomo> newPop = new ArrayList<Chomo>();
		for (int i = 0; i < ga.maxGen; i++)
		{
			newPop = ga.select(pop);
			newPop = ga.crossAndVar(newPop);
			pop = newPop;
		}

		// newPop = ga.select(pop);
		// newPop = ga.crossAndVar(newPop);
		// pop = newPop;
		// while(ga.fitness(ga.bestChomo.get(ga.bestChomo.size()-1)) > 2E-8)
		// {
		// newPop = ga.select(pop);
		// newPop = ga.crossAndVar(newPop);
		// pop = newPop;
		// }

		System.out.println(ga.bestChomo.size());
		ga.printChomo(ga.bestChomo.get(ga.bestChomo.size() - 1));
		// for(int i=0;i<ga.bestChomo.size();i++)
		// {
		// System.out.println(ga.fitness(ga.bestChomo.get(i)));
		// //��fitness������ı�ôȾɫ��·�ߵķ���
		// }
		System.out.println(ga.allTime);
		System.out.println("·�������� " + ga.lines.get(ga.dataMap.get(0).pointName).size());

		/*
		 * for(int i=0;i< ga.bestChomo.size();i++)//��ӡÿ����Ⱥ�е���ý� { int[] x =
		 * ga.bestChomo.get(i).getChomo(); for(int j=0;j<x.length;j++) {
		 * System.out.print(x[j]+" "); } System.out.println(); }
		 */

	}

	public Chomo getChomo() // ����������еõ�һ��Ⱦɫ�壬��Ϊû�г������ƣ�����û����֤Ⱦɫ��
	{
		int[] sequence = new int[length];
		Chomo chomo = new Chomo();
		for (int i = 0; i < length; i++)
		{
			sequence[i] = i + 1;
		}
		Random random = new Random();
		for (int i = 0; i < length; i++)
		{ // �����������
			int p = random.nextInt(length);
			int tmp = sequence[i];
			sequence[i] = sequence[p];
			sequence[p] = tmp;
		}
		random = null;
		chomo.setChomo(sequence);
		chomo.setFit(fitness(chomo));// ����Ӧ��
		return chomo;
	}

	public ArrayList<Chomo> getInitPop() // ��ʼ���õ�һ����Ⱥ
	{
		ArrayList<Chomo> pop = new ArrayList<Chomo>();
		for (int i = 0; i < popSize; i++)
		{
			Chomo chomo = getChomo();
			pop.add(chomo);
		}
		return pop;
	}

	// ѡ������,������Ӧ�Ⱥ���ѡ�������뽻��أ�����ѡ��
	public ArrayList<Chomo> select(ArrayList<Chomo> pop)
	{
		double[] p = new double[pop.size()]; // ���ʺ���
		double[] fit = new double[pop.size()];

		ArrayList<Chomo> crossPool = new ArrayList<Chomo>();
		quickSort(pop, 0, pop.size() - 1); // ��������
		ArrayList<Chomo> popSort = pop;
		crossPool.add(popSort.get(popSort.size() - 1)); // ��õĸ���ֱ�ӽ�����һ������������������ѡ��õ�
		if (bestChomo.size() == 0)
			bestChomo.add(popSort.get(popSort.size() - 1)); // ÿһ����õ�Ⱦɫ�嶼������
		else if (bestChomo.get(bestChomo.size() - 1).getFit() <= popSort.get(popSort.size() - 1).getFit())
			bestChomo.add(popSort.get(popSort.size() - 1));
		double fitSum = 0;
		for (int i = 0; i < pop.size(); i++)
		{
			fitSum += popSort.get(i).getFit();
			fit[i] = popSort.get(i).getFit();
		}
		for (int i = 0; i < fit.length; i++) //// ͨ��fit����˥������
		{
			if (i - 1 >= 0)
			{
				if (i != fit.length - 1)
					p[i] = p[i - 1] + fit[i] / fitSum;
				else
					p[i] = 1;
			} else
				p[i] = fit[i] / fitSum;
		}
		for (int i = 0; i < popSize; i++) // ���²���popSize���������Ⱥ
		{
			double rand = new Random().nextDouble();
			for (int m = 0; m < p.length; m++) // ���ݸ��ʷֲ������Ӧ�ĸ���
			{
				if (m == 0)
				{
					if (rand <= p[m])
						crossPool.add(popSort.get(m));
				} else
				{
					if (rand > p[m - 1] && rand <= p[m] && m != p.length - 1)
						crossPool.add(popSort.get(m));
				}
				if (i == popSize - 1 && crossPool.size() < popSize)
				{
					i--;
				}

			}
		}
		return crossPool;
	}

	public ArrayList<Chomo> crossAndVar(ArrayList<Chomo> pop) // ����������
	{
		ArrayList<Chomo> newPop1 = new ArrayList<Chomo>();
		for (int i = 0; i < pop.size() / 2; i++) // ����ż����������������Ϊż��,����
		{
			double rand = new Random().nextDouble();
			if (rand <= pc)
			{
				Chomo chomo1 = cross2(pop.get(i), pop.get(i + pop.size() / 2));
				newPop1.add(chomo1);
				Chomo chomo2 = cross2(pop.get(i + pop.size() / 2), pop.get(i));
				newPop1.add(chomo2);
			} else
			{
				newPop1.add(pop.get(i));
				newPop1.add(pop.get(i + pop.size() / 2));
			}
		}
		pop = newPop1;
		ArrayList<Chomo> newPop2 = new ArrayList<Chomo>();
		for (int i = 0; i < pop.size(); i++) // ����
		{
			double rand = new Random().nextDouble();
			if (rand <= pm)
			{
				Chomo chomo = variation(pop.get(i));
				newPop2.add(chomo);
			} else
			{
				newPop2.add(pop.get(i));
			}
		}

		return newPop2;
	}

	public Chomo cross2(Chomo chomo1, Chomo chomo2)
	{
		Chomo chomo = new Chomo();
		int[] code1 = new int[chomo1.getChomo().length];
		Random random1 = new Random();
		Random random2 = new Random();
		int rand1 = random1.nextInt(chomo1.getChomo().length);
		int m = 0;
		while (m < 1000) // Ϊ���������֮���м�࣬��Ϊ��������������ǰ�ʱ��Ļ��Ϳ�������������������һ��
		{
			m++;
		}
		int rand2 = random2.nextInt(chomo1.getChomo().length);
		if (rand1 >= rand2)
		{
			int temp = rand1;
			rand1 = rand2;
			rand2 = temp;
		}
		for (int i = 0; i < rand2 - rand1; i++)// ����Ⱦɫ��1��������
		{
			code1[i] = chomo1.getChomo()[rand1 + i];
		}
		int k = rand2 - rand1;

		for (int i = 0; i < chomo2.getChomo().length; i++) // ������һ��Ⱦɫ��
		{
			int j = 0;
			while (chomo2.getChomo()[i] != code1[j] && j < rand2 - rand1)
			{
				j++;
			}
			if (j == rand2 - rand1)
			{
				code1[k] = chomo2.getChomo()[i];
				k++;
			}
		}
		chomo.setChomo(code1);
		chomo.setFit(fitness(chomo));// ����Ӧ��
		return chomo;
	}

	public Chomo cross(Chomo chomo1, Chomo chomo2) // �������ӣ�˳�򽻲�
	{
		Chomo chomo = new Chomo();
		int[] code1 = new int[chomo1.getChomo().length];
		int m = 0;
		while (m < 10) // Ϊ���������֮���м�࣬��Ϊ��������������ǰ�ʱ��Ļ��Ϳ�������������������һ��
		{
			m++;
		}
		int[] code2 = new int[chomo2.getChomo().length];
		Random random1 = new Random();
		Random random2 = new Random();
		int rand1 = random1.nextInt(chomo1.getChomo().length);
		int rand2 = random2.nextInt(chomo1.getChomo().length);
		if (rand1 >= rand2)
		{
			int temp = rand1;
			rand1 = rand2;
			rand2 = temp;
		}
		for (int i = rand1; i <= rand2; i++)// ����Ⱦɫ��1��������
		{
			code1[i] = chomo1.getChomo()[i];
		}
		for (int j = 0; j < chomo2.getChomo().length; j++)
		{
			code2[j] = 0;
			for (int i = rand1; i <= rand2; i++)
			{
				if (chomo2.getChomo()[j] == code1[i]) // ��Ⱦɫ��2�н�Ⱦɫ��1��������Ԫ������Ϊ-1
					code2[j] = -1;
			}
			if (code2[j] != -1)
				code2[j] = chomo2.getChomo()[j];
		}
		// �����ڱ���ǰ����Ƿ��и��ŵĽ⣬������bestChomo��
		int j = 0;
		for (int i = 0; i < code1.length; i++)
		{
			if (i == rand1)
			{
				i = rand2 + 1;
				if (i >= code1.length)
					break;
			}
			while (code2[j] == -1)
			{
				j++;
			}
			code1[i] = code2[j];
			j++;
		}
		chomo.setChomo(code1);
		chomo.setFit(fitness(chomo));// ����Ӧ��
		return chomo;
	}

	public Chomo variation(Chomo chomo) // �������ӣ�2-��������
	{
		Chomo chomo2 = new Chomo();
		int[] code = new int[chomo.getChomo().length];
		int rand1 = new Random().nextInt(code.length);
		int rand2 = new Random().nextInt(code.length);
		while (rand1 == rand2)
		{
			rand2 = new Random().nextInt(code.length);
		}
		for (int i = 0; i < code.length; i++)
		{
			code[i] = chomo.getChomo()[i];
		}
		int temp = code[rand1];
		code[rand1] = code[rand2];
		code[rand2] = temp;
		chomo2.setChomo(code);
		chomo2.setFit(fitness(chomo2));// ����Ӧ��
		return chomo2;
	}

	// ���fitness�����ŵ㲻����Ӧ�ý�ʱ��������Ϊ�ͷ�����룬��������ΪӲ������������������
	public double fitness(Chomo cho) // ������Ӧ��
	{
		int[] chomo = cho.chomo;
		double fit = 0; // ��Ӧ��
		int carNum = 0; // ������Ŀ
		int num = 0; // ÿ�������ۼƼ�����ÿ����������
		int eachFit = 0;
		Path path = new Path();
		for (int i = 0; i < chomo.length; i++)
		{
			num += dataMap.get(chomo[i]).getGoods_num();
			//
			if (num > 140 || eachFit > 720)
			{
				cho.path.add(path);
				path = new Path();
				fit += dis(dataMap.get(0), dataMap.get(chomo[i - 1]));// ��·���������ص�����
				fit += dis(dataMap.get(0), dataMap.get(chomo[i]));// ����һ����·����ʼ
				if (eachFit - dis(dataMap.get(chomo[i - 1]), dataMap.get(chomo[i]))
						+ dis(dataMap.get(0), dataMap.get(chomo[i - 1])) > 720)
				{
					fit += 10000000 * (eachFit - dis(dataMap.get(chomo[i - 1]), dataMap.get(chomo[i]))
							+ dis(dataMap.get(0), dataMap.get(chomo[i - 1])) - 720);
				}
				eachFit = (int) (dis(dataMap.get(0), dataMap.get(chomo[i]))
						+ Math.round(3 * Math.sqrt(dataMap.get(chomo[i]).goods_num) + 5));
				num = dataMap.get(chomo[i]).getGoods_num();
				path.path.add(chomo[i]);
				carNum++;
			} else
			{
				if (i >= 1)
				{
					fit += dis(dataMap.get(chomo[i - 1]), dataMap.get(chomo[i]));// ��·�����붼���
					eachFit += dis(dataMap.get(chomo[i - 1]), dataMap.get(chomo[i]))
							+ Math.round(3 * Math.sqrt(dataMap.get(chomo[i]).goods_num) + 5);
					path.path.add(chomo[i]);
					if (i != chomo.length - 1 && eachFit + dis(dataMap.get(chomo[i]), dataMap.get(chomo[i + 1])) > 720)
						eachFit += dis(dataMap.get(chomo[i]), dataMap.get(chomo[i + 1]));
				} else
				{
					fit += dis(dataMap.get(0), dataMap.get(chomo[i])); // ��ʼ���
					eachFit += dis(dataMap.get(0), dataMap.get(chomo[i]))
							+ Math.round(3 * Math.sqrt(dataMap.get(chomo[i]).goods_num) + 5);
					path.path.add(chomo[i]);
				}
			}
		}
		cho.path.add(path);
		fit += dis(dataMap.get(0), dataMap.get(chomo[chomo.length - 1]));
		return 10 / fit;
	}

	public int printChomo(Chomo chomo)
	{

		for (int i = 0; i < chomo.path.size(); i++)
		{
			int time = 0;
			for (int j = 0; j < chomo.path.get(i).path.size(); j++)
			{
				if (j == 0)
					time += dis(dataMap.get(0), dataMap.get(chomo.path.get(i).path.get(j)))
							+ 3 * Math.sqrt(dataMap.get(chomo.path.get(i).path.get(j)).goods_num) + 5;
				else
					time += dis(dataMap.get(chomo.path.get(i).path.get(j - 1)),
							dataMap.get(chomo.path.get(i).path.get(j)))
							+ 3 * Math.sqrt(dataMap.get(chomo.path.get(i).path.get(j)).goods_num) + 5;

			}
			recordSize += chomo.path.get(i).path.size();
		}

		return printPaths(chomo.path);
	}

	public int printPaths(ArrayList<Path> paths)
	{
		int arriveTime = 0;
		ArrayList<Line> lineList = new ArrayList<Line>();
		lines.put(dataMap.get(0).pointName, lineList); // һ�����������·��
		for (int i = 0; i < paths.size(); i++)
		{
			int firstTime = 0;
			int endTime = 0;
			Line line = new Line(); // һ��·��
			Record record = new Record(); // ·���е�һ����¼
			Path p = paths.get(i);
			for (int j = 0; j < p.path.size(); j++)
			{
				record = new Record();
				record.place_id = dataMap.get(0).pointName;
				record.arriveTime = arriveTime;
				record.departureTime = arriveTime;
				record.num = dataMap.get(p.path.get(j)).goods_num;
				record.order_id = dataMap.get(p.path.get(j)).order_id;
				line.line.add(record);
				firstTime = arriveTime;
				printRecord(record);
			}
			for (int j = 0; j < p.path.size(); j++)
			{
				if (j == 0)
				{

					arriveTime += dis(dataMap.get(0), dataMap.get(p.path.get(j)));
					record = new Record();
					record.place_id = dataMap.get(p.path.get(j)).pointName;
					record.arriveTime = arriveTime;
					arriveTime += Math.round(3 * Math.sqrt(dataMap.get(p.path.get(j)).goods_num) + 5);
					record.departureTime = arriveTime;
					record.num = -dataMap.get(p.path.get(j)).goods_num;
					record.order_id = dataMap.get(p.path.get(j)).order_id;

					if (j == p.path.size() - 1) // ·����ֻ��һ����
					{
						arriveTime += dis(dataMap.get(0), dataMap.get(p.path.get(j)));
						line.endToStart = dis(dataMap.get(0), dataMap.get(p.path.get(j)));
						line.spot_id = dataMap.get(p.path.get(j)).pointName;
						endTime = arriveTime;
						line.time = arriveTime; // ÿ����·���ܿ���
					}
					line.line.add(record);
					printRecord(record);
				} else
				{
					arriveTime += dis(dataMap.get(p.path.get(j - 1)), dataMap.get(p.path.get(j)));
					record = new Record();
					record.place_id = dataMap.get(p.path.get(j)).pointName;
					record.arriveTime = arriveTime;
					arriveTime += Math.round(3 * Math.sqrt(dataMap.get(p.path.get(j)).goods_num) + 5);
					record.departureTime = arriveTime;
					record.num = -dataMap.get(p.path.get(j)).goods_num;
					record.order_id = dataMap.get(p.path.get(j)).order_id;
					line.line.add(record);
					printRecord(record);
					if (j == p.path.size() - 1)
					{
						line.endToStart = dis(dataMap.get(0), dataMap.get(p.path.get(j)));
						line.spot_id = dataMap.get(p.path.get(j)).pointName;
						arriveTime += dis(dataMap.get(0), dataMap.get(p.path.get(j)));
						line.time = arriveTime; // ÿ����·���ܿ���
						endTime = arriveTime;
					}
				}
			}

			line.time = endTime - firstTime;
			lineList.add(line);
		}
		allTime += arriveTime;
		return (1 + arriveTime / 720);
	}

	// ���Ըĳ����ļ���׷������
	public void printRecord(Record record)
	{
		System.out.println(record.place_id + " " + record.arriveTime + " " + record.departureTime + " " + record.num);
	}

	public int dis(Point pointA, Point pointB) // ����֮��ľ���
	{
		int cost = 0;
		cost = (int) Math
				.round(2 * 6378137
						* Math.asin(
								Math.sqrt(
										Math.pow(
												Math.sin(
														Math.PI / 180.0 * (pointA.getLan() - pointB.getLan())
																/ 2),
												2) + Math.cos(Math.PI / 180.0 * pointA.getLan())
														* Math.cos(
																Math.PI / 180.0 * pointB.getLan())
														* Math.pow(Math.sin(Math.PI / 180.0
																* (pointA.getLon() - pointB.getLon()) / 2), 2)))
						/ 250);

		return cost;
	}

	// ���ƣ������ҵ�ÿһ��������fitness�ĸ���
	public void quickSort(ArrayList<Chomo> pop, int s, int e)
	{
		if (s < e)
		{
			int m = partition(pop, s, e);
			quickSort(pop, s, m - 1);
			quickSort(pop, m + 1, e);
		}
	}

	public int partition(ArrayList<Chomo> pop, int s, int e) // �Ե�һ��Ԫ����Ϊ�ָ�Ԫ��
	{
		int m = s;
		Chomo cho = pop.get(s);
		for (int i = s + 1; i <= e; i++)
		{
			if (pop.get(i).getFit() <= cho.getFit())
			{
				m++;
				Chomo chomo = pop.get(i);
				pop.set(i, pop.get(m));
				pop.set(m, chomo);
			}
		}

		Chomo chomo = pop.get(m);
		pop.set(m, pop.get(s));
		pop.set(s, chomo);

		return m;
	}

	public Map<Integer, Point> readData(String filename)
	{
		Map<Integer, Point> map = new HashMap<Integer, Point>();

		File file = new File(filename);
		BufferedReader reader = null;
		int i = 0; // �����idΪ0
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
				point.setPointId(i); // ��Ϊÿ�����͵�ֻ��һ������������ֱ��ӳ��id
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
		length = i - 1; // Ⱦɫ�峤��
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
		int i = 0; // �����idΪ0
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
				point.setPointId(i); // ��Ϊÿ�����͵�ֻ��һ������������ֱ��ӳ��id

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
