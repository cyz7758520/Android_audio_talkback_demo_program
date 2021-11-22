package HeavenTao.Audio;

import HeavenTao.Data.*;

//RNNoise噪音抑制器类。
public class RNNoise
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
        System.loadLibrary( "RNNoise" ); //加载libRNNoise.so。
    }

    public long m_RNNoisePt; //RNNoise噪音抑制器的指针。

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

    //创建并初始化RNNoise噪音抑制器。
    public int Init( int SamplingRate, int FrameLen, VarStr ErrInfoVarStrPt )
    {
        if( m_RNNoisePt == 0 )
        {
            HTLong p_RNNoisePt = new HTLong();
            if( RNNoiseInit( p_RNNoisePt, SamplingRate, FrameLen, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_RNNoisePt = p_RNNoisePt.m_Val;
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            return 0;
        }
    }

    //用RNNoise噪音抑制器对单声道16位有符号整型PCM格式帧进行RNNoise噪音抑制。
    public int Proc( short FramePt[], short ResultFramePt[] )
    {
        return RNNoiseProc( m_RNNoisePt, FramePt, ResultFramePt );
    }

    //销毁RNNoise噪音抑制器。
    public int Destroy()
    {
        if( m_RNNoisePt != 0 )
        {
            if( RNNoiseDestroy( m_RNNoisePt ) == 0 )
            {
                m_RNNoisePt = 0;
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            return 0;
        }
    }

    //创建并初始化RNNoise噪音抑制器。
    public native int RNNoiseInit( HTLong RNNoisePt, int SamplingRate, int FrameLen, long ErrInfoVarStrPt );

    //用RNNoise噪音抑制器对单声道16位有符号整型PCM格式帧进行RNNoise噪音抑制。
    public native int RNNoiseProc( long RNNoisePt, short FramePt[], short ResultFramePt[] );

    //销毁RNNoise噪音抑制器。
    public native int RNNoiseDestroy( long RNNoisePt );
}
