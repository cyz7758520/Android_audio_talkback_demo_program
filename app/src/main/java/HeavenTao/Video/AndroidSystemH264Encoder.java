package HeavenTao.Video;

import HeavenTao.Data.HTLong;
import HeavenTao.Data.VarStr;

//Android系统自带H264编码器类。
public class AndroidSystemH264Encoder
{
    private long m_AndroidSystemH264EncoderPt; //存放Android系统自带H264编码器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "AndroidSystemH264" ); //加载libAndroidSystemH264.so。
    }

    //构造函数。
    public AndroidSystemH264Encoder()
    {
        m_AndroidSystemH264EncoderPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy( null );
    }

    //获取Android系统自带H264编码器的内存指针。
    public long GetAndroidSystemH264EncoderPt()
    {
        return m_AndroidSystemH264EncoderPt;
    }

    //创建并初始化系统自带H264编码器。
    public native int Init( int YU12FrameWidth, int YU12FrameHeight, int EncodedBitrate, int BitrateControlMode, int MaxFrameRate, int IDRFrameIntvlTimeSec, int Complexity, VarStr ErrInfoVarStrPt );

    //用系统自带H264编码器对8位无符号整型YU12格式帧进行H264格式编码。
    public native int Proc( byte YU12FramePt[], long YU12FrameTimeStampMsec, byte H264FramePt[], long H264FrameSz, HTLong H264FrameLenPt, long TimeOutMsec, VarStr ErrInfoVarStrPt );

    //销毁系统自带H264编码器。
    public native int Destroy( VarStr ErrInfoVarStrPt );
}