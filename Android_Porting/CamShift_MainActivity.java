package com.example.camshift;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2 {
	boolean selectObject = false;
	Rect selection=null;
	Point origin;

	int trackObject = 0;

	private CameraBridgeViewBase mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
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

	@SuppressLint("ShowToast")
	@Override
	public boolean onTouchEvent(MotionEvent event) {		
		if (selectObject) {
			selection.x = (int) Math.min(event.getX(), origin.x);
			selection.y = (int) Math.min(event.getY(), origin.y);
			selection.width = (int) Math.abs(event.getX() - origin.x);
			selection.height = (int) Math.abs(event.getY() - origin.y);
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			origin = new Point(event.getX(), event.getY());
			selection = new Rect((int) event.getX(), (int) event.getY(), 0, 0);
			selectObject = true;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			selectObject = false;
			if (selection.width > 0 && selection.height > 0)
				trackObject = -1;
		}
		

		return super.onTouchEvent(event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

		// mOpenCvCameraView.disableView();
		// mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
		// mOpenCvCameraView.setMaxFrameSize(320, 140);

		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	Mat image, mGray;
	Mat hsv, mask, hue, backproj;

	Rect trackWindow;

	boolean backprojMode = false;

	private Mat hist;

	@Override
	public void onCameraViewStarted(int width, int height) {
		image = new Mat();
		mGray = new Mat();
		hsv = new Mat();
		hue = new Mat();
		mask = new Mat();

		hist = new Mat();

		backproj = new Mat();
		
		trackWindow = new Rect();
	}

	@Override
	public void onCameraViewStopped() {

	}

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
		
		if(selection!=null)
			Core.rectangle(image, selection.tl(), selection.br(), new Scalar(0,255, 255),2);

		return image;
	}
}
