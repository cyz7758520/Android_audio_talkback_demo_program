package HeavenTao.Ado;

import HeavenTao.Data.*;

//WebRtc浮点版声学回音消除器。
public class WebRtcAec
{
	static
	{
		System.loadLibrary( "Func" ); //加载libFunc.so。
		System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
		System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
	}

	public long m_WebRtcAecPt; //存放WebRtc浮点版声学回音消除器的指针。

	//构造函数。
	public WebRtcAec()
	{
		m_WebRtcAecPt = 0;
	}

	//析构函数。
	protected void finalize()
	{
		Dstoy();
	}

	//WebRtc浮点版声学回音消除器获取应用程序限制信息。
	public static int GetAppLmtInfo( HTString LmtAppNameStrPt, HTString CurAppNameVstrPt, HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, Vstr ErrInfoVstrPt )
	{
		return WebRtcAecGetAppLmtInfo( LmtAppNameStrPt, CurAppNameVstrPt, LmtTimeSecPt, RmnTimeSecPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//创建并初始化WebRtc浮点版声学回音消除器。
	public int Init( int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, Vstr ErrInfoVstrPt )
	{
		if( m_WebRtcAecPt == 0 )
		{
			HTLong p_WebRtcAecPt = new HTLong();
			if( WebRtcAecInit( p_WebRtcAecPt, SmplRate, FrmLenUnit, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_WebRtcAecPt = p_WebRtcAecPt.m_Val;
				return 0;
			}
			else
			{
				return -1;
			}
		}
		else
		{
			return 0;
		}
	}

	//设置WebRtc浮点版声学回音消除器的回音延迟。
	public int SetDelay( int Delay )
	{
		return WebRtcAecSetDelay( m_WebRtcAecPt, Delay );
	}

	//获取WebRtc浮点版声学回音消除器的回音延迟。
	public int GetDelay( HTInt DelayPt )
	{
		return WebRtcAecGetDelay( m_WebRtcAecPt, DelayPt );
	}

	//用WebRtc浮点版声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行WebRtc浮点版声学回音消除。
	public int Pocs( short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] )
	{
		return WebRtcAecPocs( m_WebRtcAecPt, InptFrmPt, OtptFrmPt, RsltFrmPt );
	}

	//销毁WebRtc浮点版声学回音消除器。
	public int Dstoy()
	{
		if( m_WebRtcAecPt != 0 )
		{
			if( WebRtcAecDstoy( m_WebRtcAecPt ) == 0 )
			{
				m_WebRtcAecPt = 0;
				return 0;
			}
			else
			{
				return -1;
			}
		}
		else
		{
			return 0;
		}
	}

	//WebRtc浮点版声学回音消除器获取应用程序限制信息。
	private static native int WebRtcAecGetAppLmtInfo( HTString LmtAppNameStrPt, HTString CurAppNameVstrPt, HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, long ErrInfoVstrPt );

	//创建并初始化WebRtc浮点版声学回音消除器。
	private native int WebRtcAecInit( HTLong WebRtcAecPt, int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, long ErrInfoVstrPt );

	//设置WebRtc浮点版声学回音消除器的回音延迟。
	private native int WebRtcAecSetDelay( long WebRtcAecPt, int Delay );

	//获取WebRtc浮点版声学回音消除器的回音延迟。
	private native int WebRtcAecGetDelay( long WebRtcAecPt, HTInt DelayPt );

	//用WebRtc浮点版声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行WebRtc浮点版声学回音消除。
	private native int WebRtcAecPocs( long WebRtcAecPt, short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] );

	//销毁WebRtc浮点版声学回音消除器。
	private native int WebRtcAecDstoy( long WebRtcAecPt );
}