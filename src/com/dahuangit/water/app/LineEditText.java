package com.dahuangit.water.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

public class LineEditText extends EditText {

	private Paint mPaint;

	/**
	 * @param context
	 * @param attrs
	 */
	public LineEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();

		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.WHITE);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 画底线,this.getWidth() + 20000 解决文字输入过程下划线会缩短的问题
		canvas.drawLine(0, this.getHeight() - 1, this.getWidth() + 20000, this.getHeight() - 1, mPaint);
	}
}