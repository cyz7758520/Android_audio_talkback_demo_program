package HeavenTao.Audio;

//WebRtc移动版声学回音消除器类。
public class WebRtcAecm
{
    private Long m_pclWebRtcAecmInst; //存放WebRtc移动版声学回音消除器的指针。
    private int m_i32SamplingRate; //存放音频数据的采样频率，包括：8000Hz、16000Hz。
    public int m_i32Delay; //存放WebRtc移动版声学回音消除器的回音延迟时间，单位毫秒。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "WebRtcAecm" ); //加载libWebRtcAecm.so。
    }

    //构造函数。
    public WebRtcAecm()
    {
        m_pclWebRtcAecmInst = new Long(0);
    }

    //析构函数。
    public void finalize()
    {
        Destory(); //销毁WebRtc移动版声学回音消除器。

        m_pclWebRtcAecmInst = null;
    }

    //初始化WebRtc移动版声学回音消除器。
    public long Init( int i32SamplingRate, int i32EchoMode, int i32Delay )
    {
        m_i32SamplingRate = i32SamplingRate;
        m_i32Delay = i32Delay;
        if( m_pclWebRtcAecmInst.longValue() == 0 ) //如果WebRtc移动版声学回音消除器还没有初始化。
        {
            return WebRtcAecmInit( m_pclWebRtcAecmInst, i32SamplingRate, i32EchoMode );
        }
        else //如果WebRtc移动版声学回音消除器已经初始化。
        {
            return 0;
        }
    }

    //获取WebRtc移动版声学回音消除器的指针。
    public Long GetWebRtcAecmInst()
    {
        return m_pclWebRtcAecmInst;
    }

    //对一个单声道16位有符号整型PCM格式音频输入数据帧进行WebRtc移动版声学回音消除。
    public long Echo( short pszi16AudioInputDataFrame[], short pszi16AudioOutputDataFrame[], short pszi16AudioResultDataFrame[] )
    {
        return WebRtcAecmEcho( m_pclWebRtcAecmInst, pszi16AudioInputDataFrame, pszi16AudioOutputDataFrame, pszi16AudioResultDataFrame, m_i32SamplingRate, pszi16AudioInputDataFrame.length, m_i32Delay );
    }

    //销毁WebRtc移动版声学回音消除器。
    public long Destory()
    {
        return WebRtcAecmDestory( m_pclWebRtcAecmInst );
    }

    private native long WebRtcAecmInit( Long pclWebRtcAecmInst, int i32SamplingRate, int i32EchoMode );
    private native long WebRtcAecmEcho( Long pclWebRtcAecmInst, short pszi16AudioInputDataFrame[], short pszi16AudioOutputDataFrame[], short pszi16AudioResultDataFrame[], int i32SamplingRate, int i32FrameSize, int i32Delay );
    private native long WebRtcAecmDestory( Long pclWebRtcAecmInst );
}