package HeavenTao.Audio;

import HeavenTao.Data.*;

//Speex编码器类。
public class SpeexEncoder
{
    private long m_SpeexEncoderPt; //存放Speex编码器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Speex" ); //加载libSpeex.so。
    }

    //构造函数。
    public SpeexEncoder()
    {
        m_SpeexEncoderPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取Speex编码器的内存指针。
    public long GetSpeexEncoderPt()
    {
        return m_SpeexEncoderPt;
    }

    //创建并初始化Speex编码器。
    public native int Init( int SamplingRate, int UseCbrOrVbr, int Quality, int Complexity, int PlcExpectedLossRate );

    //用Speex编码器对单声道16位有符号整型20毫秒PCM格式帧进行Speex格式编码。
    public native int Proc( short PcmFramePt[], byte SpeexFramePt[], long SpeexFrameSz, HTLong SpeexFrameLenPt, HTInt IsNeedTransPt );

    //销毁Speex编码器。
    public native int Destroy();
}