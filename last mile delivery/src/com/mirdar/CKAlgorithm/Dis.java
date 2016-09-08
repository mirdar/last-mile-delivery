package com.mirdar.CKAlgorithm;

import java.util.HashMap;
import java.util.Map;

import com.mirdar.test.Place;

public class Dis
{
	public Map<String, Place> placeMap = new HashMap<String, Place>();

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
