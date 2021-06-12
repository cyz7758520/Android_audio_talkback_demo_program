package HeavenTao.Audio;

import HeavenTao.Data.*;

//音频自适应抖动缓冲器类。
public class AAjb
{
    private long m_AAjbPt; //存放音频自适应抖动缓冲器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "Ajb" ); //加载libAjb.so。
    }

    //构造函数。
    public AAjb()
    {
        m_AAjbPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取音频自适应抖动缓冲器的内存指针。
    public long GetAAjbPt()
    {
        return m_AAjbPt;
    }

    //创建并初始化音频自适应抖动缓冲器。
    public native int Init( int SamplingRate, int FrameLen, int IsHaveTimeStamp, int TimeStampStep, int InactIsContPut, int MinNeedBufFrameCnt, int MaxNeedBufFrameCnt, float AdaptSensitivity, int IsUseMutexLock );

    //放入一个字节型帧到音频自适应抖动缓冲器。
    public native int PutOneByteFrame( int TimeStamp, byte ByteFramePt[], long FrameStart, long FrameLen );

    //放入一个短整型帧到音频自适应抖动缓冲器。
    public native int PutOneShortFrame( int TimeStamp, short ShortFramePt[], long FrameStart, long FrameLen );

    //从音频自适应抖动缓冲器取出一个字节型帧。
    public native int GetOneByteFrame( HTInt TimeStampPt, byte ByteFramePt[], long FrameStart, long FrameSz, HTLong FrameLenPt );

    //从音频自适应抖动缓冲器取出一个短整型帧。
    public native int GetOneShortFrame( HTInt TimeStampPt, short ShortFramePt[], long FrameStart, long FrameSz, HTLong FrameLenPt );

    //获取缓冲帧的数量。
    public native int GetBufFrameCnt( HTInt CurHaveBufActFrameCntPt, HTInt CurHaveBufInactFrameCntPt, HTInt CurHaveBufFrameCntPt, HTInt MinNeedBufFrameCntPt, HTInt MaxNeedBufFrameCntPt, HTInt CurNeedBufFrameCntPt );

    //清空音频自适应抖动缓冲器。
    public native int Clear();

    //销毁音频自适应抖动缓冲器。
    public native int Destroy();
}