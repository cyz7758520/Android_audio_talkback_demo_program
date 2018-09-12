package HeavenTao.Audio;

public class SpeexAec //Speex声学回音消除器类
{
    private Long pclSpeexEchoState; //Speex声学回音消除器的内存指针

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so
        System.loadLibrary( "SpeexDsp" ); //加载libSpeexDsp.so
    }

    //构造函数
    public SpeexAec()
    {
        pclSpeexEchoState = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        Destory();
    }

    //初始化Speex声学回音消除器
    public long Init( int i32SamplingRate, int i32FrameSize, int i32FilterLength )
    {
        if( pclSpeexEchoState.longValue() == 0 ) //如果Speex声学回音消除器还没有初始化
        {
            return SpeexAecInit( pclSpeexEchoState, i32SamplingRate, i32FrameSize, i32FilterLength );
        }
        else //如果Speex声学回音消除器已经初始化
        {
            return 0;
        }
    }

    //获取Speex声学回音消除器的内存指针
    public Long GetSpeexEchoState()
    {
        return pclSpeexEchoState;
    }

    //对一个单声道16位有符号整型PCM格式音频输入数据帧进行Speex声学回音消除
    public long Aec( short pszi16AudioInputDataFrame[], short pszi16AudioOutputDataFrame[], short pszi16AudioResultDataFrame[] )
    {
        return SpeexAecAec( pclSpeexEchoState, pszi16AudioInputDataFrame, pszi16AudioOutputDataFrame, pszi16AudioResultDataFrame);
    }

    //销毁Speex声学回音消除器
    public long Destory()
    {
        return SpeexAecDestory( pclSpeexEchoState );
    }

    private native long SpeexAecInit( Long pclSpeexEchoState, int i32SamplingRate, int i32FrameSize, int i32FilterLength );
    private native long SpeexAecAec( Long pclSpeexEchoState, short pszi16AudioInputDataFrame[], short pszi16AudioOutputDataFrame[], short pszi16AudioResultDataFrame[] );
    private native long SpeexAecDestory( Long pclSpeexEchoState );
}