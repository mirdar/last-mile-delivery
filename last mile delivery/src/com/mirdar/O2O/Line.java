package com.mirdar.O2O;

import java.util.ArrayList;

import com.mirdar.graph.Edge;

public class Line {

	public ArrayList<Record> line = new ArrayList<Record>();
	public int earliestTime;
	public int lastTime; // o2o·���������ʲôʱ�򵽣����ﲻ�ԣ����������ʹ��
	public int arriveTime; // ���Աʲôʱ�򵽴�
	public ArrayList<com.mirdar.GA.Line> branchToO2oLine = new ArrayList<com.mirdar.GA.Line>(); // ���Ա�ĵ���·��
	public ArrayList<Edge> edgeLine = new ArrayList<Edge>();
	public String shop_id; // ��ʼ��������
	public String spot_id; // ��¼���̻����Ա����·�ߵ��յ�
}
