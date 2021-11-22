package HeavenTao.Audio;

import HeavenTao.Data.*;

//Speex预处理器类。
public class SpeexPproc
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "SpeexDsp" ); //加载libSpeexDsp.so。
    }

    public long m_SpeexPprocPt; //存放Speex预处理器的指针。

    //构造函数。
    public SpeexPproc()
    {
        m_SpeexPprocPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //创建并初始化Speex预处理器。
    public int Init( int SamplingRate, int FrameLen, int IsUseNs, int NoiseSupes, int IsUseDereverb, int IsUseVad, int VadProbStart, int VadProbCont, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain, VarStr ErrInfoVarStrPt )
    {
        if( m_SpeexPprocPt == 0 )
        {
            HTLong p_SpeexPprocPt = new HTLong();
            if( SpeexPprocInit( p_SpeexPprocPt, SamplingRate, FrameLen, IsUseNs, NoiseSupes, IsUseDereverb, IsUseVad, VadProbStart, VadProbCont, IsUseAgc, AgcLevel, AgcIncrement, AgcDecrement, AgcMaxGain, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_SpeexPprocPt = p_SpeexPprocPt.m_Val;
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

    //用Speex预处理器对单声道16位有符号整型PCM格式帧进行Speex预处理。
    public int Proc( short FramePt[], short ResultFramePt[], HTInt VoiceActStsPt )
    {
        return SpeexPprocProc( m_SpeexPprocPt, FramePt, ResultFramePt, VoiceActStsPt );
    }

    //销毁Speex预处理器。
    public int Destroy()
    {
        if( m_SpeexPprocPt != 0 )
        {
            if( SpeexPprocDestroy( m_SpeexPprocPt ) == 0 )
            {
                m_SpeexPprocPt = 0;
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

    //创建并初始化Speex预处理器。
    public native int SpeexPprocInit( HTLong SpeexPprocPt, int SamplingRate, int FrameLen, int IsUseNs, int NoiseSupes, int IsUseDereverb, int IsUseVad, int VadProbStart, int VadProbCont, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain, long ErrInfoVarStrPt );

    //用Speex预处理器对单声道16位有符号整型PCM格式帧进行Speex预处理。
    public native int SpeexPprocProc( long SpeexPprocPt, short FramePt[], short ResultFramePt[], HTInt VoiceActStsPt );

    //销毁Speex预处理器。
    public native int SpeexPprocDestroy( long SpeexPprocPt );
}