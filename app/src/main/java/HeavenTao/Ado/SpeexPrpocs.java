package HeavenTao.Ado;

import HeavenTao.Data.*;

//Speex预处理器类。
public class SpeexPrpocs
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "SpeexDsp" ); //加载libSpeexDsp.so。
    }

    public long m_SpeexPrpocsPt; //存放Speex预处理器的指针。

    //构造函数。
    public SpeexPrpocs()
    {
        m_SpeexPrpocsPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Dstoy();
    }

    //创建并初始化Speex预处理器。
    public int Init( int SmplRate, int FrmLen, int IsUseNs, int NoiseSupes, int IsUseDereverb, int IsUseVad, int VadProbStart, int VadProbCont, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain, VarStr ErrInfoVarStrPt )
    {
        if( m_SpeexPrpocsPt == 0 )
        {
            HTLong p_SpeexPrpocsPt = new HTLong();
            if( SpeexPrpocsInit( p_SpeexPrpocsPt, SmplRate, FrmLen, IsUseNs, NoiseSupes, IsUseDereverb, IsUseVad, VadProbStart, VadProbCont, IsUseAgc, AgcLevel, AgcIncrement, AgcDecrement, AgcMaxGain, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_SpeexPrpocsPt = p_SpeexPrpocsPt.m_Val;
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
    public int Pocs( short FrmPt[], short RsltFrmPt[], HTInt VoiceActStsPt )
    {
        return SpeexPrpocsPocs( m_SpeexPrpocsPt, FrmPt, RsltFrmPt, VoiceActStsPt );
    }

    //销毁Speex预处理器。
    public int Dstoy()
    {
        if( m_SpeexPrpocsPt != 0 )
        {
            if( SpeexPrpocsDstoy( m_SpeexPrpocsPt ) == 0 )
            {
                m_SpeexPrpocsPt = 0;
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
    public native int SpeexPrpocsInit( HTLong SpeexPrpocsPt, int SmplRate, int FrmLen, int IsUseNs, int NoiseSupes, int IsUseDereverb, int IsUseVad, int VadProbStart, int VadProbCont, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain, long ErrInfoVarStrPt );

    //用Speex预处理器对单声道16位有符号整型PCM格式帧进行Speex预处理。
    public native int SpeexPrpocsPocs( long SpeexPrpocsPt, short FrmPt[], short RsltFrmPt[], HTInt VoiceActStsPt );

    //销毁Speex预处理器。
    public native int SpeexPrpocsDstoy( long SpeexPrpocsPt );
}