package HeavenTao.Audio;

import HeavenTao.Data.*;

//Speex解码器类。
public class SpeexDecoder
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Speex" ); //加载libSpeex.so。
    }

    public long m_SpeexDecoderPt; //存放Speex解码器的指针。

    //构造函数。
    public SpeexDecoder()
    {
        m_SpeexDecoderPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //创建并初始化Speex解码器。
    public int Init( int SamplingRate, int IsUsePerceptualEnhancement )
    {
        if( m_SpeexDecoderPt == 0 )
        {
            HTLong p_SpeexDecoderPt = new HTLong();
            if( SpeexDecoderInit( p_SpeexDecoderPt, SamplingRate, IsUsePerceptualEnhancement ) == 0 )
            {
                m_SpeexDecoderPt = p_SpeexDecoderPt.m_Val;
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

    //用Speex解码器对单声道16位有符号整型20毫秒PCM格式帧进行Speex格式编码。
    public int Proc( byte SpeexFramePt[], long SpeexFrameLen, short PcmFramePt[] )
    {
        return SpeexDecoderProc( m_SpeexDecoderPt, SpeexFramePt, SpeexFrameLen, PcmFramePt );
    }

    //销毁Speex解码器。
    public int Destroy()
    {
        if( m_SpeexDecoderPt != 0 )
        {
            if( SpeexDecoderDestroy( m_SpeexDecoderPt ) == 0 )
            {
                m_SpeexDecoderPt = 0;
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

    //创建并初始化Speex解码器。
    public native int SpeexDecoderInit( HTLong SpeexDecoderPt, int SamplingRate, int IsUsePerceptualEnhancement );

    //用Speex解码器对单声道16位有符号整型20毫秒Speex格式帧进行PCM格式解码。
    public native int SpeexDecoderProc( long SpeexDecoderPt, byte SpeexFramePt[], long SpeexFrameLen, short PcmFramePt[] );

    //销毁Speex解码器。
    public native int SpeexDecoderDestroy( long SpeexDecoderPt );
}