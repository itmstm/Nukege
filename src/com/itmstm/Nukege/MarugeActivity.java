package com.itmstm.Nukege;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MarugeActivity extends Activity {
	
	protected static final String TAG = "MarugeActivity";

	private static int FP = ViewGroup.LayoutParams.FILL_PARENT;


	private Maruge2DView mChingeView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// main view 
        mChingeView = new Maruge2DView( this );
        mChingeView.setLayoutParams( mkParams( FP, FP ));
        
        setContentView( mChingeView );
        
        mChingeView.requestFocus();
        
    }

	private ViewGroup.LayoutParams mkParams(int w, int h) {
		return new ViewGroup.LayoutParams(w,  h);
	}
    
    @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		return mChingeView.onMenuItemClick(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	return mChingeView.onCreateOptionsMenu( menu );
	}
}
