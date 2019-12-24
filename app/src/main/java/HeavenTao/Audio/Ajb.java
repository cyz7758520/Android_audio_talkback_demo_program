package HeavenTao.Audio;

import HeavenTao.Data.*;

//自适应抖动缓冲器类。
public class Ajb
{
    private long m_pvPoint; //存放自适应抖动缓冲器结构体的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Ajb" ); //加载libAjb.so。
    }

    //构造函数。
    public Ajb()
    {
        m_pvPoint = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //获取自适应抖动缓冲器结构体的内存指针。
    public long GetPoint()
    {
        return m_pvPoint;
    }

    //创建并初始化自适应抖动缓冲器。
    public native long Init( int i32SamplingRate, long i64FrameLength, byte i8IsHaveTimeStamp, byte i8InactiveIsContinuePut, int i32MinNeedBufferFrameCount, int i32MaxNeedBufferFrameCount, byte i8AdaptiveSensitivity );

    //放入一个字节型帧到自适应抖动缓冲器。
    public native long PutOneByteFrame( int i32TimeStamp, byte pszi8Frame[], long i64FrameLength );

    //放入一个短整型帧到自适应抖动缓冲器。
    public native long PutOneShortFrame( int i32TimeStamp, short pszi16Frame[], long i64FrameLength );

    //从自适应抖动缓冲器取出一个字节型帧。
    public native long GetOneByteFrame( byte pszi8Frame[], long i64FrameSize, HTLong pclFrameLength );

    //从自适应抖动缓冲器取出一个短整型帧。
    public native long GetOneShortFrame( short pszi16Frame[], long i64FrameSize, HTLong pclFrameLength );

    //获取当前已缓冲有活动帧的数量。
    public native long GetCurHaveBufferActiveFrameCount( HTInteger pclFrameCount );

    //获取当前已缓冲无活动帧的数量。
    public native long GetCurHaveBufferInactiveFrameCount( HTInteger pclFrameCount );

    //获取当前已缓冲帧的数量。
    public native long GetCurHaveBufferFrameCount( HTInteger pclFrameCount );

    //获取最大需缓冲帧的数量。
    public native long GetMaxNeedBufferFrameCount( HTInteger pclFrameCount );

    //获取最小需缓冲帧的数量。
    public native long GetMinNeedBufferFrameCount( HTInteger pclFrameCount );

    //获取当前需缓冲帧的数量。
    public native long GetCurNeedBufferFrameCount( HTInteger pclFrameCount );

    //清空自适应抖动缓冲器。
    public native long Clear();

    //销毁自适应抖动缓冲器。
    public native long Destory();
}