package com.mirdar.GA;

import java.util.ArrayList;

public class Chomo {

	public int[] chomo;
	public double fit;
	public ArrayList<Path> path = new ArrayList<Path>();
	public int[] getChomo()
	{
		return chomo;
	}
	public void setChomo(int[] chomo)
	{
		this.chomo = chomo;
	}
	public double getFit()
	{
		return fit;
	}
	public void setFit(double fit)
	{
		this.fit = fit;
	}
}
