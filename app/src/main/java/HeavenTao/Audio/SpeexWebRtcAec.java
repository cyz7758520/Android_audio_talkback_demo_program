package HeavenTao.Audio;

import HeavenTao.Data.*;

//SpeexWebRtc三重声学回音消除器类。
public class SpeexWebRtcAec
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "SpeexDsp" ); //加载libSpeex.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
        System.loadLibrary( "SpeexWebRtcAec" ); //加载libSpeexWebRtcAec.so。
    }

    public long m_SpeexWebRtcAecPt; //存放SpeexWebRtc三重声学回音消除器的指针。

    //构造函数。
    public SpeexWebRtcAec()
    {
        m_SpeexWebRtcAecPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //创建并初始化SpeexWebRtc三重声学回音消除器。
    public int Init( int SamplingRate, int FrameLen, int WorkMode, int SpeexAecFilterLen, int SpeexAecIsUseRec, float SpeexAecEchoMultiple, float SpeexAecEchoCont, int SpeexAecEchoSupes, int SpeexAecEchoSupesAct, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgnosticMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay, int IsUseSameRoomAec, int SameRoomEchoMinDelay, VarStr ErrInfoVarStrPt )
    {
        if( m_SpeexWebRtcAecPt == 0 )
        {
            HTLong p_SpeexWebRtcAecPt = new HTLong();
            if( SpeexWebRtcAecInit( p_SpeexWebRtcAecPt, SamplingRate, FrameLen, WorkMode, SpeexAecFilterLen, SpeexAecIsUseRec, SpeexAecEchoMultiple, SpeexAecEchoCont, SpeexAecEchoSupes, SpeexAecEchoSupesAct, WebRtcAecmIsUseCNGMode, WebRtcAecmEchoMode, WebRtcAecmDelay, WebRtcAecEchoMode, WebRtcAecDelay, WebRtcAecIsUseDelayAgnosticMode, WebRtcAecIsUseExtdFilterMode, WebRtcAecIsUseRefinedFilterAdaptAecMode, WebRtcAecIsUseAdaptAdjDelay, IsUseSameRoomAec, SameRoomEchoMinDelay, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_SpeexWebRtcAecPt = p_SpeexWebRtcAecPt.m_Val;
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

    //设置SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
    public int SetWebRtcAecmDelay( int WebRtcAecmDelay )
    {
        return SpeexWebRtcAecSetWebRtcAecmDelay( m_SpeexWebRtcAecPt, WebRtcAecmDelay );
    }

    //获取SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
    public int GetWebRtcAecmDelay( HTInt WebRtcAecmDelayPt )
    {
        return SpeexWebRtcAecGetWebRtcAecmDelay( m_SpeexWebRtcAecPt, WebRtcAecmDelayPt );
    }

    //设置SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
    public int SetWebRtcAecDelay( int WebRtcAecDelay )
    {
        return SpeexWebRtcAecSetWebRtcAecDelay( m_SpeexWebRtcAecPt, WebRtcAecDelay );
    }

    //获取SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
    public int GetWebRtcAecDelay( HTInt WebRtcAecDelayPt )
    {
        return SpeexWebRtcAecGetWebRtcAecDelay( m_SpeexWebRtcAecPt, WebRtcAecDelayPt );
    }

    //用SpeexWebRtc三重声学回音消除器对单声道16位有符号整型PCM格式输入帧进行SpeexWebRtc三重声学回音消除。
    public int Proc( short InputFramePt[], short OutputFramePt[], short ResultFramePt[] )
    {
        return SpeexWebRtcAecProc( m_SpeexWebRtcAecPt, InputFramePt, OutputFramePt, ResultFramePt );
    }

    //销毁SpeexWebRtc三重声学回音消除器。
    public int Destroy()
    {
        if( m_SpeexWebRtcAecPt != 0 )
        {
            if( SpeexWebRtcAecDestroy( m_SpeexWebRtcAecPt ) == 0 )
            {
                m_SpeexWebRtcAecPt = 0;
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

    //创建并初始化SpeexWebRtc三重声学回音消除器。
    public native int SpeexWebRtcAecInit( HTLong SpeexWebRtcAecPt, int SamplingRate, int FrameLen, int WorkMode, int SpeexAecFilterLen, int SpeexAecIsUseRec, float SpeexAecEchoMultiple, float SpeexAecEchoCont, int SpeexAecEchoSupes, int SpeexAecEchoSupesAct, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgnosticMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay, int IsUseSameRoomAec, int SameRoomEchoMinDelay, long ErrInfoVarStrPt );

    //设置SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
    public native int SpeexWebRtcAecSetWebRtcAecmDelay( long SpeexWebRtcAecPt, int WebRtcAecmDelay );

    //获取SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
    public native int SpeexWebRtcAecGetWebRtcAecmDelay( long SpeexWebRtcAecPt, HTInt WebRtcAecmDelayPt );

    //设置SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
    public native int SpeexWebRtcAecSetWebRtcAecDelay( long SpeexWebRtcAecPt, int WebRtcAecDelay );

    //获取SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
    public native int SpeexWebRtcAecGetWebRtcAecDelay( long SpeexWebRtcAecPt, HTInt WebRtcAecDelayPt );

    //用SpeexWebRtc三重声学回音消除器对单声道16位有符号整型PCM格式输入帧进行SpeexWebRtc三重声学回音消除。
    public native int SpeexWebRtcAecProc( long SpeexWebRtcAecPt, short InputFramePt[], short OutputFramePt[], short ResultFramePt[] );

    //销毁SpeexWebRtc三重声学回音消除器。
    public native int SpeexWebRtcAecDestroy( long SpeexWebRtcAecPt );
}