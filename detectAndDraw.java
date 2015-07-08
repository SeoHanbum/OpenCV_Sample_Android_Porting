public Mat detectAndDraw(    		
			Mat src,
			CascadeClassifier cascade,
	  	CascadeClassifier nestedCascade,
			double scale, boolean tryflip) {

		int i = 0;
		double t = 0;

		List<Rect> faces = new ArrayList<Rect>(); 
		MatOfRect faces2 = new MatOfRect();

		ArrayList<Scalar> colors = new ArrayList<Scalar>();
		colors.add(new Scalar(00,0,255));
		colors.add(new Scalar(0,128,255));
		colors.add(new Scalar(0,255,255));
		colors.add(new Scalar(0,255,0));
		colors.add(new Scalar(255,128,0));
		colors.add(new Scalar(255,255,0));
		colors.add(new Scalar(255,0,0));
		colors.add(new Scalar(255,0,255));    	

		Mat gray = new Mat();

		Mat smallImg = new Mat(
				(int)Math.round(src.rows()/scale),
				(int)Math.round(src.cols()/scale), CvType.CV_8UC1 
				);

		Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGRA2GRAY);
		Imgproc.resize(gray, smallImg, smallImg.size(),0,0,Imgproc.INTER_LINEAR);
		Imgproc.equalizeHist(smallImg, smallImg);

		MatOfRect mr = new MatOfRect();
		mr.fromList(faces);

		cascade.detectMultiScale(smallImg, mr, 
				1.1, 2, 0,new Size(30, 30), new Size());

		if(tryflip){
			Core.flip(smallImg, smallImg, 1);
			cascade.detectMultiScale(smallImg, faces2,
					1.1, 2, 0,new Size(30, 30), new Size());

			Rect[] lr = faces2.toArray();

			for(Rect r : lr){
				faces.add(new Rect(
						smallImg.cols()-r.x-r.width,
						r.y,r.width,r.height)
						);
			}
		}

		for(Rect r : faces)
		{
			i++;
			MatOfRect nestedObjects = new MatOfRect();
			Point center = new Point();

			Scalar color = colors.get(i%8);
			int radius;

			double aspect_ratio = (double)r.width/r.height;

			if( 0.75 < aspect_ratio && aspect_ratio < 1.3 )
			{
				//cvRound -> (int)Math.round
				center.x = (int)Math.round((r.x + r.width*0.5)*scale);
				center.y = (int)Math.round(Math.round((r.y + r.height*0.5)*scale));
				radius = (int)Math.round((r.width + r.height)*0.25*scale);
				Core.circle( src, center, radius, color, 3, 8, 0 );
			}
			else
				Core.rectangle(src,
						new Point(
								(int)Math.round(r.x*scale), 
								(int)Math.round(r.y*scale)
								),
								new Point(
										(int)Math.round((r.x + r.width-1)*scale),
										(int)Math.round((r.y + r.height-1)*scale)
										),
										color, 3, 8, 0);

			if( nestedCascade.empty() )
				continue;

			//smallImgROI = smallImg(*r);
			Mat smallImgROI = gray.submat(r);
			nestedCascade.detectMultiScale(
					smallImgROI, 
					nestedObjects, 
					1.1, 2, 0,
					new Size(30, 30), new Size());


			for(Rect nr : nestedObjects.toList())
			{
				center.x = (int)Math.round((r.x + nr.x + nr.width*0.5)*scale);
				center.y = (int)Math.round((r.y + nr.y + nr.height*0.5)*scale);
				radius = (int)Math.round((nr.width + nr.height)*0.25*scale);
				Core.circle( src, center, radius, color, 3, 8, 0 );
			}
		}

		return src;
	}
