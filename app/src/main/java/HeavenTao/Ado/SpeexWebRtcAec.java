package HeavenTao.Ado;

import HeavenTao.Data.*;

//SpeexWebRtc三重声学回音消除器。
public class SpeexWebRtcAec
{
	static
	{
		System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
		System.loadLibrary( "Func" ); //加载libFunc.so。
		System.loadLibrary( "SpeexDsp" ); //加载libSpeex.so。
		System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
		System.loadLibrary( "WebRtc3" ); //加载libWebRtc.so。
		System.loadLibrary( "SpeexWebRtcAec" ); //加载libSpeexWebRtcAec.so。
	}

	public class WorkMode
	{
		public static final int SpeexAecWebRtcAecm = 0b0011; //Speex声学回音消除器+WebRtc定点版声学回音消除器。
		public static final int WebRtcAecmWebRtcAec = 0b0110; //WebRtc定点版声学回音消除器+WebRtc浮点版声学回音消除器。
		public static final int SpeexAecWebRtcAecmWebRtcAec = 0b0111; //Speex声学回音消除器+WebRtc定点版声学回音消除器+WebRtc浮点版声学回音消除器。
		public static final int WebRtcAecmWebRtcAec3 = 0b1010; //WebRtc定点版声学回音消除器+WebRtc第三版声学回音消除器。
		public static final int SpeexAecWebRtcAecmWebRtcAec3 = 0b1011; //Speex声学回音消除器+WebRtc定点版声学回音消除器+WebRtc第三版声学回音消除器。
	}

	public long m_SpeexWebRtcAecPt; //存放SpeexWebRtc三重声学回音消除器的指针。

	//构造函数。
	public SpeexWebRtcAec()
	{
		m_SpeexWebRtcAecPt = 0;
	}

	//析构函数。
	protected void finalize()
	{
		Dstoy();
	}

	//SpeexWebRtc三重声学回音消除器获取应用程序限制信息。
	public static int GetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, Vstr ErrInfoVstrPt )
	{
		return SpeexWebRtcAecGetAppLmtInfo( LicnCodePt, LmtTimeSecPt, RmnTimeSecPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//创建并初始化SpeexWebRtc三重声学回音消除器。
	public int Init( byte LicnCodePt[], int SmplRate, long FrmLenUnit, int WorkMode, int SpeexAecFilterLenMsec, int SpeexAecIsUseRec, float SpeexAecEchoMultiple, float SpeexAecEchoCont, int SpeexAecEchoSupes, int SpeexAecEchoSupesAct, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgstcMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay, int WebRtcAec3Delay, int IsUseSameRoomAec, int SameRoomEchoMinDelay, Vstr ErrInfoVstrPt )
	{
		if( m_SpeexWebRtcAecPt == 0 )
		{
			HTLong p_SpeexWebRtcAecPt = new HTLong();
			if( SpeexWebRtcAecInit( LicnCodePt, p_SpeexWebRtcAecPt, SmplRate, FrmLenUnit, WorkMode, SpeexAecFilterLenMsec, SpeexAecIsUseRec, SpeexAecEchoMultiple, SpeexAecEchoCont, SpeexAecEchoSupes, SpeexAecEchoSupesAct, WebRtcAecmIsUseCNGMode, WebRtcAecmEchoMode, WebRtcAecmDelay, WebRtcAecEchoMode, WebRtcAecDelay, WebRtcAecIsUseDelayAgstcMode, WebRtcAecIsUseExtdFilterMode, WebRtcAecIsUseRefinedFilterAdaptAecMode, WebRtcAecIsUseAdaptAdjDelay, WebRtcAec3Delay, IsUseSameRoomAec, SameRoomEchoMinDelay, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_SpeexWebRtcAecPt = p_SpeexWebRtcAecPt.m_Val;
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
	//销毁SpeexWebRtc三重声学回音消除器。
	public int Dstoy()
	{
		if( m_SpeexWebRtcAecPt != 0 )
		{
			if( SpeexWebRtcAecDstoy( m_SpeexWebRtcAecPt ) == 0 )
			{
				m_SpeexWebRtcAecPt = 0;
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

	//设置SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
	public int SetWebRtcAecmDelay( int WebRtcAecmDelay )
	{
		return SpeexWebRtcAecSetWebRtcAecmDelay( m_SpeexWebRtcAecPt, WebRtcAecmDelay );
	}
	//获取SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
	public int GetWebRtcAecmDelay( HTInt WebRtcAecmDelayPt )
	{
		return SpeexWebRtcAecGetWebRtcAecmDelay( m_SpeexWebRtcAecPt, WebRtcAecmDelayPt );
	}
	//设置SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
	public int SetWebRtcAecDelay( int WebRtcAecDelay )
	{
		return SpeexWebRtcAecSetWebRtcAecDelay( m_SpeexWebRtcAecPt, WebRtcAecDelay );
	}
	//获取SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
	public int GetWebRtcAecDelay( HTInt WebRtcAecDelayPt )
	{
		return SpeexWebRtcAecGetWebRtcAecDelay( m_SpeexWebRtcAecPt, WebRtcAecDelayPt );
	}
	//设置SpeexWebRtc三重声学回音消除器的WebRtc第三版声学回音消除器的回音延迟。
	public int SetWebRtcAec3Delay( int WebRtcAec3Delay )
	{
		return SpeexWebRtcAecSetWebRtcAec3Delay( m_SpeexWebRtcAecPt, WebRtcAec3Delay );
	}
	//获取SpeexWebRtc三重声学回音消除器的WebRtc第三版声学回音消除器的回音延迟。
	public int GetWebRtcAec3Delay( HTInt WebRtcAec3DelayPt )
	{
		return SpeexWebRtcAecGetWebRtcAec3Delay( m_SpeexWebRtcAecPt, WebRtcAec3DelayPt );
	}

	//用SpeexWebRtc三重声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行SpeexWebRtc三重声学回音消除。
	public int Pocs( short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] )
	{
		return SpeexWebRtcAecPocs( m_SpeexWebRtcAecPt, InptFrmPt, OtptFrmPt, RsltFrmPt );
	}

	//SpeexWebRtc三重声学回音消除器获取应用程序限制信息。
	private static native int SpeexWebRtcAecGetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, long ErrInfoVstrPt );

	//创建并初始化SpeexWebRtc三重声学回音消除器。
	private native int SpeexWebRtcAecInit( byte LicnCodePt[], HTLong SpeexWebRtcAecPt, int SmplRate, long FrmLenUnit, int WorkMode, int SpeexAecFilterLenMsec, int SpeexAecIsUseRec, float SpeexAecEchoMultiple, float SpeexAecEchoCont, int SpeexAecEchoSupes, int SpeexAecEchoSupesAct, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgstcMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay, int WebRtcAec3Delay, int IsUseSameRoomAec, int SameRoomEchoMinDelay, long ErrInfoVstrPt );
	//销毁SpeexWebRtc三重声学回音消除器。
	private native int SpeexWebRtcAecDstoy( long SpeexWebRtcAecPt );

	//设置SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
	private native int SpeexWebRtcAecSetWebRtcAecmDelay( long SpeexWebRtcAecPt, int WebRtcAecmDelay );
	//获取SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
	private native int SpeexWebRtcAecGetWebRtcAecmDelay( long SpeexWebRtcAecPt, HTInt WebRtcAecmDelayPt );
	//设置SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
	private native int SpeexWebRtcAecSetWebRtcAecDelay( long SpeexWebRtcAecPt, int WebRtcAecDelay );
	//获取SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
	private native int SpeexWebRtcAecGetWebRtcAecDelay( long SpeexWebRtcAecPt, HTInt WebRtcAecDelayPt );
	//设置SpeexWebRtc三重声学回音消除器的WebRtc第三版声学回音消除器的回音延迟。
	private native int SpeexWebRtcAecSetWebRtcAec3Delay( long SpeexWebRtcAecPt, int WebRtcAec3Delay );
	//获取SpeexWebRtc三重声学回音消除器的WebRtc第三版声学回音消除器的回音延迟。
	private native int SpeexWebRtcAecGetWebRtcAec3Delay( long SpeexWebRtcAecPt, HTInt WebRtcAec3DelayPt );

	//用SpeexWebRtc三重声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行SpeexWebRtc三重声学回音消除。
	private native int SpeexWebRtcAecPocs( long SpeexWebRtcAecPt, short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] );
}
