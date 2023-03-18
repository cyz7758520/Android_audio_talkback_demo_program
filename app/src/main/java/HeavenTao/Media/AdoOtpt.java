package HeavenTao.Media;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceView;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import HeavenTao.Ado.*;
import HeavenTao.Data.*;

public class AdoOtpt //音频输出。
{
    MediaPocsThrd m_MediaPocsThrdPt; //存放媒体处理线程的指针。

    public int m_IsUseAdoOtpt; //存放是否使用音频输入，为0表示不使用，为非0表示要使用。
    public int m_IsInitAdoOtpt; //存放是否初始化音频输入，为0表示未初始化，为非0表示已初始化。

    public int m_SmplRate; //存放采样频率，取值只能为8000、16000、32000、48000。
    public int m_FrmLenMsec; //存放帧的长度，单位为毫秒，取值只能为10毫秒的倍数。
    public int m_FrmLenUnit; //存放帧的长度，单位为采样单元，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960，48000Hz的10毫秒为480、20毫秒为960、30毫秒为1440。
    public int m_FrmLenData; //存放帧的长度，单位为采样数据，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960，48000Hz的10毫秒为480、20毫秒为960、30毫秒为1440。
    public int m_FrmLenByt; //存放帧的长度，单位为字节，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80*2、20毫秒为160*2、30毫秒为240*2，16000Hz的10毫秒为160*2、20毫秒为320*2、30毫秒为480*2，32000Hz的10毫秒为320*2、20毫秒为640*2、30毫秒为960*2，48000Hz的10毫秒为480*2、20毫秒为960*2、30毫秒为1440*2。

    public class AdoOtptStrm //音频输出流。
    {
        public int m_AdoOtptStrmIdx; //存放音频输出流索引。

        public int m_IsUseAdoOtptStrm; //存放是否使用音频输出流，为0表示不使用，为非0表示要使用。

        public int m_UseWhatDecd; //存放使用什么解码器，为0表示PCM原始数据，为1表示Speex解码器，为2表示Opus解码器。

        public SpeexDecd m_SpeexDecdPt; //存放Speex解码器的指针。
        public int m_SpeexDecdIsUsePrcplEnhsmt; //存放Speex解码器是否使用知觉增强，为非0表示要使用，为0表示不使用。

        //初始化音频输出流。
        public int Init()
        {
            int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

            Out:
            {
                //初始化解码器。
                switch( m_UseWhatDecd )
                {
                    case 0: //如果要使用PCM原始数据。
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引 " + m_AdoOtptStrmIdx + "：初始化PCM原始数据成功。" );
                        break;
                    }
                    case 1: //如果要使用Speex解码器。
                    {
                        if( m_FrmLenMsec != 20 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引 " + m_AdoOtptStrmIdx + "：帧的长度不为20毫秒不能使用Speex解码器。" );
                            break Out;
                        }
                        m_SpeexDecdPt = new SpeexDecd();
                        if( m_SpeexDecdPt.Init( m_SmplRate, m_SpeexDecdIsUsePrcplEnhsmt ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引 " + m_AdoOtptStrmIdx + "：初始化Speex解码器成功。" );
                        }
                        else
                        {
                            m_SpeexDecdPt = null;
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引 " + m_AdoOtptStrmIdx + "：初始化Speex解码器失败。" );
                            break Out;
                        }
                        break;
                    }
                    case 2: //如果要使用Opus解码器。
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引 " + m_AdoOtptStrmIdx + "：暂不支持使用Opus解码器。" );
                        break Out;
                    }
                }

                p_Rslt = 0; //设置本函数执行成功。
            }

            //if( p_Rslt != 0 ) //如果本函数执行失败。
            {
            }
            return p_Rslt;
        }

        //销毁音频输出流。
        public void Dstoy()
        {
            //销毁解码器。
            switch( m_UseWhatDecd )
            {
                case 0: //如果要使用PCM原始数据。
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引 " + m_AdoOtptStrmIdx + "：销毁PCM原始数据成功。" );
                    break;
                }
                case 1: //如果要使用Speex解码器。
                {
                    if( m_SpeexDecdPt != null )
                    {
                        if( m_SpeexDecdPt.Dstoy() == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引 " + m_AdoOtptStrmIdx + "：销毁Speex解码器成功。" );
                        }
                        else
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引 " + m_AdoOtptStrmIdx + "：销毁Speex解码器失败。" );
                        }
                        m_SpeexDecdPt = null;
                    }
                    break;
                }
                case 2: //如果要使用Opus解码器。
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引 " + m_AdoOtptStrmIdx + "：销毁Opus解码器成功。" );
                    break;
                }
            }
        }
    }
    public LinkedList< AdoOtptStrm > m_AdoOtptStrmLnkLstPt; //存放音频输出流链表的指针。
    public int m_AdoOtptStrmUseTotal; //存放音频输出流要使用的总数。

    public int m_IsDrawAdoWavfmToSurface; //存放是否绘制音频波形到Surface，为非0表示要绘制，为0表示不绘制。
    SurfaceView m_AdoOtptSrcWavfmSurfacePt; //存放音频输出原始波形Surface的指针。
    AdoWavfm m_AdoOtptSrcWavfmPt; //存放音频输出原始波形器的指针。

    public int m_IsSaveAdoToWaveFile; //存放是否保存音频到Wave文件，为非0表示要保存，为0表示不保存。
    WaveFileWriter m_AdoOtptSrcWaveFileWriterPt; //存放音频输出原始Wave文件写入器的指针。
    String m_AdoOtptWaveFileFullPathStrPt; //存放音频输出原始Wave文件完整路径字符串的指针。
    long m_AdoOtptWaveFileWrBufSzByt; //存放音频输出Wave文件写入缓冲区的大小，单位为字节。

    public AudioTrack m_AdoOtptDvcPt; //存放音频输出设备的指针。
    int m_AdoOtptDvcBufSzByt; //存放音频输出设备缓冲区大小，单位为字节。
    public int m_UseWhatAdoOtptDvc; //存放使用什么音频输出设备，为0表示扬声器，为非0表示听筒。
    public int m_UseWhatAdoOtptStreamType; //存放使用什么音频输出流类型，为0表示通话类型，为非0表示媒体类型。
    public int m_AdoOtptIsMute; //存放音频输出是否静音，为0表示有声音，为非0表示静音。

    public LinkedList< short[] > m_PcmAdoOtptSrcFrmLnkLstPt; //存放Pcm格式音频输出原始帧链表的指针。
    public LinkedList< short[] > m_PcmAdoOtptIdleFrmLnkLstPt; //存放Pcm格式音频输出空闲帧链表的指针。

    int m_IsInitAdoOtptThrdTmpVar; //存放是否初始化音频输出线程的临时变量。
    short m_PcmAdoOtptSrcFrmPt[]; //存放Pcm格式音频输出原始帧的指针。
    byte m_EncdAdoOtptSrcFrmPt[]; //存放已编码格式音频输出原始帧的指针。
    HTLong m_EncdAdoOtptSrcFrmLenBytPt; //存放已编码格式音频输出原始帧长度的指针，单位为字节。
    int m_PcmAdoOtptMixFrmPt[]; //存放Pcm格式音频输出混音帧的指针。
    int m_FrmLnkLstElmTotal; //存放帧链表的元素总数。
    long m_LastTickMsec; //存放上次的嘀嗒钟，单位为毫秒。
    long m_NowTickMsec; //存放本次的嘀嗒钟，单位为毫秒。

    AdoOtptThrd m_AdoOtptThrdPt; //存放音频输出线程的指针。
    int m_AdoOtptThrdIsStart; //存放音频输出线程是否开始，为0表示未开始，为1表示已开始。
    int m_AdoOtptThrdExitFlag; //存放音频输出线程退出标记，为0表示保持运行，为1表示请求退出。

    //添加音频输出流。
    public void AddAdoOtptStrm( int AdoOtptStrmIdx )
    {
        synchronized( m_AdoOtptStrmLnkLstPt )
        {
            //查找音频输出流索引。
            for( AdoOtptStrm p_AdoOtptStrmPt : m_AdoOtptStrmLnkLstPt )
            {
                if( p_AdoOtptStrmPt.m_AdoOtptStrmIdx == AdoOtptStrmIdx ) //如果音频输出流索引找到了。
                {
                    return;
                }
            }

            //添加到音频输出流信息链表。
            AdoOtptStrm p_AdoOtptStrmPt = new AdoOtptStrm();
            p_AdoOtptStrmPt.m_AdoOtptStrmIdx = AdoOtptStrmIdx;
            m_AdoOtptStrmLnkLstPt.addLast( p_AdoOtptStrmPt );
        }
    }

    //删除音频输出流。
    public void DelAdoOtptStrm( int AdoOtptStrmIdx )
    {
        synchronized( m_AdoOtptStrmLnkLstPt )
        {
            //查找音频输出流索引。
            for( Iterator< AdoOtptStrm > p_AdoOtptStrmItrtr = m_AdoOtptStrmLnkLstPt.iterator(); p_AdoOtptStrmItrtr.hasNext(); )
            {
                AdoOtptStrm p_AdoOtptStrmPt = p_AdoOtptStrmItrtr.next();
                if( p_AdoOtptStrmPt.m_AdoOtptStrmIdx == AdoOtptStrmIdx ) //如果音频输出流索引找到了。
                {
                    p_AdoOtptStrmPt.Dstoy();
                    p_AdoOtptStrmItrtr.remove();
                    return;
                }
            }
        }
    }

    //设置音频输出流要使用PCM原始数据。
    public void SetAdoOtptStrmUsePcm( int AdoOtptStrmIdx )
    {
        synchronized( m_AdoOtptStrmLnkLstPt )
        {
            //查找音频输出流索引。
            for( AdoOtptStrm p_AdoOtptStrmPt : m_AdoOtptStrmLnkLstPt )
            {
                if( p_AdoOtptStrmPt.m_AdoOtptStrmIdx == AdoOtptStrmIdx ) //如果音频输出流索引找到了。
                {
                    if( ( m_IsInitAdoOtpt != 0 ) && ( p_AdoOtptStrmPt.m_IsUseAdoOtptStrm != 0 ) ) p_AdoOtptStrmPt.Dstoy();

                    p_AdoOtptStrmPt.m_UseWhatDecd = 0;

                    if( ( m_IsInitAdoOtpt != 0 ) && ( p_AdoOtptStrmPt.m_IsUseAdoOtptStrm != 0 ) ) p_AdoOtptStrmPt.Init();
                    return;
                }
            }
        }
    }

    //设置音频输出流要使用Speex解码器。
    public void SetAdoOtptStrmUseSpeexDecd( int AdoOtptStrmIdx, int IsUsePrcplEnhsmt )
    {
        synchronized( m_AdoOtptStrmLnkLstPt )
        {
            //查找音频输出流索引。
            for( AdoOtptStrm p_AdoOtptStrmPt : m_AdoOtptStrmLnkLstPt )
            {
                if( p_AdoOtptStrmPt.m_AdoOtptStrmIdx == AdoOtptStrmIdx ) //如果音频输出流索引找到了。
                {
                    if( ( m_IsInitAdoOtpt != 0 ) && ( p_AdoOtptStrmPt.m_IsUseAdoOtptStrm != 0 ) ) p_AdoOtptStrmPt.Dstoy();

                    p_AdoOtptStrmPt.m_UseWhatDecd = 1;
                    p_AdoOtptStrmPt.m_SpeexDecdIsUsePrcplEnhsmt = IsUsePrcplEnhsmt;

                    if( ( m_IsInitAdoOtpt != 0 ) && ( p_AdoOtptStrmPt.m_IsUseAdoOtptStrm != 0 ) ) p_AdoOtptStrmPt.Init();
                    return;
                }
            }
        }
    }

    //设置音频输出流要使用Opus编码器。
    public void SetAdoOtptStrmUseOpusDecd( int AdoOtptStrmIdx )
    {
        synchronized( m_AdoOtptStrmLnkLstPt )
        {
            //查找音频输出流索引。
            for( AdoOtptStrm p_AdoOtptStrmPt : m_AdoOtptStrmLnkLstPt )
            {
                if( p_AdoOtptStrmPt.m_AdoOtptStrmIdx == AdoOtptStrmIdx ) //如果音频输出流索引找到了。
                {
                    if( ( m_IsInitAdoOtpt != 0 ) && ( p_AdoOtptStrmPt.m_IsUseAdoOtptStrm != 0 ) ) p_AdoOtptStrmPt.Dstoy();

                    p_AdoOtptStrmPt.m_UseWhatDecd = 2;

                    if( ( m_IsInitAdoOtpt != 0 ) && ( p_AdoOtptStrmPt.m_IsUseAdoOtptStrm != 0 ) ) p_AdoOtptStrmPt.Init();
                    return;
                }
            }
        }
    }

    //设置是否使用音频输出流。
    public void SetAdoOtptStrmIsUse( int AdoOtptStrmIdx, int IsUseAdoOtptStrm )
    {
        synchronized( m_AdoOtptStrmLnkLstPt )
        {
            //查找音频输出流索引。
            for( AdoOtptStrm p_AdoOtptStrmPt : m_AdoOtptStrmLnkLstPt )
            {
                if( p_AdoOtptStrmPt.m_AdoOtptStrmIdx == AdoOtptStrmIdx ) //如果音频输出流索引找到了。
                {
                    if( IsUseAdoOtptStrm != 0 ) //如果要使用音频输出流。
                    {
                        if( p_AdoOtptStrmPt.m_IsUseAdoOtptStrm == 0 ) //如果当前不使用音频输出流。
                        {
                            if( m_IsInitAdoOtpt != 0 ) //如果已初始化音频输出。
                            {
                                if( p_AdoOtptStrmPt.Init() == 0 ) //如果初始化音频输出流成功。
                                {
                                    p_AdoOtptStrmPt.m_IsUseAdoOtptStrm = 1;
                                    m_AdoOtptStrmUseTotal++;
                                }
                            }
                            else //如果未初始化音频输出。
                            {
                                p_AdoOtptStrmPt.m_IsUseAdoOtptStrm = 1;
                                m_AdoOtptStrmUseTotal++;
                            }
                        }
                    }
                    else //如果不使用音频输出流。
                    {
                        if( p_AdoOtptStrmPt.m_IsUseAdoOtptStrm != 0 ) //如果当前要使用音频输出流。
                        {
                            if( m_IsInitAdoOtpt != 0 ) //如果已初始化音频输出。
                            {
                                p_AdoOtptStrmPt.Dstoy();
                                p_AdoOtptStrmPt.m_IsUseAdoOtptStrm = 0;
                                m_AdoOtptStrmUseTotal--;
                            }
                            else //如果未初始化音频输出。
                            {
                                p_AdoOtptStrmPt.m_IsUseAdoOtptStrm = 0;
                                m_AdoOtptStrmUseTotal--;
                            }
                        }
                    }
                }
                return;
            }
        }
    }

    //初始化音频输出流链表。
    public int AdoOtptStrmLnkLstInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            m_AdoOtptStrmUseTotal = 0;
            for( AdoOtptStrm p_AdoOtptStrmPt : m_AdoOtptStrmLnkLstPt )
            {
                if( p_AdoOtptStrmPt.m_IsUseAdoOtptStrm != 0 )
                {
                    if( p_AdoOtptStrmPt.Init() != 0 ) break Out;
                    m_AdoOtptStrmUseTotal++;
                }
            }
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输出流链表成功。" );

            p_Rslt = 0; //设置本函数执行成功。
        }

        //if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_Rslt;
    }

    //销毁音频输出流链表。
    public void AdoOtptStrmLnkLstDstoy()
    {
        for( AdoOtptStrm p_AdoOtptStrmPt : m_AdoOtptStrmLnkLstPt )
        {
            p_AdoOtptStrmPt.Dstoy();
        }
        m_AdoOtptStrmUseTotal = 0;
        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输出流链表成功。" );
    }

    //初始化音频输出原始波形器。
    public int WavfmInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            if( m_IsDrawAdoWavfmToSurface != 0 )
            {
                m_AdoOtptSrcWavfmPt = new AdoWavfm();
                if( m_AdoOtptSrcWavfmPt.Init( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输出原始波形器成功。" );
                }
                else
                {
                    m_AdoOtptSrcWavfmPt = null;
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输出原始波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                    break Out;
                }
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        //if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_Rslt;
    }

    //销毁音频输出原始波形器。
    public void WavfmDstoy()
    {
        if( m_IsDrawAdoWavfmToSurface != 0 )
        {
            if( m_AdoOtptSrcWavfmPt != null )
            {
                if( m_AdoOtptSrcWavfmPt.Dstoy( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输出原始波形器成功。" );
                }
                else
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输出原始波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                }
                m_AdoOtptSrcWavfmPt = null;
            }
        }
    }

    //初始化音频输出原始Wave文件写入器。
    public int WaveFileWriterInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            if( m_IsSaveAdoToWaveFile != 0 )
            {
                m_AdoOtptSrcWaveFileWriterPt = new WaveFileWriter();
                if( m_AdoOtptSrcWaveFileWriterPt.Init( m_AdoOtptWaveFileFullPathStrPt, m_AdoOtptWaveFileWrBufSzByt, ( short ) 1, m_SmplRate, 16 ) == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输出原始Wave文件 " + m_AdoOtptWaveFileFullPathStrPt + " 的Wave文件写入器成功。" );
                }
                else
                {
                    m_AdoOtptSrcWaveFileWriterPt = null;
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输出原始Wave文件 " + m_AdoOtptWaveFileFullPathStrPt + " 的Wave文件写入器失败。" );
                    break Out;
                }
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        //if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_Rslt;
    }

    //销毁音频输出原始Wave文件写入器。
    public void WaveFileWriterDstoy()
    {
        if( m_IsSaveAdoToWaveFile != 0 )
        {
            if( m_AdoOtptSrcWaveFileWriterPt != null )
            {
                if( m_AdoOtptSrcWaveFileWriterPt.Dstoy() == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输出原始Wave文件写入器成功。" );
                }
                else
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输出原始Wave文件写入器失败。" );
                }
                m_AdoOtptSrcWaveFileWriterPt = null;
            }
        }
    }

    //初始化音频输出设备和线程。
    public int DvcAndThrdInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            //设置音频输出设备。
            if( m_UseWhatAdoOtptDvc == 0 ) //如果要使用扬声器。
            {
                ( ( AudioManager )MediaPocsThrd.m_MainActivityPt.getSystemService( Context.AUDIO_SERVICE ) ).setSpeakerphoneOn( true ); //打开扬声器。
            }
            else //如果要使用听筒。
            {
                ( ( AudioManager )MediaPocsThrd.m_MainActivityPt.getSystemService( Context.AUDIO_SERVICE ) ).setSpeakerphoneOn( false ); //关闭扬声器。
            }

            //用第一种方法创建并初始化音频输出设备。
            try
            {
                m_AdoOtptDvcBufSzByt = 2;
                m_AdoOtptDvcPt = new AudioTrack(
                        ( m_UseWhatAdoOtptStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
                        m_SmplRate,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        m_AdoOtptDvcBufSzByt,
                        AudioTrack.MODE_STREAM );
                if( m_AdoOtptDvcPt.getState() == AudioTrack.STATE_INITIALIZED )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：用第一种方法初始化音频输出设备成功。音频输出设备缓冲区大小：" + m_AdoOtptDvcBufSzByt );
                }
                else
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：用第一种方法初始化音频输出设备失败。" );
                    m_AdoOtptDvcPt.release();
                    m_AdoOtptDvcPt = null;
                }
            }
            catch( IllegalArgumentException e )
            {
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：用第一种方法初始化音频输出设备失败。原因：" + e.getMessage() );
            }

            //用第二种方法初始化音频输出设备。
            if( m_AdoOtptDvcPt == null )
            {
                try
                {
                    m_AdoOtptDvcBufSzByt = AudioTrack.getMinBufferSize( m_SmplRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
                    m_AdoOtptDvcPt = new AudioTrack( ( m_UseWhatAdoOtptStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
                            m_SmplRate,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            m_AdoOtptDvcBufSzByt,
                            AudioTrack.MODE_STREAM );
                    if( m_AdoOtptDvcPt.getState() == AudioTrack.STATE_INITIALIZED )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：用第二种方法初始化音频输出设备成功。音频输出设备缓冲区大小：" + m_AdoOtptDvcBufSzByt );
                    }
                    else
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：用第二种方法初始化音频输出设备失败。" );
                        break Out;
                    }
                }
                catch( IllegalArgumentException e )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：用第二种方法初始化音频输出设备失败。原因：" + e.getMessage() );
                    break Out;
                }
            }

            //初始化Pcm格式音频输出原始帧链表。
            m_PcmAdoOtptSrcFrmLnkLstPt = new LinkedList< short[] >();
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化Pcm格式音频输出原始帧链表成功。" );

            //初始化Pcm格式音频输出空闲帧链表。
            m_PcmAdoOtptIdleFrmLnkLstPt = new LinkedList< short[] >();
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化Pcm格式音频输出空闲帧链表成功。" );

            //初始化音频输出线程的临时变量。
            {
                m_IsInitAdoOtptThrdTmpVar = 1; //设置已初始化音频输出线程的临时变量。
                m_PcmAdoOtptSrcFrmPt = null; //初始化Pcm格式音频输出原始帧的指针。
                m_PcmAdoOtptMixFrmPt = new int[ m_FrmLenUnit ]; //初始化Pcm格式音频输出混音帧的指针。
                m_EncdAdoOtptSrcFrmPt = new byte[ m_FrmLenByt ]; //初始化已编码格式音频输出原始帧的指针。
                m_EncdAdoOtptSrcFrmLenBytPt = new HTLong(); //初始化已编码格式音频输出原始帧长度的指针。
                m_FrmLnkLstElmTotal = 0; //初始化帧链表的元素总数。
                m_LastTickMsec = 0; //初始化上次的嘀嗒钟。
                m_NowTickMsec = 0; //初始化本次的嘀嗒钟。
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输出线程的临时变量成功。" );
            }

            //初始化音频输出线程。
            {
                m_AdoOtptThrdIsStart = 0; //设置音频输出线程为未开始。
                m_AdoOtptThrdExitFlag = 0; //设置音频输出线程退出标记为0表示保持运行。
                m_AdoOtptThrdPt = new AdoOtptThrd();
                m_AdoOtptThrdPt.start();
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输出线程成功。" );
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        //if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_Rslt;
    }

    //销毁音频输出设备和线程。
    public void DvcAndThrdDstoy()
    {
        //销毁音频输出线程。
        if( m_AdoOtptThrdPt != null )
        {
            m_AdoOtptThrdExitFlag = 1; //请求音频输出线程退出。
            try
            {
                m_AdoOtptThrdPt.join(); //等待音频输出线程退出。
            }
            catch( InterruptedException ignored )
            {
            }
            m_AdoOtptThrdPt = null;
            m_AdoOtptThrdIsStart = 0;
            m_AdoOtptThrdExitFlag = 0;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输出线程成功。" );
        }

        //销毁音频输出线程的临时变量。
        if( m_IsInitAdoOtptThrdTmpVar != 0 )
        {
            m_IsInitAdoOtptThrdTmpVar = 0; //设置未初始化音频输出线程的临时变量。
            m_PcmAdoOtptSrcFrmPt = null; //销毁Pcm格式音频输出原始帧的指针。
            m_PcmAdoOtptMixFrmPt = null; //销毁Pcm格式音频输出混音帧的指针。
            m_EncdAdoOtptSrcFrmPt = null; //销毁已编码格式音频输出原始帧的指针。
            m_EncdAdoOtptSrcFrmLenBytPt = null; //销毁已编码格式音频输出原始帧长度的指针。
            m_FrmLnkLstElmTotal = 0; //销毁帧链表的元素总数。
            m_LastTickMsec = 0; //销毁上次的嘀嗒钟。
            m_NowTickMsec = 0; //销毁本次的嘀嗒钟。
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输出线程的临时变量成功。" );
        }

        //销毁Pcm格式音频输出空闲帧链表。
        if( m_PcmAdoOtptIdleFrmLnkLstPt != null )
        {
            m_PcmAdoOtptIdleFrmLnkLstPt.clear();
            m_PcmAdoOtptIdleFrmLnkLstPt = null;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁Pcm格式音频输出空闲帧链表成功。" );
        }

        //销毁Pcm格式音频输出原始帧链表。
        if( m_PcmAdoOtptSrcFrmLnkLstPt != null )
        {
            m_PcmAdoOtptSrcFrmLnkLstPt.clear();
            m_PcmAdoOtptSrcFrmLnkLstPt = null;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁Pcm格式音频输出原始帧链表成功。" );
        }

        //销毁音频输出设备。
        if( m_AdoOtptDvcPt != null )
        {
            if( m_AdoOtptDvcPt.getPlayState() != AudioTrack.PLAYSTATE_STOPPED ) m_AdoOtptDvcPt.stop();
            m_AdoOtptDvcPt.release();
            m_AdoOtptDvcPt = null;
            m_AdoOtptDvcBufSzByt = 0;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输出设备成功。" );
        }
    }

    //初始化音频输出。
    public int Init()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
        long p_LastMsec = 0;
        long p_NowMsec = 0;

        Out:
        {
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastMsec = SystemClock.uptimeMillis(); //记录初始化开始的时间。

            if( AdoOtptStrmLnkLstInit() != 0 ) break Out;
            if( WaveFileWriterInit() != 0 ) break Out;
            if( WavfmInit() != 0 ) break Out;
            if( DvcAndThrdInit() != 0 ) break Out;

            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
            {
                p_NowMsec = SystemClock.uptimeMillis(); //记录初始化结束的时间。
                Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输出耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
            Dstoy();
        }
        return p_Rslt;
    }

    //销毁音频输出。
    public void Dstoy()
    {
        DvcAndThrdDstoy();
        WavfmDstoy();
        WaveFileWriterDstoy();
        AdoOtptStrmLnkLstDstoy();
    }

    //音频输出线程。
    public class AdoOtptThrd extends Thread
    {
        public void run()
        {
            while( m_AdoOtptThrdIsStart == 0 ) SystemClock.sleep( 1 ); //等待音频输出线程开始。

            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：开始准备音频输出。" );

            //音频输出循环开始。
            while( true )
            {
                OutPocs:
                {
                    //获取一个Pcm格式音频输出空闲帧。
                    m_FrmLnkLstElmTotal = m_PcmAdoOtptIdleFrmLnkLstPt.size(); //获取Pcm格式音频输出空闲帧链表的元素总数。
                    if( m_FrmLnkLstElmTotal > 0 ) //如果Pcm格式音频输出空闲帧链表中有帧。
                    {
                        //从Pcm格式音频输出空闲帧链表中取出第一个帧。
                        synchronized( m_PcmAdoOtptIdleFrmLnkLstPt )
                        {
                            m_PcmAdoOtptSrcFrmPt = m_PcmAdoOtptIdleFrmLnkLstPt.getFirst();
                            m_PcmAdoOtptIdleFrmLnkLstPt.removeFirst();
                        }
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：从Pcm格式音频输出空闲帧链表中取出第一个帧，Pcm格式音频输出空闲帧链表元素总数：" + m_FrmLnkLstElmTotal + "。" );
                    }
                    else //如果Pcm格式音频输出空闲帧链表中没有帧。
                    {
                        m_FrmLnkLstElmTotal = m_PcmAdoOtptSrcFrmLnkLstPt.size(); //获取Pcm格式音频输出原始帧链表的元素总数。
                        if( m_FrmLnkLstElmTotal <= 50 )
                        {
                            m_PcmAdoOtptSrcFrmPt = new short[ m_FrmLenUnit ]; //创建一个Pcm格式音频输出空闲帧。
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：Pcm格式音频输出空闲帧链表中没有帧，创建一个Pcm格式音频输出空闲帧成功。" );
                        }
                        else
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：Pcm格式音频输出原始帧链表中帧总数为" + m_FrmLnkLstElmTotal + "已经超过上限50，不再创建Pcm格式音频输出空闲帧。" );
                            SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
                            break OutPocs;
                        }
                    }

                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) m_LastTickMsec = SystemClock.uptimeMillis();

                    //调用用户定义的写入音频输出帧函数，并解码成PCM原始数据，最后混音。
                    synchronized( m_AdoOtptStrmLnkLstPt )
                    {
                        if( m_AdoOtptStrmUseTotal > 0 )
                        {
                            Iterator< AdoOtptStrm > p_AdoOtptStrmItrtr = m_AdoOtptStrmLnkLstPt.iterator();

                            while( p_AdoOtptStrmItrtr.hasNext() ) //查找第一条要使用的音频输出流。
                            {
                                AdoOtptStrm p_AdoOtptStrmPt = ( AdoOtptStrm )p_AdoOtptStrmItrtr.next();

                                if( p_AdoOtptStrmPt.m_IsUseAdoOtptStrm != 0 ) //如果该音频输出流为要使用。
                                {
                                    switch( p_AdoOtptStrmPt.m_UseWhatDecd ) //使用什么解码器。
                                    {
                                        case 0: //如果要使用PCM原始数据。
                                        {
                                            //调用用户定义的写入音频输出帧函数。
                                            m_MediaPocsThrdPt.UserWriteAdoOtptFrm(
                                                    p_AdoOtptStrmPt.m_AdoOtptStrmIdx,
                                                    m_PcmAdoOtptSrcFrmPt, m_FrmLenUnit,
                                                    null, 0, null );

                                            //调用用户定义的获取Pcm格式音频输出帧函数。
                                            m_MediaPocsThrdPt.UserGetAdoOtptFrm(
                                                    p_AdoOtptStrmPt.m_AdoOtptStrmIdx,
                                                    m_PcmAdoOtptSrcFrmPt, m_FrmLenUnit,
                                                    null, 0 );
                                            break;
                                        }
                                        case 1: //如果要使用Speex解码器。
                                        {
                                            //调用用户定义的写入音频输出帧函数。
                                            m_MediaPocsThrdPt.UserWriteAdoOtptFrm(
                                                    p_AdoOtptStrmPt.m_AdoOtptStrmIdx,
                                                    null, 0,
                                                    m_EncdAdoOtptSrcFrmPt, m_EncdAdoOtptSrcFrmPt.length, m_EncdAdoOtptSrcFrmLenBytPt );

                                            //使用Speex解码器。
                                            if( p_AdoOtptStrmPt.m_SpeexDecdPt.Pocs( m_EncdAdoOtptSrcFrmPt, m_EncdAdoOtptSrcFrmLenBytPt.m_Val, m_PcmAdoOtptSrcFrmPt ) == 0 )
                                            {
                                                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引 " + p_AdoOtptStrmPt.m_AdoOtptStrmIdx + "：使用Speex解码器成功。" );
                                            }
                                            else
                                            {
                                                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引 " + p_AdoOtptStrmPt.m_AdoOtptStrmIdx + "：使用Speex解码器失败。" );
                                            }

                                            //调用用户定义的获取Pcm格式音频输出帧函数。
                                            m_MediaPocsThrdPt.UserGetAdoOtptFrm(
                                                    p_AdoOtptStrmPt.m_AdoOtptStrmIdx,
                                                    m_PcmAdoOtptSrcFrmPt, m_FrmLenUnit,
                                                    m_EncdAdoOtptSrcFrmPt, m_EncdAdoOtptSrcFrmLenBytPt.m_Val );
                                            break;
                                        }
                                        case 2: //如果要使用Opus解码器。
                                        {
                                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引 " + p_AdoOtptStrmPt.m_AdoOtptStrmIdx + "：暂不支持使用Opus解码器。" );
                                        }
                                    }

                                    break;
                                }
                            }

                            if( m_AdoOtptStrmUseTotal > 1 ) //查找其他要使用的音频输出流。
                            {
                                for( int p_TmpInt = 0; p_TmpInt < m_PcmAdoOtptMixFrmPt.length; p_TmpInt++ ) //将第一个音频输出流的Pcm格式音频输出原始帧复制到Pcm格式音频输出混音帧。
                                {
                                    m_PcmAdoOtptMixFrmPt[ p_TmpInt ] = m_PcmAdoOtptSrcFrmPt[ p_TmpInt ];
                                }

                                while( p_AdoOtptStrmItrtr.hasNext() )
                                {
                                    AdoOtptStrm p_AdoOtptStrmPt = ( AdoOtptStrm )p_AdoOtptStrmItrtr.next();

                                    if( p_AdoOtptStrmPt.m_IsUseAdoOtptStrm != 0 ) //如果该音频输出流为要使用。
                                    {
                                        switch( p_AdoOtptStrmPt.m_UseWhatDecd ) //使用什么解码器。
                                        {
                                            case 0: //如果要使用PCM原始数据。
                                            {
                                                //调用用户定义的写入音频输出帧函数。
                                                m_MediaPocsThrdPt.UserWriteAdoOtptFrm(
                                                        p_AdoOtptStrmPt.m_AdoOtptStrmIdx,
                                                        m_PcmAdoOtptSrcFrmPt, m_FrmLenUnit,
                                                        null, 0, null );

                                                //调用用户定义的获取Pcm格式音频输出帧函数。
                                                m_MediaPocsThrdPt.UserGetAdoOtptFrm(
                                                        p_AdoOtptStrmPt.m_AdoOtptStrmIdx,
                                                        m_PcmAdoOtptSrcFrmPt, m_FrmLenUnit,
                                                        null, 0 );
                                                break;
                                            }
                                            case 1: //如果要使用Speex解码器。
                                            {
                                                //调用用户定义的写入音频输出帧函数。
                                                m_MediaPocsThrdPt.UserWriteAdoOtptFrm(
                                                        p_AdoOtptStrmPt.m_AdoOtptStrmIdx,
                                                        null, 0,
                                                        m_EncdAdoOtptSrcFrmPt, m_EncdAdoOtptSrcFrmPt.length, m_EncdAdoOtptSrcFrmLenBytPt );

                                                //使用Speex解码器。
                                                if( p_AdoOtptStrmPt.m_SpeexDecdPt.Pocs( m_EncdAdoOtptSrcFrmPt, m_EncdAdoOtptSrcFrmLenBytPt.m_Val, m_PcmAdoOtptSrcFrmPt ) == 0 )
                                                {
                                                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引 " + p_AdoOtptStrmPt.m_AdoOtptStrmIdx + "：使用Speex解码器成功。" );
                                                }
                                                else
                                                {
                                                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引 " + p_AdoOtptStrmPt.m_AdoOtptStrmIdx + "：使用Speex解码器失败。" );
                                                }

                                                //调用用户定义的获取Pcm格式音频输出帧函数。
                                                m_MediaPocsThrdPt.UserGetAdoOtptFrm(
                                                        p_AdoOtptStrmPt.m_AdoOtptStrmIdx,
                                                        m_PcmAdoOtptSrcFrmPt, m_FrmLenUnit,
                                                        m_EncdAdoOtptSrcFrmPt, m_EncdAdoOtptSrcFrmLenBytPt.m_Val );
                                                break;
                                            }
                                            case 2: //如果要使用Opus解码器。
                                            {
                                                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引 " + p_AdoOtptStrmPt.m_AdoOtptStrmIdx + "：暂不支持使用Opus解码器。" );
                                            }
                                        }

                                        for( int p_TmpInt = 0; p_TmpInt < m_PcmAdoOtptMixFrmPt.length; p_TmpInt++ ) //混音。
                                        {
                                            m_PcmAdoOtptMixFrmPt[ p_TmpInt ] = m_PcmAdoOtptSrcFrmPt[ p_TmpInt ] + m_PcmAdoOtptMixFrmPt[ p_TmpInt ] - ( ( m_PcmAdoOtptSrcFrmPt[ p_TmpInt ] * m_PcmAdoOtptMixFrmPt[ p_TmpInt ] ) >> 0x10 );
                                        }
                                    }
                                }

                                for( int p_TmpInt = 0; p_TmpInt < m_PcmAdoOtptMixFrmPt.length; p_TmpInt++ ) //将Pcm格式音频输出混音帧复制到Pcm格式音频输出原始帧。
                                {
                                    if( m_PcmAdoOtptMixFrmPt[ p_TmpInt ] > 32767 ) m_PcmAdoOtptSrcFrmPt[ p_TmpInt ] = 32767;
                                    else if( m_PcmAdoOtptMixFrmPt[ p_TmpInt ] < -32768 ) m_PcmAdoOtptSrcFrmPt[ p_TmpInt ] = -32768;
                                    else m_PcmAdoOtptSrcFrmPt[ p_TmpInt ] = ( short ) m_PcmAdoOtptMixFrmPt[ p_TmpInt ];
                                }
                            }
                        }
                    }

                    //判断音频输出是否静音。在音频处理完后再设置静音，这样可以保证音频处理器的连续性。
                    if( m_AdoOtptIsMute != 0 )
                    {
                        Arrays.fill( m_PcmAdoOtptSrcFrmPt, ( short ) 0 );
                    }

                    //写入本次音频输出帧到音频输出设备。
                    m_AdoOtptDvcPt.write( m_PcmAdoOtptSrcFrmPt, 0, m_PcmAdoOtptSrcFrmPt.length );

                    //追加本次音频输出帧到Pcm格式音频输出原始帧链表。
                    synchronized( m_PcmAdoOtptSrcFrmLnkLstPt )
                    {
                        m_PcmAdoOtptSrcFrmLnkLstPt.addLast( m_PcmAdoOtptSrcFrmPt );
                    }
                    m_PcmAdoOtptSrcFrmPt = null;

                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
                    {
                        m_NowTickMsec = SystemClock.uptimeMillis();
                        Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：本次音频输出帧处理完毕，耗时 " + ( m_NowTickMsec - m_LastTickMsec ) + " 毫秒。" );
                    }
                }

                if( m_AdoOtptThrdExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：接收到退出请求，开始准备退出。" );
                    break;
                }
            } //音频输出循环结束。

            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：本线程已退出。" );
        }
    }
}