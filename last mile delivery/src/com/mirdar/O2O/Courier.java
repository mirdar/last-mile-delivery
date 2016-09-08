package com.mirdar.O2O;

import java.util.ArrayList;

public class Courier {

	public int current_time;
	public int last_stay_time;
	public String current_place;
	public ArrayList<String> list = new ArrayList<String>(); //用来记录走过那些订单
}
