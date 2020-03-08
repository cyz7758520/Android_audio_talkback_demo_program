package HeavenTao.Audio;

import HeavenTao.Data.*;

//WebRtc定点版噪音抑制器类。
public class WebRtcNsx
{
    private long m_WebRtcNsxPt; //WebRtc定点版噪音抑制器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    //构造函数。
    public WebRtcNsx()
    {
        m_WebRtcNsxPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取WebRtc定点版噪音抑制器的内存指针。
    public long GetWebRtcNsxPt()
    {
        return m_WebRtcNsxPt;
    }

    //创建并初始化WebRtc定点版噪音抑制器。
    public native int Init( int SamplingRate, int FrameLen, int PolicyMode );

    //用WebRtc定点版噪音抑制器对单声道16位有符号整型PCM格式帧进行WebRtc定点版噪音抑制。
    public native int Proc( short FramePt[], short ResultFramePt[] );

    //销毁WebRtc定点版噪音抑制器。
    public native int Destroy();
}
