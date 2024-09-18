package HeavenTao.Media;

import HeavenTao.Data.*;

//Avi文件写入器。
public class AviFileWriter
{
	static
	{
		System.loadLibrary( "Func" ); //加载libFunc.so。
		System.loadLibrary( "MediaFile" ); //加载libMediaFile.so。
	}

	public long m_AviFileWriterPt; //存放Avi文件写入器的指针。

	//构造函数。
	public AviFileWriter()
	{
		m_AviFileWriterPt = 0;
	}

	//析构函数。
	protected void finalize()
	{
		Dstoy( null );
	}

	//Avi文件写入器获取应用程序限制信息。
	public static int GetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterGetAppLmtInfo( LicnCodePt, LmtTimeSecPt, RmnTimeSecPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//创建并初始化Avi文件写入器。
	public int Init( byte LicnCodePt[], String AviFileFullPathStrPt, long AviFileWrBufSzByt, int MaxStrmNum, Vstr ErrInfoVstrPt )
	{
		if( m_AviFileWriterPt == 0 )
		{
			HTLong p_AviFileWriterPt = new HTLong();
			if( AviFileWriterInit( LicnCodePt, p_AviFileWriterPt, AviFileFullPathStrPt, AviFileWrBufSzByt, MaxStrmNum, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_AviFileWriterPt = p_AviFileWriterPt.m_Val;
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

	//设置整个Avi文件时间线的起始时间戳。
	public int SetStartTimeStamp( long StartTimeStampMsec, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterSetStartTimeStamp( m_AviFileWriterPt, StartTimeStampMsec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	//获取整个Avi文件时间线的起始时间戳。
	public int GetStartTimeStamp( HTLong StartTimeStampMsecPt, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterGetStartTimeStamp( m_AviFileWriterPt, StartTimeStampMsecPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//Avi文件添加音频流。
	public int AddAdoStrm( int Fmt, int SampleRate, int ChanlNum, HTInt AdoStrmIdxPt, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterAddAdoStrm( m_AviFileWriterPt, Fmt, SampleRate, ChanlNum, AdoStrmIdxPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	//设置Avi文件指定音频流的当前时间戳。
	public int AdoStrmSetCurTimeStamp( int AdoStrmIdx, long CurTimeStampMsec, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterAdoStrmSetCurTimeStamp( m_AviFileWriterPt, AdoStrmIdx, CurTimeStampMsec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	//获取Avi文件指定音频流的当前时间戳。
	public int AdoStrmGetCurTimeStamp( int AdoStrmIdx, HTLong CurTimeStampMsecPt, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterAdoStrmGetCurTimeStamp( m_AviFileWriterPt, AdoStrmIdx, CurTimeStampMsecPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	//Avi文件指定音频流的写入音频帧。
	public int AdoStrmWriteByte( int AdoStrmIdx, byte FrmPt[], long FrmLenByt, long FrmLenMsec, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterAdoStrmWriteByte( m_AviFileWriterPt, AdoStrmIdx, FrmPt, FrmLenByt, FrmLenMsec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	public int AdoStrmWriteShort( int AdoStrmIdx, short FrmPt[], long FrmLenByt, long FrmLenMsec, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterAdoStrmWriteShort( m_AviFileWriterPt, AdoStrmIdx, FrmPt, FrmLenByt, FrmLenMsec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//Avi文件添加视频流。
	public int AddVdoStrm( int Fmt, int MaxSampleRate, HTInt VdoStrmIdxPt, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterAddVdoStrm( m_AviFileWriterPt, Fmt, MaxSampleRate, VdoStrmIdxPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	//获取Avi文件指定视频流的当前时间戳。
	public int VdoStrmGetCurTimeStamp( int VdoStrmIdx, HTLong CurTimeStampMsecPt, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterVdoStrmGetCurTimeStamp( m_AviFileWriterPt, VdoStrmIdx, CurTimeStampMsecPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	//Avi文件指定视频流的写入视频帧。
	public int VdoStrmWriteByte( int VdoStrmIdx, long FrmTimeStampMsec, byte FrmPt[], long FrmLenByt, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterVdoStrmWriteByte( m_AviFileWriterPt, VdoStrmIdx, FrmTimeStampMsec, FrmPt, FrmLenByt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	public int VdoStrmWriteShort( int VdoStrmIdx, long FrmTimeStampMsec, short FrmPt[], long FrmLenByt, Vstr ErrInfoVstrPt )
	{
		return AviFileWriterVdoStrmWriteShort( m_AviFileWriterPt, VdoStrmIdx, FrmTimeStampMsec, FrmPt, FrmLenByt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//销毁Avi文件写入器。
	public int Dstoy( Vstr ErrInfoVstrPt )
	{
		if( m_AviFileWriterPt != 0 )
		{
			if( AviFileWriterDstoy( m_AviFileWriterPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_AviFileWriterPt = 0;
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

	//Avi文件写入器获取应用程序限制信息。
	private static native int AviFileWriterGetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, long ErrInfoVstrPt );

	//创建并初始化Avi文件写入器。
	private native int AviFileWriterInit( byte LicnCodePt[], HTLong AviFileWriterPt, String AviFileFullPathStrPt, long AviFileWrBufSzByt, int MaxStrmNum, long ErrInfoVstrPt );

	//设置整个Avi文件时间线的起始时间戳。
	private native int AviFileWriterSetStartTimeStamp( long AviFileWriterPt, long StartTimeStampMsec, long ErrInfoVstrPt );
	//获取整个Avi文件时间线的起始时间戳。
	private native int AviFileWriterGetStartTimeStamp( long AviFileWriterPt, HTLong StartTimeStampMsecPt, long ErrInfoVstrPt );

	//Avi文件添加音频流。
	private native int AviFileWriterAddAdoStrm( long AviFileWriterPt, int Fmt, int SampleRate, int ChanlNum, HTInt AdoStrmIdxPt, long ErrInfoVstrPt );
	//设置Avi文件指定音频流的当前时间戳。
	private native int AviFileWriterAdoStrmSetCurTimeStamp( long AviFileWriterPt, int AdoStrmIdx, long CurTimeStampMsec, long ErrInfoVstrPt );
	//获取Avi文件指定音频流的当前时间戳。
	private native int AviFileWriterAdoStrmGetCurTimeStamp( long AviFileWriterPt, int AdoStrmIdx, HTLong CurTimeStampMsecPt, long ErrInfoVstrPt );
	//Avi文件指定音频流的写入字节型音频帧。
	private native int AviFileWriterAdoStrmWriteByte( long AviFileWriterPt, int AdoStrmIdx, byte FrmPt[], long FrmLenByt, long FrmLenMsec, long ErrInfoVstrPt );
	//Avi文件指定音频流的写入短整型音频帧。
	private native int AviFileWriterAdoStrmWriteShort( long AviFileWriterPt, int AdoStrmIdx, short FrmPt[], long FrmLenTwoByt, long FrmLenMsec, long ErrInfoVstrPt );

	//Avi文件添加视频流。
	private native int AviFileWriterAddVdoStrm( long AviFileWriterPt, int Fmt, int MaxSampleRate, HTInt VdoStrmIdxPt, long ErrInfoVstrPt );
	//获取Avi文件指定视频流的当前时间戳。
	private native int AviFileWriterVdoStrmGetCurTimeStamp( long AviFileWriterPt, int VdoStrmIdx, HTLong CurTimeStampMsecPt, long ErrInfoVstrPt );
	//Avi文件指定视频流的写入字节型视频帧。
	private native int AviFileWriterVdoStrmWriteByte( long AviFileWriterPt, int VdoStrmIdx, long FrmTimeStampMsec, byte FrmPt[], long FrmLenByt, long ErrInfoVstrPt );
	//Avi文件指定视频流的写入短整型视频帧。
	private native int AviFileWriterVdoStrmWriteShort( long AviFileWriterPt, int VdoStrmIdx, long FrmTimeStampMsec, short FrmPt[], long FrmLenTwoByt, long ErrInfoVstrPt );

	//销毁Avi文件写入器。
	private native int AviFileWriterDstoy( long AviFileWriterPt, long ErrInfoVstrPt );
}
