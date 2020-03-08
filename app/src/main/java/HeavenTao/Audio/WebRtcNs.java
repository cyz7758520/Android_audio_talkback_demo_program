package HeavenTao.Audio;

import HeavenTao.Data.*;

//WebRtc浮点版噪音抑制器类。
public class WebRtcNs
{
    private long m_WebRtcNsPt; //WebRtc浮点版噪音抑制器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    //构造函数。
    public WebRtcNs()
    {
        m_WebRtcNsPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取WebRtc浮点版噪音抑制器的内存指针。
    public long GetWebRtcNsPt()
    {
        return m_WebRtcNsPt;
    }

    //创建并初始化WebRtc浮点版噪音抑制器。
    public native int Init( int SamplingRate, int FrameLen, int PolicyMode );

    //用WebRtc浮点版噪音抑制器对单声道16位有符号整型PCM格式帧进行WebRtc浮点版噪音抑制。
    public native int Proc( short FrameObj[], short ResultFrameObj[] );

    //销毁WebRtc浮点版噪音抑制器。
    public native int Destroy();
}
