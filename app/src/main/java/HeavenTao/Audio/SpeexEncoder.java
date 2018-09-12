package HeavenTao.Audio;

//Speex编码器类
public class SpeexEncoder
{
    private Long pclSpeexEncoderState; //存放Speex编码器的内存指针。
    private Long pclSpeexBits; //存放SpeexBits的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Speex" ); //加载libSpeex.so。
    }

    //构造函数。
    public SpeexEncoder()
    {
        pclSpeexEncoderState = new Long(0);
        pclSpeexBits = new Long(0);
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //初始化Speex编码器。
    public long Init( int i32SamplingRate, int i32UseCbrOrVbr, int i32Quality, int i32Complexity, int i32PlcExpectedLossRate )
    {
        if( pclSpeexEncoderState.longValue() == 0 ) //如果Speex编码器还没有初始化。
        {
            return SpeexEncoderInit( pclSpeexEncoderState, pclSpeexBits, i32SamplingRate, i32UseCbrOrVbr, i32Quality, i32Complexity, i32PlcExpectedLossRate );
        }
        else //如果Speex编码器已经初始化。
        {
            return 0;
        }
    }

    //获取Speex编码器的内存指针。
    public Long GetSpeexEncoderState()
    {
        return pclSpeexEncoderState;
    }

    //对一个单声道16位有符号整型20毫秒PCM格式音频数据帧进行Speex格式编码。
    public long Encode( short pszi16PcmAudioDataFrame[], byte pszi8SpeexAudioDataFrame[], Integer pclSpeexAudioDataFrameSize, Integer pclIsNeedTrans )
    {
        return SpeexEncoderEncode( pclSpeexEncoderState, pclSpeexBits, pszi16PcmAudioDataFrame, pszi8SpeexAudioDataFrame, pclSpeexAudioDataFrameSize, pclIsNeedTrans );
    }

    //销毁Speex编码器。
    public long Destory()
    {
        return SpeexEncoderDestory( pclSpeexEncoderState, pclSpeexBits );
    }

    private native long SpeexEncoderInit( Long pclSpeexEncoderState, Long pclSpeexBits, int i32SamplingRate, int i32UseCbrOrVbr, int i32Quality, int i32Complexity, int i32PlcExpectedLossRate );
    private native long SpeexEncoderEncode( Long pclSpeexEncoderState, Long pclSpeexBits, short pszi16PcmAudioDataFrame[], byte pszi8SpeexAudioDataFrame[], Integer pclSpeexAudioDataFrameSize, Integer pclIsNeedTrans );
    private native long SpeexEncoderDestory( Long pclSpeexEncoderState, Long pclSpeexBits );
}