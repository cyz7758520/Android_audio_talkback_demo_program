package HeavenTao.Audio;

import android.view.Surface;

import HeavenTao.Data.VarStr;

//音频波形器类。
public class AudioOscillo
{
    private long m_AudioOscilloPt; //存放音频波形器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "AudioOscillo" ); //加载libAudioOscillo.so。
    }

    //构造函数。
    public AudioOscillo()
    {
        m_AudioOscilloPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy( null );
    }

    //获取音频波形器的内存指针。
    public long GetAudioOscilloPt()
    {
        return m_AudioOscilloPt;
    }

    //创建并初始化音频波形器。
    public native int Init( VarStr ErrInfoVarStrPt );

    //绘制音频波形到Surface。
    public native int Draw( short PcmFramePt[], int FrameLen, Surface DstSurfacePt, VarStr ErrInfoVarStrPt );

    //销毁音频波形器。
    public native int Destroy( VarStr ErrInfoVarStrPt );
}