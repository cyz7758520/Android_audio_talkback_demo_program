package HeavenTao.Audio;

import HeavenTao.Data.*;

//Speex编码器类。
public class SpeexEncoder
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Speex" ); //加载libSpeex.so。
    }

    public long m_SpeexEncoderPt; //存放Speex编码器的指针。

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

    //创建并初始化Speex编码器。
    public int Init( int SamplingRate, int UseCbrOrVbr, int Quality, int Complexity, int PlcExpectedLossRate )
    {
        if( m_SpeexEncoderPt == 0 )
        {
            HTLong p_SpeexEncoderPt = new HTLong();
            if( SpeexEncoderInit( p_SpeexEncoderPt, SamplingRate, UseCbrOrVbr, Quality, Complexity, PlcExpectedLossRate ) == 0 )
            {
                m_SpeexEncoderPt = p_SpeexEncoderPt.m_Val;
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

    //用Speex编码器对单声道16位有符号整型20毫秒PCM格式帧进行Speex格式编码。
    public int Proc( short PcmFramePt[], byte SpeexFramePt[], long SpeexFrameSz, HTLong SpeexFrameLenPt, HTInt IsNeedTransPt )
    {
        return SpeexEncoderProc( m_SpeexEncoderPt, PcmFramePt, SpeexFramePt, SpeexFrameSz, SpeexFrameLenPt, IsNeedTransPt );
    }

    //销毁Speex编码器。
    public int Destroy()
    {
        if( m_SpeexEncoderPt != 0 )
        {
            if( SpeexEncoderDestroy( m_SpeexEncoderPt ) == 0 )
            {
                m_SpeexEncoderPt = 0;
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

    //创建并初始化Speex编码器。
    public native int SpeexEncoderInit( HTLong SpeexEncoderPt, int SamplingRate, int UseCbrOrVbr, int Quality, int Complexity, int PlcExpectedLossRate );

    //用Speex编码器对单声道16位有符号整型20毫秒PCM格式帧进行Speex格式编码。
    public native int SpeexEncoderProc( long SpeexEncoderPt, short PcmFramePt[], byte SpeexFramePt[], long SpeexFrameSz, HTLong SpeexFrameLenPt, HTInt IsNeedTransPt );

    //销毁Speex编码器。
    public native int SpeexEncoderDestroy( long SpeexEncoderPt );
}