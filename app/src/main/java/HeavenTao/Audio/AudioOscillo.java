package HeavenTao.Audio;

import android.view.Surface;

import HeavenTao.Data.*;

//音频波形器类。
public class AudioOscillo
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "AudioOscillo" ); //加载libAudioOscillo.so。
    }

    public long m_AudioOscilloPt; //存放音频波形器的内存指针。

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

    //创建并初始化音频波形器。
    public int Init( VarStr ErrInfoVarStrPt )
    {
        if( m_AudioOscilloPt == 0 )
        {
            HTLong p_AudioOscilloPt = new HTLong();
            if( AudioOscilloInit( p_AudioOscilloPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_AudioOscilloPt = p_AudioOscilloPt.m_Val;
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

    //绘制音频波形到Surface。
    public int Draw( short PcmFramePt[], int FrameLen, Surface DstSurfacePt, VarStr ErrInfoVarStrPt )
    {
        return AudioOscilloDraw( m_AudioOscilloPt, PcmFramePt, FrameLen, DstSurfacePt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //销毁音频波形器。
    public int Destroy( VarStr ErrInfoVarStrPt )
    {
        if( m_AudioOscilloPt != 0 )
        {
            if( AudioOscilloDestroy( m_AudioOscilloPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_AudioOscilloPt = 0;
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

    //创建并初始化音频波形器。
    public native int AudioOscilloInit( HTLong AudioOscilloPt, long ErrInfoVarStrPt );

    //绘制音频波形到Surface。
    public native int AudioOscilloDraw( long AudioOscilloPt, short PcmFramePt[], int FrameLen, Surface DstSurfacePt, long ErrInfoVarStrPt );

    //销毁音频波形器。
    public native int AudioOscilloDestroy( long AudioOscilloPt, long ErrInfoVarStrPt );
}