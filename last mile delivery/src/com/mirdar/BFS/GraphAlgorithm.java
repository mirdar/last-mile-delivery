package com.mirdar.BFS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import com.mirdar.CKAlgorithm.Dis;
import com.mirdar.CW.Represent;
import com.mirdar.GA.Point;
import com.mirdar.O2O.Order;
import com.mirdar.graph.SiteInfo;

public class GraphAlgorithm
{

	public static final int INFINITY = Integer.MAX_VALUE;
	public Map<String, Vertex> vertexMap = new HashMap<String, Vertex>();

	public void addEdge(String sourceName, String destName, int cost)
	{
		Vertex v = getVertex(sourceName);
		Vertex w = getVertex(destName);
		if (sourceName.substring(0, 1).equals("A"))
			v.flag = 1;
		if (destName.substring(0, 1).equals("A"))
			w.flag = 1;
		v.adj.add(new Edge(w, cost));
		// no direction edge
		// if (flag == 2)
		// w.adj.add(new Edge(v, cost));
	}

	public void addEdge(String sourceName, String destName, int cost, int tranformTime)
	{
		Vertex v = getVertex(sourceName);
		Vertex w = getVertex(destName);
		v.adj.add(new Edge(w, cost, tranformTime));
	}

	public void addEdge2(String sourceName, String destName, int cost, int flag)
	{
		Vertex v = getVertex(sourceName);
		Vertex w = getVertex(destName);
		if (sourceName.substring(0, 1).equals("A"))
			v.flag = 1;
		if (destName.substring(0, 1).equals("A"))
			w.flag = 1;
		v.adj.add(new Edge(w, cost));
		// no direction edge
		if (flag == 2)
			w.adj.add(new Edge(v, cost));
	}

	public Vertex printPath(String destName, Map<String, Represent> represents, com.mirdar.O2O.Line line)
	{
		Vertex w = vertexMap.get(destName);
		if (w == null)
			throw new NoSuchElementException();
		else if (w.dist == INFINITY)
			System.out.println(destName + "is unreachable");
		else
		{
			System.out.println("(转移时间  is: " + w.dist + ")");
			printPath(w, represents, line);
			System.out.println();
		}
		return w;
	}

	// 通过查询某个vertex打印出其cost与最短路径
	public Vertex printPath(String destName, Map<String, SiteInfo> siteInfo, Dis dis,
			ArrayList<ArrayList<String>> newLines, ArrayList<String> o2oLines)
	{
		Vertex w = vertexMap.get(destName);
		if (w == null)
			throw new NoSuchElementException();
		// else if (w.dist == INFINITY)
		// System.out.println(destName + "is unreachable");
		else
		{
			int time = 0;
			// System.out.println(w.name + ": ");
			for (int i = w.path.size() - 1; i >= 0; i--)
			{
				if (i % 2 == 0 && i != w.path.size() - 1)
					time += dis.disPlace(w.path.get(i), w.path.get(i + 1));
				// System.out.print(w.path.get(i) + ": " + time + " ");

				if (i % 2 == 0 && i != 0)
					for (int j = 0; j < siteInfo.get(w.path.get(i)).edges.size(); j++)
					{
						if (w.path.get(i - 1).equals(siteInfo.get(w.path.get(i)).edges.get(j).end.place_id))
						{
							time += siteInfo.get(w.path.get(i)).edges.get(j).lineTime;
							break;
						}
					}
				if (i % 2 != 0)
					vertexMap.get(w.path.get(i)).bindorNot = -1;
				o2oLines.add(w.path.get(i));
			}
			newLines.add(w.path);
			w.path = new ArrayList<String>();
			// System.out.print("-----");
		}
		return w;
	}

	// 通过查询某个vertex打印出其cost与最短路径
	public void printPath(String destName, Map<String, ArrayList<Point>> siteSpot, Dis dis, com.mirdar.O2O.Line line,
			Map<String, Point> spots, ArrayList<ArrayList<String>> newLines, ArrayList<String> o2oLines)
	{
		Vertex w = vertexMap.get(destName);
		if (w == null)
			throw new NoSuchElementException();
		// else if (w.dist == INFINITY)
		// System.out.println(destName + "is unreachable");
		else
		{
			int time = 0;
			// System.out.println(w.name + ": ");
			String siteName = null;
			for (int i = w.path.size() - 1; i >= 0; i--)
			{
				if (w.path.get(i).substring(0, 1).equals("A")) // site的转移时间
				{
					siteName = w.path.get(i);
					if (i == w.path.size() - 1)
					{
						System.out.print(w.path.get(i) + " : " + time + "  ");
					} else
					{
						time += dis.disPlace(siteName, w.path.get(i + 1));
						System.out.print(w.path.get(i) + " : " + time + "  ");
					}
				} else
				{
					if (i == 0)
					{
						time += dis.disPlace(w.path.get(i), w.path.get(i + 1));
						System.out.print(w.path.get(i) + " : " + time + "  ");
						if (time > line.line.get(0).arriveTime) // 到达shop的惩罚时间
							time += 5 * Math.abs(time - line.line.get(0).arriveTime);
						else
							time += Math.abs(time - line.line.get(0).arriveTime);
					} else
					{
						time += dis.disPlace(w.path.get(i), w.path.get(i + 1)); // spot直接的转移时间
						System.out.print(dis.disPlace(w.path.get(i), w.path.get(i + 1)) + "  ");
						System.out.print(w.path.get(i) + " : " + time + "  ");
						time += Math.round(3 * Math.sqrt(spots.get(w.path.get(i)).goods_num) + 5);
					}
				}
				if (w.path.get(i).substring(0, 1).equals("B")) // 访问过的spot不能再访问
					vertexMap.get(w.path.get(i)).bindorNot = -1;
				o2oLines.add(w.path.get(i));
			}
			newLines.add(w.path);
			w.path = new ArrayList<String>();
			// System.out.print("-----");
		}
	}

	public Vertex cleanVertex(String destName)
	{
		Vertex w = vertexMap.get(destName);
		return w;
	}

	public Vertex dijkstra(String startName, Map<String, Represent> represents, Dis dis, int t,
			ArrayList<String> bindLine, com.mirdar.O2O.Line line, ArrayList<com.mirdar.O2O.Line> lines,
			Map<String, Order> o2oOrder)
	{
		Vertex bestSpot = null;
		int maxTime = Integer.MAX_VALUE;
		// 优先级队列
		PriorityQueue<Path> pq = new PriorityQueue<Path>();

		Vertex start = vertexMap.get(startName);
		if (start == null)
			throw new NoSuchElementException("Start vertex not found.");
		clearAll();
		pq.add(new Path(start, 0));
		start.dist = 0;

		int nodesSeen = 0;
		while (!pq.isEmpty() && nodesSeen < vertexMap.size())
		{
			Path vrec = pq.remove();
			Vertex v = vrec.dest;
			if (v.scratch != 0)
				continue;
			v.scratch = 1;
			nodesSeen++;

			for (Edge e : v.adj)
			{
				if (e.dest.scratch != 0 || e.dest.bindorNot == -1
						|| (e.dest.name.substring(0, 1).equals("S") && !e.dest.name.equals(startName)))
					continue;
				Vertex w = e.dest;
				int cvw = e.tranformTime;
				if (cvw < 0)
					throw new GraphException("Graph has negative edges.");

				if (w.dist > v.dist + cvw)
				{

					if (!v.name.substring(0, 1).equals("S"))
					{
						int time = 0;
						int tranf = cvw + represents.get(w.name).lineTime + getTime(v, represents, dis);
						if (tranf < t)
							time += v.dist + cvw + t - tranf;
						else
						{
							time += v.dist + cvw + getO2oPunish(line, Math.abs(t - tranf), o2oOrder);

						}
						if (time < maxTime) // 找到转移时间最小的
						{
							maxTime = time;
							bestSpot = w;
							w.dist = v.dist + cvw;
							w.prev = v;
							pq.add(new Path(w, w.cost, w.dist));
						}
					} else
					{
						w.dist = v.dist + cvw;
						w.prev = v;
						pq.add(new Path(w, w.cost, w.dist));
					}

				}
			}
		}
		System.out.println("pickUpTime: " + line.line.get(0).arriveTime);
		System.out.println("路程的惩罚时间: " + maxTime);
		if (maxTime > line.line.get(0).arriveTime)
		{
			System.out.println(maxTime + "----------------------------------------");
			// System.out.println("getTime: " + getTime(bestSpot, represents,
			// dis));
			lines.add(line);
			// System.out.println("shop_id: " + line.shop_id);
			bestSpot = null;
		} else
		{
			bindFlag(bestSpot, bindLine);
			// System.out.println("getTime: "+getTime(bestSpot, represents,
			// dis));
		}

		return bestSpot;
	}

	// 这里惩罚的逻辑错误
	public int getO2oPunish(com.mirdar.O2O.Line line, int later, Map<String, Order> o2oOrder)
	{
		int time = 0;
		for (int i = 0; i < line.line.size(); i++)
		{
			if (line.line.get(i).place_id.substring(0, 1).equals("S")
					&& line.line.get(i).arriveTime + later > line.line.get(i).departureTime)
			{
				time += 5 * Math.abs(line.line.get(i).arriveTime + later - line.line.get(i).departureTime);
			}
		}

		return time;
	}

	public void bindFlag(Vertex dest, ArrayList<String> bindLine)
	{
		while (!dest.name.substring(0, 1).equals("S"))
		{
			dest.bindorNot = -1;
			bindLine.add(dest.name);
			dest = dest.prev;
		}
	}

	// 计算路线的时间
	public int getTime(Vertex dest, Map<String, Represent> represents, Dis dis)
	{
		int time = 0;

		while (!dest.name.substring(0, 1).equals("S"))
		{
			time += represents.get(dest.name).lineTime;
			if (dest.prev.name.substring(0, 1).equals("S"))
			{
				time += dis.disPlace(dest.name, dest.prev.name);
			} else
			{
				time += dis.disPlace(dest.name, represents.get(dest.prev.name).siteName);
			}
			dest = dest.prev;
		}

		return time;
	}

	public int BFS(String startName, String endName, int t0, int minTime)
	{
		PriorityQueue<Path> pq = new PriorityQueue<Path>();
		Vertex start = vertexMap.get(startName);
		if (start == null)
			throw new NoSuchElementException("Start vertex not found.");
		clearAll();
		pq.add(new Path(start, 0));
		start.dist = 0;
		if (startName.equals(endName))
		{
			return 0;
		}

		while (!pq.isEmpty())
		{
			Path vrec = pq.remove();
			Vertex v = vrec.dest;
			// 界定
			if (vrec.cost >= t0 + 10)
				break;
			if (v.scratch != 0 && v.flag == 0)
				continue;
			v.scratch = 1;
			// 分支
			for (Edge e : v.adj)
			{
				if (v.dist + e.cost > t0 + 10 || e.dest.scratch != 0 && v.flag == 0 && !e.dest.name.equals(endName)
						|| e.dest.bindorNot == -1)
					continue;
				Vertex w = e.dest;
				int cvw = e.cost;

				if (cvw < 0)
					throw new GraphException("Graph has negative edges.");

				if (!w.name.equals(endName))
				{
					w.dist = v.dist + cvw;
					w.prev = v;
					if (w.name.substring(0, 1).equals("A")) // 只记录spot ->
															// site空驶时候的开销
						w.transformDis = v.transformDis + (int) cvw;
					else
						w.transformDis = v.transformDis;
					pq.add(new Path(w, w.dist));
				} else
				{
					// if (w.transformDis == 0)
					// w.transformDis = Integer.MAX_VALUE;
					int time = 0;
					if (v.dist + cvw < t0)
					{
						time = (int) (v.transformDis + t0 - v.dist);
					} else
					{
						time = (int) (v.transformDis + cvw + 5 * Math.abs(t0 - v.dist - cvw));
					}
					if (time < minTime)
					{
						minTime = time;
						w.prev = v;
						addPath(w);
						w.arriveTime = (int) (v.dist + cvw);
					}
				}

			}
		}

		return minTime;
	}

	public int BFS2(String startName, String endName, int t0, int minTime, Map<String, Point> spots)
	{
		PriorityQueue<Path> pq = new PriorityQueue<Path>();
		Vertex start = vertexMap.get(startName);
		if (start == null)
			throw new NoSuchElementException("Start vertex not found.");
		clearAll();
		pq.add(new Path(start, 0));
		start.dist = 0;
		if (startName.equals(endName))
		{
			return 0;
		}

		while (!pq.isEmpty())
		{
			Path vrec = pq.remove();
			Vertex v = vrec.dest;
			// 界定
			if (vrec.cost >= t0 + 10)
				break;
			if (v.scratch != 0 && v.flag == 0)
				continue;
			v.scratch = 1;
			// 分支
			// System.out.println(v.name+" : "+v.dist);
			for (Edge e : v.adj)
			{
				if (v.dist + e.cost > t0 + 10 || e.dest.scratch != 0 && v.flag == 0 || e.dest.bindorNot == -1
						|| e.dest.name.substring(0, 1).equals("S") && !e.dest.name.equals(endName))
					continue;
				Vertex w = e.dest;
				int cvw = e.cost;

				if (cvw < 0)
					throw new GraphException("Graph has negative edges.");

				if (!w.name.equals(endName))
				{
					if (w.name.substring(0, 1).equals("B") && getWeight2(v, spots) + spots.get(w.name).goods_num > 140)
						continue;
					w.dist = v.dist + cvw;
					w.prev = v;
					if (w.name.substring(0, 1).equals("A")) // 只记录spot ->
															// site空驶时候的开销
						w.transformDis = v.transformDis + (int) cvw;
					else
					{
						w.transformDis = v.transformDis + (int) cvw;
						w.dist += Math.round(3 * Math.sqrt(spots.get(w.name).goods_num) + 5);
					}
					// if(v.name.equals(w.name))
					// System.out.println(w.name+" : "+w.dist);
					pq.add(new Path(w, w.dist));
				} else
				{
					// if (w.transformDis == 0)
					// w.transformDis = Integer.MAX_VALUE;
					int time = 0;
					if (v.dist + cvw < t0)
					{
						time = (int) (v.transformDis + t0 - v.dist);
					} else
					{
						time = (int) (v.transformDis + cvw + 5 * Math.abs(t0 - v.dist - cvw));
					}
					if (time < minTime && getWeight(v, spots) == 0)
					{
						minTime = time;
						w.prev = v;
						addPath(w);
						w.arriveTime = (int) (v.dist + cvw);
						// System.out.println("minTime: "+minTime);
					}
				}

			}
			// System.out.println();
		}

		return minTime;
	}

	// 每个时刻载重不超过140
	private int getWeight2(Vertex dest, Map<String, Point> spots)
	{
		int weight = 0;

		while (dest != null && !dest.name.substring(0, 1).equals("A"))
		{
			weight += spots.get(dest.name).goods_num;
			dest = dest.prev;
		}

		return weight;
	}

	// 检查每个时刻载重不超过140
	private int getWeight(Vertex dest, Map<String, Point> spots)
	{
		int weight = 0;

		while (dest != null)
		{
			if (dest.name.substring(0, 1).equals("B"))
				weight += spots.get(dest.name).goods_num;
			else
			{
				if (weight > 140)
					return 1;
				weight = 0;
			}
			dest = dest.prev;
		}

		return 0;
	}

	private Vertex getVertex(String vertexName)
	{
		Vertex v = vertexMap.get(vertexName);
		if (v == null)
		{
			v = new Vertex(vertexName);
			vertexMap.put(vertexName, v);
		}

		return v;
	}

	// 递归打印最短路径
	private void printPath(Vertex dest, Map<String, Represent> represents, com.mirdar.O2O.Line line)
	{
		if (dest.prev != null)
		{
			printPath(dest.prev, represents, line);
			System.out.print(" to ");
		}
		if (!dest.name.substring(0, 1).equals("S"))
			System.out.print(dest.name + " : " + represents.get(dest.name).lineTime);
		else
			System.out.print(dest.name + " : " + line.line.get(0).arriveTime);
	}

	public void addPath(Vertex shop)
	{
		shop.path = new ArrayList<String>();
		addPath(shop, shop);
	}

	private void addPath(Vertex dest, Vertex shop)
	{
		shop.path.add(dest.name);
		if (dest.prev != null)
		{
			addPath(dest.prev, shop);
		}
	}
	/*
	 * printPath的非递归版本 private void printPath(Vertex dest) { while(dest.prev !=
	 * null) { System.out.println(dest.name); dest =dest.prev;
	 * System.out.println(" <- "); }
	 * 
	 * }
	 * 
	 */

	// initializes operation
	private void clearAll()
	{
		for (Vertex v : vertexMap.values())
		{
			v.reset();
		}
	}

	@SuppressWarnings("serial")
	class GraphException extends RuntimeException
	{
		public GraphException(String name)
		{
			super(name);
		}
	}
}
