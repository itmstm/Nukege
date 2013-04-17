package com.itmstm.Maruge_Panic;

import java.util.Random;

import com.itmstm.Maruge_Panic.R;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.MotionEvent;

public class Maruge {
	private static final int NUM_POINTS = 50;  // 毛をつくる点の数
	private static final float DISTANCE = 8.f; // 点と点の距離
	
	private static final String TAG = "Maruge";
	private static final int NUM_ROUND = 3;
	private static final int NUM_INITIAL_ITERATION = 5;
	private static final float EPS = 0.00001f;
	private static final float MARUGE_X_RANDOMWALK_BIAS = 9.f;	// Random walkのバイアス (X方向)
	private static final float MARUGE_Y_RANDOMWALK_BIAS = 16.f; // Random walkのバイアス (Y方向)
	
	private PointF[] mPts;
	private Paint mDebugPaint = new Paint();
	private Paint mDebugTouchedBoxPaint = new Paint();
	private Paint mDebugBoxInMovePaint = new Paint();

	private boolean mDebug = false;
	private PointF mStartPoint = new PointF();
	private PointF mEndPoint = new PointF();
	private PointF mTouchedPoint;
	private int mTouchedPointIndex;
	
	private boolean mDragMode;
	
	private Vector2D mMotionVector = new Vector2D();
	private Vector2D mTouchedPointMotionVector = new Vector2D();
	private float mDistance;
	private int mConvergePointIndex;
	private int mConvergePointDown;
	private int mConvergePointUp;

	public Maruge( Resources res, boolean debug, Random rng, int w, int h ) {
		
		this.mDebug = debug;
		
		// Paint class for debug box
		mDebugPaint.setColor( res.getColor( R.color.DebugBoxColor ));
		mDebugPaint.setStyle(Style.STROKE);
		mDebugPaint.setStrokeWidth( 2 );	
		
		// paint class for touched Maruge debug box
		mDebugTouchedBoxPaint.setColor( res.getColor(R.color.DebugTouchedBoxColor));
		mDebugTouchedBoxPaint.setStyle(Style.FILL);
		mDebugTouchedBoxPaint.setStrokeWidth( 2 );	
		
		// Paint class for debug box in move
		mDebugBoxInMovePaint.setColor( res.getColor(R.color.DebugBoxInMoveColor));
		mDebugBoxInMovePaint.setStyle(Style.STROKE);
		mDebugBoxInMovePaint.setStrokeWidth( 2 );	
		
		// initialize Maruge points
		mTouchedPointIndex = 0;
		mPts = new PointF[NUM_POINTS];
		for( int i=0; i<NUM_POINTS; i++ ) {
			mPts[i] = new PointF();
		}
		
		mDistance = PointF.length( DISTANCE, DISTANCE );
		
		// 乱数にもとづいて初期ベクターを求め、そこからさらに乱数に基づくモーションベクターを数回に分け適用する
		float initial_w;
		float initial_h;
		
		for( int i=0; i<NUM_INITIAL_ITERATION; i++ ) {
	    	initial_w = (float) rng.nextInt( w );
	    	initial_h = (float) rng.nextInt( h );
	    	
			mMotionVector.set(
					mPts[mTouchedPointIndex].x, mPts[mTouchedPointIndex].y, 
					initial_w, initial_h );
			mPts[mTouchedPointIndex].set( initial_w, initial_h );
	    		
			calculateOtherChingePointPosition( NUM_ROUND );
		}
		
		mTouchedPointIndex = NUM_POINTS - 1;
		for( int i=0; i<NUM_INITIAL_ITERATION; i++ ) {
	    	initial_w = (float) rng.nextInt( 720 );
	    	initial_h = (float) rng.nextInt( 1280 );
	    	
			mMotionVector.set(
					mPts[mTouchedPointIndex].x, mPts[mTouchedPointIndex].y, 
					initial_w, initial_h );
			mPts[mTouchedPointIndex].set( initial_w, initial_h );
	    		
			calculateOtherChingePointPosition( NUM_ROUND );
		}
		mDragMode = false;
	}

	public void setConvergePointIndex(int index) {
		mConvergePointIndex = index;
		mConvergePointDown = index -1;
		mConvergePointUp = index+1;
	}

	public void setDebug(boolean mDebug) {
		this.mDebug = mDebug;
	}

	public void setTouchedPointIndex(int i) {
		mTouchedPointIndex = i;
	}

	public PointF getFirstPoint() {
		return mPts[0];
	}
	
	public PointF getMidPoint() {
		return mPts[NUM_POINTS / 2];
	}
	
	public PointF getLastPoint() {
		return mPts[NUM_POINTS - 1];
	}
	
	public int getTouchedPointIndex() {
		return mTouchedPointIndex;
	}

	public PointF getTouchedPoint() {
		return mPts[mTouchedPointIndex];
	}

	public boolean isTouched() {
		return mDragMode;
	}

	public boolean isDebug() {
		return mDebug;
	}

	public void drawMaruge(Canvas canvas, Paint p ) {
		
		if( mDebug ) {
			drawDebugBoxes( canvas );
		}
		for( int i=0; i < mPts.length -1; i++ ) {
			canvas.drawLine(mPts[i].x, mPts[i].y, mPts[i+1].x, mPts[i+1].y, p );
		}
		
	}

	public boolean action(MotionEvent event) {
		
		switch( event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mStartPoint.set(event.getX(), event.getY());
			return true;
		case MotionEvent.ACTION_UP:
			if( mDragMode )
				mDragMode = false;
			return true;
		case MotionEvent.ACTION_MOVE:
			mEndPoint.set( event.getX(), event.getY() );
			
			// これはドラッグモーションのベクター
			mMotionVector.set( 
					mStartPoint.x, 
					mStartPoint.y, 
					mEndPoint.x - mStartPoint.x, 
					mEndPoint.y - mStartPoint.y );
			
			if( mDragMode ) {   
				// 引っ張られるポイントに適用されるモーションのベクター
				mTouchedPointMotionVector.set( 
						mTouchedPoint.x, 
						mTouchedPoint.y,
						mEndPoint.x - mTouchedPoint.x,
						mEndPoint.y - mTouchedPoint.y );
				
				mTouchedPoint.offset( mTouchedPointMotionVector.v.x, mTouchedPointMotionVector.v.y );
				
				calculateOtherChingePointPosition( NUM_ROUND);
			}
			else if( touchedChinge() ) {
				mTouchedPoint = getTouchedPoint();
				mDragMode = true;
			}
			
			// update start point
			mStartPoint.set( mEndPoint );
			return true;
		}
		return false;
	}

	public void calculateOtherChingePointPosition( int r ) {
		calculateOtherChingePointPosition( r, mTouchedPointIndex );
	}
	
	public void calculateOtherChingePointPosition( int r, int touchedPointIndex ) {
	
		int round = r;
		
		// ボックス下方向
		for( int i=touchedPointIndex -1; i>=0; i-- ) {
			if( round != 0 ) round--;
			calculateNewPointPosition(i, i+1, round );
		}
	
		round = r;
		
		// ボックス上方向
		for( int i=touchedPointIndex +1; i<mPts.length; i++ ) {
			if( round != 0 ) round--;
			calculateNewPointPosition(i, i-1, round );
		}
	}

	public int converge() {
		int ret;
		ret = 0;
		
		// 下方向
		if( mConvergePointDown >= 0 ) {
			mPts[mConvergePointDown].set(mPts[mConvergePointIndex]);
		
			for( int i=mConvergePointDown-1; i>=0; i-- ) {
				calculateNewPointPosition(i, i+1, 0 );
				++ret;
			}
		
			--mConvergePointDown;
		}
	
		// 上方向
		if( mConvergePointUp < mPts.length ) {
			mPts[mConvergePointUp].set(mPts[mConvergePointIndex]);
			
			for( int i=mConvergePointUp +1; i<mPts.length; i++ ) {
				calculateNewPointPosition(i, i-1, 0 );
				++ret;
			}
			
			++mConvergePointUp;
		}
		
		//Log.d(TAG,  "converge: return=" + ret );
		return ret;
	}

	private void drawDebugBoxes( Canvas canvas ) {
		// For debug purpose
		
		Paint p = mDebugPaint;
		
		for( int i=0; i<mPts.length; i++ ) {
			
			if( mDragMode ) {
				p= mDebugBoxInMovePaint;
				
				if( i== mTouchedPointIndex )
					p = mDebugTouchedBoxPaint;
			}
			
			canvas.drawCircle( mPts[i].x, mPts[i].y, mDistance/2, p );
		}
	}

	private void calculateNewPointPosition(int currentIndex, int targetIndex, int round) {
		
		PointF current = mPts[ currentIndex ];
		PointF target = mPts[ targetIndex ];
		
		PointF c_to_t = new PointF();

		if( round > 0 ) {
			// もしRoundが設定されていたら、丸くなるように補正
			PointF a = getOrthgonalVector();
			// Log.d( TAG, "Orthogonal: " + a.x + "," + a.y );
			
			normalize( a );
			// Log.d( TAG, "Normalized Orthogonal: " + a.x + "," + a.y );
			
			// 交線への垂線を求める
			PointF p = getPerpendicular( current, target, a );
			// Log.d( TAG, "Perpendicular: " + p.x + "," + p.y );
			
			// 補正値
			float dx = p.x * ( (float) round / (float) NUM_ROUND );
			float dy = p.y * ( (float) round / (float) NUM_ROUND );
					
			// currentの位置をアップデート
			current.offset( dx, dy );
		}
		
		// currentからtargetへのベクターを取得
		c_to_t.set( target.x - current.x + EPS, target.y - current.y + EPS);
	
		// c_to_tの大きさとmDistanceの比率を求める
		float ratio = mDistance / c_to_t.length();
		
		// c_to_tを適切な長さにする
		multiply( c_to_t, (1.f -ratio) );
		
		// 新しい場所へアップデート
		current.offset( c_to_t.x, c_to_t.y );
	}

	private PointF getPerpendicular(PointF current, PointF target, PointF a) {
		PointF ret = new PointF();
		ret.set( target.x - current.x, target.y - current.y );
		float dot_a_c = dot( a, ret );
		float dx = a.x * dot_a_c;
		float dy = a.y * dot_a_c;
		
		// Log.d( TAG, "Perpendicular: dx=" + dx + ", dy=" + dy );
		ret.set( (target.x + dx) - current.x,  (target.y + dy) - current.y );
				
		return ret;
	}

	private float dot(PointF v1, PointF v2) {
		return v1.x * v2.x + v1.y * v2.y;
	}

	// モーションベクターに直交するベクトルを返す (モーション方向に対して左向きの直交ベクトルを返す）
	private PointF getOrthgonalVector() {
		PointF ret = new PointF();
			
		ret.set( -mMotionVector.v.y, mMotionVector.v.x );
		return ret;
	}

	private void multiply(PointF p, float m) {
		p.set( p.x * m, p.y * m );
	}

	private void normalize(PointF p) {
		p.set( p.x / p.length(), p.y / p.length() );
	}

	private boolean touchedChinge() {
		
		Vector2D line = new Vector2D();
		
		for( int i=0; i<mPts.length - 1; i++ ) {
			
			// Moveイベントの間にｘとｙの移動感覚に隙間ができるので、点と点の間の線で衝突を判定すべき
			
			line.set( mPts[i].x, mPts[i].y, mPts[i+1].x - mPts[i].x, mPts[i+1].y - mPts[i].y );
			
			if( mMotionVector.collideTo( line )) 
			{
				// Log.d(TAG, "Line " + i + " に触った！ Ratio=" + mMotionVector.mRatio );
				
				if( mMotionVector.mRatio < 0.5f ) 
					mTouchedPointIndex = i;
				else 
					mTouchedPointIndex = i + 1;
				
				return true;
			}
		}
		return false;
	}

	public void setDragMode(boolean b) {
		mDragMode = b;
	}

	public void randomWalk(Random rng) {
		PointF first = getFirstPoint();
		PointF mid = getMidPoint();
		PointF last = getLastPoint();
		
		// mid
		mid.offset( (rng.nextFloat() - 0.5f) * MARUGE_X_RANDOMWALK_BIAS, 
				(rng.nextFloat() - 0.5f) * MARUGE_Y_RANDOMWALK_BIAS );
		calculateOtherChingePointPosition(0, 0);
				
		// first
		first.offset( (rng.nextFloat() - 0.5f) * MARUGE_X_RANDOMWALK_BIAS, 
				(rng.nextFloat() - 0.5f) * MARUGE_Y_RANDOMWALK_BIAS );
		calculateOtherChingePointPosition(0, 0);
		
		// last
		last.offset( (rng.nextFloat() - 0.5f) * MARUGE_X_RANDOMWALK_BIAS, 
				(rng.nextFloat() - 0.5f) * MARUGE_Y_RANDOMWALK_BIAS );
		calculateOtherChingePointPosition(0, NUM_POINTS - 1);
	}
}
