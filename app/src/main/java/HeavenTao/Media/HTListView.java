package HeavenTao.Media;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
//import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class HTListView extends ListView
{
	public HTListView( Context context )
	{
		super( context );
	}

	@RequiresApi( api = Build.VERSION_CODES.LOLLIPOP ) public HTListView( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
	{
		super( context, attrs, defStyleAttr, defStyleRes );
	}

	public HTListView( Context context, AttributeSet attrs, int defStyleAttr )
	{
		super( context, attrs, defStyleAttr );
	}

	public HTListView( Context context, AttributeSet attrs )
	{
		super( context, attrs );
	}

	//MeasureSpec值的高2位为测量模式，低30位为测量大小。widthMeasureSpec表示View宽度的测量模式和测量大小，heightMeasureSpec表示View高度的测量模式和测量大小。
	//测量模式包括：
	//View.MeasureSpec.AT_MOST: 这个子view你最大不能超过这个值。
	//View.MeasureSpec.UNSPECIFIED：这个子view你大小不确定，还得你自己用尺子给自己量一下，父view尽量给你所需的长度。
	//View.MeasureSpec.EXACTLY：这个子view你就是这么大了。*/

	@Override protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
	{
		ListAdapter p_LstAdapterPt = getAdapter();
		int p_Height = 0;

		if( p_LstAdapterPt != null )
		{
			for( int i = 0; i < p_LstAdapterPt.getCount(); i++ )
			{
				View listItem = p_LstAdapterPt.getView( i, null, this );

				listItem.measure( widthMeasureSpec, View.MeasureSpec.UNSPECIFIED );
				p_Height += listItem.getMeasuredHeight();
			}

			p_Height += ( getDividerHeight() * ( p_LstAdapterPt.getCount() ) );
		}
		if( p_Height < 148 ) p_Height = 148;

		heightMeasureSpec = MeasureSpec.makeMeasureSpec( p_Height, MeasureSpec.EXACTLY );
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
	}
}
