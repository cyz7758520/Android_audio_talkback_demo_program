package HeavenTao.Media;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.SurfaceView;

//自定义SurfaceView类，为了保持宽高比。
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

    @RequiresApi( api = Build.VERSION_CODES.LOLLIPOP )
    public HTSurfaceView( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
    {
        super( context, attrs, defStyleAttr, defStyleRes );
    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
    {
        int width = MeasureSpec.getSize( widthMeasureSpec );
        float height = width / m_WidthToHeightRatio;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec( ( int ) height, MeasureSpec.EXACTLY );
        super.onMeasure( widthMeasureSpec, heightMeasureSpec );
    }

    public void setWidthToHeightRatio( float WidthToHeightRatio )
    {
        if( WidthToHeightRatio != m_WidthToHeightRatio ) //如果指定的宽高比与当前的宽高比不一致。
        {
            m_WidthToHeightRatio = WidthToHeightRatio; //设置视频预览SurfaceView类对象的宽高比。

            post( new Runnable() //刷新SurfaceView类对象的尺寸显示。
            {
                @Override
                public void run()
                {
                    setLayoutParams( getLayoutParams() );
                }
            } );
        }
    }
}
