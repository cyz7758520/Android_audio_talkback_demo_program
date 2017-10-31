package HeavenTao.Audio;

public class WebRtcNsx //WebRtc定点版噪音抑制器类
{
    private Long clWebRtcNsx; //WebRtc定点版噪音抑制器的内存指针

    //构造函数
    public WebRtcNsx()
    {
        clWebRtcNsx = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        clWebRtcNsx = null;
    }

    //初始化WebRtc定点版噪音抑制器
    public int Init( int iSamplingRate, int iPolicyMode )
    {
        if( clWebRtcNsx.longValue() == 0)//如果WebRtc定点版噪音抑制器还没有初始化
        {
            return WebRtcNsxInit( clWebRtcNsx, iSamplingRate, iPolicyMode );
        }
        else//如果WebRtc定点版噪音抑制器已经初始化
        {
            return 0;
        }
    }

    //获取WebRtc定点版噪音抑制器的内存指针
    public Long GetWebRtcNsx()
    {
        return clWebRtcNsx;
    }

    //对一帧音频输入数据进行WebRtc定点噪音抑制
    public int Process( int iSamplingRate, short clAudioData[], int iAudioDataSize )
    {
        return WebRtcNsxProcess( clWebRtcNsx, iSamplingRate, clAudioData, iAudioDataSize );
    }

    //销毁WebRtc定点版噪音抑制器
    public void Destory()
    {
        WebRtcNsxDestory( clWebRtcNsx);
    }

    private native int WebRtcNsxInit( Long clWebRtcNsx, int iSamplingRate, int iMode );
    private native int WebRtcNsxProcess( Long clWebRtcNsx, int iSamplingRate, short clAudioData[], int iAudioDataSize );
    private native void WebRtcNsxDestory( Long clWebRtcNsx );
}
