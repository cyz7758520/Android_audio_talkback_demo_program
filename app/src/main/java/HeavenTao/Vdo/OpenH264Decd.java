package HeavenTao.Vdo;

import HeavenTao.Data.*;

//OpenH264解码器。
public class OpenH264Decd
{
	static
	{
		System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
		System.loadLibrary( "Func" ); //加载libFunc.so。
		System.loadLibrary( "OpenH264" ); //加载libOpenH264.so。
	}

	public long m_OpenH264DecdPt; //存放OpenH264解码器的指针。

	//构造函数。
	public OpenH264Decd()
	{
		m_OpenH264DecdPt = 0;
	}

	//析构函数。
	protected void finalize()
	{
		Dstoy( null );
	}

	//OpenH264解码器获取应用程序限制信息。
	public static int GetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, Vstr ErrInfoVstrPt )
	{
		return OpenH264DecdGetAppLmtInfo( LicnCodePt, LmtTimeSecPt, RmnTimeSecPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//创建并初始化OpenH264解码器。
	public int Init( byte LicnCodePt[], int DecdThrdNum, Vstr ErrInfoVstrPt )
	{
		if( m_OpenH264DecdPt == 0 )
		{
			HTLong p_OpenH264DecdPt = new HTLong();
			if( OpenH264DecdInit( LicnCodePt, p_OpenH264DecdPt, DecdThrdNum, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_OpenH264DecdPt = p_OpenH264DecdPt.m_Val;
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
	//销毁OpenH264解码器。
	public int Dstoy( Vstr ErrInfoVstrPt )
	{
		if( m_OpenH264DecdPt != 0 )
		{
			if( OpenH264DecdDstoy( m_OpenH264DecdPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_OpenH264DecdPt = 0;
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

	//用OpenH264解码器对H264格式进行8位无符号整型Yu12格式帧解码。
	public int Pocs( byte H264FrmPt[], long H264FrmLen,
					 byte Yu12FrmPt[], long Yu12FrmSz, HTInt Yu12FrmWidth, HTInt Yu12FrmHeight,
					 Vstr ErrInfoVstrPt )
	{
		return OpenH264DecdPocs( m_OpenH264DecdPt,
								 H264FrmPt, H264FrmLen,
								 Yu12FrmPt, Yu12FrmSz, Yu12FrmWidth, Yu12FrmHeight,
								 ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//OpenH264解码器获取应用程序限制信息。
	private static native int OpenH264DecdGetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, long ErrInfoVstrPt );

	//创建并初始化OpenH264解码器。
	private native int OpenH264DecdInit( byte LicnCodePt[], HTLong OpenH264DecdPt, int DecdThrdNum, long ErrInfoVstrPt );
	//销毁OpenH264解码器。
	private native int OpenH264DecdDstoy( long OpenH264DecdPt, long ErrInfoVstrPt );

	//用OpenH264解码器对H264格式进行8位无符号整型Yu12格式帧解码。
	private native int OpenH264DecdPocs( long OpenH264DecdPt,
										 byte H264FrmPt[], long H264FrmLen,
										 byte Yu12FrmPt[], long Yu12FrmSz, HTInt Yu12FrmWidth, HTInt Yu12FrmHeight,
										 long ErrInfoVstrPt );
}
