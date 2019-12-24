package HeavenTao.Audio;

import HeavenTao.Data.*;

//Speex预处理器类。
public class SpeexPreprocessor
{
    private long m_pvPoint; //存放Speex预处理器结构体的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "SpeexDsp" ); //加载libSpeexDsp.so。
    }

    //构造函数。
    public SpeexPreprocessor()
    {
        m_pvPoint = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //获取Speex预处理器结构体的内存指针。
    public long GetPoint()
    {
        return m_pvPoint;
    }

    //创建并初始化Speex预处理器。
    public native long Init( int i32SamplingRate, long i64FrameLength, int i32IsUseNs, int i32NoiseSuppress, int i32IsUseDereverberation, int i32IsUseVad, int i32VadProbStart, int i32VadProbContinue, int i32IsUseAgc, int i32AgcLevel, int i32AgcIncrement, int i32AgcDecrement, int i32AgcMaxGain, int i32IsUseRec, long i64SpeexAecPoint, float fEchoMultiple, int i32EchoSuppress, int i32EchoSuppressActive );

    //用Speex预处理器对单声道16位有符号整型PCM格式帧进行Speex预处理。
    public native long Process( short pszi16Frame[], short pszi16ResultFrame[], HTInteger pclVoiceActivityStatus );

    //销毁Speex预处理器。
    public native long Destory();
}