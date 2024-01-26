package HeavenTao.Media;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import HeavenTao.Media.*;
import HeavenTao.Data.*;
import HeavenTao.Sokt.*;

public class BdctNtwk //广播网络。
{
    public static String m_CurClsNameStrPt = "BdctNtwk"; //存放当前类名称字符串。

    NtwkMediaPocsThrd m_NtwkMediaPocsThrdPt; //存放网络媒体处理线程的指针。

    class CnctInfo //存放连接信息。
    {
        public int m_Num; //存放序号，从0开始。

        int m_IsTcpOrAudpPrtcl; //存放是否是Tcp或Udp协议，为0表示Tcp协议，为1表示高级Udp协议。
        String m_RmtNodeNameStrPt; //存放远端套接字绑定的远端节点名称字符串的指针，
        String m_RmtNodeSrvcStrPt; //存放远端套接字绑定的远端节点服务字符串的指针，
        TcpClntSokt m_TcpClntSoktPt; //存放本端Tcp协议客户端套接字的指针。
        long m_AudpClntCnctIdx; //存放本端高级Udp协议客户端连接索引。
        int m_IsRqstDstoy; //存放是否请求销毁，为0表示未请求，为1表示已请求。

        int m_CurCnctSts; //存放当前连接状态，为[-m_MaxCnctTimes,0]表示等待远端接受连接。
        int m_RmtTkbkMode; //存放远端对讲模式。

        int m_IsRecvExitPkt; //存放是否接收到退出包，为0表示否，为1表示是。
    }
    ArrayList< CnctInfo > m_CnctInfoCntnrPt = new ArrayList<>(); //存放连接信息容器的指针。

    int m_LastSendAdoInptFrmIsAct; //存放最后一个发送的音频输入帧有无语音活动，为1表示有语音活动，为0表示无语音活动。
    int m_LastSendAdoInptFrmTimeStamp; //存放最后一个发送音频输入帧的时间戳。
    int m_LastSendVdoInptFrmTimeStamp; //存放最后一个发送视频输入帧的时间戳。

    //连接信息初始化。
    public CnctInfo CnctInfoInit( int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt, TcpClntSokt TcpClntSoktPt, long AudpClntCnctIdx, int CurCnctSts )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
        CnctInfo p_CnctInfoTmpPt;

        Out:
        {
            p_CnctInfoTmpPt = new CnctInfo();

            p_CnctInfoTmpPt.m_Num = m_CnctInfoCntnrPt.size(); //设置序号。

            p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl = IsTcpOrAudpPrtcl; //设置协议为Tcp协议或高级Udp协议。
            p_CnctInfoTmpPt.m_RmtNodeNameStrPt = RmtNodeNameStrPt; //设置远端套接字绑定的远端节点名称字符串的指针。
            p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt = RmtNodeSrvcStrPt; //设置远端套接字绑定的远端节点服务字符串的指针。
            p_CnctInfoTmpPt.m_TcpClntSoktPt = TcpClntSoktPt; //设置本端Tcp协议客户端套接字的指针。
            p_CnctInfoTmpPt.m_AudpClntCnctIdx = AudpClntCnctIdx; //设置本端高级Udp协议客户端连接索引。
            p_CnctInfoTmpPt.m_IsRqstDstoy = 0; //设置是否请求销毁。

            p_CnctInfoTmpPt.m_CurCnctSts = CurCnctSts; //设置当前连接状态。
            p_CnctInfoTmpPt.m_RmtTkbkMode = NtwkMediaPocsThrd.TkbkMode.None; //设置远端对讲模式。

            p_CnctInfoTmpPt.m_IsRecvExitPkt = 0; //存放是否接收到退出包。

            m_CnctInfoCntnrPt.add( p_CnctInfoTmpPt ); //添加到连接信息容器。

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
            p_CnctInfoTmpPt = null;
        }
        return p_CnctInfoTmpPt;
    }

    //连接信息发送数据包。
    public int CnctInfoSendPkt( CnctInfo p_CnctInfoPt, byte PktPt[], long PktLenByt, int Times, int IsRlab, Vstr ErrInfoVstrPt )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            if( ( p_CnctInfoPt != null ) && ( p_CnctInfoPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Cnct ) ) //如果当前激活的连接信息的指针不为空，且当前连接状态为已连接。
            {
                //发送数据包。
                if( ( ( p_CnctInfoPt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoPt.m_TcpClntSoktPt.SendApkt( PktPt, PktLenByt, ( short ) 0, Times, 0, ErrInfoVstrPt ) == 0 ) ) ||
                    ( ( p_CnctInfoPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.SendApkt( p_CnctInfoPt.m_AudpClntCnctIdx, PktPt, PktLenByt, Times, IsRlab, ErrInfoVstrPt ) == 0 ) ) )
                {
                    //发送数据包成功。
                }
                else
                {
                    break Out; //发送数据包失败。
                }
            }
            else
            {
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) if( ErrInfoVstrPt != null ) ErrInfoVstrPt.Cpy( "连接信息的指针为空，或当前连接状态不为已连接。" );
                break Out;
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_Rslt;
    }

    //连接信息发送对讲模式包。
    public int CnctInfoSendTkbkModePkt( CnctInfo p_CnctInfoPt, int LclTkbkMode )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] = ( byte )NtwkMediaPocsThrd.PktTyp.TkbkMode;
            m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] = ( byte )LclTkbkMode;
            if( CnctInfoSendPkt( p_CnctInfoPt, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 2, 1, 1, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
            {
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoPt.hashCode() + "：发送一个对讲模式包成功。对讲模式：" + NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ LclTkbkMode ] );
            }
            else
            {
                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoPt.hashCode() + "：发送一个对讲模式包失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_Rslt;
    }

    //连接信息销毁。
    public void CnctInfoDstoy( CnctInfo CnctInfoPt )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
        CnctInfo p_CnctInfoTmpPt;

        Out:
        {
            if( CnctInfoPt != null )
            {
                //发送退出包。
                if( ( CnctInfoPt.m_IsRecvExitPkt == 0 ) && ( CnctInfoPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Cnct ) ) //如果未接收到退出包，且当前连接状态为已连接。
                {
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] = ( byte ) NtwkMediaPocsThrd.PktTyp.Exit; //设置退出包。
                    if( ( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 0 ) && ( CnctInfoPt.m_TcpClntSoktPt.SendApkt( m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1, ( short ) 0, 1, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) ||
                        ( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.SendApkt( CnctInfoPt.m_AudpClntCnctIdx, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1, 1, 1, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) )
                    {
                        String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + CnctInfoPt.hashCode() + "：发送一个退出包成功。";
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    }
                    else
                    {
                        String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + CnctInfoPt.hashCode() + "：发送一个退出包失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    }
                }

                //销毁本端Tcp协议客户端套接字。
                if( CnctInfoPt.m_TcpClntSoktPt != null )
                {
                    CnctInfoPt.m_TcpClntSoktPt.Dstoy( ( short ) -1, null );
                    CnctInfoPt.m_TcpClntSoktPt = null;
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + CnctInfoPt.hashCode() + "：销毁本端Tcp协议客户端套接字成功。" );
                }

                //销毁本端高级Udp协议客户端连接。
                if( CnctInfoPt.m_AudpClntCnctIdx != -1 )
                {
                    m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.ClosCnct( CnctInfoPt.m_AudpClntCnctIdx, null );
                    CnctInfoPt.m_AudpClntCnctIdx = -1;
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + CnctInfoPt.hashCode() + "：销毁本端高级Udp协议客户端连接成功。" );
                }

                if( ( CnctInfoPt.m_IsRecvExitPkt == 0 ) && ( CnctInfoPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Tmot ) ) //如果为客户端的连接，且未接收到退出包，且当前连接状态为异常断开，就重连。
                {
                    CnctInfoPt.m_IsRqstDstoy = 0; //设置未请求销毁。

                    CnctInfoPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Wait; //设置当前连接状态。
                    CnctInfoPt.m_RmtTkbkMode = NtwkMediaPocsThrd.TkbkMode.None; //设置远端对讲模式。

                    CnctInfoPt.m_IsRecvExitPkt = 0; //设置未接收到退出包。

                    String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + CnctInfoPt.hashCode() + "：连接异常断开，准备重连。";
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                }
                else //如果不为客户端的连接，或已接收到退出包，或当前连接状态不为异常断开，就销毁。
                {
                    //设置当前连接状态。
                    if( CnctInfoPt.m_CurCnctSts <= NtwkMediaPocsThrd.CnctSts.Wait )
                    {
                        CnctInfoPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Tmot;
                    }
                    else if( CnctInfoPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Cnct )
                    {
                        CnctInfoPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Dsct;
                    }

                    //从连接信息容器删除。
                    for( int p_CnctInfoLstNum = CnctInfoPt.m_Num + 1; p_CnctInfoLstNum < m_CnctInfoCntnrPt.size(); p_CnctInfoLstNum++ )
                    {
                        p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstNum );
                        p_CnctInfoTmpPt.m_Num--; //设置后面的连接信息的序号全部递减1。
                    }
                    m_CnctInfoCntnrPt.remove( CnctInfoPt.m_Num );

                    {
                        String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + CnctInfoPt.hashCode() + "：已销毁。";
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    }
                }

                m_NtwkMediaPocsThrdPt.IsAutoRqirExit(); //判断是否自动请求退出。
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
        return;
    }

    //连接信息全部销毁。
    public void CnctInfoAllDstoy()
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            while( !m_CnctInfoCntnrPt.isEmpty() ) CnctInfoDstoy( m_CnctInfoCntnrPt.get( 0 ) );

            m_NtwkMediaPocsThrdPt.IsAutoRqirExit(); //判断是否自动请求退出。在没有广播连接时需要这一步判断。

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
        return;
    }

    //连接初始化。
    public void CnctInit( int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
        CnctInfo p_CnctInfoTmpPt;

        Out:
        {
            for( int p_CnctInfoLstNum = 0; p_CnctInfoLstNum < m_CnctInfoCntnrPt.size(); p_CnctInfoLstNum++ )
            {
                p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstNum );
                if( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == IsTcpOrAudpPrtcl ) &&
                    ( p_CnctInfoTmpPt.m_RmtNodeNameStrPt.equals( RmtNodeNameStrPt ) ) &&
                    ( p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt.equals( RmtNodeSrvcStrPt ) ) )
                {
                    String p_InfoStrPt = "网络媒体处理线程：广播网络：已存在与远端节点" + ( ( IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + RmtNodeNameStrPt + ":" + RmtNodeSrvcStrPt + "]的连接，无需重复连接。";
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    break Out;
                }
            }

            if( ( p_CnctInfoTmpPt = CnctInfoInit( IsTcpOrAudpPrtcl, RmtNodeNameStrPt, RmtNodeSrvcStrPt, null, -1, NtwkMediaPocsThrd.CnctSts.Wait ) ) == null ) break Out; //如果连接信息初始化失败。

            //Ping一下远程节点名称，这样可以快速获取ARP条目。
            try
            {
                Runtime.getRuntime().exec( "ping -c 1 -w 1 " + RmtNodeNameStrPt );
            }
            catch( Exception ignored )
            {
            }

            String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：初始化与远端节点" + ( ( IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + RmtNodeNameStrPt + ":" + RmtNodeSrvcStrPt + "]的连接成功。";
            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
        return;
    }

    //所有连接发送音频数据包。
    public void AllCnctSendAdoPkt( byte PktPt[], long PktLenByt, int Times, int IsRlab )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
        CnctInfo p_CnctInfoTmpPt;

        Out:
        {
            for( int p_CnctInfoLstNum = 0; p_CnctInfoLstNum < m_CnctInfoCntnrPt.size(); p_CnctInfoLstNum++ )
            {
                p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstNum );
                if( ( p_CnctInfoTmpPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Cnct ) &&
                    ( ( p_CnctInfoTmpPt.m_RmtTkbkMode == NtwkMediaPocsThrd.TkbkMode.Ado ) || ( p_CnctInfoTmpPt.m_RmtTkbkMode == NtwkMediaPocsThrd.TkbkMode.AdoVdo ) ) )
                {
                    //发送数据包。
                    if( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) p_CnctInfoTmpPt.m_TcpClntSoktPt.SendApkt( PktPt, PktLenByt, ( short ) 0, Times, 0, null );
                    else m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.SendApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, PktPt, PktLenByt, Times, IsRlab, null );
                }
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
        return;
    }

    //连接销毁。
    public void CnctDstoy( int CnctIdx )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
        CnctInfo p_CnctInfoTmpPt;

        Out:
        {
            if( ( CnctIdx >= m_CnctInfoCntnrPt.size() ) || ( CnctIdx < 0 ) )
            {
                String p_InfoStrPt = "网络媒体处理线程：广播网络：没有序号为" + CnctIdx + "]的连接，无法删除。";
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                break Out;
            }

            p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( CnctIdx );
            p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。

            String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：请求销毁远端节点" + ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]的连接。";
            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
        return;
    }

    //连接处理，包括接受连接、连接服务端、接收数据包、删除连接。
    public void CnctPocs()
    {
        CnctInfo p_CnctInfoTmpPt;
        int p_TmpInt;

        //遍历连接信息容器。
        for( int p_CnctInfoLstNum = 0; p_CnctInfoLstNum < m_CnctInfoCntnrPt.size(); p_CnctInfoLstNum++ )
        {
            p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstNum );

            //用本端客户端套接字连接远端服务端套接字。
            if( ( p_CnctInfoTmpPt.m_IsRqstDstoy == 0 ) && ( p_CnctInfoTmpPt.m_CurCnctSts <= NtwkMediaPocsThrd.CnctSts.Wait ) ) //如果该连接未请求销毁，且当前连接状态为等待远端接受连接。
            {
                int p_RmtNodeAddrFamly; //存放远端节点的地址族，为4表示IPv4，为6表示IPv6，为0表示自动选择。

                { //设置远端节点的地址族。
                    try
                    {
                        InetAddress inetAddress = InetAddress.getByName( p_CnctInfoTmpPt.m_RmtNodeNameStrPt );
                        if( inetAddress.getAddress().length == 4 ) p_RmtNodeAddrFamly = 4;
                        else p_RmtNodeAddrFamly = 6;
                    }
                    catch( UnknownHostException e )
                    {
                        p_RmtNodeAddrFamly = 0;
                    }
                }

                if( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) //用本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字。
                {
                    int p_PocsRslt = -1; //存放本处理段的执行结果，为0表示成功，为非0表示失败。

                    TcpClntSoktCnctOut:
                    {
                        if( p_CnctInfoTmpPt.m_TcpClntSoktPt == null ) //如果未初始化本端Tcp协议客户端套接字。
                        {
                            if( -p_CnctInfoTmpPt.m_CurCnctSts >= m_NtwkMediaPocsThrdPt.m_MaxCnctTimes ) //如果未达到最大连接次数。
                            {
                                p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。这里只设置请求销毁，不设置当前连接状态，因为在连接信息销毁函数里要根据当前连接状态判断是否重连。

                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：已达到最大连接次数，中断连接。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break TcpClntSoktCnctOut;
                            }

                            p_CnctInfoTmpPt.m_CurCnctSts--; //递增当前连接次数。

                            {
                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：开始第 " + -p_CnctInfoTmpPt.m_CurCnctSts + " 次连接。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            }

                            p_CnctInfoTmpPt.m_TcpClntSoktPt = new TcpClntSokt();
                            if( p_CnctInfoTmpPt.m_TcpClntSoktPt.Init( p_RmtNodeAddrFamly, p_CnctInfoTmpPt.m_RmtNodeNameStrPt, p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt, null, null, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字失败。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break TcpClntSoktCnctOut;
                            }
                        }

                        if( p_CnctInfoTmpPt.m_TcpClntSoktPt.WaitCnct( ( short ) 0, m_NtwkMediaPocsThrdPt.m_TmpHTIntPt, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //如果等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字成功。
                        {
                            if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == TcpClntSokt.TcpCnctStsWait ) //如果等待远端接受连接。
                            {
                                //继续等待本端本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字。
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == TcpClntSokt.TcpCnctStsCnct ) //如果连接成功。
                            {
                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetNoDelay( 1, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetSendBufSz( 1024 * 1024, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：设置本端Tcp协议客户端套接字的发送缓冲区大小失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetRecvBufSz( 1024 * 1024, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：设置本端Tcp协议客户端套接字的接收缓冲区大小失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetKeepAlive( 1, 1, 1, 5, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：设置本端Tcp协议客户端套接字的保活机制失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.GetLclAddr( null, m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt, m_NtwkMediaPocsThrdPt.m_LclNodePortPt, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：获取本端Tcp协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.GetRmtAddr( null, m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt, m_NtwkMediaPocsThrdPt.m_RmtNodePortPt, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：获取本端Tcp协议客户端套接字连接的远端Tcp协议客户端套接字绑定的远程节点地址和端口失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                p_CnctInfoTmpPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Cnct; //设置当前连接状态为已连接。
                                CnctInfoSendTkbkModePkt( p_CnctInfoTmpPt, NtwkMediaPocsThrd.TkbkMode.Ado ); //发送对讲模式包。

                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：初始化本端Tcp协议客户端套接字[" + m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_LclNodePortPt.m_Val + "]，并连接远端Tcp协议服务端套接字[" + m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_RmtNodePortPt.m_Val + "]成功。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            }
                            else //如果连接失败。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：连接失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break TcpClntSoktCnctOut;
                            }
                        }
                        else //如果等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字失败。
                        {
                            String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break TcpClntSoktCnctOut;
                        }

                        p_PocsRslt = 0; //设置本处理段执行成功。
                    }

                    if( p_PocsRslt != 0 ) //如果本处理段执行失败。
                    {
                        if( p_CnctInfoTmpPt.m_TcpClntSoktPt != null )
                        {
                            p_CnctInfoTmpPt.m_TcpClntSoktPt.Dstoy( ( short ) -1, null );
                            p_CnctInfoTmpPt.m_TcpClntSoktPt = null;
                        }
                    }
                }
                else //用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字。
                {
                    int p_PocsRslt = -1; //存放本处理段的执行结果，为0表示成功，为非0表示失败。

                    AudpClntSoktCnctOut:
                    {
                        if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt == null ) //如果未初始化本端高级Udp协议客户端套接字。
                        {
                            m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt = new AudpSokt();
                            if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.Init( p_RmtNodeAddrFamly, null, null, ( short )0, ( short )5000, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //如果初始化本端高级Udp协议客户端套接字成功。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.GetLclAddr( null, m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt, m_NtwkMediaPocsThrdPt.m_LclNodePortPt, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果获取本端高级Udp协议套接字绑定的本地节点地址和端口失败。
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：广播网络：获取本端高级Udp协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break AudpClntSoktCnctOut;
                                }

                                String p_InfoStrPt = "网络媒体处理线程：广播网络：初始化本端高级Udp协议客户端套接字[" + m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_LclNodePortPt.m_Val + "]成功。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            }
                            else //如果初始化本端高级Udp协议客户端套接字失败。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：广播网络：初始化本端高级Udp协议客户端套接字[" + null + ":" + null + "]失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break AudpClntSoktCnctOut;
                            }
                        }

                        if( p_CnctInfoTmpPt.m_AudpClntCnctIdx == -1 )
                        {
                            if( -p_CnctInfoTmpPt.m_CurCnctSts >= m_NtwkMediaPocsThrdPt.m_MaxCnctTimes ) //如果未达到最大连接次数。
                            {
                                p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。这里只设置请求销毁，不设置当前连接状态，因为在连接信息销毁函数里要根据当前连接状态判断是否重连。

                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：已达到最大连接次数，中断连接。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break AudpClntSoktCnctOut;
                            }

                            p_CnctInfoTmpPt.m_CurCnctSts--; //递增当前连接次数。

                            {
                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：开始第 " + -p_CnctInfoTmpPt.m_CurCnctSts + " 次连接。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            }

                            if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.Cnct( p_RmtNodeAddrFamly, p_CnctInfoTmpPt.m_RmtNodeNameStrPt, p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt, m_NtwkMediaPocsThrdPt.m_TmpHTLongPt, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字失败。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break AudpClntSoktCnctOut;
                            }
                            p_CnctInfoTmpPt.m_AudpClntCnctIdx = m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val; //设置本端高级Udp协议客户端连接索引。
                        }

                        if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.WaitCnct( p_CnctInfoTmpPt.m_AudpClntCnctIdx, ( short )0, m_NtwkMediaPocsThrdPt.m_TmpHTIntPt, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //循环等待本端高级Udp协议套接字连接远端成功。
                        {
                            if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsWait ) //如果等待远端接受连接。
                            {
                                //重新循环，继续等待本端高级Udp协议套接字连接远端。
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsCnct ) //如果连接成功。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.GetRmtAddr( p_CnctInfoTmpPt.m_AudpClntCnctIdx, null, m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt, m_NtwkMediaPocsThrdPt.m_RmtNodePortPt, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：获取本端高级Udp协议客户端套接字连接的远端高级Udp协议服务端套接字绑定的远程节点地址和端口失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break AudpClntSoktCnctOut;
                                }

                                p_CnctInfoTmpPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Cnct; //设置当前连接状态为已连接。
                                CnctInfoSendTkbkModePkt( p_CnctInfoTmpPt, NtwkMediaPocsThrd.TkbkMode.Ado ); //发送对讲模式包。

                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_RmtNodePortPt.m_Val + "]成功。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsTmot ) //如果连接超时。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：连接超时。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break AudpClntSoktCnctOut;
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsDsct ) //如果连接断开。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：连接断开。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break AudpClntSoktCnctOut;
                            }
                        }

                        p_PocsRslt = 0; //设置本处理段执行成功。
                    }

                    if( p_PocsRslt != 0 ) //如果本处理段执行失败。
                    {
                        if( ( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt != null ) && ( p_CnctInfoTmpPt.m_AudpClntCnctIdx != -1 ) )
                        {
                            m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.ClosCnct( p_CnctInfoTmpPt.m_AudpClntCnctIdx, null );
                            p_CnctInfoTmpPt.m_AudpClntCnctIdx = -1;
                        }
                    }
                }
            }

            //用本端客户端套接字接收远端服务端套接字发送过来的一个数据包。
            if( ( p_CnctInfoTmpPt.m_IsRqstDstoy == 0 ) && ( p_CnctInfoTmpPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Cnct ) ) //如果该连接未请求销毁，且当前连接状态为已连接。
            {
                RecvPktOut:
                {
                    if( ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmpPt.m_TcpClntSoktPt.RecvApkt( m_NtwkMediaPocsThrdPt.m_TmpBytePt, m_NtwkMediaPocsThrdPt.m_TmpBytePt.length, m_NtwkMediaPocsThrdPt.m_TmpHTLongPt, ( short ) 0, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) ||
                        ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.RecvApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, m_NtwkMediaPocsThrdPt.m_TmpBytePt, m_NtwkMediaPocsThrdPt.m_TmpBytePt.length, m_NtwkMediaPocsThrdPt.m_TmpHTLongPt, null, ( short ) 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) )
                    {
                        if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val != -1 ) //如果用本端套接字接收一个连接的远端套接字发送的数据包成功。
                        {
                            if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val == 0 ) //如果数据包的数据长度为0。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个数据包的数据长度为" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "，表示没有数据，无法继续接收。" );
                                break RecvPktOut;
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] == ( byte ) NtwkMediaPocsThrd.PktTyp.TkbkMode ) //如果是对讲模式包。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 ) //如果音频输出帧包的数据长度小于1 + 1，表示没有对讲模式。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个对讲模式包的数据长度为" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲模式，无法继续接收。" );
                                    break RecvPktOut;
                                }
                                if( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] >= NtwkMediaPocsThrd.TkbkMode.NoChg )
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个对讲模式包的对讲模式为" + m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] + "不正确，无法继续接收。" );
                                    break RecvPktOut;
                                }

                                p_CnctInfoTmpPt.m_RmtTkbkMode = m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ]; //设置远端对讲模式。
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个对讲模式包。对讲模式：" + NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ p_CnctInfoTmpPt.m_RmtTkbkMode ] );
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] == ( byte ) NtwkMediaPocsThrd.PktTyp.AdoFrm ) //如果是音频输出帧包。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val < 1 + 4 ) //如果音频输出帧包的数据长度小于1 + 4，表示没有音频输出帧时间戳。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个音频输出帧包的数据长度为" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 4，表示没有音频输出帧时间戳，无法继续接收。" );
                                    break RecvPktOut;
                                }

                                //读取音频输出帧时间戳。
                                p_TmpInt = ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] & 0xFF ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 2 ] & 0xFF ) << 8 ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 3 ] & 0xFF ) << 16 ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 4 ] & 0xFF ) << 24 );

                                if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个有语音活动的音频输出帧包成功，但未初始化音频输出。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                }
                                else //如果该音频输出帧为无语音活动。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个无语音活动的音频输出帧包成功，但未初始化音频输出。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                }
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] == ( byte ) NtwkMediaPocsThrd.PktTyp.VdoFrm ) //如果是视频输出帧包。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val < 1 + 4 ) //如果视频输出帧包的数据长度小于1 + 4，表示没有视频输出帧时间戳。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个视频输出帧包的数据长度为" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 4，表示没有视频输出帧时间戳，无法继续接收。" );
                                    break RecvPktOut;
                                }

                                //读取视频输出帧时间戳。
                                p_TmpInt = ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] & 0xFF ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 2 ] & 0xFF ) << 8 ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 3 ] & 0xFF ) << 16 ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 4 ] & 0xFF ) << 24 );

                                if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个有图像活动的视频输出帧包成功，但未初始化视频输出。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                }
                                else //如果该视频输出帧为无图像活动。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个无图像活动的视频输出帧包成功，但未初始化视频输出。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                }
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] == ( byte ) NtwkMediaPocsThrd.PktTyp.Exit ) //如果是退出包。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val > 1 ) //如果退出包的数据长度大于1。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个退出包的数据长度为" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "大于1，表示还有其他数据，无法继续接收。" );
                                    break RecvPktOut;
                                }

                                p_CnctInfoTmpPt.m_IsRecvExitPkt = 1; //设置已经接收到退出包。
                                p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。

                                String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个退出包。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                if( m_NtwkMediaPocsThrdPt.m_IsShowToast != 0 ) m_NtwkMediaPocsThrdPt.UserShowToast( p_InfoStrPt );
                            }
                        } //如果用本端套接字接收一个连接的远端套接字发送的数据包超时，就重新接收。
                    }
                    else //如果用本端套接字接收一个连接的远端套接字发送的数据包失败。
                    {
                        p_CnctInfoTmpPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Tmot; //设置当前连接状态为异常断开。
                        p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。

                        String p_InfoStrPt = "网络媒体处理线程：广播网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端套接字接收一个连接的远端套接字发送的数据包失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                        break RecvPktOut;
                    }
                }
            }

            //销毁连接信息。
            if( p_CnctInfoTmpPt.m_IsRqstDstoy == 1 ) //如果该连接已请求销毁。
            {
                CnctInfoDstoy( p_CnctInfoTmpPt );
                p_CnctInfoLstNum--;
            }
        }
    }

    //用户定义的读取音视频输入帧函数。
    public void UserReadAdoVdoInptFrm( short AdoInptPcmSrcFrmPt[], short AdoInptPcmRsltFrmPt[], long AdoInptPcmFrmLenUnit, int AdoInptPcmRsltFrmVoiceActSts,
                                       byte AdoInptEncdRsltFrmPt[], long AdoInptEncdRsltFrmLenByt, int AdoInptEncdRsltFrmIsNeedTrans,
                                       byte VdoInptNv21SrcFrmPt[], int VdoInptNv21SrcFrmWidthPt, int VdoInptNv21SrcFrmHeightPt, long VdoInptNv21SrcFrmLenByt,
                                       byte VdoInptYu12RsltFrmPt[], int VdoInptYu12RsltFrmWidth, int VdoInptYu12RsltFrmHeight, long VdoInptYu12RsltFrmLenByt,
                                       byte VdoInptEncdRsltFrmPt[], long VdoInptEncdRsltFrmLenByt )
    {
        int p_PktLenByt = 0; //存放数据包的长度，单位字节。
        int p_TmpInt32 = 0;

        //发送音频输入帧。
        if( AdoInptPcmSrcFrmPt != null ) //如果有音频输入Pcm格式原始帧。
        {
            if( AdoInptEncdRsltFrmPt == null ) //如果没有音频输入已编码格式结果帧。
            {
                if( AdoInptPcmRsltFrmVoiceActSts != 0 ) //如果音频输入Pcm格式结果帧为有语音活动。
                {
                    for( p_TmpInt32 = 0; p_TmpInt32 < AdoInptPcmRsltFrmPt.length; p_TmpInt32++ ) //设置音频输入帧。
                    {
                        m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 + 4 + p_TmpInt32 * 2 ] = ( byte ) ( AdoInptPcmRsltFrmPt[ p_TmpInt32 ] & 0xFF );
                        m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 + 4 + p_TmpInt32 * 2 + 1 ] = ( byte ) ( ( AdoInptPcmRsltFrmPt[ p_TmpInt32 ] & 0xFF00 ) >> 8 );
                    }
                    p_PktLenByt = 1 + 4 + AdoInptPcmRsltFrmPt.length * 2; //数据包长度 = 数据包类型 + 音频输入帧时间戳 + 音频输入Pcm格式结果帧。
                }
                else //如果音频输入Pcm格式结果帧为无语音活动。
                {
                    p_PktLenByt = 1 + 4; //数据包长度 = 数据包类型 + 音频输入帧时间戳。
                }
            }
            else //如果有音频输入已编码格式结果帧。
            {
                if( AdoInptPcmRsltFrmVoiceActSts != 0 && AdoInptEncdRsltFrmIsNeedTrans != 0 ) //如果音频输入Pcm格式结果帧为有语音活动，且音频输入已编码格式结果帧需要传输。
                {
                    System.arraycopy( AdoInptEncdRsltFrmPt, 0, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1 + 4, ( int ) AdoInptEncdRsltFrmLenByt ); //设置音频输入帧。
                    p_PktLenByt = 1 + 4 + ( int ) AdoInptEncdRsltFrmLenByt; //数据包长度 = 数据包类型 + 音频输入帧时间戳 + 音频输入已编码格式结果帧。
                }
                else //如果音频输入Pcm格式结果帧为无语音活动，或不需要传输。
                {
                    p_PktLenByt = 1 + 4; //数据包长度 = 数据包类型 + 音频输入帧时间戳。
                }
            }

            if( p_PktLenByt != 1 + 4 ) //如果本次音频输入帧为有语音活动，就发送。
            {
                m_LastSendAdoInptFrmTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

                //设置数据包类型为音频输入帧包。
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] = ( byte ) NtwkMediaPocsThrd.PktTyp.AdoFrm;
                //设置音频输入帧时间戳。
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] = ( byte ) ( m_LastSendAdoInptFrmTimeStamp & 0xFF );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 2 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 3 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 4 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

                AllCnctSendAdoPkt( m_NtwkMediaPocsThrdPt.m_TmpBytePt, p_PktLenByt, 1, 0 );
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：发送一个有语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_PktLenByt + "。" );

                m_LastSendAdoInptFrmIsAct = 1; //设置最后一个发送的音频输入帧有语音活动。
            }
            else //如果本次音频输入帧为无语音活动。
            {
                if( m_LastSendAdoInptFrmIsAct != 0 ) //如果最后一个发送的音频输入帧为有语音活动，就发送。
                {
                    m_LastSendAdoInptFrmTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

                    //设置数据包类型为音频输入帧包。
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] = ( byte ) NtwkMediaPocsThrd.PktTyp.AdoFrm;
                    //设置音频输入帧时间戳。
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] = ( byte ) ( m_LastSendAdoInptFrmTimeStamp & 0xFF );
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 2 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 3 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 4 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

                    AllCnctSendAdoPkt( m_NtwkMediaPocsThrdPt.m_TmpBytePt, p_PktLenByt, 1, 0 );
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：发送一个无语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_PktLenByt + "。" );

                    m_LastSendAdoInptFrmIsAct = 0; //设置最后一个发送的音频输入帧无语音活动。
                }
                else //如果最后一个发送的音频输入帧为无语音活动，无需发送。
                {
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：广播网络：本次音频输入帧为无语音活动，且最后一个发送的音频输入帧为无语音活动，无需发送。" );
                }
            }
        }
    }
}
