float stt_dx_2[] = { 0,5,0,5 };
float end_dx_2[] = { 5,10,5,10 };
float stt_dy_2[] = { 0,0,5,5 };
float end_dy_2[] = { 5,5,10,10 };

float jump_dx[] = { 0,10,0,10 };
float jump_dy[] = { 0,0,10,10 };

//#define showLayer1_2

UINT CMOD_OpenCVDlg::GrabLoop(void) {
	Scalar color = Scalar(255);

	CString temp;

	Mat f1, f2;

	CDC* pDC;
	CRect rect;
	IplImage*   m_pImage = NULL;
	CvvImage    m_cImage;

#ifdef SAMPLE_IMG
	f1 = imread("f1.bmp");
	f2 = imread("f2.bmp");

	cvtColor(f1, f1, CV_RGB2GRAY);
#else
	while (1) {
		if (this->cam.isOpened()) {
			this->cam >> f2;
#endif // SAMPLE_IMG

			cvtColor(f2, f2, CV_RGB2GRAY);

			if (!f1.empty()) {
				vector<vector<Point2f> > preUVs(f2.size().height,vector<Point2f>(f2.size().width));

				Mat f1_layer3, f2_layer3;
				Mat f1_layer2, f2_layer2;
				Mat myLKMat;

				int height, width;

				f1_layer2 = gausPyramid(f1);
				f2_layer2 = gausPyramid(f2);

				f1_layer3 = gausPyramid(f1_layer2);
				f2_layer3 = gausPyramid(f2_layer2);

				height = f2_layer3.size().height;
				width = f2_layer3.size().width;
#ifdef showLayer1_2
				f2_layer3.copyTo(myLKMat);
#endif // showLayer1_2

				for (int i = 0; i < height; i += 5)
					for (int j = 0; j < width; j += 5) {
						Point2f& nowUV = preUVs[i * 4][j * 4];

						Range yRange = Range(i, i + 5);
						Range xRange = Range(j, j + 5);

						nowUV = OpticalFlowLK(f1_layer3(yRange, xRange), f2_layer3(yRange, xRange));
						int lengD = norm(nowUV);

						if (lengD > 5 || lengD < 1) {
							nowUV = Point2f(0, 0);
							continue;
						}
#ifdef showLayer1_2
						Point2f here = Point2f(j + 2.5f, i + 2.5f);
						line(myLKMat, here, here + nowUV, color);
					}
				pDC = this->layer1.GetDC(); // picture control의 DC얻어옴
				this->layer1.GetClientRect(&rect); //picture control의 크기알아내기
				m_cImage.CopyOf(&IplImage(myLKMat)); //IPL이미지-> CVV이미지 
				m_cImage.DrawToHDC(pDC->m_hDC, rect);//CVV이미지를 DC에 그림
#else
			}
#endif // showLayer1_2

				height = f2_layer2.size().height;
				width = f2_layer2.size().width;

#ifdef showLayer1_2
				f2_layer2.copyTo(myLKMat);
#endif // showLayer1_2

				for (int i = 0; i < height; i += 10)
					for (int j = 0; j < width; j += 10) {
						const Point2f& preUV = preUVs[i*2][j*2] * 2;

						for (int k = 0; k < 4; k++) {
							int sttY = i + stt_dy_2[k];
							int endY = i + end_dy_2[k];
							int sttX = j + stt_dx_2[k];
							int endX = j + end_dx_2[k];

							int moved_sttY = sttY;
							int moved_endY = endY;
							int moved_sttX = sttX;
							int moved_endX = endX;

							if (sttY + preUV.y > 0 && endY + preUV.y < height) {
								moved_sttY += preUV.y;
								moved_endY += preUV.y;
							}
							if (sttX + preUV.x > 0 && endX + preUV.x < height) {
								moved_sttX += preUV.x;
								moved_endX += preUV.x;
							}

							int x = j + (stt_dx_2[k] + end_dx_2[k]) / 2;
							int y = i + (stt_dy_2[k] + end_dy_2[k]) / 2;

							Point2f& nowUV = preUVs[y * 2][x * 2];

							nowUV = OpticalFlowLK(f1_layer2(Range(sttY, endY), Range(sttX, endX)), f2_layer2(Range(moved_sttY, moved_endY), Range(moved_sttX, moved_endX))) + preUV;

							int lengD = norm(nowUV);

							if (lengD > 10 || lengD < 1) {
								nowUV = Point2f(0, 0);
								continue;
							}

#ifdef showLayer1_2
							Point2f here = Point2f(x,y);
							line(myLKMat, here, here + nowUV, color);
						}
					}

				pDC = this->layer2.GetDC(); // picture control의 DC얻어옴
				this->layer2.GetClientRect(&rect); //picture control의 크기알아내기
				m_cImage.CopyOf(&IplImage(myLKMat)); //IPL이미지-> CVV이미지 
				m_cImage.DrawToHDC(pDC->m_hDC, rect);//CVV이미지를 DC에 그림
#else
		}
	}
#endif // showLayer1_2
				f2.copyTo(myLKMat);

				height = f2.size().height;
				width = f2.size().width;


				for (int i = 0; i < height; i += 20)
					for (int j = 0; j < width; j += 20) {

						for (int t = 0; t < 4; t++) {
							const Point2f& preUV = preUVs[i][j]*2;

							for (int k = 0; k < 4; k++) {
								int sttY = i + stt_dy_2[k] + jump_dy[t];
								int endY = i + end_dy_2[k] + jump_dy[t];
								int sttX = j + stt_dx_2[k] + jump_dx[t];
								int endX = j + end_dx_2[k] + jump_dx[t];

								int moved_sttY = sttY;
								int moved_endY = endY;
								int moved_sttX = sttX;
								int moved_endX = endX;

								if (sttY + preUV.y > 0 && endY + preUV.y < height) {
									moved_sttY += preUV.y;
									moved_endY += preUV.y;
								}
								if (sttX + preUV.x > 0 && endX + preUV.x < height) {
									moved_sttX += preUV.x;
									moved_endX += preUV.x;
								}

								Point2f nowUV = OpticalFlowLK(f1(Range(sttY, endY), Range(sttX, endX)), f2(Range(moved_sttY, moved_endY), Range(moved_sttX, moved_endX))) + preUV;

								int lengD = norm(nowUV);

								if (lengD >= 20 || lengD < 1) continue;

								Point2f here = Point2f(j + (stt_dx_2[k] + end_dx_2[k] + 2*jump_dx[t]) / 2, i + (stt_dy_2[k] + end_dy_2[k] + 2 * jump_dy[t]) / 2);

								line(myLKMat, here, here + nowUV, color);
							}
						}
					}

				pDC = this->layer3.GetDC(); // picture control의 DC얻어옴
				this->layer3.GetClientRect(&rect); //picture control의 크기알아내기
				m_cImage.CopyOf(&IplImage(myLKMat)); //IPL이미지-> CVV이미지
				m_cImage.DrawToHDC(pDC->m_hDC, rect);//CVV이미지를 DC에 그림

				ReleaseDC(pDC);//가져온DC해제

				f1_layer2.release();
				f2_layer2.release();
				f1_layer3.release();
				f2_layer3.release();
				f1.release();

#ifndef SAMPLE_IMG
}
#endif
			}
#ifndef SAMPLE_IMG
		f2.copyTo(f1);
		f2.release();
		Sleep(5);
	}
	this->cam.release();
#endif

	return 0;
}
