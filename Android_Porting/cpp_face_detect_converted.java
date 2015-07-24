private CascadeClassifier face_cascade, eyes_cascade;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				
				//System.loadLibrary("detection_based_tracker");
				
				face_cascade = load("haarcascade_frontalface_alt2.xml",R.raw.haarcascade_frontalface_alt2);
				eyes_cascade = load("haarcascade_eye_tree_eyeglasses.xml",R.raw.haarcascade_eye_tree_eyeglasses);
				
				mOpenCvCameraView.setMaxFrameSize(320, 240);
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
	
	public CascadeClassifier load(String cascadeFileName,int RID){
		CascadeClassifier mJavaDetector = null;
		
		try {
            // load cascade file from application resources
			InputStream is = getResources().openRawResource(RID);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, cascadeFileName);
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e("", "Failed to load cascade classifier");
                mJavaDetector = null;
            } else
                Log.i("", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
            

            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("", "Failed to load cascade. Exception thrown: " + e);
        }
		
		return mJavaDetector;
	}
	
	public void detectAndDraw2(    		
			Mat mRgba,
			Mat mGray) {

		Imgproc.equalizeHist(mGray, mGray);
	    face_cascade.detectMultiScale( mGray, faces, 1.1, 2, 0, new Size(30, 30), new Size() );
	    
	    List<Rect> fa = faces.toList();
	    
	    for (Rect rf : fa)
	       {
	         Point center = new Point(rf.x + rf.width * 0.5, rf.y + rf.height * 0.5);
	         Core.ellipse(mRgba, center, new Size(rf.width * 0.5, rf.height * 0.5), 0, 0, 360, new Scalar(255, 0, 255), 4, 8, 0);
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
	}
