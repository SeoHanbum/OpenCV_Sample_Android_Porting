@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		image = inputFrame.rgba();
		mGray = inputFrame.gray();

		Imgproc.pyrDown(mGray, mGray);
		Imgproc.pyrDown(image, image);

		if (addRemovePt) {
			Imgproc.goodFeaturesToTrack(mGray, mp, MAX_COUNT, 0.01, 5);

			mp2f = new MatOfPoint2f(mp.toArray());

			Imgproc.cornerSubPix(mGray, mp2f, new Size(10, 10),
					new Size(-1, -1), termcrit);
			List<Point> lp = mp2f.toList();

			addRemovePt = false;
			needToInit = true;

			points[1].fromList(lp);
		} else if (!points[0].empty()) {
			MatOfByte status = new MatOfByte();
			MatOfFloat err = new MatOfFloat();

			/*
			 * Video.calcOpticalFlowPyrLK(mGray,prevImg, points[0], points[1],
			 * status, err, new Size(53,53), 3, termcrit, 0, 0.001);
			 */

			// Video.calcOpticalFlowPyrLK(err, nextImg, prevPts, nextPts,
			// status, err, winSize, maxLevel, criteria, flags,
			// minEigThreshold);
			Video.calcOpticalFlowPyrLK(prevImg, mGray, points[0], points[1],
					status, err);
			// Video.calcOpticalFlowPyrLK(err, nextImg, prevPts, nextPts,
			// status, err, winSize, maxLevel);

			List<Point> lp = points[1].toList();
			List<Byte> ls = status.toList();

			int i, k;

			for (i = k = 0; i < lp.size(); i++) {
				if (ls.get(i) == 0)
					continue;
				// Core.line(image, lp0.get(i), lp1.get(i), new
				// Scalar(255,0,0));
				Core.circle(image, lp.get(i), 3, new Scalar(255, 255, 0), -1,
						8, 0);
			}
		}

		prevImg = mGray;
		points[0] = pointSwap(points[1], points[1] = points[0]);
		Imgproc.pyrUp(image, image);

		return image;
	}
