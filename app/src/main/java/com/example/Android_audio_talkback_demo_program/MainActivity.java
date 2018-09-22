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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

//初始化线程类
class InitThread extends Thread
{
    String m_pclCurrentClassNameString = this.getClass().getSimpleName(); //当前类名称字符串

    View clLayoutActivityMainView; //主界面布局控件的内存指针

    public void run()
    {
        String clIPAddressString = "127.0.0.1";
        String clPortString = "12345";

        //准备IP地址控件的内容为本机IP
        try
        {
            for( Enumeration<NetworkInterface> clEnumerationNetworkInterface = NetworkInterface.getNetworkInterfaces(); clEnumerationNetworkInterface.hasMoreElements();)
            {
                NetworkInterface clNetworkInterface = clEnumerationNetworkInterface.nextElement();
                if( clNetworkInterface.getName().compareTo( "usbnet0" ) != 0 ) //如果该网络接口设备不是USB接口对应的网络接口设备
                {
                    for( Enumeration<InetAddress> enumIpAddr = clNetworkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); )
                    {
                        InetAddress clInetAddress = enumIpAddr.nextElement();
                        if( (!clInetAddress.isLoopbackAddress()) && (clInetAddress.getAddress().length == 4)) //如果该IP地址不是回环地址，且是IPv4的
                        {
                            clIPAddressString = clInetAddress.getHostAddress();
                        }
                    }
                }
            }
        }
        catch( SocketException e)
        {
            clIPAddressString = "127.0.0.1";
        }

        //设置IP地址控件的内容
        ((EditText)clLayoutActivityMainView.findViewById( R.id.IPAddressEdit )).setText( clIPAddressString );

        //设置端口控件的内容
        ((EditText)clLayoutActivityMainView.findViewById( R.id.PortEdit )).setText( clPortString );
    }
}

//主界面消息处理类
class MainActivityHandler extends Handler
{
    String m_pclCurrentClassNameString = this.getClass().getSimpleName(); //当前类名称字符串

    MainActivity clMainActivity;

    public void handleMessage( Message clMessage )
    {
        if( clMessage.what == 1 ) //如果是音频处理线程正常退出的消息
        {
            clMainActivity.clMyAudioProcessThread = null;

            ((EditText)clMainActivity.findViewById( R.id.IPAddressEdit )).setEnabled( true ); //设置IP地址控件为可用
            ((EditText)clMainActivity.findViewById( R.id.PortEdit )).setEnabled( true ); //设置端口控件为可用
            ((Button)clMainActivity.findViewById( R.id.CreateServerButton )).setText( "创建服务端" ); //设置创建服务端按钮的内容为“创建服务端”
            ((Button)clMainActivity.findViewById( R.id.ConnectServerButton )).setEnabled( true ); //设置连接服务端按钮为可用
            ((Button)clMainActivity.findViewById( R.id.ConnectServerButton )).setText( "连接服务端" ); //设置连接服务端按钮的内容为“连接服务端”
            ((Button)clMainActivity.findViewById( R.id.CreateServerButton )).setEnabled( true ); //设置创建服务端按钮为可用
            ((Button)clMainActivity.findViewById( R.id.SettingButton )).setEnabled( true ); //设置设置按钮为可用
        }
        if( clMessage.what == 2 ) //如果是显示日志的消息
        {
            LinearLayout clLogLinearLayout = (LinearLayout)clMainActivity.clLayoutActivityMainView.findViewById( R.id.LogLinearLayout );
            TextView clTempTextView = new TextView(clMainActivity);
            clTempTextView.setText( (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format( new Date() ) + "：" + (String)(clMessage.obj) );
            clLogLinearLayout.addView(clTempTextView);
        }
    }
}

//音频处理线程类
class MyAudioProcessThread extends AudioProcessThread
{
    int iIsCreateServerOrClient; //创建服务端或者客户端标记，1表示创建服务端，0表示创建客户端
    String m_clIPAddressString; //IP地址字符串
    int m_iPort; //端口号
    MainActivity clMainActivity; //主界面类对象的内存指针
    Handler clMainActivityHandler; //主界面消息处理类对象的内存指针

    ServerSocket m_clServerSocket; //TCP协议服务端套接字类
    Socket m_clClientSocket; //TCP协议客户端套接字类
    long lLastPacketSendTime; //存放最后一个数据包的发送时间，用于判断连接是否中断
    long lLastPacketRecvTime; //存放最后一个数据包的接收时间，用于判断连接是否中断

    int m_i32LastSendAudioDataFrameIsActive; //最后发送的一个音频数据帧有无语音活动，1表示有语音活动，0表示无语音活动
    int iSocketPrereadSize; //本次套接字数据包的预读长度
    int m_i32SendAudioDataTimeStamp; //发送音频数据的时间戳
    int m_i32RecvAudioDataTimeStamp; //接收音频数据的时间戳
    int iIsRecvExitPacket; //是否接收到退出包，0表示否，1表示是

    int m_i32UseWhatRecvAudioOutputDataFrame; //使用什么接收音频输出数据帧，0表示链表，1表示自适应抖动缓冲器。
    LinkedList<byte[]> m_clRecvAudioOutputDataFrameLinkedList; //存放接收音频输出数据帧的链表类对象的内存指针。
    Ajb m_clAjb; //存放自适应抖动缓冲器类对象的内存指针。
    int m_iAjbMaxNeedBufferDataFrameCount; //存放自适应抖动缓冲器最大需缓冲数据帧帧数量，单位帧。
    int m_iAjbMinNeedBufferDataFrameCount; //存放自适应抖动缓冲器最小需缓冲数据帧帧数量，单位帧。

    int iIsSaveAudioDataFile; //是否保存音频数据到文件。
    FileOutputStream clAudioInputFileOutputStream; //存放音频输入数据文件的文件输出流对象的内存指针。
    FileOutputStream clAudioOutputFileOutputStream; //存放音频输出数据文件的文件输出流对象的内存指针。
    FileOutputStream clAudioResultFileOutputStream; //存放音频结果数据文件的文件输出流对象的内存指针。

    byte m_szi8TempData[]; //存放临时数据
    byte m_szi8TempData2[]; //存放临时数据
    short m_szi16TempData[]; //存放临时数据
    int m_i32Temp;
    int m_i32Temp2;
    long m_i64Temp;

    //用户定义的初始化函数，在本线程刚启动时调用一次，返回值表示是否成功，0表示成功，非0表示失败
    public long UserInit()
    {
        long p_i64Result = -1; //存放本函数执行结果的值，0表示成功，非0表示失败。

        out:
        {
            if( iIsCreateServerOrClient == 1 ) //如果是创建本地TCP协议服务端套接字接受远端TCP协议客户端套接字的连接
            {
                if( m_clServerSocket == null ) //如果还没有创建TCP协议客户端套接字
                {
                    try
                    {
                        m_clServerSocket = new ServerSocket();
                        m_clServerSocket.setReuseAddress( true );
                        m_clServerSocket.bind( new InetSocketAddress( m_clIPAddressString, m_iPort ), 1 ); //创建服务端套接字
                        m_clServerSocket.setSoTimeout( 500 ); //设置accept()函数的超时时间

                        String clInfoString = "创建TCP协议服务端套接字[" + m_clServerSocket.getInetAddress().getHostAddress() + ":" + m_clServerSocket.getLocalPort() + "]成功。";
                        Log.i( m_pclCurrentClassNameString, clInfoString );
                        Message clMessage = new Message();
                        clMessage.what = 2;
                        clMessage.obj = clInfoString;
                        clMainActivityHandler.sendMessage( clMessage );
                    }
                    catch( IOException e )
                    {
                        String clInfoString = "创建TCP协议服务端套接字[" + m_clIPAddressString + ":" + m_iPort + "]失败。原因：" + e.toString();
                        Log.e( m_pclCurrentClassNameString, clInfoString );
                        Message clMessage = new Message();
                        clMessage.what = 2;
                        clMessage.obj = clInfoString;
                        clMainActivityHandler.sendMessage( clMessage );
                        break out;
                    }
                }

                while( true )
                {
                    //接受远端TCP协议客户端套接字的连接
                    try
                    {
                        m_clClientSocket = m_clServerSocket.accept();
                    }
                    catch( IOException e )
                    {

                    }

                    if( m_clClientSocket != null ) //如果成功接受了远端TCP协议客户端套接字的连接，就开始传输数据
                    {
                        try
                        {
                            m_clServerSocket.close(); //关闭本地TCP协议服务端套接字，防止还有其他远端TCP协议客户端套接字继续连接
                        }
                        catch( IOException e )
                        {
                        }
                        m_clServerSocket = null;

                        String clInfoString = "接受了远端TCP协议客户端套接字[" + m_clClientSocket.getInetAddress().getHostAddress() + ":" + m_clClientSocket.getPort() + "]与本地TCP协议客户端套接字[" + m_clClientSocket.getLocalAddress().getHostAddress() + ":" + m_clClientSocket.getLocalPort() + "]的连接。";
                        Log.i( m_pclCurrentClassNameString, clInfoString );
                        Message clMessage = new Message();
                        clMessage.what = 2;
                        clMessage.obj = clInfoString;
                        clMainActivityHandler.sendMessage( clMessage );
                        break;
                    }

                    if( m_i32ExitFlag != 0 ) //如果本线程接收到退出请求
                    {
                        Log.i( m_pclCurrentClassNameString, "本线程接收到退出请求，开始准备退出。" );
                        break out;
                    }
                }
            }
            else if( iIsCreateServerOrClient == 0 ) //如果是创建本地TCP协议客户端套接字连接远端TCP协议服务端套接字
            {
                m_clClientSocket = new Socket();
                try
                {
                    m_clClientSocket.connect( new InetSocketAddress( m_clIPAddressString, m_iPort ), 5000 ); //连接指定的IP地址和端口号，超时时间为5秒

                    String clInfoString = "创建本地TCP协议客户端套接字[" + m_clClientSocket.getLocalAddress().getHostAddress() + ":" + m_clClientSocket.getLocalPort() + "]与远端TCP协议服务端套接字[" + m_clClientSocket.getInetAddress().getHostAddress() + ":" + m_clClientSocket.getPort() + "]的连接成功。";
                    Log.i( m_pclCurrentClassNameString, clInfoString );
                    Message clMessage = new Message();
                    clMessage.what = 2;
                    clMessage.obj = clInfoString;
                    clMainActivityHandler.sendMessage( clMessage );
                }
                catch( IOException e )
                {
                    try
                    {
                        m_clClientSocket.close(); //关闭TCP协议客户端套接字
                    }
                    catch( IOException e1 )
                    {

                    }
                    m_clClientSocket = null;

                    String clInfoString = " 创建本地TCP协议客户端套接字与远端TCP协议服务端套接字[" + m_clIPAddressString + ":" + m_iPort + "]的连接失败。原因：" + e.getMessage();
                    Log.i( m_pclCurrentClassNameString, clInfoString );
                    Message clMessage = new Message();
                    clMessage.what = 2;
                    clMessage.obj = clInfoString;
                    clMainActivityHandler.sendMessage( clMessage );
                    break out;
                }
            }

            try
            {
                m_clClientSocket.setTcpNoDelay(true); //设置TCP协议客户端套接字的TCP_NODELAY选项为true。
            }
            catch (SocketException e)
            {
                break out;
            }

            switch( m_i32UseWhatRecvAudioOutputDataFrame ) //使用什么接收音频输出数据帧。
            {
                case 0: //如果使用链表。
                {
                    //初始化音频输出数据帧的链表类对象。
                    m_clRecvAudioOutputDataFrameLinkedList = new LinkedList<byte[]>();

                    Log.i( m_pclCurrentClassNameString, "初始化音频输出数据帧的链表类对象成功。" );

                    break;
                }
                case 1: //如果使用自适应抖动缓冲器。
                {
                    //初始化自适应抖动缓冲器类对象。
                    m_clAjb = new Ajb();
                    m_i64Temp = m_clAjb.Init( m_i32SamplingRate, m_i32FrameSize, 1, 0, m_iAjbMaxNeedBufferDataFrameCount, m_iAjbMinNeedBufferDataFrameCount );
                    if( m_i64Temp == 0 )
                    {
                        Log.i( m_pclCurrentClassNameString, "初始化自适应抖动缓冲器类对象成功。返回值：" + m_i64Temp );
                    }
                    else
                    {
                        Log.i( m_pclCurrentClassNameString, "初始化自适应抖动缓冲器类对象失败。返回值：" + m_i64Temp );
                        break out;
                    }

                    break;
                }
            }

            //初始化各个音频数据文件的文件输出流对象。
            if( iIsSaveAudioDataFile != 0 )
            {
                //创建音频输入数据文件的文件输出流对象。
                try
                {
                    clAudioInputFileOutputStream = new FileOutputStream( Environment.getExternalStorageDirectory() + "/AudioInput.pcm" );
                    Log.i( m_pclCurrentClassNameString, "创建 " + Environment.getExternalStorageDirectory() + "/AudioInput.pcm 音频输入数据文件的文件输出流对象成功。");
                }
                catch( FileNotFoundException e )
                {
                    Log.e( m_pclCurrentClassNameString, "创建 " + Environment.getExternalStorageDirectory() + "/AudioInput.pcm 音频输入数据文件的文件输出流对象失败。原因：" + e.toString() );
                    break out;
                }

                //创建音频输出数据文件的文件输出流对象。
                try
                {
                    clAudioOutputFileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/AudioOutput.pcm");
                    Log.i( m_pclCurrentClassNameString, "创建 " + Environment.getExternalStorageDirectory() + "/AudioOutput.pcm 音频输出数据文件的文件输出流对象成功。");
                }
                catch( FileNotFoundException e )
                {
                    Log.e( m_pclCurrentClassNameString, "创建 " + Environment.getExternalStorageDirectory() + "/AudioOutput.pcm 音频输出数据文件的文件输出流对象失败。原因：" + e.toString() );
                    break out;
                }

                //创建音频结果数据文件的文件输出流对象。
                try
                {
                    clAudioResultFileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/AudioResult.pcm");
                    Log.i( m_pclCurrentClassNameString, "创建 " + Environment.getExternalStorageDirectory() + "/AudioResult.pcm 音频结果数据文件的文件输出流对象成功。");
                }
                catch( FileNotFoundException e )
                {
                    Log.e(m_pclCurrentClassNameString, "创建 " + Environment.getExternalStorageDirectory() + "/AudioOutput.pcm 音频结果数据文件的文件输出流对象失败。原因：" + e.toString() );
                    break out;
                }
            }

            lLastPacketSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间为当前时间。
            lLastPacketRecvTime = System.currentTimeMillis(); //设置最后一个数据包的接收时间为当前时间。

            m_i32LastSendAudioDataFrameIsActive = 0; //设置最后发送的一个音频数据帧无语音活动。
            iSocketPrereadSize = 0; //设置本次套接字数据包的预读长度为0。
            m_i32SendAudioDataTimeStamp = 0; //设置发送音频数据的时间戳为0。
            m_i32RecvAudioDataTimeStamp = 0; //设置接收音频数据的时间戳为0。
            iIsRecvExitPacket = 0; //设置没有接收到退出包。
            if( m_szi8TempData == null ) m_szi8TempData =  new byte[m_i32FrameSize * 2 + 8]; //初始化存放临时数据的数组。
            if( m_szi8TempData2 == null ) m_szi8TempData2 =  new byte[m_i32FrameSize * 2 + 8]; //初始化存放临时数据的数组。
            if( m_szi16TempData == null ) m_szi16TempData =  new short[m_i32FrameSize]; //初始化存放临时数据的数组。

            String clInfoString = "开始进行音频对讲。";
            Log.i( m_pclCurrentClassNameString, clInfoString);
            Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

            p_i64Result = 0; //设置本函数执行成功。
        }

        return p_i64Result;
    }

    //用户定义的处理函数，在本线程运行时每隔1毫秒就调用一次，返回值表示是否成功，0表示成功，非0表示失败
    public long UserProcess()
    {
        long p_i64Result = -1; //存放本函数执行结果的值，0表示成功，非0表示失败。

        out:
        {
            //接收远端发送过来的一个音频输出数据帧，并放入自适应抖动缓冲器
            try
            {
                InputStream clInputStream = m_clClientSocket.getInputStream();

                if( ( iSocketPrereadSize == 0 ) && ( clInputStream.available() >= 2 ) ) //如果还没有接收预读长度，且客户端套接字可以接收到预读长度
                {
                    //接收音频输出数据帧的预读长度
                    if( clInputStream.read( m_szi8TempData, 0, 2 ) != 2 ) //如果接收到预读长度的长度不对，就返回
                    {
                        Log.e( m_pclCurrentClassNameString, "接收到预读长度的长度不对。" );
                        break out;
                    }

                    //读取音频输出数据帧的预读长度
                    iSocketPrereadSize = (m_szi8TempData[0] & 0xFF) + (((int) (m_szi8TempData[1] & 0xFF)) << 8);
                    if( iSocketPrereadSize == 0 ) //如果预读长度为0，表示这是一个心跳包，就更新一下时间即可
                    {
                        lLastPacketRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间
                        Log.i( m_pclCurrentClassNameString, "接收到一个心跳包。" );
                    }
                    else if( iSocketPrereadSize == 0xFFFF ) //如果预读长度为0xFFFF，表示这是一个退出包
                    {
                        iIsRecvExitPacket = 1; //设置已经接收到退出包
                        lLastPacketRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间

                        String clInfoString = "接收到一个退出包。";
                        Log.i( m_pclCurrentClassNameString, clInfoString);
                        Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                        break out;
                    }
                    else if( iSocketPrereadSize < 4 ) //如果预读长度小于4，表示没有时间戳
                    {
                        Log.e( m_pclCurrentClassNameString, "接收到预读长度为" + iSocketPrereadSize + "小于4，表示没有时间戳，无法继续接收。" );
                        break out;
                    }
                    else if( iSocketPrereadSize > m_szi8TempData.length ) //如果预读长度大于接收缓存区的长度
                    {
                        Log.e( m_pclCurrentClassNameString, "接收到预读长度大于接收缓存区的长度，无法继续接收。" );
                        break out;
                    }
                }

                if( ( iSocketPrereadSize != 0 ) && ( clInputStream.available() >= iSocketPrereadSize ) ) //如果已经接收了预读长度，且该音频输出数据帧可以一次性接收完毕
                {
                    //接收音频输出数据帧的时间戳
                    if( clInputStream.read( m_szi8TempData, 0, 4 ) != 4 ) //如果接收到时间戳长度不对
                    {
                        Log.e( m_pclCurrentClassNameString, "接收到时间戳长度不对。" );
                        break out;
                    }

                    //读取音频输出数据帧的时间戳
                    m_i32RecvAudioDataTimeStamp = ( m_szi8TempData[0] & 0xFF ) + ( ( (int)( m_szi8TempData[1] & 0xFF ) ) << 8) + ( ( (int)( m_szi8TempData[2] & 0xFF ) ) << 16 ) + ( ( (int)( m_szi8TempData[3] & 0xFF ) ) << 24 );

                    //接收音频输出数据帧
                    if( iSocketPrereadSize == 4 ) //如果该音频输出数据帧是无语音活动，就不用再接收
                    {

                    }
                    else
                    {
                        if( clInputStream.read( m_szi8TempData, 0, iSocketPrereadSize - 4 ) != iSocketPrereadSize - 4 ) //如果接收到数据长度与预读长度-时间戳长度不同
                        {
                            Log.e( m_pclCurrentClassNameString, "接收到的数据长度与预读长度不同。" );
                            break out;
                        }

                        if( ( m_i32UseWhatCodec == 0 ) && ( iSocketPrereadSize - 4 != m_i32FrameSize * 2 ) ) //如果使用了PCM原始数据，但接收到的PCM格式音频输出数据帧的数据长度与帧长度不同
                        {
                            Log.e(m_pclCurrentClassNameString, "接收到的PCM格式音频数据帧的数据长度与帧长度不同。" );
                            break out;
                        }
                    }

                    lLastPacketRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间
                    Log.i( m_pclCurrentClassNameString, "接收一个音频输出数据帧成功。时间戳：" + m_i32RecvAudioDataTimeStamp + "，总长度：" + iSocketPrereadSize + "。" );

                    //将音频输出数据帧放入链表或自适应抖动缓冲器。
                    switch( m_i32UseWhatRecvAudioOutputDataFrame ) //使用什么接收音频输出数据帧。
                    {
                        case 0: //如果使用链表。
                        {
                            m_i32Temp = iSocketPrereadSize - 4;
                            byte szi8RecvAudioOutputDataFrame[] = new byte[m_i32Temp];

                            for( m_i32Temp--; m_i32Temp >= 0; m_i32Temp-- )
                            {
                                szi8RecvAudioOutputDataFrame[m_i32Temp] = m_szi8TempData[m_i32Temp];
                            }

                            m_clRecvAudioOutputDataFrameLinkedList.addLast( szi8RecvAudioOutputDataFrame );

                            break;
                        }
                        case 1: //如果使用自适应抖动缓冲器。
                        {
                            if( iSocketPrereadSize == 4 ) //如果该音频输出数据帧为无语音活动。
                            {
                                synchronized( m_clAjb )
                                {
                                    m_clAjb.PutOneByteDataFrame( m_i32RecvAudioDataTimeStamp, null, 0 );
                                }
                            }
                            else //如果该音频输出数据帧为有语音活动。
                            {
                                synchronized( m_clAjb )
                                {
                                    m_clAjb.PutOneByteDataFrame( m_i32RecvAudioDataTimeStamp, m_szi8TempData, iSocketPrereadSize - 4 );
                                }
                            }

                            break;
                        }
                    }

                    iSocketPrereadSize = 0; //清空预读长度。
                }
            }
            catch( IOException e )
            {
                String clInfoString = "接收一个音频输出数据帧失败。原因：" + e.getMessage();
                Log.e(m_pclCurrentClassNameString, clInfoString);
                Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                break out;
            }

            //发送心跳包。
            if( System.currentTimeMillis() - lLastPacketSendTime >= 500 ) //如果超过500毫秒没有发送任何数据包，就发送一个心跳包。
            {
                //设置预读长度。
                m_szi8TempData[0] = ( byte ) ( 0 );
                m_szi8TempData[1] = ( byte ) ( 0 );

                try
                {
                    OutputStream clOutputStream = m_clClientSocket.getOutputStream();
                    clOutputStream.write( m_szi8TempData, 0, 2 );
                    clOutputStream.flush(); //防止出现Software caused connection abort异常。
                    lLastPacketSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间。
                    Log.i( m_pclCurrentClassNameString, "发送一个心跳包成功。" );
                }
                catch( IOException e )
                {
                    String clInfoString = "发送一个心跳包失败。原因：" + e.getMessage();
                    Log.e( m_pclCurrentClassNameString, clInfoString );
                    Message clMessage = new Message();
                    clMessage.what = 2;
                    clMessage.obj = clInfoString;
                    clMainActivityHandler.sendMessage( clMessage );

                    break out;
                }
            }

            //判断TCP协议套接字连接是否中断。
            if( System.currentTimeMillis() - lLastPacketRecvTime > 2000 ) //如果超过2000毫秒没有接收任何数据包，就判定连接已经断开了。
            {
                String clInfoString = "超过2000毫秒没有接收任何数据包，判定连接已经断开了。";
                Log.e(m_pclCurrentClassNameString, clInfoString);
                Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                break out;
            }

            p_i64Result = 0; //设置本函数执行成功。
        }

        return p_i64Result;
    }

    //用户定义的销毁函数，在本线程退出时调用一次，返回值表示是否重新初始化，0表示直接退出，非0表示重新初始化
    public long UserDestory()
    {
        if( ( m_i32ExitFlag == 1 ) && ( m_clClientSocket != null ) && ( iIsRecvExitPacket == 0 ) ) //如果本线程接收到退出请求，且TCP协议客户端套接字类对象不为空，且没有接收到退出包
        {
            //设置预读长度
            m_szi8TempData[0] = (byte)0xFF;
            m_szi8TempData[1] = (byte)0xFF;

            try
            {
                OutputStream clOutputStream = m_clClientSocket.getOutputStream();
                clOutputStream.write( m_szi8TempData, 0, 2 );
                clOutputStream.flush(); //防止出现Software caused connection abort异常
                lLastPacketSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间

                String clInfoString = "发送一个退出包成功。";
                Log.i( m_pclCurrentClassNameString, clInfoString);
                Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);
            }
            catch (IOException e)
            {
                String clInfoString = "发送一个退出包失败。原因：" + e.getMessage();
                Log.e(m_pclCurrentClassNameString, clInfoString);
                Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);
            }
        }

        if( m_clServerSocket != null ) //销毁TCP协议服务端套接字
        {
            try
            {
                String clInfoString = "已关闭TCP协议服务端套接字[" + m_clServerSocket.getInetAddress().getHostAddress() + ":" + m_clServerSocket.getLocalPort() + "]。";
                Log.i( m_pclCurrentClassNameString, clInfoString );
                Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                m_clServerSocket.close();
            }
            catch (IOException e)
            {
            }
            m_clServerSocket = null;
        }
        if( m_clClientSocket != null ) //销毁TCP协议客户端套接字
        {
            try
            {
                String clInfoString = "已断开本地TCP协议客户端套接字[" + m_clClientSocket.getLocalAddress().getHostAddress() + ":" + m_clClientSocket.getLocalPort() + "]与远端TCP协议客户端套接字[" + m_clClientSocket.getInetAddress().getHostAddress() + ":" + m_clClientSocket.getPort() + "]的连接。";
                Log.i( m_pclCurrentClassNameString, clInfoString);
                Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                m_clClientSocket.close();
            }
            catch (IOException e)
            {
            }
            m_clClientSocket = null;
        }

        if( ( iIsCreateServerOrClient == 1 ) && ( m_i32ExitFlag == 0 ) ) //如果当前是创建服务端，且本线程未接收到退出请求。
        {
            Log.i( m_pclCurrentClassNameString, "由于当前是创建服务端，且本线程未接收到退出请求，本线程重新初始化来继续保持监听。" );
            return 1;
        }
        else if( ( iIsCreateServerOrClient == 0 ) && ( m_i32ExitFlag == 0 ) && ( iIsRecvExitPacket == 0 ) ) //如果当前是创建客户端，且本线程未接收到退出请求，且没有接收到退出包。
        {
            Log.i( m_pclCurrentClassNameString, "由于当前是创建客户端，且本线程未接收到退出请求，且没有接收到退出包，本线程在1秒后重新初始化来重连。" );
            SystemClock.sleep( 1000 ); //暂停1秒
            return 1;
        }
        else //其他情况，本线程直接退出。
        {
            //销毁接收音频输出数据帧的链表类对象。
            if( m_clRecvAudioOutputDataFrameLinkedList != null )
            {
                m_clRecvAudioOutputDataFrameLinkedList.clear();
                m_clRecvAudioOutputDataFrameLinkedList = null;
            }

            //销毁自适应抖动缓冲器类对象。
            if( m_clAjb != null )
            {
                m_clAjb.Destory();
                m_clAjb = null;
            }

            //发送本线程退出消息给主界面线程。
            Message clMessage = new Message();
            clMessage.what = 1;
            clMainActivityHandler.sendMessage( clMessage );

            return 0;
        }
    }

    //用户定义的读取音频输入数据帧函数，在采样完一个音频输入数据帧并处理完后回调一次
    public long UserReadAudioInputDataFrame( short pszi16PcmAudioInputDataFrame[], short pszi16PcmAudioResultDataFrame[], int i32VoiceActivityStatus, byte pszi8SpeexAudioInputDataFrame[], int i32SpeexAudioInputDataFrameSize, int i32SpeexAudioInputDataFrameIsNeedTrans )
    {
        long p_i64Result = -1; //存放本函数执行结果的值，0表示成功，非0表示失败。

        out:
        {
            //使用TCP协议客户端套接字发送本音频输入数据帧。
            {
                if( i32VoiceActivityStatus == 1 ) //如果本音频输入数据帧为有语音活动。
                {
                    switch( m_i32UseWhatCodec ) //使用什么编解码器。
                    {
                        case 0: //如果使用PCM原始数据。
                        {
                            for( m_i32Temp = 0; m_i32Temp < pszi16PcmAudioResultDataFrame.length; m_i32Temp++ )
                            {
                                m_szi8TempData[6 + m_i32Temp * 2] = ( byte ) ( pszi16PcmAudioResultDataFrame[m_i32Temp] & 0xFF );
                                m_szi8TempData[6 + m_i32Temp * 2 + 1] = ( byte ) ( ( pszi16PcmAudioResultDataFrame[m_i32Temp] & 0xFF00 ) >> 8 );
                            }

                            m_i32Temp = pszi16PcmAudioResultDataFrame.length * 2 + 4; //预读长度 = PCM格式音频输入数据帧长度 + 时间戳长度。

                            break;
                        }
                        case 1: //如果使用Speex编解码器。
                        {
                            if( i32SpeexAudioInputDataFrameIsNeedTrans == 1 ) //如果本Speex格式音频输入数据帧需要传输。
                            {
                                for( m_i32Temp = 0; m_i32Temp < i32SpeexAudioInputDataFrameSize; m_i32Temp++ )
                                {
                                    m_szi8TempData[6 + m_i32Temp] = pszi8SpeexAudioInputDataFrame[m_i32Temp];
                                }

                                m_i32Temp = i32SpeexAudioInputDataFrameSize + 4; //预读长度 = Speex格式音频输入数据帧长度 + 时间戳长度。
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
                    ( ( m_i32Temp == 4 ) && ( m_i32LastSendAudioDataFrameIsActive != 0 ) ) ) //如果本音频输入数据帧为无语音活动，但最后发送的一个音频数据帧为有语音活动，就发送。
                {
                    //设置预读长度。
                    m_szi8TempData[0] = ( byte ) ( m_i32Temp & 0xFF );
                    m_szi8TempData[1] = ( byte ) ( ( m_i32Temp & 0xFF00 ) >> 8 );

                    //设置时间戳。
                    m_szi8TempData[2] = ( byte ) ( m_i32SendAudioDataTimeStamp & 0xFF );
                    m_szi8TempData[3] = ( byte ) ( ( m_i32SendAudioDataTimeStamp & 0xFF00 ) >> 8 );
                    m_szi8TempData[4] = ( byte ) ( ( m_i32SendAudioDataTimeStamp & 0xFF0000 ) >> 16 );
                    m_szi8TempData[5] = ( byte ) ( ( m_i32SendAudioDataTimeStamp & 0xFF000000 ) >> 24 );

                    m_i32SendAudioDataTimeStamp += m_i32FrameSize; //时间戳递增一个音频输入数据帧的长度。

                    try
                    {
                        OutputStream clOutputStream = m_clClientSocket.getOutputStream();
                        clOutputStream.write( m_szi8TempData, 0, m_i32Temp + 2 );
                        clOutputStream.flush(); //防止出现Software caused connection abort异常。
                        lLastPacketSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。

                        Log.i( m_pclCurrentClassNameString, "发送一个音频输入数据帧成功。时间戳：" + m_i32SendAudioDataTimeStamp + "，总长度：" + m_i32Temp + "。" );
                    }
                    catch( IOException e )
                    {
                        String clInfoString = "发送一个音频输入数据帧失败。原因：" + e.getMessage();
                        Log.e( m_pclCurrentClassNameString, clInfoString );
                        Message clMessage = new Message();
                        clMessage.what = 2;
                        clMessage.obj = clInfoString;
                        clMainActivityHandler.sendMessage( clMessage );
                        break out;
                    }
                }
                else
                {
                    Log.i( m_pclCurrentClassNameString, "本音频输入数据帧为无语音活动，且最后发送的一个音频数据帧为无语音活动，无需发送。" );
                }

                //设置最后发送的一个音频数据帧有无语音活动。
                if( m_i32Temp != 4 ) m_i32LastSendAudioDataFrameIsActive = 1;
                else m_i32LastSendAudioDataFrameIsActive = 0;
            }

            //写入音频输入数据帧到文件。
            if( clAudioInputFileOutputStream != null )
            {
                for( m_i32Temp = 0; m_i32Temp < pszi16PcmAudioInputDataFrame.length; m_i32Temp++ )
                {
                    m_szi8TempData[m_i32Temp * 2] = ( byte ) ( pszi16PcmAudioInputDataFrame[m_i32Temp] & 0xFF );
                    m_szi8TempData[m_i32Temp * 2 + 1] = ( byte ) ( ( pszi16PcmAudioInputDataFrame[m_i32Temp] & 0xFF00 ) >> 8 );
                }

                try
                {
                    clAudioInputFileOutputStream.write( m_szi8TempData, 0, pszi16PcmAudioInputDataFrame.length * 2 );

                    Log.i( m_pclCurrentClassNameString, "写入音频输入数据帧到文件成功。" );
                }
                catch( IOException e )
                {
                    Log.e( m_pclCurrentClassNameString, "写入音频输入数据帧到文件失败。" );
                }
            }

            //写入音频结果数据帧到文件。
            if( clAudioResultFileOutputStream != null )
            {
                for( m_i32Temp = 0; m_i32Temp < pszi16PcmAudioResultDataFrame.length; m_i32Temp++ )
                {
                    m_szi8TempData[m_i32Temp * 2] = ( byte ) ( pszi16PcmAudioResultDataFrame[m_i32Temp] & 0xFF );
                    m_szi8TempData[m_i32Temp * 2 + 1] = ( byte ) ( ( pszi16PcmAudioResultDataFrame[m_i32Temp] & 0xFF00 ) >> 8 );
                }

                try
                {
                    clAudioResultFileOutputStream.write( m_szi8TempData, 0, pszi16PcmAudioResultDataFrame.length * 2 );

                    Log.i( m_pclCurrentClassNameString, "写入音频结果数据帧到文件成功。" );
                }
                catch( IOException e )
                {
                    Log.e( m_pclCurrentClassNameString, "写入音频结果数据帧到文件失败。" );
                }
            }

            p_i64Result = 0; //设置本函数执行成功。
        }

        return p_i64Result;
    }

    //用户定义的写入音频输出数据帧函数，在需要播放一个音频输出数据帧时回调一次。
    public void UserWriteAudioOutputDataFrame( short pszi16PcmAudioOutputDataFrame[], byte p_pszi8SpeexAudioInputDataFrame[], long p_pszi64SpeexAudioInputDataFrameSize[] )
    {
        Long p_clAjbGetAudioDataSize; //从自适应抖动缓冲器中取出的音频输出数据帧的长度。
        Integer p_clAjbBufferSize; //自适应抖动缓冲器中数据帧的数量。

        //从自适应抖动缓冲器取出一个音频输出数据帧。
        //将音频输出数据帧放入链表或自适应抖动缓冲器。
        switch( m_i32UseWhatRecvAudioOutputDataFrame ) //使用什么接收音频输出数据帧。
        {
            case 0: //如果使用链表。
            {
                byte m_szi8RecvAudioOutputDataFrame[];

                switch( m_i32UseWhatCodec ) //使用什么编解码器。
                {
                    case 0: //如果使用PCM原始数据。
                    {
                        if( ( m_clRecvAudioOutputDataFrameLinkedList.size() > 0 ) && ( m_clRecvAudioOutputDataFrameLinkedList.getFirst().length > 0 ) ) //如果接收音频输出数据帧的链表的第一个音频输出数据帧为有语音活动。
                        {
                            m_szi8RecvAudioOutputDataFrame = m_clRecvAudioOutputDataFrameLinkedList.getFirst();

                            for( m_i32Temp2 = 0; m_i32Temp2 < m_i32FrameSize; m_i32Temp2++ )
                            {
                                pszi16PcmAudioOutputDataFrame[m_i32Temp2] = ( short ) ( ( ( short ) m_szi8RecvAudioOutputDataFrame[m_i32Temp2 * 2] ) & 0xFF | ( ( short ) m_szi8RecvAudioOutputDataFrame[m_i32Temp2 * 2 + 1] ) << 8 );
                            }
                        }
                        else //如果接收音频输出数据帧的链表为空，或第一个音频输出数据帧为无语音活动。
                        {
                            for( m_i32Temp2 = 0; m_i32Temp2 < m_i32FrameSize; m_i32Temp2++ )
                            {
                                pszi16PcmAudioOutputDataFrame[m_i32Temp2] = 0;
                            }
                        }

                        break;
                    }
                    case 1: //如果使用Speex编解码器。
                    {
                        if( ( m_clRecvAudioOutputDataFrameLinkedList.size() > 0 ) && ( m_clRecvAudioOutputDataFrameLinkedList.getFirst().length > 0 ) ) //如果接收音频输出数据帧的链表的第一个音频输出数据帧为有语音活动。
                        {
                            m_szi8RecvAudioOutputDataFrame = m_clRecvAudioOutputDataFrameLinkedList.getFirst();

                            for( m_i32Temp2 = 0; m_i32Temp2 < m_szi8RecvAudioOutputDataFrame.length; m_i32Temp2++ )
                            {
                                p_pszi8SpeexAudioInputDataFrame[m_i32Temp2] = m_szi8RecvAudioOutputDataFrame[m_i32Temp2];
                            }

                            p_pszi64SpeexAudioInputDataFrameSize[0] = m_szi8RecvAudioOutputDataFrame.length;
                        }
                        else //如果接收音频输出数据帧的链表为空，或第一个音频输出数据帧为无语音活动。
                        {
                            p_pszi64SpeexAudioInputDataFrameSize[0] = 0;
                        }

                        break;
                    }
                }

                //删除接收音频输出数据帧的链表的第一个音频输出数据帧。
                if( m_clRecvAudioOutputDataFrameLinkedList.size() > 0 )
                {
                    m_clRecvAudioOutputDataFrameLinkedList.removeFirst();
                }

                break;
            }
            case 1: //如果使用自适应抖动缓冲器。
            {
                switch( m_i32UseWhatCodec ) //使用什么编解码器。
                {
                    case 0: //如果使用PCM原始数据。
                    {
                        //从自适应抖动缓冲器取出一个音频输出数据帧。
                        p_clAjbGetAudioDataSize = new Long( m_szi8TempData2.length );
                        synchronized( m_clAjb )
                        {
                            m_clAjb.GetOneByteDataFrame( m_szi8TempData2, p_clAjbGetAudioDataSize );
                        }

                        if( p_clAjbGetAudioDataSize != 0 ) //如果取出来的音频输出数据帧为有语音活动。
                        {
                            for( m_i32Temp2 = 0; m_i32Temp2 < m_i32FrameSize; m_i32Temp2++ )
                            {
                                pszi16PcmAudioOutputDataFrame[m_i32Temp2] = ( short ) ( ( ( short ) m_szi8TempData2[m_i32Temp2 * 2] ) & 0xFF | ( ( short ) m_szi8TempData2[m_i32Temp2 * 2 + 1] ) << 8 );
                            }
                        }
                        else //如果取出来的音频输出数据帧为无语音活动。
                        {
                            for( m_i32Temp2 = 0; m_i32Temp2 < m_i32FrameSize; m_i32Temp2++ )
                            {
                                pszi16PcmAudioOutputDataFrame[m_i32Temp2] = 0;
                            }
                        }

                        break;
                    }
                    case 1: //如果使用Speex编解码器。
                    {
                        //从自适应抖动缓冲器取出一个音频输出数据帧。
                        p_clAjbGetAudioDataSize = new Long( p_pszi8SpeexAudioInputDataFrame.length );
                        synchronized( m_clAjb )
                        {
                            m_clAjb.GetOneByteDataFrame( p_pszi8SpeexAudioInputDataFrame, p_clAjbGetAudioDataSize );
                        }

                        p_pszi64SpeexAudioInputDataFrameSize[0] = p_clAjbGetAudioDataSize;

                        break;
                    }
                }

                p_clAjbBufferSize = new Integer( 0 );

                m_clAjb.GetCurHaveBufferActiveDataFrameCount( p_clAjbBufferSize );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中当前已缓冲有语音活动音频数据帧数量为 " + p_clAjbBufferSize + " 个。" );
                m_clAjb.GetCurHaveBufferInactiveDataFrameCount( p_clAjbBufferSize );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中当前已缓冲无语音活动音频数据帧数量为 " + p_clAjbBufferSize + " 个。" );
                m_clAjb.GetCurHaveBufferDataFrameCount( p_clAjbBufferSize );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中当前已缓冲活动音频数据帧数量为 " + p_clAjbBufferSize + " 个。" );

                m_clAjb.GetMaxNeedBufferDataFrameCount( p_clAjbBufferSize );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中最大需缓冲音频数据帧的数量为 " + p_clAjbBufferSize + " 个。" );
                m_clAjb.GetMinNeedBufferDataFrameCount( p_clAjbBufferSize );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中最小需缓冲音频数据帧的数量为 " + p_clAjbBufferSize + " 个。" );
                m_clAjb.GetCurNeedBufferDataFrameCount( p_clAjbBufferSize );
                Log.i( m_pclCurrentClassNameString, "自适应抖动缓冲器中当前需缓冲音频数据帧的数量为 " + p_clAjbBufferSize + " 个。" );

                break;
            }
        }
    }

    //用户定义的获取PCM格式音频输出数据帧函数，在解码完一个音频输出数据帧时回调一次。
    public void UserGetPcmAudioOutputDataFrame( short pszi16PcmAudioOutputDataFrame[] )
    {
        //写入音频输出数据帧到文件。
        if( clAudioOutputFileOutputStream != null )
        {
            for( int m_i32Temp = 0; m_i32Temp < pszi16PcmAudioOutputDataFrame.length; m_i32Temp++ )
            {
                m_szi8TempData2[m_i32Temp * 2] = (byte)( pszi16PcmAudioOutputDataFrame[m_i32Temp] & 0xFF );
                m_szi8TempData2[m_i32Temp * 2 + 1] = (byte)( ( pszi16PcmAudioOutputDataFrame[m_i32Temp] & 0xFF00 ) >> 8 );
            }

            try
            {
                clAudioOutputFileOutputStream.write( m_szi8TempData2, 0, pszi16PcmAudioOutputDataFrame.length * 2 );

                Log.i( m_pclCurrentClassNameString, "写入音频输出数据帧到文件成功。" );
            }
            catch (IOException e)
            {
                Log.e( m_pclCurrentClassNameString, "写入音频输出数据帧到文件失败。" );
            }
        }
    }
}

public class MainActivity extends AppCompatActivity
{
    String m_pclCurrentClassNameString = this.getClass().getSimpleName(); //存放当前类名称字符串。

    View clLayoutActivityMainView; //存放主界面布局控件的内存指针。
    View clLayoutActivitySettingView; //存放设置界面布局控件的内存指针。
    View clLayoutActivityWebRtcAecView; //存放WebRtc声学回音消除器设置布局控件的内存指针。
    View clLayoutActivityWebRtcAecmView; //存放WebRtc移动版声学回音消除器设置布局控件的内存指针。
    View clLayoutActivitySpeexAecView; //存放Speex声学回音消除器设置布局控件的内存指针。
    View clLayoutActivityWebRtcNsxView; //存放WebRtc定点噪音抑制器设置布局控件的内存指针。
    View clLayoutActivitySpeexPreprocessorView; //存放Speex预处理器设置布局控件的内存指针。
    View clLayoutActivitySpeexCodecView; //存放Speex编解码器设置布局控件的内存指针。
    View clLayoutActivityAjbView; //存放自适应抖动缓冲器设置布局控件的内存指针。
    View clLayoutActivityReadMeView; //存放说明界面布局控件的内存指针。
    View clLayoutActivityCurrentView; //存放当前界面布局控件的内存指针。

    MainActivity clMainActivity; //存放主界面类对象的内存指针。
    MyAudioProcessThread clMyAudioProcessThread; //存放音频处理线程类对象的内存指针。
    MainActivityHandler clMainActivityHandler; //存放主界面消息处理类对象的内存指针。

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = LayoutInflater.from( this );
        clLayoutActivityMainView = layoutInflater.inflate( R.layout.activity_main, null );
        clLayoutActivitySettingView = layoutInflater.inflate( R.layout.activity_setting, null );
        clLayoutActivityWebRtcAecView  = layoutInflater.inflate( R.layout.activity_webrtcaec, null );
        clLayoutActivityWebRtcAecmView = layoutInflater.inflate( R.layout.activity_webrtcaecm, null );
        clLayoutActivitySpeexAecView = layoutInflater.inflate( R.layout.activity_speexaec, null );
        clLayoutActivityWebRtcNsxView = layoutInflater.inflate( R.layout.activity_webrtcnsx, null );
        clLayoutActivitySpeexPreprocessorView = layoutInflater.inflate( R.layout.activity_speexpreprocessor, null );
        clLayoutActivitySpeexCodecView = layoutInflater.inflate( R.layout.activity_speexcodec, null );
        clLayoutActivityAjbView = layoutInflater.inflate( R.layout.activity_ajb, null );
        clLayoutActivityReadMeView = layoutInflater.inflate( R.layout.activity_readme, null );

        setContentView( clLayoutActivityMainView ); //设置界面的内容为主界面
        clLayoutActivityCurrentView = clLayoutActivityMainView;

        clMainActivity = this;

        //请求录音权限、网络权限、写入存储权限
        //if( ContextCompat.checkSelfPermission( this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1 );
        }

        //启动初始化线程
        InitThread clInitThread;
        clInitThread = new InitThread();
        clInitThread.clLayoutActivityMainView = clLayoutActivityMainView;
        clInitThread.start();

        //初始化消息处理类对象
        clMainActivityHandler = new MainActivityHandler();
        clMainActivityHandler.clMainActivity = clMainActivity;
    }

    //清空日志按钮
    public void OnClickClearLog( View v )
    {
        LinearLayout clLogLinearLayout = (LinearLayout)clLayoutActivityMainView.findViewById( R.id.LogLinearLayout );
        clLogLinearLayout.removeAllViews();
    }

    //返回键
    @Override
    public void onBackPressed()
    {
        if( clLayoutActivityCurrentView == clLayoutActivityMainView )
        {
            Log.i( m_pclCurrentClassNameString, "用户在主界面按下返回键，本软件退出。" );
            System.exit( 0 );
        }
        else if( clLayoutActivityCurrentView == clLayoutActivitySettingView )
        {
            this.OnClickSettingOk( null );
        }
        else if( ( clLayoutActivityCurrentView == clLayoutActivityWebRtcAecView ) ||
                 ( clLayoutActivityCurrentView == clLayoutActivityWebRtcAecmView ) ||
                 ( clLayoutActivityCurrentView == clLayoutActivitySpeexAecView ) ||
                 ( clLayoutActivityCurrentView == clLayoutActivityWebRtcNsxView ) ||
                 ( clLayoutActivityCurrentView == clLayoutActivitySpeexPreprocessorView ) ||
                 ( clLayoutActivityCurrentView == clLayoutActivitySpeexCodecView ) ||
                 ( clLayoutActivityCurrentView == clLayoutActivityAjbView ) )
        {
            this.OnWebRtcAecSettingOkClick( null );
        }
        else if( clLayoutActivityCurrentView == clLayoutActivityReadMeView )
        {
            this.OnClickReadMeOk( null );
        }
    }

    //创建服务器或连接服务器按钮
    public void OnClickCreateServerAndConnectServer( View v )
    {
        long p_i64Result = -1;

        out:
        {
            if( clMyAudioProcessThread == null ) //如果音频处理线程还没有启动。
            {
                Log.i( m_pclCurrentClassNameString, "开始启动音频处理线程。" );

                clMyAudioProcessThread = new MyAudioProcessThread();

                if( v.getId() == R.id.CreateServerButton )
                {
                    clMyAudioProcessThread.iIsCreateServerOrClient = 1; //标记创建服务端接受客户端。
                }
                else if( v.getId() == R.id.ConnectServerButton )
                {
                    clMyAudioProcessThread.iIsCreateServerOrClient = 0; //标记创建客户端连接服务端。
                }

                clMyAudioProcessThread.clMainActivity = this;
                clMyAudioProcessThread.clMainActivityHandler = clMainActivityHandler;

                //设置IP地址字符串、端口、音频播放线程启动延迟。
                clMyAudioProcessThread.m_clIPAddressString = ((EditText) clLayoutActivityMainView.findViewById(R.id.IPAddressEdit)).getText().toString();
                clMyAudioProcessThread.m_iPort = Integer.parseInt(((EditText) clLayoutActivityMainView.findViewById(R.id.PortEdit)).getText().toString());

                //设置音频数据的采样频率、一帧音频数据的采样数量。
                String clTempString = ((Spinner) clLayoutActivitySettingView.findViewById(R.id.SamplingRate)).getSelectedItem().toString();
                if( clTempString.equals( "8000Hz" ) )
                {
                    clMyAudioProcessThread.SetAudioData( 8000 );
                }
                else if( clTempString.equals( "16000Hz" ) )
                {
                    clMyAudioProcessThread.SetAudioData( 16000 );
                }
                else if( clTempString.equals( "32000Hz" ) )
                {
                    clMyAudioProcessThread.SetAudioData( 32000 );
                }

                //判断是否使用WebRtc声学回音消除器。
                if( ((RadioButton)clLayoutActivitySettingView.findViewById(R.id.RadioButtonUseWebRtcAec)).isChecked() )
                {
                    try
                    {
                        clMyAudioProcessThread.SetUseWebRtcAec( Integer.parseInt(((TextView) clLayoutActivityWebRtcAecView.findViewById(R.id.WebRtcAecNlpMode)).getText().toString()) );
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }
                }

                //判断是否使用WebRtc移动版声学回音消除器。
                if( ((RadioButton)clLayoutActivitySettingView.findViewById(R.id.RadioButtonUseWebRtcAecm)).isChecked() )
                {
                    try
                    {
                        clMyAudioProcessThread.SetUseWebRtcAecm( Integer.parseInt(((TextView) clLayoutActivityWebRtcAecmView.findViewById(R.id.WebRtcAecmEchoMode)).getText().toString()), Integer.parseInt(((TextView) clLayoutActivityWebRtcAecmView.findViewById(R.id.WebRtcAecmDelay)).getText().toString()) );
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }
                }

                //判断是否使用Speex声学回音消除器。
                if( ((RadioButton)clLayoutActivitySettingView.findViewById(R.id.RadioButtonUseSpeexAec)).isChecked() )
                {
                    try
                    {
                        clMyAudioProcessThread.SetUseSpeexAec( Integer.parseInt(((TextView) clLayoutActivitySpeexAecView.findViewById(R.id.SpeexAecFilterLength)).getText().toString()) );
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }
                }

                //判断是否使用WebRtc定点噪音抑制器。
                if( ((CheckBox)clLayoutActivitySettingView.findViewById(R.id.CheckBoxIsUseWebRtcNsx)).isChecked() )
                {
                    try
                    {
                        clMyAudioProcessThread.SetWebRtcNsx( 1, Integer.parseInt(((TextView) clLayoutActivityWebRtcNsxView.findViewById(R.id.WebRtcNsxPolicyMode)).getText().toString()) );
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }

                //判断是否使用Speex预处理。
                if( ((CheckBox)clLayoutActivitySettingView.findViewById( R.id.CheckBoxIsUseSpeexPreprocessor )).isChecked() )
                {
                    try
                    {
                        clMyAudioProcessThread.SetSpeexPreprocessor(
                                1,
                                (((CheckBox)clLayoutActivitySpeexPreprocessorView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseNs )).isChecked())?1:0,
                                Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorNoiseSuppress)).getText().toString()),
                                (((CheckBox)clLayoutActivitySpeexPreprocessorView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseVad )).isChecked())?1:0,
                                Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorVadProbStart)).getText().toString()),
                                Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorVadProbContinue)).getText().toString()),
                                (((CheckBox)clLayoutActivitySpeexPreprocessorView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseAgc )).isChecked())?1:0,
                                Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorAgcLevel)).getText().toString()),
                                Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorAgcMaxGain)).getText().toString()),
                                (((CheckBox)clLayoutActivitySpeexPreprocessorView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseRec )).isChecked())?1:0,
                                Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorEchoSuppress)).getText().toString()),
                                Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorEchoSuppressActive)).getText().toString())
                                );
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }
                }

                //判断是否使用PCM原始数据。
                if( ((RadioButton)clLayoutActivitySettingView.findViewById(R.id.RadioButtonUsePcm)).isChecked() )
                {
                    clMyAudioProcessThread.SetUsePcm();
                }

                //判断是否使用Speex编解码器。
                if( ((RadioButton)clLayoutActivitySettingView.findViewById(R.id.RadioButtonUseSpeexCodec)).isChecked() )
                {
                    try
                    {
                        clMyAudioProcessThread.SetUseSpeexCodec(
                                ( ( ( RadioButton ) clLayoutActivitySpeexCodecView.findViewById( R.id.RadioButtonSpeexCodecEncoderUseCbr ) ).isChecked() ) ? 0 : 1,
                                Integer.parseInt( ( ( TextView ) clLayoutActivitySpeexCodecView.findViewById( R.id.SpeexCodecEncoderQuality ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) clLayoutActivitySpeexCodecView.findViewById( R.id.SpeexCodecEncoderComplexity ) ).getText().toString() ),
                                Integer.parseInt( ( ( TextView ) clLayoutActivitySpeexCodecView.findViewById( R.id.SpeexCodecEncoderPlcExpectedLossRate ) ).getText().toString() )
                        );
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }
                }

                //判断是否使用Opus编解码器。
                if( ((RadioButton)clLayoutActivitySettingView.findViewById(R.id.RadioButtonUseOpusCodec)).isChecked())
                {
                    clMyAudioProcessThread.SetUseOpusCodec();
                }

                //判断是否使用链表。
                if( ((RadioButton)clLayoutActivitySettingView.findViewById(R.id.RadioButtonUseList)).isChecked())
                {
                    clMyAudioProcessThread.m_i32UseWhatRecvAudioOutputDataFrame = 0;
                }

                //判断是否使用自己设计的自适应抖动缓冲器。
                if( ((RadioButton)clLayoutActivitySettingView.findViewById(R.id.RadioButtonUseAjb)).isChecked() )
                {
                    clMyAudioProcessThread.m_i32UseWhatRecvAudioOutputDataFrame = 1;

                    try
                    {
                        clMyAudioProcessThread.m_iAjbMaxNeedBufferDataFrameCount = Integer.parseInt( ( ( TextView ) clLayoutActivityAjbView.findViewById( R.id.AjbMaxNeedBufferDataFrameCount ) ).getText().toString() );
                        clMyAudioProcessThread.m_iAjbMinNeedBufferDataFrameCount = Integer.parseInt( ( ( TextView ) clLayoutActivityAjbView.findViewById( R.id.AjbMinNeedBufferDataFrameCount ) ).getText().toString() );
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }
                }

                //判断是否保存音频数据到文件。
                if( ((CheckBox)clLayoutActivitySettingView.findViewById(R.id.CheckBoxIsSaveAudioDataFile)).isChecked())
                {
                    clMyAudioProcessThread.iIsSaveAudioDataFile = 1;
                }
                else
                {
                    clMyAudioProcessThread.iIsSaveAudioDataFile = 0;
                }

                clMyAudioProcessThread.start();

                Log.i( m_pclCurrentClassNameString, "启动音频处理线程完毕。" );

                if( v.getId() == R.id.CreateServerButton )
                {
                    ((EditText)findViewById( R.id.IPAddressEdit )).setEnabled( false ); //设置IP地址控件为不可用
                    ((EditText)findViewById( R.id.PortEdit )).setEnabled( false ); //设置端口控件为不可用
                    ((Button)findViewById( R.id.CreateServerButton )).setText( "中断" ); //设置创建服务端按钮的内容为“中断”
                    ((Button)findViewById( R.id.ConnectServerButton )).setEnabled( false ); //设置连接服务端按钮为不可用
                    ((Button)findViewById( R.id.SettingButton )).setEnabled( false ); //设置设置按钮为不可用
                }
                else if( v.getId() == R.id.ConnectServerButton )
                {
                    ((EditText)findViewById( R.id.IPAddressEdit )).setEnabled( false ); //设置IP地址控件为不可用
                    ((EditText)findViewById( R.id.PortEdit )).setEnabled( false ); //设置端口控件为不可用
                    ((Button)findViewById( R.id.CreateServerButton )).setEnabled( false ); //设置创建服务端按钮为不可用
                    ((Button)findViewById( R.id.ConnectServerButton )).setText( "中断" ); //设置连接服务端按钮的内容为“中断”
                    ((Button)findViewById( R.id.SettingButton )).setEnabled( false ); //设置设置按钮为不可用
                }
            }
            else
            {
                clMyAudioProcessThread.RequireExit(); //请求音频处理线程退出

                try
                {
                    Log.i( m_pclCurrentClassNameString, "开始等待音频处理线程退出。" );
                    clMyAudioProcessThread.join(); //等待音频处理线程退出
                    Log.i( m_pclCurrentClassNameString, "结束等待音频处理线程退出。" );
                }
                catch (InterruptedException e)
                {

                }
            }

            p_i64Result = 0;
            
            break out;
        }

        if( p_i64Result != 0 )
        {
            clMyAudioProcessThread = null;
        }
    }

    //主界面设置按钮
    public void OnClickSetting( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //设置界面的确定按钮
    public void OnClickSettingOk( View clButton )
    {
        setContentView( clLayoutActivityMainView );
        clLayoutActivityCurrentView = clLayoutActivityMainView;
    }

    //WebRtc声学回音消除器设置按钮
    public void OnWebRtcAecSettingClick( View clButton )
    {
        setContentView( clLayoutActivityWebRtcAecView );
        clLayoutActivityCurrentView = clLayoutActivityWebRtcAecView;
    }

    //WebRtc声学回音消除器设置界面的确定按钮
    public void OnWebRtcAecSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //WebRtc移动版声学回音消除器设置按钮
    public void OnWebRtcAecmSettingClick( View clButton )
    {
        setContentView( clLayoutActivityWebRtcAecmView );
        clLayoutActivityCurrentView = clLayoutActivityWebRtcAecmView;
    }

    //WebRtc移动版声学回音消除器设置界面的确定按钮
    public void OnWebRtcAecmSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //Speex声学回音消除器设置按钮
    public void OnSpeexAecSettingClick( View clButton )
    {
        setContentView( clLayoutActivitySpeexAecView );
        clLayoutActivityCurrentView = clLayoutActivitySpeexAecView;
    }

    //Speex声学回音消除器设置界面的确定按钮
    public void OnSpeexAecSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //WebRtc定点噪音抑制器设置按钮
    public void OnWebRtcNsxSettingClick( View clButton )
    {
        setContentView( clLayoutActivityWebRtcNsxView );
        clLayoutActivityCurrentView = clLayoutActivityWebRtcNsxView;
    }

    //WebRtc定点噪音抑制器设置界面的确定按钮
    public void OnWebRtcNsxSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //Speex预处理器设置按钮
    public void OnSpeexPreprocessorSettingClick( View clButton )
    {
        setContentView( clLayoutActivitySpeexPreprocessorView );
        clLayoutActivityCurrentView = clLayoutActivitySpeexPreprocessorView;
    }

    //Speex预处理器设置界面的确定按钮
    public void OnSpeexPreprocessorSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //Pcm原始数据设置按钮
    public void OnPcmSettingClick( View clButton )
    {
        setContentView( clLayoutActivitySpeexCodecView );
        clLayoutActivityCurrentView = clLayoutActivitySpeexCodecView;
    }

    //Pcm原始数据设置界面的确定按钮
    public void OnPcmSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //Speex编解码器设置按钮
    public void OnSpeexCodecSettingClick( View clButton )
    {
        setContentView( clLayoutActivitySpeexCodecView );
        clLayoutActivityCurrentView = clLayoutActivitySpeexCodecView;
    }

    //Speex编解码器设置界面的确定按钮
    public void OnSpeexCodecSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //Opus编解码器设置按钮
    public void OnOpusCodecSettingClick( View clButton )
    {
        setContentView( clLayoutActivitySpeexCodecView );
        clLayoutActivityCurrentView = clLayoutActivitySpeexCodecView;
    }

    //Opus编解码器设置界面的确定按钮
    public void OnOpusCodecSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //自适应抖动缓冲器设置按钮
    public void OnAjbSettingClick( View clButton )
    {
        setContentView( clLayoutActivityAjbView );
        clLayoutActivityCurrentView = clLayoutActivityAjbView;
    }

    //自适应抖动缓冲器设置界面的确定按钮
    public void OnAjbSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //主界面说明按钮
    public void OnClickReadMe( View clButton )
    {
        setContentView( clLayoutActivityReadMeView );
        clLayoutActivityCurrentView = clLayoutActivityReadMeView;
    }

    //说明界面的确定按钮
    public void OnClickReadMeOk( View clButton )
    {
        setContentView( clLayoutActivityMainView );
        clLayoutActivityCurrentView = clLayoutActivityMainView;
    }
}
