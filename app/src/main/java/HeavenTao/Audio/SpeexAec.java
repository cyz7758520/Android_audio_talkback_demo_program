package HeavenTao.Audio;

import HeavenTao.Data.*;

//Speex声学回音消除器类。
public class SpeexAec
{
    private long m_SpeexAecPt; //存放Speex声学回音消除器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "SpeexDsp" ); //加载libSpeexDsp.so。
    }

    //构造函数。
    public SpeexAec()
    {
        m_SpeexAecPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取Speex声学回音消除器的内存指针。
    public long GetSpeexAecPt()
    {
        return m_SpeexAecPt;
    }

    //创建并初始化Speex声学回音消除器。
    public native int Init( int SamplingRate, int FrameLen, int FilterLen, int IsUseRec, float EchoMultiple, float EchoCont, int EchoSupes, int EchoSupesAct );

    //根据Speex声学回音消除器内存块来创建并初始化Speex声学回音消除器。
    public native int InitByMem( int SamplingRate, int FrameLen, int FilterLen, int IsUseRec, float EchoMultiple, float EchoCont, int EchoSupes, int EchoSupesAct, byte SpeexAecMemPt[], long SpeexAecMemLen );

    //根据Speex声学回音消除器内存块文件来创建并初始化Speex声学回音消除器。
    public native int InitByMemFile( int SamplingRate, int FrameLen, int FilterLen, int IsUseRec, float EchoMultiple, float EchoCont, int EchoSupes, int EchoSupesAct, String SpeexAecMemFileFullPathStrPt, VarStr ErrInfoVarStrPt );

    //获取Speex声学回音消除器内存块的数据长度。
    public native int GetMemLen( int SamplingRate, int FrameLen, int FilterLen, int IsUseRec, float EchoMultiple, float EchoCont, int EchoSupes, int EchoSupesAct, HTLong SpeexAecMemLenPt );

    //获取Speex声学回音消除器的内存块。
    public native int GetMem( int SamplingRate, int FrameLen, int FilterLen, int IsUseRec, float EchoMultiple, float EchoCont, int EchoSupes, int EchoSupesAct, byte SpeexAecMemPt[], long SpeexAecMemSz );

    //将Speex声学回音消除器内存块保存到指定的文件。
    public native int SaveMemFile( int SamplingRate, int FrameLen, int FilterLen, int IsUseRec, float EchoMultiple, float EchoCont, int EchoSupes, int EchoSupesAct, String SpeexAecMemFileFullPathStrPt, VarStr ErrInfoVarStrPt );

    //用Speex声学回音消除器对单声道16位有符号整型PCM格式输入帧进行Speex声学回音消除。
    public native int Proc( short InputFramePt[], short OutputFramePt[], short ResultFramePt[] );

    //销毁Speex声学回音消除器。
    public native int Destroy();
}