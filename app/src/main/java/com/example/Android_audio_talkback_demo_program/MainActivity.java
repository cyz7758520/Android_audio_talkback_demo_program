package com.example.Android_audio_talkback_demo_program;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;

import HeavenTao.Audio.*;
import HeavenTao.Data.*;

//主界面消息处理类。
class MainActivityHandler extends Handler
{
    String m_CurClsNameStrPt = this.getClass().getSimpleName(); //当前类名称字符串类对象的内存指针。

    MainActivity m_MainActivityPt;

    public void handleMessage( Message MessagePt )
    {
        if( MessagePt.what == 1 ) //如果是音频处理线程正常退出的消息。
        {
            m_MainActivityPt.m_MyAudioProcThreadPt = null;

            ( ( EditText ) m_MainActivityPt.findViewById( R.id.IPAddrEdit ) ).setEnabled( true ); //设置IP地址控件为可用。
            ( ( EditText ) m_MainActivityPt.findViewById( R.id.PortEdit ) ).setEnabled( true ); //设置端口控件为可用。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtn ) ).setText( "创建服务端" ); //设置创建服务端按钮的内容为“创建服务端”。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.ConnectSrvrBtn ) ).setEnabled( true ); //设置连接服务端按钮为可用。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.ConnectSrvrBtn ) ).setText( "连接服务端" ); //设置连接服务端按钮的内容为“连接服务端”。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtn ) ).setEnabled( true ); //设置创建服务端按钮为可用。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.SettingBtn ) ).setEnabled( true ); //设置设置按钮为可用。
        }
        if( MessagePt.what == 2 ) //如果是显示日志的消息。
        {
            LinearLayout clLogLinearLayout = m_MainActivityPt.m_LyotActivityMainViewPt.findViewById( R.id.LogLinearLyot );
            TextView clTempTextView = new TextView( m_MainActivityPt );
            clTempTextView.setText( ( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) ).format( new Date() ) + "：" + MessagePt.obj );
            clLogLinearLayout.addView( clTempTextView );
        }
    }
}

//音频处理线程类。
class MyAudioProcThread extends AudioProcThread
{
    String m_IPAddrStrPt; //存放IP地址字符串类对象的内存指针。
    int m_Port; //存放端口号。
    Handler m_MainActivityHandlerPt; //存放主界面消息处理类对象的内存指针。

    byte m_IsCreateSrvrOrClnt; //存放创建服务端或者客户端标记，为1表示创建服务端，为0表示创建客户端。
    ServerSocket m_SrvrSocketPt; //存放TCP协议服务端套接字类对象的内存指针。
    Socket m_ClntSocketPt; //存放TCP协议客户端套接字类对象的内存指针。
    long m_LastPktSendTime; //存放最后一个数据包的发送时间，用于判断连接是否中断。
    long m_LastPktRecvTime; //存放最后一个数据包的接收时间，用于判断连接是否中断。

    int m_LastSendInputFrameIsAct; //存放最后一个发送的输入帧有无语音活动，为1表示有语音活动，为0表示无语音活动。
    short m_PktPrereadSz; //存放本次数据包的预读长度。
    int m_SendInputFrameTimeStamp; //存放发送输入帧的时间戳。
    int m_RecvOutputFrameTimeStamp; //存放接收输出帧的时间戳。
    byte m_IsRecvExitPkt; //存放是否接收到退出包，为0表示否，为1表示是。

    int m_UseWhatRecvOutputFrame; //存放使用什么接收输出帧，为0表示链表，为1表示自适应抖动缓冲器。
    LinkedList< byte[] > m_RecvOutputFrameLnkLstPt; //存放接收输出帧链表类对象的内存指针。
    Ajb m_AjbPt; //存放自适应抖动缓冲器类对象的内存指针。
    int m_AjbMinNeedBufFrameCnt; //存放自适应抖动缓冲器的最小需缓冲帧数量，单位个。
    int m_AjbMaxNeedBufFrameCnt; //存放自适应抖动缓冲器的最大需缓冲帧数量，单位个。
    byte m_AjbAdaptSensitivity; //存放自适应抖动缓冲器的自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0,127]。

    byte m_TmpBytePt[]; //存放临时数据。
    int m_TmpInt32;

    //用户定义的初始化函数，在本线程刚启动时调用一次，返回值表示是否成功，为0表示成功，为非0表示失败。
    public int UserInit()
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            if( m_IsCreateSrvrOrClnt == 1 ) //如果是创建本地TCP协议服务端套接字接受远端TCP协议客户端套接字的连接。
            {
                if( m_SrvrSocketPt == null ) //如果还没有创建TCP协议服务端套接字。
                {
                    try
                    {
                        m_SrvrSocketPt = new ServerSocket(); //创建本地TCP协议服务端套接字类对象。
                        m_SrvrSocketPt.setReuseAddress( true ); //设置重用地址。
                        m_SrvrSocketPt.bind( new InetSocketAddress( m_IPAddrStrPt, m_Port ), 1 ); //将本地TCP协议服务端套接字绑定本地节点的IP地址。
                        m_SrvrSocketPt.setSoTimeout( 500 ); //设置accept()函数的超时时间。

                        String p_InfoStrPt = "创建本地TCP协议服务端套接字[" + m_SrvrSocketPt.getInetAddress().getHostAddress() + ":" + m_SrvrSocketPt.getLocalPort() + "]成功。";
                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();
                        p_MessagePt.what = 2;
                        p_MessagePt.obj = p_InfoStrPt;
                        m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    }
                    catch( IOException e )
                    {
                        String p_InfoStrPt = "创建本地TCP协议服务端套接字[" + m_IPAddrStrPt + ":" + m_Port + "]失败。原因：" + e.toString();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();
                        p_MessagePt.what = 2;
                        p_MessagePt.obj = p_InfoStrPt;
                        m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }
                }

                while( true )
                {
                    //接受远端TCP协议客户端套接字的连接。
                    try
                    {
                        m_ClntSocketPt = m_SrvrSocketPt.accept();
                    }
                    catch( IOException e )
                    {
                    }

                    if( m_ClntSocketPt != null ) //如果成功接受了远端TCP协议客户端套接字的连接，就开始传输数据。
                    {
                        try
                        {
                            m_SrvrSocketPt.close(); //关闭本地TCP协议服务端套接字，防止还有其他远端TCP协议客户端套接字继续连接。
                        }
                        catch( IOException e )
                        {
                        }
                        m_SrvrSocketPt = null;

                        String p_InfoStrPt = "接受了远端TCP协议客户端套接字[" + m_ClntSocketPt.getInetAddress().getHostAddress() + ":" + m_ClntSocketPt.getPort() + "]与本地TCP协议客户端套接字[" + m_ClntSocketPt.getLocalAddress().getHostAddress() + ":" + m_ClntSocketPt.getLocalPort() + "]的连接。";
                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();
                        p_MessagePt.what = 2;
                        p_MessagePt.obj = p_InfoStrPt;
                        m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break;
                    }

                    if( m_ExitFlag != 0 ) //如果本线程接收到退出请求。
                    {
                        Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
                        break out;
                    }
                }
            }
            else if( m_IsCreateSrvrOrClnt == 0 ) //如果是创建本地TCP协议客户端套接字连接远端TCP协议服务端套接字。
            {
                try
                {
                    m_ClntSocketPt = new Socket(); //创建本地TCP协议客户端套接字类对象。
                    m_ClntSocketPt.connect( new InetSocketAddress( m_IPAddrStrPt, m_Port ), 5000 ); //连接指定的IP地址和端口号，超时时间为5秒。

                    String p_InfoStrPt = "创建本地TCP协议客户端套接字[" + m_ClntSocketPt.getLocalAddress().getHostAddress() + ":" + m_ClntSocketPt.getLocalPort() + "]与远端TCP协议服务端套接字[" + m_ClntSocketPt.getInetAddress().getHostAddress() + ":" + m_ClntSocketPt.getPort() + "]的连接成功。";
                    Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();
                    p_MessagePt.what = 2;
                    p_MessagePt.obj = p_InfoStrPt;
                    m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                }
                catch( IOException e )
                {
                    String p_InfoStrPt = " 创建本地TCP协议客户端套接字与远端TCP协议服务端套接字[" + m_IPAddrStrPt + ":" + m_Port + "]的连接失败。原因：" + e.getMessage();
                    Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();
                    p_MessagePt.what = 2;
                    p_MessagePt.obj = p_InfoStrPt;
                    m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break out;
                }
            }

            try
            {
                m_ClntSocketPt.setTcpNoDelay( true ); //设置TCP协议客户端套接字的TCP_NODELAY选项为true。
            }
            catch( SocketException e )
            {
                break out;
            }

            switch( m_UseWhatRecvOutputFrame ) //使用什么接收输出帧。
            {
                case 0: //如果使用链表。
                {
                    m_RecvOutputFrameLnkLstPt = new LinkedList< byte[] >(); //创建接收输出帧链表类对象。

                    Log.i( m_CurClsNameStrPt, "创建接收输出帧链表类对象成功。" );

                    break;
                }
                case 1: //如果使用自适应抖动缓冲器。
                {
                    //初始化自适应抖动缓冲器类对象。
                    m_AjbPt = new Ajb();
                    m_TmpInt32 = m_AjbPt.Init( m_SamplingRate, m_FrameLen, ( byte ) 1, ( byte ) 0, m_AjbMinNeedBufFrameCnt, m_AjbMaxNeedBufFrameCnt, m_AjbAdaptSensitivity );
                    if( m_TmpInt32 == 0 )
                    {
                        Log.i( m_CurClsNameStrPt, "初始化自适应抖动缓冲器类对象成功。返回值：" + m_TmpInt32 );
                    }
                    else
                    {
                        Log.e( m_CurClsNameStrPt, "初始化自适应抖动缓冲器类对象失败。返回值：" + m_TmpInt32 );
                        break out;
                    }

                    break;
                }
            }

            m_LastPktRecvTime = m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间和接收时间为当前时间。

            m_LastSendInputFrameIsAct = 0; //设置最后发送的一个音频帧为无语音活动。
            m_PktPrereadSz = 0; //设置本次数据包的预读长度为0。
            m_SendInputFrameTimeStamp = 0; //设置发送音频数据的时间戳为0。
            m_RecvOutputFrameTimeStamp = 0; //设置接收音频数据的时间戳为0。
            m_IsRecvExitPkt = 0; //设置没有接收到退出包。
            if( m_TmpBytePt == null )
                m_TmpBytePt = new byte[m_FrameLen * 2 + 6]; //初始化存放临时数据的数组。

            String p_InfoStrPt = "开始进行音频对讲。";
            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();
            p_MessagePt.what = 2;
            p_MessagePt.obj = p_InfoStrPt;
            m_MainActivityHandlerPt.sendMessage( p_MessagePt );

            p_Result = 0; //设置本函数执行成功。
        }

        return p_Result;
    }

    //用户定义的处理函数，在本线程运行时每隔1毫秒就调用一次，返回值表示是否成功，为0表示成功，为非0表示失败。
    public int UserProcess()
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            //接收远端发送过来的一个音频输出帧，并放入自适应抖动缓冲器。
            try
            {
                InputStream clInputStream = m_ClntSocketPt.getInputStream();

                if( ( m_PktPrereadSz == 0 ) && ( clInputStream.available() >= 2 ) ) //如果还没有接收预读长度，且客户端套接字可以接收到预读长度。
                {
                    //接收预读长度。
                    if( clInputStream.read( m_TmpBytePt, 0, 2 ) != 2 ) //如果接收到的预读长度的数据长度不为2。
                    {
                        Log.e( m_CurClsNameStrPt, "接收到的预读长度的数据长度不为2。" );
                        break out;
                    }

                    //读取预读长度。
                    m_PktPrereadSz = ( short ) ( ( m_TmpBytePt[0] & 0xFF ) + ( ( m_TmpBytePt[1] & 0xFF ) << 8 ) );
                    if( m_PktPrereadSz == 0 ) //如果预读长度为0，表示这是一个心跳包，就更新一下时间即可。
                    {
                        m_LastPktRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。
                        Log.i( m_CurClsNameStrPt, "接收到一个心跳包。" );
                    }
                    else if( m_PktPrereadSz == -1 ) //如果预读长度为0xFFFF，表示这是一个退出包。
                    {
                        m_LastPktRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。
                        m_IsRecvExitPkt = 1; //设置已经接收到退出包。

                        String p_InfoStrPt = "接收到一个退出包。";
                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();
                        p_MessagePt.what = 2;
                        p_MessagePt.obj = p_InfoStrPt;
                        m_MainActivityHandlerPt.sendMessage( p_MessagePt );

                        break out;
                    }
                    else if( m_PktPrereadSz < 4 ) //如果预读长度小于4，表示没有时间戳。
                    {
                        Log.e( m_CurClsNameStrPt, "接收到预读长度为" + m_PktPrereadSz + "小于4，表示没有时间戳，无法继续接收。" );
                        break out;
                    }
                    else if( m_PktPrereadSz > m_TmpBytePt.length ) //如果预读长度大于接收缓冲区的长度。
                    {
                        Log.e( m_CurClsNameStrPt, "接收到预读长度大于接收缓冲区的长度，无法继续接收。" );
                        break out;
                    }
                }

                if( ( m_PktPrereadSz != 0 ) && ( clInputStream.available() >= m_PktPrereadSz ) ) //如果已经接收了预读长度，且该输出帧可以一次性接收完毕。
                {
                    //接收时间戳。
                    if( clInputStream.read( m_TmpBytePt, 0, 4 ) != 4 ) //如果接收时间戳失败。
                    {
                        Log.e( m_CurClsNameStrPt, "接收到的时间戳的数据长度不为4。" );
                        break out;
                    }

                    //读取时间戳。
                    m_RecvOutputFrameTimeStamp = ( m_TmpBytePt[0] & 0xFF ) + ( ( m_TmpBytePt[1] & 0xFF ) << 8 ) + ( ( m_TmpBytePt[2] & 0xFF ) << 16 ) + ( ( m_TmpBytePt[3] & 0xFF ) << 24 );

                    //接收输出帧。
                    if( m_PktPrereadSz > 4 ) //如果该输出帧为有语音活动。
                    {
                        if( clInputStream.read( m_TmpBytePt, 0, m_PktPrereadSz - 4 ) != m_PktPrereadSz - 4 ) //如果接收到的数据长度与预读长度-时间戳长度不同。
                        {
                            Log.e( m_CurClsNameStrPt, "接收到的输出帧的数据长度与预读长度-时间戳长度不同。" );
                            break out;
                        }

                        if( ( m_UseWhatCodec == 0 ) && ( m_PktPrereadSz - 4 != m_FrameLen * 2 ) ) //如果使用了PCM原始数据，但接收到的PCM格式输出帧的数据长度与帧的数据长度不同。
                        {
                            Log.e( m_CurClsNameStrPt, "接收到的PCM格式输出帧的数据长度与帧的数据长度不同。" );
                            break out;
                        }
                    }

                    m_LastPktRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。

                    //将输出帧放入链表或自适应抖动缓冲器。
                    switch( m_UseWhatRecvOutputFrame ) //使用什么接收输出帧。
                    {
                        case 0: //如果使用链表。
                        {
                            byte p_TmpOutputFramePt[] = new byte[m_PktPrereadSz - 4]; //创建只有输出帧的数据长度大小的临时数组。

                            System.arraycopy( m_TmpBytePt, 0, p_TmpOutputFramePt, 0, p_TmpOutputFramePt.length );

                            synchronized( m_RecvOutputFrameLnkLstPt )
                            {
                                m_RecvOutputFrameLnkLstPt.addLast( p_TmpOutputFramePt );
                            }

                            Log.i( m_CurClsNameStrPt, "接收一个输出帧并放入链表成功。时间戳：" + m_RecvOutputFrameTimeStamp + "，总长度：" + m_PktPrereadSz + "。" );

                            break;
                        }
                        case 1: //如果使用自适应抖动缓冲器。
                        {
                            if( m_PktPrereadSz == 4 ) //如果该输出帧为无语音活动。
                            {
                                synchronized( m_AjbPt )
                                {
                                    m_AjbPt.PutOneByteFrame( m_RecvOutputFrameTimeStamp, null, 0 );
                                }
                            }
                            else //如果该输出帧为有语音活动。
                            {
                                synchronized( m_AjbPt )
                                {
                                    m_AjbPt.PutOneByteFrame( m_RecvOutputFrameTimeStamp, m_TmpBytePt, m_PktPrereadSz - 4 );
                                }
                            }

                            Log.i( m_CurClsNameStrPt, "接收一个输出帧并放入自适应抖动缓冲器成功。时间戳：" + m_RecvOutputFrameTimeStamp + "，总长度：" + m_PktPrereadSz + "。" );

                            break;
                        }
                    }

                    m_PktPrereadSz = 0; //清空预读长度，以便下一次接收新的数据包。
                }
            }
            catch( IOException e )
            {
                String p_InfoStrPt = "接收一个输出帧失败。原因：" + e.getMessage();
                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();
                p_MessagePt.what = 2;
                p_MessagePt.obj = p_InfoStrPt;
                m_MainActivityHandlerPt.sendMessage( p_MessagePt );

                break out;
            }

            //发送心跳包。
            if( System.currentTimeMillis() - m_LastPktSendTime >= 500 ) //如果超过500毫秒没有发送任何数据包，就发送一个心跳包。
            {
                //设置预读长度。
                m_TmpBytePt[0] = 0;
                m_TmpBytePt[1] = 0;

                try
                {
                    OutputStream clOutputStream = m_ClntSocketPt.getOutputStream();
                    clOutputStream.write( m_TmpBytePt, 0, 2 );
                    clOutputStream.flush(); //防止出现Software caused connection abort异常。
                    m_LastPktSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间。

                    Log.i( m_CurClsNameStrPt, "发送一个心跳包成功。" );
                }
                catch( IOException e )
                {
                    String p_InfoStrPt = "发送一个心跳包失败。原因：" + e.getMessage();
                    Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();
                    p_MessagePt.what = 2;
                    p_MessagePt.obj = p_InfoStrPt;
                    m_MainActivityHandlerPt.sendMessage( p_MessagePt );

                    break out;
                }
            }

            //判断TCP协议套接字连接是否中断。
            if( System.currentTimeMillis() - m_LastPktRecvTime > 2000 ) //如果超过2000毫秒没有接收任何数据包，就判定连接已经断开了。
            {
                String p_InfoStrPt = "超过2000毫秒没有接收任何数据包，判定连接已经断开了。";
                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();
                p_MessagePt.what = 2;
                p_MessagePt.obj = p_InfoStrPt;
                m_MainActivityHandlerPt.sendMessage( p_MessagePt );

                break out;
            }

            p_Result = 0; //设置本函数执行成功。
        }

        return p_Result;
    }

    //用户定义的销毁函数，在本线程退出时调用一次，返回值表示是否重新初始化，为0表示直接退出，为非0表示重新初始化。
    public int UserDestroy()
    {
        if( ( m_ExitFlag == 1 ) && ( m_ClntSocketPt != null ) && ( m_ClntSocketPt.isConnected() ) && ( m_IsRecvExitPkt == 0 ) ) //如果本线程接收到退出请求，且TCP协议客户端套接字类对象不为空，且TCP协议客户端套接字类对象已连接，且没有接收到退出包。
        {
            //设置预读长度。
            if( m_TmpBytePt == null )
            {
                m_TmpBytePt = new byte[2];
            }
            m_TmpBytePt[0] = ( byte ) 0xFF;
            m_TmpBytePt[1] = ( byte ) 0xFF;

            try
            {
                OutputStream clOutputStream = m_ClntSocketPt.getOutputStream();
                clOutputStream.write( m_TmpBytePt, 0, 2 );
                clOutputStream.flush(); //防止出现Software caused connection abort异常。
                m_LastPktSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间。

                String p_InfoStrPt = "发送一个退出包成功。";
                Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();
                p_MessagePt.what = 2;
                p_MessagePt.obj = p_InfoStrPt;
                m_MainActivityHandlerPt.sendMessage( p_MessagePt );
            }
            catch( IOException e )
            {
                String p_InfoStrPt = "发送一个退出包失败。原因：" + e.getMessage();
                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();
                p_MessagePt.what = 2;
                p_MessagePt.obj = p_InfoStrPt;
                m_MainActivityHandlerPt.sendMessage( p_MessagePt );
            }
        }

        //销毁TCP协议服务端套接字。
        if( m_SrvrSocketPt != null )
        {
            try
            {
                String p_InfoStrPt;
                if( m_SrvrSocketPt.getInetAddress() != null )
                {
                    p_InfoStrPt = "已关闭TCP协议服务端套接字[" + m_SrvrSocketPt.getInetAddress().getHostAddress() + ":" + m_SrvrSocketPt.getLocalPort() + "]。";
                }
                else
                {
                    p_InfoStrPt = "已关闭TCP协议服务端套接字。";
                }
                Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();
                p_MessagePt.what = 2;
                p_MessagePt.obj = p_InfoStrPt;
                m_MainActivityHandlerPt.sendMessage( p_MessagePt );

                m_SrvrSocketPt.close(); //关闭TCP协议服务端套接字。
            }
            catch( IOException e )
            {
            }
            m_SrvrSocketPt = null;
        }

        //销毁TCP协议客户端套接字。
        if( m_ClntSocketPt != null )
        {
            try
            {
                String p_InfoStrPt;
                if( ( m_ClntSocketPt.getLocalAddress() != null ) && ( m_ClntSocketPt.getInetAddress() != null ) )
                {
                    p_InfoStrPt = "已关闭本地TCP协议客户端套接字[" + m_ClntSocketPt.getLocalAddress().getHostAddress() + ":" + m_ClntSocketPt.getLocalPort() + "]与远端TCP协议客户端套接字[" + m_ClntSocketPt.getInetAddress().getHostAddress() + ":" + m_ClntSocketPt.getPort() + "]的连接。";
                }
                else
                {
                    p_InfoStrPt = "已关闭TCP协议客户端套接字。";
                }
                Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();
                p_MessagePt.what = 2;
                p_MessagePt.obj = p_InfoStrPt;
                m_MainActivityHandlerPt.sendMessage( p_MessagePt );

                m_ClntSocketPt.close(); //关闭TCP协议客户端套接字。
            }
            catch( IOException e )
            {
            }
            m_ClntSocketPt = null;
        }

        //销毁接收音频输出帧的链表类对象。
        if( m_RecvOutputFrameLnkLstPt != null )
        {
            m_RecvOutputFrameLnkLstPt.clear();
            m_RecvOutputFrameLnkLstPt = null;
        }

        //销毁自适应抖动缓冲器类对象。
        if( m_AjbPt != null )
        {
            m_AjbPt.Destroy();
            m_AjbPt = null;
        }

        if( ( m_IsCreateSrvrOrClnt == 1 ) && ( m_ExitCode == -2 ) && ( m_ExitFlag == 0 ) ) //如果当前是创建服务端，且退出代码为处理失败，且本线程未接收到退出请求。
        {
            Log.i( m_CurClsNameStrPt, "由于当前是创建服务端，且退出代码为处理失败，且本线程未接收到退出请求，本线程重新初始化来继续保持监听。" );
            return 1;
        }
        else if( ( m_IsCreateSrvrOrClnt == 0 ) && ( m_ExitFlag == 0 ) && ( m_IsRecvExitPkt == 0 ) ) //如果当前是创建客户端，且本线程未接收到退出请求，且没有接收到退出包。
        {
            Log.i( m_CurClsNameStrPt, "由于当前是创建客户端，且本线程未接收到退出请求，且没有接收到退出包，本线程在500毫秒后重新初始化来重连。" );
            SystemClock.sleep( 500 ); //暂停500毫秒。
            return 1;
        }
        else //其他情况，本线程直接退出。
        {
            //发送本线程退出消息给主界面线程。
            Message clMessage = new Message();
            clMessage.what = 1;
            m_MainActivityHandlerPt.sendMessage( clMessage );

            return 0;
        }
    }

    //用户定义的读取输入帧函数，在读取到一个输入帧并处理完后回调一次，为0表示成功，为非0表示失败。
    public int UserReadInputFrame( short PcmInputFramePt[], short PcmResultFramePt[], int VoiceActSts, byte SpeexInputFramePt[], long SpeexInputFrameLen, int SpeexInputFrameIsNeedTrans )
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            //发送输入帧。
            {
                if( VoiceActSts == 1 ) //如果该输入帧为有语音活动。
                {
                    switch( m_UseWhatCodec ) //使用什么编解码器。
                    {
                        case 0: //如果使用PCM原始数据。
                        {
                            for( m_TmpInt32 = 0; m_TmpInt32 < PcmResultFramePt.length; m_TmpInt32++ )
                            {
                                m_TmpBytePt[6 + m_TmpInt32 * 2] = ( byte ) ( PcmResultFramePt[m_TmpInt32] & 0xFF );
                                m_TmpBytePt[6 + m_TmpInt32 * 2 + 1] = ( byte ) ( ( PcmResultFramePt[m_TmpInt32] & 0xFF00 ) >> 8 );
                            }

                            m_TmpInt32 = PcmResultFramePt.length * 2 + 4; //预读长度 = PCM格式音频输入数据帧长度 + 时间戳长度。

                            break;
                        }
                        case 1: //如果使用Speex编解码器。
                        {
                            if( SpeexInputFrameIsNeedTrans == 1 ) //如果本Speex格式音频输入数据帧需要传输。
                            {
                                System.arraycopy( SpeexInputFramePt, 0, m_TmpBytePt, 6, ( int ) SpeexInputFrameLen );

                                m_TmpInt32 = ( int ) SpeexInputFrameLen + 4; //预读长度 = Speex格式音频输入数据帧长度 + 时间戳长度。
                            }
                            else //如果本Speex格式音频输入数据帧不需要传输。
                            {
                                m_TmpInt32 = 4; //预读长度 = 时间戳长度。
                            }

                            break;
                        }
                    }
                }
                else //如果本音频输入数据帧为无语音活动。
                {
                    m_TmpInt32 = 4; //预读长度 = 时间戳长度。
                }

                if( ( m_TmpInt32 != 4 ) || //如果本音频输入数据帧为有语音活动，就发送。
                        ( ( m_TmpInt32 == 4 ) && ( m_LastSendInputFrameIsAct != 0 ) ) ) //如果本音频输入数据帧为无语音活动，但最后发送的一个音频数据帧为有语音活动，就发送。
                {
                    //设置预读长度。
                    m_TmpBytePt[0] = ( byte ) ( m_TmpInt32 & 0xFF );
                    m_TmpBytePt[1] = ( byte ) ( ( m_TmpInt32 & 0xFF00 ) >> 8 );

                    //设置时间戳。
                    m_TmpBytePt[2] = ( byte ) ( m_SendInputFrameTimeStamp & 0xFF );
                    m_TmpBytePt[3] = ( byte ) ( ( m_SendInputFrameTimeStamp & 0xFF00 ) >> 8 );
                    m_TmpBytePt[4] = ( byte ) ( ( m_SendInputFrameTimeStamp & 0xFF0000 ) >> 16 );
                    m_TmpBytePt[5] = ( byte ) ( ( m_SendInputFrameTimeStamp & 0xFF000000 ) >> 24 );

                    m_SendInputFrameTimeStamp += m_FrameLen; //时间戳递增一个帧的数据长度。

                    try
                    {
                        OutputStream clOutputStream = m_ClntSocketPt.getOutputStream();
                        clOutputStream.write( m_TmpBytePt, 0, m_TmpInt32 + 2 );
                        clOutputStream.flush(); //防止出现Software caused connection abort异常。
                        m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。

                        Log.i( m_CurClsNameStrPt, "发送一个输入帧成功。时间戳：" + m_SendInputFrameTimeStamp + "，总长度：" + m_TmpInt32 + "。" );
                    }
                    catch( IOException e )
                    {
                        String p_InfoStrPt = "发送一个输入帧失败。原因：" + e.getMessage();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();
                        p_MessagePt.what = 2;
                        p_MessagePt.obj = p_InfoStrPt;
                        m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }
                }
                else
                {
                    Log.i( m_CurClsNameStrPt, "本输入帧为无语音活动，且最后发送的一个输入帧为无语音活动，无需发送。" );
                }

                //设置最后发送的一个音频数据帧有无语音活动。
                if( m_TmpInt32 != 4 ) m_LastSendInputFrameIsAct = 1;
                else m_LastSendInputFrameIsAct = 0;
            }

            p_Result = 0; //设置本函数执行成功。
        }

        return p_Result;
    }

    //用户定义的写入输出帧函数，在需要写入一个输出帧时回调一次。注意：本函数不是在音频处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致回音消除失败。
    public void UserWriteOutputFrame( short PcmOutputFramePt[], byte SpeexOutputFramePt[], HTLong SpeexOutputFrameLenPt )
    {
        int m_TmpInt32;

        //从链表或自适应抖动缓冲器取出一个输出帧。
        switch( m_UseWhatRecvOutputFrame ) //使用什么接收输出帧。
        {
            case 0: //如果使用链表。
            {
                byte p_TmpOutputFramePt[] = null;

                if( m_RecvOutputFrameLnkLstPt.size() != 0 ) //如果接收输出帧链表不为空。
                {
                    synchronized( m_RecvOutputFrameLnkLstPt )
                    {
                        p_TmpOutputFramePt = m_RecvOutputFrameLnkLstPt.getFirst(); //获取接收输出帧链表的第一个输出帧。
                        m_RecvOutputFrameLnkLstPt.removeFirst(); //删除接收输出帧链表的第一个输出帧。
                    }
                }

                switch( m_UseWhatCodec ) //使用什么编解码器。
                {
                    case 0: //如果使用PCM原始数据。
                    {
                        if( ( p_TmpOutputFramePt != null ) && ( p_TmpOutputFramePt.length > 0 ) ) //如果接收输出帧链表的第一个输出帧为有语音活动。
                        {
                            for( m_TmpInt32 = 0; m_TmpInt32 < m_FrameLen; m_TmpInt32++ )
                            {
                                PcmOutputFramePt[m_TmpInt32] = ( short ) ( ( p_TmpOutputFramePt[m_TmpInt32 * 2] & 0xFF ) | ( p_TmpOutputFramePt[m_TmpInt32 * 2 + 1] << 8 ) );
                            }

                            Log.i( m_CurClsNameStrPt, "从接收输出帧链表取出一个有语音活动的PCM格式输出帧，帧的数据长度：" + p_TmpOutputFramePt.length + "。" );
                        }
                        else //如果接收音频输出数据帧的链表为空，或第一个音频输出数据帧为无语音活动。
                        {
                            for( m_TmpInt32 = 0; m_TmpInt32 < m_FrameLen; m_TmpInt32++ )
                            {
                                PcmOutputFramePt[m_TmpInt32] = 0;
                            }

                            Log.i( m_CurClsNameStrPt, "从接收输出帧链表取出一个无语音活动的PCM格式输出帧。" );
                        }

                        break;
                    }
                    case 1: //如果使用Speex编解码器。
                    {
                        if( ( p_TmpOutputFramePt != null ) && ( p_TmpOutputFramePt.length > 0 ) ) //如果接收输出帧链表的第一个输出帧为有语音活动。
                        {
                            System.arraycopy( p_TmpOutputFramePt, 0, SpeexOutputFramePt, 0, p_TmpOutputFramePt.length );

                            SpeexOutputFrameLenPt.m_Val = p_TmpOutputFramePt.length;

                            Log.i( m_CurClsNameStrPt, "从接收输出帧链表取出一个有语音活动的Speex格式输出帧，帧的数据长度：" + p_TmpOutputFramePt.length + "。" );
                        }
                        else //如果接收音频输出数据帧的链表为空，或第一个音频输出数据帧为无语音活动。
                        {
                            SpeexOutputFrameLenPt.m_Val = 0;

                            Log.i( m_CurClsNameStrPt, "从接收输出帧链表取出一个无语音活动的Speex格式输出帧。" );
                        }

                        break;
                    }
                }

                break;
            }
            case 1: //如果使用自适应抖动缓冲器。
            {
                HTLong p_OutputFrameLenPt = new HTLong(); //从自适应抖动缓冲器中取出的输出帧的数据长度。
                HTInt p_AjbFrameCnt = new HTInt(); //自适应抖动缓冲器中帧的数量。

                switch( m_UseWhatCodec ) //使用什么编解码器。
                {
                    case 0: //如果使用PCM原始数据。
                    {
                        //从自适应抖动缓冲器取出一个输出帧。
                        synchronized( m_AjbPt )
                        {
                            m_AjbPt.GetOneShortFrame( PcmOutputFramePt, PcmOutputFramePt.length, p_OutputFrameLenPt );
                        }

                        if( p_OutputFrameLenPt.m_Val != 0 ) //如果输出帧为有语音活动。
                        {
                            Log.i( m_CurClsNameStrPt, "从自适应抖动缓冲器取出一个有语音活动的PCM格式输出帧，帧的数据长度：" + p_OutputFrameLenPt.m_Val + "。" );
                        }
                        else //如果输出帧为无语音活动。
                        {
                            for( m_TmpInt32 = 0; m_TmpInt32 < m_FrameLen; m_TmpInt32++ )
                            {
                                PcmOutputFramePt[m_TmpInt32] = 0;
                            }

                            Log.i( m_CurClsNameStrPt, "从自适应抖动缓冲器取出一个无语音活动的PCM格式输出帧。" );
                        }

                        break;
                    }
                    case 1: //如果使用Speex编解码器。
                    {
                        //从自适应抖动缓冲器取出一个音频输出数据帧。
                        synchronized( m_AjbPt )
                        {
                            m_AjbPt.GetOneByteFrame( SpeexOutputFramePt, SpeexOutputFramePt.length, p_OutputFrameLenPt );
                        }

                        SpeexOutputFrameLenPt.m_Val = p_OutputFrameLenPt.m_Val;

                        if( p_OutputFrameLenPt.m_Val != 0 ) //如果输出帧为有语音活动。
                        {
                            Log.i( m_CurClsNameStrPt, "从自适应抖动缓冲器取出一个有语音活动的Speex格式输出帧，帧的数据长度：" + p_OutputFrameLenPt.m_Val + "。" );
                        }
                        else //如果输出帧为无语音活动。
                        {
                            Log.i( m_CurClsNameStrPt, "从自适应抖动缓冲器取出一个无语音活动的Speex格式输出帧。" );
                        }

                        break;
                    }
                }

                m_AjbPt.GetCurHaveBufActFrameCnt( p_AjbFrameCnt );
                Log.i( m_CurClsNameStrPt, "自适应抖动缓冲器中当前已缓冲有活动帧的数量为 " + p_AjbFrameCnt.m_Val + " 个。" );
                m_AjbPt.GetCurHaveBufInactFrameCnt( p_AjbFrameCnt );
                Log.i( m_CurClsNameStrPt, "自适应抖动缓冲器中当前已缓冲无活动帧的数量为 " + p_AjbFrameCnt.m_Val + " 个。" );
                m_AjbPt.GetCurHaveBufFrameCnt( p_AjbFrameCnt );
                Log.i( m_CurClsNameStrPt, "自适应抖动缓冲器中当前已缓冲帧的数量为 " + p_AjbFrameCnt.m_Val + " 个。" );

                m_AjbPt.GetMaxNeedBufFrameCnt( p_AjbFrameCnt );
                Log.i( m_CurClsNameStrPt, "自适应抖动缓冲器中最大需缓冲音频数据帧的数量为 " + p_AjbFrameCnt.m_Val + " 个。" );
                m_AjbPt.GetMinNeedBufFrameCnt( p_AjbFrameCnt );
                Log.i( m_CurClsNameStrPt, "自适应抖动缓冲器中最小需缓冲音频数据帧的数量为 " + p_AjbFrameCnt.m_Val + " 个。" );
                m_AjbPt.GetCurNeedBufFrameCnt( p_AjbFrameCnt );
                Log.i( m_CurClsNameStrPt, "自适应抖动缓冲器中当前需缓冲音频数据帧的数量为 " + p_AjbFrameCnt.m_Val + " 个。" );

                break;
            }
        }
    }

    //用户定义的获取PCM格式输出帧函数，在解码完一个输出帧时回调一次。注意：本函数不是在音频处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致回音消除失败。
    public void UserGetPcmOutputFrame( short PcmOutputFramePt[] )
    {

    }
}

public class MainActivity extends AppCompatActivity
{
    String m_CurClsNameStrPt = this.getClass().getSimpleName(); //存放当前类名称字符串。

    View m_LyotActivityMainViewPt; //存放主界面布局控件的内存指针。
    View m_LyotActivitySettingViewPt; //存放设置界面布局控件的内存指针。
    View m_LyotActivitySpeexAecViewPt; //存放Speex声学回音消除器设置布局控件的内存指针。
    View m_LyotActivityWebRtcAecmViewPt; //存放WebRtc定点版声学回音消除器设置布局控件的内存指针。
    View m_LyotActivityWebRtcAecViewPt; //存放WebRtc浮点版声学回音消除器设置布局控件的内存指针。
    View m_LyotActivitySpeexWebRtcAecViewPt; //存放SpeexWebRtc三重声学回音消除器设置布局控件的内存指针。
    View m_LyotActivitySpeexPprocNsViewPt; //存放Speex预处理器的噪音抑制设置布局控件的内存指针。
    View m_LyotActivityWebRtcNsxViewPt; //存放WebRtc定点噪音抑制器设置布局控件的内存指针。
    View m_LyotActivityWebRtcNsViewPt; //存放WebRtc浮点噪音抑制器设置布局控件的内存指针。
    View m_LyotActivitySpeexPprocOtherViewPt; //存放Speex预处理器的其他功能设置布局控件的内存指针。
    View m_LyotActivitySpeexCodecViewPt; //存放Speex编解码器设置布局控件的内存指针。
    View m_LyotActivityAjbViewPt; //存放自适应抖动缓冲器设置布局控件的内存指针。
    View m_LyotActivityReadMeViewPt; //存放说明界面布局控件的内存指针。
    View m_LyotActivityCurViewPt; //存放当前界面布局控件的内存指针。

    MainActivity m_MainActivityPt; //存放主界面类对象的内存指针。
    MyAudioProcThread m_MyAudioProcThreadPt; //存放音频处理线程类对象的内存指针。
    MainActivityHandler m_MainActivityHandlerPt; //存放主界面消息处理类对象的内存指针。

    String m_ExternalDirFullPathStrPt; //存放扩展目录绝对路径字符串的内存指针。

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        LayoutInflater layoutInflater = LayoutInflater.from( this );
        m_LyotActivityMainViewPt = layoutInflater.inflate( R.layout.activity_main, null );
        m_LyotActivitySettingViewPt = layoutInflater.inflate( R.layout.activity_setting, null );
        m_LyotActivitySpeexAecViewPt = layoutInflater.inflate( R.layout.activity_speexaec, null );
        m_LyotActivityWebRtcAecmViewPt = layoutInflater.inflate( R.layout.activity_webrtcaecm, null );
        m_LyotActivityWebRtcAecViewPt = layoutInflater.inflate( R.layout.activity_webrtcaec, null );
        m_LyotActivitySpeexWebRtcAecViewPt = layoutInflater.inflate( R.layout.activity_speexwebrtcaec, null );
        m_LyotActivitySpeexPprocNsViewPt = layoutInflater.inflate( R.layout.activity_speexpprocns, null );
        m_LyotActivityWebRtcNsxViewPt = layoutInflater.inflate( R.layout.activity_webrtcnsx, null );
        m_LyotActivityWebRtcNsViewPt = layoutInflater.inflate( R.layout.activity_webrtcns, null );
        m_LyotActivitySpeexPprocOtherViewPt = layoutInflater.inflate( R.layout.activity_speexpprocother, null );
        m_LyotActivitySpeexCodecViewPt = layoutInflater.inflate( R.layout.activity_speexcodec, null );
        m_LyotActivityAjbViewPt = layoutInflater.inflate( R.layout.activity_ajb, null );
        m_LyotActivityReadMeViewPt = layoutInflater.inflate( R.layout.activity_readme, null );

        setContentView( m_LyotActivityMainViewPt ); //设置界面的内容为主界面。
        m_LyotActivityCurViewPt = m_LyotActivityMainViewPt;

        m_MainActivityPt = this;

        //检测并请求录音权限。
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions( this, new String[] {Manifest.permission.RECORD_AUDIO}, 1 );

        //检测并请求网络权限。
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.INTERNET ) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions( this, new String[] {Manifest.permission.INTERNET}, 1 );

        //检测并请求唤醒锁权限。
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.WAKE_LOCK ) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions( this, new String[] {Manifest.permission.WAKE_LOCK}, 1 );

        //初始化消息处理类对象。
        m_MainActivityHandlerPt = new MainActivityHandler();
        m_MainActivityHandlerPt.m_MainActivityPt = m_MainActivityPt;

        //获取本机IP地址。
        String p_pclString = null;
        try
        {
            //遍历所有的网络接口设备。
            out:
            for( Enumeration< NetworkInterface > clEnumerationNetworkInterface = NetworkInterface.getNetworkInterfaces(); clEnumerationNetworkInterface.hasMoreElements(); )
            {
                NetworkInterface clNetworkInterface = clEnumerationNetworkInterface.nextElement();
                if( clNetworkInterface.getName().compareTo( "usbnet0" ) != 0 ) //如果该网络接口设备不是USB接口对应的网络接口设备。
                {
                    //遍历该网络接口设备所有的IP地址。
                    for( Enumeration< InetAddress > enumIpAddr = clNetworkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); )
                    {
                        InetAddress clInetAddress = enumIpAddr.nextElement();
                        if( ( !clInetAddress.isLoopbackAddress() ) && ( clInetAddress.getAddress().length == 4 ) ) //如果该IP地址不是回环地址，且是IPv4的。
                        {
                            p_pclString = clInetAddress.getHostAddress();
                            break out;
                        }
                    }
                }
            }
        }
        catch( SocketException e )
        {
        }
        if( p_pclString == null )
        {
            p_pclString = "127.0.0.1";
        }

        //设置IP地址控件的内容。
        ( ( EditText ) m_LyotActivityMainViewPt.findViewById( R.id.IPAddrEdit ) ).setText( p_pclString );

        //设置端口控件的内容。
        ( ( EditText ) m_LyotActivityMainViewPt.findViewById( R.id.PortEdit ) ).setText( "12345" );

        //获取扩展目录绝对路径字符串。
        if( getExternalFilesDir( null ) != null )
        {
            m_ExternalDirFullPathStrPt = getExternalFilesDir( null ).getPath();
        }
        else
        {
            m_ExternalDirFullPathStrPt = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getApplicationContext().getPackageName();
        }

        //测试代码。
        /*m_MyAudioProcThreadPt.m_AppContextPt = getApplicationContext();

        String p_AudioInputFileFullPathStrPt = m_ExternalDirFullPathStrPt + "/AudioInput.wav";
        String p_AudioOutputFileFullPathStrPt = m_ExternalDirFullPathStrPt + "/AudioOutput.wav";
        String p_AudioResultFileFullPathStrPt = m_ExternalDirFullPathStrPt + "/AudioResult.wav";
        int p_SamplingRate = 16000;
        int p_FrameLen = 320;

        int p_Result;
        HTShort NumChanlPt = new HTShort();
        HTInt SamplingRatePt = new HTInt();
        HTInt SamplingBitPt = new HTInt();
        WaveFileReader p_AudioInputWaveFileReaderPt = new WaveFileReader();
        WaveFileReader p_AudioOutputWaveFileReaderPt = new WaveFileReader();
        WaveFileWriter p_AudioResultWaveFileWriterPt = new WaveFileWriter();
        SpeexAec p_SpeexAecPt = new SpeexAec();
        SpeexPproc p_SpeexPprocPt = new SpeexPproc();
        SpeexPproc p_SpeexPprocOtherPt = new SpeexPproc();
        WebRtcAecm p_WebRtcAecmPt = new WebRtcAecm();
        WebRtcAec p_WebRtcAecPt = new WebRtcAec();
        SpeexWebRtcAec p_SpeexWebRtcAecPt = new SpeexWebRtcAec();
        WebRtcNsx p_WebRtcNsx = new WebRtcNsx();
        WebRtcNs p_WebRtcNs = new WebRtcNs();
        RNNoise p_RNNoise = new RNNoise();
        SpeexEncoder p_SpeexEncoderPt = new SpeexEncoder();
        SpeexDecoder p_SpeexDecoderPt = new SpeexDecoder();
        short p_PcmInputFramePt[] = new short[p_FrameLen];
        short p_PcmOutputFramePt[] = new short[p_FrameLen];
        short p_PcmSwapFramePt[];
        short p_PcmResultFramePt[] = new short[p_FrameLen];
        HTLong DataLenPt = new HTLong();
        byte p_SpeexFramePt[] = new byte[p_FrameLen];
        HTInt p_VoiceActStsPt = new HTInt();
        HTLong SpeexFrameLenObj = new HTLong();
        HTInt IsNeedTransObj = new HTInt();
        long p_FrameTotal; //帧总数。

        p_Result = p_AudioInputWaveFileReaderPt.Init( ( p_AudioInputFileFullPathStrPt + "\0" ).getBytes(), NumChanlPt, SamplingRatePt, SamplingBitPt );
        p_Result = p_AudioOutputWaveFileReaderPt.Init( ( p_AudioOutputFileFullPathStrPt + "\0" ).getBytes(), NumChanlPt, SamplingRatePt, SamplingBitPt );
        p_Result = p_AudioResultWaveFileWriterPt.Init( ( p_AudioResultFileFullPathStrPt + "\0" ).getBytes(), NumChanlPt.m_Val, SamplingRatePt.m_Val, SamplingBitPt.m_Val );

        p_Result = p_SpeexAecPt.Init( p_SamplingRate, p_FrameLen, 500 );
        p_Result = p_SpeexPprocPt.Init( p_SamplingRate, p_FrameLen,
                1, -32768,
                0,
                0, 98, 98,
                0, 32767, 32768, -32768, 32768,
                1, p_SpeexAecPt.GetSpeexAecPt(), 3.0f, 0.6f, -32768, -32768 );
        p_Result = p_SpeexPprocOtherPt.Init( p_SamplingRate, p_FrameLen,
                0, -32768,
                0,
                1, 98, 98,
                1, 32767, 32768, -32768, 32768,
                0, p_SpeexAecPt.GetSpeexAecPt(), 3.0f, 0.6f, -32768, -32768 );
        p_Result = p_WebRtcAecmPt.Init( p_SamplingRate, p_FrameLen, 0, 4, 0 );
        p_Result = p_WebRtcAecPt.Init( p_SamplingRate, p_FrameLen, 2, 20, 1, 1 );
        p_Result = p_SpeexWebRtcAecPt.Init( p_SamplingRate, p_FrameLen, 3,
                500, 3.0f, 0.6f, -32768, -32768,
                0, 4, 0,
                2, 20, 1, 1 );
        p_Result = p_WebRtcNsx.Init( p_SamplingRate, p_FrameLen, 3 );
        p_Result = p_WebRtcNs.Init( p_SamplingRate, p_FrameLen, 3 );
        p_Result = p_RNNoise.Init( p_SamplingRate, p_FrameLen );
        p_Result = p_SpeexEncoderPt.Init( p_SamplingRate, 0, 10, 10, 100 );
        p_Result = p_SpeexDecoderPt.Init( p_SamplingRate, 1 );

        for( p_FrameTotal = 0; ; p_FrameTotal++ )
        {
            if( p_AudioInputWaveFileReaderPt.ReadData( p_PcmInputFramePt, p_PcmInputFramePt.length, DataLenPt ) != 0 )
                break;
            if( DataLenPt.m_Val != p_PcmInputFramePt.length ) break;

            if( p_AudioOutputWaveFileReaderPt.ReadData( p_PcmOutputFramePt, p_PcmOutputFramePt.length, DataLenPt ) != 0 )
                break;
            if( DataLenPt.m_Val != p_PcmOutputFramePt.length ) break;

            /*p_Result = p_SpeexAecPt.Proc( p_PcmInputFramePt, p_PcmOutputFramePt, p_PcmResultFramePt );
            p_PcmSwapFramePt = p_PcmInputFramePt;
            p_PcmInputFramePt = p_PcmResultFramePt;
            p_PcmResultFramePt = p_PcmSwapFramePt;*/

            /*p_Result = p_SpeexPprocPt.Proc( p_PcmInputFramePt, p_PcmResultFramePt, p_VoiceActStsPt );
            p_PcmSwapFramePt = p_PcmInputFramePt;
            p_PcmInputFramePt = p_PcmResultFramePt;
            p_PcmResultFramePt = p_PcmSwapFramePt;*/

            /*p_Result = p_WebRtcAecmPt.Proc( p_PcmInputFramePt, p_PcmOutputFramePt, p_PcmResultFramePt );
            p_PcmSwapFramePt = p_PcmInputFramePt;
            p_PcmInputFramePt = p_PcmResultFramePt;
            p_PcmResultFramePt = p_PcmSwapFramePt;*/

            /*p_Result = p_WebRtcAecPt.Proc( p_PcmInputFramePt, p_PcmOutputFramePt, p_PcmResultFramePt );
            p_PcmSwapFramePt = p_PcmInputFramePt;
            p_PcmInputFramePt = p_PcmResultFramePt;
            p_PcmResultFramePt = p_PcmSwapFramePt;*/

            /*p_Result = p_SpeexWebRtcAecPt.Proc( p_PcmInputFramePt, p_PcmOutputFramePt, p_PcmResultFramePt );
            p_PcmSwapFramePt = p_PcmInputFramePt;
            p_PcmInputFramePt = p_PcmResultFramePt;
            p_PcmResultFramePt = p_PcmSwapFramePt;*/

            /*p_Result = p_WebRtcNsx.Proc( p_PcmInputFramePt, p_PcmResultFramePt );
            p_PcmSwapFramePt = p_PcmInputFramePt;
            p_PcmInputFramePt = p_PcmResultFramePt;
            p_PcmResultFramePt = p_PcmSwapFramePt;*/

            /*p_Result = p_WebRtcNs.Proc( p_PcmInputFramePt, p_PcmResultFramePt );
            p_PcmSwapFramePt = p_PcmInputFramePt;
            p_PcmInputFramePt = p_PcmResultFramePt;
            p_PcmResultFramePt = p_PcmSwapFramePt;*/

            /*p_Result = p_RNNoise.Proc( p_PcmInputFramePt, p_PcmResultFramePt );
            p_PcmSwapFramePt = p_PcmInputFramePt;
            p_PcmInputFramePt = p_PcmResultFramePt;
            p_PcmResultFramePt = p_PcmSwapFramePt;*/

            /*p_Result = p_SpeexPprocOtherPt.Proc( p_PcmInputFramePt, p_PcmResultFramePt, p_VoiceActStsPt );
            if( p_VoiceActStsPt.m_Val == 0 ) Arrays.fill( p_PcmResultFramePt, ( short ) 0 );
            p_PcmSwapFramePt = p_PcmInputFramePt;
            p_PcmInputFramePt = p_PcmResultFramePt;
            p_PcmResultFramePt = p_PcmSwapFramePt;*/

            /*p_Result = p_SpeexEncoderPt.Proc( p_PcmInputFramePt, p_SpeexFramePt, p_SpeexFramePt.length, SpeexFrameLenObj, IsNeedTransObj );
            p_Result = p_SpeexDecoderPt.Proc( p_SpeexFramePt, SpeexFrameLenObj.m_Val, p_PcmInputFramePt );*/

            /*if( p_AudioResultWaveFileWriterPt.WriteData( p_PcmInputFramePt, p_PcmInputFramePt.length ) != 0 )
                break;
        }

        p_Result = p_SpeexAecPt.Destroy();
        p_Result = p_SpeexPprocPt.Destroy();
        p_Result = p_WebRtcAecmPt.Destroy();
        p_Result = p_WebRtcAecPt.Destroy();
        p_Result = p_WebRtcNsx.Destroy();
        p_Result = p_WebRtcNs.Destroy();
        p_Result = p_RNNoise.Destroy();
        p_Result = p_SpeexEncoderPt.Destroy();
        p_Result = p_SpeexDecoderPt.Destroy();
        p_Result = p_AudioInputWaveFileReaderPt.Destroy();
        p_Result = p_AudioOutputWaveFileReaderPt.Destroy();
        p_Result = p_AudioResultWaveFileWriterPt.Destroy();*/
    }

    //返回键。
    @Override
    public void onBackPressed()
    {
        if( m_LyotActivityCurViewPt == m_LyotActivityMainViewPt )
        {
            Log.i( m_CurClsNameStrPt, "用户在主界面按下返回键，本软件退出。" );
            finish();
        }
        else if( m_LyotActivityCurViewPt == m_LyotActivitySettingViewPt )
        {
            this.OnClickSettingOk( null );
        }
        else if( ( m_LyotActivityCurViewPt == m_LyotActivitySpeexAecViewPt ) ||
                ( m_LyotActivityCurViewPt == m_LyotActivityWebRtcAecmViewPt ) ||
                ( m_LyotActivityCurViewPt == m_LyotActivityWebRtcAecViewPt ) ||
                ( m_LyotActivityCurViewPt == m_LyotActivitySpeexWebRtcAecViewPt ) ||
                ( m_LyotActivityCurViewPt == m_LyotActivitySpeexPprocNsViewPt ) ||
                ( m_LyotActivityCurViewPt == m_LyotActivityWebRtcNsxViewPt ) ||
                ( m_LyotActivityCurViewPt == m_LyotActivityWebRtcNsViewPt ) ||
                ( m_LyotActivityCurViewPt == m_LyotActivitySpeexPprocOtherViewPt ) ||
                ( m_LyotActivityCurViewPt == m_LyotActivitySpeexCodecViewPt ) ||
                ( m_LyotActivityCurViewPt == m_LyotActivityAjbViewPt ) )
        {
            this.OnClickWebRtcAecSettingOk( null );
        }
        else if( m_LyotActivityCurViewPt == m_LyotActivityReadMeViewPt )
        {
            this.OnClickReadMeOk( null );
        }
    }

    //创建服务器或连接服务器按钮。
    public void OnClickCreateSrvrAndConnectSrvr( View BtnPt )
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。


        out:
        {
            if( m_MyAudioProcThreadPt == null ) //如果音频处理线程还没有启动。
            {
                Log.i( m_CurClsNameStrPt, "开始启动音频处理线程。" );

                m_MyAudioProcThreadPt = new MyAudioProcThread();

                if( BtnPt.getId() == R.id.CreateSrvrBtn )
                {
                    m_MyAudioProcThreadPt.m_IsCreateSrvrOrClnt = 1; //标记创建服务端接受客户端。
                }
                else if( BtnPt.getId() == R.id.ConnectSrvrBtn )
                {
                    m_MyAudioProcThreadPt.m_IsCreateSrvrOrClnt = 0; //标记创建客户端连接服务端。
                }

                m_MyAudioProcThreadPt.m_MainActivityHandlerPt = m_MainActivityHandlerPt;

                //设置IP地址字符串、端口、音频播放线程启动延迟。
                m_MyAudioProcThreadPt.m_IPAddrStrPt = ( ( EditText ) m_LyotActivityMainViewPt.findViewById( R.id.IPAddrEdit ) ).getText().toString();
                m_MyAudioProcThreadPt.m_Port = Integer.parseInt( ( ( EditText ) m_LyotActivityMainViewPt.findViewById( R.id.PortEdit ) ).getText().toString() );

                //初始化音频处理线程类对象。
                m_MyAudioProcThreadPt.Init(
                        getApplicationContext(),
                        ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSamplingRate8000RadioBtn ) ).isChecked() ) ? 8000 :
                                ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSamplingRate16000RadioBtn ) ).isChecked() ) ? 16000 :
                                        ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSamplingRate32000RadioBtn ) ).isChecked() ) ? 32000 : 0,
                        ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseFrame10msLenRadioBtn ) ).isChecked() ) ? 10 :
                                ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseFrame20msLenRadioBtn ) ).isChecked() ) ? 20 :
                                        ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseFrame30msLenRadioBtn ) ).isChecked() ) ? 30 : 0 );

                //判断是否打印Logcat日志。
                if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsPrintLogcatCheckBox ) ).isChecked() )
                {
                    m_MyAudioProcThreadPt.SetPrintLogcat( 1 );
                }
                else
                {
                    m_MyAudioProcThreadPt.SetPrintLogcat( 0 );
                }

                //判断是否使用唤醒锁。
                if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseWakeLockCheckBox ) ).isChecked() )
                {
                    m_MyAudioProcThreadPt.SetUseWakeLock( 1 );
                }
                else
                {
                    m_MyAudioProcThreadPt.SetUseWakeLock( 0 );
                }

                //判断是否不使用声学回音消除器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseNoAecRadioBtn ) ).isChecked() )
                {
                    m_MyAudioProcThreadPt.SetUseNoAec();
                }

                //判断是否使用Speex声学回音消除器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexAecRadioBtn ) ).isChecked() )
                {
                    try
                    {
                        m_MyAudioProcThreadPt.SetUseSpeexAec(
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecFilterLenEdit ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCheckBox ) ).isChecked() ) ? 1 : 0,
                                m_ExternalDirFullPathStrPt + "/SpeexAecMemory"
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用WebRtc定点版声学回音消除器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcAecmRadioBtn ) ).isChecked() )
                {
                    try
                    {
                        m_MyAudioProcThreadPt.SetUseWebRtcAecm(
                                ( ( ( CheckBox ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.CheckBoxWebRtcAecmIsUseCNGMode ) ).isChecked() ) ? 1 : 0,
                                Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmEchoMode ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmDelay ) ).getText().toString() )
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用WebRtc浮点版声学回音消除器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcAecRadioBtn ) ).isChecked() )
                {
                    try
                    {
                        m_MyAudioProcThreadPt.SetUseWebRtcAec(
                                Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecEchoModeEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecDelayEdit ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgnosticModeCheckBox ) ).isChecked() ) ? 1 : 0,
                                ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCheckBox ) ).isChecked() ) ? 1 : 0,
                                ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCheckBox ) ).isChecked() ) ? 1 : 0,
                                m_ExternalDirFullPathStrPt + "/WebRtcAecMemory"
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用SpeexWebRtc三重声学回音消除器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexWebRtcAecRadioBtn ) ).isChecked() )
                {
                    try
                    {
                        m_MyAudioProcThreadPt.SetUseSpeexWebRtcAec(
                                ( ( RadioButton ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmRadioBtn ) ).isChecked() ? 1 :
                                        ( ( RadioButton ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeWebRtcAecmWebRtcAecRadioBtn ) ).isChecked() ? 2 :
                                                ( ( RadioButton ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRadioBtn ) ).isChecked() ? 3 : 0,
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdit ) ).getText().toString() ),
                                Float.parseFloat( ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMultipleEdit ) ).getText().toString() ),
                                Float.parseFloat( ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoContEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdit ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCheckBox ) ).isChecked() ) ? 1 : 0,
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdit ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticModeCheckBox ) ).isChecked() ) ? 1 : 0,
                                ( ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCheckBox ) ).isChecked() ) ? 1 : 0
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否不使用噪音抑制器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseNoNsRadioBtn ) ).isChecked() )
                {
                    m_MyAudioProcThreadPt.SetUseNoNs();
                }

                //判断是否使用Speex预处理器的噪音抑制。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexPprocNsRadioBtn ) ).isChecked() )
                {
                    try
                    {
                        m_MyAudioProcThreadPt.SetUseSpeexPprocNs(
                                ( ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseNsCheckBox ) ).isChecked() ) ? 1 : 0,
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocNoiseSupesEdit ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseDereverbCheckBox ) ).isChecked() ) ? 1 : 0,
                                ( ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseRecCheckBox ) ).isChecked() ) ? 1 : 0,
                                Float.parseFloat( ( ( TextView ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocEchoMultipleEdit ) ).getText().toString() ),
                                Float.parseFloat( ( ( TextView ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocEchoContEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocEchoSupesEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocEchoSupesActEdit ) ).getText().toString() )
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用WebRtc定点版噪音抑制器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcNsxRadioBtn ) ).isChecked() )
                {
                    try
                    {
                        m_MyAudioProcThreadPt.SetUseWebRtcNsx(
                                Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcNsxViewPt.findViewById( R.id.WebRtcNsxPolicyMode ) ).getText().toString() )
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用WebRtc浮点版噪音抑制器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcNsRadioBtn ) ).isChecked() )
                {
                    try
                    {
                        m_MyAudioProcThreadPt.SetUseWebRtcNs(
                                Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcNsViewPt.findViewById( R.id.WebRtcNsPolicyMode ) ).getText().toString() )
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用RNNoise噪音抑制器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseRNNoiseRadioBtn ) ).isChecked() )
                {
                    try
                    {
                        m_MyAudioProcThreadPt.SetUseRNNoise();
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用Speex预处理器的其他功能。
                if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSpeexPprocOtherCheckBox ) ).isChecked() )
                {
                    try
                    {
                        m_MyAudioProcThreadPt.SetSpeexPprocOther(
                                1,
                                ( ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseVadCheckBox ) ).isChecked() ) ? 1 : 0,
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbStartEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbContEdit ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseAgcCheckBox ) ).isChecked() ) ? 1 : 0,
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcLevelEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcIncrementEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcDecrementEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcMaxGainEdit ) ).getText().toString() )
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用PCM原始数据。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UsePcmRadioBtn ) ).isChecked() )
                {
                    m_MyAudioProcThreadPt.SetUsePcm();
                }

                //判断是否使用Speex编解码器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexCodecRadioBtn ) ).isChecked() )
                {
                    try
                    {
                        m_MyAudioProcThreadPt.SetUseSpeexCodec(
                                ( ( ( RadioButton ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderUseCbrRadioBtn ) ).isChecked() ) ? 0 : 1,
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderQualityEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderComplexityEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderPlcExpectedLossRateEdit ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecIsUsePerceptualEnhancementCheckBox ) ).isChecked() ) ? 1 : 0
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用Opus编解码器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseOpusCodecRadioBtn ) ).isChecked() )
                {
                    m_MyAudioProcThreadPt.SetUseOpusCodec();
                }

                //判断是否使用链表。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseLnkLstRadioBtn ) ).isChecked() )
                {
                    m_MyAudioProcThreadPt.m_UseWhatRecvOutputFrame = 0;
                }

                //判断是否使用自己设计的自适应抖动缓冲器。
                if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAjbRadioBtn ) ).isChecked() )
                {
                    m_MyAudioProcThreadPt.m_UseWhatRecvOutputFrame = 1;

                    try
                    {
                        m_MyAudioProcThreadPt.m_AjbMinNeedBufFrameCnt = Integer.parseInt( ( ( TextView ) m_LyotActivityAjbViewPt.findViewById( R.id.AjbMinNeedBufFrameCnt ) ).getText().toString() );
                        m_MyAudioProcThreadPt.m_AjbMaxNeedBufFrameCnt = Integer.parseInt( ( ( TextView ) m_LyotActivityAjbViewPt.findViewById( R.id.AjbMaxNeedBufFrameCnt ) ).getText().toString() );
                        m_MyAudioProcThreadPt.m_AjbAdaptSensitivity = ( byte ) Integer.parseInt( ( ( TextView ) m_LyotActivityAjbViewPt.findViewById( R.id.AjbAdaptSensitivity ) ).getText().toString() );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否保存音频到文件。
                if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveAudioToFileCheckBox ) ).isChecked() )
                {
                    m_MyAudioProcThreadPt.SetSaveAudioToFile(
                            1,
                            m_ExternalDirFullPathStrPt + "/AudioInput.wav",
                            m_ExternalDirFullPathStrPt + "/AudioOutput.wav",
                            m_ExternalDirFullPathStrPt + "/AudioResult.wav"
                    );
                }

                m_MyAudioProcThreadPt.start();

                Log.i( m_CurClsNameStrPt, "启动音频处理线程完毕。" );

                if( BtnPt.getId() == R.id.CreateSrvrBtn )
                {
                    ( ( EditText ) findViewById( R.id.IPAddrEdit ) ).setEnabled( false ); //设置IP地址控件为不可用
                    ( ( EditText ) findViewById( R.id.PortEdit ) ).setEnabled( false ); //设置端口控件为不可用
                    ( ( Button ) findViewById( R.id.CreateSrvrBtn ) ).setText( "中断" ); //设置创建服务端按钮的内容为“中断”
                    ( ( Button ) findViewById( R.id.ConnectSrvrBtn ) ).setEnabled( false ); //设置连接服务端按钮为不可用
                    ( ( Button ) findViewById( R.id.SettingBtn ) ).setEnabled( false ); //设置设置按钮为不可用
                }
                else if( BtnPt.getId() == R.id.ConnectSrvrBtn )
                {
                    ( ( EditText ) findViewById( R.id.IPAddrEdit ) ).setEnabled( false ); //设置IP地址控件为不可用
                    ( ( EditText ) findViewById( R.id.PortEdit ) ).setEnabled( false ); //设置端口控件为不可用
                    ( ( Button ) findViewById( R.id.CreateSrvrBtn ) ).setEnabled( false ); //设置创建服务端按钮为不可用
                    ( ( Button ) findViewById( R.id.ConnectSrvrBtn ) ).setText( "中断" ); //设置连接服务端按钮的内容为“中断”
                    ( ( Button ) findViewById( R.id.SettingBtn ) ).setEnabled( false ); //设置设置按钮为不可用
                }
            }
            else
            {
                m_MyAudioProcThreadPt.RequireExit(); //请求音频处理线程退出。

                try
                {
                    Log.i( m_CurClsNameStrPt, "开始等待音频处理线程退出。" );
                    m_MyAudioProcThreadPt.join(); //等待音频处理线程退出。
                    Log.i( m_CurClsNameStrPt, "结束等待音频处理线程退出。" );
                }
                catch( InterruptedException e )
                {

                }
            }

            p_Result = 0;

            break out;
        }

        if( p_Result != 0 )
        {
            m_MyAudioProcThreadPt = null;
        }
    }

    //主界面设置按钮。
    public void OnClickSetting( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //主界面清空日志按钮。
    public void OnClickClearLog( View BtnPt )
    {
        LinearLayout clLogLinearLayout = ( LinearLayout ) m_LyotActivityMainViewPt.findViewById( R.id.LogLinearLyot );
        clLogLinearLayout.removeAllViews();
    }

    //主界面必读说明按钮。
    public void OnClickReadMe( View BtnPt )
    {
        setContentView( m_LyotActivityReadMeViewPt );
        m_LyotActivityCurViewPt = m_LyotActivityReadMeViewPt;
    }

    //必读说明界面的确定按钮。
    public void OnClickReadMeOk( View BtnPt )
    {
        setContentView( m_LyotActivityMainViewPt );
        m_LyotActivityCurViewPt = m_LyotActivityMainViewPt;
    }

    //设置界面的确定按钮。
    public void OnClickSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivityMainViewPt );
        m_LyotActivityCurViewPt = m_LyotActivityMainViewPt;
    }

    //Speex声学回音消除器设置按钮。
    public void OnClickSpeexAecSetting( View BtnPt )
    {
        setContentView( m_LyotActivitySpeexAecViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySpeexAecViewPt;
    }

    //Speex声学回音消除器设置界面的删除内存块文件按钮。
    public void OnClickSpeexAecDelMemFile( View BtnPt )
    {
        String p_pclSpeexAecMemoryFullPath = m_ExternalDirFullPathStrPt + "/SpeexAecMemory";
        File file = new File( p_pclSpeexAecMemoryFullPath );
        if( file.exists() )
        {
            if( file.delete() )
            {
                Toast.makeText( this, "删除Speex声学回音消除器的内存块文件 " + p_pclSpeexAecMemoryFullPath + " 成功。", Toast.LENGTH_LONG ).show();
            }
            else
            {
                Toast.makeText( this, "删除Speex声学回音消除器的内存块文件 " + p_pclSpeexAecMemoryFullPath + " 失败。", Toast.LENGTH_LONG ).show();
            }
        }
        else
        {
            Toast.makeText( this, "Speex声学回音消除器的内存块文件 " + p_pclSpeexAecMemoryFullPath + " 不存在。", Toast.LENGTH_LONG ).show();
        }
    }

    //Speex声学回音消除器设置界面的确定按钮。
    public void OnClickSpeexAecSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //WebRtc定点版声学回音消除器设置按钮。
    public void OnClickWebRtcAecmSetting( View BtnPt )
    {
        setContentView( m_LyotActivityWebRtcAecmViewPt );
        m_LyotActivityCurViewPt = m_LyotActivityWebRtcAecmViewPt;
    }

    //WebRtc定点版声学回音消除器设置界面的确定按钮。
    public void OnClickWebRtcAecmSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //WebRtc浮点版声学回音消除器设置按钮。
    public void OnClickWebRtcAecSetting( View BtnPt )
    {
        setContentView( m_LyotActivityWebRtcAecViewPt );
        m_LyotActivityCurViewPt = m_LyotActivityWebRtcAecViewPt;
    }

    //WebRtc浮点版声学回音消除器设置界面的删除内存块文件按钮。
    public void OnClickWebRtcAecDelMemFile( View BtnPt )
    {
        String p_pclWebRtcAecMemoryFullPath = m_ExternalDirFullPathStrPt + "/WebRtcAecMemory";
        File file = new File( p_pclWebRtcAecMemoryFullPath );
        if( file.exists() )
        {
            if( file.delete() )
            {
                Toast.makeText( this, "删除WebRtc浮点版声学回音消除器的内存块文件 " + p_pclWebRtcAecMemoryFullPath + " 成功。", Toast.LENGTH_LONG ).show();
            }
            else
            {
                Toast.makeText( this, "删除WebRtc浮点版声学回音消除器的内存块文件 " + p_pclWebRtcAecMemoryFullPath + " 失败。", Toast.LENGTH_LONG ).show();
            }
        }
        else
        {
            Toast.makeText( this, "WebRtc浮点版声学回音消除器的内存块文件 " + p_pclWebRtcAecMemoryFullPath + " 不存在。", Toast.LENGTH_LONG ).show();
        }
    }

    //WebRtc浮点版声学回音消除器设置界面的确定按钮。
    public void OnClickWebRtcAecSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //SpeexWebRtc三重声学回音消除器设置按钮。
    public void OnClickSpeexWebRtcAecSetting( View BtnPt )
    {
        setContentView( m_LyotActivitySpeexWebRtcAecViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySpeexWebRtcAecViewPt;
    }

    //SpeexWebRtc三重声学回音消除器设置界面的确定按钮。
    public void OnClickSpeexWebRtcAecSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //Speex预处理器的噪音抑制设置按钮。
    public void OnClickSpeexPprocNsSetting( View BtnPt )
    {
        setContentView( m_LyotActivitySpeexPprocNsViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySpeexPprocNsViewPt;
    }

    //Speex预处理器的噪音抑制设置界面的确定按钮。
    public void OnClickSpeexPprocNsSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //WebRtc定点版噪音抑制器设置按钮。
    public void OnClickWebRtcNsxSetting( View BtnPt )
    {
        setContentView( m_LyotActivityWebRtcNsxViewPt );
        m_LyotActivityCurViewPt = m_LyotActivityWebRtcNsxViewPt;
    }

    //WebRtc定点版噪音抑制器设置界面的确定按钮。
    public void OnClickWebRtcNsxSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //WebRtc浮点版噪音抑制器设置按钮。
    public void OnClickWebRtcNsSetting( View BtnPt )
    {
        setContentView( m_LyotActivityWebRtcNsViewPt );
        m_LyotActivityCurViewPt = m_LyotActivityWebRtcNsViewPt;
    }

    //WebRtc浮点版噪音抑制器设置界面的确定按钮。
    public void OnClickWebRtcNsSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //Speex预处理器的其他功能设置按钮。
    public void OnClickSpeexPprocOtherSetting( View BtnPt )
    {
        setContentView( m_LyotActivitySpeexPprocOtherViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySpeexPprocOtherViewPt;
    }

    //Speex预处理器的其他功能设置界面的确定按钮。
    public void OnClickSpeexPprocOtherSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //Speex编解码器设置按钮。
    public void OnClickSpeexCodecSetting( View BtnPt )
    {
        setContentView( m_LyotActivitySpeexCodecViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySpeexCodecViewPt;
    }

    //Speex编解码器设置界面的确定按钮。
    public void OnClickSpeexCodecSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //Opus编解码器设置按钮。
    public void OnClickOpusCodecSetting( View BtnPt )
    {
        setContentView( m_LyotActivitySpeexCodecViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySpeexCodecViewPt;
    }

    //Opus编解码器设置界面的确定按钮。
    public void OnOpusCodecSettingOkClick( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //自适应抖动缓冲器设置按钮。
    public void OnClickAjbSetting( View BtnPt )
    {
        setContentView( m_LyotActivityAjbViewPt );
        m_LyotActivityCurViewPt = m_LyotActivityAjbViewPt;
    }

    //自适应抖动缓冲器设置界面的确定按钮。
    public void OnClickAjbSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

}
