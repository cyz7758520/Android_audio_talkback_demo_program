package HeavenTao.Audio;

import HeavenTao.Data.*;

//Speex声学回音消除器类。
public class SpeexAec
{
    private long m_pvPoint; //存放Speex声学回音消除器结构体的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "SpeexDsp" ); //加载libSpeexDsp.so。
    }

    //构造函数。
    public SpeexAec()
    {
        m_pvPoint = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //获取Speex声学回音消除器结构体的内存指针。
    public long GetPoint()
    {
        return m_pvPoint;
    }

    //创建并初始化Speex声学回音消除器。
    public native long Init( int i32SamplingRate, long i64FrameLength, int i32FilterLength );

    //根据Speex声学回音消除器内存块来创建并初始化Speex声学回音消除器。
    public native long InitFromMemory( byte pszi8SpeexAecMemory[], long i64SpeexAecMemoryLength );

    //获取Speex声学回音消除器内存块的数据长度。
    public native long GetMemoryLength( HTLong pclSpeexAecMemoryLength );

    //获取Speex声学回音消除器的内存块。
    public native long GetMemory( byte pszi8SpeexAecMemory[], long i64SpeexAecMemorySize );

    //用Speex声学回音消除器对单声道16位有符号整型PCM格式输入帧进行Speex声学回音消除。
    public native long Process( short pszi16InputFrame[], short pszi16OutputFrame[], short pszi16ResultFrame[] );

    //销毁Speex声学回音消除器。
    public native long Destory();
}