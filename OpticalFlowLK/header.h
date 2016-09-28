#include "stdafx.h"

#pragma once
class Ixy {
private:
	Mat m_img;

public:
	vector<vector<int> >* m_Data;
	int m_width, m_heigt;
	int m_type;

	Ixy(const Mat& img);

	Mat toImage(int index);
};

class It {
	Mat m_img;
public:
	vector<vector<int> > m_Data;
	int m_width, m_heigt;
	int m_type;

	It(const Mat & f1, const Mat & f2);

	Mat toImage();
};

class Matrix {
	//int m_dw, m_dh;
public:
	//Matrix(int dw,int dh) : m_dh(dh),m_dw(dw){}
	double invMatATA[4];
	long long matATB[2];

	void Matrix::calcInvATA(const Ixy& ixy);
	void Matrix::calcMat(const It& it, const Ixy& ixy);
	Point2f calcD();
};

Mat gausPyramid(const Mat& src);
bool operator>(const Point2f& l, const Point2f& r);
Point2f OpticalFlowLK(const Mat& f1, const Mat& f2);
