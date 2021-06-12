package HeavenTao.Video;

import HeavenTao.Data.*;

//视频自适应抖动缓冲器类。
public class VAjb
{
    private long m_VAjbPt; //存放视频自适应抖动缓冲器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "Ajb" ); //加载libAjb.so。
    }

    //构造函数。
    public VAjb()
    {
        m_VAjbPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取视频自适应抖动缓冲器的内存指针。
    public long GetVAjbPt()
    {
        return m_VAjbPt;
    }

    //创建并初始化视频自适应抖动缓冲器。
    public native int Init( int IsHaveTimeStamp, int MinNeedBufFrameCnt, int MaxNeedBufFrameCnt, float AdaptSensitivity, int IsUseMutexLock );

    //放入一个字节型帧到视频自适应抖动缓冲器。
    public native int PutOneByteFrame( long CurTime, int TimeStamp, byte ByteFramePt[], long FrameStart, long FrameLen );

    //放入一个短整型帧到视频自适应抖动缓冲器。
    public native int PutOneShortFrame( long CurTime, int TimeStamp, short ShortFramePt[], long FrameStart, long FrameLen );

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public native int GetOneByteFrame( long CurTime, HTInt TimeStampPt, byte ByteFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt );

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public native int GetOneShortFrame( long CurTime, HTInt TimeStampPt, short ShortFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt );

    //从视频自适应抖动缓冲器取出一个字节型帧。
    public native int GetOneByteFrameWantTimeStamp( long CurTime, int WantTimeStamp, HTInt TimeStampPt, byte ByteFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt );

    //从视频自适应抖动缓冲器取出一个短整型帧。
    public native int GetOneShortFrameWantTimeStamp( long CurTime, int WantTimeStamp, HTInt TimeStampPt, short ShortFramePt[], long FrameStart, long FrameStartSz, HTLong FrameLenPt );

    //获取缓冲帧的数量。
    public native int GetBufFrameCnt( HTInt CurHaveBufFrameCntPt, HTInt MinNeedBufFrameCntPt, HTInt MaxNeedBufFrameCntPt, HTInt CurNeedBufFrameCntPt );

    //清空视频自适应抖动缓冲器。
    public native int Clear();

    //销毁视频自适应抖动缓冲器。
    public native int Destroy();
}