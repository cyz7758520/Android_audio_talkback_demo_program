package com.example.Android_audio_talkback_demo_program;

public class WebRtcAecm //WebRtc移动版声学回音消除器类
{
    private Long clWebRtcAecmInst;//WebRtc移动版声学回音消除器的内存指针
    public int m_iDelay;//WebRtc移动版声学回音消除器的回音延迟时间，单位毫秒

    //构造函数
    public WebRtcAecm()
    {
        clWebRtcAecmInst = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        clWebRtcAecmInst = null;
    }

    //初始化WebRtc移动版声学回音消除器
    public int Init( int iSamplingRate, int iEchoMode, int iDelay )
    {
        m_iDelay = iDelay;
        if( clWebRtcAecmInst.longValue() == 0 )//如果WebRtc移动版声学回音消除器还没有初始化
        {
            return WebRtcAecmInit( clWebRtcAecmInst, iSamplingRate, iEchoMode );
        }
        else//如果WebRtc移动版声学回音消除器已经初始化
        {
            return 0;
        }
    }

    //获取WebRtc移动版声学回音消除器的内存指针
    public Long GetWebRtcAecmInst()
    {
        return clWebRtcAecmInst;
    }

    //对一帧音频输入数据进行WebRtc移动版声学回音消除
    public int Echo( short AudioIn[], short AudioOut[], short AudioResult[] )
    {
        return WebRtcAecmEcho( clWebRtcAecmInst, AudioIn, AudioOut, AudioResult, AudioIn.length, m_iDelay );
    }

    //销毁WebRtc移动版声学回音消除器
    public void Destory()
    {
        WebRtcAecmDestory( clWebRtcAecmInst );
    }

    private native int WebRtcAecmInit( Long clWebRtcAecmInst, int iSamplingRate, int iEchoMode );
    private native int WebRtcAecmEcho( Long clWebRtcAecmInst, short AudioIn[], short AudioOut[], short AudioResult[], int iFrameSize, int iDelay );
    private native void WebRtcAecmDestory( Long clWebRtcAecmInst );
}