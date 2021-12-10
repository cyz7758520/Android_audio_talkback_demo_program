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
    public void finalize()
    {
        Dstoy( null );
    }

    //创建并初始化视频自适应抖动缓冲器。
    public int Init( int IsHaveTimeStamp, int MinNeedBufFrmCnt, int MaxNeedBufFrmCnt, float AdaptSensitivity, VarStr ErrInfoVarStrPt )
    {
        if( m_VAjbPt == 0 )
        {
            HTLong p_VAjbPt = new HTLong();
            if( VAjbInit( p_VAjbPt, IsHaveTimeStamp, MinNeedBufFrmCnt, MaxNeedBufFrmCnt, AdaptSensitivity, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
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
    public int PutOneByteFrm( long CurTime, int TimeStamp, byte ByteFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbPutOneByteFrm( m_VAjbPt, CurTime, TimeStamp, ByteFrmPt, FrmStart, FrmLen, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //放入一个短整型帧到视频自适应抖动缓冲器。
    public int PutOneShortFrm( long CurTime, int TimeStamp, short ShortFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbPutOneShortFrm( m_VAjbPt, CurTime, TimeStamp, ShortFrmPt, FrmStart, FrmLen, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public int GetOneByteFrm( long CurTime, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbGetOneByteFrm( m_VAjbPt, CurTime, TimeStampPt, ByteFrmPt, FrmStart, FrmStartSz, FrmLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public int GetOneShortFrm( long CurTime, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbGetOneShortFrm( m_VAjbPt, CurTime, TimeStampPt, ShortFrmPt, FrmStart, FrmStartSz, FrmLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public int GetOneByteFrmWantTimeStamp( long CurTime, int WantTimeStamp, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbGetOneByteFrmWantTimeStamp( m_VAjbPt, CurTime, WantTimeStamp, TimeStampPt, ByteFrmPt, FrmStart, FrmStartSz, FrmLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public int GetOneShortFrmWantTimeStamp( long CurTime, int WantTimeStamp, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbGetOneShortFrmWantTimeStamp( m_VAjbPt, CurTime, WantTimeStamp, TimeStampPt, ShortFrmPt, FrmStart, FrmStartSz, FrmLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //获取缓冲帧的数量。
    public int GetBufFrmCnt( HTInt CurHaveBufFrmCntPt, HTInt MinNeedBufFrmCntPt, HTInt MaxNeedBufFrmCntPt, HTInt CurNeedBufFrmCntPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbGetBufFrmCnt( m_VAjbPt, CurHaveBufFrmCntPt, MinNeedBufFrmCntPt, MaxNeedBufFrmCntPt, CurNeedBufFrmCntPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //清空视频自适应抖动缓冲器。
    public int Clear( int IsAutoLockUnlock, VarStr ErrInfoVarStrPt)
    {
        return VAjbClear( m_VAjbPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //销毁视频自适应抖动缓冲器。
    public int Dstoy( VarStr ErrInfoVarStrPt)
    {
        if( m_VAjbPt != 0 )
        {
            if( VAjbDstoy( m_VAjbPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
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
    public native int VAjbInit( HTLong VAjbPt, int IsHaveTimeStamp, int MinNeedBufFrmCnt, int MaxNeedBufFrmCnt, float AdaptSensitivity, long ErrInfoVarStrPt );

    //放入一个字节型帧到视频自适应抖动缓冲器。
    public native int VAjbPutOneByteFrm( long VAjbPt, long CurTime, int TimeStamp, byte ByteFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //放入一个短整型帧到视频自适应抖动缓冲器。
    public native int VAjbPutOneShortFrm( long VAjbPt, long CurTime, int TimeStamp, short ShortFrmPt[], long FrmStart, long FrmLen, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public native int VAjbGetOneByteFrm( long VAjbPt, long CurTime, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public native int VAjbGetOneShortFrm( long VAjbPt, long CurTime, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public native int VAjbGetOneByteFrmWantTimeStamp( long VAjbPt, long CurTime, int WantTimeStamp, HTInt TimeStampPt, byte ByteFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public native int VAjbGetOneShortFrmWantTimeStamp( long VAjbPt, long CurTime, int WantTimeStamp, HTInt TimeStampPt, short ShortFrmPt[], long FrmStart, long FrmStartSz, HTLong FrmLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //获取缓冲帧的数量。
    public native int VAjbGetBufFrmCnt( long VAjbPt, HTInt CurHaveBufFrmCntPt, HTInt MinNeedBufFrmCntPt, HTInt MaxNeedBufFrmCntPt, HTInt CurNeedBufFrmCntPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //清空视频自适应抖动缓冲器。
    public native int VAjbClear( long VAjbPt, int IsAutoLockUnlock, long ErrInfoVarStrPt);

    //销毁视频自适应抖动缓冲器。
    public native int VAjbDstoy( long VAjbPt, long ErrInfoVarStrPt);
}