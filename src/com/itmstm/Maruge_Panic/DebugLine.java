package com.itmstm.Maruge_Panic;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import com.itmstm.Maruge_Panic.R;

public class DebugLine {

	private static final String TAG = "DebugLine";
	private Paint mDebugLinePaint;
	
	public DebugLine( Resources res ) {
		// TODO Auto-generated constructor stub
		Log.d(TAG, "DebugLine constructor: DebugLinecolor id=" + R.color.DebugLineColor );
		
		mDebugLinePaint = new Paint();
		mDebugLinePaint.setColor( res.getColor( R.color.DebugLineColor ));
    	mDebugLinePaint.setStrokeWidth( 1 );
	}

	public void drawGrid(Canvas canvas) {
		// TODO Auto-generated method stub
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		

    	for( int x=0; x<width; x = x+10 ) {
    		canvas.drawLine( x, 0, x, height-1 , mDebugLinePaint );
    	}
    	for( int y=0; y<height; y = y+10 ) {
    		canvas.drawLine( 0, y, width-1, y, mDebugLinePaint );
    	}
    	
	}

}
