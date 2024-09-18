package HeavenTao.Ado;

import HeavenTao.Data.*;

//Speex声学回音消除器。
public class SpeexAec
{
	static
	{
		System.loadLibrary( "Func" ); //加载libFunc.so。
		System.loadLibrary( "SpeexDsp" ); //加载libSpeexDsp.so。
	}

	public long m_SpeexAecPt; //存放Speex声学回音消除器的指针。

	//构造函数。
	public SpeexAec()
	{
		m_SpeexAecPt = 0;
	}

	//析构函数。
	protected void finalize()
	{
		Dstoy();
	}

	//Speex声学回音消除器获取应用程序限制信息。
	public static int GetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, Vstr ErrInfoVstrPt )
	{
		return SpeexAecGetAppLmtInfo( LicnCodePt, LmtTimeSecPt, RmnTimeSecPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//创建并初始化Speex声学回音消除器。
	public int Init( byte LicnCodePt[], int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, Vstr ErrInfoVstrPt )
	{
		if( m_SpeexAecPt == 0 )
		{
			HTLong p_SpeexAecPt = new HTLong();
			if( SpeexAecInit( LicnCodePt, p_SpeexAecPt, SmplRate, FrmLenUnit, FilterLenMsec, IsUseRec, EchoMutp, EchoCntu, EchoSupes, EchoSupesAct, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_SpeexAecPt = p_SpeexAecPt.m_Val;
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

	//用Speex声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行Speex声学回音消除。
	public int Pocs( short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] )
	{
		return SpeexAecPocs( m_SpeexAecPt, InptFrmPt, OtptFrmPt, RsltFrmPt );
	}

	//销毁Speex声学回音消除器。
	public int Dstoy()
	{
		if( m_SpeexAecPt != 0 )
		{
			if( SpeexAecDstoy( m_SpeexAecPt ) == 0 )
			{
				m_SpeexAecPt = 0;
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

	//Speex声学回音消除器获取应用程序限制信息。
	private static native int SpeexAecGetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, long ErrInfoVstrPt );

	//创建并初始化Speex声学回音消除器。
	private native int SpeexAecInit( byte LicnCodePt[], HTLong SpeexAecPt, int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, long ErrInfoVstrPt );

	//用Speex声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行Speex声学回音消除。
	private native int SpeexAecPocs( long SpeexAecPt, short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] );

	//销毁Speex声学回音消除器。
	private native int SpeexAecDstoy( long SpeexAecPt );
}