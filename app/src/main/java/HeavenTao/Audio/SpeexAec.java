package HeavenTao.Audio;

public class SpeexAec //Speex声学回音消除器类
{
    private Long clSpeexEchoState;//Speex声学回音消除器的内存指针

    //构造函数
    public SpeexAec()
    {
        clSpeexEchoState = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        Destory();
    }

    //初始化Speex声学回音消除器
    public int Init( int iFrameSize, int iSamplingRate, int iFilterLength )
    {
        if( clSpeexEchoState.longValue() == 0)//如果Speex声学回音消除器还没有初始化
        {
            return SpeexAecInit( clSpeexEchoState, iFrameSize, iSamplingRate, iFilterLength );
        }
        else//如果Speex声学回音消除器已经初始化
        {
            return 0;
        }
    }

    //获取Speex声学回音消除器的内存指针
    public Long GetSpeexEchoState()
    {
        return clSpeexEchoState;
    }

    //对一帧音频输入数据进行Speex声学回音消除
    public int Aec( short clAudioInput[], short clAudioOutput[], short clAudioResult[] )
    {
        return SpeexAecAec( clSpeexEchoState, clAudioInput, clAudioOutput, clAudioResult);
    }

    //销毁Speex声学回音消除器
    public void Destory()
    {
        SpeexAecDestory( clSpeexEchoState );
    }

    private native int SpeexAecInit( Long clSpeexEchoState, int iFrameSize, int iSamplingRate, int iFilterLength );
    private native int SpeexAecAec( Long clSpeexEchoState, short clAudioInput[], short clAudioOutput[], short clAudioResult[] );
    private native void SpeexAecDestory( Long clSpeexEchoState );
}