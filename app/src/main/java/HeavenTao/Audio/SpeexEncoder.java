package HeavenTao.Audio;

import HeavenTao.Data.*;

//Speex编码器类。
public class SpeexEncoder
{
    private long m_pvPoint; //存放Speex编码器结构体的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Speex" ); //加载libSpeex.so。
    }

    //构造函数。
    public SpeexEncoder()
    {
        m_pvPoint = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //获取Speex编码器结构体的内存指针。
    public long GetPoint()
    {
        return m_pvPoint;
    }

    //创建并初始化Speex编码器。
    public native long Init( int i32SamplingRate, int i32UseCbrOrVbr, int i32Quality, int i32Complexity, int i32PlcExpectedLossRate );

    //用Speex编码器对单声道16位有符号整型20毫秒PCM格式帧进行Speex格式编码。
    public native long Process( short pszi16PcmFrame[], byte pszi8SpeexFrame[], long i64SpeexFrameSize, HTLong pclSpeexFrameLength, HTInteger pclIsNeedTrans );

    //销毁Speex编码器。
    public native long Destory();
}