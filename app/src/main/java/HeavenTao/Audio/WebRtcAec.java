package HeavenTao.Audio;

import HeavenTao.Data.*;

//WebRtc浮点版声学回音消除器类。
public class WebRtcAec
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    public long m_WebRtcAecPt; //存放WebRtc浮点版声学回音消除器的指针。

    //构造函数。
    public WebRtcAec()
    {
        m_WebRtcAecPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //创建并初始化WebRtc浮点版声学回音消除器。
    public int Init( int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, VarStr ErrInfoVarStrPt )
    {
        if( m_WebRtcAecPt == 0 )
        {
            HTLong p_WebRtcAecPt = new HTLong();
            if( WebRtcAecInit( p_WebRtcAecPt, SamplingRate, FrameLen, EchoMode, Delay, IsUseDelayAgnosticMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_WebRtcAecPt = p_WebRtcAecPt.m_Val;
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

    //根据WebRtc浮点版声学回音消除器内存块来创建并初始化WebRtc浮点版声学回音消除器。
    public int InitByMem( int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte WebRtcAecMemPt[], long WebRtcAecMemLen, VarStr ErrInfoVarStrPt )
    {
        if( m_WebRtcAecPt == 0 )
        {
            HTLong p_WebRtcAecPt = new HTLong();
            if( WebRtcAecInitByMem( p_WebRtcAecPt, SamplingRate, FrameLen, EchoMode, Delay, IsUseDelayAgnosticMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, WebRtcAecMemPt, WebRtcAecMemLen, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_WebRtcAecPt = p_WebRtcAecPt.m_Val;
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

    //根据WebRtc浮点版声学回音消除器内存块文件来创建并初始化WebRtc浮点版声学回音消除器。
    public int InitByMemFile( int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String WebRtcAecMemFileFullPathStrPt, VarStr ErrInfoVarStrPt )
    {
        if( m_WebRtcAecPt == 0 )
        {
            HTLong p_WebRtcAecPt = new HTLong();
            if( WebRtcAecInitByMemFile( p_WebRtcAecPt, SamplingRate, FrameLen, EchoMode, Delay, IsUseDelayAgnosticMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, WebRtcAecMemFileFullPathStrPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_WebRtcAecPt = p_WebRtcAecPt.m_Val;
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

    //获取WebRtc浮点版声学回音消除器内存块的数据长度。
    public int GetMemLen( int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, HTLong WebrtcAecMemLenPt )
    {
        return WebRtcAecGetMemLen( m_WebRtcAecPt, SamplingRate, FrameLen, EchoMode, Delay, IsUseDelayAgnosticMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, WebrtcAecMemLenPt );
    }

    //获取WebRtc浮点版声学回音消除器的内存块。
    public int GetMem( int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte WebRtcAecMemPt[], long WebRtcAecMemSz )
    {
        return WebRtcAecGetMem( m_WebRtcAecPt, SamplingRate, FrameLen, EchoMode, Delay, IsUseDelayAgnosticMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, WebRtcAecMemPt, WebRtcAecMemSz );
    }

    //将WebRtc浮点版声学回音消除器内存块保存到指定的文件。
    public int SaveMemFile( int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String WebRtcAecMemFileFullPathStrPt, VarStr ErrInfoVarStrPt )
    {
        return WebRtcAecSaveMemFile( m_WebRtcAecPt, SamplingRate, FrameLen, EchoMode, Delay, IsUseDelayAgnosticMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, WebRtcAecMemFileFullPathStrPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //设置WebRtc浮点版声学回音消除器的回音延迟。
    public int SetDelay( int Delay )
    {
        return WebRtcAecSetDelay( m_WebRtcAecPt, Delay );
    }

    //获取WebRtc浮点版声学回音消除器的回音延迟。
    public int GetDelay( HTInt DelayPt )
    {
        return WebRtcAecGetDelay( m_WebRtcAecPt, DelayPt );
    }

    //用WebRtc浮点版声学回音消除器对单声道16位有符号整型PCM格式输入帧进行WebRtc浮点版声学回音消除。
    public int Proc( short InputFramePt[], short OutputFramePt[], short ResultFramePt[] )
    {
        return WebRtcAecProc( m_WebRtcAecPt, InputFramePt, OutputFramePt, ResultFramePt );
    }

    //销毁WebRtc浮点版声学回音消除器。
    public int Destroy()
    {
        if( m_WebRtcAecPt != 0 )
        {
            if( WebRtcAecDestroy( m_WebRtcAecPt ) == 0 )
            {
                m_WebRtcAecPt = 0;
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

    //创建并初始化WebRtc浮点版声学回音消除器。
    public native int WebRtcAecInit( HTLong WebRtcAecPt, int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, long ErrInfoVarStrPt );

    //根据WebRtc浮点版声学回音消除器内存块来创建并初始化WebRtc浮点版声学回音消除器。
    public native int WebRtcAecInitByMem( HTLong WebRtcAecPt, int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte WebRtcAecMemPt[], long WebRtcAecMemLen, long ErrInfoVarStrPt );

    //根据WebRtc浮点版声学回音消除器内存块文件来创建并初始化WebRtc浮点版声学回音消除器。
    public native int WebRtcAecInitByMemFile( HTLong WebRtcAecPt, int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String WebRtcAecMemFileFullPathStrPt, long ErrInfoVarStrPt );

    //获取WebRtc浮点版声学回音消除器内存块的数据长度。
    public native int WebRtcAecGetMemLen( long WebRtcAecPt, int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, HTLong WebrtcAecMemLenPt );

    //获取WebRtc浮点版声学回音消除器的内存块。
    public native int WebRtcAecGetMem( long WebRtcAecPt, int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte WebRtcAecMemPt[], long WebRtcAecMemSz );

    //将WebRtc浮点版声学回音消除器内存块保存到指定的文件。
    public native int WebRtcAecSaveMemFile( long WebRtcAecPt, int SamplingRate, int FrameLen, int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String WebRtcAecMemFileFullPathStrPt, long ErrInfoVarStrPt );

    //设置WebRtc浮点版声学回音消除器的回音延迟。
    public native int WebRtcAecSetDelay( long WebRtcAecPt, int Delay );

    //获取WebRtc浮点版声学回音消除器的回音延迟。
    public native int WebRtcAecGetDelay( long WebRtcAecPt, HTInt DelayPt );

    //用WebRtc浮点版声学回音消除器对单声道16位有符号整型PCM格式输入帧进行WebRtc浮点版声学回音消除。
    public native int WebRtcAecProc( long WebRtcAecPt, short InputFramePt[], short OutputFramePt[], short ResultFramePt[] );

    //销毁WebRtc浮点版声学回音消除器。
    public native int WebRtcAecDestroy( long WebRtcAecPt );
}