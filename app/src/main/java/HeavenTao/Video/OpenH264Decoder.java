package HeavenTao.Video;

import HeavenTao.Data.*;

//OpenH264解码器类。
public class OpenH264Decoder
{
    private long m_OpenH264DecoderPt; //存放OpenH264解码器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "OpenH264" ); //加载libOpenH264.so。
    }

    //构造函数。
    public OpenH264Decoder()
    {
        m_OpenH264DecoderPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy( null );
    }

    //获取OpenH264解码器的内存指针。
    public long GetOpenH264DecoderPt()
    {
        return m_OpenH264DecoderPt;
    }

    //创建并初始化OpenH264解码器。
    public native int Init( int DecodeThreadNum, VarStr ErrInfoVarStrPt );

    //用OpenH264解码器对8位无符号整型YU12格式帧进行H264格式编码。
    public native int Proc( byte H264FramePt[], long H264FrameLen, byte YU12FramePt[], long YU12FrameSz, HTInt YU12Width, HTInt YU12Height, VarStr ErrInfoVarStrPt );

    //销毁OpenH264解码器。
    public native int Destroy( VarStr ErrInfoVarStrPt );
}