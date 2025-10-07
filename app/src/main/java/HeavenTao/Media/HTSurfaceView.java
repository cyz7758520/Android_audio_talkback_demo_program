package HeavenTao.Media;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
//import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

//自定义SurfaceView，为了保持宽高比。
public class HTSurfaceView extends SurfaceView
{
	public float m_WidthToHeightRatio = 1.0f; //存放宽高比。

	public HTSurfaceView( Context context )
	{
		super( context );
	}

	public HTSurfaceView( Context context, AttributeSet attrs )
	{
		super( context, attrs );
	}

	public HTSurfaceView( Context context, AttributeSet attrs, int defStyleAttr )
	{
		super( context, attrs, defStyleAttr );
	}

	@RequiresApi( api = Build.VERSION_CODES.LOLLIPOP ) public HTSurfaceView( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
	{
		super( context, attrs, defStyleAttr, defStyleRes );
	}

	//MeasureSpec值的高2位为测量模式，低30位为测量大小。widthMeasureSpec表示View宽度的测量模式和测量大小，heightMeasureSpec表示View高度的测量模式和测量大小。
	//测量模式包括：
	//View.MeasureSpec.AT_MOST: 这个子view你最大不能超过这个值。
	//View.MeasureSpec.UNSPECIFIED：这个子view你大小不确定，还得你自己用尺子给自己量一下，父view尽量给你所需的长度。
	//View.MeasureSpec.EXACTLY：这个子view你就是这么大了。*/

	@Override protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
	{
		int width = MeasureSpec.getSize( widthMeasureSpec );
		float height = width / m_WidthToHeightRatio;
		heightMeasureSpec = MeasureSpec.makeMeasureSpec( ( int ) height, heightMeasureSpec );
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
	}

	//设置宽高比。
	public void SetWidthToHeightRatio( float WidthToHeightRatio )
	{
		if( WidthToHeightRatio != m_WidthToHeightRatio ) //如果指定的宽高比与当前的宽高比不一致。
		{
			m_WidthToHeightRatio = WidthToHeightRatio; //设置视频预览SurfaceView的宽高比。

			post( new Runnable() { @Override public void run() { setLayoutParams( getLayoutParams() ); } } ); //刷新SurfaceView的尺寸显示。
		}
	}

	//设置黑屏。
	public void SetBlack()
	{
		Context p_CtxPt = getContext();

		if( p_CtxPt instanceof Activity )
		{
			( ( Activity )p_CtxPt ).runOnUiThread( new Runnable()
			{
				public void run()
				{
					setVisibility( View.GONE );
					setVisibility( View.VISIBLE );
				}
			} );
		}
	}
}
