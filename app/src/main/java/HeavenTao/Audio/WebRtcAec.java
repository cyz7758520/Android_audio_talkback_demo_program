package HeavenTao.Audio;

import HeavenTao.Data.*;

//WebRtc浮点版声学回音消除器类。
public class WebRtcAec
{
    private long m_WebRtcAecPt; //存放WebRtc浮点版声学回音消除器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    //构造函数。
    public WebRtcAec()
    {
        m_WebRtcAecPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取WebRtc浮点版声学回音消除器的内存指针。
    public long GetWebRtcAecPt()
    {
        return m_WebRtcAecPt;
    }

    //创建并初始化WebRtc浮点版声学回音消除器。
    public native int Init( int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay );

    //根据WebRtc浮点版声学回音消除器内存块来创建并初始化WebRtc浮点版声学回音消除器。
    public native int InitByMem( byte WebRtcAecMemPt[], long WebRtcAecMemLen );

    //根据WebRtc浮点版声学回音消除器内存块文件来创建并初始化WebRtc浮点版声学回音消除器。
    public native int InitByMemFile( int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String WebRtcAecMemFileFullPathStrPt );

    //获取WebRtc浮点版声学回音消除器内存块的数据长度。
    public native int GetMemLen( HTLong WebrtcAecMemLenPt );

    //获取WebRtc浮点版声学回音消除器的内存块。
    public native int GetMem( byte WebRtcAecMemPt[], long WebRtcAecMemSz );

    //将WebRtc浮点版声学回音消除器内存块保存到指定的文件。
    public native int SaveMemFile( int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String WebRtcAecMemFileFullPathStrPt );

    //设置WebRtc浮点版声学回音消除器的回音延迟。
    public native int SetDelay( int Delay );

    //获取WebRtc浮点版声学回音消除器的回音延迟。
    public native int GetDelay( HTInt DelayPt );

    //用WebRtc浮点版声学回音消除器对单声道16位有符号整型PCM格式输入帧进行WebRtc浮点版声学回音消除。
    public native int Proc( short InputFramePt[], short OutputFramePt[], short ResultFramePt[] );

    //销毁WebRtc浮点版声学回音消除器。
    public native int Destroy();
}