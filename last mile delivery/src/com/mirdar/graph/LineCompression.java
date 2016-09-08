package com.mirdar.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mirdar.GA.Line;
import com.mirdar.GA.Record;
import com.mirdar.test.Place;

public class LineCompression {

	public Map<String,SiteInfo> lineCompression(Map<String,ArrayList<Line>> lines,Map<String, Place> placeMap)
	{
		Map<String,SiteInfo> siteInfo = new HashMap<String,SiteInfo>();
		for(String key : lines.keySet())
		{
			SiteInfo site = new SiteInfo();
			site.siteName = placeMap.get(key);
			siteInfo.put(key, site);
			for(int i=0;i<lines.get(key).size();i++)
			{
				ArrayList<Record> line = lines.get(key).get(i).line;
				Edge e = new Edge();
//				System.out.println(line.get(0).place_id);
				e.start = placeMap.get(line.get(0).place_id);
				e.end = placeMap.get(line.get(line.size()-1).place_id);
				e.lineTime = lines.get(key).get(i).time;
				e.lastArriveTime = line.get(line.size()-1).arriveTime;
				e.line = line;
				ArrayList<String> list = findNearSite(e.end, lines, placeMap);
				e.nearSite = placeMap.get(list.get(0));
				e.dis = Integer.parseInt(list.get(1));
				site.edges.add(e);
			}
		}
		
		return siteInfo;
	}
	//找到最近的网点
	public ArrayList<String> findNearSite(Place end,Map<String,ArrayList<Line>> lines,Map<String, Place> placeMap)
	{
		ArrayList<String> list = new ArrayList<String>();
		String nearSite = null;
		int minDis = Integer.MAX_VALUE;
		for(String key : lines.keySet())
		{
			if(disPlace(end.place_id, key, placeMap) < minDis)
			{
				nearSite = key;
				minDis = disPlace(end.place_id, key, placeMap);
			}
		}
		
		list.add(nearSite);
		list.add(Integer.toString(minDis));
		
		return list;
	}
	
	public int disPlace(String name1,String name2,Map<String, Place> placeMap) //两点之间的距离
	{
		int cost = 0;
		cost = (int)(Math.round( 2*6378137*Math.asin(Math.sqrt(Math.pow(Math.sin(Math.PI/180.0*(placeMap.get(name1).lan-placeMap.get(name2).lan)/2),2)+
				Math.cos(Math.PI/180.0*placeMap.get(name1).lan)*Math.cos(Math.PI/180.0*placeMap.get(name2).lan)*
				Math.pow(Math.sin(Math.PI/180.0*(placeMap.get(name1).lon-placeMap.get(name2).lon)/2),2)))/250));
		
		return cost;
	}
	
	public void printSiteInfo(Map<String,SiteInfo> siteInfo)
	{
		for(String key : siteInfo.keySet())
		{
			System.out.println("key: "+key+" siteInfo."+siteInfo.get(key).edges.size());
			System.out.println("nearSize: "+siteInfo.get(key).edges.get(0).nearSite.place_id);
		}
	}
}
