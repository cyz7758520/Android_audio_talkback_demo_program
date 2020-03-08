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
    public native int GetOneByteFrame( byte ByteFramePt[], long FrameSz, HTLong FrameLenPt );

    //从自适应抖动缓冲器取出一个短整型帧。
    public native int GetOneShortFrame( short ShortFramePt[], long FrameSz, HTLong FrameLenPt );

    //获取当前已缓冲有活动帧的数量。
    public native int GetCurHaveBufActFrameCnt( HTInt FrameCntPt );

    //获取当前已缓冲无活动帧的数量。
    public native int GetCurHaveBufInactFrameCnt( HTInt FrameCntPt );

    //获取当前已缓冲帧的数量。
    public native int GetCurHaveBufFrameCnt( HTInt FrameCntPt );

    //获取最大需缓冲帧的数量。
    public native int GetMaxNeedBufFrameCnt( HTInt FrameCntPt );

    //获取最小需缓冲帧的数量。
    public native int GetMinNeedBufFrameCnt( HTInt FrameCntPt );

    //获取当前需缓冲帧的数量。
    public native int GetCurNeedBufFrameCnt( HTInt FrameCntPt );

    //清空自适应抖动缓冲器。
    public native int Clear();

    //销毁自适应抖动缓冲器。
    public native int Destroy();
}