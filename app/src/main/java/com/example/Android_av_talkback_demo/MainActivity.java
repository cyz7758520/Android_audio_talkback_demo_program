package com.example.Android_av_talkback_demo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
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
import HeavenTao.Video.*;
import HeavenTao.Media.*;
import HeavenTao.Data.*;
import HeavenTao.Sokt.*;

//主界面消息处理类。
class MainActivityHandler extends Handler
{
    String m_CurClsNameStrPt = this.getClass().getSimpleName(); //当前类名称字符串类对象的内存指针。

    MainActivity m_MainActivityPt; //存放主界面类对象的内存指针。
    ServiceConnection m_FrgndSrvcCnctPt; //存放前台服务连接器类对象的内存指针。

    public void handleMessage( Message MessagePt )
    {
        if( MessagePt.what == 1 ) //如果是媒体处理线程启动的消息。
        {
            if( m_MainActivityPt.m_MyMediaProcThreadPt.m_IsCreateSrvrOrClnt == 1 ) //如果是创建服务端。
            {
                ( ( EditText ) m_MainActivityPt.findViewById( R.id.IPAddrEdit ) ).setEnabled( false ); //设置IP地址控件为不可用。
                ( ( EditText ) m_MainActivityPt.findViewById( R.id.PortEdit ) ).setEnabled( false ); //设置端口控件为不可用。
                ( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseTcpPrtclRadioBtn ) ).setEnabled( false ); //设置TCP协议按钮为不可用。
                ( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseUdpPrtclRadioBtn ) ).setEnabled( false ); //设置UDP协议按钮为不可用。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtn ) ).setText( "中断" ); //设置创建服务端按钮的内容为“中断”。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.ConnectSrvrBtn ) ).setEnabled( false ); //设置连接服务端按钮为不可用。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.SettingBtn ) ).setEnabled( false ); //设置设置按钮为不可用。
            }
            else //如果是创建客户端。
            {
                ( ( EditText ) m_MainActivityPt.findViewById( R.id.IPAddrEdit ) ).setEnabled( false ); //设置IP地址控件为不可用。
                ( ( EditText ) m_MainActivityPt.findViewById( R.id.PortEdit ) ).setEnabled( false ); //设置端口控件为不可用。
                ( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseTcpPrtclRadioBtn ) ).setEnabled( false ); //设置TCP协议按钮为不可用。
                ( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseUdpPrtclRadioBtn ) ).setEnabled( false ); //设置UDP协议按钮为不可用。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtn ) ).setEnabled( false ); //设置创建服务端按钮为不可用。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.ConnectSrvrBtn ) ).setText( "中断" ); //设置连接服务端按钮的内容为“中断”。
                ( ( Button ) m_MainActivityPt.findViewById( R.id.SettingBtn ) ).setEnabled( false ); //设置设置按钮为不可用。
            }

            //创建并绑定前台服务，从而确保本进程在转入后台或系统锁屏时不会被系统限制运行，且只能放在主线程中执行，因为要使用界面类对象。
            if( ( ( CheckBox ) m_MainActivityPt.m_LyotActivitySettingViewPt.findViewById( R.id.IsUseFrgndSrvcCheckBox ) ).isChecked() && m_FrgndSrvcCnctPt == null )
            {
                m_FrgndSrvcCnctPt = new ServiceConnection() //创建存放前台服务连接器。
                {
                    @Override public void onServiceConnected( ComponentName name, IBinder service ) //前台服务绑定成功。
                    {
                        ( ( FrgndSrvc.FrgndSrvcBinder ) service ).SetForeground( m_MainActivityPt ); //将服务设置为前台服务。
                    }

                    @Override public void onServiceDisconnected( ComponentName name ) //前台服务解除绑定。
                    {

                    }
                };
                m_MainActivityPt.bindService( new Intent( m_MainActivityPt, FrgndSrvc.class ), m_FrgndSrvcCnctPt, Context.BIND_AUTO_CREATE ); //创建并绑定前台服务。
            }
        }
        else if( MessagePt.what == 2 ) //如果是媒体处理线程退出的消息。
        {
            m_MainActivityPt.m_MyMediaProcThreadPt = null;

            if( m_FrgndSrvcCnctPt != null ) //如果已经创建并绑定了前台服务。
            {
                m_MainActivityPt.unbindService( m_FrgndSrvcCnctPt ); //解除绑定并销毁前台服务。
                m_FrgndSrvcCnctPt = null;
            }

            ( ( EditText ) m_MainActivityPt.findViewById( R.id.IPAddrEdit ) ).setEnabled( true ); //设置IP地址控件为可用。
            ( ( EditText ) m_MainActivityPt.findViewById( R.id.PortEdit ) ).setEnabled( true ); //设置端口控件为可用。
            ( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseTcpPrtclRadioBtn ) ).setEnabled( true ); //设置TCP协议按钮为可用。
            ( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseUdpPrtclRadioBtn ) ).setEnabled( true ); //设置UDP协议按钮为可用。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtn ) ).setText( "创建服务端" ); //设置创建服务端按钮的内容为“创建服务端”。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.ConnectSrvrBtn ) ).setEnabled( true ); //设置连接服务端按钮为可用。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.ConnectSrvrBtn ) ).setText( "连接服务端" ); //设置连接服务端按钮的内容为“连接服务端”。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtn ) ).setEnabled( true ); //设置创建服务端按钮为可用。
            ( ( Button ) m_MainActivityPt.findViewById( R.id.SettingBtn ) ).setEnabled( true ); //设置设置按钮为可用。
        }
        else if( MessagePt.what == 3 ) //如果是显示日志的消息。
        {
            TextView p_LogTextView = new TextView( m_MainActivityPt );
            p_LogTextView.setText( ( new SimpleDateFormat( "HH:mm:ss SSS" ) ).format( new Date() ) + "：" + MessagePt.obj );
            ( ( LinearLayout ) m_MainActivityPt.m_LyotActivityMainViewPt.findViewById( R.id.LogLinearLyot ) ).addView( p_LogTextView );
        }
        else if( MessagePt.what == 4 ) //如果是重建SurfaceView控件消息，用来清空残余画面。
        {
            m_MainActivityPt.m_VideoInputPreviewSurfaceViewPt.setVisibility( View.GONE ); //销毁视频输入预览SurfaceView控件。
            m_MainActivityPt.m_VideoInputPreviewSurfaceViewPt.setVisibility( View.VISIBLE ); //创建视频输入预览SurfaceView控件。
            m_MainActivityPt.m_VideoOutputDisplaySurfaceViewPt.setVisibility( View.GONE ); //销毁视频输出显示SurfaceView控件。
            m_MainActivityPt.m_VideoOutputDisplaySurfaceViewPt.setVisibility( View.VISIBLE ); //创建视频输出显示SurfaceView控件。
        }
    }
}

//我的媒体处理线程类。
class MyMediaProcThread extends MediaProcThread
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
    public static final byte PKT_TYP_CNCT_HTBT = 0x00; //数据包类型：连接请求包或心跳包。
    public static final byte PKT_TYP_AFRAME = 0x01; //数据包类型：音频输入输出帧。
    public static final byte PKT_TYP_VFRAME = 0x02; //数据包类型：视频输入输出帧。
    public static final byte PKT_TYP_ACK = 0x03; //数据包类型：连接应答包或音视频输入输出帧应答包。
    public static final byte PKT_TYP_EXIT = 0x04; //数据包类型：退出包。

    int m_LastSendAudioInputFrameIsAct; //存放最后一个发送的音频输入帧有无语音活动，为1表示有语音活动，为0表示无语音活动。
    int m_LastSendAudioInputFrameIsRecv; //存放最后一个发送的音频输入帧远端是否接收到，为0表示没有收到，为非0表示已经收到。
    int m_LastSendAudioInputFrameTimeStamp; //存放最后一个发送音频输入帧的时间戳。
    int m_LastSendVideoInputFrameTimeStamp; //存放最后一个发送视频输入帧的时间戳。
    byte m_IsRecvExitPkt; //存放是否接收到退出包，为0表示否，为1表示是。

    int m_UseWhatRecvOutputFrame; //存放使用什么接收输出帧，为0表示链表，为1表示自适应抖动缓冲器。
    int m_LastGetAudioOutputFrameIsAct; //存放最后一个取出的音频输出帧是否为有语音活动，为0表示否，为非0表示是。
    int m_LastGetAudioOutputFrameVideoOutputFrameTimeStamp; //存放最后一个取出的音频输出帧对应视频输出帧的时间戳。

    LinkedList< byte[] > m_RecvAudioOutputFrameLnkLstPt; //存放接收音频输出帧链表类对象的内存指针。
    LinkedList< byte[] > m_RecvVideoOutputFrameLnkLstPt; //存放接收视频输出帧链表类对象的内存指针。

    AAjb m_AAjbPt; //存放音频自适应抖动缓冲器类对象的内存指针。
    int m_AAjbMinNeedBufFrameCnt; //存放音频自适应抖动缓冲器的最小需缓冲帧数量，单位个，必须大于0。
    int m_AAjbMaxNeedBufFrameCnt; //存放音频自适应抖动缓冲器的最大需缓冲帧数量，单位个，必须大于最小需缓冲数据帧的数量。
    float m_AAjbAdaptSensitivity; //存放音频自适应抖动缓冲器的自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0.0,127.0]。
    VAjb m_VAjbPt; //存放视频自适应抖动缓冲器类对象的内存指针。
    int m_VAjbMinNeedBufFrameCnt; //存放视频自适应抖动缓冲器的最小需缓冲帧数量，单位个，必须大于0。
    int m_VAjbMaxNeedBufFrameCnt; //存放视频自适应抖动缓冲器的最大需缓冲帧数量，单位个，必须大于最小需缓冲数据帧的数量。
    float m_VAjbAdaptSensitivity; //存放视频自适应抖动缓冲器的自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0.0,127.0]。

    byte m_TmpBytePt[]; //存放临时数据。
    byte m_TmpByte2Pt[]; //存放临时数据。
    byte m_TmpByte3Pt[]; //存放临时数据。
    HTInt m_TmpHTIntPt; //存放临时数据。
    HTInt m_TmpHTInt2Pt; //存放临时数据。
    HTInt m_TmpHTInt3Pt; //存放临时数据。
    HTLong m_TmpHTLongPt; //存放临时数据。
    HTLong m_TmpHTLong2Pt; //存放临时数据。
    HTLong m_TmpHTLong3Pt; //存放临时数据。

    VarStr m_ErrInfoVarStrPt; //存放错误信息动态字符串类对象的内存指针，可以为NULL。

    MyMediaProcThread( Context AppContextPt )
    {
        super( AppContextPt );
    }

    //用户定义的初始化函数，在本线程刚启动时回调一次，返回值表示是否成功，为0表示成功，为非0表示失败。
    @Override public int UserInit()
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            {Message p_MessagePt = new Message();p_MessagePt.what = 1;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送媒体处理线程启动的消息。

            m_IsRecvExitPkt = 0; //设置没有接收到退出包。
            if( m_TmpBytePt == null ) m_TmpBytePt = new byte[1024 * 1024]; //初始化临时数据。
            if( m_TmpByte2Pt == null ) m_TmpByte2Pt = new byte[1024 * 1024]; //初始化临时数据。
            if( m_TmpByte3Pt == null ) m_TmpByte3Pt = new byte[1024 * 1024]; //初始化临时数据。
            if( m_TmpHTIntPt == null ) m_TmpHTIntPt = new HTInt(); //初始化临时数据。
            if( m_TmpHTInt2Pt == null ) m_TmpHTInt2Pt = new HTInt(); //初始化临时数据。
            if( m_TmpHTInt3Pt == null ) m_TmpHTInt3Pt = new HTInt(); //初始化临时数据。
            if( m_TmpHTLongPt == null ) m_TmpHTLongPt = new HTLong(); //初始化临时数据。
            if( m_TmpHTLong2Pt == null ) m_TmpHTLong2Pt = new HTLong(); //初始化临时数据。
            if( m_TmpHTLong3Pt == null ) m_TmpHTLong3Pt = new HTLong(); //初始化临时数据。
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

                        if( m_TcpSrvrSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 ) //如果获取已监听的本端TCP协议服务端套接字绑定的本地节点地址和端口失败。
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
                    HTString p_RmtNodeAddrPt = new HTString();
                    HTString p_RmtNodePortPt = new HTString();

                    while( true ) //循环接受远端TCP协议客户端套接字的连接。
                    {
                        if( m_TcpSrvrSoktPt.Accept( null, p_RmtNodeAddrPt, p_RmtNodePortPt, ( short ) 1, m_TcpClntSoktPt, 0, m_ErrInfoVarStrPt ) == 0 )
                        {
                            if( m_TcpClntSoktPt.m_TcpClntSoktPt != 0 ) //如果用已监听的本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接成功。
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
                    //Ping一下远程节点地址，这样可以快速获取ARP条目。
                    try
                    {
                        Runtime.getRuntime().exec( "ping -c 1 -w 1 " + m_IPAddrStrPt );
                    }
                    catch( Exception ignored )
                    {
                    }

                    m_TcpClntSoktPt = new TcpClntSokt();
                    int p_ReInitTimes = 1;
                    while( true ) //循环连接已监听的远端TCP协议服务端套接字。
                    {
                        if( m_TcpClntSoktPt.Init( 4, m_IPAddrStrPt, m_PortStrPt, null, null, ( short ) 5000, m_ErrInfoVarStrPt ) == 0 ) //如果创建并初始化本端TCP协议客户端套接字，并连接已监听的远端TCP协议服务端套接字成功。
                        {
                            HTString p_LclNodeAddrPt = new HTString();
                            HTString p_LclNodePortPt = new HTString();
                            HTString p_RmtNodeAddrPt = new HTString();
                            HTString p_RmtNodePortPt = new HTString();

                            if( m_TcpClntSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 )
                            {
                                String p_InfoStrPt = "获取已连接的本端TCP协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break out;
                            }
                            if( m_TcpClntSoktPt.GetRmtAddr( null, p_RmtNodeAddrPt, p_RmtNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 )
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

                if( m_TcpClntSoktPt.SetNoDelay( 1, 0, m_ErrInfoVarStrPt ) != 0 ) //如果设置已连接的本端TCP协议客户端套接字的Nagle延迟算法状态为禁用失败。
                {
                    String p_InfoStrPt = "设置已连接的本端TCP协议客户端套接字的Nagle延迟算法状态为禁用失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                    Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break out;
                }

                if( m_TcpClntSoktPt.SetSendBufSz( 128 * 1024, 0, m_ErrInfoVarStrPt ) != 0 )
                {
                    String p_InfoStrPt = "设置已连接的本端TCP协议客户端套接字的发送缓冲区内存大小失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                    Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break out;
                }

                if( m_TcpClntSoktPt.SetRecvBufSz( 128 * 1024, 0, m_ErrInfoVarStrPt ) != 0 )
                {
                    String p_InfoStrPt = "设置已连接的本端TCP协议客户端套接字的接收缓冲区内存大小失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                    Log.e( m_CurClsNameStrPt, p_InfoStrPt );
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

                        if( m_UdpSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 ) //如果获取已监听的本端UDP协议套接字绑定的本地节点地址和端口失败。
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

                    UdpSrvrReAccept:
                    while( true ) //循环接受远端UDP协议套接字的连接。
                    {
                        if( m_UdpSoktPt.RecvPkt( null, p_RmtNodeAddrPt, p_RmtNodePortPt, m_TmpBytePt, m_TmpBytePt.length, p_TmpHTLong, ( short ) 1, 0, m_ErrInfoVarStrPt ) == 0 )
                        {
                            if( p_TmpHTLong.m_Val != -1 ) //如果用已监听的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个数据包成功。
                            {
                                if( ( p_TmpHTLong.m_Val == 1 ) && ( m_TmpBytePt[0] == PKT_TYP_CNCT_HTBT ) ) //如果是连接请求包。
                                {
                                    m_UdpSoktPt.Connect( 4, p_RmtNodeAddrPt.m_Val, p_RmtNodePortPt.m_Val, 0, null ); //用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字，已连接的本端UDP协议套接字只能接收连接的远端UDP协议套接字发送的数据包。

                                    int p_ReSendTimes = 1;
                                    UdpSrvrReSend:
                                    while( true ) //循环发送连接请求包，并接收连接应答包。
                                    {
                                        m_TmpBytePt[0] = PKT_TYP_CNCT_HTBT; //设置连接请求包。
                                        if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 0, m_ErrInfoVarStrPt ) != 0 )
                                        {
                                            String p_InfoStrPt = "用已监听的本端UDP协议套接字接受远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]的连接失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                            break out;
                                        }

                                        UdpSrvrReRecv:
                                        while( true ) //循环接收连接应答包。
                                        {
                                            if( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, p_TmpHTLong, ( short ) 1000, 0, m_ErrInfoVarStrPt ) == 0 )
                                            {
                                                if( p_TmpHTLong.m_Val != -1 ) //如果用已监听的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个数据包成功。
                                                {
                                                    if( ( p_TmpHTLong.m_Val >= 1 ) && ( m_TmpBytePt[0] != PKT_TYP_CNCT_HTBT ) ) //如果不是连接请求包。
                                                    {
                                                        String p_InfoStrPt = "用已监听的本端UDP协议套接字接受远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]的连接成功。";
                                                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                                        break UdpSrvrReAccept; //跳出连接循环。
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
                                                        break UdpSrvrReRecv; //重发连接请求包。
                                                    }
                                                    else //如果不需要重连了。
                                                    {
                                                        String p_InfoStrPt = "用已监听的本端UDP协议套接字接受远端UDP协议套接字的连接失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                                        break UdpSrvrReSend; //重新接受连接。
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                String p_InfoStrPt = "用已监听的本端UDP协议套接字接受远端UDP协议套接字的连接失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                                break UdpSrvrReSend; //重新接受连接。
                                            }
                                        }
                                    }

                                    m_UdpSoktPt.Disconnect( 0, null ); //将已连接的本端UDP协议套接字断开连接的远端UDP协议套接字，已连接的本端UDP协议套接字将变成已监听的本端UDP协议套接字。

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
                    //Ping一下远程节点地址，这样可以快速获取ARP条目。
                    try
                    {
                        Runtime.getRuntime().exec( "ping -c 1 -w 1 " + m_IPAddrStrPt );
                    }
                    catch( Exception ignored )
                    {
                    }

                    if( m_UdpSoktPt.Init( 4, null, null, m_ErrInfoVarStrPt ) == 0 ) //如果创建并初始化已监听的本端UDP协议套接字成功。
                    {
                        HTString p_LclNodeAddrPt = new HTString();
                        HTString p_LclNodePortPt = new HTString();

                        if( m_UdpSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 ) //如果获取已监听的本端UDP协议套接字绑定的本地节点地址和端口失败。
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
                        String p_InfoStrPt = "创建并初始化已监听的本端UDP协议套接字失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    HTString p_RmtNodeAddrPt = new HTString();
                    HTString p_RmtNodePortPt = new HTString();
                    HTLong p_TmpHTLong = new HTLong(  );

                    if( m_UdpSoktPt.Connect( 4, m_IPAddrStrPt, m_PortStrPt, 0, m_ErrInfoVarStrPt ) != 0 )
                    {
                        String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字[" + m_IPAddrStrPt + ":" + m_PortStrPt + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    if( m_UdpSoktPt.GetRmtAddr( null, p_RmtNodeAddrPt, p_RmtNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 )
                    {
                        m_UdpSoktPt.Disconnect( 0, null );
                        String p_InfoStrPt = "获取已连接的本端UDP协议套接字连接的远端UDP协议套接字绑定的远程节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    int p_ReSendTimes = 1;
                    UdpClntReSend:
                    while( true ) //循环连接已监听的远端UDP协议套接字。
                    {
                        m_TmpBytePt[0] = PKT_TYP_CNCT_HTBT; //设置连接请求包。
                        if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 0, m_ErrInfoVarStrPt ) != 0 )
                        {
                            m_UdpSoktPt.Disconnect( 0, m_ErrInfoVarStrPt );
                            String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break out;
                        }

                        UdpClntReRecv:
                        while( true ) //循环接收连接请求包。
                        {
                            if( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, p_TmpHTLong, ( short ) 1000, 0, m_ErrInfoVarStrPt ) == 0 )
                            {
                                if( p_TmpHTLong.m_Val != -1 ) //如果用已连接的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个数据包成功。
                                {
                                    if( ( p_TmpHTLong.m_Val == 1 ) && ( m_TmpBytePt[0] == PKT_TYP_CNCT_HTBT ) ) //如果是连接请求包。
                                    {
                                        m_TmpBytePt[0] = PKT_TYP_ACK; //设置连接应答包。
                                        if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 0, m_ErrInfoVarStrPt ) != 0 )
                                        {
                                            m_UdpSoktPt.Disconnect( 0, m_ErrInfoVarStrPt );
                                            String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                            Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                            break out;
                                        }

                                        String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]成功。";
                                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                        break UdpClntReSend; //跳出连接循环。
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
                                        break UdpClntReRecv; //重发连接请求包。
                                    }
                                    else //如果不需要重连了。
                                    {
                                        m_UdpSoktPt.Disconnect( 0, m_ErrInfoVarStrPt );
                                        String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                        break out;
                                    }
                                }
                            }
                            else
                            {
                                m_UdpSoktPt.Disconnect( 0, m_ErrInfoVarStrPt );
                                String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break out;
                            }
                        }
                    }
                }

                if( m_UdpSoktPt.SetSendBufSz( 128 * 1024, 0, m_ErrInfoVarStrPt ) != 0 )
                {
                    String p_InfoStrPt = "设置已监听的本端UDP协议套接字的发送缓冲区内存大小失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                    Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break out;
                }

                if( m_UdpSoktPt.SetRecvBufSz( 128 * 1024, 0, m_ErrInfoVarStrPt ) != 0 )
                {
                    String p_InfoStrPt = "设置已监听的本端UDP协议套接字的接收缓冲区内存大小失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                    Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break out;
                }
            } //协议连接结束。

            switch( m_UseWhatRecvOutputFrame ) //使用什么接收输出帧。
            {
                case 0: //如果使用链表。
                {
                    //初始化接收音频输出帧链表类对象。
                    m_RecvAudioOutputFrameLnkLstPt = new LinkedList< byte[] >(); //创建接收音频输出帧链表类对象。
                    Log.i( m_CurClsNameStrPt, "创建并初始化接收音频输出帧链表对象成功。" );

                    //初始化接收视频输出帧链表类对象。
                    m_RecvVideoOutputFrameLnkLstPt = new LinkedList< byte[] >(); //创建接收视频输出帧链表类对象。
                    Log.i( m_CurClsNameStrPt, "创建并初始化接收视频输出帧链表对象成功。" );
                    break;
                }
                case 1: //如果使用自适应抖动缓冲器。
                {
                    //初始化音频自适应抖动缓冲器类对象。
                    m_AAjbPt = new AAjb();
                    if( m_AAjbPt.Init( m_AudioOutputPt.m_SamplingRate, m_AudioOutputPt.m_FrameLen, 1, 1, 0, m_AAjbMinNeedBufFrameCnt, m_AAjbMaxNeedBufFrameCnt, m_AAjbAdaptSensitivity, m_ErrInfoVarStrPt ) == 0 )
                    {
                        Log.i( m_CurClsNameStrPt, "创建并初始化音频自适应抖动缓冲器类对象成功。" );
                    }
                    else
                    {
                        String p_InfoStrPt = "创建并初始化音频自适应抖动缓冲器类对象失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    //初始化视频自适应抖动缓冲器类对象。
                    m_VAjbPt = new VAjb();
                    if( m_VAjbPt.Init( 1, m_VAjbMinNeedBufFrameCnt, m_VAjbMaxNeedBufFrameCnt, m_VAjbAdaptSensitivity, m_ErrInfoVarStrPt ) == 0 )
                    {
                        Log.i( m_CurClsNameStrPt, "创建并初始化视频自适应抖动缓冲器类对象成功。" );
                    }
                    else
                    {
                        String p_InfoStrPt = "创建并初始化视频自适应抖动缓冲器类对象失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }
                    break;
                }
            }

            m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间为当前时间。
            m_LastPktRecvTime = m_LastPktSendTime; //设置最后一个数据包的接收时间为当前时间。

            m_LastSendAudioInputFrameIsAct = 0; //设置最后发送的一个音频输入帧为无语音活动。
            m_LastSendAudioInputFrameIsRecv = 1; //设置最后一个发送的音频输入帧远端已经接收到。
            m_LastSendAudioInputFrameTimeStamp = 0 - 1; //设置最后一个发送音频输入帧的时间戳为0的前一个，因为第一次发送音频输入帧时会递增一个步进。
            m_LastSendVideoInputFrameTimeStamp = 0 - 1; //设置最后一个发送视频输入帧的时间戳为0的前一个，因为第一次发送视频输入帧时会递增一个步进。

            m_LastGetAudioOutputFrameIsAct = 0; //设置最后一个取出的音频输出帧为无语音活动，因为如果不使用音频输出，只使用视频输出时，可以保证视频正常输出。
            m_LastGetAudioOutputFrameVideoOutputFrameTimeStamp = 0; //设置最后一个取出的音频输出帧对应视频输出帧的时间戳为0。

            String p_InfoStrPt = "开始进行对讲。";
            Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
            if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );

            p_Result = 0; //设置本函数执行成功。
        }

        return p_Result;
    }

    //用户定义的处理函数，在本线程运行时每隔1毫秒就回调一次，返回值表示是否成功，为0表示成功，为非0表示失败。
    @Override public int UserProcess()
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
        int p_TmpInt;

        out:
        {
            //接收远端发送过来的一个数据包。
            if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.RecvPkt( m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
                ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
            {
                if( m_TmpHTLongPt.m_Val != -1 ) //如果用已连接的本端套接字开始接收连接的远端套接字发送的一个数据包成功。
                {
                    m_LastPktRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。

                    if( m_TmpHTLongPt.m_Val == 0 ) //如果数据包的数据长度为0。
                    {
                        Log.e( m_CurClsNameStrPt, "接收一个数据包的数据长度为" + m_TmpHTLongPt.m_Val + "，表示没有数据，无法继续接收。" );
                        break out;
                    }
                    else if( m_TmpBytePt[0] == PKT_TYP_CNCT_HTBT ) //如果是心跳包。
                    {
                        if( m_TmpHTLongPt.m_Val > 1 ) //如果心跳包的数据长度大于1。
                        {
                            Log.e( m_CurClsNameStrPt, "接收一个心跳包的数据长度为" + m_TmpHTLongPt.m_Val + "大于1，表示还有其他数据，无法继续接收。" );
                            break out;
                        }

                        Log.i( m_CurClsNameStrPt, "接收一个心跳包。" );
                    }
                    else if( m_TmpBytePt[0] == PKT_TYP_AFRAME ) //如果是音频输出帧包。
                    {
                        if( m_TmpHTLongPt.m_Val < 1 + 4 ) //如果音频输出帧包的数据长度小于1 + 4，表示没有音频输出帧时间戳。
                        {
                            Log.e( m_CurClsNameStrPt, "接收一个音频输出帧包的数据长度为" + m_TmpHTLongPt.m_Val + "小于1 + 4，表示没有音频输出帧时间戳，无法继续接收。" );
                            break out;
                        }

                        //读取音频输出帧时间戳。
                        p_TmpInt = ( m_TmpBytePt[1] & 0xFF ) + ( ( m_TmpBytePt[2] & 0xFF ) << 8 ) + ( ( m_TmpBytePt[3] & 0xFF ) << 16 ) + ( ( m_TmpBytePt[4] & 0xFF ) << 24 );

                        if( m_AudioOutputPt.m_IsUseAudioOutput != 0 ) //如果要使用音频输出。
                        {
                            //将音频输出帧放入链表或自适应抖动缓冲器。
                            switch( m_UseWhatRecvOutputFrame ) //使用什么接收输出帧。
                            {
                                case 0: //如果使用链表。
                                {
                                    if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
                                    {
                                        synchronized( m_RecvAudioOutputFrameLnkLstPt )
                                        {
                                            m_RecvAudioOutputFrameLnkLstPt.addLast( Arrays.copyOfRange( m_TmpBytePt, 1 + 4, ( int ) ( m_TmpHTLongPt.m_Val ) ) );
                                        }
                                        Log.i( m_CurClsNameStrPt, "接收一个有语音活动的音频输出帧包，并放入接收音频输出帧链表成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }
                                    else //如果该音频输出帧为无语音活动。
                                    {
                                        Log.i( m_CurClsNameStrPt, "接收一个无语音活动的音频输出帧包，无需放入接收音频输出帧链表。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }
                                    break;
                                }
                                case 1: //如果使用自适应抖动缓冲器。
                                {
                                    if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
                                    {
                                        m_AAjbPt.PutOneByteFrame( p_TmpInt, m_TmpBytePt, 1 + 4, m_TmpHTLongPt.m_Val - 1 - 4, 1, null );
                                        Log.i( m_CurClsNameStrPt, "接收一个有语音活动的音频输出帧包，并放入音频自适应抖动缓冲器成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }
                                    else //如果该音频输出帧为无语音活动。
                                    {
                                        m_AAjbPt.PutOneByteFrame( p_TmpInt, m_TmpBytePt, 1 + 4, 0, 1, null );
                                        Log.i( m_CurClsNameStrPt, "接收一个无语音活动的音频输出帧包，并放入音频自适应抖动缓冲器成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }

                                    HTInt p_CurHaveBufActFrameCntPt = new HTInt(); //存放当前已缓冲有活动帧的数量。
                                    HTInt p_CurHaveBufInactFrameCntPt = new HTInt(); //存放当前已缓冲无活动帧的数量。
                                    HTInt p_CurHaveBufFrameCntPt = new HTInt(); //存放当前已缓冲帧的数量。
                                    HTInt p_MinNeedBufFrameCntPt = new HTInt(); //存放最小需缓冲帧的数量。
                                    HTInt p_MaxNeedBufFrameCntPt = new HTInt(); //存放最大需缓冲帧的数量。
                                    HTInt p_CurNeedBufFrameCntPt = new HTInt(); //存放当前需缓冲帧的数量。
                                    m_AAjbPt.GetBufFrameCnt( p_CurHaveBufActFrameCntPt, p_CurHaveBufInactFrameCntPt, p_CurHaveBufFrameCntPt, p_MinNeedBufFrameCntPt, p_MaxNeedBufFrameCntPt, p_CurNeedBufFrameCntPt, 1, null );
                                    Log.i( m_CurClsNameStrPt, "音频自适应抖动缓冲器：有活动帧：" + p_CurHaveBufActFrameCntPt.m_Val + "，无活动帧：" + p_CurHaveBufInactFrameCntPt.m_Val + "，帧：" + p_CurHaveBufFrameCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrameCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrameCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrameCntPt.m_Val + "。" );

                                    break;
                                }
                            }
                        }
                        else //如果不使用音频输出。
                        {
                            if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
                            {
                                Log.i( m_CurClsNameStrPt, "接收一个有语音活动的音频输出帧包成功，但不使用音频输出。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                            }
                            else //如果该音频输出帧为无语音活动。
                            {
                                Log.i( m_CurClsNameStrPt, "接收一个无语音活动的音频输出帧包成功，但不使用音频输出。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                            }
                        }

                        if( ( m_UseWhatXfrPrtcl == 1 ) && ( m_TmpHTLongPt.m_Val == 1 + 4 ) ) //如果是使用UDP协议，且本音频输出帧为无语音活动。
                        {
                            //设置音频输出帧应答包。
                            m_TmpBytePt[0] = PKT_TYP_ACK;
                            //设置音频输出帧时间戳。
                            m_TmpBytePt[1] = ( byte ) ( p_TmpInt & 0xFF );
                            m_TmpBytePt[2] = ( byte ) ( ( p_TmpInt & 0xFF00 ) >> 8 );
                            m_TmpBytePt[3] = ( byte ) ( ( p_TmpInt & 0xFF0000 ) >> 16 );
                            m_TmpBytePt[4] = ( byte ) ( ( p_TmpInt & 0xFF000000 ) >> 24 );

                            if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1 + 4, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 )
                            {
                                m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。
                                Log.i( m_CurClsNameStrPt, "发送一个音频输出帧应答包成功。时间戳：" + p_TmpInt + "，总长度：" + 1 + 4 + "。" );
                            }
                            else
                            {
                                String p_InfoStrPt = "发送一个音频输出帧应答包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break out;
                            }
                        }
                    }
                    else if( m_TmpBytePt[0] == PKT_TYP_VFRAME ) //如果是视频输出帧包。
                    {
                        if( m_TmpHTLongPt.m_Val < 1 + 4 ) //如果视频输出帧包的数据长度小于1 + 4，表示没有视频输出帧时间戳。
                        {
                            Log.e( m_CurClsNameStrPt, "接收一个视频输出帧包的数据长度为" + m_TmpHTLongPt.m_Val + "小于1 + 4，表示没有视频输出帧时间戳，无法继续接收。" );
                            break out;
                        }

                        //读取视频输出帧时间戳。
                        p_TmpInt = ( m_TmpBytePt[1] & 0xFF ) + ( ( m_TmpBytePt[2] & 0xFF ) << 8 ) + ( ( m_TmpBytePt[3] & 0xFF ) << 16 ) + ( ( m_TmpBytePt[4] & 0xFF ) << 24 );

                        if( m_VideoOutputPt.m_IsUseVideoOutput != 0 ) //如果要使用视频输出。
                        {
                            //将视频输出帧放入链表或自适应抖动缓冲器。
                            switch( m_UseWhatRecvOutputFrame ) //使用什么接收输出帧。
                            {
                                case 0: //如果使用链表。
                                {
                                    if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
                                    {
                                        synchronized( m_RecvVideoOutputFrameLnkLstPt )
                                        {
                                            m_RecvVideoOutputFrameLnkLstPt.addLast( Arrays.copyOfRange( m_TmpBytePt, 1 + 4, ( int ) ( m_TmpHTLongPt.m_Val ) ) );
                                        }
                                        Log.i( m_CurClsNameStrPt, "接收一个有图像活动的视频输出帧包，并放入接收视频输出帧链表成功。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }
                                    else //如果该视频输出帧为无图像活动。
                                    {
                                        Log.i( m_CurClsNameStrPt, "接收一个无图像活动的视频输出帧包，无需放入接收视频输出帧链表。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }
                                    break;
                                }
                                case 1: //如果使用自适应抖动缓冲器。
                                {
                                    if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
                                    {
                                        m_VAjbPt.PutOneByteFrame( System.currentTimeMillis(), p_TmpInt, m_TmpBytePt, 1 + 4, m_TmpHTLongPt.m_Val - 1 - 4, 1, null );
                                        Log.i( m_CurClsNameStrPt, "接收一个有图像活动的视频输出帧包，并放入视频自适应抖动缓冲器成功。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "，类型：" + ( m_TmpBytePt[13] & 0xff ) + "。" );
                                    }
                                    else //如果该视频输出帧为无图像活动。
                                    {
                                        Log.i( m_CurClsNameStrPt, "接收一个无图像活动的视频输出帧包，无需放入视频自适应抖动缓冲器。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }

                                    HTInt p_CurHaveBufFrameCntPt = new HTInt(); //存放当前已缓冲帧的数量。
                                    HTInt p_MinNeedBufFrameCntPt = new HTInt(); //存放最小需缓冲帧的数量。
                                    HTInt p_MaxNeedBufFrameCntPt = new HTInt(); //存放最大需缓冲帧的数量。
                                    HTInt p_CurNeedBufFrameCntPt = new HTInt(); //存放当前需缓冲帧的数量。
                                    m_VAjbPt.GetBufFrameCnt( p_CurHaveBufFrameCntPt, p_MinNeedBufFrameCntPt, p_MaxNeedBufFrameCntPt, p_CurNeedBufFrameCntPt, 1, null );
                                    Log.i( m_CurClsNameStrPt, "视频自适应抖动缓冲器：帧：" + p_CurHaveBufFrameCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrameCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrameCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrameCntPt.m_Val + "。" );

                                    break;
                                }
                            }
                        }
                        else //如果不使用视频输出。
                        {
                            if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
                            {
                                Log.i( m_CurClsNameStrPt, "接收一个有图像活动的视频输出帧包成功，但不使用视频输出。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                            }
                            else //如果该视频输出帧为无图像活动。
                            {
                                Log.i( m_CurClsNameStrPt, "接收一个无图像活动的视频输出帧包成功，但不使用视频输出。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                            }
                        }
                    }
                    else if( m_TmpBytePt[0] == PKT_TYP_ACK ) //如果是连接应答包或音视频输入输出帧应答包。
                    {
                        if( m_TmpHTLongPt.m_Val == 1 ) //如果数据包的数据长度等于1，表示是连接应答包，就不管。
                        {

                        }
                        else //如果数据包的数据长度大于1，表示是音视频输入输出帧应答包。
                        {
                            if( m_TmpHTLongPt.m_Val != 1 + 4 )
                            {
                                Log.e( m_CurClsNameStrPt, "接收一个音视频输入输出帧应答包的数据长度为" + m_TmpHTLongPt.m_Val + "不等于1 + 4，表示格式不正确，无法继续接收。" );
                                break out;
                            }

                            //读取时间戳。
                            p_TmpInt = ( m_TmpBytePt[1] & 0xFF ) + ( ( m_TmpBytePt[2] & 0xFF ) << 8 ) + ( ( m_TmpBytePt[3] & 0xFF ) << 16 ) + ( ( m_TmpBytePt[4] & 0xFF ) << 24 );

                            Log.i( m_CurClsNameStrPt, "接收一个音视频输入输出帧应答包。时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );

                            //设置最后一个发送的音频输入帧远端是否接收到。
                            if( m_LastSendAudioInputFrameTimeStamp == p_TmpInt ) m_LastSendAudioInputFrameIsRecv = 1;
                        }
                    }
                    else if( m_TmpBytePt[0] == PKT_TYP_EXIT ) //如果是退出包。
                    {
                        if( m_TmpHTLongPt.m_Val > 1 ) //如果退出包的数据长度大于1。
                        {
                            Log.e( m_CurClsNameStrPt, "接收一个退出包的数据长度为" + m_TmpHTLongPt.m_Val + "大于1，表示还有其他数据，无法继续接收。" );
                            break out;
                        }

                        m_IsRecvExitPkt = 1; //设置已经接收到退出包。
                        RequireExit( 1, 0 ); //请求退出。

                        String p_InfoStrPt = "接收一个退出包。";
                        Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    }
                }
                else //如果用已连接的本端套接字开始接收连接的远端套接字发送的一个数据包超时。
                {

                }
            }
            else //如果用已连接的本端套接字开始接收连接的远端套接字发送的一个数据包失败。
            {
                String p_InfoStrPt = "用已连接的本端套接字开始接收连接的远端套接字发送的一个数据包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                break out;
            }

            //发送心跳包。
            if( System.currentTimeMillis() - m_LastPktSendTime >= 100 ) //如果超过100毫秒没有发送任何数据包，就发送一个心跳包。
            {
                m_TmpBytePt[0] = PKT_TYP_CNCT_HTBT; //设置心跳包。
                if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, 1, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
                    ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
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
            if( m_UseWhatXfrPrtcl == 0 ) //如果使用TCP协议。
            {
                if( System.currentTimeMillis() - m_LastPktRecvTime > 2000 ) //如果超过2000毫秒没有接收任何数据包，就判定连接已经断开了。
                {
                    String p_InfoStrPt = "超过2000毫秒没有接收任何数据包，判定套接字连接已经断开了。";
                    Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break out;
                }
            }
            else if( m_UseWhatXfrPrtcl == 1 ) //如果使用UDP协议。
            {
                if( System.currentTimeMillis() - m_LastPktRecvTime > 5000 ) //如果超过5000毫秒没有接收任何数据包，就判定连接已经断开了。
                {
                    String p_InfoStrPt = "超过5000毫秒没有接收任何数据包，判定套接字连接已经断开了。";
                    Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break out;
                }
            }

            p_Result = 0; //设置本函数执行成功。
        }

        return p_Result;
    }

    //用户定义的销毁函数，在本线程退出时回调一次。
    @Override public void UserDestroy()
    {
        SendExitPkt:
        if( ( m_ExitFlag == 1 ) && ( ( m_TcpClntSoktPt != null ) || ( ( m_UdpSoktPt != null ) && ( m_UdpSoktPt.GetRmtAddr( null, null, null, 0, null ) == 0 ) ) ) ) //如果本线程接收到退出请求，且本端TCP协议客户端套接字类对象不为空或本端UDP协议套接字类对象不为空且已连接远端。
        {
            //循环发送退出包。
            m_TmpBytePt[0] = PKT_TYP_EXIT; //设置退出包。
            for( int p_SendTimes = ( m_UseWhatXfrPrtcl == 0 ) ? 1 : 5; p_SendTimes > 0; p_SendTimes-- )
            {
                if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, 1, ( short ) 0, 0, m_ErrInfoVarStrPt ) != 0 ) ) ||
                    ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 0, m_ErrInfoVarStrPt ) != 0 ) ) )
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
                    if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.RecvPkt( m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 5000, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
                        ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 5000, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
                    {
                        if( m_TmpHTLongPt.m_Val != -1 ) //如果用已连接的本端套接字开始接收连接的远端套接字发送的一个数据包成功。
                        {
                            m_LastPktRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。

                            if( ( m_TmpHTLongPt.m_Val == 1 ) && ( m_TmpBytePt[0] == PKT_TYP_EXIT ) ) //如果是退出包。
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
        if( m_RecvAudioOutputFrameLnkLstPt != null )
        {
            m_RecvAudioOutputFrameLnkLstPt.clear();
            m_RecvAudioOutputFrameLnkLstPt = null;

            Log.i( m_CurClsNameStrPt, "销毁接收输出帧链表类对象成功。" );
        }

        //销毁视频自适应抖动缓冲器类对象。
        if( m_VAjbPt != null )
        {
            m_VAjbPt.Destroy( null );
            m_VAjbPt = null;

            Log.i( m_CurClsNameStrPt, "销毁视频自适应抖动缓冲器类对象成功。" );
        }

        //销毁音频自适应抖动缓冲器类对象。
        if( m_AAjbPt != null )
        {
            m_AAjbPt.Destroy( null );
            m_AAjbPt = null;

            Log.i( m_CurClsNameStrPt, "销毁音频自适应抖动缓冲器类对象成功。" );
        }

        if( m_IsCreateSrvrOrClnt == 1 ) //如果是创建服务端。
        {
            if( ( m_ExitFlag == 1 ) && ( m_IsRecvExitPkt == 1 ) ) //如果本线程接收到退出请求，且接收到了退出包。
            {
                String p_InfoStrPt = "由于是创建服务端，且本线程接收到退出请求，且接收到了退出包，表示是远端TCP协议客户端套接字主动退出，本线程重新初始化来继续保持监听。";
                Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                {Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

                RequireExit( 2, 0 ); //请求重启。
                {Message clMessage = new Message();clMessage.what = 4;m_MainActivityHandlerPt.sendMessage( clMessage );} //向主界面发送重建SurfaceView控件消息。
            }
            else if( ( m_ExitFlag == 0 ) && ( m_ExitCode == -2 ) ) //如果本线程没收到退出请求，且退出代码为处理失败。
            {
                String p_InfoStrPt = "由于是创建服务端，且本线程没收到退出请求，且退出码为处理失败，表示是处理失败或连接异常断开，本线程重新初始化来继续保持监听。";
                Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                {Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

                RequireExit( 2, 0 ); //请求重启。
                {Message clMessage = new Message();clMessage.what = 4;m_MainActivityHandlerPt.sendMessage( clMessage );} //向主界面发送重建SurfaceView控件消息。
            }
            else //其他情况，本线程直接退出。
            {
                {Message clMessage = new Message();clMessage.what = 2;m_MainActivityHandlerPt.sendMessage( clMessage );} //向主界面发送媒体处理线程退出的消息。
                {Message clMessage = new Message();clMessage.what = 4;m_MainActivityHandlerPt.sendMessage( clMessage );} //向主界面发送重建SurfaceView控件消息。
            }
        }
        else if( m_IsCreateSrvrOrClnt == 0 ) //如果是创建客户端。
        {
            if( ( m_ExitFlag == 0 ) && ( m_ExitCode == -2 ) ) //如果本线程没收到退出请求，且退出代码为处理失败。
            {
                String p_InfoStrPt = "由于是创建客户端，且本线程没收到退出请求，且退出码为处理失败，表示是处理失败或连接异常断开，本线程重新初始化来重连服务端。";
                Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                {Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

                RequireExit( 2, 0 ); //请求重启。
            }
            else //其他情况，本线程直接退出。
            {
                {Message clMessage = new Message();clMessage.what = 2;m_MainActivityHandlerPt.sendMessage( clMessage );} //向主界面发送媒体处理线程退出的消息。
                {Message clMessage = new Message();clMessage.what = 4;m_MainActivityHandlerPt.sendMessage( clMessage );} //向主界面发送重建SurfaceView控件消息。
            }
        }
    }

    //用户定义的读取音视频输入帧函数，在读取到一个音频输入帧或视频输入帧并处理完后回调一次，为0表示成功，为非0表示失败。
    @Override public int UserReadAudioVideoInputFrame( short PcmAudioInputFramePt[], short PcmAudioResultFramePt[], HTInt VoiceActStsPt, byte EncoderAudioInputFramePt[], HTLong EncoderAudioInputFrameLenPt, HTInt EncoderAudioInputFrameIsNeedTransPt,
                                                       byte YU12VideoInputFramePt[], HTInt YU12VideoInputFrameWidthPt, HTInt YU12VideoInputFrameHeightPt, byte EncoderVideoInputFramePt[], HTLong EncoderVideoInputFrameLenPt )
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
        int p_FramePktLen = 0; //存放输入输出帧数据包的数据长度，单位字节。
        int p_TmpInt32 = 0;

        out:
        {
            //发送音频输入帧。
            if( PcmAudioInputFramePt != null ) //如果要使用音频输入。
            {
                if( EncoderAudioInputFramePt != null ) //如果要使用已编码格式音频输入帧。
                {
                    if( VoiceActStsPt.m_Val != 0 && EncoderAudioInputFrameIsNeedTransPt.m_Val != 0 ) //如果本次音频输入帧为有语音活动，且需要传输。
                    {
                        System.arraycopy( EncoderAudioInputFramePt, 0, m_TmpBytePt, 1 + 4 + 4, ( int ) EncoderAudioInputFrameLenPt.m_Val ); //设置音频输入输出帧。
                        p_FramePktLen = 1 + 4 + 4 + ( int )EncoderAudioInputFrameLenPt.m_Val; //数据包长度 = 数据包类型 + 音频输入帧时间戳 + 视频输入帧时间戳 + 已编码格式音频输入帧。
                    }
                    else //如果本次音频输入帧为无语音活动，或不需要传输。
                    {
                        p_FramePktLen = 1 + 4; //数据包长度 = 数据包类型 + 音频输入帧时间戳。
                    }
                }
                else //如果要使用PCM格式音频输入帧。
                {
                    if( VoiceActStsPt.m_Val != 0 ) //如果本次音频输入帧为有语音活动。
                    {
                        for( p_TmpInt32 = 0; p_TmpInt32 < PcmAudioResultFramePt.length; p_TmpInt32++ ) //设置音频输入输出帧。
                        {
                            m_TmpBytePt[1 + 4 + 4 + p_TmpInt32 * 2] = ( byte ) ( PcmAudioResultFramePt[p_TmpInt32] & 0xFF );
                            m_TmpBytePt[1 + 4 + 4 + p_TmpInt32 * 2 + 1] = ( byte ) ( ( PcmAudioResultFramePt[p_TmpInt32] & 0xFF00 ) >> 8 );
                        }
                        p_FramePktLen = 1 + 4 + 4 + PcmAudioResultFramePt.length * 2; //数据包长度 = 数据包类型 + 音频输入帧时间戳 + 视频输入帧时间戳 + PCM格式音频输入帧。
                    }
                    else //如果本次音频输入帧为无语音活动，或不需要传输。
                    {
                        p_FramePktLen = 1 + 4; //数据包长度 = 数据包类型 + 音频输入帧时间戳。
                    }
                }

                //发送音频输入帧数据包。
                if( p_FramePktLen != 1 + 4 ) //如果本音频输入帧为有语音活动，就发送。
                {
                    m_LastSendAudioInputFrameTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

                    //设置数据包类型为音频输入帧包。
                    m_TmpBytePt[0] = PKT_TYP_AFRAME;
                    //设置音频输入帧时间戳。
                    m_TmpBytePt[1] = ( byte ) ( m_LastSendAudioInputFrameTimeStamp & 0xFF );
                    m_TmpBytePt[2] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF00 ) >> 8 );
                    m_TmpBytePt[3] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF0000 ) >> 16 );
                    m_TmpBytePt[4] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF000000 ) >> 24 );
                    //设置视频输入帧时间戳。
                    m_TmpBytePt[5] = ( byte ) ( m_LastSendVideoInputFrameTimeStamp & 0xFF );
                    m_TmpBytePt[6] = ( byte ) ( ( m_LastSendVideoInputFrameTimeStamp & 0xFF00 ) >> 8 );
                    m_TmpBytePt[7] = ( byte ) ( ( m_LastSendVideoInputFrameTimeStamp & 0xFF0000 ) >> 16 );
                    m_TmpBytePt[8] = ( byte ) ( ( m_LastSendVideoInputFrameTimeStamp & 0xFF000000 ) >> 24 );

                    if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, p_FramePktLen, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
                        ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, p_FramePktLen, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
                    {
                        m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。
                        Log.i( m_CurClsNameStrPt, "发送一个有语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_LastSendAudioInputFrameTimeStamp + "，视频输入帧时间戳：" + m_LastSendVideoInputFrameTimeStamp + "，总长度：" + p_FramePktLen + "。" );
                    }
                    else
                    {
                        String p_InfoStrPt = "发送一个有语音活动的音频输入帧包失败。原因：" + m_ErrInfoVarStrPt.GetStr() + "音频输入帧时间戳：" + m_LastSendAudioInputFrameTimeStamp + "，视频输入帧时间戳：" + m_LastSendVideoInputFrameTimeStamp + "，总长度：" + p_FramePktLen + "。";
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    m_LastSendAudioInputFrameIsAct = 1; //设置最后一个发送的音频输入帧有语音活动。
                    m_LastSendAudioInputFrameIsRecv = 1; //设置最后一个发送的音频输入帧远端已经接收到。
                }
                else if( ( p_FramePktLen == 1 + 4 ) && ( m_LastSendAudioInputFrameIsAct != 0 ) ) //如果本音频输入帧为无语音活动，但最后一个发送的音频输入帧为有语音活动，就发送。
                {
                    m_LastSendAudioInputFrameTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

                    //设置数据包类型为音频输入帧包。
                    m_TmpBytePt[0] = PKT_TYP_AFRAME;
                    //设置音频输入帧时间戳。
                    m_TmpBytePt[1] = ( byte ) ( m_LastSendAudioInputFrameTimeStamp & 0xFF );
                    m_TmpBytePt[2] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF00 ) >> 8 );
                    m_TmpBytePt[3] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF0000 ) >> 16 );
                    m_TmpBytePt[4] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF000000 ) >> 24 );

                    if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, p_FramePktLen, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
                        ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, p_FramePktLen, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
                    {
                        m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。
                        Log.i( m_CurClsNameStrPt, "发送一个无语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_LastSendAudioInputFrameTimeStamp + "，总长度：" + p_FramePktLen + "。" );
                    }
                    else
                    {
                        String p_InfoStrPt = "发送一个无语音活动的音频输入帧包失败。原因：" + m_ErrInfoVarStrPt.GetStr() + "音频输入帧时间戳：" + m_LastSendAudioInputFrameTimeStamp + "，总长度：" + p_FramePktLen + "。";
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }

                    m_LastSendAudioInputFrameIsAct = 0; //设置最后一个发送的音频输入帧无语音活动。
                    m_LastSendAudioInputFrameIsRecv = 0; //设置最后一个发送的音频输入帧远端没有接收到。
                }
                else //如果本音频输入帧为无语音活动，且最后一个发送的音频输入帧为无语音活动，无需发送。
                {
                    Log.i( m_CurClsNameStrPt, "本音频输入帧为无语音活动，且最后一个发送的音频输入帧为无语音活动，无需发送。" );

                    if( ( m_UseWhatXfrPrtcl == 1 ) && ( m_LastSendAudioInputFrameIsRecv == 0 ) ) //如果是使用UDP协议，且本音频输入帧为无语音活动，且最后一个发送的音频输入帧为无语音活动，且最后一个发送的音频输入帧远端没有接收到。
                    {
                        //设置音频输入帧包。
                        m_TmpBytePt[0] = PKT_TYP_AFRAME;
                        //设置音频输入帧时间戳。
                        m_TmpBytePt[1] = ( byte ) ( m_LastSendAudioInputFrameTimeStamp & 0xFF );
                        m_TmpBytePt[2] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF00 ) >> 8 );
                        m_TmpBytePt[3] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF0000 ) >> 16 );
                        m_TmpBytePt[4] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF000000 ) >> 24 );

                        if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, p_FramePktLen, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 )
                        {
                            m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。
                            Log.i( m_CurClsNameStrPt, "重新发送最后一个无语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_LastSendAudioInputFrameTimeStamp + "，总长度：" + p_FramePktLen + "。" );
                        }
                        else
                        {
                            String p_InfoStrPt = "重新发送最后一个无语音活动的音频输入帧包失败。原因：" + m_ErrInfoVarStrPt.GetStr() + "音频输入帧时间戳：" + m_LastSendAudioInputFrameTimeStamp + "，总长度：" + p_FramePktLen + "。";
                            Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break out;
                        }
                    }
                }
            }

            //发送视频输入帧。
            if( YU12VideoInputFramePt != null ) //如果要使用视频输入。
            {
                if( EncoderVideoInputFramePt != null ) //如果要使用已编码格式视频输入帧。
                {
                    if( EncoderVideoInputFrameLenPt.m_Val != 0 ) //如果本次视频输入帧为有图像活动。
                    {
                        System.arraycopy( EncoderVideoInputFramePt, 0, m_TmpBytePt, 1 + 4 + 4, ( int ) EncoderVideoInputFrameLenPt.m_Val ); //设置视频输入输出帧。
                        p_FramePktLen = 1 + 4 + 4 + ( int ) EncoderVideoInputFrameLenPt.m_Val; //数据包长度 = 数据包类型 + 视频输入帧时间戳 + 音频输入帧时间戳 + 已编码格式视频输入帧。
                    }
                    else
                    {
                        p_FramePktLen = 1 + 4; //数据包长度 = 数据包类型 + 视频输入帧时间戳。
                    }
                }
                else //如果要使用YU12格式视频输入帧。
                {
                    //设置视频输入帧宽度。
                    m_TmpBytePt[9] = ( byte ) ( YU12VideoInputFrameWidthPt.m_Val & 0xFF );
                    m_TmpBytePt[10] = ( byte ) ( ( YU12VideoInputFrameWidthPt.m_Val & 0xFF00 ) >> 8 );
                    m_TmpBytePt[11] = ( byte ) ( ( YU12VideoInputFrameWidthPt.m_Val & 0xFF0000 ) >> 16 );
                    m_TmpBytePt[12] = ( byte ) ( ( YU12VideoInputFrameWidthPt.m_Val & 0xFF000000 ) >> 24 );
                    //设置视频输入帧高度。
                    m_TmpBytePt[13] = ( byte ) ( YU12VideoInputFrameHeightPt.m_Val & 0xFF );
                    m_TmpBytePt[14] = ( byte ) ( ( YU12VideoInputFrameHeightPt.m_Val & 0xFF00 ) >> 8 );
                    m_TmpBytePt[15] = ( byte ) ( ( YU12VideoInputFrameHeightPt.m_Val & 0xFF0000 ) >> 16 );
                    m_TmpBytePt[16] = ( byte ) ( ( YU12VideoInputFrameHeightPt.m_Val & 0xFF000000 ) >> 24 );

                    System.arraycopy( YU12VideoInputFramePt, 0, m_TmpBytePt, 1 + 4 + 4 + 4 + 4, YU12VideoInputFramePt.length ); //设置视频输入输出帧。
                    p_FramePktLen = 1 + 4 + 4 + 4 + 4 + YU12VideoInputFramePt.length; //数据包长度 = 数据包类型 + 视频输入帧时间戳 + 音频输入帧时间戳 + 视频输入帧宽度 + 视频输入帧高度 + YU12格式视频输入帧。
                }

                //发送视频输入帧数据包。
                if( p_FramePktLen != 1 + 4 ) //如果本视频输入帧为有图像活动，就发送。
                {
                    m_LastSendVideoInputFrameTimeStamp += 1; //视频输入帧的时间戳递增一个步进。

                    //设置数据包类型为视频输入帧包。
                    m_TmpBytePt[0] = PKT_TYP_VFRAME;
                    //设置视频输入帧时间戳。
                    m_TmpBytePt[1] = ( byte ) ( m_LastSendVideoInputFrameTimeStamp & 0xFF );
                    m_TmpBytePt[2] = ( byte ) ( ( m_LastSendVideoInputFrameTimeStamp & 0xFF00 ) >> 8 );
                    m_TmpBytePt[3] = ( byte ) ( ( m_LastSendVideoInputFrameTimeStamp & 0xFF0000 ) >> 16 );
                    m_TmpBytePt[4] = ( byte ) ( ( m_LastSendVideoInputFrameTimeStamp & 0xFF000000 ) >> 24 );
                    //设置音频输入帧时间戳。
                    m_TmpBytePt[5] = ( byte ) ( m_LastSendAudioInputFrameTimeStamp & 0xFF );
                    m_TmpBytePt[6] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF00 ) >> 8 );
                    m_TmpBytePt[7] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF0000 ) >> 16 );
                    m_TmpBytePt[8] = ( byte ) ( ( m_LastSendAudioInputFrameTimeStamp & 0xFF000000 ) >> 24 );

                    if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, p_FramePktLen, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
                        ( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, p_FramePktLen, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
                    {
                        m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。
                        Log.i( m_CurClsNameStrPt, "发送一个有图像活动的视频输入帧包成功。视频输入帧时间戳：" + m_LastSendVideoInputFrameTimeStamp + "，音频输入帧时间戳：" + m_LastSendAudioInputFrameTimeStamp + "，总长度：" + p_FramePktLen + "，类型：" + ( m_TmpBytePt[13] & 0xff ) + "。" );
                    }
                    else
                    {
                        String p_InfoStrPt = "发送一个有图像活动的视频输入帧包失败。原因：" + m_ErrInfoVarStrPt.GetStr() + "视频输入帧时间戳：" + m_LastSendVideoInputFrameTimeStamp + "，音频输入帧时间戳：" + m_LastSendAudioInputFrameTimeStamp + "，总长度：" + p_FramePktLen + "，类型：" + ( m_TmpBytePt[13] & 0xff ) + "。";
                        Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = 3;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break out;
                    }
                }
                else //如果本视频输入帧为无图像活动，无需发送。
                {
                    Log.i( m_CurClsNameStrPt, "本视频输入帧为无图像活动，无需发送。" );
                }
            }

            p_Result = 0; //设置本函数执行成功。
        }

        return p_Result;
    }

    //用户定义的写入音频输出帧函数，在需要写入一个音频输出帧时回调一次。注意：本函数不是在媒体处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致声学回音消除失败。
    @Override public void UserWriteAudioOutputFrame( short PcmAudioOutputFramePt[], byte EncoderAudioOutputFramePt[], HTLong EncoderAudioOutputFrameLen )
    {
        int p_AudioOutputFrameTimeStamp = 0;
        byte p_AudioOutputFramePt[] = null;
        long p_AudioOutputFrameLen = 0;
        int p_TmpInt32;

        out:
        {
            //取出并写入音频输出帧。
            {
                //从链表或自适应抖动缓冲器取出一个音频输出帧。
                switch( m_UseWhatRecvOutputFrame ) //使用什么接收输出帧。
                {
                    case 0: //如果使用链表。
                    {
                        if( m_RecvAudioOutputFrameLnkLstPt.size() != 0 ) //如果接收音频输出帧链表不为空。
                        {
                            synchronized( m_RecvAudioOutputFrameLnkLstPt )
                            {
                                p_AudioOutputFramePt = m_RecvAudioOutputFrameLnkLstPt.getFirst(); //获取接收音频输出帧链表的第一个音频输出帧。
                                m_RecvAudioOutputFrameLnkLstPt.removeFirst(); //删除接收音频输出帧链表的第一个音频输出帧。
                            }
                            p_AudioOutputFrameLen = p_AudioOutputFramePt.length;
                        }

                        if( p_AudioOutputFrameLen != 0 ) //如果音频输出帧为有语音活动。
                        {
                            Log.i( m_CurClsNameStrPt, "从接收音频输出帧链表取出一个有语音活动的音频输出帧。数据长度：" + p_AudioOutputFrameLen + "。" );
                        }
                        else //如果音频输出帧为无语音活动。
                        {
                            Log.i( m_CurClsNameStrPt, "从接收音频输出帧链表取出一个无语音活动的音频输出帧。数据长度：" + p_AudioOutputFrameLen + "。" );
                        }

                        break;
                    }
                    case 1: //如果使用自适应抖动缓冲器。
                    {
                        //从音频自适应抖动缓冲器取出一个音频输出帧。
                        m_AAjbPt.GetOneByteFrame( m_TmpHTInt2Pt, m_TmpByte2Pt, 0, m_TmpByte2Pt.length, m_TmpHTLong2Pt, 1, null );
                        p_AudioOutputFrameTimeStamp = m_TmpHTInt2Pt.m_Val;
                        p_AudioOutputFramePt = m_TmpByte2Pt;
                        p_AudioOutputFrameLen = m_TmpHTLong2Pt.m_Val;

                        if( p_AudioOutputFrameLen > 0 ) //如果音频输出帧为有语音活动。
                        {
                            m_LastGetAudioOutputFrameVideoOutputFrameTimeStamp = ( p_AudioOutputFramePt[0] & 0xFF ) + ( ( p_AudioOutputFramePt[1] & 0xFF ) << 8 ) + ( ( p_AudioOutputFramePt[2] & 0xFF ) << 16 ) + ( ( p_AudioOutputFramePt[3] & 0xFF ) << 24 ); //设置最后一个取出的音频输出帧对应视频输出帧的时间戳。
                            m_LastGetAudioOutputFrameIsAct = 1; //设置最后一个取出的音频输出帧为有语音活动。
                            Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个有语音活动的音频输出帧。音频输出帧时间戳：" + p_AudioOutputFrameTimeStamp + "，视频输出帧时间戳：" + m_LastGetAudioOutputFrameVideoOutputFrameTimeStamp + "，数据长度：" + p_AudioOutputFrameLen + "。" );
                        }
                        else if( p_AudioOutputFrameLen == 0 ) //如果音频输出帧为无语音活动。
                        {
                            m_LastGetAudioOutputFrameIsAct = 0; //设置最后一个取出的音频输出帧为无语音活动。
                            Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个无语音活动的音频输出帧。音频输出帧时间戳：" + p_AudioOutputFrameTimeStamp + "，数据长度：" + p_AudioOutputFrameLen + "。" );
                        }
                        else //如果音频输出帧为丢失。
                        {
                            m_LastGetAudioOutputFrameIsAct = 1; //设置最后一个取出的音频输出帧为有语音活动。
                            Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个丢失的音频输出帧。音频输出帧时间戳：" + p_AudioOutputFrameTimeStamp + "，视频输出帧时间戳：" + m_LastGetAudioOutputFrameVideoOutputFrameTimeStamp + "，数据长度：" + p_AudioOutputFrameLen + "。" );
                        }

                        HTInt p_CurHaveBufActFrameCntPt = new HTInt(); //存放当前已缓冲有活动帧的数量。
                        HTInt p_CurHaveBufInactFrameCntPt = new HTInt(); //存放当前已缓冲无活动帧的数量。
                        HTInt p_CurHaveBufFrameCntPt = new HTInt(); //存放当前已缓冲帧的数量。
                        HTInt p_MinNeedBufFrameCntPt = new HTInt(); //存放最小需缓冲帧的数量。
                        HTInt p_MaxNeedBufFrameCntPt = new HTInt(); //存放最大需缓冲帧的数量。
                        HTInt p_CurNeedBufFrameCntPt = new HTInt(); //存放当前需缓冲帧的数量。
                        m_AAjbPt.GetBufFrameCnt( p_CurHaveBufActFrameCntPt, p_CurHaveBufInactFrameCntPt, p_CurHaveBufFrameCntPt, p_MinNeedBufFrameCntPt, p_MaxNeedBufFrameCntPt, p_CurNeedBufFrameCntPt, 1, null );
                        Log.i( m_CurClsNameStrPt, "音频自适应抖动缓冲器：有活动帧：" + p_CurHaveBufActFrameCntPt.m_Val + "，无活动帧：" + p_CurHaveBufInactFrameCntPt.m_Val + "，帧：" + p_CurHaveBufFrameCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrameCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrameCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrameCntPt.m_Val + "。" );

                        break;
                    }
                }

                //写入音频输出帧。
                if( p_AudioOutputFrameLen > 0 ) //如果音频输出帧为有语音活动。
                {
                    if( PcmAudioOutputFramePt != null ) //如果要使用PCM格式音频输出帧。
                    {
                        if( p_AudioOutputFrameLen - 4 != PcmAudioOutputFramePt.length * 2 )
                        {
                            Arrays.fill( PcmAudioOutputFramePt, ( short ) 0 );
                            Log.e( m_CurClsNameStrPt, "音频输出帧的数据长度不等于PCM格式的数据长度。音频输出帧：" + ( p_AudioOutputFrameLen - 4 ) + "，PCM格式：" + ( PcmAudioOutputFramePt.length * 2 ) + "。" );
                            break out;
                        }

                        //写入PCM格式音频输出帧。
                        for( p_TmpInt32 = 0; p_TmpInt32 < PcmAudioOutputFramePt.length; p_TmpInt32++ )
                        {
                            PcmAudioOutputFramePt[p_TmpInt32] = ( short ) ( ( p_AudioOutputFramePt[4 + p_TmpInt32 * 2] & 0xFF ) | ( p_AudioOutputFramePt[4 + p_TmpInt32 * 2 + 1] << 8 ) );
                        }
                    }
                    else //如果要使用已编码格式音频输出帧。
                    {
                        if( p_AudioOutputFrameLen - 4 > EncoderAudioOutputFramePt.length )
                        {
                            EncoderAudioOutputFrameLen.m_Val = 0;
                            Log.e( m_CurClsNameStrPt, "视频输出帧的数据长度已超过已编码格式的数据长度。音频输出帧：" + ( p_AudioOutputFrameLen - 4 ) + "，已编码格式：" + EncoderAudioOutputFramePt.length + "。" );
                            break out;
                        }

                        //写入已编码格式音频输出帧。
                        System.arraycopy( p_AudioOutputFramePt, 4, EncoderAudioOutputFramePt, 0, ( int ) ( p_AudioOutputFrameLen - 4 ) );
                        EncoderAudioOutputFrameLen.m_Val = p_AudioOutputFrameLen - 4;
                    }
                }
                else if( p_AudioOutputFrameLen == 0 ) //如果音频输出帧为无语音活动。
                {
                    if( PcmAudioOutputFramePt != null ) //如果要使用PCM格式音频输出帧。
                    {
                        Arrays.fill( PcmAudioOutputFramePt, ( short ) 0 );
                    }
                    else //如果要使用已编码格式音频输出帧。
                    {
                        EncoderAudioOutputFrameLen.m_Val = 0;
                    }
                }
                else //如果音频输出帧为丢失。
                {
                    if( PcmAudioOutputFramePt != null ) //如果要使用PCM格式音频输出帧。
                    {
                        Arrays.fill( PcmAudioOutputFramePt, ( short ) 0 );
                    }
                    else //如果要使用已编码格式音频输出帧。
                    {
                        EncoderAudioOutputFrameLen.m_Val = p_AudioOutputFrameLen;
                    }
                }
            }
        }
    }

    //用户定义的获取PCM格式音频输出帧函数，在解码完一个已编码音频输出帧时回调一次。注意：本函数不是在媒体处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致声学回音消除失败。
    @Override public void UserGetPcmAudioOutputFrame( short PcmOutputFramePt[], long PcmAudioOutputFrameLen )
    {

    }

    //用户定义的写入视频输出帧函数，在可以显示一个视频输出帧时回调一次。注意：本函数不是在媒体处理线程中执行的，而是在视频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音视频输出帧不同步。
    @Override public void UserWriteVideoOutputFrame( byte YU12VideoOutputFramePt[], HTInt YU12VideoInputFrameWidthPt, HTInt YU12VideoInputFrameHeightPt, byte EncoderVideoOutputFramePt[], HTLong VideoOutputFrameLen )
    {
        int p_VideoOutputFrameTimeStamp = 0;
        int p_VideoOutputFrameAudioOutputFrameTimeStamp = 0;
        byte p_VideoOutputFramePt[] = null;
        long p_VideoOutputFrameLen = 0;

        //从链表或自适应抖动缓冲器取出一个视频输出帧。
        switch( m_UseWhatRecvOutputFrame ) //使用什么接收输出帧。
        {
            case 0: //如果使用链表。
            {
                if( m_RecvVideoOutputFrameLnkLstPt.size() != 0 ) //如果接收视频输出帧链表不为空。
                {
                    synchronized( m_RecvVideoOutputFrameLnkLstPt )
                    {
                        p_VideoOutputFramePt = m_RecvVideoOutputFrameLnkLstPt.getFirst(); //获取接收视频输出帧链表的第一个视频输出帧。
                        m_RecvVideoOutputFrameLnkLstPt.removeFirst(); //删除接收视频输出帧链表的第一个视频输出帧。
                    }
                    p_VideoOutputFrameLen = p_VideoOutputFramePt.length;
                }

                if( p_VideoOutputFrameLen != 0 ) //如果视频输出帧为有图像活动。
                {
                    Log.i( m_CurClsNameStrPt, "从接收视频输出帧链表取出一个有图像活动的视频输出帧。数据长度：" + p_VideoOutputFrameLen + "。" );
                }
                else //如果视频输出帧为无图像活动。
                {
                    Log.i( m_CurClsNameStrPt, "从接收视频输出帧链表取出一个无图像活动的视频输出帧。数据长度：" + p_VideoOutputFrameLen + "。" );
                }

                break;
            }
            case 1: //如果使用自适应抖动缓冲器。
            {
                HTInt p_CurHaveBufFrameCntPt = new HTInt(); //存放当前已缓冲帧的数量。
                HTInt p_MinNeedBufFrameCntPt = new HTInt(); //存放最小需缓冲帧的数量。
                HTInt p_MaxNeedBufFrameCntPt = new HTInt(); //存放最大需缓冲帧的数量。
                HTInt p_CurNeedBufFrameCntPt = new HTInt(); //存放当前需缓冲帧的数量。
                m_VAjbPt.GetBufFrameCnt( p_CurHaveBufFrameCntPt, p_MinNeedBufFrameCntPt, p_MaxNeedBufFrameCntPt, p_CurNeedBufFrameCntPt, 1, null );

                if( p_CurHaveBufFrameCntPt.m_Val != 0 ) //如果视频自适应抖动缓冲器不为空。
                {
                    Log.i( m_CurClsNameStrPt, "视频自适应抖动缓冲器：帧：" + p_CurHaveBufFrameCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrameCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrameCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrameCntPt.m_Val + "。" );

                    //从视频自适应抖动缓冲器取出一个视频输出帧。
                    if( m_AudioOutputPt.m_IsUseAudioOutput != 0 && m_LastGetAudioOutputFrameIsAct != 0 ) //如果要使用音频输出，且最后一个取出的音频输出帧为有语音活动，就根据最后一个取出的音频输出帧对应视频输出帧的时间戳来取出。
                    {
                        m_VAjbPt.GetOneByteFrameWantTimeStamp( System.currentTimeMillis(), m_LastGetAudioOutputFrameVideoOutputFrameTimeStamp, m_TmpHTInt3Pt, m_TmpByte3Pt, 0, m_TmpByte3Pt.length, m_TmpHTLong3Pt, 1, null );
                    }
                    else //如果最后一个取出的音频输出帧为无语音活动，就根据直接取出。
                    {
                        m_VAjbPt.GetOneByteFrame( System.currentTimeMillis(), m_TmpHTInt3Pt, m_TmpByte3Pt, 0, m_TmpByte3Pt.length, m_TmpHTLong3Pt, 1, null );
                    }
                    p_VideoOutputFrameTimeStamp = m_TmpHTInt3Pt.m_Val;
                    p_VideoOutputFramePt = m_TmpByte3Pt;
                    p_VideoOutputFrameLen = m_TmpHTLong3Pt.m_Val;

                    if( p_VideoOutputFrameLen > 0 ) //如果视频输出帧为有图像活动。
                    {
                        Log.i( m_CurClsNameStrPt, "从视频自适应抖动缓冲器取出一个有图像活动的视频输出帧。时间戳：" + p_VideoOutputFrameTimeStamp + "，数据长度：" + p_VideoOutputFrameLen + "。" );
                    }
                    else //如果视频输出帧为无图像活动。
                    {
                        Log.i( m_CurClsNameStrPt, "从视频自适应抖动缓冲器取出一个无图像活动的视频输出帧。时间戳：" + p_VideoOutputFrameTimeStamp + "，数据长度：" + p_VideoOutputFrameLen + "。" );
                    }
                }

                break;
            }
        }

        //写入视频输出帧。
        if( p_VideoOutputFrameLen > 0 ) //如果视频输出帧为有图像活动。
        {
            //读取视频输出帧对应音频输出帧的时间戳。
            p_VideoOutputFrameAudioOutputFrameTimeStamp = ( p_VideoOutputFramePt[0] & 0xFF ) + ( ( p_VideoOutputFramePt[1] & 0xFF ) << 8 ) + ( ( p_VideoOutputFramePt[2] & 0xFF ) << 16 ) + ( ( p_VideoOutputFramePt[3] & 0xFF ) << 24 );

            if( YU12VideoOutputFramePt != null ) //如果要使用YU12格式视频输出帧。
            {
                //读取视频输出帧宽度。
                YU12VideoInputFrameWidthPt.m_Val = ( p_VideoOutputFramePt[4] & 0xFF ) + ( ( p_VideoOutputFramePt[5] & 0xFF ) << 8 ) + ( ( p_VideoOutputFramePt[6] & 0xFF ) << 16 ) + ( ( p_VideoOutputFramePt[7] & 0xFF ) << 24 );
                //读取视频输出帧高度。
                YU12VideoInputFrameHeightPt.m_Val = ( p_VideoOutputFramePt[8] & 0xFF ) + ( ( p_VideoOutputFramePt[9] & 0xFF ) << 8 ) + ( ( p_VideoOutputFramePt[10] & 0xFF ) << 16 ) + ( ( p_VideoOutputFramePt[11] & 0xFF ) << 24 );

                if( p_VideoOutputFrameLen - 4 - 4 - 4 != ( long )( YU12VideoInputFrameWidthPt.m_Val * YU12VideoInputFrameHeightPt.m_Val * 3 / 2 ) )
                {
                    Log.e( m_CurClsNameStrPt, "视频输出帧的数据长度不等于YU12格式的数据长度。视频输出帧：" + ( p_VideoOutputFrameLen - 4 - 4 - 4 ) + "，YU12格式：" + ( YU12VideoInputFrameWidthPt.m_Val * YU12VideoInputFrameHeightPt.m_Val * 3 / 2 ) + "。" );
                    YU12VideoInputFrameWidthPt.m_Val = 0;
                    YU12VideoInputFrameHeightPt.m_Val = 0;
                    return;
                }

                //写入YU12格式视频输出帧。
                System.arraycopy( p_VideoOutputFramePt, 4 + 4 + 4, YU12VideoOutputFramePt, 0, ( int )( p_VideoOutputFrameLen - 4 - 4 - 4 ) );
            }
            else //如果要使用已编码格式视频输出帧。
            {
                if( p_VideoOutputFrameLen - 4 > VideoOutputFrameLen.m_Val )
                {
                    VideoOutputFrameLen.m_Val = 0;
                    Log.e( m_CurClsNameStrPt, "视频输出帧的数据长度已超过已编码格式的数据长度。视频输出帧：" + ( p_VideoOutputFrameLen - 4 ) + "，已编码格式：" + VideoOutputFrameLen.m_Val + "。" );
                    return;
                }

                //写入已编码格式视频输出帧。
                System.arraycopy( p_VideoOutputFramePt, 4, EncoderVideoOutputFramePt, 0, ( int )( p_VideoOutputFrameLen - 4 ) );
                VideoOutputFrameLen.m_Val = p_VideoOutputFrameLen - 4;
            }
        }
        else if( p_VideoOutputFrameLen == 0 ) //如果视频输出帧为无图像活动。
        {
            if( YU12VideoOutputFramePt != null ) //如果要使用YU12格式视频输出帧。
            {
                VideoOutputFrameLen.m_Val = 0;
            }
            else //如果要使用已编码格式视频输出帧。
            {
                VideoOutputFrameLen.m_Val = 0;
            }
        }
    }

    //用户定义的获取YU12格式视频输出帧函数，在解码完一个已编码视频输出帧时回调一次。注意：本函数不是在媒体处理线程中执行的，而是在视频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音视频输出帧不同步。
    @Override public void UserGetYU12VideoOutputFrame( byte YU12VideoOutputFramePt[], int YU12VideoOutputFrameWidth, int YU12VideoOutputFrameHeight )
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
    View m_LyotActivityWebRtcNsxViewPt; //存放WebRtc定点版噪音抑制器设置布局控件的内存指针。
    View m_LyotActivityWebRtcNsViewPt; //存放WebRtc浮点版噪音抑制器设置布局控件的内存指针。
    View m_LyotActivitySpeexPprocOtherViewPt; //存放Speex预处理器的其他功能设置布局控件的内存指针。
    View m_LyotActivitySpeexCodecViewPt; //存放Speex编解码器设置布局控件的内存指针。
    View m_LyotActivityOpenH264CodecViewPt; //存放OpenH264编解码器设置布局控件的内存指针。
    View m_LyotActivitySystemH264CodecViewPt; //存放系统自带H264编解码器设置布局控件的内存指针。
    View m_LyotActivityAjbViewPt; //存放音频自适应抖动缓冲器设置布局控件的内存指针。
    View m_LyotActivityCurViewPt; //存放当前界面布局控件的内存指针。

    MainActivity m_MainActivityPt; //存放主界面类对象的内存指针。
    MyMediaProcThread m_MyMediaProcThreadPt; //存放媒体处理线程类对象的内存指针。
    MainActivityHandler m_MainActivityHandlerPt; //存放主界面消息处理类对象的内存指针。

    HTSurfaceView m_VideoInputPreviewSurfaceViewPt; //存放视频输入预览SurfaceView控件的内存指针。
    HTSurfaceView m_VideoOutputDisplaySurfaceViewPt; //存放视频输出显示SurfaceView控件的内存指针。

    String m_ExternalDirFullAbsPathStrPt; //存放扩展目录完整绝对路径字符串的内存指针。

    //Activity创建消息。
    @Override protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Log.i( m_CurClsNameStrPt, "onCreate" );

        //创建布局。
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
        m_LyotActivityOpenH264CodecViewPt = layoutInflater.inflate( R.layout.activity_openh264codec, null );
        m_LyotActivitySystemH264CodecViewPt = layoutInflater.inflate( R.layout.activity_systemh264codec, null );
        m_LyotActivityAjbViewPt = layoutInflater.inflate( R.layout.activity_ajb, null );

        setContentView( m_LyotActivityMainViewPt ); //设置界面的内容为主界面。
        m_LyotActivityCurViewPt = m_LyotActivityMainViewPt;

        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseEffectSuperRadioBtn ) ).performClick(); //默认效果等级：超。
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseBitrateSuperRadioBtn ) ).performClick(); //默认比特率等级：超。

        //请求权限。
        MediaProcThread.RequestPermissions( this, 1, 1, 1, 1, 1, 1, 1, 1 );

        //设置主界面类对象。
        m_MainActivityPt = this;

        //初始化消息处理类对象。
        m_MainActivityHandlerPt = new MainActivityHandler();
        m_MainActivityHandlerPt.m_MainActivityPt = m_MainActivityPt;

        //获取AppID。
        ( ( TextView ) m_LyotActivityMainViewPt.findViewById( R.id.AppID ) ).setText( "AppID：" + getApplicationContext().getPackageName() );

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

        //初始化音频输出音量控件。
        {
            SeekBar p_AudioOutputVolumePt = ( SeekBar ) m_LyotActivityMainViewPt.findViewById( R.id.SystemAudioOutputVolume ); //获取音频输出音量控件的内存指针。
            AudioManager p_AudioManagerPt = ( AudioManager ) getSystemService( Context.AUDIO_SERVICE ); //获取音频服务的内存指针。

            p_AudioOutputVolumePt.setMax( p_AudioManagerPt.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL ) ); //设置音频输出音量控件的最大值。
            p_AudioOutputVolumePt.setProgress( p_AudioManagerPt.getStreamVolume( AudioManager.STREAM_VOICE_CALL ) ); //设置音频输出音量控件的当前值。

            //设置音频输出音量控件的变化消息监听器。
            p_AudioOutputVolumePt.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
            {
                @Override public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
                {
                    ( ( AudioManager ) getSystemService( Context.AUDIO_SERVICE ) ).setStreamVolume( AudioManager.STREAM_VOICE_CALL, progress, AudioManager.FLAG_PLAY_SOUND );
                    p_AudioOutputVolumePt.setProgress( p_AudioManagerPt.getStreamVolume( AudioManager.STREAM_VOICE_CALL ) );
                }

                @Override public void onStartTrackingTouch( SeekBar seekBar )
                {

                }

                @Override public void onStopTrackingTouch( SeekBar seekBar )
                {

                }
            } );

            //设置系统音量的变化消息监听器。
            IntentFilter p_VolumeChangedActionIntentFilterPt = new IntentFilter();
            p_VolumeChangedActionIntentFilterPt.addAction( "android.media.VOLUME_CHANGED_ACTION" );
            registerReceiver( new BroadcastReceiver()
            {
                @Override public void onReceive( Context context, Intent intent )
                {
                    p_AudioOutputVolumePt.setProgress( p_AudioManagerPt.getStreamVolume( AudioManager.STREAM_VOICE_CALL ) );
                }
            }, p_VolumeChangedActionIntentFilterPt );
        }

        //添加视频输入预览SurfaceView的回调函数。
        m_VideoInputPreviewSurfaceViewPt = ( ( HTSurfaceView )findViewById( R.id.VideoInputPreviewSurfaceView ) );
        m_VideoInputPreviewSurfaceViewPt.getHolder().setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
        m_VideoInputPreviewSurfaceViewPt.getHolder().addCallback( new SurfaceHolder.Callback()
        {
            @Override public void surfaceCreated( SurfaceHolder holder )
            {
                Log.i( m_CurClsNameStrPt, "VideoInputPreviewSurfaceView Created" );
                if( m_MyMediaProcThreadPt != null && m_MyMediaProcThreadPt.m_VideoInputPt.m_IsUseVideoInput != 0 && m_MyMediaProcThreadPt.m_RunFlag == MediaProcThread.RUN_FLAG_PROC ) //如果SurfaceView已经重新创建，且媒体处理线程已经启动，且要使用视频输入，并处于初始化完毕正在循环处理帧。
                {
                    m_MyMediaProcThreadPt.RequireExit( 3, 1 ); //请求重启媒体处理线程，来保证正常的视频输入，否则视频输入会中断。
                }
            }

            @Override public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
            {
                Log.i( m_CurClsNameStrPt, "VideoInputPreviewSurfaceView Changed" );
            }

            @Override public void surfaceDestroyed( SurfaceHolder holder )
            {
                Log.i( m_CurClsNameStrPt, "VideoInputPreviewSurfaceView Destroyed" );
            }
        } );

        //添加视频输出显示SurfaceView的回调函数。
        m_VideoOutputDisplaySurfaceViewPt = ( ( HTSurfaceView )findViewById( R.id.VideoOutputDisplaySurfaceView ) );
        m_VideoOutputDisplaySurfaceViewPt.getHolder().setType( SurfaceHolder.SURFACE_TYPE_NORMAL );
        m_VideoOutputDisplaySurfaceViewPt.getHolder().addCallback( new SurfaceHolder.Callback()
        {
            @Override public void surfaceCreated( SurfaceHolder holder )
            {
                Log.i( m_CurClsNameStrPt, "VideoOutputDisplaySurfaceView Created" );
            }

            @Override public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
            {
                Log.i( m_CurClsNameStrPt, "VideoOutputDisplaySurfaceView Changed" );
            }

            @Override public void surfaceDestroyed( SurfaceHolder holder )
            {
                Log.i( m_CurClsNameStrPt, "VideoOutputDisplaySurfaceView Destroyed" );
            }
        } );

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

    //Activity从遮挡恢复消息。
    @Override public void onStart()
    {
        super.onStart();
        Log.i( m_CurClsNameStrPt, "onStart" );
    }

    //Activity从后台恢复消息。
    @Override public void onRestart()
    {
        super.onRestart();
        Log.i( m_CurClsNameStrPt, "onRestart" );
    }

    //Activity恢复运行消息。
    @Override public void onResume()
    {
        super.onResume();
        Log.i( m_CurClsNameStrPt, "onResume" );
    }

    //Activity被遮挡消息。
    @Override public void onPause()
    {
        super.onPause();
        Log.i( m_CurClsNameStrPt, "onPause" );
    }

    //Activity转入后台消息。
    @Override public void onStop()
    {
        super.onStop();
        Log.i( m_CurClsNameStrPt, "onStop" );
    }

    //Activity销毁消息。
    @Override public void onDestroy()
    {
        super.onDestroy();
        Log.i( m_CurClsNameStrPt, "onDestroy" );
    }

    //Activity返回键消息。
    @Override public void onBackPressed()
    {
        Log.i( m_CurClsNameStrPt, "onBackPressed" );

        if( m_LyotActivityCurViewPt == m_LyotActivityMainViewPt )
        {
            Log.i( m_CurClsNameStrPt, "用户在主界面按下返回键，本软件退出。" );
            if( m_MyMediaProcThreadPt != null )
            {
                Log.i( m_CurClsNameStrPt, "开始请求并等待媒体处理线程退出。" );
                m_MyMediaProcThreadPt.RequireExit( 1, 1 );
                Log.i( m_CurClsNameStrPt, "结束请求并等待媒体处理线程退出。" );
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
                ( m_LyotActivityCurViewPt == m_LyotActivityOpenH264CodecViewPt ) ||
                ( m_LyotActivityCurViewPt == m_LyotActivitySystemH264CodecViewPt ) ||
                ( m_LyotActivityCurViewPt == m_LyotActivityAjbViewPt ) )
        {
            this.OnClickWebRtcAecSettingOk( null );
        }
    }

    //界面横竖屏切换消息。
    @Override public void onConfigurationChanged( Configuration newConfig )
    {
        super.onConfigurationChanged( newConfig );

        if( m_MyMediaProcThreadPt != null && m_MyMediaProcThreadPt.m_VideoInputPt.m_IsUseVideoInput != 0 && m_MyMediaProcThreadPt.m_RunFlag == MediaProcThread.RUN_FLAG_PROC ) //如果SurfaceView已经重新创建，且媒体处理线程已经启动，且要使用视频输入，并处于初始化完毕正在循环处理帧。
        {
            m_MyMediaProcThreadPt.SetIsUseVideoInput(
                    ( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseVideoTalkbackRadioBtn ) ).isChecked() ) ? 1 :
                            ( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseAudioVideoTalkbackRadioBtn ) ).isChecked() ) ? 1 : 0,
                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate12RadioBtn ) ).isChecked() ) ? 12 :
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate15RadioBtn ) ).isChecked() ) ? 15 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate24RadioBtn ) ).isChecked() ) ? 24 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate30RadioBtn ) ).isChecked() ) ? 30 : 0,
                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize120_160RadioBtn ) ).isChecked() ) ? 120 :
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize240_320RadioBtn ) ).isChecked() ) ? 240 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize480_640RadioBtn ) ).isChecked() ) ? 480 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize960_1280RadioBtn ) ).isChecked() ) ? 960 : 0,
                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize120_160RadioBtn ) ).isChecked() ) ? 160 :
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize240_320RadioBtn ) ).isChecked() ) ? 320 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize480_640RadioBtn ) ).isChecked() ) ? 640 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize960_1280RadioBtn ) ).isChecked() ) ? 1280 : 0,
                    getWindowManager().getDefaultDisplay().getRotation() * 90,
                    m_VideoInputPreviewSurfaceViewPt
            );

            m_MyMediaProcThreadPt.RequireExit( 3, 1 ); //请求重启媒体处理线程，来保证正常的视频输入，否则视频输入会中断。
        }
    }

    //使用音频按钮。
    public void OnUseAudio( View BtnPt )
    {
        if( m_MyMediaProcThreadPt != null )
        {
            m_MyMediaProcThreadPt.m_AudioInputPt.m_IsUseAudioInput = 1;
            m_MyMediaProcThreadPt.m_AudioOutputPt.m_IsUseAudioOutput = 1;
            m_MyMediaProcThreadPt.m_VideoInputPt.m_IsUseVideoInput = 0;
            m_MyMediaProcThreadPt.m_VideoOutputPt.m_IsUseVideoOutput = 0;

            if( m_MyMediaProcThreadPt.m_RunFlag > MediaProcThread.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
            {
                m_MyMediaProcThreadPt.RequireExit( 3, 1 ); //请求重启并阻塞等待。
            }
        }
    }

    //使用视频按钮。
    public void OnUseVideo( View BtnPt )
    {
        if( m_MyMediaProcThreadPt != null )
        {
            m_MyMediaProcThreadPt.m_AudioInputPt.m_IsUseAudioInput = 0;
            m_MyMediaProcThreadPt.m_AudioOutputPt.m_IsUseAudioOutput = 0;
            m_MyMediaProcThreadPt.m_VideoInputPt.m_IsUseVideoInput = 1;
            m_MyMediaProcThreadPt.m_VideoOutputPt.m_IsUseVideoOutput = 1;

            if( m_MyMediaProcThreadPt.m_RunFlag > MediaProcThread.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
            {
                m_MyMediaProcThreadPt.RequireExit( 3, 1 ); //请求重启并阻塞等待。
            }
        }
    }

    //使用音视频按钮。
    public void OnUseAudioVideo( View BtnPt )
    {
        if( m_MyMediaProcThreadPt != null )
        {
            m_MyMediaProcThreadPt.m_AudioInputPt.m_IsUseAudioInput = 1;
            m_MyMediaProcThreadPt.m_AudioOutputPt.m_IsUseAudioOutput = 1;
            m_MyMediaProcThreadPt.m_VideoInputPt.m_IsUseVideoInput = 1;
            m_MyMediaProcThreadPt.m_VideoOutputPt.m_IsUseVideoOutput = 1;

            if( m_MyMediaProcThreadPt.m_RunFlag > MediaProcThread.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
            {
                m_MyMediaProcThreadPt.RequireExit( 3, 1 ); //请求重启并阻塞等待。
            }
        }
    }

    //使用扬声器按钮。
    public void OnUseSpeaker( View BtnPt )
    {
        if( m_MyMediaProcThreadPt != null )
        {
            m_MyMediaProcThreadPt.SetAudioOutputUseDevice( 0, 0 );

            if( m_MyMediaProcThreadPt.m_AudioOutputPt.m_IsUseAudioOutput != 0 && m_MyMediaProcThreadPt.m_RunFlag > MediaProcThread.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
            {
                m_MyMediaProcThreadPt.RequireExit( 3, 1 ); //请求重启并阻塞等待。
            }
        }
    }

    //使用听筒按钮。
    public void OnUseHeadset( View BtnPt )
    {
        if( m_MyMediaProcThreadPt != null )
        {
            m_MyMediaProcThreadPt.SetAudioOutputUseDevice( 1, 0 );

            if( m_MyMediaProcThreadPt.m_AudioOutputPt.m_IsUseAudioOutput != 0 && m_MyMediaProcThreadPt.m_RunFlag > MediaProcThread.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
            {
                m_MyMediaProcThreadPt.RequireExit( 3, 1 ); //请求重启并阻塞等待。
            }
        }
    }

    //使用前置摄像头按钮。
    public void OnUseFrontCamere( View BtnPt )
    {
        if( m_MyMediaProcThreadPt != null )
        {
            m_MyMediaProcThreadPt.SetVideoInputUseDevice( 0, -1, -1 );

            if( m_MyMediaProcThreadPt.m_VideoInputPt.m_IsUseVideoInput != 0 && m_MyMediaProcThreadPt.m_RunFlag > MediaProcThread.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
            {
                m_MyMediaProcThreadPt.RequireExit( 3, 1 ); //请求重启并阻塞等待。
            }
        }
    }

    //使用后置摄像头按钮。
    public void OnUseBackCamere( View BtnPt )
    {
        if( m_MyMediaProcThreadPt != null )
        {
            m_MyMediaProcThreadPt.SetVideoInputUseDevice( 1, -1, -1 );

            if( m_MyMediaProcThreadPt.m_VideoInputPt.m_IsUseVideoInput != 0 && m_MyMediaProcThreadPt.m_RunFlag > MediaProcThread.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
            {
                m_MyMediaProcThreadPt.RequireExit( 3, 1 ); //请求重启并阻塞等待。
            }
        }
    }

    //音频输入设备静音按钮。
    public void OnAudioInputIsMute( View BtnPt )
    {
        if( m_MyMediaProcThreadPt != null )
        {
            m_MyMediaProcThreadPt.SetAudioInputIsMute( ( ( ( CheckBox ) m_LyotActivityMainViewPt.findViewById( R.id.AudioInputIsMuteCheckBox ) ).isChecked() ) ? 1 : 0 );
        }
    }

    //音频输出设备静音按钮。
    public void OnAudioOutputIsMute( View BtnPt )
    {
        if( m_MyMediaProcThreadPt != null )
        {
            m_MyMediaProcThreadPt.SetAudioOutputIsMute( ( ( ( CheckBox ) m_LyotActivityMainViewPt.findViewById( R.id.AudioOutputIsMuteCheckBox ) ).isChecked() ) ? 1 : 0 );
        }
    }

    //视频输入设备黑屏按钮。
    public void OnVideoInputIsBlack( View BtnPt )
    {
        if( m_MyMediaProcThreadPt != null )
        {
            m_MyMediaProcThreadPt.SetVideoInputIsBlack( ( ( ( CheckBox ) m_LyotActivityMainViewPt.findViewById( R.id.VideoInputIsBlackCheckBox ) ).isChecked() ) ? 1 : 0 );
        }
    }

    //视频输出设备黑屏按钮。
    public void OnVideoOutputIsBlack( View BtnPt )
    {
        if( m_MyMediaProcThreadPt != null )
        {
            m_MyMediaProcThreadPt.SetVideoOutputIsBlack( ( ( ( CheckBox ) m_LyotActivityMainViewPt.findViewById( R.id.VideoOutputIsBlackCheckBox ) ).isChecked() ) ? 1 : 0 );
        }
    }

    //创建服务器或连接服务器按钮。
    public void OnClickCreateSrvrAndConnectSrvr( View BtnPt )
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            if( m_MyMediaProcThreadPt == null ) //如果媒体处理线程还没有启动。
            {
                Log.i( m_CurClsNameStrPt, "开始启动媒体处理线程。" );

                //创建并初始化媒体处理线程类对象。
                {
                    m_MyMediaProcThreadPt = new MyMediaProcThread( getApplicationContext() ); //创建媒体处理线程类对象。

                    if( BtnPt.getId() == R.id.CreateSrvrBtn )
                    {
                        m_MyMediaProcThreadPt.m_IsCreateSrvrOrClnt = 1; //标记创建服务端接受客户端。
                    }
                    else if( BtnPt.getId() == R.id.ConnectSrvrBtn )
                    {
                        m_MyMediaProcThreadPt.m_IsCreateSrvrOrClnt = 0; //标记创建客户端连接服务端。
                    }

                    m_MyMediaProcThreadPt.m_MainActivityHandlerPt = m_MainActivityHandlerPt; //设置主界面消息处理类对象的内存指针。

                    //设置IP地址字符串、端口。
                    m_MyMediaProcThreadPt.m_IPAddrStrPt = ( ( EditText ) m_LyotActivityMainViewPt.findViewById( R.id.IPAddrEdit ) ).getText().toString();
                    m_MyMediaProcThreadPt.m_PortStrPt = ( ( EditText ) m_LyotActivityMainViewPt.findViewById( R.id.PortEdit ) ).getText().toString();

                    //判断是否使用什么传输协议。
                    if( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseTcpPrtclRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.m_UseWhatXfrPrtcl = 0;
                    }
                    else
                    {
                        m_MyMediaProcThreadPt.m_UseWhatXfrPrtcl = 1;
                    }

                    //判断是否使用链表。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseLnkLstRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.m_UseWhatRecvOutputFrame = 0;
                    }

                    //判断是否使用自己设计的音频自适应抖动缓冲器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAjbRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.m_UseWhatRecvOutputFrame = 1;

                        try
                        {
                            m_MyMediaProcThreadPt.m_AAjbMinNeedBufFrameCnt = Integer.parseInt( ( ( TextView ) m_LyotActivityAjbViewPt.findViewById( R.id.AAjbMinNeedBufFrameCnt ) ).getText().toString() );
                            m_MyMediaProcThreadPt.m_AAjbMaxNeedBufFrameCnt = Integer.parseInt( ( ( TextView ) m_LyotActivityAjbViewPt.findViewById( R.id.AAjbMaxNeedBufFrameCnt ) ).getText().toString() );
                            m_MyMediaProcThreadPt.m_AAjbAdaptSensitivity = Float.parseFloat( ( ( TextView ) m_LyotActivityAjbViewPt.findViewById( R.id.AAjbAdaptSensitivity ) ).getText().toString() );

                            m_MyMediaProcThreadPt.m_VAjbMinNeedBufFrameCnt = Integer.parseInt( ( ( TextView ) m_LyotActivityAjbViewPt.findViewById( R.id.VAjbMinNeedBufFrameCnt ) ).getText().toString() );
                            m_MyMediaProcThreadPt.m_VAjbMaxNeedBufFrameCnt = Integer.parseInt( ( ( TextView ) m_LyotActivityAjbViewPt.findViewById( R.id.VAjbMaxNeedBufFrameCnt ) ).getText().toString() );
                            m_MyMediaProcThreadPt.m_VAjbAdaptSensitivity = Float.parseFloat( ( ( TextView ) m_LyotActivityAjbViewPt.findViewById( R.id.VAjbAdaptSensitivity ) ).getText().toString() );
                        }
                        catch( NumberFormatException e )
                        {
                            Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                            break out;
                        }
                    }

                    //判断是否保存设置到文件。
                    if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveSettingToFileCheckBox ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetIsSaveSettingToFile( 1, m_ExternalDirFullAbsPathStrPt + "/Setting.txt" );
                    }
                    else
                    {
                        m_MyMediaProcThreadPt.SetIsSaveSettingToFile( 0, null );
                    }

                    //判断是否打印Logcat日志，并显示Toast。
                    if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsPrintLogcatShowToastCheckBox ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetIsPrintLogcatShowToast( 1, 1, this );
                    }
                    else
                    {
                        m_MyMediaProcThreadPt.SetIsPrintLogcatShowToast( 0, 0, null );
                    }

                    //判断是否使用唤醒锁。
                    if( ( ( CheckBox ) m_MainActivityPt.m_LyotActivitySettingViewPt.findViewById( R.id.IsUseWakeLockCheckBox ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetIsUseWakeLock( 1 );
                    }
                    else
                    {
                        m_MyMediaProcThreadPt.SetIsUseWakeLock( 0 );
                    }

                    //判断是否使用音频输入。
                    m_MyMediaProcThreadPt.SetIsUseAudioInput(
                            ( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseAudioTalkbackRadioBtn ) ).isChecked() ) ? 1 :
                                    ( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseAudioVideoTalkbackRadioBtn ) ).isChecked() ) ? 1 : 0,
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate8000RadioBtn ) ).isChecked() ) ? 8000 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate16000RadioBtn ) ).isChecked() ) ? 16000 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate32000RadioBtn ) ).isChecked() ) ? 32000 :
                                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate48000RadioBtn ) ).isChecked() ) ? 48000 : 0,
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioFrameLen10msRadioBtn ) ).isChecked() ) ? 10 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioFrameLen20msRadioBtn ) ).isChecked() ) ? 20 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioFrameLen30msRadioBtn ) ).isChecked() ) ? 30 : 0 );

                    //判断音频输入是否使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器。
                    if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSystemAecNsAgcCheckBox ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetAudioInputIsUseSystemAecNsAgc( 1 );
                    }
                    else
                    {
                        m_MyMediaProcThreadPt.SetAudioInputIsUseSystemAecNsAgc( 0 );
                    }

                    //判断音频输入是否不使用声学回音消除器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseNoAecRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetAudioInputUseNoAec();
                    }

                    //判断音频输入是否使用Speex声学回音消除器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexAecRadioBtn ) ).isChecked() )
                    {
                        try
                        {
                            m_MyMediaProcThreadPt.SetAudioInputUseSpeexAec(
                                    Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecFilterLenEdit ) ).getText().toString() ),
                                    ( ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsUseRecCheckBox ) ).isChecked() ) ? 1 : 0,
                                    Float.parseFloat( ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoMultipleEdit ) ).getText().toString() ),
                                    Float.parseFloat( ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoContEdit ) ).getText().toString() ),
                                    Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesEdit ) ).getText().toString() ),
                                    Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesActEdit ) ).getText().toString() ),
                                    ( ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCheckBox ) ).isChecked() ) ? 1 : 0,
                                    m_ExternalDirFullAbsPathStrPt + "/SpeexAecMem"
                            );
                        }
                        catch( NumberFormatException e )
                        {
                            Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                            break out;
                        }
                    }

                    //判断音频输入是否使用WebRtc定点版声学回音消除器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcAecmRadioBtn ) ).isChecked() )
                    {
                        try
                        {
                            m_MyMediaProcThreadPt.SetAudioInputUseWebRtcAecm(
                                    ( ( ( CheckBox ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCheckBox ) ).isChecked() ) ? 1 : 0,
                                    Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmEchoModeEdit ) ).getText().toString() ),
                                    Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmDelayEdit ) ).getText().toString() )
                            );
                        }
                        catch( NumberFormatException e )
                        {
                            Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                            break out;
                        }
                    }

                    //判断音频输入是否使用WebRtc浮点版声学回音消除器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcAecRadioBtn ) ).isChecked() )
                    {
                        try
                        {
                            m_MyMediaProcThreadPt.SetAudioInputUseWebRtcAec(
                                    Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecEchoModeEdit ) ).getText().toString() ),
                                    Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecDelayEdit ) ).getText().toString() ),
                                    ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgnosticModeCheckBox ) ).isChecked() ) ? 1 : 0,
                                    ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCheckBox ) ).isChecked() ) ? 1 : 0,
                                    ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).isChecked() ) ? 1 : 0,
                                    ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCheckBox ) ).isChecked() ) ? 1 : 0,
                                    ( ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCheckBox ) ).isChecked() ) ? 1 : 0,
                                    m_ExternalDirFullAbsPathStrPt + "/WebRtcAecMem"
                            );
                        }
                        catch( NumberFormatException e )
                        {
                            Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                            break out;
                        }
                    }

                    //判断音频输入是否使用SpeexWebRtc三重声学回音消除器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexWebRtcAecRadioBtn ) ).isChecked() )
                    {
                        try
                        {
                            m_MyMediaProcThreadPt.SetAudioInputUseSpeexWebRtcAec(
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
                                    ( ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCheckBox ) ).isChecked() ) ? 1 : 0,
                                    ( ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseSameRoomAecCheckBox ) ).isChecked() ) ? 1 : 0,
                                    Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdit ) ).getText().toString() )
                            );
                        }
                        catch( NumberFormatException e )
                        {
                            Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                            break out;
                        }
                    }

                    //判断音频输入是否不使用噪音抑制器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseNoNsRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetAudioInputUseNoNs();
                    }

                    //判断音频输入是否使用Speex预处理器的噪音抑制。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexPprocNsRadioBtn ) ).isChecked() )
                    {
                        try
                        {
                            m_MyMediaProcThreadPt.SetAudioInputUseSpeexPprocNs(
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

                    //判断音频输入是否使用WebRtc定点版噪音抑制器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcNsxRadioBtn ) ).isChecked() )
                    {
                        try
                        {
                            m_MyMediaProcThreadPt.SetAudioInputUseWebRtcNsx(
                                    Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcNsxViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdit ) ).getText().toString() )
                            );
                        }
                        catch( NumberFormatException e )
                        {
                            Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                            break out;
                        }
                    }

                    //判断音频输入是否使用WebRtc浮点版噪音抑制器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcNsRadioBtn ) ).isChecked() )
                    {
                        try
                        {
                            m_MyMediaProcThreadPt.SetAudioInputUseWebRtcNs(
                                    Integer.parseInt( ( ( TextView ) m_LyotActivityWebRtcNsViewPt.findViewById( R.id.WebRtcNsPolicyModeEdit ) ).getText().toString() )
                            );
                        }
                        catch( NumberFormatException e )
                        {
                            Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                            break out;
                        }
                    }

                    //判断音频输入是否使用RNNoise噪音抑制器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseRNNoiseRadioBtn ) ).isChecked() )
                    {
                        try
                        {
                            m_MyMediaProcThreadPt.SetAudioInputUseRNNoise();
                        }
                        catch( NumberFormatException e )
                        {
                            Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                            break out;
                        }
                    }

                    //判断音频输入是否使用Speex预处理器的其他功能。
                    if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSpeexPprocOtherCheckBox ) ).isChecked() )
                    {
                        try
                        {
                            m_MyMediaProcThreadPt.SetAudioInputIsUseSpeexPprocOther(
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
                    else
                    {
                        m_MyMediaProcThreadPt.SetAudioInputIsUseSpeexPprocOther( 0, 0, 0, 0, 0, 0, 0, 0, 0 );
                    }

                    //判断音频输入是否使用PCM原始数据。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UsePcmRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetAudioInputUsePcm();
                    }

                    //判断音频输入是否使用Speex编码器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexCodecRadioBtn ) ).isChecked() )
                    {
                        try
                        {
                            m_MyMediaProcThreadPt.SetAudioInputUseSpeexEncoder(
                                    ( ( ( RadioButton ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderUseCbrRadioBtn ) ).isChecked() ) ? 0 : 1,
                                    Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderQualityEdit ) ).getText().toString() ),
                                    Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderComplexityEdit ) ).getText().toString() ),
                                    Integer.parseInt( ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderPlcExpectedLossRateEdit ) ).getText().toString() )
                            );
                        }
                        catch( NumberFormatException e )
                        {
                            Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                            break out;
                        }
                    }

                    //判断音频输入是否使用Opus编码器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseOpusCodecRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetAudioInputUseOpusEncoder();
                    }

                    //判断音频输入是否保存音频到文件。
                    if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveAudioToFileCheckBox ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetAudioInputIsSaveAudioToFile(
                                1,
                                m_ExternalDirFullAbsPathStrPt + "/AudioInput.wav",
                                m_ExternalDirFullAbsPathStrPt + "/AudioResult.wav"
                        );
                    }
                    else
                    {
                        m_MyMediaProcThreadPt.SetAudioInputIsSaveAudioToFile( 0, null, null );
                    }

                    //判断音频输入是否绘制音频波形到Surface。
                    m_MyMediaProcThreadPt.SetAudioInputIsDrawAudioOscilloToSurface(
                            ( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveDrawAudioOscilloToSurfaceCheckBox ) ).isChecked() ) ? 1 : 0,
                            ( ( SurfaceView )findViewById( R.id.AudioInputOscilloSurfaceView ) ),
                            ( ( SurfaceView )findViewById( R.id.AudioResultOscilloSurfaceView ) )
                    );

                    //判断音频输入是否静音。
                    m_MyMediaProcThreadPt.SetAudioInputIsMute( ( ( ( CheckBox ) m_LyotActivityMainViewPt.findViewById( R.id.AudioInputIsMuteCheckBox ) ).isChecked() ) ? 1 : 0 );

                    //判断是否使用音频输出。
                    m_MyMediaProcThreadPt.SetIsUseAudioOutput(
                            ( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseAudioTalkbackRadioBtn ) ).isChecked() ) ? 1 :
                                    ( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseAudioVideoTalkbackRadioBtn ) ).isChecked() ) ? 1 : 0,
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate8000RadioBtn ) ).isChecked() ) ? 8000 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate16000RadioBtn ) ).isChecked() ) ? 16000 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate32000RadioBtn ) ).isChecked() ) ? 32000 :
                                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate48000RadioBtn ) ).isChecked() ) ? 48000 : 0,
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioFrameLen10msRadioBtn ) ).isChecked() ) ? 10 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioFrameLen20msRadioBtn ) ).isChecked() ) ? 20 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioFrameLen30msRadioBtn ) ).isChecked() ) ? 30 : 0 );

                    //判断音频输出是否使用PCM原始数据。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UsePcmRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetAudioOutputUsePcm();
                    }

                    //判断音频输出是否使用Speex解码器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexCodecRadioBtn ) ).isChecked() )
                    {
                        try
                        {
                            m_MyMediaProcThreadPt.SetAudioOutputUseSpeexDecoder(
                                    ( ( ( CheckBox ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecIsUsePerceptualEnhancementCheckBox ) ).isChecked() ) ? 1 : 0
                            );
                        }
                        catch( NumberFormatException e )
                        {
                            Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                            break out;
                        }
                    }

                    //判断音频输出是否使用Opus解码器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseOpusCodecRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetAudioOutputUseOpusDecoder();
                    }

                    //判断使用的音频输出设备。
                    if( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseSpeakerRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetAudioOutputUseDevice( 0, 0 );
                    }
                    else
                    {
                        m_MyMediaProcThreadPt.SetAudioOutputUseDevice( 1, 0 );
                    }

                    //判断音频输出是否保存音频到文件。
                    if( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveAudioToFileCheckBox ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetAudioOutputIsSaveAudioToFile(
                                1,
                                m_ExternalDirFullAbsPathStrPt + "/AudioOutput.wav"
                        );
                    }
                    else
                    {
                        m_MyMediaProcThreadPt.SetAudioOutputIsSaveAudioToFile( 0, null );
                    }

                    //判断音频输出是否绘制音频波形到Surface。
                    m_MyMediaProcThreadPt.SetAudioOutputIsDrawAudioOscilloToSurface(
                            ( ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveDrawAudioOscilloToSurfaceCheckBox ) ).isChecked() ) ? 1 : 0,
                            ( ( SurfaceView )findViewById( R.id.AudioOutputOscilloSurfaceView ) )
                    );

                    //判断音频输出是否静音。
                    m_MyMediaProcThreadPt.SetAudioOutputIsMute( ( ( ( CheckBox ) m_LyotActivityMainViewPt.findViewById( R.id.AudioOutputIsMuteCheckBox ) ).isChecked() ) ? 1 : 0 );

                    //判断是否使用视频输入。
                    m_MyMediaProcThreadPt.SetIsUseVideoInput(
                            ( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseVideoTalkbackRadioBtn ) ).isChecked() ) ? 1 :
                                    ( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseAudioVideoTalkbackRadioBtn ) ).isChecked() ) ? 1 : 0,
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate12RadioBtn ) ).isChecked() ) ? 12 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate15RadioBtn ) ).isChecked() ) ? 15 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate24RadioBtn ) ).isChecked() ) ? 24 :
                                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate30RadioBtn ) ).isChecked() ) ? 30 : 0,
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize120_160RadioBtn ) ).isChecked() ) ? 120 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize240_320RadioBtn ) ).isChecked() ) ? 240 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize480_640RadioBtn ) ).isChecked() ) ? 480 :
                                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize960_1280RadioBtn ) ).isChecked() ) ? 960 : 0,
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize120_160RadioBtn ) ).isChecked() ) ? 160 :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize240_320RadioBtn ) ).isChecked() ) ? 320 :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize480_640RadioBtn ) ).isChecked() ) ? 640 :
                                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize960_1280RadioBtn ) ).isChecked() ) ? 1280 : 0,
                            getWindowManager().getDefaultDisplay().getRotation() * 90,
                            m_VideoInputPreviewSurfaceViewPt
                    );

                    //判断视频输入是否使用YU12原始数据。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseYU12RadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetVideoInputUseYU12();
                    }

                    //判断视频输入是否使用OpenH264编码器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseOpenH264CodecRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetVideoInputUseOpenH264Encoder(
                                Integer.parseInt( ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderVideoTypeEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderEncodedBitrateEdit ) ).getText().toString() ) * 1024 * 8,
                                Integer.parseInt( ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderBitrateControlModeEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderIDRFrameIntvlEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderComplexityEdit ) ).getText().toString() )
                        );
                    }

                    //判断视频输入是否使用系统自带H264编码器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSystemH264CodecRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetVideoInputUseSystemH264Encoder(
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderEncodedBitrateEdit ) ).getText().toString() ) * 1024 * 8,
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderBitrateControlModeEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderIDRFrameIntvlEdit ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderComplexityEdit ) ).getText().toString() )
                        );
                    }

                    //判断使用的视频输入设备。
                    m_MyMediaProcThreadPt.SetVideoInputUseDevice( ( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseFrontCamereRadioBtn ) ).isChecked() ) ? 0 : 1,
                                                                  -1, -1 );

                    //判断视频输入是否黑屏。
                    m_MyMediaProcThreadPt.SetVideoInputIsBlack( ( ( ( CheckBox ) m_LyotActivityMainViewPt.findViewById( R.id.VideoInputIsBlackCheckBox ) ).isChecked() ) ? 1 : 0 );

                    //判断是否使用视频输出。
                    m_MyMediaProcThreadPt.SetIsUseVideoOutput(
                            ( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseVideoTalkbackRadioBtn ) ).isChecked() ) ? 1 :
                                    ( ( ( RadioButton ) m_LyotActivityMainViewPt.findViewById( R.id.UseAudioVideoTalkbackRadioBtn ) ).isChecked() ) ? 1 : 0,
                            m_VideoOutputDisplaySurfaceViewPt,
                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseDisplayScale1_0RadioBtn ) ).isChecked() ) ? 1.0f :
                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseDisplayScale1_5RadioBtn ) ).isChecked() ) ? 1.5f :
                                            ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseDisplayScale2_0RadioBtn ) ).isChecked() ) ? 2.0f :
                                                    ( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseDisplayScale3_0RadioBtn ) ).isChecked() ) ? 3.0f : 1.0f
                    );

                    //判断视频输出是否使用YU12原始数据。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseYU12RadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetVideoOutputUseYU12();
                    }

                    //判断视频输出是否使用OpenH264解码器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseOpenH264CodecRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetVideoOutputUseOpenH264Decoder( 0 );
                    }

                    //判断视频输出是否使用系统自带H264解码器。
                    if( ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSystemH264CodecRadioBtn ) ).isChecked() )
                    {
                        m_MyMediaProcThreadPt.SetVideoOutputUseSystemH264Decoder();
                    }

                    //判断视频输出是否黑屏。
                    m_MyMediaProcThreadPt.SetVideoOutputIsBlack( ( ( ( CheckBox ) m_LyotActivityMainViewPt.findViewById( R.id.VideoOutputIsBlackCheckBox ) ).isChecked() ) ? 1 : 0 );
                }

                m_MyMediaProcThreadPt.start(); //启动媒体处理线程。

                Log.i( m_CurClsNameStrPt, "启动媒体处理线程完毕。" );
            }
            else
            {
                Log.i( m_CurClsNameStrPt, "开始请求并等待媒体处理线程退出。" );
                m_MyMediaProcThreadPt.RequireExit( 1, 1 );
                Log.i( m_CurClsNameStrPt, "结束请求并等待媒体处理线程退出。" );
            }

            p_Result = 0;

            break out;
        }

        if( p_Result != 0 ) //如果媒体处理线程启动失败。
        {
            m_MyMediaProcThreadPt = null;
        }
    }

    //主界面视频输入预览或视频输出显示SurfaceView按钮。
    public void onClickVideoSurfaceView( View BtnPt )
    {
        if( ( ( LinearLayout )BtnPt.getParent().getParent() ).getOrientation() == LinearLayout.HORIZONTAL )
        {
            ( ( LinearLayout )BtnPt.getParent().getParent() ).setOrientation( LinearLayout.VERTICAL );
        }
        else
        {
            ( ( LinearLayout )BtnPt.getParent().getParent() ).setOrientation( LinearLayout.HORIZONTAL );
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
        startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "https://github.com/cyz7758520/Android_audio_talkback_demo_program" ) ) );
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
        String p_pclSpeexAecMemoryFullPath = m_ExternalDirFullAbsPathStrPt + "/SpeexAecMem";
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
        String p_pclWebRtcAecMemoryFullPath = m_ExternalDirFullAbsPathStrPt + "/WebRtcAecMem";
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

    //OpenH264编解码器设置按钮。
    public void OnClickOpenH264CodecSetting( View BtnPt )
    {
        setContentView( m_LyotActivityOpenH264CodecViewPt );
        m_LyotActivityCurViewPt = m_LyotActivityOpenH264CodecViewPt;
    }

    //OpenH264编解码器设置界面的确定按钮。
    public void OnOpenH264CodecSettingOkClick( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //系统自带H264编解码器设置按钮。
    public void OnClickSystemH264CodecSetting( View BtnPt )
    {
        setContentView( m_LyotActivitySystemH264CodecViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySystemH264CodecViewPt;
    }

    //系统自带H264编解码器设置界面的确定按钮。
    public void OnSystemH264CodecSettingOkClick( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //音频自适应抖动缓冲器设置按钮。
    public void OnClickAjbSetting( View BtnPt )
    {
        setContentView( m_LyotActivityAjbViewPt );
        m_LyotActivityCurViewPt = m_LyotActivityAjbViewPt;
    }

    //音频自适应抖动缓冲器设置界面的确定按钮。
    public void OnClickAjbSettingOk( View BtnPt )
    {
        setContentView( m_LyotActivitySettingViewPt );
        m_LyotActivityCurViewPt = m_LyotActivitySettingViewPt;
    }

    //效果等级：低。
    public void OnClickUseEffectLowRadioBtn( View BtnPt )
    {
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseEffectLowRadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate8000RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioFrameLen20msRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSystemAecNsAgcCheckBox ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcAecmRadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexPprocNsRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSpeexPprocOtherCheckBox ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexCodecRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveAudioToFileCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveDrawAudioOscilloToSurfaceCheckBox ) ).setChecked( false );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate12RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize120_160RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseDisplayScale1_0RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseOpenH264CodecRadioBtn ) ).setChecked( true );

        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecFilterLenEdit ) ).setText( "500" );
        ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsUseRecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoMultipleEdit ) ).setText( "3.0" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoContEdit ) ).setText( "0.65" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesEdit ) ).setText( "-32768" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesActEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCheckBox ) ).setChecked( false );

        ( ( CheckBox ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmEchoModeEdit ) ).setText( "4" );
        ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmDelayEdit ) ).setText( "0" );

        ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecEchoModeEdit ) ).setText( "2" );
        ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecDelayEdit ) ).setText( "0" );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgnosticModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).setChecked( false );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCheckBox ) ).setChecked( false );

        ( ( RadioButton ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmRadioBtn ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdit ) ).setText( "500" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMultipleEdit ) ).setText( "1.0" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoContEdit ) ).setText( "0.6" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdit ) ).setText( "-32768" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdit ) ).setText( "4" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdit ) ).setText( "0" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdit ) ).setText( "2" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdit ) ).setText( "0" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).setChecked( false );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseSameRoomAecCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdit ) ).setText( "380" );

        ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseNsCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocNoiseSupesEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseDereverbCheckBox ) ).setChecked( true );

        ( ( TextView ) m_LyotActivityWebRtcNsxViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdit ) ).setText( "3" );

        ( ( TextView ) m_LyotActivityWebRtcNsViewPt.findViewById( R.id.WebRtcNsPolicyModeEdit ) ).setText( "3" );

        ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseVadCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbStartEdit ) ).setText( "95" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbContEdit ) ).setText( "95" );
        ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseAgcCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcLevelEdit ) ).setText( "30000" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcIncrementEdit ) ).setText( "10" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcDecrementEdit ) ).setText( "-30000" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcMaxGainEdit ) ).setText( "25" );

        ( ( RadioButton ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderUseCbrRadioBtn ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderComplexityEdit ) ).setText( "1" );
        ( ( CheckBox ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecIsUsePerceptualEnhancementCheckBox ) ).setChecked( true );

        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderVideoTypeEdit ) ).setText( "0" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderBitrateControlModeEdit ) ).setText( "3" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderIDRFrameIntvlEdit ) ).setText( "12" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderComplexityEdit ) ).setText( "0" );

        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderBitrateControlModeEdit ) ).setText( "1" );
        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderIDRFrameIntvlEdit ) ).setText( "1" );
        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderComplexityEdit ) ).setText( "0" );
    }

    //效果等级：中。
    public void OnClickUseEffectMidRadioBtn( View BtnPt )
    {
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseEffectMidRadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate16000RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioFrameLen20msRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSystemAecNsAgcCheckBox ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcAecRadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcNsxRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSpeexPprocOtherCheckBox ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexCodecRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveAudioToFileCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveDrawAudioOscilloToSurfaceCheckBox ) ).setChecked( false );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate15RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize240_320RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseDisplayScale1_0RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseOpenH264CodecRadioBtn ) ).setChecked( true );

        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecFilterLenEdit ) ).setText( "500" );
        ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsUseRecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoMultipleEdit ) ).setText( "3.0" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoContEdit ) ).setText( "0.65" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesEdit ) ).setText( "-32768" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesActEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCheckBox ) ).setChecked( false );

        ( ( CheckBox ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmEchoModeEdit ) ).setText( "4" );
        ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmDelayEdit ) ).setText( "0" );

        ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecEchoModeEdit ) ).setText( "2" );
        ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecDelayEdit ) ).setText( "0" );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgnosticModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).setChecked( false );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCheckBox ) ).setChecked( false );

        ( ( RadioButton ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeWebRtcAecmWebRtcAecRadioBtn ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdit ) ).setText( "500" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMultipleEdit ) ).setText( "1.0" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoContEdit ) ).setText( "0.6" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdit ) ).setText( "-32768" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdit ) ).setText( "4" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdit ) ).setText( "0" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdit ) ).setText( "2" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdit ) ).setText( "0" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).setChecked( false );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseSameRoomAecCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdit ) ).setText( "380" );

        ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseNsCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocNoiseSupesEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseDereverbCheckBox ) ).setChecked( true );

        ( ( TextView ) m_LyotActivityWebRtcNsxViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdit ) ).setText( "3" );

        ( ( TextView ) m_LyotActivityWebRtcNsViewPt.findViewById( R.id.WebRtcNsPolicyModeEdit ) ).setText( "3" );

        ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseVadCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbStartEdit ) ).setText( "95" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbContEdit ) ).setText( "95" );
        ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseAgcCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcLevelEdit ) ).setText( "30000" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcIncrementEdit ) ).setText( "10" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcDecrementEdit ) ).setText( "-30000" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcMaxGainEdit ) ).setText( "25" );

        ( ( RadioButton ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderUseCbrRadioBtn ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderComplexityEdit ) ).setText( "4" );
        ( ( CheckBox ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecIsUsePerceptualEnhancementCheckBox ) ).setChecked( true );

        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderVideoTypeEdit ) ).setText( "0" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderBitrateControlModeEdit ) ).setText( "3" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderIDRFrameIntvlEdit ) ).setText( "15" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderComplexityEdit ) ).setText( "0" );

        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderBitrateControlModeEdit ) ).setText( "1" );
        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderIDRFrameIntvlEdit ) ).setText( "1" );
        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderComplexityEdit ) ).setText( "1" );
    }

    //效果等级：高。
    public void OnClickUseEffectHighRadioBtn( View BtnPt )
    {
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseEffectHighRadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate16000RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioFrameLen20msRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSystemAecNsAgcCheckBox ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexWebRtcAecRadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseWebRtcNsRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSpeexPprocOtherCheckBox ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexCodecRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveAudioToFileCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveDrawAudioOscilloToSurfaceCheckBox ) ).setChecked( false );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate15RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize480_640RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseDisplayScale1_0RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseOpenH264CodecRadioBtn ) ).setChecked( true );

        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecFilterLenEdit ) ).setText( "500" );
        ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsUseRecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoMultipleEdit ) ).setText( "3.0" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoContEdit ) ).setText( "0.65" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesEdit ) ).setText( "-32768" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesActEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCheckBox ) ).setChecked( false );

        ( ( CheckBox ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmEchoModeEdit ) ).setText( "4" );
        ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmDelayEdit ) ).setText( "0" );

        ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecEchoModeEdit ) ).setText( "2" );
        ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecDelayEdit ) ).setText( "0" );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgnosticModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).setChecked( false );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCheckBox ) ).setChecked( false );

        ( ( RadioButton ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRadioBtn ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdit ) ).setText( "500" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMultipleEdit ) ).setText( "1.0" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoContEdit ) ).setText( "0.6" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdit ) ).setText( "-32768" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdit ) ).setText( "4" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdit ) ).setText( "0" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdit ) ).setText( "2" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdit ) ).setText( "0" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).setChecked( false );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseSameRoomAecCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdit ) ).setText( "380" );

        ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseNsCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocNoiseSupesEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseDereverbCheckBox ) ).setChecked( true );

        ( ( TextView ) m_LyotActivityWebRtcNsxViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdit ) ).setText( "3" );

        ( ( TextView ) m_LyotActivityWebRtcNsViewPt.findViewById( R.id.WebRtcNsPolicyModeEdit ) ).setText( "3" );

        ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseVadCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbStartEdit ) ).setText( "95" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbContEdit ) ).setText( "95" );
        ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseAgcCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcLevelEdit ) ).setText( "30000" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcIncrementEdit ) ).setText( "10" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcDecrementEdit ) ).setText( "-30000" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcMaxGainEdit ) ).setText( "25" );

        ( ( RadioButton ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderUseVbrRadioBtn ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderComplexityEdit ) ).setText( "8" );
        ( ( CheckBox ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecIsUsePerceptualEnhancementCheckBox ) ).setChecked( true );

        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderVideoTypeEdit ) ).setText( "0" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderBitrateControlModeEdit ) ).setText( "3" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderIDRFrameIntvlEdit ) ).setText( "15" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderComplexityEdit ) ).setText( "0" );

        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderBitrateControlModeEdit ) ).setText( "1" );
        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderIDRFrameIntvlEdit ) ).setText( "1" );
        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderComplexityEdit ) ).setText( "2" );
    }

    //效果等级：超。
    public void OnClickUseEffectSuperRadioBtn( View BtnPt )
    {
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseEffectSuperRadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate16000RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioFrameLen20msRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSystemAecNsAgcCheckBox ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexWebRtcAecRadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseRNNoiseRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSpeexPprocOtherCheckBox ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexCodecRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveAudioToFileCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveDrawAudioOscilloToSurfaceCheckBox ) ).setChecked( false );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate24RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize480_640RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseDisplayScale1_0RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseOpenH264CodecRadioBtn ) ).setChecked( true );

        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecFilterLenEdit ) ).setText( "500" );
        ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsUseRecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoMultipleEdit ) ).setText( "3.0" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoContEdit ) ).setText( "0.65" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesEdit ) ).setText( "-32768" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesActEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCheckBox ) ).setChecked( false );

        ( ( CheckBox ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmEchoModeEdit ) ).setText( "4" );
        ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmDelayEdit ) ).setText( "0" );

        ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecEchoModeEdit ) ).setText( "2" );
        ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecDelayEdit ) ).setText( "0" );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgnosticModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).setChecked( false );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCheckBox ) ).setChecked( false );

        ( ( RadioButton ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRadioBtn ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdit ) ).setText( "500" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMultipleEdit ) ).setText( "1.0" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoContEdit ) ).setText( "0.6" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdit ) ).setText( "-32768" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdit ) ).setText( "4" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdit ) ).setText( "0" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdit ) ).setText( "2" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdit ) ).setText( "0" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).setChecked( false );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseSameRoomAecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdit ) ).setText( "380" );

        ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseNsCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocNoiseSupesEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseDereverbCheckBox ) ).setChecked( true );

        ( ( TextView ) m_LyotActivityWebRtcNsxViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdit ) ).setText( "3" );

        ( ( TextView ) m_LyotActivityWebRtcNsViewPt.findViewById( R.id.WebRtcNsPolicyModeEdit ) ).setText( "3" );

        ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseVadCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbStartEdit ) ).setText( "95" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbContEdit ) ).setText( "95" );
        ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseAgcCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcLevelEdit ) ).setText( "30000" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcIncrementEdit ) ).setText( "10" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcDecrementEdit ) ).setText( "-30000" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcMaxGainEdit ) ).setText( "25" );

        ( ( RadioButton ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderUseVbrRadioBtn ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderComplexityEdit ) ).setText( "10" );
        ( ( CheckBox ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecIsUsePerceptualEnhancementCheckBox ) ).setChecked( true );

        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderVideoTypeEdit ) ).setText( "0" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderBitrateControlModeEdit ) ).setText( "3" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderIDRFrameIntvlEdit ) ).setText( "24" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderComplexityEdit ) ).setText( "1" );

        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderBitrateControlModeEdit ) ).setText( "1" );
        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderIDRFrameIntvlEdit ) ).setText( "1" );
        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderComplexityEdit ) ).setText( "2" );
    }

    //效果等级：特。
    public void OnClickUseEffectPremiumRadioBtn( View BtnPt )
    {
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseEffectPremiumRadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioSamplingRate32000RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseAudioFrameLen20msRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSystemAecNsAgcCheckBox ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexWebRtcAecRadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseRNNoiseRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsUseSpeexPprocOtherCheckBox ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseSpeexCodecRadioBtn ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveAudioToFileCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySettingViewPt.findViewById( R.id.IsSaveDrawAudioOscilloToSurfaceCheckBox ) ).setChecked( false );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoSamplingRate30RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseVideoFrameSize960_1280RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseDisplayScale1_0RadioBtn ) ).setChecked( true );
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseOpenH264CodecRadioBtn ) ).setChecked( true );

        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecFilterLenEdit ) ).setText( "500" );
        ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsUseRecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoMultipleEdit ) ).setText( "3.0" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoContEdit ) ).setText( "0.65" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesEdit ) ).setText( "-32768" );
        ( ( TextView ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecEchoSupesActEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexAecViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCheckBox ) ).setChecked( false );

        ( ( CheckBox ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmEchoModeEdit ) ).setText( "4" );
        ( ( TextView ) m_LyotActivityWebRtcAecmViewPt.findViewById( R.id.WebRtcAecmDelayEdit ) ).setText( "0" );

        ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecEchoModeEdit ) ).setText( "2" );
        ( ( TextView ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecDelayEdit ) ).setText( "0" );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgnosticModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).setChecked( false );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivityWebRtcAecViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCheckBox ) ).setChecked( false );

        ( ( RadioButton ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRadioBtn ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdit ) ).setText( "500" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMultipleEdit ) ).setText( "1.0" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoContEdit ) ).setText( "0.6" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdit ) ).setText( "-32768" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCheckBox ) ).setChecked( false );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdit ) ).setText( "4" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdit ) ).setText( "0" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdit ) ).setText( "2" );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdit ) ).setText( "0" );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCheckBox ) ).setChecked( false );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCheckBox ) ).setChecked( true );
        ( ( CheckBox ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseSameRoomAecCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexWebRtcAecViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdit ) ).setText( "380" );

        ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseNsCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocNoiseSupesEdit ) ).setText( "-32768" );
        ( ( CheckBox ) m_LyotActivitySpeexPprocNsViewPt.findViewById( R.id.SpeexPprocIsUseDereverbCheckBox ) ).setChecked( true );

        ( ( TextView ) m_LyotActivityWebRtcNsxViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdit ) ).setText( "3" );

        ( ( TextView ) m_LyotActivityWebRtcNsViewPt.findViewById( R.id.WebRtcNsPolicyModeEdit ) ).setText( "3" );

        ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseVadCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbStartEdit ) ).setText( "95" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocVadProbContEdit ) ).setText( "95" );
        ( ( CheckBox ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocIsUseAgcCheckBox ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcLevelEdit ) ).setText( "30000" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcIncrementEdit ) ).setText( "10" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcDecrementEdit ) ).setText( "-30000" );
        ( ( TextView ) m_LyotActivitySpeexPprocOtherViewPt.findViewById( R.id.SpeexPprocAgcMaxGainEdit ) ).setText( "25" );

        ( ( RadioButton ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderUseVbrRadioBtn ) ).setChecked( true );
        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderComplexityEdit ) ).setText( "10" );
        ( ( CheckBox ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecIsUsePerceptualEnhancementCheckBox ) ).setChecked( true );

        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderVideoTypeEdit ) ).setText( "0" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderBitrateControlModeEdit ) ).setText( "3" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderIDRFrameIntvlEdit ) ).setText( "30" );
        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderComplexityEdit ) ).setText( "2" );

        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderBitrateControlModeEdit ) ).setText( "1" );
        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderIDRFrameIntvlEdit ) ).setText( "1" );
        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderComplexityEdit ) ).setText( "2" );
    }

    //比特率等级：低。
    public void OnClickUseBitrateLowRadioBtn( View BtnPt )
    {
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseBitrateLowRadioBtn ) ).setChecked( true );

        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderQualityEdit ) ).setText( "1" );
        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderPlcExpectedLossRateEdit ) ).setText( "1" );

        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderEncodedBitrateEdit ) ).setText( "10" );

        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderEncodedBitrateEdit ) ).setText( "10" );
    }

    //比特率等级：中。
    public void OnClickUseBitrateMidRadioBtn( View BtnPt )
    {
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseBitrateMidRadioBtn ) ).setChecked( true );

        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderQualityEdit ) ).setText( "4" );
        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderPlcExpectedLossRateEdit ) ).setText( "40" );

        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderEncodedBitrateEdit ) ).setText( "20" );

        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderEncodedBitrateEdit ) ).setText( "20" );
    }

    //比特率等级：高。
    public void OnClickUseBitrateHighRadioBtn( View BtnPt )
    {
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseBitrateHighRadioBtn ) ).setChecked( true );

        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderQualityEdit ) ).setText( "8" );
        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderPlcExpectedLossRateEdit ) ).setText( "80" );

        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderEncodedBitrateEdit ) ).setText( "40" );

        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderEncodedBitrateEdit ) ).setText( "40" );
    }

    //比特率等级：超。
    public void OnClickUseBitrateSuperRadioBtn( View BtnPt )
    {
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseBitrateSuperRadioBtn ) ).setChecked( true );

        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderQualityEdit ) ).setText( "10" );
        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderPlcExpectedLossRateEdit ) ).setText( "100" );

        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderEncodedBitrateEdit ) ).setText( "60" );

        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderEncodedBitrateEdit ) ).setText( "60" );
    }

    //比特率等级：特。
    public void OnClickUseBitratePremiumRadioBtn( View BtnPt )
    {
        ( ( RadioButton ) m_LyotActivitySettingViewPt.findViewById( R.id.UseBitratePremiumRadioBtn ) ).setChecked( true );

        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderQualityEdit ) ).setText( "10" );
        ( ( TextView ) m_LyotActivitySpeexCodecViewPt.findViewById( R.id.SpeexCodecEncoderPlcExpectedLossRateEdit ) ).setText( "100" );

        ( ( TextView ) m_LyotActivityOpenH264CodecViewPt.findViewById( R.id.OpenH264EncoderEncodedBitrateEdit ) ).setText( "80" );

        ( ( TextView ) m_LyotActivitySystemH264CodecViewPt.findViewById( R.id.SystemH264EncoderEncodedBitrateEdit ) ).setText( "80" );
    }
}