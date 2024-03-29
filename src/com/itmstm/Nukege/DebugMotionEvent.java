package com.itmstm.Nukege;

import android.util.Log;
import android.view.MotionEvent;

public class DebugMotionEvent {

	private static final String TAG = "DebugMotionEvent";
	private boolean mDebugEnable;
	

	public DebugMotionEvent() {
		mDebugEnable = false;
	}
	
	public DebugMotionEvent(boolean debugEnable) {
		mDebugEnable = debugEnable;
	}
	
	public boolean isDebugEnable() {
		return mDebugEnable;
	}

	public void setDebugEnable(boolean mDebugEnable) {
		this.mDebugEnable = mDebugEnable;
	}

	public void dumpEvent(MotionEvent event) {
		
		if( mDebugEnable ) {
			// TODO Auto-generated method stub
			String[] names = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
			StringBuilder sb = new StringBuilder();
	
			int action = event.getAction();
			int actionCode = action & MotionEvent.ACTION_MASK;
			
			sb.append( "event ACTION_").append(names[actionCode]);
			
			if( actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_UP ) {
				sb.append( "(pid " ).append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT).append( ")" );
			}
			
			sb.append( "[" );
			
			for( int i=0; i<event.getPointerCount(); i++ ) {
				sb.append( "#").append(i);
				sb.append( "(pid ").append(event.getPointerId(i));
				sb.append( ")=").append((int) event.getX(i));
				sb.append( ",").append((int) event.getY(i));
				if( i+1 < event.getPointerCount())
						sb.append( ";" );
			}
			
			sb.append( "]" );
			Log.d( TAG, sb.toString() );
		
		}
	}

	
}
