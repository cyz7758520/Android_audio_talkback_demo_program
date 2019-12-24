package HeavenTao.Audio;

import HeavenTao.Data.*;

//Speex解码器类。
public class SpeexDecoder
{
    private long m_pvPoint; //存放Speex解码器结构体的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Speex" ); //加载libSpeex.so。
    }

    //构造函数。
    public SpeexDecoder()
    {
        m_pvPoint = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //获取Speex解码器结构体的内存指针。
    public long GetPoint()
    {
        return m_pvPoint;
    }

    //创建并初始化Speex解码器。
    public native long Init( int i32SamplingRate, int i32IsUsePerceptualEnhancement );

    //用Speex解码器对单声道16位有符号整型20毫秒Speex格式帧进行PCM格式解码。
    public native long Process( byte pszi8SpeexFrame[], long i64SpeexFrameLength, short pszi16PcmFrame[] );

    //销毁Speex解码器。
    public native long Destory();
}