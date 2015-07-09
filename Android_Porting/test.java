	public Mat detectAndDraw2(    		
			Mat mRgba,
			Mat mGray) {

		Imgproc.equalizeHist(mGray, mGray);
	    face_cascade.detectMultiScale( mGray, faces, 1.1, 2, 0, new Size(30, 30), new Size() );
	    
	    List<Rect> fa = faces.toList();
	    
	    for (Rect rf : fa)
	       {
	         Point center = new Point(rf.x + rf.width * 0.5, rf.y + rf.height * 0.5);
	         Core.ellipse(mRgba, center, new Size(rf.width * 0.5, rf.height * 0.5), 0, 0, 360, new Scalar(255, 0, 255), 4, 8, 0);
	         //Mat faceROI = mGray.submat(rf);
				if( eyes_cascade.empty() )
					continue;

				Mat smallImgROI = mGray.submat(rf);				
				MatOfRect eyes = new MatOfRect();
				
				eyes_cascade.detectMultiScale(
						smallImgROI, 
						eyes, 
						1.1, 2, 0,
						new Size(30, 30), new Size());
				
				Point eCenter = new Point();
				
				for(Rect e : eyes.toList())
				{
					eCenter.x = (int)Math.round((rf.x + e.x + e.width*0.5));
					eCenter.y = (int)Math.round((rf.y + e.y + e.height*0.5));
					int radius = (int)Math.round((e.width + e.height)*0.25);
					Core.circle( mRgba, eCenter, radius, new Scalar(255, 0, 255), 3, 8, 0 );
				}
	       }
		
		return mRgba;
	}
