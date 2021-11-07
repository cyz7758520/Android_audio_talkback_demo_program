package HeavenTao.Video;

import HeavenTao.Data.*;

//OpenH264编码器类。
public class OpenH264Encoder
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "OpenH264" ); //加载libOpenH264.so。
    }

    public long m_OpenH264EncoderPt; //存放OpenH264编码器的内存指针。

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

    //创建并初始化OpenH264编码器。
    public int Init( int EncodedPictrWidth, int EncodedPictrHeight, int VideoType, int EncodedBitrate, int BitrateControlMode, int MaxFrameRate, int IDRFrameIntvlFrameCnt, int Complexity, VarStr ErrInfoVarStrPt )
    {
        if( m_OpenH264EncoderPt == 0 )
        {
            HTLong p_OpenH264EncoderPt = new HTLong();
            if( OpenH264EncoderInit( p_OpenH264EncoderPt, EncodedPictrWidth, EncodedPictrHeight, VideoType, EncodedBitrate, BitrateControlMode, MaxFrameRate, IDRFrameIntvlFrameCnt, Complexity, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_OpenH264EncoderPt = p_OpenH264EncoderPt.m_Val;
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

    //设置OpenH264编码器的编码后比特率。
    public int SetEncodedBitrate( int EncodedBitrate, VarStr ErrInfoVarStrPt )
    {
        return OpenH264EncoderSetEncodedBitrate( m_OpenH264EncoderPt, EncodedBitrate, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //获取OpenH264编码器的编码后比特率。
    public int GetEncodedBitrate( HTInt EncodedBitratePt, VarStr ErrInfoVarStrPt )
    {
        return OpenH264EncoderGetEncodedBitrate( m_OpenH264EncoderPt, EncodedBitratePt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //用OpenH264编码器对8位无符号整型YU12格式帧进行H264格式编码。
    public int Proc( byte YU12FramePt[], int YU12FrameWidth, int YU12FrameHeight, long YU12FrameTimeStampMsec,
                     byte H264FramePt[], long H264FrameSz, HTLong H264FrameLenPt,
                     VarStr ErrInfoVarStrPt )
    {
        return OpenH264EncoderProc( m_OpenH264EncoderPt,
                                    YU12FramePt, YU12FrameWidth, YU12FrameHeight, YU12FrameTimeStampMsec,
                                    H264FramePt, H264FrameSz, H264FrameLenPt,
                                    ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //销毁OpenH264编码器。
    public int Destroy( VarStr ErrInfoVarStrPt )
    {
        if( m_OpenH264EncoderPt != 0 )
        {
            if( OpenH264EncoderDestroy( m_OpenH264EncoderPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_OpenH264EncoderPt = 0;
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

    //创建并初始化OpenH264编码器。
    public native int OpenH264EncoderInit( HTLong OpenH264EncoderPt, int EncodedPictrWidth, int EncodedPictrHeight, int VideoType, int EncodedBitrate, int BitrateControlMode, int MaxFrameRate, int IDRFrameIntvlFrameCnt, int Complexity, long ErrInfoVarStrPt );

    //设置OpenH264编码器的编码后比特率。
    public native int OpenH264EncoderSetEncodedBitrate( long OpenH264EncoderPt, int EncodedBitrate, long ErrInfoVarStrPt );

    //获取OpenH264编码器的编码后比特率。
    public native int OpenH264EncoderGetEncodedBitrate( long OpenH264EncoderPt, HTInt EncodedBitratePt, long ErrInfoVarStrPt );

    //用OpenH264编码器对8位无符号整型YU12格式帧进行H264格式编码。
    public native int OpenH264EncoderProc( long OpenH264EncoderPt,
                                           byte YU12FramePt[], int YU12FrameWidth, int YU12FrameHeight, long YU12FrameTimeStampMsec,
                                           byte H264FramePt[], long H264FrameSz, HTLong H264FrameLenPt,
                                           long ErrInfoVarStrPt );

    //销毁OpenH264编码器。
    public native int OpenH264EncoderDestroy( long OpenH264EncoderPt, long ErrInfoVarStrPt );
}