package com.example.andrd_ado_vdo_tkbk_demo;

import HeavenTao.Media.NtwkMediaPocsThrd;
import HeavenTao.Media.TkbkNtwk;

public class MyNtwkMediaPocsThrd extends NtwkMediaPocsThrd
{
    MainAct m_MainActPt; //存放主界面的指针。

    MyNtwkMediaPocsThrd( MainAct MainActPt )
    {
        super( MainActPt.getApplicationContext() );

        m_MainActPt = MainActPt;
    }

    //用户定义的网络媒体处理线程初始化函数。
    @Override public void UserNtwkMediaPocsThrdInit()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.MyNtwkMediaPocsThrdInit );
    }

    //用户定义的网络媒体处理线程销毁函数。
    @Override public void UserNtwkMediaPocsThrdDstoy()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.MyNtwkMediaPocsThrdDstoy );
    }

    //用户定义的服务端初始化函数。
    @Override public void UserSrvrInit()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.SrvrInit );
    }

    //用户定义的服务端销毁函数。
    @Override public void UserSrvrDstoy()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.SrvrDstoy );
    }

    //用户定义的显示日志函数。
    @Override public void UserShowLog( String InfoStrPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ShowLog, InfoStrPt );
    }

    //用户定义的显示Toast函数。
    @Override public void UserShowToast( String InfoStrPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ShowToast, InfoStrPt );
    }

    //用户定义的振动函数。
    @Override public void UserVibrate()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.Vibrate );
    }

    //用户定义的连接添加函数。
    @Override public void UserCnctInit( TkbkNtwk.CnctInfo CnctInfoPt, String PrtclStrPt, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewAddItem, CnctInfoPt.m_Num, PrtclStrPt, RmtNodeNameStrPt, RmtNodeSrvcStrPt );
    }

    //用户定义的连接状态函数。
    @Override public void UserCnctSts( TkbkNtwk.CnctInfo CnctInfoPt, int CurCnctSts )
    {
        if( CurCnctSts == CnctSts.Wait )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewModifyItem, CnctInfoPt.m_Num, null, "等待远端接受连接", "" );
        }
        else if( CurCnctSts < CnctSts.Wait )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewModifyItem, CnctInfoPt.m_Num, null, "第" + -CurCnctSts + "次连接", "" );
        }
        else if( CurCnctSts == CnctSts.Cnct )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewModifyItem, CnctInfoPt.m_Num, null, "已连接", "" );
        }
        else if( CurCnctSts == CnctSts.Tmot )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewModifyItem, CnctInfoPt.m_Num, null, "异常断开", "" );
        }
        else if( CurCnctSts == CnctSts.Dsct )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewModifyItem, CnctInfoPt.m_Num, null, "已断开", "" );
        }
    }

    //用户定义的连接激活函数。
    @Override public void UserCnctAct( TkbkNtwk.CnctInfo CnctInfoPt, int IsAct )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewModifyItem, CnctInfoPt.m_Num, ( IsAct != 0 ) ? "⇶" : "", null, null );
    }

    //用户定义的连接本端对讲模式函数。
    @Override public void UserCnctLclTkbkMode( TkbkNtwk.CnctInfo CnctInfoPt, int IsRqirAct, int LclTkbkMode )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewModifyItem, CnctInfoPt.m_Num, null, ( ( IsRqirAct != 0 ) ? "请求激活" : "" ) + m_TkbkModeStrArrPt[ LclTkbkMode ], null );
    }

    //用户定义的连接远端对讲模式函数。
    @Override public void UserCnctRmtTkbkMode( TkbkNtwk.CnctInfo CnctInfoPt, int IsRqirAct, int RmtTkbkMode )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewModifyItem, CnctInfoPt.m_Num, null, null, ( ( IsRqirAct != 0 ) ? "请求激活" : "" ) + m_TkbkModeStrArrPt[ RmtTkbkMode ] );
    }

    //用户定义的连接销毁函数。
    @Override public void UserCnctDstoy( TkbkNtwk.CnctInfo CnctInfoPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewDelItem, CnctInfoPt.m_Num );
    }
}
