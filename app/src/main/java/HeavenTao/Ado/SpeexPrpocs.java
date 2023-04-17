package HeavenTao.Ado;

import HeavenTao.Data.*;

//Speex预处理器。
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
    protected void finalize()
    {
        Dstoy();
    }

    //创建并初始化Speex预处理器。
    public int Init( int SmplRate, long FrmLenUnit, int IsUseNs, int NoiseSupes, int IsUseDereverb, int IsUseVad, int VadProbStart, int VadProbCntu, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain, Vstr ErrInfoVstrPt )
    {
        if( m_SpeexPrpocsPt == 0 )
        {
            HTLong p_SpeexPrpocsPt = new HTLong();
            if( SpeexPrpocsInit( p_SpeexPrpocsPt, SmplRate, FrmLenUnit, IsUseNs, NoiseSupes, IsUseDereverb, IsUseVad, VadProbStart, VadProbCntu, IsUseAgc, AgcLevel, AgcIncrement, AgcDecrement, AgcMaxGain, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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

    //用Speex预处理器对单声道16位有符号整型Pcm格式帧进行Speex预处理。
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
    private native int SpeexPrpocsInit( HTLong SpeexPrpocsPt, int SmplRate, long FrmLenUnit, int IsUseNs, int NoiseSupes, int IsUseDereverb, int IsUseVad, int VadProbStart, int VadProbCntu, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain, long ErrInfoVstrPt );

    //用Speex预处理器对单声道16位有符号整型Pcm格式帧进行Speex预处理。
    private native int SpeexPrpocsPocs( long SpeexPrpocsPt, short FrmPt[], short RsltFrmPt[], HTInt VoiceActStsPt );

    //销毁Speex预处理器。
    private native int SpeexPrpocsDstoy( long SpeexPrpocsPt );
}