package HeavenTao.Video;

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
    public int Init( int IsHaveTimeStamp, int MinNeedBufFrameCnt, int MaxNeedBufFrameCnt, float AdaptSensitivity, VarStr ErrInfoVarStrPt )
    {
        if( m_VAjbPt == 0 )
        {
            HTLong p_VAjbPt = new HTLong();
            if( VAjbInit( p_VAjbPt, IsHaveTimeStamp, MinNeedBufFrameCnt, MaxNeedBufFrameCnt, AdaptSensitivity, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
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
    public int PutOneByteFrame( long CurTime, int TimeStamp, byte ByteFramePt[], long FrameStart, long FrameLen, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbPutOneByteFrame( m_VAjbPt, CurTime, TimeStamp, ByteFramePt, FrameStart, FrameLen, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //放入一个短整型帧到视频自适应抖动缓冲器。
    public int PutOneShortFrame( long CurTime, int TimeStamp, short ShortFramePt[], long FrameStart, long FrameLen, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbPutOneShortFrame( m_VAjbPt, CurTime, TimeStamp, ShortFramePt, FrameStart, FrameLen, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public int GetOneByteFrame( long CurTime, HTInt TimeStampPt, byte ByteFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbGetOneByteFrame( m_VAjbPt, CurTime, TimeStampPt, ByteFramePt, FrameStart, FrameStartSz, FrameLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public int GetOneShortFrame( long CurTime, HTInt TimeStampPt, short ShortFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbGetOneShortFrame( m_VAjbPt, CurTime, TimeStampPt, ShortFramePt, FrameStart, FrameStartSz, FrameLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public int GetOneByteFrameWantTimeStamp( long CurTime, int WantTimeStamp, HTInt TimeStampPt, byte ByteFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbGetOneByteFrameWantTimeStamp( m_VAjbPt, CurTime, WantTimeStamp, TimeStampPt, ByteFramePt, FrameStart, FrameStartSz, FrameLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public int GetOneShortFrameWantTimeStamp( long CurTime, int WantTimeStamp, HTInt TimeStampPt, short ShortFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbGetOneShortFrameWantTimeStamp( m_VAjbPt, CurTime, WantTimeStamp, TimeStampPt, ShortFramePt, FrameStart, FrameStartSz, FrameLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //获取缓冲帧的数量。
    public int GetBufFrameCnt( HTInt CurHaveBufFrameCntPt, HTInt MinNeedBufFrameCntPt, HTInt MaxNeedBufFrameCntPt, HTInt CurNeedBufFrameCntPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return VAjbGetBufFrameCnt( m_VAjbPt, CurHaveBufFrameCntPt, MinNeedBufFrameCntPt, MaxNeedBufFrameCntPt, CurNeedBufFrameCntPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
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
    public native int VAjbInit( HTLong VAjbPt, int IsHaveTimeStamp, int MinNeedBufFrameCnt, int MaxNeedBufFrameCnt, float AdaptSensitivity, long ErrInfoVarStrPt );

    //放入一个字节型帧到视频自适应抖动缓冲器。
    public native int VAjbPutOneByteFrame( long VAjbPt, long CurTime, int TimeStamp, byte ByteFramePt[], long FrameStart, long FrameLen, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //放入一个短整型帧到视频自适应抖动缓冲器。
    public native int VAjbPutOneShortFrame( long VAjbPt, long CurTime, int TimeStamp, short ShortFramePt[], long FrameStart, long FrameLen, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public native int VAjbGetOneByteFrame( long VAjbPt, long CurTime, HTInt TimeStampPt, byte ByteFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public native int VAjbGetOneShortFrame( long VAjbPt, long CurTime, HTInt TimeStampPt, short ShortFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public native int VAjbGetOneByteFrameWantTimeStamp( long VAjbPt, long CurTime, int WantTimeStamp, HTInt TimeStampPt, byte ByteFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public native int VAjbGetOneShortFrameWantTimeStamp( long VAjbPt, long CurTime, int WantTimeStamp, HTInt TimeStampPt, short ShortFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //获取缓冲帧的数量。
    public native int VAjbGetBufFrameCnt( long VAjbPt, HTInt CurHaveBufFrameCntPt, HTInt MinNeedBufFrameCntPt, HTInt MaxNeedBufFrameCntPt, HTInt CurNeedBufFrameCntPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //清空视频自适应抖动缓冲器。
    public native int VAjbClear( long VAjbPt, int IsAutoLockUnlock, long ErrInfoVarStrPt);

    //销毁视频自适应抖动缓冲器。
    public native int VAjbDstoy( long VAjbPt, long ErrInfoVarStrPt);
}