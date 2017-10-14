package HeavenTao.Audio;

public class WebRtcAec//WebRtc声学回音消除器类
{
    private Long clWebRtcAecInst;//WebRtc声学回音消除器的内存指针

    //构造函数
    public WebRtcAec()
    {
        clWebRtcAecInst = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        clWebRtcAecInst = null;
    }

    //初始化WebRtc声学回音消除器
    public int Init( int iSamplingRate, int iNlpMode )
    {
        if( clWebRtcAecInst.longValue() == 0 )//如果WebRtc声学回音消除器还没有初始化
        {
            return WebRtcAecInit( clWebRtcAecInst, iSamplingRate, iNlpMode );
        }
        else//如果WebRtc声学回音消除器已经初始化
        {
            return 0;
        }
    }

    //获取WebRtc声学回音消除器的内存指针
    public Long GetWebRtcAecInst()
    {
        return clWebRtcAecInst;
    }

    //对一帧音频输入数据进行WebRtcAec声学回音消除
    public int Echo( short AudioIn[], short AudioOut[], short AudioResult[] )
    {
        return WebRtcAecEcho( clWebRtcAecInst, AudioIn, AudioOut, AudioResult, AudioIn.length );
    }

    //销毁WebRtcAec声学回音消除句柄
    public void Destory()
    {
        WebRtcAecDestory( clWebRtcAecInst );
    }

    public native int WebRtcAecInit( Long clWebRtcAecInst, int iSamplingRate, int iNlpMode );
    public native int WebRtcAecEcho( Long clWebRtcAecInst, short AudioIn[], short AudioOut[], short AudioResult[], int iFrameSize );
    public native void WebRtcAecDestory( Long clWebRtcAecInst );
}