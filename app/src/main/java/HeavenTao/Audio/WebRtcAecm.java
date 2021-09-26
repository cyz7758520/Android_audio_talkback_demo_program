package HeavenTao.Audio;

import HeavenTao.Data.*;

//WebRtc定点版声学回音消除器类。
public class WebRtcAecm
{
    private long m_WebRtcAecmPt; //存放WebRtc定点版声学回音消除器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    //构造函数。
    public WebRtcAecm()
    {
        m_WebRtcAecmPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取WebRtc定点版声学回音消除器的内存指针。
    public long GetWebRtcAecmPt()
    {
        return m_WebRtcAecmPt;
    }

    //创建并初始化WebRtc定点版声学回音消除器。
    public native int Init( int SamplingRate, int FrameLen, int IsUseCNGMode, int EchoMode, int Delay, VarStr ErrInfoVarStrPt );

    //设置WebRtc定点版声学回音消除器的回音延迟。
    public native int SetDelay( int Delay );

    //获取WebRtc定点版声学回音消除器的回音延迟。
    public native int GetDelay( HTInt DelayPt );

    //用WebRtc定点版声学回音消除器对单声道16位有符号整型PCM格式输入帧进行WebRtc定点版声学回音消除。
    public native int Proc( short InputFramePt[], short OutputFramePt[], short ResultFramePt[] );

    //销毁WebRtc定点版声学回音消除器。
    public native int Destroy();
}