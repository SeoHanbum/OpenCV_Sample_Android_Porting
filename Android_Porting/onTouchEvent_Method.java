//onTouchEvent to save Point

@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (selectObject) {
			selection.x = (int) Math.min(event.getX(), origin.x);
			selection.y = (int) Math.min(event.getY(), origin.y);
			selection.width = (int) Math.abs(event.getX() - origin.x);
			selection.height = (int) Math.abs(event.getY() - origin.y);

			Core.rectangle(image, selection.tl(), selection.br(), new Scalar(0,
					255, 255));
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
