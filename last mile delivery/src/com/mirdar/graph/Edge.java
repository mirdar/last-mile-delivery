package com.mirdar.graph;

import java.util.ArrayList;

import com.mirdar.GA.Record;
import com.mirdar.test.Place;

public class Edge
{

	// ����յ����Ϣ
	public Place start;
	public Place end;
	public int lineTime; // ·�ߵĳ���
	public int flag = 0; // Ϊ0��û�б�����1�ͱ�������
	public int lastArriveTime = 0;
	public ArrayList<Record> line; // ��������������·��

	public Place nearSite; // ���������
	public int dis; // ����·�������������ľ���
}
