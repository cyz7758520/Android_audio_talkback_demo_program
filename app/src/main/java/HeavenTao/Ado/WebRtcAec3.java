package HeavenTao.Ado;

import HeavenTao.Data.*;

//WebRtc第三版声学回音消除器。
public class WebRtcAec3
{
	static
	{
		System.loadLibrary( "Func" ); //加载libFunc.so。
		System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
		System.loadLibrary( "WebRtc3" ); //加载libWebRtc3.so。
	}

	public long m_WebRtcAec3Pt; //存放WebRtc第三版声学回音消除器的指针。

	//构造函数。
	public WebRtcAec3()
	{
		m_WebRtcAec3Pt = 0;
	}

	//析构函数。
	protected void finalize()
	{
		Dstoy();
	}

	//WebRtc第三版声学回音消除器获取应用程序限制信息。
	public static int GetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, Vstr ErrInfoVstrPt )
	{
		return WebRtcAec3GetAppLmtInfo( LicnCodePt, LmtTimeSecPt, RmnTimeSecPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//创建并初始化WebRtc第三版声学回音消除器。
	public int Init( byte LicnCodePt[], int SmplRate, long FrmLenUnit, int Delay, Vstr ErrInfoVstrPt )
	{
		if( m_WebRtcAec3Pt == 0 )
		{
			HTLong p_WebRtcAec3Pt = new HTLong();
			if( WebRtcAec3Init( LicnCodePt, p_WebRtcAec3Pt, SmplRate, FrmLenUnit, Delay, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_WebRtcAec3Pt = p_WebRtcAec3Pt.m_Val;
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

	//设置WebRtc第三版声学回音消除器的回音延迟。
	public int SetDelay( int Delay )
	{
		return WebRtcAec3SetDelay( m_WebRtcAec3Pt, Delay );
	}

	//获取WebRtc第三版声学回音消除器的回音延迟。
	public int GetDelay( HTInt DelayPt )
	{
		return WebRtcAec3GetDelay( m_WebRtcAec3Pt, DelayPt );
	}

	//用WebRtc第三版声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行WebRtc第三版声学回音消除。
	public int Pocs( short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] )
	{
		return WebRtcAec3Pocs( m_WebRtcAec3Pt, InptFrmPt, OtptFrmPt, RsltFrmPt );
	}

	//销毁WebRtc第三版声学回音消除器。
	public int Dstoy()
	{
		if( m_WebRtcAec3Pt != 0 )
		{
			if( WebRtcAec3Dstoy( m_WebRtcAec3Pt ) == 0 )
			{
				m_WebRtcAec3Pt = 0;
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

	//WebRtc第三版声学回音消除器获取应用程序限制信息。
	private static native int WebRtcAec3GetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, long ErrInfoVstrPt );

	//创建并初始化WebRtc第三版声学回音消除器。
	private native int WebRtcAec3Init( byte LicnCodePt[], HTLong WebRtcAec3Pt, int SmplRate, long FrmLenUnit, int Delay, long ErrInfoVstrPt );

	//设置WebRtc第三版声学回音消除器的回音延迟。
	private native int WebRtcAec3SetDelay( long WebRtcAec3Pt, int Delay );

	//获取WebRtc第三版声学回音消除器的回音延迟。
	private native int WebRtcAec3GetDelay( long WebRtcAec3Pt, HTInt DelayPt );

	//用WebRtc第三版声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行WebRtc第三版声学回音消除。
	private native int WebRtcAec3Pocs( long WebRtcAec3Pt, short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] );

	//销毁WebRtc第三版声学回音消除器。
	private native int WebRtcAec3Dstoy( long WebRtcAec3Pt );
}