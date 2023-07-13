package HeavenTao.Media;

import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import HeavenTao.Vdo.*;
import HeavenTao.Data.*;

public class VdoOtpt //视频输出。
{
    MediaPocsThrd m_MediaPocsThrdPt; //存放媒体处理线程的指针。

    public int m_IsUse; //存放是否使用视频输出，为0表示不使用，为非0表示要使用。
    public int m_IsInit; //存放是否初始化视频输出，为0表示未初始化，为非0表示已初始化。

	static final int m_FrmMaxWidth = 960; //存放帧的最大宽度，单位为像素。
    static final int m_FrmMaxHeight = 1280; //存放帧的最大高度，单位为像素。

    public class Frm //存放帧。
    {
        int m_StrmIdx; //存放流索引。
        byte m_Yu12SrcFrmPt[]; //存放Yu12格式原始帧的指针，大小为m_FrmMaxWidth * m_FrmMaxHeight * 3 / 2字节。
        HTInt m_Yu12SrcFrmWidthPt; //存放Yu12格式原始帧的宽度，单位为像素。
        HTInt m_Yu12SrcFrmHeightPt; //存放Yu12格式原始帧的高度，单位为像素。
        byte m_EncdSrcFrmPt[]; //存放已编码格式原始帧的指针，大小为m_FrmMaxWidth * m_FrmMaxHeight * 3 / 2字节。
        HTLong m_EncdSrcFrmLenBytPt; //存放已编码格式原始帧的长度，单位为字节。
        long m_TimeStampMsec; //存放时间戳，单位为毫秒。
    }
    public ConcurrentLinkedQueue< Frm > m_FrmCntnrPt; //存放帧容器的指针。
    public ConcurrentLinkedQueue< Frm > m_IdleFrmCntnrPt; //存放空闲帧容器的指针。

    public class Strm //存放流。
    {
        public int m_Idx; //存放流索引。

        public int m_IsUse; //存放是否使用流，为0表示不使用，为非0表示要使用。

        public int m_UseWhatDecd; //存放使用什么编码器，为0表示Yu12原始数据，为1表示OpenH264解码器，为2表示系统自带H264解码器。

        class OpenH264Decd //存放OpenH264解码器。
        {
            HeavenTao.Vdo.OpenH264Decd m_Pt; //存放指针。
            int m_DecdThrdNum; //存放解码线程数，单位为个，为0表示直接在调用线程解码，为1或2或3表示解码子线程的数量。
        }
        OpenH264Decd m_OpenH264DecdPt = new OpenH264Decd();

        class SystemH264Decd //存放系统自带H264解码器。
        {
            HeavenTao.Vdo.SystemH264Decd m_Pt; //存放指针。
        }
        SystemH264Decd m_SystemH264DecdPt = new SystemH264Decd();

        class Dvc //存放设备。
        {
            HTSurfaceView m_DspySurfaceViewPt; //存放显示SurfaceView的指针。
            SurfaceHolder.Callback m_DspySurfaceClbkPt; //存放显示Surface回调函数的指针。
            int m_IsBlack; //存放是否黑屏，为0表示有图像，为非0表示黑屏。
        }
        Dvc m_DvcPt = new Dvc();

        class Thrd //存放线程。
        {
            int m_IsInitThrdTmpVar; //存放是否初始化线程的临时变量。
            VdoOtpt.Frm m_FrmPt; //存放帧的指针。
            int m_ElmTotal; //存放元素总数。
            long m_LastTickMsec; //存放上次的嘀嗒钟，单位为毫秒。
            long m_NowTickMsec; //存放本次的嘀嗒钟，单位为毫秒。

            VdoOtptThrd m_ThrdPt; //存放线程的指针。
            int m_ExitFlag; //存放退出标记，为0表示保持运行，为1表示请求退出。
        }
        Thrd m_ThrdPt = new Thrd();
        
        //初始化视频输出流的解码器。
        public int DecdInit()
        {
            int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

            Out:
            {
                switch( m_UseWhatDecd )
                {
                    case 0: //如果要使用Yu12原始数据。
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：初始化Yu12原始数据成功。" );
                        break;
                    }
                    case 1: //如果要使用OpenH264解码器。
                    {
                        m_OpenH264DecdPt.m_Pt = new HeavenTao.Vdo.OpenH264Decd();
                        if( m_OpenH264DecdPt.m_Pt.Init( m_OpenH264DecdPt.m_DecdThrdNum, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：初始化OpenH264解码器成功。" );
                        }
                        else
                        {
                            m_OpenH264DecdPt.m_Pt = null;
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：初始化OpenH264解码器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                            break Out;
                        }
                        break;
                    }
                    case 2: //如果要使用系统自带H264解码器。
                    {
                        m_SystemH264DecdPt.m_Pt = new HeavenTao.Vdo.SystemH264Decd();
                        if( m_SystemH264DecdPt.m_Pt.Init( null ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：初始化系统自带H264解码器成功。" );
                        }
                        else
                        {
                            m_SystemH264DecdPt.m_Pt = null;
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：初始化系统自带H264解码器失败。" );
                            break Out;
                        }
                        break;
                    }
                }

                p_Rslt = 0; //设置本函数执行成功。
            }

            //if( p_Rslt != 0 ) //如果本函数执行失败。
            {
            }
            return p_Rslt;
        }

        //销毁视频输出流的解码器。
        public void DecdDstoy()
        {
            switch( m_UseWhatDecd )
            {
                case 0: //如果要使用Yu12原始数据。
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：销毁Yu12原始数据成功。" );
                    break;
                }
                case 1: //如果要使用OpenH264解码器。
                {
                    if( m_OpenH264DecdPt.m_Pt != null )
                    {
                        if( m_OpenH264DecdPt.m_Pt.Dstoy( null ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：销毁OpenH264解码器成功。" );
                        }
                        else
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：销毁OpenH264解码器失败。" );
                        }
                        m_OpenH264DecdPt.m_Pt = null;
                    }
                    break;
                }
                case 2: //如果要使用系统自带H264解码器。
                {
                    if( m_SystemH264DecdPt.m_Pt != null )
                    {
                        if( m_SystemH264DecdPt.m_Pt.Dstoy( null ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：销毁系统自带H264解码器成功。" );
                        }
                        else
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：销毁系统自带H264解码器失败。" );
                        }
                        m_SystemH264DecdPt.m_Pt = null;
                    }
                    break;
                }
            }
        }

        //初始化视频输出流的线程。
        public int ThrdInit()
        {
            int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

            Out:
            {
                m_DvcPt.m_DspySurfaceViewPt.getHolder().setType( SurfaceHolder.SURFACE_TYPE_NORMAL );
                m_DvcPt.m_DspySurfaceClbkPt = new SurfaceHolder.Callback() //创建显示Surface的回调函数。
                {
                    @Override public void surfaceCreated( SurfaceHolder holder )
                    {
                        Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：VdoOtptDspySurface Created" );
                    }

                    @Override public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
                    {
                        Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：VdoOtptDspySurface Changed" );
                    }

                    @Override public void surfaceDestroyed( SurfaceHolder holder )
                    {
                        Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：VdoOtptDspySurface Destroyed" );
                    }
                };
                m_DvcPt.m_DspySurfaceViewPt.getHolder().addCallback( m_DvcPt.m_DspySurfaceClbkPt ); //设置显示Surface的回调函数。

                //初始化线程的临时变量。
                {
                    m_ThrdPt.m_IsInitThrdTmpVar = 1; //设置已初始化线程的临时变量。
                    m_ThrdPt.m_FrmPt = null; //初始化帧的指针。
                    m_ThrdPt.m_ElmTotal = 0; //初始化元素总数。
                    m_ThrdPt.m_LastTickMsec = 0; //初始化上次的嘀嗒钟。
                    m_ThrdPt.m_NowTickMsec = 0; //初始化本次的嘀嗒钟。
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：初始化线程的临时变量成功。" );
                }

                //初始化线程。
                m_ThrdPt.m_ExitFlag = 0; //设置线程退出标记为0表示保持运行。
                m_ThrdPt.m_ThrdPt = new VdoOtptThrd();
                m_ThrdPt.m_ThrdPt.start();
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：初始化线程成功。" );

                p_Rslt = 0; //设置本函数执行成功。
            }

            //if( p_Rslt != 0 ) //如果本函数执行失败。
            {
            }
            return p_Rslt;
        }

        //销毁视频输出流的线程。
        public void ThrdDstoy()
        {
            //销毁线程。
            if( m_ThrdPt.m_ThrdPt != null )
            {
                m_ThrdPt.m_ExitFlag = 1; //请求线程退出。
                try
                {
                    m_ThrdPt.m_ThrdPt.join(); //等待线程退出。
                }
                catch( InterruptedException ignored )
                {
                }
                m_ThrdPt.m_ThrdPt = null;
                m_ThrdPt.m_ExitFlag = 0;
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：销毁线程成功。" );
            }

            //销毁线程的临时变量。
            if( m_ThrdPt.m_IsInitThrdTmpVar != 0 )
            {
                m_ThrdPt.m_IsInitThrdTmpVar = 0; //设置未初始化线程的临时变量。
                m_ThrdPt.m_FrmPt = null; //销毁帧的指针。
                m_ThrdPt.m_ElmTotal = 0; //销毁元素总数。
                m_ThrdPt.m_LastTickMsec = 0; //销毁上次的嘀嗒钟。
                m_ThrdPt.m_NowTickMsec = 0; //销毁本次的嘀嗒钟。
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_Idx + "：销毁线程的临时变量成功。" );
            }

            m_DvcPt.m_DspySurfaceViewPt.getHolder().removeCallback( m_DvcPt.m_DspySurfaceClbkPt );
            m_DvcPt.m_DspySurfaceClbkPt = null;
            MediaPocsThrd.m_MainActivityPt.runOnUiThread( new Runnable() { public void run() { m_DvcPt.m_DspySurfaceViewPt.setVisibility( View.GONE ); m_DvcPt.m_DspySurfaceViewPt.setVisibility( View.VISIBLE ); } } ); //重建显示Surface视图。
        }

        //初始化视频输出的流。
        public int Init()
        {
            int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

            Out:
            {
                if( DecdInit() != 0 ) break Out;
                if( ThrdInit() != 0 ) break Out;

                p_Rslt = 0; //设置本函数执行成功。
            }

            //if( p_Rslt != 0 ) //如果本函数执行失败。
            {
            }
            return p_Rslt;
        }

        //销毁视频输出的流。
        public void Dstoy()
        {
            ThrdDstoy();
            DecdDstoy();
        }

        //视频输出线程。
        public class VdoOtptThrd extends Thread
        {
            public void run()
            {
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：开始准备视频输出。" );

                //视频输出循环开始。
                while( true )
                {
                    OutPocs:
                    {
                        if( m_ThrdPt.m_FrmPt == null ) //如果没获取一个空闲帧。
                        {
                            //获取一个空闲帧。
                            m_ThrdPt.m_ElmTotal = m_IdleFrmCntnrPt.size(); //获取空闲帧容器的元素总数。
                            if( m_ThrdPt.m_ElmTotal > 0 ) //如果空闲帧容器中有帧。
                            {
                                m_ThrdPt.m_FrmPt = m_IdleFrmCntnrPt.poll(); //从空闲帧容器中取出并删除第一个帧。
                                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：从空闲帧容器中取出并删除第一个帧，空闲帧容器元素总数：" + m_ThrdPt.m_ElmTotal + "。" );
                            }
                            else //如果空闲帧容器中没有帧。
                            {
                                m_ThrdPt.m_ElmTotal = m_FrmCntnrPt.size(); //获取帧容器的元素总数。
                                if( m_ThrdPt.m_ElmTotal <= 20 )
                                {
                                    m_ThrdPt.m_FrmPt = new VdoOtpt.Frm(); //创建一个空闲帧。
                                    m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt = new byte[ m_FrmMaxWidth * m_FrmMaxHeight * 3 / 2 ];
                                    m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt = new HTInt();
                                    m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt = new HTInt();
                                    if( m_UseWhatDecd != 0 )
                                    {
                                        m_ThrdPt.m_FrmPt.m_EncdSrcFrmPt = new byte[ m_FrmMaxWidth * m_FrmMaxHeight * 3 / 2 ];
                                    }
                                    else
                                    {
                                        m_ThrdPt.m_FrmPt.m_EncdSrcFrmPt = null;
                                    }
                                    m_ThrdPt.m_FrmPt.m_EncdSrcFrmLenBytPt = new HTLong( 0 );
                                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：空闲帧容器中没有帧，创建一个空闲帧成功。" );
                                }
                                else
                                {
                                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：帧容器中帧总数为" + m_ThrdPt.m_ElmTotal + "已经超过上限20，不再创建空闲帧。" );
                                    break OutPocs;
                                }
                            }
                        }

                        m_ThrdPt.m_LastTickMsec = SystemClock.uptimeMillis();

                        //调用用户定义的写入视频输出帧函数，并解码成Yu12原始数据。
                        switch( m_UseWhatDecd ) //使用什么解码器。
                        {
                            case 0: //如果要使用Yu12原始数据。
                            {
                                //调用用户定义的写入视频输出帧函数。
                                m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val = 0;
                                m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val = 0;
                                m_MediaPocsThrdPt.UserWriteVdoOtptFrm(
                                        m_Idx,
                                        m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt,
                                        null, 0, null );

                                if( ( m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val > 0 ) && ( m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val > 0 ) ) //如果本次写入了帧。
                                {
                                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：使用Yu12原始数据成功。Yu12格式原始帧宽度：" + m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val + "，高度：" + m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val + "。" );
                                }
                                else //如果本次没写入帧。
                                {
                                    break OutPocs;
                                }

                                //用户定义的获取视频输出帧函数。
                                m_MediaPocsThrdPt.UserGetVdoOtptFrm(
                                        m_Idx,
                                        m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val,
                                        null, 0 );
                                break;
                            }
                            case 1: //如果要使用OpenH264解码器。
                            {
                                //调用用户定义的写入视频输出帧函数。
                                m_ThrdPt.m_FrmPt.m_EncdSrcFrmLenBytPt.m_Val = 0;
                                m_MediaPocsThrdPt.UserWriteVdoOtptFrm(
                                        m_Idx,
                                        null, null, null,
                                        m_ThrdPt.m_FrmPt.m_EncdSrcFrmPt, m_ThrdPt.m_FrmPt.m_EncdSrcFrmPt.length, m_ThrdPt.m_FrmPt.m_EncdSrcFrmLenBytPt );

                                if( m_ThrdPt.m_FrmPt.m_EncdSrcFrmLenBytPt.m_Val > 0 ) //如果本次写入了帧。
                                {
                                    //使用OpenH264解码器。
                                    if( m_OpenH264DecdPt.m_Pt.Pocs(
                                            m_ThrdPt.m_FrmPt.m_EncdSrcFrmPt, m_ThrdPt.m_FrmPt.m_EncdSrcFrmLenBytPt.m_Val,
                                            m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt.length, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt,
                                            null ) == 0 )
                                    {
                                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：使用OpenH264解码器成功。Yu12格式原始帧宽度：" + m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val + "，高度：" + m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val + "。" );
                                        if( ( m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val == 0 ) || ( m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val == 0 ) ) break OutPocs; //如果未解码出Yu12格式帧，就把本次帧丢弃。
                                    }
                                    else
                                    {
                                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：使用OpenH264解码器失败，本次帧丢弃。" );
                                        break OutPocs;
                                    }
                                }
                                else //如果本次没写入帧。
                                {
                                    break OutPocs;
                                }

                                //用户定义的获取视频输出帧函数。
                                m_MediaPocsThrdPt.UserGetVdoOtptFrm(
                                        m_Idx,
                                        m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val,
                                        m_ThrdPt.m_FrmPt.m_EncdSrcFrmPt, m_ThrdPt.m_FrmPt.m_EncdSrcFrmLenBytPt.m_Val );
                                break;
                            }
                            case 2: //如果要使用系统自带H264解码器。
                            {
                                //调用用户定义的写入视频输出帧函数。
                                m_ThrdPt.m_FrmPt.m_EncdSrcFrmLenBytPt.m_Val = 0;
                                m_MediaPocsThrdPt.UserWriteVdoOtptFrm(
                                        m_Idx,
                                        null, null, null,
                                        m_ThrdPt.m_FrmPt.m_EncdSrcFrmPt, m_ThrdPt.m_FrmPt.m_EncdSrcFrmPt.length, m_ThrdPt.m_FrmPt.m_EncdSrcFrmLenBytPt );

                                if( m_ThrdPt.m_FrmPt.m_EncdSrcFrmLenBytPt.m_Val != 0 ) //如果本次写入了帧。
                                {
                                    //使用系统自带H264解码器。
                                    if( m_SystemH264DecdPt.m_Pt.Pocs(
                                            m_ThrdPt.m_FrmPt.m_EncdSrcFrmPt, m_ThrdPt.m_FrmPt.m_EncdSrcFrmLenBytPt.m_Val,
                                            m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt.length, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt,
                                            40, null ) == 0 )
                                    {
                                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：使用系统自带H264解码器成功。Yu12格式原始帧宽度：" + m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val + "，高度：" + m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val + "。" );
                                        if( ( m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val == 0 ) || ( m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val == 0 ) ) break OutPocs; //如果未解码出Yu12格式帧，就把本次帧丢弃。
                                    }
                                    else
                                    {
                                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：使用系统自带H264解码器失败，本次帧丢弃。" );
                                        break OutPocs;
                                    }
                                }
                                else //如果本次没写入帧。
                                {
                                    break OutPocs;
                                }

                                //用户定义的获取视频输出帧函数。
                                m_MediaPocsThrdPt.UserGetVdoOtptFrm(
                                        m_Idx,
                                        m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val,
                                        m_ThrdPt.m_FrmPt.m_EncdSrcFrmPt, m_ThrdPt.m_FrmPt.m_EncdSrcFrmLenBytPt.m_Val );
                                break;
                            }
                        }

                        //判断设备是否黑屏。在视频处理完后再设置黑屏，这样可以保证视频处理器的连续性。
                        if( m_DvcPt.m_IsBlack != 0 )
                        {
                            int p_TmpLenByt = m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val * m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val;
                            Arrays.fill( m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt, 0, p_TmpLenByt, ( byte ) 0 );
                            Arrays.fill( m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt, p_TmpLenByt, p_TmpLenByt + p_TmpLenByt / 2, ( byte ) 128 );
                        }

                        //设置显示SurfaceView的宽高比。
                        m_DvcPt.m_DspySurfaceViewPt.setWidthToHeightRatio( ( float )m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val / m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val );

                        //显示帧。
                        if( LibYUV.PictrDrawToSurface(
                                m_ThrdPt.m_FrmPt.m_Yu12SrcFrmPt, 0, LibYUV.PICTR_FMT_BT601F8_Yu12_I420, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmWidthPt.m_Val, m_ThrdPt.m_FrmPt.m_Yu12SrcFrmHeightPt.m_Val,
                                m_DvcPt.m_DspySurfaceViewPt.getHolder().getSurface(),
                                null ) != 0 )
                        {
                            Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：绘制Yu12格式原始帧到显示SurfaceView失败，本次帧丢弃。" );
                            break OutPocs;
                        }

                        m_ThrdPt.m_FrmPt.m_StrmIdx = m_Idx; //设置流索引。
                        m_ThrdPt.m_FrmPt.m_TimeStampMsec = m_ThrdPt.m_LastTickMsec; //设置时间戳。

                        //放入本次帧到帧容器。注意：从取出到放入过程中可以跳出，跳出后会再次使用本次帧。
                        m_FrmCntnrPt.offer( m_ThrdPt.m_FrmPt );
                        m_ThrdPt.m_FrmPt = null;

                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
                        {
                            m_ThrdPt.m_NowTickMsec = SystemClock.uptimeMillis();
                            Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：本次帧处理完毕，耗时 " + ( m_ThrdPt.m_NowTickMsec - m_ThrdPt.m_LastTickMsec ) + " 毫秒。" );
                        }
                    }

                    if( m_ThrdPt.m_ExitFlag == 1 ) //如果退出标记为请求退出。
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：接收到退出请求，开始准备退出。" );
                        break;
                    }

                    SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
                } //视频输出循环结束。

                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_Idx + "：本线程已退出。" );
            }
        }
    }
    public ArrayList< Strm > m_StrmCntnrPt; //存放流容器的指针。

    //添加视频输出的流。
    public void AddStrm( int StrmIdx )
    {
        //查找流索引。
        for( Strm p_StrmPt : m_StrmCntnrPt )
        {
            if( p_StrmPt.m_Idx == StrmIdx )
            {
                return;
            }
        }

        //添加到流容器。
        VdoOtpt.Strm p_StrmPt = new VdoOtpt.Strm();
        p_StrmPt.m_Idx = StrmIdx;
        m_StrmCntnrPt.add( p_StrmPt );
    }

    //删除视频输出的流。
    public void DelStrm( int StrmIdx )
    {
        //查找流索引。
        for( Iterator< VdoOtpt.Strm > p_StrmItrtr = m_StrmCntnrPt.iterator(); p_StrmItrtr.hasNext(); )
        {
            VdoOtpt.Strm p_StrmPt = p_StrmItrtr.next();
            if( p_StrmPt.m_Idx == StrmIdx )
            {
                p_StrmPt.Dstoy();
                p_StrmItrtr.remove();
                return;
            }
        }
    }

    //设置视频输出的流。
    public void SetStrm( int StrmIdx, HTSurfaceView DspySurfaceViewPt )
    {
        if( DspySurfaceViewPt == null ) //如果显示SurfaceView的指针不正确。
        {
            return;
        }

        //查找流索引。
        for( VdoOtpt.Strm p_StrmPt : m_StrmCntnrPt )
        {
            if( p_StrmPt.m_Idx == StrmIdx )
            {
                if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Dstoy();

                p_StrmPt.m_DvcPt.m_DspySurfaceViewPt = DspySurfaceViewPt;

                if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Init();
                return;
            }
        }
    }

    //设置视频输出的流使用Yu12原始数据。
    public void SetStrmUseYu12( int StrmIdx )
    {
        //查找流索引。
        for( VdoOtpt.Strm p_StrmPt : m_StrmCntnrPt )
        {
            if( p_StrmPt.m_Idx == StrmIdx )
            {
                if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Dstoy();

                p_StrmPt.m_UseWhatDecd = 0;

                if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Init();
                return;
            }
        }
    }

    //设置视频输出的流使用OpenH264解码器。
    public void SetStrmUseOpenH264Decd( int StrmIdx, int DecdThrdNum )
    {
        //查找流索引。
        for( VdoOtpt.Strm p_StrmPt : m_StrmCntnrPt )
        {
            if( p_StrmPt.m_Idx == StrmIdx )
            {
                if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Dstoy();

                p_StrmPt.m_UseWhatDecd = 1;
                p_StrmPt.m_OpenH264DecdPt.m_DecdThrdNum = DecdThrdNum;

                if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Init();
                return;
            }
        }
    }

    //设置视频输出的流使用系统自带H264解码器。
    public void SetStrmUseSystemH264Decd( int StrmIdx )
    {
        //查找流索引。
        for( VdoOtpt.Strm p_StrmPt : m_StrmCntnrPt )
        {
            if( p_StrmPt.m_Idx == StrmIdx )
            {
                if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Dstoy();

                p_StrmPt.m_UseWhatDecd = 2;

                if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Init();
                return;
            }
        }
    }

    //设置视频输出的流是否黑屏。
    public void SetStrmIsBlack( int StrmIdx, int IsBlack )
    {
        //查找流索引。
        for( VdoOtpt.Strm p_StrmPt : m_StrmCntnrPt )
        {
            if( p_StrmPt.m_Idx == StrmIdx )
            {
                p_StrmPt.m_DvcPt.m_IsBlack = IsBlack;
                return;
            }
        }
    }

    //设置视频输出的流是否使用。
    public void SetStrmIsUse( int StrmIdx, int IsUseStrm )
    {
        //查找流索引。
        for( VdoOtpt.Strm p_StrmPt : m_StrmCntnrPt )
        {
            if( p_StrmPt.m_Idx == StrmIdx ) //如果索引找到了。
            {
                if( IsUseStrm != 0 ) //如果要使用流。
                {
                    if( p_StrmPt.m_IsUse == 0 ) //如果当前不使用流。
                    {
                        if( m_IsInit != 0 ) //如果已初始化视频输出。
                        {
                            if( p_StrmPt.Init() == 0 ) //如果初始化流成功。
                            {
                                p_StrmPt.m_IsUse = 1;
                            }
                        }
                        else //如果未初始化视频输出。
                        {
                            p_StrmPt.m_IsUse = 1;
                        }
                    }
                }
                else //如果不使用流。
                {
                    if( p_StrmPt.m_IsUse != 0 ) //如果当前要使用流。
                    {
                        if( m_IsInit != 0 ) //如果已初始化视频输出。
                        {
                            p_StrmPt.Dstoy();
                            p_StrmPt.m_IsUse = 0;
                        }
                        else //如果未初始化视频输出。
                        {
                            p_StrmPt.m_IsUse = 0;
                        }
                    }
                }
            }
        }
    }
    
    //初始化视频输出。
    public int Init()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
        long p_LastMsec = 0;
        long p_NowMsec = 0;

        Out:
        {
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastMsec = SystemClock.uptimeMillis(); //记录初始化开始的时间。

            //初始化帧容器。
            m_FrmCntnrPt = new ConcurrentLinkedQueue<>();
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出：初始化帧容器成功。" );

            //初始化空闲帧容器。
            m_IdleFrmCntnrPt = new ConcurrentLinkedQueue<>();
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出：初始化空闲帧容器成功。" );

            for( VdoOtpt.Strm p_StrmPt : m_StrmCntnrPt )
            {
                if( p_StrmPt.m_IsUse != 0 )
                {
                    if( p_StrmPt.Init() != 0 ) break Out;
                }
            }

            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
            {
                p_NowMsec = SystemClock.uptimeMillis(); //记录初始化结束的时间。
                Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出：初始化耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
            Dstoy();
        }
        return p_Rslt;
    }

    //销毁视频输出。
    public void Dstoy()
    {
        long p_LastMsec = 0;
        long p_NowMsec = 0;

        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastMsec = SystemClock.uptimeMillis(); //记录销毁开始的时间。

        for( VdoOtpt.Strm p_StrmPt : m_StrmCntnrPt )
        {
            p_StrmPt.Dstoy();
        }

        //销毁空闲帧容器。
        if( m_IdleFrmCntnrPt != null )
        {
            m_IdleFrmCntnrPt.clear();
            m_IdleFrmCntnrPt = null;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出：销毁空闲帧容器成功。" );
        }

        //销毁帧容器。
        if( m_FrmCntnrPt != null )
        {
            m_FrmCntnrPt.clear();
            m_FrmCntnrPt = null;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出：销毁帧容器成功。" );
        }

        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
        {
            p_NowMsec = SystemClock.uptimeMillis(); //记录销毁结束的时间。
            Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出：销毁耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
        }
    }
}