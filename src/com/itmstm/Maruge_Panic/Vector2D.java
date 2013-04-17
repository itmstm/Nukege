package com.itmstm.Maruge_Panic;

import android.graphics.PointF;

public class Vector2D {
	//private static final float EPS = 0.000001f;
	public PointF s;  // 始点
	public PointF v;  // 方向ベクトル
	
	public float mRatio;
	
	Vector2D() {
		s = new PointF();
		v = new PointF();
	}

	public Vector2D(float x, float y, float x2, float y2) {
		s = new PointF(x, y);
		v = new PointF(x2, y2);
	}

	public void set(float x1, float y1, float x2, float y2) {
		s.x = x1;
		s.y = y1;
		v.x = x2;
		v.y = y2;
	}
	
	public float cross( Vector2D v2) {
		return v.x * v2.v.y - v.y * v2.v.x;
	}
	
	public float cross( PointF p, Vector2D v ) {
		return p.x * v.v.y - p.y * v.v.x;
	}

	public boolean collideTo(Vector2D v2) {
		
		PointF s_to_s = new PointF( v2.s.x - s.x, v2.s.y - s.y );
		float crs_v2 = this.cross( v2 );
		
		if( crs_v2 == 0.f ) {
			// 並行である
			return false;
		}
		
		float crs_v_v1 = cross( s_to_s, this );
		float crs_v_v2 = cross( s_to_s, v2 );
		
		float t1 = crs_v_v2 / crs_v2;
		float t2 = crs_v_v1 / crs_v2;
				
		
		//if( (t1 + EPS < 0) || (t1 - EPS > 1) || (t2 + EPS < 0) || (t2 - EPS > 1) )
		if( (t1 < 0) || (t1 > 1) || (t2 < 0) || (t2 > 1) ) 
			return false;
					
		mRatio = crs_v_v1/crs_v2;
		
		return true;
	}

	public float length() {
		//return FloatMath.sqrt( (v.x * v.x) + (v.y * v.y)) ;
		return PointF.length( v.x, v.y );
	}
}
