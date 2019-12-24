package com.example.Android_audio_talkback_demo_program;

import android.Manifest;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
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

//初始化线程类。
class InitThread extends Thread
{
    String m_pclCurClassNameString = this.getClass().getSimpleName(); //当前类名称字符串类对象的内存指针。

    View m_pclLayoutActivityMainView; //主界面布局控件的内存指针。

    public void run()
    {
        String p_pclString = null;

        //获取本机IP地址。
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
        ( ( EditText ) m_pclLayoutActivityMainView.findViewById( R.id.IPAddressEdit ) ).setText( p_pclString );

        //设置端口控件的内容。
        ( ( EditText ) m_pclLayoutActivityMainView.findViewById( R.id.PortEdit ) ).setText( "12345" );
    }
}

//主界面消息处理类。
class MainActivityHandler extends Handler
{
    String m_pclCurClassNameString = this.getClass().getSimpleName(); //当前类名称字符串类对象的内存指针。

    MainActivity clMainActivity;

    public void handleMessage( Message clMessage )
    {
        if( clMessage.what == 1 ) //如果是音频处理线程正常退出的消息。
        {
            clMainActivity.m_pclMyAudioProcessThread = null;

            ( ( EditText ) clMainActivity.findViewById( R.id.IPAddressEdit ) ).setEnabled( true ); //设置IP地址控件为可用。
            ( ( EditText ) clMainActivity.findViewById( R.id.PortEdit ) ).setEnabled( true ); //设置端口控件为可用。
            ( ( Button ) clMainActivity.findViewById( R.id.CreateServerButton ) ).setText( "创建服务端" ); //设置创建服务端按钮的内容为“创建服务端”。
            ( ( Button ) clMainActivity.findViewById( R.id.ConnectServerButton ) ).setEnabled( true ); //设置连接服务端按钮为可用。
            ( ( Button ) clMainActivity.findViewById( R.id.ConnectServerButton ) ).setText( "连接服务端" ); //设置连接服务端按钮的内容为“连接服务端”。
            ( ( Button ) clMainActivity.findViewById( R.id.CreateServerButton ) ).setEnabled( true ); //设置创建服务端按钮为可用。
            ( ( Button ) clMainActivity.findViewById( R.id.SettingButton ) ).setEnabled( true ); //设置设置按钮为可用。
        }
        if( clMessage.what == 2 ) //如果是显示日志的消息。
        {
            LinearLayout clLogLinearLayout = clMainActivity.m_pclLayoutActivityMainView.findViewById( R.id.LogLinearLayout );
            TextView clTempTextView = new TextView( clMainActivity );
            clTempTextView.setText( ( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) ).format( new Date() ) + "：" + clMessage.obj );
            clLogLinearLayout.addView( clTempTextView );
        }
    }
}

//音频处理线程类。
class MyAudioProcessThread extends AudioProcessThread
{
    String m_pclIPAddressString; //存放IP地址字符串类对象的内存指针。
    int m_i32Port; //存放端口号。
    Handler m_pclMainActivityHandler; //存放主界面消息处理类对象的内存指针。

    byte m_i8IsCreateServerOrClient; //存放创建服务端或者客户端标记，为1表示创建服务端，为0表示创建客户端。
    ServerSocket m_pclServerSocket; //存放TCP协议服务端套接字类对象的内存指针。
    Socket m_pclClientSocket; //存放TCP协议客户端套接字类对象的内存指针。
    long m_i64LastPacketSendTime; //存放最后一个数据包的发送时间，用于判断连接是否中断。
    long m_i64LastPacketRecvTime; //存放最后一个数据包的接收时间，用于判断连接是否中断。

    int m_i32LastSendInputFrameIsActive; //存放最后一个发送的输入帧有无语音活动，为1表示有语音活动，为0表示无语音活动。
    short m_i16PacketPrereadSize; //存放本次数据包的预读长度。
    int m_i32SendInputFrameTimeStamp; //存放发送输入帧的时间戳。
    int m_i32RecvOutputFrameTimeStamp; //存放接收输出帧的时间戳。
    byte m_i8IsRecvExitPacket; //存放是否接收到退出包，为0表示否，为1表示是。

    int m_i32UseWhatRecvOutputFrame; //存放使用什么接收输出帧，为0表示链表，为1表示自适应抖动缓冲器。
    LinkedList< byte[] > m_clRecvOutputFrameLinkedList; //存放接收输出帧链表类对象的内存指针。
    Ajb m_pclAjb; //存放自适应抖动缓冲器类对象的内存指针。
    int m_i32AjbMinNeedBufferFrameCount; //存放自适应抖动缓冲器的最小需缓冲帧数量，单位个。
    int m_i32AjbMaxNeedBufferFrameCount; //存放自适应抖动缓冲器的最大需缓冲帧数量，单位个。
    byte m_i8AjbAdaptiveSensitivity; //存放自适应抖动缓冲器的自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0,127]。

    byte m_pszi8TempData[]; //存放临时数据。
    byte m_pszi8TempData2[]; //存放临时数据。
    short m_pszi16TempData[]; //存放临时数据。
    int m_i32Temp;
    int m_i32Temp2;
    long m_i64Temp;

    //用户定义的初始化函数，在本线程刚启动时调用一次，返回值表示是否成功，为0表示成功，为非0表示失败。
    public long UserInit()
    {
        long p_i64Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            if( m_i8IsCreateServerOrClient == 1 ) //如果是创建本地TCP协议服务端套接字接受远端TCP协议客户端套接字的连接。
            {
                if( m_pclServerSocket == null ) //如果还没有创建TCP协议服务端套接字。
                {
                    try
                    {
                        m_pclServerSocket = new ServerSocket(); //创建本地TCP协议服务端套接字类对象。
                        m_pclServerSocket.setReuseAddress( true ); //设置重用地址。
                        m_pclServerSocket.bind( new InetSocketAddress( m_pclIPAddressString, m_i32Port ), 1 ); //将本地TCP协议服务端套接字绑定本地节点的IP地址。
                        m_pclServerSocket.setSoTimeout( 500 ); //设置accept()函数的超时时间。

                        String p_pclInfoString = "创建本地TCP协议服务端套接字[" + m_pclServerSocket.getInetAddress().getHostAddress() + ":" + m_pclServerSocket.getLocalPort() + "]成功。";
                        Log.i( m_pclCurrentClassNameString, p_pclInfoString );
                        Message p_pclMessage = new Message();
                        p_pclMessage.what = 2;
                        p_pclMessage.obj = p_pclInfoString;
                        m_pclMainActivityHandler.sendMessage( p_pclMessage );
                    }
                    catch( IOException e )
                    {
                        String p_pclInfoString = "创建本地TCP协议服务端套接字[" + m_pclIPAddressString + ":" + m_i32Port + "]失败。原因：" + e.toString();
                        Log.e( m_pclCurrentClassNameString, p_pclInfoString );
                        Message p_pclMessage = new Message();
                        p_pclMessage.what = 2;
                        p_pclMessage.obj = p_pclInfoString;
                        m_pclMainActivityHandler.sendMessage( p_pclMessage );
                        break out;
                    }
                }

                while( true )
                {
                    //接受远端TCP协议客户端套接字的连接。
                    try
                    {
                        m_pclClientSocket = m_pclServerSocket.accept();
                    }
                    catch( IOException e )
                    {

                    }

                    if( m_pclClientSocket != null ) //如果成功接受了远端TCP协议客户端套接字的连接，就开始传输数据。
                    {
                        try
                        {
                            m_pclServerSocket.close(); //关闭本地TCP协议服务端套接字，防止还有其他远端TCP协议客户端套接字继续连接。
                        }
                        catch( IOException e )
                        {
                        }
                        m_pclServerSocket = null;

                        String p_pclInfoString = "接受了远端TCP协议客户端套接字[" + m_pclClientSocket.getInetAddress().getHostAddress() + ":" + m_pclClientSocket.getPort() + "]与本地TCP协议客户端套接字[" + m_pclClientSocket.getLocalAddress().getHostAddress() + ":" + m_pclClientSocket.getLocalPort() + "]的连接。";
                        Log.i( m_pclCurrentClassNameString, p_pclInfoString );
                        Message p_pclMessage = new Message();
                        p_pclMessage.what = 2;
                        p_pclMessage.obj = p_pclInfoString;
                        m_pclMainActivityHandler.sendMessage( p_pclMessage );
                        break;
                    }

                    if( m_i32ExitFlag != 0 ) //如果本线程接收到退出请求。
                    {
                        Log.i( m_pclCurrentClassNameString, "本线程接收到退出请求，开始准备退出。" );
                        break out;
                    }
                }
            }
            else if( m_i8IsCreateServerOrClient == 0 ) //如果是创建本地TCP协议客户端套接字连接远端TCP协议服务端套接字。
            {
                try
                {
                    m_pclClientSocket = new Socket(); //创建本地TCP协议客户端套接字类对象。
                    m_pclClientSocket.connect( new InetSocketAddress( m_pclIPAddressString, m_i32Port ), 5000 ); //连接指定的IP地址和端口号，超时时间为5秒。

                    String p_pclInfoString = "创建本地TCP协议客户端套接字[" + m_pclClientSocket.getLocalAddress().getHostAddress() + ":" + m_pclClientSocket.getLocalPort() + "]与远端TCP协议服务端套接字[" + m_pclClientSocket.getInetAddress().getHostAddress() + ":" + m_pclClientSocket.getPort() + "]的连接成功。";
                    Log.i( m_pclCurrentClassNameString, p_pclInfoString );
                    Message p_pclMessage = new Message();
                    p_pclMessage.what = 2;
                    p_pclMessage.obj = p_pclInfoString;
                    m_pclMainActivityHandler.sendMessage( p_pclMessage );
                }
                catch( IOException e )
                {
                    String p_pclInfoString = " 创建本地TCP协议客户端套接字与远端TCP协议服务端套接字[" + m_pclIPAddressString + ":" + m_i32Port + "]的连接失败。原因：" + e.getMessage();
                    Log.i( m_pclCurrentClassNameString, p_pclInfoString );
                    Message p_pclMessage = new Message();
                    p_pclMessage.what = 2;
                    p_pclMessage.obj = p_pclInfoString;
                    m_pclMainActivityHandler.sendMessage( p_pclMessage );
                    break out;
                }
            }

            try
            {
                m_pclClientSocket.setTcpNoDelay( true ); //设置TCP协议客户端套接字的TCP_NODELAY选项为true。
            }
            catch( SocketException e )
            {
                break out;
            }

            switch( m_i32UseWhatRecvOutputFrame ) //使用什么接收输出帧。
            {
                case 0: //如果使用链表。
                {
                    m_clRecvOutputFrameLinkedList = new LinkedList< byte[] >(); //创建接收输出帧链表类对象。

                    Log.i( m_pclCurrentClassNameString, "创建接收输出帧链表类对象成功。" );

                    break;
                }
                case 1: //如果使用自适应抖动缓冲器。
                {
                    //初始化自适应抖动缓冲器类对象。
                    m_pclAjb = new Ajb();
                    m_i64Temp = m_pclAjb.Init( m_i32SamplingRate, ( int ) m_i64FrameLength, ( byte ) 1, ( byte ) 0, m_i32AjbMinNeedBufferFrameCount, m_i32AjbMaxNeedBufferFrameCount, m_i8AjbAdaptiveSensitivity );
                    if( m_i64Temp == 0 )
                    {
                        Log.i( m_pclCurrentClassNameString, "初始化自适应抖动缓冲器类对象成功。返回值：" + m_i64Temp );
                    }
                    else
                    {
                        Log.e( m_pclCurrentClassNameString, "初始化自适应抖动缓冲器类对象失败。返回值：" + m_i64Temp );
                        break out;
                    }

                    break;
                }
            }

            m_i64LastPacketRecvTime = m_i64LastPacketSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间和接收时间为当前时间。

            m_i32LastSendInputFrameIsActive = 0; //设置最后发送的一个音频帧为无语音活动。
            m_i16PacketPrereadSize = 0; //设置本次数据包的预读长度为0。
            m_i32SendInputFrameTimeStamp = 0; //设置发送音频数据的时间戳为0。
            m_i32RecvOutputFrameTimeStamp = 0; //设置接收音频数据的时间戳为0。
            m_i8IsRecvExitPacket = 0; //设置没有接收到退出包。
            if( m_pszi8TempData == null )
                m_pszi8TempData = new byte[( int ) m_i64FrameLength * 2 + 6]; //初始化存放临时数据的数组。
            if( m_pszi8TempData2 == null )
                m_pszi8TempData2 = new byte[( int ) m_i64FrameLength * 2 + 6]; //初始化存放临时数据的数组。
            if( m_pszi16TempData == null )
                m_pszi16TempData = new short[( int ) m_i64FrameLength]; //初始化存放临时数据的数组。

            String p_pclInfoString = "开始进行音频对讲。";
            Log.i( m_pclCurrentClassNameString, p_pclInfoString );
            Message p_pclMessage = new Message();
            p_pclMessage.what = 2;
            p_pclMessage.obj = p_pclInfoString;
            m_pclMainActivityHandler.sendMessage( p_pclMessage );

            p_i64Result = 0; //设置本函数执行成功。
        }

        return p_i64Result;
    }

    //用户定义的处理函数，在本线程运行时每隔1毫秒就调用一次，返回值表示是否成功，为0表示成功，为非0表示失败。
    public long UserProcess()
    {
        long p_i64Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            //接收远端发送过来的一个音频输出帧，并放入自适应抖动缓冲器。
            try
            {
                InputStream clInputStream = m_pclClientSocket.getInputStream();

                if( ( m_i16PacketPrereadSize == 0 ) && ( clInputStream.available() >= 2 ) ) //如果还没有接收预读长度，且客户端套接字可以接收到预读长度。
                {
                    //接收预读长度。
                    if( clInputStream.read( m_pszi8TempData, 0, 2 ) != 2 ) //如果接收到的预读长度的数据长度不为2。
                    {
                        Log.e( m_pclCurrentClassNameString, "接收到的预读长度的数据长度不为2。" );
                        break out;
                    }

                    //读取预读长度。
                    m_i16PacketPrereadSize = ( short )( ( m_pszi8TempData[0] & 0xFF ) + ( ( m_pszi8TempData[1] & 0xFF ) << 8 ) );
                    if( m_i16PacketPrereadSize == 0 ) //如果预读长度为0，表示这是一个心跳包，就更新一下时间即可。
                    {
                        m_i64LastPacketRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。
                        Log.i( m_pclCurrentClassNameString, "接收到一个心跳包。" );
                    }
                    else if( m_i16PacketPrereadSize == -1 ) //如果预读长度为0xFFFF，表示这是一个退出包。
                    {
                        m_i64LastPacketRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。
                        m_i8IsRecvExitPacket = 1; //设置已经接收到退出包。

                        String p_pclInfoString = "接收到一个退出包。";
                        Log.i( m_pclCurrentClassNameString, p_pclInfoString );
                        Message p_pclMessage = new Message();
                        p_pclMessage.what = 2;
                        p_pclMessage.obj = p_pclInfoString;
                        m_pclMainActivityHandler.sendMessage( p_pclMessage );

                        break out;
                    }
                    else if( m_i16PacketPrereadSize < 4 ) //如果预读长度小于4，表示没有时间戳。
                    {
                        Log.e( m_pclCurrentClassNameString, "接收到预读长度为" + m_i16PacketPrereadSize + "小于4，表示没有时间戳，无法继续接收。" );
                        break out;
                    }
                    else if( m_i16PacketPrereadSize > m_pszi8TempData.length ) //如果预读长度大于接收缓冲区的长度。
                    {
                        Log.e( m_pclCurrentClassNameString, "接收到预读长度大于接收缓冲区的长度，无法继续接收。" );
                        break out;
                    }
                }

                if( ( m_i16PacketPrereadSize != 0 ) && ( clInputStream.available() >= m_i16PacketPrereadSize ) ) //如果已经接收了预读长度，且该输出帧可以一次性接收完毕。
                {
                    //接收时间戳。
                    if( clInputStream.read( m_pszi8TempData, 0, 4 ) != 4 ) //如果接收时间戳失败。
                    {
                        Log.e( m_pclCurrentClassNameString, "接收到的时间戳的数据长度不为4。" );
                        break out;
                    }

                    //读取时间戳。
                    m_i32RecvOutputFrameTimeStamp = ( m_pszi8TempData[0] & 0xFF ) + ( ( m_pszi8TempData[1] & 0xFF ) << 8 ) + ( ( m_pszi8TempData[2] & 0xFF ) << 16 ) + ( ( m_pszi8TempData[3] & 0xFF ) << 24 );

                    //接收输出帧。
                    if( m_i16PacketPrereadSize > 4 ) //如果该输出帧为有语音活动。
                    {
                        if( clInputStream.read( m_pszi8TempData, 0, m_i16PacketPrereadSize - 4 ) != m_i16PacketPrereadSize - 4 ) //如果接收到的数据长度与预读长度-时间戳长度不同。
                        {
                            Log.e( m_pclCurrentClassNameString, "接收到的输出帧的数据长度与预读长度-时间戳长度不同。" );
                            break out;
                        }

                        if( ( m_i32UseWhatCodec == 0 ) && ( m_i16PacketPrereadSize - 4 != m_i64FrameLength * 2 ) ) //如果使用了PCM原始数据，但接收到的PCM格式输出帧的数据长度与帧的数据长度不同。
                        {
                            Log.e( m_pclCurrentClassNameString, "接收到的PCM格式输出帧的数据长度与帧的数据长度不同。" );
                            break out;
                        }
                    }

                    m_i64LastPacketRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。

                    //将输出帧放入链表或自适应抖动缓冲器。
                    switch( m_i32UseWhatRecvOutputFrame ) //使用什么接收输出帧。
                    {
                        case 0: //如果使用链表。
                        {
                            m_i32Temp = m_i16PacketPrereadSize - 4;
                            byte p_pszi8RecvOutputFrame[] = new byte[m_i32Temp];

                            for( m_i32Temp--; m_i32Temp >= 0; m_i32Temp-- )
                            {
                                p_pszi8RecvOutputFrame[m_i32Temp] = m_pszi8TempData[m_i32Temp];
                            }

                            m_clRecvOutputFrameLinkedList.addLast( p_pszi8RecvOutputFrame );

                            Log.i( m_pclCurrentClassNameString, "接收一个输出帧并放入链表成功。时间戳：" + m_i32RecvOutputFrameTimeStamp + "，总长度：" + m_i16PacketPrereadSize + "。" );

                            break;
                        }
                        case 1: //如果使用自适应抖动缓冲器。
                        {
                            if( m_i16PacketPrereadSize == 4 ) //如果该输出帧为无语音活动。
                            {
                                synchronized( m_pclAjb )
                                {
                                    m_pclAjb.PutOneByteFrame( m_i32RecvOutputFrameTimeStamp, null, 0 );
                                }
                            }
                            else //如果该输出帧为有语音活动。
                            {
                                synchronized( m_pclAjb )
                                {
                                    m_pclAjb.PutOneByteFrame( m_i32RecvOutputFrameTimeStamp, m_pszi8TempData, m_i16PacketPrereadSize - 4 );
                                }
                            }

                            Log.i( m_pclCurrentClassNameString, "接收一个输出帧并放入自适应抖动缓冲器成功。时间戳：" + m_i32RecvOutputFrameTimeStamp + "，总长度：" + m_i16PacketPrereadSize + "。" );

                            break;
                        }
                    }

                    m_i16PacketPrereadSize = 0; //清空预读长度，以便下一次接收新的数据包。
                }
            }
            catch( IOException e )
            {
                String p_pclInfoString = "接收一个输出帧失败。原因：" + e.getMessage();
                Log.e( m_pclCurrentClassNameString, p_pclInfoString );
                Message p_pclMessage = new Message();
                p_pclMessage.what = 2;
                p_pclMessage.obj = p_pclInfoString;
                m_pclMainActivityHandler.sendMessage( p_pclMessage );

                break out;
            }

            //发送心跳包。
            if( System.currentTimeMillis() - m_i64LastPacketSendTime >= 500 ) //如果超过500毫秒没有发送任何数据包，就发送一个心跳包。
            {
                //设置预读长度。
                m_pszi8TempData[0] = 0;
                m_pszi8TempData[1] = 0;

                try
                {
                    OutputStream clOutputStream = m_pclClientSocket.getOutputStream();
                    clOutputStream.write( m_pszi8TempData, 0, 2 );
                    clOutputStream.flush(); //防止出现Software caused connection abort异常。
                    m_i64LastPacketSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间。

                    String p_pclInfoString = "发送一个心跳包成功。";
                    Log.i( m_pclCurrentClassNameString, p_pclInfoString );
                    Message p_pclMessage = new Message();
                    p_pclMessage.what = 2;
                    p_pclMessage.obj = p_pclInfoString;
                    m_pclMainActivityHandler.sendMessage( p_pclMessage );
                }
                catch( IOException e )
                {
                    String p_pclInfoString = "发送一个心跳包失败。原因：" + e.getMessage();
                    Log.e( m_pclCurrentClassNameString, p_pclInfoString );
                    Message p_pclMessage = new Message();
                    p_pclMessage.what = 2;
                    p_pclMessage.obj = p_pclInfoString;
                    m_pclMainActivityHandler.sendMessage( p_pclMessage );

                    break out;
                }
            }

            //判断TCP协议套接字连接是否中断。
            if( System.currentTimeMillis() - m_i64LastPacketRecvTime > 2000 ) //如果超过2000毫秒没有接收任何数据包，就判定连接已经断开了。
            {
                String p_pclInfoString = "超过2000毫秒没有接收任何数据包，判定连接已经断开了。";
                Log.e( m_pclCurrentClassNameString, p_pclInfoString );
                Message p_pclMessage = new Message();
                p_pclMessage.what = 2;
                p_pclMessage.obj = p_pclInfoString;
                m_pclMainActivityHandler.sendMessage( p_pclMessage );

                break out;
            }

            p_i64Result = 0; //设置本函数执行成功。
        }

        return p_i64Result;
    }

    //用户定义的销毁函数，在本线程退出时调用一次，返回值表示是否重新初始化，为0表示直接退出，为非0表示重新初始化。
    public long UserDestory()
    {
        if( ( m_i32ExitFlag == 1 ) && ( m_pclClientSocket != null ) && ( m_pclClientSocket.isConnected() ) && ( m_i8IsRecvExitPacket == 0 ) ) //如果本线程接收到退出请求，且TCP协议客户端套接字类对象不为空，且TCP协议客户端套接字类对象已连接，且没有接收到退出包。
        {
            //设置预读长度。
            if( m_pszi8TempData == null )
            {
                m_pszi8TempData = new byte[2];
            }
            m_pszi8TempData[0] = ( byte ) 0xFF;
            m_pszi8TempData[1] = ( byte ) 0xFF;

            try
            {
                OutputStream clOutputStream = m_pclClientSocket.getOutputStream();
                clOutputStream.write( m_pszi8TempData, 0, 2 );
                clOutputStream.flush(); //防止出现Software caused connection abort异常。
                m_i64LastPacketSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间。

                String p_pclInfoString = "发送一个退出包成功。";
                Log.i( m_pclCurrentClassNameString, p_pclInfoString );
                Message p_pclMessage = new Message();
                p_pclMessage.what = 2;
                p_pclMessage.obj = p_pclInfoString;
                m_pclMainActivityHandler.sendMessage( p_pclMessage );
            }
            catch( IOException e )
            {
                String p_pclInfoString = "发送一个退出包失败。原因：" + e.getMessage();
                Log.e( m_pclCurrentClassNameString, p_pclInfoString );
                Message p_pclMessage = new Message();
                p_pclMessage.what = 2;
                p_pclMessage.obj = p_pclInfoString;
                m_pclMainActivityHandler.sendMessage( p_pclMessage );
            }
        }

        //销毁TCP协议服务端套接字。
        if( m_pclServerSocket != null )
        {
            try
            {
                String p_pclInfoString;
                if( m_pclServerSocket.getInetAddress() != null )
                {
                    p_pclInfoString = "已关闭TCP协议服务端套接字[" + m_pclServerSocket.getInetAddress().getHostAddress() + ":" + m_pclServerSocket.getLocalPort() + "]。";
                }
                else
                {
                    p_pclInfoString = "已关闭TCP协议服务端套接字。";
                }
                Log.i( m_pclCurrentClassNameString, p_pclInfoString );
                Message p_pclMessage = new Message();
                p_pclMessage.what = 2;
                p_pclMessage.obj = p_pclInfoString;
                m_pclMainActivityHandler.sendMessage( p_pclMessage );

                m_pclServerSocket.close(); //关闭TCP协议服务端套接字。
            }
            catch( IOException e )
            {
            }
            m_pclServerSocket = null;
        }

        //销毁TCP协议客户端套接字。
        if( m_pclClientSocket != null )
        {
            try
            {
                String p_pclInfoString;
                if( ( m_pclClientSocket.getLocalAddress() != null ) && ( m_pclClientSocket.getInetAddress() != null ) )
                {
                    p_pclInfoString = "已关闭本地TCP协议客户端套接字[" + m_pclClientSocket.getLocalAddress().getHostAddress() + ":" + m_pclClientSocket.getLocalPort() + "]与远端TCP协议客户端套接字[" + m_pclClientSocket.getInetAddress().getHostAddress() + ":" + m_pclClientSocket.getPort() + "]的连接。";
                }
                else
                {
                    p_pclInfoString = "已关闭TCP协议客户端套接字。";
                }
                Log.i( m_pclCurrentClassNameString, p_pclInfoString );
                Message p_pclMessage = new Message();
                p_pclMessage.what = 2;
                p_pclMessage.obj = p_pclInfoString;
                m_pclMainActivityHandler.sendMessage( p_pclMessage );

                m_pclClientSocket.close(); //关闭TCP协议客户端套接字。
            }
            catch( IOException e )
            {
            }
            m_pclClientSocket = null;
        }

        //销毁接收音频输出帧的链表类对象。
        if( m_clRecvOutputFrameLinkedList != null )
        {
            m_clRecvOutputFrameLinkedList.clear();
            m_clRecvOutputFrameLinkedList = null;
        }

        //销毁自适应抖动缓冲器类对象。
        if( m_pclAjb != null )
        {
            m_pclAjb.Destory();
            m_pclAjb = null;
        }

        if( ( m_i8IsCreateServerOrClient == 1 ) && ( m_i32ExitCode == -2 ) && ( m_i32ExitFlag == 0 ) ) //如果当前是创建服务端，且退出代码为处理失败，且本线程未接收到退出请求。
        {
            Log.i( m_pclCurrentClassNameString, "由于当前是创建服务端，且退出代码为处理失败，且本线程未接收到退出请求，本线程重新初始化来继续保持监听。" );
            return 1;
        }
        else if( ( m_i8IsCreateServerOrClient == 0 ) && ( m_i32ExitFlag == 0 ) && ( m_i8IsRecvExitPacket == 0 ) ) //如果当前是创建客户端，且本线程未接收到退出请求，且没有接收到退出包。
        {
            Log.i( m_pclCurrentClassNameString, "由于当前是创建客户端，且本线程未接收到退出请求，且没有接收到退出包，本线程在500毫秒后重新初始化来重连。" );
            SystemClock.sleep( 500 ); //暂停500毫秒。
            return 1;
        }
        else //其他情况，本线程直接退出。
        {
            //发送本线程退出消息给主界面线程。
            Message clMessage = new Message();
            clMessage.what = 1;
            m_pclMainActivityHandler.sendMessage( clMessage );

            return 0;
        }
    }

    //用户定义的读取输入帧函数，在读取到一个输入帧并处理完后回调一次，为0表示成功，为非0表示失败。
    public long UserReadInputFrame( short pszi16PcmInputFrame[], short pszi16PcmResultFrame[], int i32VoiceActivityStatus, byte pszi8SpeexInputFrame[], long i64SpeexInputFrameLength, int i32SpeexInputFrameIsNeedTrans )
    {
        long p_i64Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            //发送输入帧。
            {
                if( i32VoiceActivityStatus == 1 ) //如果该输入帧为有语音活动。
                {
                    switch( m_i32UseWhatCodec ) //使用什么编解码器。
                    {
                        case 0: //如果使用PCM原始数据。
                        {
                            for( m_i32Temp = 0; m_i32Temp < pszi16PcmResultFrame.length; m_i32Temp++ )
                            {
                                m_pszi8TempData[6 + m_i32Temp * 2] = ( byte ) ( pszi16PcmResultFrame[m_i32Temp] & 0xFF );
                                m_pszi8TempData[6 + m_i32Temp * 2 + 1] = ( byte ) ( ( pszi16PcmResultFrame[m_i32Temp] & 0xFF00 ) >> 8 );
                            }

                            m_i32Temp = pszi16PcmResultFrame.length * 2 + 4; //预读长度 = PCM格式音频输入数据帧长度 + 时间戳长度。

                            break;
                        }
                        case 1: //如果使用Speex编解码器。
                        {
                            if( i32SpeexInputFrameIsNeedTrans == 1 ) //如果本Speex格式音频输入数据帧需要传输。
                            {
                                for( m_i32Temp = 0; m_i32Temp < i64SpeexInputFrameLength; m_i32Temp++ )
                                {
                                    m_pszi8TempData[6 + m_i32Temp] = pszi8SpeexInputFrame[m_i32Temp];
                                }

                                m_i32Temp = ( int ) i64SpeexInputFrameLength + 4; //预读长度 = Speex格式音频输入数据帧长度 + 时间戳长度。
                            }
                            else //如果本Speex格式音频输入数据帧不需要传输。
                            {
                                m_i32Temp = 4; //预读长度 = 时间戳长度。
                            }

                            break;
                        }
                    }
                }
                else //如果本音频输入数据帧为无语音活动
                {
                    m_i32Temp = 4; //预读长度 = 时间戳长度
                }

                if( ( m_i32Temp != 4 ) || //如果本音频输入数据帧为有语音活动，就发送。
                        ( ( m_i32Temp == 4 ) && ( m_i32LastSendInputFrameIsActive != 0 ) ) ) //如果本音频输入数据帧为无语音活动，但最后发送的一个音频数据帧为有语音活动，就发送。
                {
                    //设置预读长度。
                    m_pszi8TempData[0] = ( byte ) ( m_i32Temp & 0xFF );
                    m_pszi8TempData[1] = ( byte ) ( ( m_i32Temp & 0xFF00 ) >> 8 );

                    //设置时间戳。
                    m_pszi8TempData[2] = ( byte ) ( m_i32SendInputFrameTimeStamp & 0xFF );
                    m_pszi8TempData[3] = ( byte ) ( ( m_i32SendInputFrameTimeStamp & 0xFF00 ) >> 8 );
                    m_pszi8TempData[4] = ( byte ) ( ( m_i32SendInputFrameTimeStamp & 0xFF0000 ) >> 16 );
                    m_pszi8TempData[5] = ( byte ) ( ( m_i32SendInputFrameTimeStamp & 0xFF000000 ) >> 24 );

                    m_i32SendInputFrameTimeStamp += m_i64FrameLength; //时间戳递增一个帧的数据长度。

                    try
                    {
                        OutputStream clOutputStream = m_pclClientSocket.getOutputStream();
                        clOutputStream.write( m_pszi8TempData, 0, m_i32Temp + 2 );
                        clOutputStream.flush(); //防止出现Software caused connection abort异常。
                        m_i64LastPacketSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。

                        Log.i( m_pclCurrentClassNameString, "发送一个输入帧成功。时间戳：" + m_i32SendInputFrameTimeStamp + "，总长度：" + m_i32Temp + "。" );
                    }
                    catch( IOException e )
                    {
                        String p_pclInfoString = "发送一个输入帧失败。原因：" + e.getMessage();
                        Log.e( m_pclCurrentClassNameString, p_pclInfoString );
                        Message p_pclMessage = new Message();
                        p_pclMessage.what = 2;
                        p_pclMessage.obj = p_pclInfoString;
                        m_pclMainActivityHandler.sendMessage( p_pclMessage );
                        break out;
                    }
                }
                else
                {
                    Log.i( m_pclCurrentClassNameString, "本输入帧为无语音活动，且最后发送的一个输入帧为无语音活动，无需发送。" );
                }

                //设置最后发送的一个音频数据帧有无语音活动。
                if( m_i32Temp != 4 ) m_i32LastSendInputFrameIsActive = 1;
                else m_i32LastSendInputFrameIsActive = 0;
            }

            p_i64Result = 0; //设置本函数执行成功。
        }

        return p_i64Result;
    }

    //用户定义的写入输出帧函数，在需要写入一个输出帧时回调一次。注意：本函数不是在音频处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致回音消除失败。
    public void UserWriteOutputFrame( short pszi16PcmOutputFrame[], byte p_pszi8SpeexOutputFrame[], long p_pszi64SpeexOutputFrameLength[] )
    {
        //从链表或自适应抖动缓冲器取出一个输出帧。
        switch( m_i32UseWhatRecvOutputFrame ) //使用什么接收输出帧。
        {
            case 0: //如果使用链表。
            {
                byte p_pszi8OutputFrame[];

                switch( m_i32UseWhatCodec ) //使用什么编解码器。
                {
                    case 0: //如果使用PCM原始数据。
                    {
                        if( ( m_clRecvOutputFrameLinkedList.size() > 0 ) && ( m_clRecvOutputFrameLinkedList.getFirst().length > 0 ) ) //如果接收输出帧链表的第一个输出帧为有语音活动。
                        {
                            p_pszi8OutputFrame = m_clRecvOutputFrameLinkedList.getFirst(); //获取接收输出帧链表的第一个输出帧。

                            for( m_i32Temp2 = 0; m_i32Temp2 < m_i64FrameLength; m_i32Temp2++ )
                            {
                                pszi16PcmOutputFrame[m_i32Temp2] = ( short ) ( ( p_pszi8OutputFrame[m_i32Temp2 * 2] & 0xFF ) | ( p_pszi8OutputFrame[m_i32Temp2 * 2 + 1] << 8 ) );
                            }
                        }
                        else //如果接收音频输出数据帧的链表为空，或第一个音频输出数据帧为无语音活动。
                        {
                            for( m_i32Temp2 = 0; m_i32Temp2 < m_i64FrameLength; m_i32Temp2++ )
                            {
                                pszi16PcmOutputFrame[m_i32Temp2] = 0;
                            }
                        }

                        break;
                    }
                    case 1: //如果使用Speex编解码器。
                    {
                        if( ( m_clRecvOutputFrameLinkedList.size() > 0 ) && ( m_clRecvOutputFrameLinkedList.getFirst().length > 0 ) ) //如果接收输出帧链表的第一个输出帧为有语音活动。
                        {
                            p_pszi8OutputFrame = m_clRecvOutputFrameLinkedList.getFirst(); //获取接收输出帧链表的第一个输出帧。

                            for( m_i32Temp2 = 0; m_i32Temp2 < p_pszi8OutputFrame.length; m_i32Temp2++ )
                            {
                                p_pszi8SpeexOutputFrame[m_i32Temp2] = p_pszi8OutputFrame[m_i32Temp2];
                            }

                            p_pszi64SpeexOutputFrameLength[0] = p_pszi8OutputFrame.length;
                        }
                        else //如果接收音频输出数据帧的链表为空，或第一个音频输出数据帧为无语音活动。
                        {
                            p_pszi64SpeexOutputFrameLength[0] = 0;
                        }

                        break;
                    }
                }

                //删除接收输出帧链表的第一个输出帧。
                if( m_clRecvOutputFrameLinkedList.size() > 0 )
                {
                    m_clRecvOutputFrameLinkedList.removeFirst();
                }

                break;
            }
            case 1: //如果使用自适应抖动缓冲器。
            {
                HTLong p_pclOutputFrameLength = new HTLong(); //从自适应抖动缓冲器中取出的输出帧的数据长度。
                HTInteger p_pclAjbFrameCount = new HTInteger(); //自适应抖动缓冲器中帧的数量。

                switch( m_i32UseWhatCodec ) //使用什么编解码器。
                {
                    case 0: //如果使用PCM原始数据。
                    {
                        //从自适应抖动缓冲器取出一个输出帧。
                        synchronized( m_pclAjb )
                        {
                            m_pclAjb.GetOneShortFrame( pszi16PcmOutputFrame, pszi16PcmOutputFrame.length, p_pclOutputFrameLength );
                        }

                        if( p_pclOutputFrameLength.m_i64Value != 0 ) //如果输出帧为有语音活动。
                        {

                        }
                        else //如果输出帧为无语音活动。
                        {
                            for( m_i32Temp2 = 0; m_i32Temp2 < m_i64FrameLength; m_i32Temp2++ )
                            {
                                pszi16PcmOutputFrame[m_i32Temp2] = 0;
                            }
                        }

                        Log.i( m_pclCurrentClassNameString, "从自适应抖动缓冲器取出一个输出帧，帧的数据长度：" + p_pclOutputFrameLength.m_i64Value + "。" );

                        break;
                    }
                    case 1: //如果使用Speex编解码器。
                    {
                        //从自适应抖动缓冲器取出一个音频输出数据帧。
                        synchronized( m_pclAjb )
                        {
                            m_pclAjb.GetOneByteFrame( p_pszi8SpeexOutputFrame, p_pszi8SpeexOutputFrame.length, p_pclOutputFrameLength );
                        }

                        p_pszi64SpeexOutputFrameLength[0] = p_pclOutputFrameLength.m_i64Value;

                        break;
                    }
                }

                m_pclAjb.GetCurHaveBufferActiveFrameCount( p_pclAjbFrameCount );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中当前已缓冲有活动帧的数量为 " + p_pclAjbFrameCount.m_i32Value + " 个。" );
                m_pclAjb.GetCurHaveBufferInactiveFrameCount( p_pclAjbFrameCount );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中当前已缓冲无活动帧的数量为 " + p_pclAjbFrameCount.m_i32Value + " 个。" );
                m_pclAjb.GetCurHaveBufferFrameCount( p_pclAjbFrameCount );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中当前已缓冲帧的数量为 " + p_pclAjbFrameCount.m_i32Value + " 个。" );

                m_pclAjb.GetMaxNeedBufferFrameCount( p_pclAjbFrameCount );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中最大需缓冲音频数据帧的数量为 " + p_pclAjbFrameCount.m_i32Value + " 个。" );
                m_pclAjb.GetMinNeedBufferFrameCount( p_pclAjbFrameCount );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中最小需缓冲音频数据帧的数量为 " + p_pclAjbFrameCount.m_i32Value + " 个。" );
                m_pclAjb.GetCurNeedBufferFrameCount( p_pclAjbFrameCount );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中当前需缓冲音频数据帧的数量为 " + p_pclAjbFrameCount.m_i32Value + " 个。" );

                break;
            }
        }
    }

    //用户定义的获取PCM格式输出帧函数，在解码完一个输出帧时回调一次。注意：本函数不是在音频处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致回音消除失败。
    public void UserGetPcmOutputFrame( short pszi16PcmOutputFrame[] )
    {

    }
}

public class MainActivity extends AppCompatActivity
{
    String m_pclCurrentClassNameString = this.getClass().getSimpleName(); //存放当前类名称字符串。

    View m_pclLayoutActivityMainView; //存放主界面布局控件的内存指针。
    View m_pclLayoutActivitySettingView; //存放设置界面布局控件的内存指针。
    View m_pclLayoutActivitySpeexAecView; //存放Speex声学回音消除器设置布局控件的内存指针。
    View m_pclLayoutActivityWebRtcAecmView; //存放WebRtc定点版声学回音消除器设置布局控件的内存指针。
    View m_pclLayoutActivityWebRtcAecView; //存放WebRtc浮点版声学回音消除器设置布局控件的内存指针。
    View m_pclLayoutActivitySpeexWebRtcAecView; //存放SpeexWebRtc三重声学回音消除器设置布局控件的内存指针。
    View m_pclLayoutActivitySpeexPreprocessorNsView; //存放Speex预处理器的噪音抑制设置布局控件的内存指针。
    View m_pclLayoutActivityWebRtcNsxView; //存放WebRtc定点噪音抑制器设置布局控件的内存指针。
    View m_pclLayoutActivityWebRtcNsView; //存放WebRtc浮点噪音抑制器设置布局控件的内存指针。
    View m_pclLayoutActivitySpeexPreprocessorOtherView; //存放Speex预处理器的其他功能设置布局控件的内存指针。
    View m_pclLayoutActivitySpeexCodecView; //存放Speex编解码器设置布局控件的内存指针。
    View m_pclLayoutActivityAjbView; //存放自适应抖动缓冲器设置布局控件的内存指针。
    View m_pclLayoutActivityReadMeView; //存放说明界面布局控件的内存指针。
    View m_pclLayoutActivityCurrentView; //存放当前界面布局控件的内存指针。

    MainActivity m_pclMainActivity; //存放主界面类对象的内存指针。
    MyAudioProcessThread m_pclMyAudioProcessThread; //存放音频处理线程类对象的内存指针。
    MainActivityHandler m_pclMainActivityHandler; //存放主界面消息处理类对象的内存指针。

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        LayoutInflater layoutInflater = LayoutInflater.from( this );
        m_pclLayoutActivityMainView = layoutInflater.inflate( R.layout.activity_main, null );
        m_pclLayoutActivitySettingView = layoutInflater.inflate( R.layout.activity_setting, null );
        m_pclLayoutActivitySpeexAecView = layoutInflater.inflate( R.layout.activity_speexaec, null );
        m_pclLayoutActivityWebRtcAecmView = layoutInflater.inflate( R.layout.activity_webrtcaecm, null );
        m_pclLayoutActivityWebRtcAecView = layoutInflater.inflate( R.layout.activity_webrtcaec, null );
        m_pclLayoutActivitySpeexWebRtcAecView = layoutInflater.inflate( R.layout.activity_speexwebrtcaec, null );
        m_pclLayoutActivitySpeexPreprocessorNsView = layoutInflater.inflate( R.layout.activity_speexpreprocessorns, null );
        m_pclLayoutActivityWebRtcNsxView = layoutInflater.inflate( R.layout.activity_webrtcnsx, null );
        m_pclLayoutActivityWebRtcNsView = layoutInflater.inflate( R.layout.activity_webrtcns, null );
        m_pclLayoutActivitySpeexPreprocessorOtherView = layoutInflater.inflate( R.layout.activity_speexpreprocessorother, null );
        m_pclLayoutActivitySpeexCodecView = layoutInflater.inflate( R.layout.activity_speexcodec, null );
        m_pclLayoutActivityAjbView = layoutInflater.inflate( R.layout.activity_ajb, null );
        m_pclLayoutActivityReadMeView = layoutInflater.inflate( R.layout.activity_readme, null );

        setContentView( m_pclLayoutActivityMainView ); //设置界面的内容为主界面。
        m_pclLayoutActivityCurrentView = m_pclLayoutActivityMainView;

        m_pclMainActivity = this;

        //请求录音权限、网络权限、写入存储权限。
        ActivityCompat.requestPermissions( this, new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1 );

        //初始化消息处理类对象。
        m_pclMainActivityHandler = new MainActivityHandler();
        m_pclMainActivityHandler.clMainActivity = m_pclMainActivity;

        //启动初始化线程。
        InitThread clInitThread;
        clInitThread = new InitThread();
        clInitThread.m_pclLayoutActivityMainView = m_pclLayoutActivityMainView;
        clInitThread.start();
    }

    //清空日志按钮。
    public void OnClickClearLog( View v )
    {
        LinearLayout clLogLinearLayout = ( LinearLayout ) m_pclLayoutActivityMainView.findViewById( R.id.LogLinearLayout );
        clLogLinearLayout.removeAllViews();
    }

    //返回键。
    @Override
    public void onBackPressed()
    {
        if( m_pclLayoutActivityCurrentView == m_pclLayoutActivityMainView )
        {
            Log.i( m_pclCurrentClassNameString, "用户在主界面按下返回键，本软件退出。" );
            finish();
        }
        else if( m_pclLayoutActivityCurrentView == m_pclLayoutActivitySettingView )
        {
            this.OnClickSettingOk( null );
        }
        else if( ( m_pclLayoutActivityCurrentView == m_pclLayoutActivitySpeexAecView ) ||
                ( m_pclLayoutActivityCurrentView == m_pclLayoutActivityWebRtcAecmView ) ||
                ( m_pclLayoutActivityCurrentView == m_pclLayoutActivityWebRtcAecView ) ||
                ( m_pclLayoutActivityCurrentView == m_pclLayoutActivitySpeexWebRtcAecView ) ||
                ( m_pclLayoutActivityCurrentView == m_pclLayoutActivitySpeexPreprocessorNsView ) ||
                ( m_pclLayoutActivityCurrentView == m_pclLayoutActivityWebRtcNsxView ) ||
                ( m_pclLayoutActivityCurrentView == m_pclLayoutActivityWebRtcNsView ) ||
                ( m_pclLayoutActivityCurrentView == m_pclLayoutActivitySpeexPreprocessorOtherView ) ||
                ( m_pclLayoutActivityCurrentView == m_pclLayoutActivitySpeexCodecView ) ||
                ( m_pclLayoutActivityCurrentView == m_pclLayoutActivityAjbView ) )
        {
            this.OnWebRtcAecSettingOkClick( null );
        }
        else if( m_pclLayoutActivityCurrentView == m_pclLayoutActivityReadMeView )
        {
            this.OnClickReadMeOk( null );
        }
    }

    //创建服务器或连接服务器按钮。
    public void OnClickCreateServerAndConnectServer( View v )
    {
        long p_i64Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            if( m_pclMyAudioProcessThread == null ) //如果音频处理线程还没有启动。
            {
                Log.i( m_pclCurrentClassNameString, "开始启动音频处理线程。" );

                m_pclMyAudioProcessThread = new MyAudioProcessThread();

                if( v.getId() == R.id.CreateServerButton )
                {
                    m_pclMyAudioProcessThread.m_i8IsCreateServerOrClient = 1; //标记创建服务端接受客户端。
                }
                else if( v.getId() == R.id.ConnectServerButton )
                {
                    m_pclMyAudioProcessThread.m_i8IsCreateServerOrClient = 0; //标记创建客户端连接服务端。
                }

                m_pclMyAudioProcessThread.m_pclMainActivityHandler = m_pclMainActivityHandler;

                //设置IP地址字符串、端口、音频播放线程启动延迟。
                m_pclMyAudioProcessThread.m_pclIPAddressString = ( ( EditText ) m_pclLayoutActivityMainView.findViewById( R.id.IPAddressEdit ) ).getText().toString();
                m_pclMyAudioProcessThread.m_i32Port = Integer.parseInt( ( ( EditText ) m_pclLayoutActivityMainView.findViewById( R.id.PortEdit ) ).getText().toString() );

                //初始化音频处理线程类对象。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseSamplingRate8000 ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.Init( getApplicationContext(), 8000 );
                }
                else if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseSamplingRate16000 ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.Init( getApplicationContext(), 16000 );
                }
                else if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseSamplingRate32000 ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.Init( getApplicationContext(), 32000 );
                }

                //判断是否打印Logcat日志。
                if( ( ( CheckBox ) m_pclLayoutActivitySettingView.findViewById( R.id.CheckBoxIsPrintLogcat ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.SetPrintLogcat( 1 );
                }
                else
                {
                    m_pclMyAudioProcessThread.SetPrintLogcat( 0 );
                }

                //判断是否使用唤醒锁。
                if( ( ( CheckBox ) m_pclLayoutActivitySettingView.findViewById( R.id.CheckBoxIsUseWakeLock ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.SetUseWakeLock( 1 );
                }
                else
                {
                    m_pclMyAudioProcessThread.SetUseWakeLock( 0 );
                }

                //判断是否不使用声学回音消除器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseNoAec ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.SetUseNoAec();
                }

                //判断是否使用Speex声学回音消除器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseSpeexAec ) ).isChecked() )
                {
                    try
                    {
                        m_pclMyAudioProcessThread.SetUseSpeexAec(
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexAecView.findViewById( R.id.SpeexAecFilterLength ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_pclLayoutActivitySpeexAecView.findViewById( R.id.CheckBoxSpeexAecIsSaveMemoryFile ) ).isChecked() ) ? 1 : 0,
                                Environment.getExternalStorageDirectory() + "/SpeexAecMemory"
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用WebRtc浮点版声学回音消除器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseWebRtcAec ) ).isChecked() )
                {
                    try
                    {
                        m_pclMyAudioProcessThread.SetUseWebRtcAec(
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivityWebRtcAecView.findViewById( R.id.WebRtcAecEchoMode ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivityWebRtcAecView.findViewById( R.id.WebRtcAecDelay ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_pclLayoutActivityWebRtcAecView.findViewById( R.id.CheckBoxWebRtcAecIsUseDelayAgnostic ) ).isChecked() ) ? 1 : 0,
                                ( ( ( CheckBox ) m_pclLayoutActivityWebRtcAecView.findViewById( R.id.CheckBoxWebRtcAecIsUseAdaptiveAdjustDelay ) ).isChecked() ) ? 1 : 0,
                                ( ( ( CheckBox ) m_pclLayoutActivityWebRtcAecView.findViewById( R.id.CheckBoxWebRtcAecIsSaveMemoryFile ) ).isChecked() ) ? 1 : 0,
                                Environment.getExternalStorageDirectory() + "/WebRtcAecMemory"
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用WebRtc定点版声学回音消除器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseWebRtcAecm ) ).isChecked() )
                {
                    try
                    {
                        m_pclMyAudioProcessThread.SetUseWebRtcAecm(
                                ( ( ( CheckBox ) m_pclLayoutActivityWebRtcAecmView.findViewById( R.id.CheckBoxWebRtcAecmIsUseCNGMode ) ).isChecked() ) ? 1 : 0,
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivityWebRtcAecmView.findViewById( R.id.WebRtcAecmEchoMode ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivityWebRtcAecmView.findViewById( R.id.WebRtcAecmDelay ) ).getText().toString() )
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用SpeexWebRtc三重声学回音消除器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseSpeexWebRtcAec ) ).isChecked() )
                {
                    try
                    {
                        m_pclMyAudioProcessThread.SetUseSpeexWebRtcAec(
                                ( ( RadioButton ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecm ) ).isChecked() ? 1 :
                                        ( ( RadioButton ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.SpeexWebRtcAecWorkModeWebRtcAecmWebRtcAec ) ).isChecked() ? 2 :
                                                ( ( RadioButton ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAec ) ).isChecked() ? 3 : 0,
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLength ) ).getText().toString() ),
                                Float.parseFloat( ( ( TextView ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMultiple ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSuppress ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSuppressActive ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.CheckBoxSpeexWebRtcAecWebRtcAecmIsUseCNGMode ) ).isChecked() ) ? 1 : 0,
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoMode ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelay ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoMode ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelay ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.CheckBoxSpeexWebRtcAecWebRtcAecIsUseDelayAgnostic ) ).isChecked() ) ? 1 : 0,
                                ( ( ( CheckBox ) m_pclLayoutActivitySpeexWebRtcAecView.findViewById( R.id.CheckBoxSpeexWebRtcAecWebRtcAecIsUseAdaptiveAdjustDelay ) ).isChecked() ) ? 1 : 0
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否不使用噪音抑制器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseNoNs ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.SetUseNoNs();
                }

                //判断是否使用Speex预处理器的噪音抑制。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseSpeexPreorocessorNs ) ).isChecked() )
                {
                    try
                    {
                        m_pclMyAudioProcessThread.SetUseSpeexPreprocessorNs(
                                ( ( ( CheckBox ) m_pclLayoutActivitySpeexPreprocessorNsView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseNs ) ).isChecked() ) ? 1 : 0,
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexPreprocessorNsView.findViewById( R.id.SpeexPreprocessorNoiseSuppress ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_pclLayoutActivitySpeexPreprocessorNsView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseDereverberation ) ).isChecked() ) ? 1 : 0,
                                ( ( ( CheckBox ) m_pclLayoutActivitySpeexPreprocessorNsView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseRec ) ).isChecked() ) ? 1 : 0,
                                Float.parseFloat( ( ( TextView ) m_pclLayoutActivitySpeexPreprocessorNsView.findViewById( R.id.SpeexPreprocessorEchoMultiple ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexPreprocessorNsView.findViewById( R.id.SpeexPreprocessorEchoSuppress ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexPreprocessorNsView.findViewById( R.id.SpeexPreprocessorEchoSuppressActive ) ).getText().toString() )
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用WebRtc定点版噪音抑制器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseWebRtcNsx ) ).isChecked() )
                {
                    try
                    {
                        m_pclMyAudioProcessThread.SetUseWebRtcNsx(
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivityWebRtcNsxView.findViewById( R.id.WebRtcNsxPolicyMode ) ).getText().toString() )
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用WebRtc浮点版噪音抑制器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseWebRtcNs ) ).isChecked() )
                {
                    try
                    {
                        m_pclMyAudioProcessThread.SetUseWebRtcNs(
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivityWebRtcNsView.findViewById( R.id.WebRtcNsPolicyMode ) ).getText().toString() )
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用RNNoise噪音抑制器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseRNNoise ) ).isChecked() )
                {
                    try
                    {
                        m_pclMyAudioProcessThread.SetUseRNNoise();
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用Speex预处理器的其他功能。
                if( ( ( CheckBox ) m_pclLayoutActivitySettingView.findViewById( R.id.CheckBoxIsUseSpeexPreprocessorOther ) ).isChecked() )
                {
                    try
                    {
                        m_pclMyAudioProcessThread.SetSpeexPreprocessorOther(
                                1,
                                ( ( ( CheckBox ) m_pclLayoutActivitySpeexPreprocessorOtherView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseVad ) ).isChecked() ) ? 1 : 0,
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexPreprocessorOtherView.findViewById( R.id.SpeexPreprocessorVadProbStart ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexPreprocessorOtherView.findViewById( R.id.SpeexPreprocessorVadProbContinue ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_pclLayoutActivitySpeexPreprocessorOtherView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseAgc ) ).isChecked() ) ? 1 : 0,
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexPreprocessorOtherView.findViewById( R.id.SpeexPreprocessorAgcLevel ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexPreprocessorOtherView.findViewById( R.id.SpeexPreprocessorAgcIncrement ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexPreprocessorOtherView.findViewById( R.id.SpeexPreprocessorAgcDecrement ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexPreprocessorOtherView.findViewById( R.id.SpeexPreprocessorAgcMaxGain ) ).getText().toString() )
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用PCM原始数据。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUsePcm ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.SetUsePcm();
                }

                //判断是否使用Speex编解码器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseSpeexCodec ) ).isChecked() )
                {
                    try
                    {
                        m_pclMyAudioProcessThread.SetUseSpeexCodec(
                                ( ( ( RadioButton ) m_pclLayoutActivitySpeexCodecView.findViewById( R.id.RadioButtonSpeexCodecEncoderUseCbr ) ).isChecked() ) ? 0 : 1,
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexCodecView.findViewById( R.id.SpeexCodecEncoderQuality ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexCodecView.findViewById( R.id.SpeexCodecEncoderComplexity ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) m_pclLayoutActivitySpeexCodecView.findViewById( R.id.SpeexCodecEncoderPlcExpectedLossRate ) ).getText().toString() ),
                                ( ( ( CheckBox ) m_pclLayoutActivitySpeexCodecView.findViewById( R.id.CheckBoxSpeexCodecIsUsePerceptualEnhancement ) ).isChecked() ) ? 1 : 0
                        );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用Opus编解码器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseOpusCodec ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.SetUseOpusCodec();
                }

                //判断是否使用链表。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseList ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.m_i32UseWhatRecvOutputFrame = 0;
                }

                //判断是否使用自己设计的自适应抖动缓冲器。
                if( ( ( RadioButton ) m_pclLayoutActivitySettingView.findViewById( R.id.RadioButtonUseAjb ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.m_i32UseWhatRecvOutputFrame = 1;

                    try
                    {
                        m_pclMyAudioProcessThread.m_i32AjbMinNeedBufferFrameCount = Integer.parseInt( ( ( TextView ) m_pclLayoutActivityAjbView.findViewById( R.id.AjbMinNeedBufferFrameCount ) ).getText().toString() );
                        m_pclMyAudioProcessThread.m_i32AjbMaxNeedBufferFrameCount = Integer.parseInt( ( ( TextView ) m_pclLayoutActivityAjbView.findViewById( R.id.AjbMaxNeedBufferFrameCount ) ).getText().toString() );
                        m_pclMyAudioProcessThread.m_i8AjbAdaptiveSensitivity = ( byte ) Integer.parseInt( ( ( TextView ) m_pclLayoutActivityAjbView.findViewById( R.id.AjbAdaptiveSensitivity ) ).getText().toString() );
                    }
                    catch( NumberFormatException e )
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否保存音频到文件。
                if( ( ( CheckBox ) m_pclLayoutActivitySettingView.findViewById( R.id.CheckBoxIsSaveAudioToFile ) ).isChecked() )
                {
                    m_pclMyAudioProcessThread.SetSaveAudioToFile(
                            1,
                            Environment.getExternalStorageDirectory() + "/AudioInput.wav",
                            Environment.getExternalStorageDirectory() + "/AudioOutput.wav",
                            Environment.getExternalStorageDirectory() + "/AudioResult.wav"
                    );
                }

                m_pclMyAudioProcessThread.start();

                Log.i( m_pclCurrentClassNameString, "启动音频处理线程完毕。" );

                if( v.getId() == R.id.CreateServerButton )
                {
                    ( ( EditText ) findViewById( R.id.IPAddressEdit ) ).setEnabled( false ); //设置IP地址控件为不可用
                    ( ( EditText ) findViewById( R.id.PortEdit ) ).setEnabled( false ); //设置端口控件为不可用
                    ( ( Button ) findViewById( R.id.CreateServerButton ) ).setText( "中断" ); //设置创建服务端按钮的内容为“中断”
                    ( ( Button ) findViewById( R.id.ConnectServerButton ) ).setEnabled( false ); //设置连接服务端按钮为不可用
                    ( ( Button ) findViewById( R.id.SettingButton ) ).setEnabled( false ); //设置设置按钮为不可用
                }
                else if( v.getId() == R.id.ConnectServerButton )
                {
                    ( ( EditText ) findViewById( R.id.IPAddressEdit ) ).setEnabled( false ); //设置IP地址控件为不可用
                    ( ( EditText ) findViewById( R.id.PortEdit ) ).setEnabled( false ); //设置端口控件为不可用
                    ( ( Button ) findViewById( R.id.CreateServerButton ) ).setEnabled( false ); //设置创建服务端按钮为不可用
                    ( ( Button ) findViewById( R.id.ConnectServerButton ) ).setText( "中断" ); //设置连接服务端按钮的内容为“中断”
                    ( ( Button ) findViewById( R.id.SettingButton ) ).setEnabled( false ); //设置设置按钮为不可用
                }
            }
            else
            {
                m_pclMyAudioProcessThread.RequireExit(); //请求音频处理线程退出。

                try
                {
                    Log.i( m_pclCurrentClassNameString, "开始等待音频处理线程退出。" );
                    m_pclMyAudioProcessThread.join(); //等待音频处理线程退出。
                    Log.i( m_pclCurrentClassNameString, "结束等待音频处理线程退出。" );
                }
                catch( InterruptedException e )
                {

                }
            }

            p_i64Result = 0;

            break out;
        }

        if( p_i64Result != 0 )
        {
            m_pclMyAudioProcessThread = null;
        }
    }

    //主界面设置按钮。
    public void OnClickSetting( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //设置界面的确定按钮。
    public void OnClickSettingOk( View clButton )
    {
        setContentView( m_pclLayoutActivityMainView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivityMainView;
    }

    //Speex声学回音消除器设置按钮。
    public void OnSpeexAecSettingClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySpeexAecView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySpeexAecView;
    }

    //Speex声学回音消除器设置界面的删除内存块文件按钮。
    public void OnSpeexAecDeleteMemoryFileClick( View clButton )
    {
        String p_pclSpeexAecMemoryFullPath = Environment.getExternalStorageDirectory() + "/SpeexAecMemory";
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
    public void OnSpeexAecSettingOkClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //WebRtc定点版声学回音消除器设置按钮。
    public void OnWebRtcAecmSettingClick( View clButton )
    {
        setContentView( m_pclLayoutActivityWebRtcAecmView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivityWebRtcAecmView;
    }

    //WebRtc定点版声学回音消除器设置界面的确定按钮。
    public void OnWebRtcAecmSettingOkClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //WebRtc浮点版声学回音消除器设置按钮。
    public void OnWebRtcAecSettingClick( View clButton )
    {
        setContentView( m_pclLayoutActivityWebRtcAecView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivityWebRtcAecView;
    }

    //WebRtc浮点版声学回音消除器设置界面的删除内存块文件按钮。
    public void OnWebRtcAecDeleteMemoryFileClick( View clButton )
    {
        String p_pclWebRtcAecMemoryFullPath = Environment.getExternalStorageDirectory() + "/WebRtcAecMemory";
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
    public void OnWebRtcAecSettingOkClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //SpeexWebRtc三重声学回音消除器设置按钮。
    public void OnSpeexWebRtcAecSettingClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySpeexWebRtcAecView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySpeexWebRtcAecView;
    }

    //SpeexWebRtc三重声学回音消除器设置界面的确定按钮。
    public void OnSpeexWebRtcAecSettingOkClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //Speex预处理器的噪音抑制设置按钮。
    public void OnSpeexPreprocessorNsSettingClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySpeexPreprocessorNsView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySpeexPreprocessorNsView;
    }

    //Speex预处理器的噪音抑制设置界面的确定按钮。
    public void OnSpeexPreprocessorNsSettingOkClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //WebRtc定点版噪音抑制器设置按钮。
    public void OnWebRtcNsxSettingClick( View clButton )
    {
        setContentView( m_pclLayoutActivityWebRtcNsxView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivityWebRtcNsxView;
    }

    //WebRtc定点版噪音抑制器设置界面的确定按钮。
    public void OnWebRtcNsxSettingOkClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //WebRtc浮点版噪音抑制器设置按钮。
    public void OnWebRtcNsSettingClick( View clButton )
    {
        setContentView( m_pclLayoutActivityWebRtcNsView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivityWebRtcNsView;
    }

    //WebRtc浮点版噪音抑制器设置界面的确定按钮。
    public void OnWebRtcNsSettingOkClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //Speex预处理器的其他功能设置按钮。
    public void OnSpeexPreprocessorOtherSettingClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySpeexPreprocessorOtherView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySpeexPreprocessorOtherView;
    }

    //Speex预处理器的其他功能设置界面的确定按钮。
    public void OnSpeexPreprocessorOtherSettingOkClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //Speex编解码器设置按钮。
    public void OnSpeexCodecSettingClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySpeexCodecView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySpeexCodecView;
    }

    //Speex编解码器设置界面的确定按钮。
    public void OnSpeexCodecSettingOkClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //Opus编解码器设置按钮。
    public void OnOpusCodecSettingClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySpeexCodecView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySpeexCodecView;
    }

    //Opus编解码器设置界面的确定按钮。
    public void OnOpusCodecSettingOkClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //自适应抖动缓冲器设置按钮。
    public void OnAjbSettingClick( View clButton )
    {
        setContentView( m_pclLayoutActivityAjbView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivityAjbView;
    }

    //自适应抖动缓冲器设置界面的确定按钮。
    public void OnAjbSettingOkClick( View clButton )
    {
        setContentView( m_pclLayoutActivitySettingView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivitySettingView;
    }

    //主界面说明按钮。
    public void OnClickReadMe( View clButton )
    {
        setContentView( m_pclLayoutActivityReadMeView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivityReadMeView;
    }

    //说明界面的确定按钮。
    public void OnClickReadMeOk( View clButton )
    {
        setContentView( m_pclLayoutActivityMainView );
        m_pclLayoutActivityCurrentView = m_pclLayoutActivityMainView;
    }
}