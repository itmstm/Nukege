package com.itmstm.Maruge_Panic;

import java.util.Random;
import java.util.Date;

import com.itmstm.Maruge_Panic.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class Maruge2DView extends View implements OnTouchListener  {

	enum BG {
		BG_WHITE,
		BG_BLACK,
	}

	enum MM {
		MM_WAITING,
		MM_VACUUM,
	}
	
	enum VM {
		VM_NO_VACUUM,
		VM_INITIAL_VACUUM,
		VM_VACUUM_ALL,
		VM_INITIAL_SPIT,
		VM_SPIT_AT_RANDOM,
	}

	private static final String TAG = "Maruge2DView";
	private static final int MENU_TOGGLE_DEBUG = Menu.FIRST;
	private static final int MENU_SET_BG_WHITE = Menu.FIRST+1;
	private static final int MENU_SET_BG_BLACK = Menu.FIRST+2;
	private static final int MENU_HIDE_MAN = Menu.FIRST+3;
	private static final int NUM_MARUGE = 10;
	private static final long MAN_UPDATE_DELAY = 500;		// 500 ms
	private static final long VACUUM_UPDATE_DELAY = 100;	// 100 ms
	protected static final long MARUGE_UPDATE_DELAY = 16;   //  16 ms

	private static final float SPIT_DELTA_X = 1.0005f;
	private static final float SPIT_DEFAULT_DX = 20.f;
	
	
	private DebugLine mDebugLine;
	private Maruge[] mMaruge;
	private Paint mMarugePaint = new Paint();	
	private Paint mDebugMouthPaint = new Paint();	
	private boolean mDebug = false;
	private Resources mRes;
	private Rect mMouthRect = new Rect();

	private DebugMotionEvent mDebugMotionEvent;
	private BG mBG = BG.BG_WHITE;
	private Random mRNG;
	private Date mDate;
	private Bitmap mManTaiki1Bitmap;
	private Bitmap mManTaiki2Bitmap;
	private Bitmap mManVacuumBitmap;
	private Bitmap mManSpitBitmap;
	
	private Rect mManDstRect = new Rect();
	
	private Handler mManHandler = new Handler();
	private Handler mVacuumHandler = new Handler();
	private Handler mMarugePanicHandler = new Handler();
	private Runnable mUpdateManImageTask;
	private Runnable mVacuumTask;
	private Runnable mMarugePanicTask;
	private Bitmap mManBitmap;
	protected int mVacuumFrameCount = 0;
	private VM mVacuumMode;
	private float mSpitDx[] = new float[NUM_MARUGE];
	private float mSpitVar;
	private boolean mHideMan = false;
	private int mHeight;
	private int mWidth;
	private float mThroat_Y;
	private float mThroat_X;
	
	protected void makeVacuumAction() {
		
		// まず吸う  →　残りのポイントを吸う　→　吐く
		
		switch( mVacuumMode ) {
		case VM_INITIAL_VACUUM:		
			
			//Log.d( TAG, "VM_INITIAL_VACUUM");
			// TouchedPointをまず喉の奥に移動する
			
			for( int i=0; i<mMaruge.length; i++ ) {
				mMaruge[i].getTouchedPoint().set(mThroat_X, mThroat_Y );
				mMaruge[i].calculateOtherChingePointPosition( 0 );
				
		    	// PointのConvergeするポイントを設定（TouchedPoint)
				mMaruge[i].setConvergePointIndex( mMaruge[i].getTouchedPointIndex() );
				
				// TouchedPointを最初のインデックスにする (後でSpitする時に最初に動かすポイントをセット）
				mMaruge[i].setTouchedPointIndex( 0 );
		    	mSpitDx[i] = -((float)i+5.f);  // 若干のOffsetをつける
		    	mSpitVar = SPIT_DEFAULT_DX;  // 若干のOffsetをつける
		    	
				// ○毛のドラッグモードを解除する
		    	mMaruge[i].setDragMode( false );
			}

	    	// Vacuum モードの次のステートへ移動
	    	mVacuumMode = VM.VM_VACUUM_ALL;
	    	
			// 次のTaskの設定
			mManHandler.removeCallbacks( mVacuumTask );
	    	mManHandler.postDelayed( mVacuumTask, VACUUM_UPDATE_DELAY );
	    	
			break; 
		case VM_VACUUM_ALL:
			// Log.d( TAG, "VM_VACUUM_ALL");
			
			int conv = 0;
			
			// Pointをひとつづつ喉の奥へ移動する
			for( int i=0; i<mMaruge.length; i++ ) {
				conv += mMaruge[i].converge();
			}
			//Log.d( TAG, "convergence count: " + conv );
			
			mManHandler.removeCallbacks( mVacuumTask );
			if( conv == 0 ) {   // No more Convergence
		    	mVacuumMode = VM.VM_INITIAL_SPIT;
		    	
				// 次のTaskの設定
		    	mManHandler.postDelayed( mVacuumTask, VACUUM_UPDATE_DELAY * 5);	// 少し間をおく
			}
			else {
		    	mManHandler.postDelayed( mVacuumTask, VACUUM_UPDATE_DELAY / 50 );	// 少し早めにVacuumする
			}
	    	
			break;
		case VM_INITIAL_SPIT:
			// Log.d( TAG, "VM_SPIT");
			
			// 男の顔をSpitにする
			mManBitmap = mManSpitBitmap;
			
			// まず中心に向かって吐く
			
			float dy;
			float a = (8.f / (float) this.getHeight() );
			float y_limit = (float) this.getHeight() / 6.f;
			
			// Log.d (TAG, "this.getHeight()="+getHeight());
			
			int spitted_count = 0;
			for( int i=0; i<mMaruge.length; i++ ) {
				if( mMaruge[i].getFirstPoint().x < 40 || mMaruge[i].getFirstPoint().y < y_limit ) {
					++spitted_count;
				}
				else {
					// x, yの場所を計算
					mSpitVar = mSpitVar / SPIT_DELTA_X;
					mSpitDx[i] = mSpitDx[i] - mSpitVar;
					dy = -1 * (a/(((float)i)/2.f+0.25f)) * (mSpitDx[i] * mSpitDx[i]); 	// Formula -> y = a * x^2
					
					// Log.d( TAG, "a="+a+"  (dx, dy)=(" + mSpitDx[i] + ", " + dy + ")");
					
					mMaruge[i].getFirstPoint().set( mThroat_X + mSpitDx[i], mThroat_Y + dy);
					mMaruge[i].calculateOtherChingePointPosition( 0 );
				}
			}
		
			// 全部吐き出されたか？
			if( spitted_count == mMaruge.length )
				mVacuumMode = VM.VM_SPIT_AT_RANDOM;
			
			mManHandler.removeCallbacks( mVacuumTask );
	    	mManHandler.postDelayed( mVacuumTask, VACUUM_UPDATE_DELAY / 5 );	// 少し間をおく
			
			break;
		case VM_SPIT_AT_RANDOM:
			// 吐いた毛がバラバラになる
			// Log.d( TAG, "VM_SPIT_AT_RANDOM");
			
			// 男の顔を普通に戻す
			mManBitmap = mManTaiki1Bitmap;
			
			// タッチパネルを再度有効にする
			this.setOnTouchListener(this);
			
			// 吸うモードをデフォルトに戻す
			mVacuumMode = VM.VM_NO_VACUUM;
			
			// 男の顔を動かすようにする
	    	mManHandler.postDelayed(mUpdateManImageTask, MAN_UPDATE_DELAY);
	    	
			mManHandler.removeCallbacks( mVacuumTask );
			break;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		Log.d( TAG, "OnSizeChanged("+w+","+h+","+oldw+","+oldh+")!");
	}

	@Override
	protected void onDraw( Canvas canvas ) {
		// begin drawing
	
		if( mDebug ) {
	    	mDebugLine.drawGrid( canvas );
	    	canvas.drawRect(mMouthRect, mDebugMouthPaint );
		}
		
		switch( mBG ) {
		case BG_WHITE:
			mMarugePaint.setColor( mRes.getColor( R.color.ChingeColorBlack ));
			setBackgroundResource( R.color.BackGroundColorWhite );
			break;
		case BG_BLACK:
			mMarugePaint.setColor( mRes.getColor( R.color.ChingeColorWhite ));
			setBackgroundResource( R.color.BackGroundColorBlack );
			break;
		}
		
		// Draw maruge
		for( int i=0; i<mMaruge.length; i++ ) {
			//mMarugePaint.setStrokeWidth((float) i+1.f );
	    	mMaruge[i].drawMaruge(canvas, mMarugePaint );
		}
		
		if( ! mHideMan ) {
			// 男によるVacuumアクションに入るかどうか
			if( (mVacuumMode == VM.VM_NO_VACUUM ) && MarugeAtMouth() ) {
				
				//Log.d( TAG, "Maruge touched" );
				// Vacuumアクションの開始コード
				mVacuumMode = VM.VM_INITIAL_VACUUM;
		    	mVacuumHandler.postDelayed( mVacuumTask, VACUUM_UPDATE_DELAY );
		    	
		    	// 男の顔をVacuumにする
		    	mManBitmap = mManVacuumBitmap;
		    	
		    	// disable Man-Waiting thread
		    	mManHandler.removeCallbacks(mUpdateManImageTask);
		    	
		    	// disable motion event
				this.setOnTouchListener(null);
			}
			
			// Draw Man
			canvas.drawBitmap(mManBitmap, null, mManDstRect, null );
		}
	}

	// Constructor
	public Maruge2DView(MarugeActivity context, int w, int h) {
		super( context );
		
		//Log.d(TAG,  "Ching2DView constructor!");
		
		// widthとheightを設定
		mWidth = w;
		mHeight = h;
		mThroat_X  = (float) mWidth - 30;
		mThroat_Y = (float) mHeight - 60;

		// enable this view to receive touch event
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		this.setOnTouchListener(this);
		
       // create random number generator
        mDate = new Date();
        
        mRNG = new Random();
        mRNG.setSeed( mDate.getTime() );
        
		// background
		mRes = getResources();
		this.setBackgroundColor( mRes.getColor( R.color.BackGroundColorBlack ));
		
		//  Drawing object
		mDebugLine = new DebugLine( mRes );
		
		mMaruge = new Maruge[ NUM_MARUGE ];
		for( int i=0; i<NUM_MARUGE; i++ ) {
			mMaruge[i] = new Maruge( mRes,  mDebug, mRNG, mWidth, mHeight );		// TODO: try avoid hard-coded width & height
		}
		
        // for touch event debug
        mDebugMotionEvent = new DebugMotionEvent();
        mDebugMotionEvent.setDebugEnable(mDebug);
        
		// Paint class for Maruge
		mMarugePaint.setColor( mRes.getColor( R.color.ChingeColorBlack ));
    	mMarugePaint.setStrokeWidth( 2 );	 
    	
    	// PaintClass for Mouth (debug)
		mDebugMouthPaint.setColor( mRes.getColor( R.color.DebugMouthColor ));
    	mDebugMouthPaint.setStrokeWidth( 1 );	 
    	
    	// Bitmaps for man
    	mManTaiki1Bitmap = BitmapFactory.decodeResource(mRes, R.drawable.man_taiki1);
    	mManTaiki2Bitmap = BitmapFactory.decodeResource(mRes, R.drawable.man_taiki2);
    	mManVacuumBitmap = BitmapFactory.decodeResource(mRes, R.drawable.man_vacuum);
    	mManSpitBitmap = BitmapFactory.decodeResource(mRes, R.drawable.man_spit);
    	
    	mManBitmap = mManTaiki1Bitmap;
    	mVacuumMode = VM.VM_NO_VACUUM;
    	
    	// Man's mouse position
    	mMouthRect.set( mWidth - 80, mHeight - 100, mWidth - 40, mHeight - 30 );
    	
    	// Rect for area where Man is drawn
    	mManDstRect.set( mWidth - 100, mHeight - 300, mWidth, mHeight ); 
    	
    	// Timer for Man Waiting
    	mUpdateManImageTask = new Runnable() {
			public void run() {
				//Log.d( TAG, "UpdateManImage Task" );
				
				if( mManBitmap == mManTaiki1Bitmap ) 
					mManBitmap = mManTaiki2Bitmap;
				else
					mManBitmap = mManTaiki1Bitmap;
				
				invalidate();
				
    			mManHandler.removeCallbacks( mUpdateManImageTask );
		    	mManHandler.postDelayed(mUpdateManImageTask, MAN_UPDATE_DELAY);
    		}
    	};
    	mManHandler.postDelayed(mUpdateManImageTask, MAN_UPDATE_DELAY);
    	
    	
    	// Thread for vacuum animation
    	mVacuumTask = new Runnable() {

			public void run() {
				//Log.d( TAG, "Vacuum Task (Frame): " + mVacuumFrameCount );
				makeVacuumAction();
				mVacuumFrameCount++;
				invalidate();
				
			}
    	};
    	
    	// Thread for animating maruge
    	mMarugePanicTask = new Runnable() {
    		
    		public void run() {
    			
    			// update mMaruge position
    			for( int i=0; i<NUM_MARUGE; i++ ) {
    				mMaruge[i].randomWalk(mRNG);
    			}
    			
    			invalidate();
    			
    			mManHandler.removeCallbacks( mMarugePanicTask );
		    	mManHandler.postDelayed(mMarugePanicTask, MARUGE_UPDATE_DELAY);
    		}
    	};
    	mMarugePanicHandler.postDelayed(mMarugePanicTask, MARUGE_UPDATE_DELAY);
    }	
	
	public void setDebug(boolean mDebug) {
		this.mDebug = mDebug;
	}

	public boolean onTouch(View v, MotionEvent event) {
		mDebugMotionEvent.dumpEvent( event );
		boolean ret = true;;
		
		for( int i=0; i<mMaruge.length; i++ ) {
			mMaruge[i].action( event );
		}
		
		invalidate();
		
		return ret;
	}

	public void toggleDebug() {
		if( mDebug ) {
			mDebug = false;
			for( int i=0; i<mMaruge.length; i++ ) 
				mMaruge[i].setDebug(false);
		}
		else {
			mDebug = true;
			for( int i=0; i<mMaruge.length; i++ ) 
				mMaruge[i].setDebug(true);
		}
		invalidate();
	}

	public boolean onMenuItemClick(MenuItem item) {
		
		switch( item.getItemId()) {
		case MENU_TOGGLE_DEBUG:
			this.toggleDebug();
			break;
		case MENU_SET_BG_BLACK:
			mBG = BG.BG_BLACK;
			break;
		case MENU_SET_BG_WHITE:
			mBG = BG.BG_WHITE;
		case MENU_HIDE_MAN:
			mHideMan = ! mHideMan;
			break;
		}
		invalidate();
		return false;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add( 0, MENU_TOGGLE_DEBUG, Menu.NONE, R.string.menu_toggle_debug).setIcon( R.drawable.ic_launcher);
		menu.add( 0, MENU_SET_BG_WHITE, Menu.NONE, R.string.menu_set_bg_white);
		menu.add( 0, MENU_SET_BG_BLACK, Menu.NONE, R.string.menu_set_bg_black);
		menu.add( 0, MENU_HIDE_MAN, 	Menu.NONE, R.string.menu_hide_man);
		return true;
	}

	private boolean MarugeAtMouth() {
		for( int i=0; i<mMaruge.length; i++ ) {
			// タッチされているか？
			if( ! mMaruge[i].isTouched() ) return false;
			
			// 毛が口のところにあるか？
			if( ! mMouthRect.contains( (int)mMaruge[i].getTouchedPoint().x, (int)mMaruge[i].getTouchedPoint().y )) 
				return false;
		}
		return true;
	}
}
