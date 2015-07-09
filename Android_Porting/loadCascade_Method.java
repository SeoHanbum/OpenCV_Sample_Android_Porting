	private CascadeClassifier loadCascade(int RID, String fileName)
	{
		//파일 로드를 위한 함수
		CascadeClassifier mJavaDetector = null;

		try {
			// load cascade file from application resources
			InputStream is = getResources().openRawResource(RID);
			File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
			File mCascadeFile = new File(cascadeDir, fileName);
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
				Log.i("", 
						"Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

			cascadeDir.delete();

		} catch (IOException e) {
			e.printStackTrace();
			Log.e("", "Failed to load cascade. Exception thrown: " + e);
		}

		return mJavaDetector;
	}
