package HeavenTao.Video;

import HeavenTao.Data.*;

//Android系统自带H264编码器类。
public class AndroidSystemH264Decoder
{
    private long m_AndroidSystemH264DecoderPt; //存放Android系统自带H264解码器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "AndroidSystemH264" ); //加载libAndroidSystemH264.so。
    }

    //构造函数。
    public AndroidSystemH264Decoder()
    {
        m_AndroidSystemH264DecoderPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy( null );
    }

    //获取Android系统自带H264解码器的内存指针。
    public long GetAndroidSystemH264DecoderPt()
    {
        return m_AndroidSystemH264DecoderPt;
    }

    //创建并初始化Android系统自带H264解码器。
    public native int Init( VarStr ErrInfoVarStrPt );

    //用Android系统自带H264解码器对H264格式进行8位无符号整型YU12格式帧解码。
    public native int Proc( byte H264FramePt[], long H264FrameLen, byte YU12FramePt[], long YU12FrameSz, HTInt YU12FrameWidth, HTInt YU12FrameHeight, long TimeOutMsec, VarStr ErrInfoVarStrPt );

    //销毁Android系统自带H264解码器。
    public native int Destroy( VarStr ErrInfoVarStrPt );
}