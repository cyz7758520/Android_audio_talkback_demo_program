package HeavenTao.Audio;

import HeavenTao.Data.*;

//自适应抖动缓冲器类。
public class Ajb
{
    private long m_AjbPt; //存放自适应抖动缓冲器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "Ajb" ); //加载libAjb.so。
    }

    //构造函数。
    public Ajb()
    {
        m_AjbPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取自适应抖动缓冲器的内存指针。
    public long GetAjbPt()
    {
        return m_AjbPt;
    }

    //创建并初始化自适应抖动缓冲器。
    public native int Init( int SamplingRate, int FrameLen, byte IsHaveTimeStamp, byte InactIsContPut, int MinNeedBufFrameCnt, int MaxNeedBufFrameCnt, byte AdaptSensitivity );

    //放入一个字节型帧到自适应抖动缓冲器。
    public native int PutOneByteFrame( int TimeStamp, byte ByteFramePt[], long FrameLen );

    //放入一个短整型帧到自适应抖动缓冲器。
    public native int PutOneShortFrame( int TimeStamp, short ShortFramePt[], long FrameLen );

    //从自适应抖动缓冲器取出一个字节型帧。
    public native int GetOneByteFrame( HTInt TimeStampPt, byte ByteFramePt[], long FrameSz, HTLong FrameLenPt );

    //从自适应抖动缓冲器取出一个短整型帧。
    public native int GetOneShortFrame( HTInt TimeStampPt, short ShortFramePt[], long FrameSz, HTLong FrameLenPt );

    //获取缓冲帧的数量。
    public native int GetBufFrameCnt( HTInt CurHaveBufActFrameCntPt, HTInt CurHaveBufInactFrameCntPt, HTInt CurHaveBufFrameCntPt, HTInt MinNeedBufFrameCntPt, HTInt MaxNeedBufFrameCntPt, HTInt CurNeedBufFrameCntPt );

    //清空自适应抖动缓冲器。
    public native int Clear();

    //销毁自适应抖动缓冲器。
    public native int Destroy();
}