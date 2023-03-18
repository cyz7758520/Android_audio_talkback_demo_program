package HeavenTao.Media;

import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import HeavenTao.Vdo.*;
import HeavenTao.Data.*;

public class VdoOtpt //视频输出。
{
    MediaPocsThrd m_MediaPocsThrdPt; //存放媒体处理线程的指针。

    public int m_IsUseVdoOtpt; //存放是否使用视频输出，为0表示不使用，为非0表示要使用。
    public int m_IsInitVdoOtpt; //存放是否初始化视频输出，为0表示未初始化，为非0表示已初始化。

    public class VdoOtptFrm //视频输出帧。
    {
        int m_VdoOtptStrmIdx; //存放视频输出流索引。
        byte m_YU12VdoOtptSrcFrmPt[]; //存放YU12格式视频输出原始帧的指针，大小为960 * 1280 * 3 / 2字节。
        HTInt m_YU12VdoOtptSrcFrmWidthPt; //存放YU12格式视频输出原始帧的宽度，单位为像素。
        HTInt m_YU12VdoOtptSrcFrmHeightPt; //存放YU12格式视频输出原始帧的高度，单位为像素。
        byte m_EncdVdoOtptSrcFrmPt[]; //存放已编码格式视频输出原始帧的指针，大小为960 * 1280 * 3 / 2字节。
        HTLong m_EncdVdoOtptSrcFrmLenBytPt; //存放已编码格式视频输出原始帧的长度，单位为字节。
        long m_TimeStampMsec; //存放时间戳，单位为毫秒。
    }
    public LinkedList< VdoOtptFrm > m_VdoOtptFrmLnkLstPt; //存放视频输出帧链表的指针。
    public LinkedList< VdoOtptFrm > m_VdoOtptIdleFrmLnkLstPt; //存放视频输出空闲帧链表的指针。

    public class VdoOtptStrm
    {
        public int m_VdoOtptStrmIdx; //存放视频输出流索引。

        public int m_IsUseVdoOtptStrm; //存放是否使用视频输出流，为0表示不使用，为非0表示要使用。

        public int m_UseWhatDecd; //存放使用什么编码器，为0表示YU12原始数据，为1表示OpenH264解码器，为2表示系统自带H264解码器。

        OpenH264Decd m_OpenH264DecdPt; //存放OpenH264解码器的指针。
        int m_OpenH264DecdDecdThrdNum; //存放OpenH264解码器的解码线程数，单位为个，为0表示直接在调用线程解码，为1或2或3表示解码子线程的数量。

        SystemH264Decd m_SystemH264DecdPt; //存放系统自带H264解码器的指针。

        HTSurfaceView m_VdoOtptDspySurfaceViewPt; //存放视频输出显示SurfaceView的指针。
        SurfaceHolder.Callback m_VdoOtptDspySurfaceClbkPt; //存放视频输出显示Surface回调函数的指针。
        int m_VdoOtptIsBlack; //存放视频输出是否黑屏，为0表示有图像，为非0表示黑屏。

        int m_IsInitVdoOtptThrdTmpVar; //存放是否初始化视频输出线程的临时变量。
        VdoOtptFrm m_VdoOtptFrmPt; //存放视频输出帧的指针。
        int m_FrmLnkLstElmTotal; //存放帧链表的元素总数。
        long m_LastTickMsec; //存放上次的嘀嗒钟，单位为毫秒。
        long m_NowTickMsec; //存放本次的嘀嗒钟，单位为毫秒。

        VdoOtptThrd m_VdoOtptThrdPt; //存放视频输出线程的指针。
        int m_VdoOtptThrdExitFlag; //存放视频输出线程退出标记，0表示保持运行，1表示请求退出。
        
        //初始化解码器。
        public int DecdInit()
        {
            int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

            Out:
            {
                switch( m_UseWhatDecd )
                {
                    case 0: //如果要使用YU12原始数据。
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：初始化YU12原始数据成功。" );
                        break;
                    }
                    case 1: //如果要使用OpenH264解码器。
                    {
                        m_OpenH264DecdPt = new OpenH264Decd();
                        if( m_OpenH264DecdPt.Init( m_OpenH264DecdDecdThrdNum, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：初始化OpenH264解码器成功。" );
                        }
                        else
                        {
                            m_OpenH264DecdPt = null;
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：初始化OpenH264解码器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                            break Out;
                        }
                        break;
                    }
                    case 2: //如果要使用系统自带H264解码器。
                    {
                        m_SystemH264DecdPt = new SystemH264Decd();
                        if( m_SystemH264DecdPt.Init( null ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：初始化系统自带H264解码器成功。" );
                        }
                        else
                        {
                            m_SystemH264DecdPt = null;
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：初始化系统自带H264解码器失败。" );
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

        //销毁解码器。
        public void DecdDstoy()
        {
            switch( m_UseWhatDecd )
            {
                case 0: //如果要使用YU12原始数据。
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：销毁YU12原始数据成功。" );
                    break;
                }
                case 1: //如果要使用OpenH264解码器。
                {
                    if( m_OpenH264DecdPt != null )
                    {
                        if( m_OpenH264DecdPt.Dstoy( null ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：销毁OpenH264解码器成功。" );
                        }
                        else
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：销毁OpenH264解码器失败。" );
                        }
                        m_OpenH264DecdPt = null;
                    }
                    break;
                }
                case 2: //如果要使用系统自带H264解码器。
                {
                    if( m_SystemH264DecdPt != null )
                    {
                        if( m_SystemH264DecdPt.Dstoy( null ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：销毁系统自带H264解码器成功。" );
                        }
                        else
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：销毁系统自带H264解码器失败。" );
                        }
                        m_SystemH264DecdPt = null;
                    }
                    break;
                }
            }
        }

        //初始化视频输出线程。
        public int ThrdInit()
        {
            int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

            Out:
            {
                m_VdoOtptDspySurfaceViewPt.getHolder().setType( SurfaceHolder.SURFACE_TYPE_NORMAL );
                m_VdoOtptDspySurfaceClbkPt = new SurfaceHolder.Callback() //创建视频输出显示Surface的回调函数。
                {
                    @Override public void surfaceCreated( SurfaceHolder holder )
                    {
                        Log.i( MediaPocsThrd.m_CurClsNameStrPt, "VdoOtptDspySurface Created" );
                    }

                    @Override public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
                    {
                        Log.i( MediaPocsThrd.m_CurClsNameStrPt, "VdoOtptDspySurface Changed" );
                    }

                    @Override public void surfaceDestroyed( SurfaceHolder holder )
                    {
                        Log.i( MediaPocsThrd.m_CurClsNameStrPt, "VdoOtptDspySurface Destroyed" );
                    }
                };
                m_VdoOtptDspySurfaceViewPt.getHolder().addCallback( m_VdoOtptDspySurfaceClbkPt ); //设置视频输出显示Surface的回调函数。

                //初始化视频输出线程的临时变量。
                {
                    m_IsInitVdoOtptThrdTmpVar = 1; //设置已初始化视频输出线程的临时变量。
                    m_VdoOtptFrmPt = null; //初始化视频输出帧的指针。
                    m_FrmLnkLstElmTotal = 0; //初始化帧链表的元素总数。
                    m_LastTickMsec = 0; //初始化上次的嘀嗒钟。
                    m_NowTickMsec = 0; //初始化本次的嘀嗒钟。
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：初始化视频输出线程的临时变量成功。" );
                }

                //创建视频输出线程。
                m_VdoOtptThrdExitFlag = 0; //设置视频输出线程退出标记为0表示保持运行。
                m_VdoOtptThrdPt = new VdoOtptThrd();
                m_VdoOtptThrdPt.start();
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：创建视频输出线程成功。" );

                p_Rslt = 0; //设置本函数执行成功。
            }

            //if( p_Rslt != 0 ) //如果本函数执行失败。
            {
            }
            return p_Rslt;
        }

        //销毁视频输出线程。
        public void ThrdDstoy()
        {
            //销毁视频输出线程。
            if( m_VdoOtptThrdPt != null )
            {
                m_VdoOtptThrdExitFlag = 1; //请求视频输出线程退出。
                try
                {
                    m_VdoOtptThrdPt.join(); //等待视频输出线程退出。
                }
                catch( InterruptedException ignored )
                {
                }
                m_VdoOtptThrdPt = null;
                m_VdoOtptThrdExitFlag = 0;
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：销毁视频输出线程成功。" );
            }

            //销毁视频输出线程的临时变量。
            if( m_IsInitVdoOtptThrdTmpVar != 0 )
            {
                m_IsInitVdoOtptThrdTmpVar = 0; //设置未初始化视频输出线程的临时变量。
                m_VdoOtptFrmPt = null; //销毁视频输出帧的指针。
                m_FrmLnkLstElmTotal = 0; //销毁帧链表的元素总数。
                m_LastTickMsec = 0; //销毁上次的嘀嗒钟。
                m_NowTickMsec = 0; //销毁本次的嘀嗒钟。
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：销毁视频输出线程的临时变量成功。" );
            }

            m_VdoOtptDspySurfaceViewPt.getHolder().removeCallback( m_VdoOtptDspySurfaceClbkPt );
            m_VdoOtptDspySurfaceClbkPt = null;
            MediaPocsThrd.m_MainActivityPt.runOnUiThread( new Runnable() { public void run() { m_VdoOtptDspySurfaceViewPt.setVisibility( View.GONE ); m_VdoOtptDspySurfaceViewPt.setVisibility( View.VISIBLE ); } } ); //重建视频输出显示Surface视图。
        }

        //初始化视频输出流。
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

        //销毁视频输出流。
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
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：开始准备视频输出。" );

                //视频输出循环开始。
                while( true )
                {
                    OutPocs:
                    {
                        if( m_VdoOtptFrmPt == null ) //如果没获取一个视频输出空闲帧。
                        {
                            //获取一个视频输出空闲帧。
                            m_FrmLnkLstElmTotal = m_VdoOtptIdleFrmLnkLstPt.size(); //获取视频输出空闲帧链表的元素总数。
                            if( m_FrmLnkLstElmTotal > 0 ) //如果视频输出空闲帧链表中有视频输出空闲帧。
                            {
                                //从视频输出空闲帧链表中取出第一个帧。
                                synchronized( m_VdoOtptIdleFrmLnkLstPt )
                                {
                                    m_VdoOtptFrmPt = m_VdoOtptIdleFrmLnkLstPt.getFirst();
                                    m_VdoOtptIdleFrmLnkLstPt.removeFirst();
                                }
                                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：从视频输出空闲帧链表中取出第一个帧，视频输出空闲帧链表元素总数：" + m_FrmLnkLstElmTotal + "。" );
                            }
                            else //如果视频输出空闲帧链表中没有帧。
                            {
                                m_FrmLnkLstElmTotal = m_VdoOtptFrmLnkLstPt.size(); //获取视频输出帧链表的元素总数。
                                if( m_FrmLnkLstElmTotal <= 20 )
                                {
                                    m_VdoOtptFrmPt = new VdoOtpt.VdoOtptFrm(); //创建一个视频输出空闲帧。
                                    m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt = new byte[ 960 * 1280 * 3 / 2 ];
                                    m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt = new HTInt();
                                    m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt = new HTInt();
                                    if( m_UseWhatDecd != 0 )
                                    {
                                        m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmPt = new byte[ 960 * 1280 * 3 / 2 ];
                                    }
                                    else
                                    {
                                        m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmPt = null;
                                    }
                                    m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt = new HTLong( 0 );
                                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：视频输出空闲帧链表中没有帧，创建一个视频输出空闲帧成功。" );
                                }
                                else
                                {
                                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：视频输出帧链表中视频输出帧总数为" + m_FrmLnkLstElmTotal + "已经超过上限20，不再创建一个视频输出空闲帧。" );
                                    break OutPocs;
                                }
                            }
                        }

                        m_LastTickMsec = SystemClock.uptimeMillis();

                        //调用用户定义的写入视频输出帧函数，并解码成YU12原始数据。
                        switch( m_UseWhatDecd ) //使用什么解码器。
                        {
                            case 0: //如果使用YU12原始数据。
                            {
                                //调用用户定义的写入视频输出帧函数。
                                m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val = 0;
                                m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val = 0;
                                m_MediaPocsThrdPt.UserWriteVdoOtptFrm(
                                        m_VdoOtptStrmIdx,
                                        m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt,
                                        null, 0, null );

                                if( ( m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val > 0 ) && ( m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val > 0 ) ) //如果本次写入了视频输出帧。
                                {
                                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：使用YU12原始数据成功。YU12格式视频输出原始帧宽度：" + m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val + "，高度：" + m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val + "。" );
                                }
                                else //如果本次没写入视频输出帧。
                                {
                                    break OutPocs;
                                }

                                //用户定义的获取YU12格式视频输出帧函数。
                                m_MediaPocsThrdPt.UserGetYU12VdoOtptFrm(
                                        m_VdoOtptStrmIdx, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val,
                                        null, 0 );
                                break;
                            }
                            case 1: //如果使用OpenH264解码器。
                            {
                                //调用用户定义的写入视频输出帧函数。
                                m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt.m_Val = 0;
                                m_MediaPocsThrdPt.UserWriteVdoOtptFrm(
                                        m_VdoOtptStrmIdx,
                                        null, null, null,
                                        m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmPt.length, m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt );

                                if( m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt.m_Val > 0 ) //如果本次写入了视频输出帧。
                                {
                                    //使用OpenH264解码器。
                                    if( m_OpenH264DecdPt.Pocs(
                                            m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt.m_Val,
                                            m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt.length, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt,
                                            null ) == 0 )
                                    {
                                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：使用OpenH264解码器成功。YU12格式视频输出原始帧宽度：" + m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val + "，高度：" + m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val + "。" );
                                        if( ( m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val == 0 ) || ( m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val == 0 ) ) break OutPocs; //如果未解码出YU12格式帧，就把本次视频输出帧丢弃。
                                    }
                                    else
                                    {
                                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：使用OpenH264解码器失败，本次视频输出帧丢弃。" );
                                        break OutPocs;
                                    }
                                }
                                else //如果本次没写入视频输出帧。
                                {
                                    break OutPocs;
                                }

                                //用户定义的获取YU12格式视频输出帧函数。
                                m_MediaPocsThrdPt.UserGetYU12VdoOtptFrm(
                                        m_VdoOtptStrmIdx, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val,
                                        m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt.m_Val );
                                break;
                            }
                            case 2: //如果使用系统自带H264解码器。
                            {
                                //调用用户定义的写入视频输出帧函数。
                                m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt.m_Val = 0;
                                m_MediaPocsThrdPt.UserWriteVdoOtptFrm(
                                        m_VdoOtptStrmIdx,
                                        null, null, null,
                                        m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmPt.length, m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt );

                                if( m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt.m_Val != 0 ) //如果本次写入了视频输出帧。
                                {
                                    //使用系统自带H264解码器。
                                    if( m_SystemH264DecdPt.Pocs(
                                            m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt.m_Val,
                                            m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt.length, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt,
                                            40, null ) == 0 )
                                    {
                                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：使用系统自带H264解码器成功。YU12格式视频输出原始帧宽度：" + m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val + "，高度：" + m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val + "。" );
                                        if( ( m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val == 0 ) || ( m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val == 0 ) ) break OutPocs; //如果未解码出YU12格式帧，就把本次视频输出帧丢弃。
                                    }
                                    else
                                    {
                                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：使用系统自带H264解码器失败，本次视频输出帧丢弃。" );
                                        break OutPocs;
                                    }
                                }
                                else //如果本次没写入视频输出帧。
                                {
                                    break OutPocs;
                                }

                                //用户定义的获取YU12格式视频输出帧函数。
                                m_MediaPocsThrdPt.UserGetYU12VdoOtptFrm(
                                        m_VdoOtptStrmIdx, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val,
                                        m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt.m_Val );
                                break;
                            }
                        }

                        //判断视频输出是否黑屏。在视频处理完后再设置黑屏，这样可以保证视频处理器的连续性。
                        if( m_VdoOtptIsBlack != 0 )
                        {
                            int p_TmpLen = m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val * m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val;
                            Arrays.fill( m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt, 0, p_TmpLen, ( byte ) 0 );
                            Arrays.fill( m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt, p_TmpLen, p_TmpLen + p_TmpLen / 2, ( byte ) 128 );
                        }

                        //设置视频输出显示SurfaceView的宽高比。
                        m_VdoOtptDspySurfaceViewPt.setWidthToHeightRatio( ( float )m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val / m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val );

                        //显示视频输出帧。
                        if( LibYUV.PictrDrawToSurface(
                                m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmPt, 0, LibYUV.PICTR_FMT_BT601F8_YU12_I420, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmWidthPt.m_Val, m_VdoOtptFrmPt.m_YU12VdoOtptSrcFrmHeightPt.m_Val,
                                m_VdoOtptDspySurfaceViewPt.getHolder().getSurface(),
                                null ) != 0 )
                        {
                            Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：绘制视频输出帧到视频输出显示SurfaceView失败，本次视频输出帧丢弃。" );
                            break OutPocs;
                        }

                        m_VdoOtptFrmPt.m_VdoOtptStrmIdx = m_VdoOtptStrmIdx; //设置视频输出流索引。
                        m_VdoOtptFrmPt.m_TimeStampMsec = m_LastTickMsec; //设置时间戳。

                        //追加本次视频输出帧到视频输出帧链表。
                        synchronized( m_VdoOtptFrmLnkLstPt )
                        {
                            m_VdoOtptFrmLnkLstPt.addLast( m_VdoOtptFrmPt );
                        }
                        m_VdoOtptFrmPt = null;

                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
                        {
                            m_NowTickMsec = SystemClock.uptimeMillis();
                            Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：本次视频输出帧处理完毕，耗时 " + ( m_NowTickMsec - m_LastTickMsec ) + " 毫秒。" );
                        }
                    }

                    if( m_VdoOtptThrdExitFlag == 1 ) //如果退出标记为请求退出。
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：接收到退出请求，开始准备退出。" );
                        break;
                    }

                    SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
                } //视频输出循环结束。

                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引 " + m_VdoOtptStrmIdx + "：本线程已退出。" );
            }
        }
    }
    public LinkedList< VdoOtptStrm > m_VdoOtptStrmLnkLstPt; //存放视频输出流链表的指针。

    //添加视频输出流。
    public void AddVdoOtptStrm( int VdoOtptStrmIdx )
    {
        //查找视频输出流索引。
        for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
        {
            if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
            {
                return;
            }
        }

        //添加到视频输出流信息链表。
        VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt = new VdoOtpt.VdoOtptStrm();
        p_VdoOtptStrmPt.m_VdoOtptStrmIdx = VdoOtptStrmIdx;
        m_VdoOtptStrmLnkLstPt.addLast( p_VdoOtptStrmPt );
    }

    //删除视频输出流。
    public void DelVdoOtptStrm( int VdoOtptStrmIdx )
    {
        //查找视频输出流索引。
        for( Iterator< VdoOtpt.VdoOtptStrm > p_VdoOtptStrmItrtr = m_VdoOtptStrmLnkLstPt.iterator(); p_VdoOtptStrmItrtr.hasNext(); )
        {
            VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt = p_VdoOtptStrmItrtr.next();
            if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
            {
                p_VdoOtptStrmPt.Dstoy();
                p_VdoOtptStrmItrtr.remove();
                return;
            }
        }
    }

    //设置视频输出流。
    public void SetVdoOtptStrm( int VdoOtptStrmIdx, HTSurfaceView VdoOtptDspySurfaceViewPt )
    {
        if( VdoOtptDspySurfaceViewPt == null ) //如果视频显示SurfaceView的指针不正确。
        {
            return;
        }

        //查找视频输出流索引。
        for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
        {
            if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
            {
                if( ( m_IsInitVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Dstoy();

                p_VdoOtptStrmPt.m_VdoOtptDspySurfaceViewPt = VdoOtptDspySurfaceViewPt;

                if( ( m_IsInitVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Init();
                return;
            }
        }
    }

    //设置视频输出流要使用YU12原始数据。
    public void SetVdoOtptStrmUseYU12( int VdoOtptStrmIdx )
    {
        //查找视频输出流索引。
        for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
        {
            if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
            {
                if( ( m_IsInitVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Dstoy();

                p_VdoOtptStrmPt.m_UseWhatDecd = 0;

                if( ( m_IsInitVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Init();
                return;
            }
        }
    }

    //设置视频输出流要使用OpenH264解码器。
    public void SetVdoOtptStrmUseOpenH264Decd( int VdoOtptStrmIdx, int DecdThrdNum )
    {
        //查找视频输出流索引。
        for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
        {
            if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
            {
                if( ( m_IsInitVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Dstoy();

                p_VdoOtptStrmPt.m_UseWhatDecd = 1;
                p_VdoOtptStrmPt.m_OpenH264DecdDecdThrdNum = DecdThrdNum;

                if( ( m_IsInitVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Init();
                return;
            }
        }
    }

    //设置视频输出流要使用系统自带H264解码器。
    public void SetVdoOtptStrmUseSystemH264Decd( int VdoOtptStrmIdx )
    {
        //查找视频输出流索引。
        for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
        {
            if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
            {
                if( ( m_IsInitVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Dstoy();

                p_VdoOtptStrmPt.m_UseWhatDecd = 2;

                if( ( m_IsInitVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Init();
                return;
            }
        }
    }

    //设置视频输出流是否黑屏。
    public void SetVdoOtptStrmIsBlack( int VdoOtptStrmIdx, int IsBlack )
    {
        //查找视频输出流索引。
        for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
        {
            if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
            {
                p_VdoOtptStrmPt.m_VdoOtptIsBlack = IsBlack;
                return;
            }
        }
    }

    //设置视频输出流是否使用。
    public void SetVdoOtptStrmIsUse( int VdoOtptStrmIdx, int IsUseVdoOtptStrm )
    {
        //查找视频输出流索引。
        for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
        {
            if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx ) //如果索引找到了。
            {
                if( IsUseVdoOtptStrm != 0 ) //如果要使用视频输出流。
                {
                    if( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm == 0 ) //如果当前不使用视频输出流。
                    {
                        if( m_IsInitVdoOtpt != 0 ) //如果已初始化视频输出。
                        {
                            if( p_VdoOtptStrmPt.Init() == 0 ) //如果初始化视频输出流成功。
                            {
                                p_VdoOtptStrmPt.m_IsUseVdoOtptStrm = 1;
                            }
                        }
                        else //如果未初始化视频输出。
                        {
                            p_VdoOtptStrmPt.m_IsUseVdoOtptStrm = 1;
                        }
                    }
                }
                else //如果不使用视频输出流。
                {
                    if( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) //如果当前要使用视频输出流。
                    {
                        if( m_IsInitVdoOtpt != 0 ) //如果已初始化视频输出。
                        {
                            p_VdoOtptStrmPt.Dstoy();
                            p_VdoOtptStrmPt.m_IsUseVdoOtptStrm = 0;
                        }
                        else //如果未初始化视频输出。
                        {
                            p_VdoOtptStrmPt.m_IsUseVdoOtptStrm = 0;
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

            //初始化视频输出帧链表。
            m_VdoOtptFrmLnkLstPt = new LinkedList<>();
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化视频输出帧链表成功。" );

            //初始化视频输出空闲帧链表。
            m_VdoOtptIdleFrmLnkLstPt = new LinkedList<>();
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化视频输出空闲帧链表成功。" );

            for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
            {
                if( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 )
                {
                    if( p_VdoOtptStrmPt.Init() != 0 ) break Out;
                }
            }

            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
            {
                p_NowMsec = SystemClock.uptimeMillis(); //记录初始化结束的时间。
                Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化视频输出耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
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
        for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
        {
            p_VdoOtptStrmPt.Dstoy();
        }

        //销毁视频输出空闲帧链表。
        if( m_VdoOtptIdleFrmLnkLstPt != null )
        {
            m_VdoOtptIdleFrmLnkLstPt.clear();
            m_VdoOtptIdleFrmLnkLstPt = null;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁视频输出空闲帧链表成功。" );
        }

        //销毁视频输出帧链表。
        if( m_VdoOtptFrmLnkLstPt != null )
        {
            m_VdoOtptFrmLnkLstPt.clear();
            m_VdoOtptFrmLnkLstPt = null;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁视频输出帧链表成功。" );
        }

        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁视频输出成功。" );
    }
}