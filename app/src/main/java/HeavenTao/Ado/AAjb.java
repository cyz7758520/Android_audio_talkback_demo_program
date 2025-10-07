package HeavenTao.Ado;

import HeavenTao.Data.*;

//音频自适应抖动缓冲器。
public class AAjb
{
	static
	{
		System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
		System.loadLibrary( "Func" ); //加载libFunc.so。
		System.loadLibrary( "Ajb" ); //加载libAjb.so。
	}

	public long m_AAjbPt; //存放音频自适应抖动缓冲器的指针。

	//构造函数。
	public AAjb()
	{
		m_AAjbPt = 0;
	}

	//析构函数。
	protected void finalize()
	{
		Dstoy( null );
	}

	//音频自适应抖动缓冲器获取应用程序限制信息。
	public static int GetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, Vstr ErrInfoVstrPt )
	{
		return AAjbGetAppLmtInfo( LicnCodePt, LmtTimeSecPt, RmnTimeSecPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//创建并初始化音频自适应抖动缓冲器。
	public int Init( byte LicnCodePt[], int SmplRate, long FrmLenUnit, int IsHaveTimeStamp, int TimeStampStep, int InactIsContPut, int MinNeedBufFrmCnt, int MaxNeedBufFrmCnt, int MaxCntuLostFrmCnt, float AdaptSensitivity, int IsDelObsltFrm, Vstr ErrInfoVstrPt )
	{
		if( m_AAjbPt == 0 )
		{
			HTLong p_AAjbPt = new HTLong();
			if( AAjbInit( LicnCodePt, p_AAjbPt, SmplRate, FrmLenUnit, IsHaveTimeStamp, TimeStampStep, InactIsContPut, MinNeedBufFrmCnt, MaxNeedBufFrmCnt, MaxCntuLostFrmCnt, AdaptSensitivity, IsDelObsltFrm, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_AAjbPt = p_AAjbPt.m_Val;
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
	//销毁音频自适应抖动缓冲器。
	public int Dstoy( Vstr ErrInfoVstrPt )
	{
		if( m_AAjbPt != 0 )
		{
			if( AAjbDstoy( m_AAjbPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_AAjbPt = 0;
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

	//放入字节型帧到音频自适应抖动缓冲器。
	public int PutByteFrm( int TimeStamp, byte ByteFrmPt[], long FrmStart, long FrmLen, int IsAutoLock, Vstr ErrInfoVstrPt )
	{
		return AAjbPutByteFrm( m_AAjbPt, TimeStamp, ByteFrmPt, FrmStart, FrmLen, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	//放入短整型帧到音频自适应抖动缓冲器。
	public int PutShortFrm( int TimeStamp, short ShortFrmPt[], long FrmStart, long FrmLen, int IsAutoLock, Vstr ErrInfoVstrPt )
	{
		return AAjbPutShortFrm( m_AAjbPt, TimeStamp, ShortFrmPt, FrmStart, FrmLen, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//从音频自适应抖动缓冲器取出字节型帧。
	public int GetByteFrm( HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmSz, HTLong FrmLenPt, int IsAutoLock, Vstr ErrInfoVstrPt )
	{
		return AAjbGetByteFrm( m_AAjbPt, TimeStampPt, ByteFrmPt, FrmStart, FrmSz, FrmLenPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	//从音频自适应抖动缓冲器取出短整型帧。
	public int GetShortFrm( HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmSz, HTLong FrmLenPt, int IsAutoLock, Vstr ErrInfoVstrPt )
	{
		return AAjbGetShortFrm( m_AAjbPt, TimeStampPt, ShortFrmPt, FrmStart, FrmSz, FrmLenPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//获取缓冲帧的数量。
	public int GetBufFrmCnt( HTInt CurHaveBufActFrmCntPt, HTInt CurHaveBufInactFrmCntPt, HTInt CurHaveBufFrmCntPt, HTInt MinNeedBufFrmCntPt, HTInt MaxNeedBufFrmCntPt, HTInt MaxCntuLostFrmCntPt, HTInt CurNeedBufFrmCntPt, int IsAutoLock, Vstr ErrInfoVstrPt )
	{
		return AAjbGetBufFrmCnt( m_AAjbPt, CurHaveBufActFrmCntPt, CurHaveBufInactFrmCntPt, CurHaveBufFrmCntPt, MinNeedBufFrmCntPt, MaxNeedBufFrmCntPt, MaxCntuLostFrmCntPt, CurNeedBufFrmCntPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//清空音频自适应抖动缓冲器。
	public int Clear( int IsAutoLock, Vstr ErrInfoVstrPt )
	{
		return AAjbClear( m_AAjbPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	//清空并重置音频自适应抖动缓冲器。
	public int Reset( int IsAutoLock, Vstr ErrInfoVstrPt )
	{
		return AAjbReset( m_AAjbPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//音频自适应抖动缓冲器获取应用程序限制信息。
	private static native int AAjbGetAppLmtInfo( byte LicnCodePt[], HTLong LmtTimeSecPt, HTLong RmnTimeSecPt, long ErrInfoVstrPt );

	//创建并初始化音频自适应抖动缓冲器。
	private native int AAjbInit( byte LicnCodePt[], HTLong AAjbPt, int SmplRate, long FrmLenUnit, int IsHaveTimeStamp, int TimeStampStep, int InactIsContPut, int MinNeedBufFrmCnt, int MaxNeedBufFrmCnt, int MaxCntuLostFrmCnt, float AdaptSensitivity, int IsDelObsltFrm, long ErrInfoVstrPt );
	//销毁音频自适应抖动缓冲器。
	private native int AAjbDstoy( long AAjbPt, long ErrInfoVstrPt );

	//放入字节型帧到音频自适应抖动缓冲器。
	private native int AAjbPutByteFrm( long AAjbPt, int TimeStamp, byte ByteFrmPt[], long FrmStart, long FrmStartLenByt, int IsAutoLock, long ErrInfoVstrPt );
	//放入短整型帧到音频自适应抖动缓冲器。
	private native int AAjbPutShortFrm( long AAjbPt, int TimeStamp, short ShortFrmPt[], long FrmStart, long FrmStartLenTwoByt, int IsAutoLock, long ErrInfoVstrPt );

	//从音频自适应抖动缓冲器取出字节型帧。
	private native int AAjbGetByteFrm( long AAjbPt, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmStartSzByt, HTLong FrmLenBytPt, int IsAutoLock, long ErrInfoVstrPt );
	//从音频自适应抖动缓冲器取出短整型帧。
	private native int AAjbGetShortFrm( long AAjbPt, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmStartSzTwoByt, HTLong FrmLenTwoBytPt, int IsAutoLock, long ErrInfoVstrPt );

	//获取缓冲帧的数量。
	private native int AAjbGetBufFrmCnt( long AAjbPt, HTInt CurHaveBufActFrmCntPt, HTInt CurHaveBufInactFrmCntPt, HTInt CurHaveBufFrmCntPt, HTInt MinNeedBufFrmCntPt, HTInt MaxNeedBufFrmCntPt, HTInt MaxCntuLostFrmCntPt, HTInt CurNeedBufFrmCntPt, int IsAutoLock, long ErrInfoVstrPt );

	//清空音频自适应抖动缓冲器。
	private native int AAjbClear( long AAjbPt, int IsAutoLock, long ErrInfoVstrPt );
	//清空并重置音频自适应抖动缓冲器。
	private native int AAjbReset( long AAjbPt, int IsAutoLock, long ErrInfoVstrPt );
}