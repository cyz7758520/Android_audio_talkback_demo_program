package HeavenTao.Audio;

import HeavenTao.Data.*;

//WebRtc定点版声学回音消除器类。
public class WebRtcAecm
{
    private long m_pvPoint; //存放WebRtc定点版声学回音消除器结构体的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    //构造函数。
    public WebRtcAecm()
    {
        m_pvPoint = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //获取WebRtc定点版声学回音消除器结构体的内存指针。
    public long GetPoint()
    {
        return m_pvPoint;
    }

    //创建并初始化WebRtc定点版声学回音消除器。
    public native long Init( int i32SamplingRate, long i64FrameLength, int i32IsUseCNGMode, int i32EchoMode, int i32Delay );

    //设置WebRtc定点版声学回音消除器的回音延迟。
    public native long SetDelay( int i32Delay );

    //获取WebRtc定点版声学回音消除器的回音延迟。
    public native long GetDelay( HTInteger pclDelay );

    //用WebRtc定点版声学回音消除器对单声道16位有符号整型PCM格式输入帧进行WebRtc定点版声学回音消除。
    public native long Process( short pszi16InputFrame[], short pszi16OutputFrame[], short pszi16ResultFrame[] );

    //销毁WebRtc定点版声学回音消除器。
    public native long Destory();
}