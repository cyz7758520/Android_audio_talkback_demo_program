package HeavenTao.Audio;

public class SpeexDecoder//Speex解码器类
{
    private Long clSpeexDecoderState;//Speex解码器的内存指针
    private Long clSpeexBits;//SpeexBits的内存指针

    //构造函数
    public SpeexDecoder()
    {
        clSpeexDecoderState = new Long(0);
        clSpeexBits = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        Destory();
    }

    public int Init( int iSamplingRate )//初始化Speex解码器
    {
        if( clSpeexDecoderState.longValue() == 0)//如果Speex编码器还没有初始化
        {
            return SpeexDecoderInit( clSpeexDecoderState, clSpeexBits, iSamplingRate );
        }
        else//如果Speex编码器已经初始化
        {
            return 0;
        }
    }

    public Long GetSpeexDecoderState()//获取Speex解码器的内存指针
    {
        return clSpeexDecoderState;
    }

    public int Decode( byte clAudioDecodeBefore[], int iAudioDecodeBeforeSize, short clAudioDecodeAfter[] )//对一帧Speex音频数据进行解码
    {
        return SpeexDecoderDecode( clSpeexDecoderState, clSpeexBits, clAudioDecodeBefore, iAudioDecodeBeforeSize, clAudioDecodeAfter );
    }

    public void Destory()//销毁Speex解码器
    {
        SpeexDecoderDestory( clSpeexDecoderState, clSpeexBits );
        clSpeexDecoderState = new Long(0);
        clSpeexBits = new Long(0);
    }

    private native int SpeexDecoderInit( Long clSpeexDecoderState, Long clSpeexBits, int iSamplingRate );
    private native int SpeexDecoderDecode( Long clSpeexDecoderState, Long clSpeexBits, byte clSpeexAudioData[], long lSpeexAudioDataSize, short clPCMAudioData[]);
    private native void SpeexDecoderDestory( Long clSpeexDecoderState, Long clSpeexBits );
}