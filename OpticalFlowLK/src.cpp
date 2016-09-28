#include "stdafx.h"
#include "OptcFlMath.h"

BYTE colorToByte(COLORREF* ref) {
	return (GetRValue(*ref) * 30 + GetGValue(*ref) * 59 + GetBValue(*ref) * 11) / 100;
}

It::It(const Mat& f1, const Mat& f2) {
	//TODO : f1과 f2사이즈가 다르면 에러!

	m_width = f1.size().width;
	m_heigt = f1.size().height;

	f1.copyTo(m_img);

	this->m_Data.assign(m_heigt, vector<int>(m_width, 0));

	for (int i = 0; i < m_heigt; i++)
		for (int j = 0; j < m_width; j++) {
			m_Data[i][j] = f2.at<UCHAR>(i, j) - f1.at<UCHAR>(i, j);
			m_img.at<uchar>(i, j) = abs(m_Data[i][j]);
		}
}

Mat It::toImage() {
	return m_img;
}

Mat Ixy::toImage(int index) {
	vector<vector<int> >& obj = m_Data[index];

	m_img = Mat(m_heigt, m_width, m_type);

	for (int i = 0; i < m_heigt; i++)
		for (int j = 0; j < m_width; j++) {
			m_img.at<UCHAR>(i, j) = abs(obj[i][j]);
		}

	return m_img;
}

Ixy::Ixy(const Mat& img) {
	m_Data = new vector<vector<int> >[2];

	m_width = img.size().width;
	m_heigt = img.size().height;
	m_type = img.type();

	m_Data[0].assign(m_heigt, vector<int>(m_width, 0));
	m_Data[1].assign(m_heigt, vector<int>(m_width, 0));

	for (int i = 1; i < m_heigt - 1; i++) {
		for (int j = 1; j < m_width - 1; j++) {
			int& ix = m_Data[0][i][j];
			int& iy = m_Data[1][i][j];

			for (int d = -1; d < 2; d++) {
				ix = img.at<uchar>(i + d, j + 1) - img.at<uchar>(i + d, j - 1);
				iy = img.at<uchar>(i + 1, j + d) - img.at<uchar>(i - 1, j + d);
			}
		}
	}
}


bool isRange(const int &w, const int &h, int x, int y) {
	return x >= 0 && x < w && y >= 0 && y < h;
}

void Matrix::calcInvATA(const Ixy& ixy) {
	long long Mat[4] = { 0, 0, 0, 0 };
	invMatATA[0] = 0.0;
	invMatATA[1] = 0.0;
	invMatATA[2] = 0.0;
	invMatATA[3] = 0.0;

	for (int i = 0; i < ixy.m_heigt; i++)
		for (int j = 0; j < ixy.m_width; j++) {
			Mat[0] += pow(ixy.m_Data[0][i][j], 2);
			Mat[3] += pow(ixy.m_Data[1][i][j], 2);

			long long temp = ixy.m_Data[0][i][j] * ixy.m_Data[1][i][j];

			Mat[1] += temp;
			Mat[2] += temp;
		}

	long long t = (Mat[0] * Mat[3] - Mat[1] * Mat[2]);

	this->invMatATA[0] = Mat[3];
	this->invMatATA[1] = -Mat[1];
	this->invMatATA[2] = -Mat[2];
	this->invMatATA[3] = Mat[0];

	if (invMatATA[0] == 0 || invMatATA[3] == 0) return;

	for (auto& it : this->invMatATA)
		it *= 1.0 / t;
}

void Matrix::calcMat(const It& it, const Ixy& ixy) {
	matATB[0] = matATB[1] = 0;

	for (int i = 0; i < it.m_heigt; i++)
		for (int j = 0; j < it.m_width; j++) {
			matATB[0] += it.m_Data[i][j] * ixy.m_Data[0][i][j];
			matATB[1] += it.m_Data[i][j] * ixy.m_Data[1][i][j];
		}

	matATB[0] *= -1;
	matATB[1] *= -1;
}

Point2f Matrix::calcD() {
	double a = invMatATA[0] * matATB[0] + invMatATA[1] * matATB[1];
	double b = invMatATA[2] * matATB[0] + invMatATA[3] * matATB[1];

	bool isNanA = isnan(a);
	bool isNaNB = isnan(b);

	if (isNanA && isNaNB)
		return Point2f(0, 0);

	if (isNanA || isNaNB) {
		if(isNanA)
			return Point2f(0, b);
		return Point2f(a, 0);
	}

	return Point2f(a, b);
}

Mat gausPyramid(const Mat& src) {
	int wid = src.size().width / 2;
	int hgt = src.size().height / 2;

	Mat grayImage = Mat(hgt, wid, src.type());

	for (int x = 0; x < wid; x++)
		for (int y = 0; y < hgt; y++) {

			int pixel = 0;

			int sttX = 2 * x;
			int endX = 2 * x + 1;

			int sttY = 2 * y;
			int endY = 2 * y + 1;

			for (int i = sttX; i <= endX; i++)
				for (int j = sttY; j <= endY; j++)
					pixel += src.at<uchar>(j, i);

			pixel /= 4.0;

			grayImage.at<uchar>(y, x) = pixel;
		}

	return grayImage;
}

Point2f OpticalFlowLK(const Mat& f1, const Mat& f2) {
	It it = It(f1, f2);
	Ixy ixy = Ixy(f2);
	Matrix mat;

	mat.calcInvATA(ixy);
	mat.calcMat(it, ixy);

	Point2f resUV = mat.calcD();

	return resUV;
}
