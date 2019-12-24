package HeavenTao.Audio;

import HeavenTao.Data.*;

//WebRtc浮点版声学回音消除器类。
public class WebRtcAec
{
    private long m_pvPoint; //存放WebRtc浮点版声学回音消除器结构体的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    //构造函数。
    public WebRtcAec()
    {
        m_pvPoint = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //获取WebRtc浮点版声学回音消除器结构体的内存指针。
    public long GetPoint()
    {
        return m_pvPoint;
    }

    //创建并初始化WebRtc浮点版声学回音消除器。
    public native long Init( int i32SamplingRate, long i64FrameLength, int i32EchoMode, int i32Delay, int i32IsUseDelayAgnosticMode, int i32IsUseAdaptiveAdjustDelay );

    //根据WebRtc浮点版声学回音消除器内存块来创建并初始化WebRtc浮点版声学回音消除器。
    public native long InitFromMemory( byte pszi8WebRtcAecMemory[], long i64WebRtcAecMemoryLength );

    //获取WebRtc浮点版声学回音消除器内存块的数据长度。
    public native long GetMemoryLength( HTLong pclWebRtcAecMemoryLength );

    //获取WebRtc浮点版声学回音消除器的内存块。
    public native long GetMemory( byte pszi8WebRtcAecMemory[], long i64WebRtcAecMemorySize );

    //设置WebRtc浮点版声学回音消除器的回音延迟。
    public native long SetDelay( int i32Delay );

    //获取WebRtc浮点版声学回音消除器的回音延迟。
    public native long GetDelay( HTInteger pclDelay );

    //用WebRtc浮点版声学回音消除器对单声道16位有符号整型PCM格式输入帧进行WebRtc浮点版声学回音消除。
    public native long Process( short pszi16InputFrame[], short pszi16OutputFrame[], short pszi16ResultFrame[] );

    //销毁WebRtc浮点版声学回音消除器。
    public native long Destory();
}