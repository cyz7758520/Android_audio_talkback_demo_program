package HeavenTao.Audio;

import HeavenTao.Data.*;

//SpeexWebRtc三重声学回音消除器类。
public class SpeexWebRtcAec
{
    private long m_pvPoint; //存放SpeexWebRtc三重声学回音消除器结构体的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "SpeexDsp" ); //加载libSpeex.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
        System.loadLibrary( "SpeexWebRtcAec" ); //加载libSpeexWebRtcAec.so。
    }

    //构造函数。
    public SpeexWebRtcAec()
    {
        m_pvPoint = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //获取SpeexWebRtc三重声学回音消除器结构体的内存指针。
    public long GetPoint()
    {
        return m_pvPoint;
    }

    //创建并初始化SpeexWebRtc三重声学回音消除器。
    public native long Init( int i32SamplingRate, long i64FrameLength, int i32WorkMode, int i32SpeexAecFilterLength, float fSpeexAecEchoMultiple, int i32SpeexAecEchoSuppress, int i32SpeexAecEchoSuppressActive,  int i32WebRtcAecmIsUseCNGMode, int i32WebRtcAecmEchoMode, int i32WebRtcAecmDelay, int i32WebRtcAecEchoMode, int i32WebRtcAecDelay, int i32WebRtcAecIsUseDelayAgnostic, int i32WebRtcAecIsUseAdaptiveAdjustDelay );

    //设置SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
    public native long SetWebRtcAecmDelay( int i32WebRtcAecmDelay );

    //获取SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
    public native long GetWebRtcAecmDelay( HTInteger pclWebRtcAecmDelay );

    //设置SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
    public native long SetWebRtcAecDelay( int i32WebRtcAecDelay );

    //获取SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
    public native long GetWebRtcAecDelay( HTInteger pclWebRtcAecDelay );

    //用SpeexWebRtc三重声学回音消除器对单声道16位有符号整型PCM格式输入帧进行SpeexWebRtc三重声学回音消除。
    public native long Process( short pszi16InputFrame[], short pszi16OutputFrame[], short pszi16ResultFrame[] );

    //销毁SpeexWebRtc三重声学回音消除器。
    public native long Destory();
}