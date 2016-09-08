package com.mirdar.O2O;

import java.util.ArrayList;

public class test {

	public static void main(String[] args)
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		System.out.println(list.get(1));
		list.remove(1);
		System.out.println(list.get(1));
	}
}
