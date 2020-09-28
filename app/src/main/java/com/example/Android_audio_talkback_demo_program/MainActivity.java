package com.example.Android_audio_talkback_demo_program;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;

import HeavenTao.Audio.*;
import HeavenTao.Data.*;
import HeavenTao.Sokt.*;

//主界面消息处理类。
class MainActivityHandler extends Handler
{
    String m_CurClsNameStrPt = this.getClass().getSimpleName(); //当前类名称字符串类对象的内存指针。

    MainActivity m_MainActivityPt;

    public void handleMessage( Message MessagePt )
    {
        if( MessagePt.what == 1 ) //如果是音频处理线程启动的消息。
        {
            if( m_MainActivityPt.m_MyAudioProcThreadPt.m_IsCreateSrvrOrClnt == 1 ) //如果是创建服务端。
            {
                ( ( EditText ) m_MainActivityPt.findViewById( R.id.IPAddrEdit ) ).setEnabled( false ); //设置IP地址控件为不可用。
                ( ( EditText ) m_MainActivityPt.findViewById( R.id.PortEdit ) ).setEnabled( false ); //设置端口控件为不可用。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtn ) ).setText( "中断" ); //设置创建服务端按钮的内容为“中断”。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.ConnectSrvrBtn ) ).setEnabled( false ); //设置连接服务端按钮为不可用。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.SettingBtn ) ).setEnabled( false ); //设置设置按钮为不可用。
            }
            else //如果是创建客户端。
            {
                ( ( EditText ) m_MainActivityPt.findViewById( R.id.IPAddrEdit ) ).setEnabled( false ); //设置IP地址控件为不可用。
                ( ( EditText ) m_MainActivityPt.findViewById( R.id.PortEdit ) ).setEnabled( false ); //设置端口控件为不可用。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtn ) ).setEnabled( false ); //设置创建服务端按钮为不可用。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.ConnectSrvrBtn ) ).setText( "中断" ); //设置连接服务端按钮的内容为“中断”。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.SettingBtn ) ).setEnabled( false ); //设置设置按钮为不可用。
            }

            //判断是否使用唤醒锁。
            if( ( ( CheckBox ) m_MainActivityPt.m_LyotActivitySettingViewPt.findViewById( R.id.IsUseWakeLockCheckBox ) ).isChecked() )
            {
                m_MainActivityPt.SetUseWakeLock( 1 );
            }
            else
            {
                m_MainActivityPt.SetUseWakeLock( 0 );
            }
        }
        else if( MessagePt.what == 2 ) //如果是音频处理线程退出的消息。
        {
            m_MainActivityPt.m_MyAudioProcThreadPt = null;

            ( ( EditText ) m_MainActivityPt.findViewById( R.id.IPAddrEdit ) ).setEnabled( true ); //设置IP地址控件为可用。
            ( ( EditText ) m_MainActivityPt.findViewById( R.id.PortEdit ) ).setEnabled( true ); //设置端口控件为可用。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtn ) ).setText( "创建服务端" ); //设置创建服务端按钮的内容为“创建服务端”。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.ConnectSrvrBtn ) ).setEnabled( true ); //设置连接服务端按钮为可用。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.ConnectSrvrBtn ) ).setText( "连接服务端" ); //设置连接服务端按钮的内容为“连接服务端”。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtn ) ).setEnabled( true ); //设置创建服务端按钮为可用。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.SettingBtn ) ).setEnabled( true ); //设置设置按钮为可用。

            m_MainActivityPt.SetUseWakeLock( 0 );
        }
        else if( MessagePt.what == 3 ) //如果是显示日志的消息。
        {
            TextView p_LogTextView = new TextView( m_MainActivityPt );
            p_LogTextView.setText( ( new SimpleDateFormat( "HH:mm:ss SSS" ) ).format( new Date() ) + "：" + MessagePt.obj );
            ( ( LinearLayout ) m_MainActivityPt.m_LyotActivityMainViewPt.findViewById( R.id.LogLinearLyot ) ).addView( p_LogTextView );
        }
    }
}

//数据包类型：0x00表示连接请求或心跳，0x01表示输入输出帧，0x02表示连接应答或输入输出帧应答，0x03表示退出。
//我的音频处理线程类。
class MyAudioProcThread extends AudioProcThread
{
    String m_IPAddrStrPt; //存放IP地址字符串类对象的内存指针。
    String m_PortStrPt; //存放端口字符串类对象的内存指针。
    Handler m_MainActivityHandlerPt; //存放主界面消息处理类对象的内存指针。

    int m_UseWhatXfrPrtcl; //存放使用什么传输协议，为0表示TCP协议，为1表示UDP协议。
    int m_IsCreateSrvrOrClnt; //存放创建服务端或者客户端标记，为1表示创建服务端，为0表示创建客户端。
    TcpSrvrSokt m_TcpSrvrSoktPt; //存放本端TCP协议服务端套接字类对象的内存指针。
    TcpClntSokt m_TcpClntSoktPt; //存放本端TCP协议客户端套接字类对象的内存指针。
    UdpSokt m_UdpSoktPt; //存放本端UDP协议套接字类对象的内存指针。
    long m_LastPktSendTime; //存放最后一个数据包的发送时间，用于判断连接是否中断。
    long m_LastPktRecvTime; //存放最后一个数据包的接收时间，用于判断连接是否中断。

    int m_LastSendInputFrameIsAct; //存放最后一个发送的输入帧有无语音活动，为1表示有语音活动，为0表示无语音活动。
    int m_LastSendInputFrameIsRecv; //存放最后一个发送的输入帧远端是否接收到，为0表示没有收到，为非0表示已经收到。
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

    VarStr m_ErrInfoVarStrPt; //存放错误信息动态字符串类对象的内存指针，可以为NULL。

    //用户定义的初始化函数，在本线程刚启动时调用一次，返回值表示是否成功，为0表示成功，为非0表示失败。
    public int UserInit()
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            {Message p_MessagePt = new Message();p_MessagePt.what = 1;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送音频处理线程启动的消息。

            m_IsRecvExitPkt = 0; //设置没有接收到退出包。
            if( m_TmpBytePt == null ) m_TmpBytePt = new byte[1 + 4 + m_FrameLen * 2]; //初始化存放临时数据的数组。
            if( m_ErrInfoVarStrPt == null ) //创建并初始化错误信息动态字符串类对象。
            {
                m_ErrInfoVarStrPt = new VarStr();
                if( m_ErrInfoVarStrPt.Init() != 0 )
                {
                    m_ErrInfoVarStrPt = null;
                }
            }

            if( m_UseWhatXfrPrtcl == 0 ) //如果使用TCP协议。
            {
                if( m_IsCreateSrvrOrClnt == 1 ) //如果是创建本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接。
                {
                    m_TcpSrvrSoktPt = new TcpSrvrSokt();

                    if( m_TcpSrvrSoktPt.Init( 4, m_IPAddrStrPt, m_PortStrPt, 1, 1, m_ErrInfoVarStrPt ) == 0 ) //如果创建并初始化已监听的本端TCP协议服务端套接字成功。
                    {
                        HTString p_LclNodeAddrPt = new HTString();
                        HTString p_LclNodePortPt = new HTString();

                        if( m_TcpSrvrSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, m_ErrInfoVarStrPt ) != 0 ) //如果获取已监听的本端TCP协议服务端套接字绑定的本地节点地址和端口失败。
                        {
                            String p_InfoStrPt = "获取已监听的本端TCP协议服务端套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                            Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break out;
                        }

                        String p_InfoStrPt = "创建并初始化已监听的本端TCP协议服务端套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]成功。";
                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    }
                    else //如果创建并初始化已监听的本端TCP协议服务端套接字失败。
                    {
                        String p_InfoStrPt = "创建并初始化已监听的本端TCP协议服务端套接字[" + m_IPAddrStrPt + ":" + m_PortStrPt + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    m_TcpClntSoktPt = new TcpClntSokt();

                    while( true ) //循环接受远端TCP协议客户端套接字的连接。
                    {
                        HTString p_RmtNodeAddrPt = new HTString();
                        HTString p_RmtNodePortPt = new HTString();

                        if( m_TcpSrvrSoktPt.Accept( null, p_RmtNodeAddrPt, p_RmtNodePortPt, ( short ) 1, m_TcpClntSoktPt, m_ErrInfoVarStrPt ) == 0 )
                        {
                            if( m_TcpClntSoktPt.GetTcpClntSoktPt() != 0 ) //如果用已监听的本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接成功。
                            {
                                m_TcpSrvrSoktPt.Destroy( null ); //关闭并销毁已创建的本端TCP协议服务端套接字，防止还有其他远端TCP协议客户端套接字继续连接。
                                m_TcpSrvrSoktPt = null;

                                String p_InfoStrPt = "用已监听的本端TCP协议服务端套接字接受远端TCP协议客户端套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]的连接成功。";
                                Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break;
                            }
                            else //如果用已监听的本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接超时，就重新接受。
                            {

                            }
                        }
                        else
                        {
                            m_TcpClntSoktPt = null;

                            String p_InfoStrPt = "用已监听的本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                            Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break out;
                        }

                        if( m_ExitFlag != 0 ) //如果本线程接收到退出请求。
                        {
                            m_TcpClntSoktPt = null;

                            Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
                            break out;
                        }
                    }
                }
                else if( m_IsCreateSrvrOrClnt == 0 ) //如果是创建本端TCP协议客户端套接字连接远端TCP协议服务端套接字。
                {
                    m_TcpClntSoktPt = new TcpClntSokt();

                    int p_ReInitTimes = 1;
                    while( true ) //循环连接已监听的远端TCP协议服务端套接字。
                    {
                        if( m_TcpClntSoktPt.Init( 4, m_IPAddrStrPt, m_PortStrPt, null, null, ( short ) 5000, m_ErrInfoVarStrPt ) == 0 ) //创建并初始化本端TCP协议客户端套接字，并连接已监听的远端TCP协议服务端套接字成功。
                        {
                            HTString p_LclNodeAddrPt = new HTString();
                            HTString p_LclNodePortPt = new HTString();
                            HTString p_RmtNodeAddrPt = new HTString();
                            HTString p_RmtNodePortPt = new HTString();

                            if( m_TcpClntSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, m_ErrInfoVarStrPt ) != 0 )
                            {
                                String p_InfoStrPt = "获取已连接的本端TCP协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break out;
                            }
                            if( m_TcpClntSoktPt.GetRmtAddr( null, p_RmtNodeAddrPt, p_RmtNodePortPt, m_ErrInfoVarStrPt ) != 0 )
                            {
                                String p_InfoStrPt = "获取已连接的本端TCP协议客户端套接字连接的远端TCP协议客户端套接字绑定的远程节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break out;
                            }

                            String p_InfoStrPt = "创建并初始化本端TCP协议客户端套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]，并连接已监听的远端TCP协议服务端套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]成功。";
                            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break; //跳出重连。
                        }
                        else
                        {
                            {String p_InfoStrPt = "创建并初始化本端TCP协议客户端套接字，并连接已监听的远端TCP协议服务端套接字[" + m_IPAddrStrPt + ":" + m_PortStrPt + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                            Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

                            if( p_ReInitTimes <= 5 ) //如果还需要进行重连。
                            {
                                String p_InfoStrPt = "开始第 " + p_ReInitTimes + " 次重连。";
                                Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                p_ReInitTimes++;
                                SystemClock.sleep( 500 ); //暂停一下，避免CPU使用率过高。
                            }
                            else //如果不需要重连了。
                            {
                                m_TcpClntSoktPt = null;
                                break out;
                            }
                        }
                    }
                }

                if( m_TcpClntSoktPt.SetNoDelay( 1, m_ErrInfoVarStrPt ) != 0 ) //如果设置已连接的本端TCP协议客户端套接字的Nagle延迟算法状态为禁用失败。
                {
                    String p_InfoStrPt = "设置已连接的本端TCP协议客户端套接字的Nagle延迟算法状态为禁用失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                    Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break out;
                }
            }
            else //如果使用UDP协议。
            {
                m_UdpSoktPt = new UdpSokt();

                if( m_IsCreateSrvrOrClnt == 1 ) //如果是创建本端UDP协议套接字接受远端UDP协议套接字的连接。
                {
                    if( m_UdpSoktPt.Init( 4, m_IPAddrStrPt, m_PortStrPt, m_ErrInfoVarStrPt ) == 0 ) //如果创建并初始化已监听的本端UDP协议套接字成功。
                    {
                        HTString p_LclNodeAddrPt = new HTString();
                        HTString p_LclNodePortPt = new HTString();

                        if( m_UdpSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, m_ErrInfoVarStrPt ) != 0 ) //如果获取已监听的本端UDP协议套接字绑定的本地节点地址和端口失败。
                        {
                            String p_InfoStrPt = "获取已监听的本端UDP协议套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                            Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break out;
                        }

                        String p_InfoStrPt = "创建并初始化已监听的本端UDP协议套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]成功。";
                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    }
                    else //如果创建并初始化已监听的本端UDP协议套接字失败。
                    {
                        String p_InfoStrPt = "创建并初始化已监听的本端UDP协议套接字[" + m_IPAddrStrPt + ":" + m_PortStrPt + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    HTString p_RmtNodeAddrPt = new HTString();
                    HTString p_RmtNodePortPt = new HTString();
                    HTLong p_TmpHTLong = new HTLong(  );

                    ReAccept:
                    while( true ) //循环接受远端UDP协议套接字的连接。
                    {
                        if( m_UdpSoktPt.RecvPkt( null, p_RmtNodeAddrPt, p_RmtNodePortPt, m_TmpBytePt, m_TmpBytePt.length, p_TmpHTLong, ( short ) 1, m_ErrInfoVarStrPt ) == 0 )
                        {
                            if( p_TmpHTLong.m_Val != -1 ) //如果用已监听的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个数据包成功。
                            {
                                if( ( p_TmpHTLong.m_Val == 1 ) && ( m_TmpBytePt[0] == 0x00 ) ) //如果是连接请求包。
                                {
                                    m_UdpSoktPt.Connect( 4, p_RmtNodeAddrPt.m_Val, p_RmtNodePortPt.m_Val, m_ErrInfoVarStrPt ); //用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字，已连接的本端UDP协议套接字只能接收连接的远端UDP协议套接字发送的数据包。

                                    int p_ReSendTimes = 1;
                                    ReSend:
                                    while( true ) //循环发送连接请求包，并接收连接应答包。
                                    {
                                        m_TmpBytePt[0] = 0x00; //设置连接请求包。
                                        if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, m_ErrInfoVarStrPt ) != 0 )
                                        {
                                            String p_InfoStrPt = "用已监听的本端UDP协议套接字接受远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]的连接失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                            break out;
                                        }

                                        ReRecv:
                                        while( true ) //循环接收连接应答包。
                                        {
                                            if( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, p_TmpHTLong, ( short ) 1000, m_ErrInfoVarStrPt ) == 0 )
                                            {
                                                if( p_TmpHTLong.m_Val != -1 ) //如果用已监听的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个数据包成功。
                                                {
                                                    if( ( p_TmpHTLong.m_Val >= 1 ) && ( m_TmpBytePt[0] != 0x00 ) ) //如果不是连接请求包。
                                                    {
                                                        String p_InfoStrPt = "用已监听的本端UDP协议套接字接受远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]的连接成功。";
                                                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                                        break ReAccept; //跳出连接循环。
                                                    }
                                                    else //如果是连接请求包，就不管，重新接收连接应答包。
                                                    {

                                                    }
                                                }
                                                else //如果用已监听的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个数据包超时。
                                                {
                                                    if( p_ReSendTimes <= 5 ) //如果还需要进行重发。
                                                    {
                                                        p_ReSendTimes++;
                                                        break ReRecv; //重发连接请求包。
                                                    }
                                                    else //如果不需要重连了。
                                                    {
                                                        String p_InfoStrPt = "用已监听的本端UDP协议套接字接受远端UDP协议套接字的连接失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                                        break ReSend; //重新接受连接。
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                String p_InfoStrPt = "用已监听的本端UDP协议套接字接受远端UDP协议套接字的连接失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                                break ReSend; //重新接受连接。
                                            }
                                        }
                                    }

                                    m_UdpSoktPt.Disconnect( m_ErrInfoVarStrPt ); //将已连接的本端UDP协议套接字断开连接的远端UDP协议套接字，已连接的本端UDP协议套接字将变成已监听的本端UDP协议套接字。

                                    String p_InfoStrPt = "本端UDP协议套接字继续保持监听来接受连接。";
                                    Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                    Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                }
                                else //如果是其他包，就不管。
                                {

                                }
                            }
                            else //如果用已监听的本端UDP协议套接字接受到远端UDP协议套接字的连接请求超时。
                            {

                            }
                        }
                        else
                        {
                            String p_InfoStrPt = "用已监听的本端UDP协议套接字接受远端UDP协议套接字的连接失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                            Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break out;
                        }

                        if( m_ExitFlag != 0 ) //如果本线程接收到退出请求。
                        {
                            Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
                            break out;
                        }
                    }
                }
                else if( m_IsCreateSrvrOrClnt == 0 ) //如果是创建本端UDP协议套接字连接远端UDP协议套接字。
                {
                    if( m_UdpSoktPt.Init( 4, null, null, m_ErrInfoVarStrPt ) == 0 ) //如果创建并初始化已监听的本端UDP协议套接字成功。
                    {
                        HTString p_LclNodeAddrPt = new HTString();
                        HTString p_LclNodePortPt = new HTString();

                        if( m_UdpSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, m_ErrInfoVarStrPt ) != 0 ) //如果获取已监听的本端UDP协议套接字绑定的本地节点地址和端口失败。
                        {
                            String p_InfoStrPt = "获取已监听的本端UDP协议套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                            Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break out;
                        }

                        String p_InfoStrPt = "创建并初始化已监听的本端UDP协议套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]成功。";
                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    }
                    else //如果创建并初始化已监听的本端UDP协议套接字失败。
                    {
                        String p_InfoStrPt = "创建并初始化已监听的本端UDP协议套接字[" + m_IPAddrStrPt + ":" + m_PortStrPt + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    HTString p_RmtNodeAddrPt = new HTString();
                    HTString p_RmtNodePortPt = new HTString();
                    HTLong p_TmpHTLong = new HTLong(  );

                    if( m_UdpSoktPt.Connect( 4, m_IPAddrStrPt, m_PortStrPt, m_ErrInfoVarStrPt ) != 0 )
                    {
                        String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字[" + m_IPAddrStrPt + ":" + m_PortStrPt + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    if( m_UdpSoktPt.GetRmtAddr( null, p_RmtNodeAddrPt, p_RmtNodePortPt, m_ErrInfoVarStrPt ) != 0 )
                    {
                        m_UdpSoktPt.Disconnect( m_ErrInfoVarStrPt );
                        String p_InfoStrPt = "获取已连接的本端UDP协议套接字连接的远端UDP协议套接字绑定的远程节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    int p_ReSendTimes = 1;
                    ReSend:
                    while( true ) //循环连接已监听的远端UDP协议套接字。
                    {
                        m_TmpBytePt[0] = 0x00; //设置连接请求包。
                        if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, m_ErrInfoVarStrPt ) != 0 )
                        {
                            m_UdpSoktPt.Disconnect( m_ErrInfoVarStrPt );
                            String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break out;
                        }

                        ReRecv:
                        while( true ) //循环接收连接请求包。
                        {
                            if( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, p_TmpHTLong, ( short ) 1000, m_ErrInfoVarStrPt ) == 0 )
                            {
                                if( p_TmpHTLong.m_Val != -1 ) //如果用已连接的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个数据包成功。
                                {
                                    if( ( p_TmpHTLong.m_Val == 1 ) && ( m_TmpBytePt[0] == 0x00 ) ) //如果是连接请求包。
                                    {
                                        m_TmpBytePt[0] = 0x02; //设置连接应答包。
                                        if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, m_ErrInfoVarStrPt ) != 0 )
                                        {
                                            m_UdpSoktPt.Disconnect( m_ErrInfoVarStrPt );
                                            String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                            break out;
                                        }

                                        String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]成功。";
                                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                        break ReSend; //跳出连接循环。
                                    }
                                    else //如果不是连接请求包，就不管，重新接收连接请求包。
                                    {

                                    }
                                }
                                else //如果用已连接的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个数据包超时。
                                {
                                    if( p_ReSendTimes <= 5 ) //如果还需要进行重发。
                                    {
                                        p_ReSendTimes++;
                                        break ReRecv; //重发连接请求包。
                                    }
                                    else //如果不需要重连了。
                                    {
                                        m_UdpSoktPt.Disconnect( m_ErrInfoVarStrPt );
                                        String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                        break out;
                                    }
                                }
                            }
                            else
                            {
                                m_UdpSoktPt.Disconnect( m_ErrInfoVarStrPt );
                                String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break out;
                            }
                        }
                    }
                }
            }

            switch( m_UseWhatRecvOutputFrame ) //使用什么接收输出帧。
            {
                case 0: //如果使用链表。
                {
                    m_RecvOutputFrameLnkLstPt = new LinkedList< byte[] >(); //创建接收输出帧链表类对象。

                    Log.i( m_CurClsNameStrPt, "创建并初始化接收输出帧链表类对象成功。" );
                    break;
                }
                case 1: //如果使用自适应抖动缓冲器。
                {
                    //初始化自适应抖动缓冲器类对象。
                    m_AjbPt = new Ajb();
                    if( m_AjbPt.Init( m_SamplingRate, m_FrameLen, ( byte ) 1, ( byte ) 0, m_AjbMinNeedBufFrameCnt, m_AjbMaxNeedBufFrameCnt, m_AjbAdaptSensitivity ) == 0 )
                    {
                        Log.i( m_CurClsNameStrPt, "创建并初始化自适应抖动缓冲器类对象成功。" );
                    }
                    else
                    {
                        String p_InfoStrPt = "创建并初始化自适应抖动缓冲器类对象失败。";
                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }
                    break;
                }
            }

            m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间为当前时间。
            m_LastPktRecvTime = m_LastPktSendTime; //设置最后一个数据包的接收时间为当前时间。

            m_LastSendInputFrameIsAct = 0; //设置最后发送的一个输入帧为无语音活动。
            m_LastSendInputFrameIsRecv = 1; //设置最后一个发送的输入帧远端已经接收到。
            m_SendInputFrameTimeStamp = 0 - m_FrameLen; //设置发送输入帧的时间戳为0的前一个。
            m_RecvOutputFrameTimeStamp = 0; //设置接收输出帧的时间戳为0。

            String p_InfoStrPt = "开始进行音频对讲。";
            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );

            p_Result = 0; //设置本函数执行成功。
        }

        return p_Result;
    }

    //用户定义的处理函数，在本线程运行时每隔1毫秒就调用一次，返回值表示是否成功，为0表示成功，为非0表示失败。
    public int UserProcess()
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
        HTLong p_TmpHTLong = new HTLong(  );

        out:
        {
            //接收远端发送过来的一个数据包。
            if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.RecvPkt( m_TmpBytePt, m_TmpBytePt.length, p_TmpHTLong, ( short ) 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
                ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, p_TmpHTLong, ( short ) 0, m_ErrInfoVarStrPt ) == 0 ) ) )
            {
                if( p_TmpHTLong.m_Val != -1 ) //如果用已连接的本端TCP协议客户端套接字开始接收连接的远端TCP协议客户端套接字发送的一个数据包成功。
                {
                    m_LastPktRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。

                    if( p_TmpHTLong.m_Val == 0 ) //如果数据包的数据长度为0。
                    {
                        Log.e( m_CurClsNameStrPt, "接收到数据包的数据长度为" + p_TmpHTLong.m_Val + "，表示没有数据，无法继续接收。" );
                        break out;
                    }
                    else if( m_TmpBytePt[0] == 0x00 ) //如果是心跳包。
                    {
                        if( p_TmpHTLong.m_Val > 1 ) //如果心跳包的数据长度大于1。
                        {
                            Log.e( m_CurClsNameStrPt, "接收到心跳包的数据长度为" + p_TmpHTLong.m_Val + "大于1，表示还有其他数据，无法继续接收。" );
                            break out;
                        }

                        Log.i( m_CurClsNameStrPt, "接收到一个心跳包。" );
                    }
                    else if( m_TmpBytePt[0] == 0x01 ) //如果是输出帧包。
                    {
                        if( p_TmpHTLong.m_Val < 1 + 4 ) //如果输出帧包的数据长度小于1 + 4，表示没有时间戳。
                        {
                            Log.e( m_CurClsNameStrPt, "接收到输出帧包的数据长度为" + p_TmpHTLong.m_Val + "小于1 + 4，表示没有时间戳，无法继续接收。" );
                            break out;
                        }

                        if( ( p_TmpHTLong.m_Val > 1 + 4 ) && ( m_UseWhatCodec == 0 ) && ( p_TmpHTLong.m_Val != 1 + 4 + m_FrameLen * 2 ) ) //如果该输出帧为有语音活动，且使用了PCM原始数据，但接收到的PCM格式输出帧的数据长度与帧的数据长度不同。
                        {
                            Log.e( m_CurClsNameStrPt, "接收到的PCM格式输出帧的数据长度与帧的数据长度不同，无法继续接收。" );
                            break out;
                        }

                        //读取时间戳。
                        m_RecvOutputFrameTimeStamp = ( m_TmpBytePt[1] & 0xFF ) + ( ( m_TmpBytePt[2] & 0xFF ) << 8 ) + ( ( m_TmpBytePt[3] & 0xFF ) << 16 ) + ( ( m_TmpBytePt[4] & 0xFF ) << 24 );

                        //将输出帧放入链表或自适应抖动缓冲器。
                        switch( m_UseWhatRecvOutputFrame ) //使用什么接收输出帧。
                        {
                            case 0: //如果使用链表。
                            {
                                if( p_TmpHTLong.m_Val > 1 + 4 ) //如果该输出帧为有语音活动。
                                {
                                    synchronized( m_RecvOutputFrameLnkLstPt )
                                    {
                                        m_RecvOutputFrameLnkLstPt.addLast( Arrays.copyOfRange( m_TmpBytePt, 1 + 4, ( int ) ( p_TmpHTLong.m_Val ) ) );
                                    }
                                }

                                Log.i( m_CurClsNameStrPt, "接收到一个输出帧包，并放入链表成功。时间戳：" + m_RecvOutputFrameTimeStamp + "，总长度：" + p_TmpHTLong.m_Val + "。" );

                                break;
                            }
                            case 1: //如果使用自适应抖动缓冲器。
                            {
                                if( p_TmpHTLong.m_Val == 1 + 4 ) //如果该输出帧为无语音活动。
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
                                        m_AjbPt.PutOneByteFrame( m_RecvOutputFrameTimeStamp, Arrays.copyOfRange( m_TmpBytePt, 1 + 4, ( int ) ( p_TmpHTLong.m_Val ) ), p_TmpHTLong.m_Val - 1 - 4 );
                                    }
                                }

                                Log.i( m_CurClsNameStrPt, "接收一个到输出帧包，并放入自适应抖动缓冲器成功。时间戳：" + m_RecvOutputFrameTimeStamp + "，总长度：" + p_TmpHTLong.m_Val + "。" );
                                break;
                            }
                        }

                        if( ( m_UseWhatXfrPrtcl == 1 ) && ( p_TmpHTLong.m_Val == 1 + 4 ) ) //如果是使用UDP协议，且本输出帧为无语音活动。
                        {
                            //设置输出帧应答包。
                            m_TmpBytePt[0] = 0x02;
                            //设置时间戳。
                            m_TmpBytePt[1] = ( byte ) ( m_RecvOutputFrameTimeStamp & 0xFF );
                            m_TmpBytePt[2] = ( byte ) ( ( m_RecvOutputFrameTimeStamp & 0xFF00 ) >> 8 );
                            m_TmpBytePt[3] = ( byte ) ( ( m_RecvOutputFrameTimeStamp & 0xFF0000 ) >> 16 );
                            m_TmpBytePt[4] = ( byte ) ( ( m_RecvOutputFrameTimeStamp & 0xFF000000 ) >> 24 );

                            if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1 + 4, ( short ) 0, m_ErrInfoVarStrPt ) == 0 )
                            {
                                m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。
                                Log.i( m_CurClsNameStrPt, "发送一个输出帧应答包成功。时间戳：" + m_RecvOutputFrameTimeStamp + "，总长度：" + 1 + 4 + "。" );
                            }
                            else
                            {
                                String p_InfoStrPt = "发送一个输出帧应答包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break out;
                            }
                        }
                    }
                    else if( m_TmpBytePt[0] == 0x02 ) //如果是连接应答包或输入输出帧应答包。
                    {
                        if( p_TmpHTLong.m_Val == 1 ) //如果退出包的数据长度等于1，表示是连接应答包，就不管。
                        {

                        }
                        else //如果退出包的数据长度大于1，表示是输入输出帧应答包。
                        {
                            if( p_TmpHTLong.m_Val != 1 + 4 )
                            {
                                Log.e( m_CurClsNameStrPt, "接收到输入输出帧应答包的数据长度为" + p_TmpHTLong.m_Val + "不等于1 + 4，表示格式不正确，无法继续接收。" );
                                break out;
                            }

                            //读取时间戳。
                            m_RecvOutputFrameTimeStamp = ( m_TmpBytePt[1] & 0xFF ) + ( ( m_TmpBytePt[2] & 0xFF ) << 8 ) + ( ( m_TmpBytePt[3] & 0xFF ) << 16 ) + ( ( m_TmpBytePt[4] & 0xFF ) << 24 );

                            Log.i( m_CurClsNameStrPt, "接收到一个输入输出帧应答包。时间戳：" + m_RecvOutputFrameTimeStamp + "，总长度：" + p_TmpHTLong.m_Val + "。" );

                            //设置最后一个发送的输入帧远端是否接收到。
                            if( m_SendInputFrameTimeStamp == m_RecvOutputFrameTimeStamp ) m_LastSendInputFrameIsRecv = 1;
                        }
                    }
                    else if( m_TmpBytePt[0] == 0x03 ) //如果是退出包。
                    {
                        if( p_TmpHTLong.m_Val > 1 ) //如果退出包的数据长度大于1。
                        {
                            Log.e( m_CurClsNameStrPt, "接收到退出包的数据长度为" + p_TmpHTLong.m_Val + "大于1，表示还有其他数据，无法继续接收。" );
                            break out;
                        }

                        m_IsRecvExitPkt = 1; //设置已经接收到退出包。
                        RequireExit( 1, 0 ); //请求退出。

                        String p_InfoStrPt = "接收到一个退出包。";
                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    }
                }
                else //如果用已连接的本端TCP协议客户端套接字开始接收连接的远端TCP协议客户端套接字发送的一个数据包超时。
                {

                }
            }
            else //如果用已连接的本端TCP协议客户端套接字开始接收连接的远端TCP协议客户端套接字发送的一个数据包失败。
            {
                String p_InfoStrPt = "用已连接的本端TCP协议客户端套接字开始接收连接的远端TCP协议客户端套接字发送的一个数据包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                break out;
            }

            //发送心跳包。
            if( System.currentTimeMillis() - m_LastPktSendTime >= 100 ) //如果超过100毫秒没有发送任何数据包，就发送一个心跳包。
            {
                m_TmpBytePt[0] = 0x00; //设置心跳包。
                if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, 1, ( short ) 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
                    ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, m_ErrInfoVarStrPt ) == 0 ) ) )
                {
                    m_LastPktSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间。
                    Log.i( m_CurClsNameStrPt, "发送一个心跳包成功。" );
                }
                else
                {
                    String p_InfoStrPt = "发送一个心跳包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                    Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break out;
                }
            }

            //判断套接字连接是否中断。
            if( System.currentTimeMillis() - m_LastPktRecvTime > 2000 ) //如果超过2000毫秒没有接收任何数据包，就判定连接已经断开了。
            {
                String p_InfoStrPt = "超过2000毫秒没有接收任何数据包，判定套接字连接已经断开了。";
                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                break out;
            }

            p_Result = 0; //设置本函数执行成功。
        }

        return p_Result;
    }

    //用户定义的销毁函数，在本线程退出时调用一次。
    public void UserDestroy()
    {
        SendExitPkt:
        if( ( m_ExitFlag == 1 ) && ( ( m_TcpClntSoktPt != null ) || ( ( m_UdpSoktPt != null ) && ( m_UdpSoktPt.GetRmtAddr( null, null, null, null ) == 0 ) ) ) ) //如果本线程接收到退出请求，且本端TCP协议客户端套接字类对象不为空或本端UDP协议套接字类对象不为空且已连接远端。
        {
            //循环发送退出包。
            m_TmpBytePt[0] = 0x03; //设置退出包。
            for( int p_SendTimes = ( m_UseWhatXfrPrtcl == 0 ) ? 1 : 5; p_SendTimes > 0; p_SendTimes-- )
            {
                if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, 1, ( short ) 0, m_ErrInfoVarStrPt ) != 0 ) ) ||
                    ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, m_ErrInfoVarStrPt ) != 0 ) ) )
                {
                    String p_InfoStrPt = "发送一个退出包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                    Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break SendExitPkt;
                }
            }

            m_LastPktSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间。

            {String p_InfoStrPt = "发送一个退出包成功。";
            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

            if( m_IsRecvExitPkt == 0 ) //如果没有接收到退出包。
            {
                while( true ) //循环接收退出包。
                {
                    HTLong p_TmpHTLong = new HTLong(  );

                    if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.RecvPkt( m_TmpBytePt, m_TmpBytePt.length, p_TmpHTLong, ( short ) 5000, m_ErrInfoVarStrPt ) == 0 ) ) ||
                        ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, p_TmpHTLong, ( short ) 5000, m_ErrInfoVarStrPt ) == 0 ) ) )
                    {
                        if( p_TmpHTLong.m_Val != -1 ) //如果用已连接的本端套接字开始接收连接的远端套接字发送的一个数据包成功。
                        {
                            m_LastPktRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。

                            if( ( p_TmpHTLong.m_Val == 1 ) && ( m_TmpBytePt[0] == 0x03 ) ) //如果是退出包。
                            {
                                String p_InfoStrPt = "接收到一个退出包。";
                                Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break SendExitPkt;
                            }
                            else //如果是其他包，继续接收。
                            {

                            }
                        }
                        else //如果用已连接的本端套接字开始接收连接的远端套接字发送的一个数据包超时。
                        {
                            String p_InfoStrPt = "用已连接的本端套接字开始接收连接的远端套接字发送的一个数据包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                            Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break SendExitPkt;
                        }
                    }
                    else //如果用已连接的本端套接字开始接收连接的远端套接字发送的一个数据包失败。
                    {
                        String p_InfoStrPt = "用已连接的本端套接字开始接收连接的远端套接字发送的一个数据包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break SendExitPkt;
                    }
                }
            }
        }

        //销毁本端TCP协议服务端套接字。
        if( m_TcpSrvrSoktPt != null )
        {
            m_TcpSrvrSoktPt.Destroy( null ); //关闭并销毁已创建的本端TCP协议服务端套接字。
            m_TcpSrvrSoktPt = null;

            String p_InfoStrPt = "关闭并销毁已创建的本端TCP协议服务端套接字成功。";
            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
        }

        //销毁本端TCP协议客户端套接字。
        if( m_TcpClntSoktPt != null )
        {
            m_TcpClntSoktPt.Destroy( ( short ) -1, null ); //关闭并销毁已创建的本端TCP协议客户端套接字。
            m_TcpClntSoktPt = null;

            String p_InfoStrPt = "关闭并销毁已创建的本端TCP协议客户端套接字成功。";
            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
        }

        //销毁本端UDP协议套接字。
        if( m_UdpSoktPt != null )
        {
            m_UdpSoktPt.Destroy( null ); //关闭并销毁已创建的本端UDP协议套接字。
            m_UdpSoktPt = null;

            String p_InfoStrPt = "关闭并销毁已创建的本端UDP协议套接字成功。";
            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
        }

        //销毁接收音频输出帧的链表类对象。
        if( m_RecvOutputFrameLnkLstPt != null )
        {
            m_RecvOutputFrameLnkLstPt.clear();
            m_RecvOutputFrameLnkLstPt = null;

            Log.i( m_CurClsNameStrPt, "销毁接收输出帧链表类对象成功。" );
        }

        //销毁自适应抖动缓冲器类对象。
        if( m_AjbPt != null )
        {
            m_AjbPt.Destroy();
            m_AjbPt = null;

            Log.i( m_CurClsNameStrPt, "销毁自适应抖动缓冲器类对象成功。" );
        }

        if( m_IsCreateSrvrOrClnt == 1 ) //如果是创建服务端。
        {
            if( ( m_ExitFlag == 1 ) && ( m_IsRecvExitPkt == 1 ) ) //如果本线程接收到退出请求，且接收到了退出包。
            {
                String p_InfoStrPt = "由于是创建服务端，且本线程接收到退出请求，且接收到了退出包，表示是远端TCP协议客户端套接字主动退出，本线程重新初始化来继续保持监听。";
                Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );

                RequireExit( 2, 0 ); //请求重启。
            }
            else if( ( m_ExitFlag == 0 ) && ( m_ExitCode == -2 ) ) //如果本线程没收到退出请求，且退出代码为处理失败。
            {
                String p_InfoStrPt = "由于是创建服务端，且本线程没收到退出请求，且退出码为处理失败，表示是处理失败或连接异常断开，本线程重新初始化来继续保持监听。";
                Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );

                RequireExit( 2, 0 ); //请求重启。
            }
            else //其他情况，本线程直接退出。
            {
                Message clMessage = new Message();clMessage.what = 2;m_MainActivityHandlerPt.sendMessage( clMessage ); //向主界面发送音频处理线程退出的消息。
            }
        }
        else if( m_IsCreateSrvrOrClnt == 0 ) //如果是创建客户端。
        {
            if( ( m_ExitFlag == 0 ) && ( m_ExitCode == -2 ) ) //如果本线程没收到退出请求，且退出代码为处理失败。
            {
                String p_InfoStrPt = "由于是创建客户端，且本线程没收到退出请求，且退出码为处理失败，表示是处理失败或连接异常断开，本线程重新初始化来重连服务端。";
                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );

                RequireExit( 2, 0 ); //请求重启。
            }
            else //其他情况，本线程直接退出。
            {
                Message clMessage = new Message();clMessage.what = 2;m_MainActivityHandlerPt.sendMessage( clMessage ); //向主界面发送音频处理线程退出的消息。
            }
        }
    }

    //用户定义的读取输入帧函数，在读取到一个输入帧并处理完后回调一次，为0表示成功，为非0表示失败。
    public int UserReadInputFrame( short PcmInputFramePt[], short PcmResultFramePt[], int VoiceActSts, byte SpeexInputFramePt[], HTLong SpeexInputFrameLen, HTInt SpeexInputFrameIsNeedTrans )
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
        int p_TmpInt32 = 0;

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
                            for( p_TmpInt32 = 0; p_TmpInt32 < PcmResultFramePt.length; p_TmpInt32++ )
                            {
                                m_TmpBytePt[1 + 4 + p_TmpInt32 * 2] = ( byte ) ( PcmResultFramePt[p_TmpInt32] & 0xFF );
                                m_TmpBytePt[1 + 4 + p_TmpInt32 * 2 + 1] = ( byte ) ( ( PcmResultFramePt[p_TmpInt32] & 0xFF00 ) >> 8 );
                            }

                            p_TmpInt32 = 1 + 4 + PcmResultFramePt.length * 2; //数据包长度 = 格式标记 + 时间戳长度 + PCM格式输入帧长度。

                            break;
                        }
                        case 1: //如果使用Speex编解码器。
                        {
                            if( SpeexInputFrameIsNeedTrans.m_Val == 1 ) //如果本Speex格式音频输入数据帧需要传输。
                            {
                                System.arraycopy( SpeexInputFramePt, 0, m_TmpBytePt, 1 + 4, ( int ) SpeexInputFrameLen.m_Val );

                                p_TmpInt32 = 1 + 4 + ( int ) SpeexInputFrameLen.m_Val; //数据包长度 = 格式标记 + 时间戳长度 + Speex格式输入帧长度。
                            }
                            else //如果本Speex格式音频输入数据帧不需要传输。
                            {
                                p_TmpInt32 = 1 + 4; //数据包长度 = 格式标记 + 时间戳长度。
                            }

                            break;
                        }
                    }
                }
                else //如果本音频输入数据帧为无语音活动。
                {
                    p_TmpInt32 = 1 + 4; //数据包长度 = 格式标记 + 时间戳长度。
                }

                if( ( p_TmpInt32 != 1 + 4 ) || //如果本音频输入数据帧为有语音活动，就发送。
                    ( ( p_TmpInt32 == 1 + 4 ) && ( m_LastSendInputFrameIsAct != 0 ) ) ) //如果本音频输入数据帧为无语音活动，但最后一个发送的输入帧为有语音活动，就发送。
                {
                    m_SendInputFrameTimeStamp += m_FrameLen; //时间戳递增一个帧的数据长度。

                    //设置输入帧包。
                    m_TmpBytePt[0] = 0x01;
                    //设置时间戳。
                    m_TmpBytePt[1] = ( byte ) ( m_SendInputFrameTimeStamp & 0xFF );
                    m_TmpBytePt[2] = ( byte ) ( ( m_SendInputFrameTimeStamp & 0xFF00 ) >> 8 );
                    m_TmpBytePt[3] = ( byte ) ( ( m_SendInputFrameTimeStamp & 0xFF0000 ) >> 16 );
                    m_TmpBytePt[4] = ( byte ) ( ( m_SendInputFrameTimeStamp & 0xFF000000 ) >> 24 );

                    if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, p_TmpInt32, ( short ) 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
                        ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, p_TmpInt32, ( short ) 0, m_ErrInfoVarStrPt ) == 0 ) ) )
                    {
                        m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。
                        Log.i( m_CurClsNameStrPt, "发送一个输入帧包成功。时间戳：" + m_SendInputFrameTimeStamp + "，总长度：" + p_TmpInt32 + "。" );
                    }
                    else
                    {
                        String p_InfoStrPt = "发送一个输入帧包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    if( p_TmpInt32 != 1 + 4 ) //如果本音频输入数据帧为有语音活动。
                    {
                        m_LastSendInputFrameIsAct = 1; //设置最后一个发送的输入帧有语音活动。
                        m_LastSendInputFrameIsRecv = 1; //设置最后一个发送的输入帧远端已经接收到。
                    }
                    else if( ( ( p_TmpInt32 == 1 + 4 ) && ( m_LastSendInputFrameIsAct != 0 ) ) ) //如果本音频输入数据帧为无语音活动，但最后一个发送的输入帧为有语音活动。
                    {
                        m_LastSendInputFrameIsAct = 0; //设置最后一个发送的输入帧无语音活动。
                        m_LastSendInputFrameIsRecv = 0; //设置最后一个发送的输入帧远端没有接收到。
                    }
                }
                else
                {
                    Log.i( m_CurClsNameStrPt, "本输入帧为无语音活动，且最后发送的一个输入帧为无语音活动，无需发送。" );

                    if( ( m_UseWhatXfrPrtcl == 1 ) && ( m_LastSendInputFrameIsAct == 0 ) && ( m_LastSendInputFrameIsRecv == 0 ) ) //如果是使用UDP协议，且最后一个发送的输入帧为无语音活动，且最后一个发送的输入帧远端没有接收到。
                    {
                        //设置输入帧包。
                        m_TmpBytePt[0] = 0x01;
                        //设置时间戳。
                        m_TmpBytePt[1] = ( byte ) ( m_SendInputFrameTimeStamp & 0xFF );
                        m_TmpBytePt[2] = ( byte ) ( ( m_SendInputFrameTimeStamp & 0xFF00 ) >> 8 );
                        m_TmpBytePt[3] = ( byte ) ( ( m_SendInputFrameTimeStamp & 0xFF0000 ) >> 16 );
                        m_TmpBytePt[4] = ( byte ) ( ( m_SendInputFrameTimeStamp & 0xFF000000 ) >> 24 );

                        if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1 + 4, ( short ) 0, m_ErrInfoVarStrPt ) == 0 )
                        {
                            m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。
                            Log.i( m_CurClsNameStrPt, "重新发送一个无语音活动输入帧包成功。时间戳：" + m_SendInputFrameTimeStamp + "，总长度：" + 1 + 4 + "。" );
                        }
                        else
                        {
                            String p_InfoStrPt = "重新发送一个无语音活动输入帧包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                            Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break out;
                        }
                    }
                }
            }

            p_Result = 0; //设置本函数执行成功。
        }

        return p_Result;
    }

    //用户定义的写入输出帧函数，在需要写入一个输出帧时回调一次。注意：本函数不是在音频处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致回音消除失败。
    public void UserWriteOutputFrame( short PcmOutputFramePt[], byte SpeexOutputFramePt[], HTLong SpeexOutputFrameLenPt )
    {
        int p_TmpInt32;

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
                            for( p_TmpInt32 = 0; p_TmpInt32 < m_FrameLen; p_TmpInt32++ )
                            {
                                PcmOutputFramePt[p_TmpInt32] = ( short ) ( ( p_TmpOutputFramePt[p_TmpInt32 * 2] & 0xFF ) | ( p_TmpOutputFramePt[p_TmpInt32 * 2 + 1] << 8 ) );
                            }

                            Log.i( m_CurClsNameStrPt, "从接收输出帧链表取出一个有语音活动的PCM格式输出帧，帧的数据长度：" + p_TmpOutputFramePt.length + "。" );
                        }
                        else //如果接收音频输出数据帧的链表为空，或第一个音频输出数据帧为无语音活动。
                        {
                            for( p_TmpInt32 = 0; p_TmpInt32 < m_FrameLen; p_TmpInt32++ )
                            {
                                PcmOutputFramePt[p_TmpInt32] = 0;
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
                            Arrays.fill( PcmOutputFramePt, ( short ) 0 );

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
                Log.i( m_CurClsNameStrPt, "自适应抖动缓冲器中最大需缓冲帧的数量为 " + p_AjbFrameCnt.m_Val + " 个。" );
                m_AjbPt.GetMinNeedBufFrameCnt( p_AjbFrameCnt );
                Log.i( m_CurClsNameStrPt, "自适应抖动缓冲器中最小需缓冲帧的数量为 " + p_AjbFrameCnt.m_Val + " 个。" );
                m_AjbPt.GetCurNeedBufFrameCnt( p_AjbFrameCnt );
                Log.i( m_CurClsNameStrPt, "自适应抖动缓冲器中当前需缓冲帧的数量为 " + p_AjbFrameCnt.m_Val + " 个。" );

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

    String m_ExternalDirFullAbsPathStrPt; //存放扩展目录完整绝对路径字符串的内存指针。

    int m_IsUseWakeLock; //存放是否使用唤醒锁，非0表示要使用，0表示不使用。
    PowerManager.WakeLock m_ProximityScreenOffWakeLockPt; //存放接近息屏唤醒锁类对象的内存指针。
    PowerManager.WakeLock m_FullWakeLockPt; //存放屏幕键盘全亮唤醒锁类对象的内存指针。

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

        //检测并请求录音权限。
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions( this, new String[] {Manifest.permission.RECORD_AUDIO}, 1 );

        //检测并请求修改音频设置权限。
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.MODIFY_AUDIO_SETTINGS ) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions( this, new String[] {Manifest.permission.MODIFY_AUDIO_SETTINGS}, 1 );

        //检测并请求网络权限。
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.INTERNET ) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions( this, new String[] {Manifest.permission.INTERNET}, 1 );

        //检测并请求唤醒锁权限。
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.WAKE_LOCK ) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions( this, new String[] {Manifest.permission.WAKE_LOCK}, 1 );

        //设置主界面类对象。
        m_MainActivityPt = this;

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

        //获取扩展目录完整绝对路径字符串。
        if( getExternalFilesDir( null ) != null )
        {
            m_ExternalDirFullAbsPathStrPt = getExternalFilesDir( null ).getPath();
        }
        else
        {
            m_ExternalDirFullAbsPathStrPt = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getApplicationContext().getPackageName();
        }

        String p_InfoStrPt = "扩展目录完整绝对路径：" + m_ExternalDirFullAbsPathStrPt;
        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
    }

    //返回键。
    @Override
    public void onBackPressed()
    {
        if( m_LyotActivityCurViewPt == m_LyotActivityMainViewPt )
        {
            Log.i( m_CurClsNameStrPt, "用户在主界面按下返回键，本软件退出。" );
            if( m_MyAudioProcThreadPt != null )
            {
                Log.i( m_CurClsNameStrPt, "开始请求并等待音频处理线程退出。" );
                m_MyAudioProcThreadPt.RequireExit( 1, 1 );
                Log.i( m_CurClsNameStrPt, "结束请求并等待音频处理线程退出。" );
            }
            System.exit(0);
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

    //使用扬声器按钮。
    public void OnUseSpeaker( View BtnPt )
    {
        if( m_MyAudioProcThreadPt != null )
        {
            m_MyAudioProcThreadPt.SetUseDevice( 0, 0 );

            if( m_MyAudioProcThreadPt.m_InputFrameLnkLstPt != null ) //如果音频处理线程已经启动。
            {
                m_MyAudioProcThreadPt.RequireExit( 3, 1 ); //请求重启并阻塞等待。
            }
        }

        SetUseWakeLock( m_IsUseWakeLock );
    }

    //使用听筒按钮。
    public void OnUseHeadset( View BtnPt )
    {
        if( m_MyAudioProcThreadPt != null )
        {
            m_MyAudioProcThreadPt.SetUseDevice( 1, 0 );

            if( m_MyAudioProcThreadPt.m_InputFrameLnkLstPt != null ) //如果音频处理线程已经启动。
            {
                m_MyAudioProcThreadPt.RequireExit( 3, 1 ); //请求重启并阻塞等待。
            }
        }
        SetUseWakeLock( m_IsUseWakeLock );
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

                //创建并初始化音频处理线程类对象。
                {
                    m_MyAudioProcThreadPt = new MyAudioProcThread();

                    if( BtnPt.getId() == R.id.CreateSrvrBtn )
                    {
                        m_MyAudioProcThreadPt.m_IsCreateSrvrOrClnt = 1; //标记创建服务端接受客户端。
                    }
                    else if( BtnPt.getId() == R.id.ConnectSrvrBtn )
                    {
                        m_MyAudioProcThreadPt.m_IsCreateSrvrOrClnt = 0; //标记创建客户端连接服务端。
                    }

                    m_MyAudioProcThreadPt.m_MainActivityHandlerPt = m_MainActivityHandlerPt; //设置主界面消息处理类对象的内存指针。

                    //设置IP地址字符串、端口。
                    m_MyAudioProcThreadPt.m_IPAddrStrPt = ( ( EditText ) m_LyotActivityMainViewPt.findViewById( R.id.IPAddrEdit ) ).getText().toString();
                    m_MyAudioProcThreadPt.m_PortStrPt = ( ( EditText ) m_LyotActivityMainViewPt.findViewById( R.id.PortEdit ) ).getText().toString();

                    //初始化音频处理线程类对象。
                    m_MyAudioProcThreadPt.Init(
                            getApplicationContext(),
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSamplingRate8000RadioBtn ) ).isChecked() ) ? 8000 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSamplingRate16000RadioBtn ) ).isChecked() ) ? 16000 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSamplingRate32000RadioBtn ) ).isChecked() ) ? 32000 : 0,
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseFrame10msLenRadioBtn ) ).isChecked() ) ? 10 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseFrame20msLenRadioBtn ) ).isChecked() ) ? 20 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseFrame30msLenRadioBtn ) ).isChecked() ) ? 30 : 0 );

                    //判断是否使用什么传输协议。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseTcpPrtclRadioBtn ) ).isChecked() )
                    {
                        m_MyAudioProcThreadPt.m_UseWhatXfrPrtcl = 0;
                    }
                    else
                    {
                        m_MyAudioProcThreadPt.m_UseWhatXfrPrtcl = 1;
                    }

                    //判断是否保存设置到文件。
                    if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveSettingToFileCheckBox ) ).isChecked() )
                    {
                        m_MyAudioProcThreadPt.SetSaveSettingToFile( 1, m_ExternalDirFullAbsPathStrPt + "/Setting.txt" );
                    }
                    else
                    {
                        m_MyAudioProcThreadPt.SetSaveSettingToFile( 0, null );
                    }

                    //判断是否打印Logcat日志。
                    if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsPrintLogcatCheckBox ) ).isChecked() )
                    {
                        m_MyAudioProcThreadPt.SetPrintLogcat( 1 );
                    }
                    else
                    {
                        m_MyAudioProcThreadPt.SetPrintLogcat( 0 );
                    }

                    //判断使用的音频输出设备。
                    if( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseSpeakerRadioBtn ) ).isChecked() )
                    {
                        m_MyAudioProcThreadPt.SetUseDevice( 0, 0 );
                    }
                    else
                    {
                        m_MyAudioProcThreadPt.SetUseDevice( 1, 0 );
                    }

                    //判断是否使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器。
                    if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSystemAecNsAgcCheckBox ) ).isChecked() )
                    {
                        m_MyAudioProcThreadPt.SetUseSystemAecNsAgc( 1 );
                    }
                    else
                    {
                        m_MyAudioProcThreadPt.SetUseSystemAecNsAgc( 0 );
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
                                    ( ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsUseRecCheckBox ) ).isChecked() ) ? 1 : 0,
                                    Float.parseFloat( ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoMultipleEdit ) ).getText().toString() ),
                                    Float.parseFloat( ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoContEdit ) ).getText().toString() ),
                                    Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesEdit ) ).getText().toString() ),
                                    Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesActEdit ) ).getText().toString() ),
                                    ( ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCheckBox ) ).isChecked() ) ? 1 : 0,
                                    m_ExternalDirFullAbsPathStrPt + "/SpeexAecMemory"
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
                                    ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCheckBox ) ).isChecked() ) ? 1 : 0,
                                    ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).isChecked() ) ? 1 : 0,
                                    ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCheckBox ) ).isChecked() ) ? 1 : 0,
                                    ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCheckBox ) ).isChecked() ) ? 1 : 0,
                                    m_ExternalDirFullAbsPathStrPt + "/WebRtcAecMemory"
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
                                    ( ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCheckBox ) ).isChecked() ) ? 1 : 0,
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
                                    ( ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCheckBox ) ).isChecked() ) ? 1 : 0,
                                    ( ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).isChecked() ) ? 1 : 0,
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
                                    ( ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseDereverbCheckBox ) ).isChecked() ) ? 1 : 0
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
                                m_ExternalDirFullAbsPathStrPt + "/AudioInput.wav",
                                m_ExternalDirFullAbsPathStrPt + "/AudioOutput.wav",
                                m_ExternalDirFullAbsPathStrPt + "/AudioResult.wav"
                        );
                    }
                }

                m_MyAudioProcThreadPt.start(); //启动音频处理线程。

                Log.i( m_CurClsNameStrPt, "启动音频处理线程完毕。" );
            }
            else
            {
                Log.i( m_CurClsNameStrPt, "开始请求并等待音频处理线程退出。" );
                m_MyAudioProcThreadPt.RequireExit( 1, 1 );
                Log.i( m_CurClsNameStrPt, "结束请求并等待音频处理线程退出。" );
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
        ( ( LinearLayout ) m_LyotActivityMainViewPt.findViewById( R.id.LogLinearLyot ) ).removeAllViews();
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
        String p_pclSpeexAecMemoryFullPath = m_ExternalDirFullAbsPathStrPt + "/SpeexAecMemory";
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
        String p_pclWebRtcAecMemoryFullPath = m_ExternalDirFullAbsPathStrPt + "/WebRtcAecMemory";
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

    //设置使用唤醒锁。
    public void SetUseWakeLock( int IsUseWakeLock )
    {
        m_IsUseWakeLock = IsUseWakeLock;

        if( m_IsUseWakeLock != 0 ) //如果要使用唤醒锁。
        {
            if( m_MyAudioProcThreadPt != null )
            {
                if( m_MyAudioProcThreadPt.m_UseWhatAudioOutputDevice == 0 ) //如果使用扬声器音频输出设备。
                {
                    if( m_ProximityScreenOffWakeLockPt != null )
                    {
                        try
                        {
                            m_ProximityScreenOffWakeLockPt.release();
                        }
                        catch( RuntimeException e )
                        {
                        }
                        m_ProximityScreenOffWakeLockPt = null;
                        Log.i( m_CurClsNameStrPt, "销毁接近息屏唤醒锁类对象成功。" );
                    }
                }
                else //如果使用听筒音频输出设备。
                {
                    if( m_ProximityScreenOffWakeLockPt == null )
                    {
                        m_ProximityScreenOffWakeLockPt = ( ( PowerManager ) getApplicationContext().getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
                        if( m_ProximityScreenOffWakeLockPt != null )
                        {
                            m_ProximityScreenOffWakeLockPt.acquire();
                            Log.i( m_CurClsNameStrPt, "创建并初始化接近息屏唤醒锁类对象成功。" );
                        }
                        else
                        {
                            Log.e( m_CurClsNameStrPt, "创建并初始化接近息屏唤醒锁类对象失败。" );
                        }
                    }
                }
                if( m_FullWakeLockPt == null )
                {
                    m_FullWakeLockPt = ( ( PowerManager ) getApplicationContext().getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
                    if( m_FullWakeLockPt != null )
                    {
                        m_FullWakeLockPt.acquire();
                        Log.i( m_CurClsNameStrPt, "创建并初始化屏幕键盘全亮唤醒锁类对象成功。" );
                    }
                    else
                    {
                        Log.e( m_CurClsNameStrPt, "创建并初始化屏幕键盘全亮唤醒锁类对象失败。" );
                    }
                }
            }
        }
        else //如果不使用唤醒锁。
        {
            if( m_ProximityScreenOffWakeLockPt != null )
            {
                try
                {
                    m_ProximityScreenOffWakeLockPt.release();
                }
                catch( RuntimeException e )
                {
                }
                m_ProximityScreenOffWakeLockPt = null;
                Log.i( m_CurClsNameStrPt, "销毁接近息屏唤醒锁类对象成功。" );
            }
            if( m_FullWakeLockPt != null )
            {
                try
                {
                    m_FullWakeLockPt.release();
                }
                catch( RuntimeException e )
                {
                }
                m_FullWakeLockPt = null;
                Log.i( m_CurClsNameStrPt, "销毁屏幕键盘全亮唤醒锁类对象成功。" );
            }
        }
    }
}
