package HeavenTao.Video;

import HeavenTao.Data.*;

//OpenH264编码器类。
public class OpenH264Encoder
{
    private long m_OpenH264EncoderPt; //存放OpenH264编码器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "OpenH264" ); //加载libOpenH264.so。
    }

    //构造函数。
    public OpenH264Encoder()
    {
        m_OpenH264EncoderPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy( null );
    }

    //获取OpenH264编码器的内存指针。
    public long GetOpenH264EncoderPt()
    {
        return m_OpenH264EncoderPt;
    }

    //创建并初始化OpenH264编码器。
    public native int Init( int EncodedPictrWidth, int EncodedPictrHeight, int VideoType, int EncodedBitrate, int BitrateControlMode, int MaxFrameRate, int IDRFrameIntvl, int Complexity, VarStr ErrInfoVarStrPt );

    //设置OpenH264编码器的编码后比特率。
    public native int SetEncodedBitrate( int EncodedBitrate, VarStr ErrInfoVarStrPt );

    //获取OpenH264编码器的编码后比特率。
    public native int GetEncodedBitrate( HTInt EncodedBitratePt, VarStr ErrInfoVarStrPt );

    //用OpenH264编码器对8位无符号整型YU12格式帧进行H264格式编码。
    public native int Proc( byte YU12FramePt[], int YU12Width, int YU12Height, long YU12FrameTimeStamp, byte H264FramePt[], long H264FrameSz, HTLong H264FrameLenPt, VarStr ErrInfoVarStrPt );

    //销毁OpenH264编码器。
    public native int Destroy( VarStr ErrInfoVarStrPt );
}