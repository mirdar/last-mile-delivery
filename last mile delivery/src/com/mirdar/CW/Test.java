package com.mirdar.CW;

import java.util.ArrayList;
import java.util.Random;

public class Test
{
	public static void main(String[] args)
	{
		Integer i = 0;
		Test test = new Test();
		test.test1(i);
		System.out.println(i);
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.add(1);
		System.out.println("remove: " + temp.remove(0));
		System.out.println(new Random().nextInt(3));
		String file = "\"F/sfs/"+(1+3)+"sdfs\"";
		System.out.println(file);
	}

	public void test1(Integer i)
	{
		i += 10;
	}
}
