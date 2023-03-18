package HeavenTao.Vdo;

import HeavenTao.Data.*;

//视频自适应抖动缓冲器。
public class VAjb
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "Ajb" ); //加载libAjb.so。
    }

    public long m_VAjbPt; //存放视频自适应抖动缓冲器的指针。

    //构造函数。
    public VAjb()
    {
        m_VAjbPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy( null );
    }

    //创建并初始化视频自适应抖动缓冲器。
    public int Init( int IsHaveTimeStamp, int MinNeedBufFrmCnt, int MaxNeedBufFrmCnt, float AdaptSensitivity, Vstr ErrInfoVstrPt )
    {
        if( m_VAjbPt == 0 )
        {
            HTLong p_VAjbPt = new HTLong();
            if( VAjbInit( p_VAjbPt, IsHaveTimeStamp, MinNeedBufFrmCnt, MaxNeedBufFrmCnt, AdaptSensitivity, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_VAjbPt = p_VAjbPt.m_Val;
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

    //放入字节型帧到视频自适应抖动缓冲器。
    public int PutByteFrm( long CurTime, int TimeStamp, byte ByteFrmPt[], long FrmStart, long FrmLen, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return VAjbPutByteFrm( m_VAjbPt, CurTime, TimeStamp, ByteFrmPt, FrmStart, FrmLen, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //放入短整型帧到视频自适应抖动缓冲器。
    public int PutShortFrm( long CurTime, int TimeStamp, short ShortFrmPt[], long FrmStart, long FrmLen, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return VAjbPutShortFrm( m_VAjbPt, CurTime, TimeStamp, ShortFrmPt, FrmStart, FrmLen, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出字节型帧。
    public int GetByteFrm( long CurTime, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return VAjbGetByteFrm( m_VAjbPt, CurTime, TimeStampPt, ByteFrmPt, FrmStart, FrmStartSz, FrmLenPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出短整型帧。
    public int GetShortFrm( long CurTime, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return VAjbGetShortFrm( m_VAjbPt, CurTime, TimeStampPt, ShortFrmPt, FrmStart, FrmStartSz, FrmLenPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //获取缓冲帧的数量。
    public int GetBufFrmCnt( HTInt CurHaveBufFrmCntPt, HTInt MinNeedBufFrmCntPt, HTInt MaxNeedBufFrmCntPt, HTInt CurNeedBufFrmCntPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return VAjbGetBufFrmCnt( m_VAjbPt, CurHaveBufFrmCntPt, MinNeedBufFrmCntPt, MaxNeedBufFrmCntPt, CurNeedBufFrmCntPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //清空视频自适应抖动缓冲器。
    public int Clear( int IsAutoLock, Vstr ErrInfoVstrPt)
    {
        return VAjbClear( m_VAjbPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //销毁视频自适应抖动缓冲器。
    public int Dstoy( Vstr ErrInfoVstrPt)
    {
        if( m_VAjbPt != 0 )
        {
            if( VAjbDstoy( m_VAjbPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_VAjbPt = 0;
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

    //创建并初始化视频自适应抖动缓冲器。
    public native int VAjbInit( HTLong VAjbPt, int IsHaveTimeStamp, int MinNeedBufFrmCnt, int MaxNeedBufFrmCnt, float AdaptSensitivity, long ErrInfoVstrPt );

    //放入字节型帧到视频自适应抖动缓冲器。
    public native int VAjbPutByteFrm( long VAjbPt, long CurTime, int TimeStamp, byte ByteFrmPt[], long FrmStart, long FrmLen, int IsAutoLock, long ErrInfoVstrPt );

    //放入短整型帧到视频自适应抖动缓冲器。
    public native int VAjbPutShortFrm( long VAjbPt, long CurTime, int TimeStamp, short ShortFrmPt[], long FrmStart, long FrmLen, int IsAutoLock, long ErrInfoVstrPt );

    //从视频自适应抖动缓冲器取出字节型帧。
    public native int VAjbGetByteFrm( long VAjbPt, long CurTime, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLock, long ErrInfoVstrPt );

    //从视频自适应抖动缓冲器取出短整型帧。
    public native int VAjbGetShortFrm( long VAjbPt, long CurTime, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLock, long ErrInfoVstrPt );

    //获取缓冲帧的数量。
    public native int VAjbGetBufFrmCnt( long VAjbPt, HTInt CurHaveBufFrmCntPt, HTInt MinNeedBufFrmCntPt, HTInt MaxNeedBufFrmCntPt, HTInt CurNeedBufFrmCntPt, int IsAutoLock, long ErrInfoVstrPt );

    //清空视频自适应抖动缓冲器。
    public native int VAjbClear( long VAjbPt, int IsAutoLock, long ErrInfoVstrPt);

    //销毁视频自适应抖动缓冲器。
    public native int VAjbDstoy( long VAjbPt, long ErrInfoVstrPt);
}