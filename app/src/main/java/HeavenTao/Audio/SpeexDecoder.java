package HeavenTao.Audio;

//Speex解码器类
public class SpeexDecoder
{
    private Long pclSpeexDecoderState; //存放Speex解码器的指针
    private Long pclSpeexBits; //存放SpeexBits的指针

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so
        System.loadLibrary( "Speex" ); //加载libSpeex.so
    }

    //构造函数
    public SpeexDecoder()
    {
        pclSpeexDecoderState = new Long(0);
        pclSpeexBits = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        Destory();
    }

    //初始化Speex解码器
    public long Init( int i32SamplingRate )
    {
        if( pclSpeexDecoderState.longValue() == 0)//如果Speex编码器还没有初始化
        {
            return SpeexDecoderInit( pclSpeexDecoderState, pclSpeexBits, i32SamplingRate );
        }
        else//如果Speex编码器已经初始化
        {
            return 0;
        }
    }

    //获取Speex解码器的内存指针
    public Long GetSpeexDecoderState()
    {
        return pclSpeexDecoderState;
    }

    //对一帧Speex音频数据进行解码
    public long Decode( byte pszi8SpeexAudioDataFrame[], int i32SpeexAudioDataFrameSize, short pszi16PcmAudioDataFrame[] )
    {
        return SpeexDecoderDecode( pclSpeexDecoderState, pclSpeexBits, pszi8SpeexAudioDataFrame, i32SpeexAudioDataFrameSize, pszi16PcmAudioDataFrame );
    }

    //销毁Speex解码器
    public long Destory()
    {
        return SpeexDecoderDestory( pclSpeexDecoderState, pclSpeexBits );
    }

    private native long SpeexDecoderInit( Long pclSpeexDecoderState, Long pclSpeexBits, int i32SamplingRate );
    private native long SpeexDecoderDecode( Long pclSpeexDecoderState, Long pclSpeexBits, byte pszi8SpeexAudioDataFrame[], int i32SpeexAudioDataFrameSize, short pszi16PcmAudioDataFrame[] );
    private native long SpeexDecoderDestory( Long pclSpeexDecoderState, Long pclSpeexBits );
}