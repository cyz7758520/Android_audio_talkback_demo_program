package HeavenTao.Ado;

import HeavenTao.Data.*;

//音频自适应抖动缓冲器类。
public class AAjb
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
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

    //创建并初始化音频自适应抖动缓冲器。
    public int Init( int SmplRate, int FrmLen, int IsHaveTimeStamp, int TimeStampStep, int InactIsContPut, int MinNeedBufFrmCnt, int MaxNeedBufFrmCnt, int MaxCntuLostFrmCnt, float AdaptSensitivity, Vstr ErrInfoVstrPt )
    {
        if( m_AAjbPt == 0 )
        {
            HTLong p_AAjbPt = new HTLong();
            if( AAjbInit( p_AAjbPt, SmplRate, FrmLen, IsHaveTimeStamp, TimeStampStep, InactIsContPut, MinNeedBufFrmCnt, MaxNeedBufFrmCnt, MaxCntuLostFrmCnt, AdaptSensitivity, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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

    //放入一个字节型帧到音频自适应抖动缓冲器。
    public int PutOneByteFrm( int TimeStamp, byte ByteFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return AAjbPutOneByteFrm( m_AAjbPt, TimeStamp, ByteFrmPt, FrmStart, FrmLen, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //放入一个短整型帧到音频自适应抖动缓冲器。
    public int PutOneShortFrm( int TimeStamp, short ShortFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return AAjbPutOneShortFrm( m_AAjbPt, TimeStamp, ShortFrmPt, FrmStart, FrmLen, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //从音频自适应抖动缓冲器取出一个字节型帧。
    public int GetOneByteFrm( HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmSz, HTLong FrmLenPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return AAjbGetOneByteFrm( m_AAjbPt, TimeStampPt, ByteFrmPt, FrmStart, FrmSz, FrmLenPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //从音频自适应抖动缓冲器取出一个短整型帧。
    public int GetOneShortFrm( HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmSz, HTLong FrmLenPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return AAjbGetOneShortFrm( m_AAjbPt, TimeStampPt, ShortFrmPt, FrmStart, FrmSz, FrmLenPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //获取缓冲帧的数量。
    public int GetBufFrmCnt( HTInt CurHaveBufActFrmCntPt, HTInt CurHaveBufInactFrmCntPt, HTInt CurHaveBufFrmCntPt, HTInt MinNeedBufFrmCntPt, HTInt MaxNeedBufFrmCntPt, HTInt MaxCntuLostFrmCntPt, HTInt CurNeedBufFrmCntPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return AAjbGetBufFrmCnt( m_AAjbPt, CurHaveBufActFrmCntPt, CurHaveBufInactFrmCntPt, CurHaveBufFrmCntPt, MinNeedBufFrmCntPt, MaxNeedBufFrmCntPt, MaxCntuLostFrmCntPt, CurNeedBufFrmCntPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //清空音频自适应抖动缓冲器。
    public int Clear( int IsAutoLockUnlock, Vstr ErrInfoVstrPt)
    {
        return AAjbClear( m_AAjbPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //销毁音频自适应抖动缓冲器。
    public int Dstoy( Vstr ErrInfoVstrPt)
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

    //创建并初始化音频自适应抖动缓冲器。
    public native int AAjbInit( HTLong AAjbPt, int SmplRate, int FrmLen, int IsHaveTimeStamp, int TimeStampStep, int InactIsContPut, int MinNeedBufFrmCnt, int MaxNeedBufFrmCnt, int MaxCntuLostFrmCnt, float AdaptSensitivity, long ErrInfoVstrPt );

    //放入一个字节型帧到音频自适应抖动缓冲器。
    public native int AAjbPutOneByteFrm( long AAjbPt, int TimeStamp, byte ByteFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //放入一个短整型帧到音频自适应抖动缓冲器。
    public native int AAjbPutOneShortFrm( long AAjbPt, int TimeStamp, short ShortFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //从音频自适应抖动缓冲器取出一个字节型帧。
    public native int AAjbGetOneByteFrm( long AAjbPt, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmSz, HTLong FrmLenPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //从音频自适应抖动缓冲器取出一个短整型帧。
    public native int AAjbGetOneShortFrm( long AAjbPt, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmSz, HTLong FrmLenPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //获取缓冲帧的数量。
    public native int AAjbGetBufFrmCnt( long AAjbPt, HTInt CurHaveBufActFrmCntPt, HTInt CurHaveBufInactFrmCntPt, HTInt CurHaveBufFrmCntPt, HTInt MinNeedBufFrmCntPt, HTInt MaxNeedBufFrmCntPt, HTInt MaxCntuLostFrmCntPt, HTInt CurNeedBufFrmCntPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //清空音频自适应抖动缓冲器。
    public native int AAjbClear( long AAjbPt, int IsAutoLockUnlock, long ErrInfoVstrPt);

    //销毁音频自适应抖动缓冲器。
    public native int AAjbDstoy( long AAjbPt, long ErrInfoVstrPt);
}