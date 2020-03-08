package HeavenTao.Audio;

import HeavenTao.Data.*;

//RNNoise噪音抑制器类。
public class RNNoise
{
    private long m_RNNoisePt; //RNNoise噪音抑制器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
        System.loadLibrary( "RNNoise" ); //加载libRNNoise.so。
    }

    //构造函数。
    public RNNoise()
    {
        m_RNNoisePt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取RNNoise噪音抑制器的内存指针。
    public long GetRNNoisePt()
    {
        return m_RNNoisePt;
    }

    //创建并初始化RNNoise噪音抑制器。
    public native int Init( int SamplingRate, int FrameLen );

    //用RNNoise噪音抑制器对单声道16位有符号整型PCM格式帧进行RNNoise噪音抑制。
    public native int Proc( short FrameObj[], short ResultFrameObj[] );

    //销毁RNNoise噪音抑制器。
    public native int Destroy();
}
