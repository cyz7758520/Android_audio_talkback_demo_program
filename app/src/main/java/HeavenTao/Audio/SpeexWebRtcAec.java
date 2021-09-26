package HeavenTao.Audio;

import HeavenTao.Data.*;

//SpeexWebRtc三重声学回音消除器类。
public class SpeexWebRtcAec
{
    private long m_SpeexWebRtcAecPt; //存放SpeexWebRtc三重声学回音消除器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "SpeexDsp" ); //加载libSpeex.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
        System.loadLibrary( "SpeexWebRtcAec" ); //加载libSpeexWebRtcAec.so。
    }

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

    //获取SpeexWebRtc三重声学回音消除器的内存指针。
    public long GetSpeexWebRtcAecPt()
    {
        return m_SpeexWebRtcAecPt;
    }

    //创建并初始化SpeexWebRtc三重声学回音消除器。
    public native int Init( int SamplingRate, int FrameLen, int WorkMode, int SpeexAecFilterLen, int SpeexAecIsUseRec, float SpeexAecEchoMultiple, float SpeexAecEchoCont, int SpeexAecEchoSupes, int SpeexAecEchoSupesAct, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgnosticMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay, int IsUseSameRoomAec, int SameRoomEchoMinDelay, VarStr ErrInfoVarStrPt );

    //设置SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
    public native int SetWebRtcAecmDelay( int WebRtcAecmDelay );

    //获取SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟。
    public native int GetWebRtcAecmDelay( HTInt WebRtcAecmDelayPt );

    //设置SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
    public native int SetWebRtcAecDelay( int WebRtcAecDelay );

    //获取SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟。
    public native int GetWebRtcAecDelay( HTInt WebRtcAecDelayPt );

    //用SpeexWebRtc三重声学回音消除器对单声道16位有符号整型PCM格式输入帧进行SpeexWebRtc三重声学回音消除。
    public native int Proc( short InputFramePt[], short OutputFramePt[], short ResultFramePt[] );

    //销毁SpeexWebRtc三重声学回音消除器。
    public native int Destroy();
}