package HeavenTao.Audio;

//WebRtc声学回音消除器类。
public class WebRtcAec
{
    private Long pclWebRtcAecInst; //存放WebRtc声学回音消除器的指针。
    private int m_i32SamplingRate; //存放音频数据的采样频率，包括：8000Hz、16000Hz、32000Hz。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "WebRtcAec" ); //加载libWebRtcAec.so。
    }

    //构造函数
    public WebRtcAec()
    {
        pclWebRtcAecInst = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        Destory(); //销毁WebRtcAec声学回音消除器。

        pclWebRtcAecInst = null;
    }

    //初始化WebRtc声学回音消除器。
    public long Init( int i32SamplingRate, int i32NlpMode )
    {
        m_i32SamplingRate = i32SamplingRate;
        if( pclWebRtcAecInst.longValue() == 0 ) //如果WebRtc声学回音消除器还没有初始化。
        {
            return WebRtcAecInit( pclWebRtcAecInst, i32SamplingRate, i32NlpMode );
        }
        else //如果WebRtc声学回音消除器已经初始化。
        {
            return 0;
        }
    }

    //获取WebRtc声学回音消除器的指针。
    public Long GetWebRtcAecInst()
    {
        return pclWebRtcAecInst;
    }

    //对一个单声道16位有符号整型PCM格式音频输入数据帧进行WebRtc声学回音消除。
    public long Echo( short pszi16AudioInputDataFrame[], short pszi16AudioOutputDataFrame[], short pszi16AudioResultDataFrame[] )
    {
        return WebRtcAecEcho( pclWebRtcAecInst, pszi16AudioInputDataFrame, pszi16AudioOutputDataFrame, pszi16AudioResultDataFrame, m_i32SamplingRate, pszi16AudioInputDataFrame.length );
    }

    //销毁WebRtcAec声学回音消除器。
    public long Destory()
    {
        return WebRtcAecDestory( pclWebRtcAecInst );
    }

    public native long WebRtcAecInit( Long pclWebRtcAecInst, int i32SamplingRate, int i32NlpMode );
    public native long WebRtcAecEcho( Long pclWebRtcAecInst, short pszi16AudioInputDataFrame[], short pszi16AudioOutputDataFrame[], short pszi16AudioResultDataFrame[], int i32SamplingRate, int i32FrameSize );
    public native long WebRtcAecDestory( Long pclWebRtcAecInst );
}