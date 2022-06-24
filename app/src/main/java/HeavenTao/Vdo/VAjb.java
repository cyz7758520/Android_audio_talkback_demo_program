package HeavenTao.Vdo;

import HeavenTao.Data.*;

//视频自适应抖动缓冲器类。
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

    //放入一个字节型帧到视频自适应抖动缓冲器。
    public int PutOneByteFrm( long CurTime, int TimeStamp, byte ByteFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return VAjbPutOneByteFrm( m_VAjbPt, CurTime, TimeStamp, ByteFrmPt, FrmStart, FrmLen, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //放入一个短整型帧到视频自适应抖动缓冲器。
    public int PutOneShortFrm( long CurTime, int TimeStamp, short ShortFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return VAjbPutOneShortFrm( m_VAjbPt, CurTime, TimeStamp, ShortFrmPt, FrmStart, FrmLen, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public int GetOneByteFrm( long CurTime, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return VAjbGetOneByteFrm( m_VAjbPt, CurTime, TimeStampPt, ByteFrmPt, FrmStart, FrmStartSz, FrmLenPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public int GetOneShortFrm( long CurTime, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return VAjbGetOneShortFrm( m_VAjbPt, CurTime, TimeStampPt, ShortFrmPt, FrmStart, FrmStartSz, FrmLenPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public int GetOneByteFrmWantTimeStamp( long CurTime, int WantTimeStamp, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return VAjbGetOneByteFrmWantTimeStamp( m_VAjbPt, CurTime, WantTimeStamp, TimeStampPt, ByteFrmPt, FrmStart, FrmStartSz, FrmLenPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public int GetOneShortFrmWantTimeStamp( long CurTime, int WantTimeStamp, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return VAjbGetOneShortFrmWantTimeStamp( m_VAjbPt, CurTime, WantTimeStamp, TimeStampPt, ShortFrmPt, FrmStart, FrmStartSz, FrmLenPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //获取缓冲帧的数量。
    public int GetBufFrmCnt( HTInt CurHaveBufFrmCntPt, HTInt MinNeedBufFrmCntPt, HTInt MaxNeedBufFrmCntPt, HTInt CurNeedBufFrmCntPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return VAjbGetBufFrmCnt( m_VAjbPt, CurHaveBufFrmCntPt, MinNeedBufFrmCntPt, MaxNeedBufFrmCntPt, CurNeedBufFrmCntPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //清空视频自适应抖动缓冲器。
    public int Clear( int IsAutoLockUnlock, Vstr ErrInfoVstrPt)
    {
        return VAjbClear( m_VAjbPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
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

    //放入一个字节型帧到视频自适应抖动缓冲器。
    public native int VAjbPutOneByteFrm( long VAjbPt, long CurTime, int TimeStamp, byte ByteFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //放入一个短整型帧到视频自适应抖动缓冲器。
    public native int VAjbPutOneShortFrm( long VAjbPt, long CurTime, int TimeStamp, short ShortFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public native int VAjbGetOneByteFrm( long VAjbPt, long CurTime, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public native int VAjbGetOneShortFrm( long VAjbPt, long CurTime, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public native int VAjbGetOneByteFrmWantTimeStamp( long VAjbPt, long CurTime, int WantTimeStamp, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public native int VAjbGetOneShortFrmWantTimeStamp( long VAjbPt, long CurTime, int WantTimeStamp, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //获取缓冲帧的数量。
    public native int VAjbGetBufFrmCnt( long VAjbPt, HTInt CurHaveBufFrmCntPt, HTInt MinNeedBufFrmCntPt, HTInt MaxNeedBufFrmCntPt, HTInt CurNeedBufFrmCntPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //清空视频自适应抖动缓冲器。
    public native int VAjbClear( long VAjbPt, int IsAutoLockUnlock, long ErrInfoVstrPt);

    //销毁视频自适应抖动缓冲器。
    public native int VAjbDstoy( long VAjbPt, long ErrInfoVstrPt);
}