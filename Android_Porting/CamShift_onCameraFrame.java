	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		image = inputFrame.rgba();
		Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);

		if (trackObject != 0) {
			int vmin = 10, vmax = 256, smin = 30;

			Core.inRange(hsv, new Scalar(0, smin, Math.min(vmin, vmax)),
					new Scalar(180, 256, Math.max(vmin, vmax)), mask);

			hue.create(hsv.size(), hsv.depth());

			List<Mat> hueList = new LinkedList<Mat>();
			List<Mat> hsvList = new LinkedList<Mat>();
			hsvList.add(hsv);
			hueList.add(hue);

			MatOfInt ch = new MatOfInt(0,0);

			Core.mixChannels(hsvList, hueList, ch);

			MatOfFloat histRange = new MatOfFloat(0, 180);
			
			if (trackObject < 0) {

				Mat subHue = hue.submat(selection);

				Imgproc.calcHist(Arrays.asList(subHue), new MatOfInt(0),
						new Mat(), hist, new MatOfInt(16), histRange);
				Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);
				trackWindow = selection;
				trackObject = 1;
			}
			
			MatOfInt ch2 = new MatOfInt(0, 1);
			Imgproc.calcBackProject(Arrays.asList(hue), ch2, hist, backproj,
					histRange, 1);

			Core.bitwise_and(backproj, mask, backproj);

			RotatedRect trackBox = Video.CamShift(backproj, trackWindow,
					new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 10,1));
			
			Core.ellipse(image, trackBox, new Scalar(0,0,255),4);
			
			if (trackWindow.area() <= 1) {
				trackObject = 0;
			}
		}

		return image;
	}
