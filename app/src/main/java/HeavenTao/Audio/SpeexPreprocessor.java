package HeavenTao.Audio;

public class SpeexPreprocessor//Speex预处理器类
{
    private Long pclSpeexPreprocessState;//Speex预处理器的内存指针

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so
        System.loadLibrary( "SpeexDsp" ); //加载libSpeexDsp.so
    }

    //构造函数
    public SpeexPreprocessor()
    {
        pclSpeexPreprocessState = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        Destory();
    }

    //初始化Speex预处理器
    public long Init( int i32SamplingRate, int i32FrameSize, int i32IsUseNs, int i32NoiseSuppress, int i32IsUseVad, int i32VadProbStart, int i32VadProbContinue, int i32IsUseAgc, int i32AgcLevel, int i32AgcMaxGain, int i32IsUseRec, long i64SpeexEchoState, int i32EchoSuppress, int i32EchoSuppressActive )
    {
        if( pclSpeexPreprocessState.longValue() == 0)//如果Speex预处理器还没有初始化
        {
            return SpeexPreprocessInit( pclSpeexPreprocessState, i32SamplingRate, i32FrameSize, i32IsUseNs, i32NoiseSuppress, i32IsUseVad, i32VadProbStart, i32VadProbContinue, i32IsUseAgc, i32AgcLevel, i32AgcMaxGain, i32IsUseRec, i64SpeexEchoState, i32EchoSuppress, i32EchoSuppressActive );
        }
        else//如果Speex预处理器已经初始化
        {
            return 0;
        }
    }

    //获取Speex预处理器的内存指针
    public Long GetSpeexPreprocessState()
    {
        return pclSpeexPreprocessState;
    }

    //对一个单声道16位有符号整型PCM格式音频数据帧进行Speex预处理
    public long Preprocess( short pszi16AudioDataFrame[], Integer pclVoiceActivityStatus )
    {
        return SpeexPreprocessPreprocess( pclSpeexPreprocessState, pszi16AudioDataFrame, pclVoiceActivityStatus );
    }

    //销毁Speex预处理器
    public long Destory()
    {
        return SpeexPreprocessDestory( pclSpeexPreprocessState );
    }

    private native long SpeexPreprocessInit( Long pclSpeexPreprocessState, int i32SamplingRate, int i32FrameSize, int i32IsUseNs, int i32NoiseSuppress, int i32IsUseVad, int i32VadProbStart, int i32VadProbContinue, int i32IsUseAgc, int i32AgcLevel, int i32AgcMaxGain, int i32IsUseRec, long i64SpeexEchoState, int i32EchoSuppress, int i32EchoSuppressActive );
    private native long SpeexPreprocessPreprocess( Long pclSpeexPreprocessState, short pszi16AudioDataFrame[], Integer pclVoiceActivityStatus );
    private native long SpeexPreprocessDestory( Long pclSpeexPreprocessState );
}