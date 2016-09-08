package com.mirdar.O2O;

import java.util.ArrayList;

public class Order {

	public String shop_id;
	public String spot_id;
	public String order_id;
	public int pickup_time;
	public int delivery_time;
	public int num;
	public int time;
	public int stay_time;
	public int last_time;
	public int flag = 1; //用来记录是否被配送过，1表示还未配送，而0表示已经被配送
	public ArrayList<String> list = new ArrayList<String>();
}
