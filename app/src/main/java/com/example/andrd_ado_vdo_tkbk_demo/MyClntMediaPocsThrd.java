package com.example.andrd_ado_vdo_tkbk_demo;

import android.view.SurfaceView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import HeavenTao.Media.BdctClnt;
import HeavenTao.Media.ClntMediaPocsThrd;
import HeavenTao.Media.TkbkClnt;
import HeavenTao.Media.VdoOtpt;

public class MyClntMediaPocsThrd extends ClntMediaPocsThrd
{
    MainAct m_MainActPt; //存放主界面的指针。
    public int m_TkbkClntNum; //存放对讲客户端的序号。

    public MyClntMediaPocsThrd( MainAct MainActPt )
    {
        super( MainActPt );

        m_MainActPt = MainActPt;
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

    //用户定义的客户端媒体处理线程初始化函数。
    @Override public void UserClntMediaPocsThrdInit()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.MyClntMediaPocsThrdInit );
    }

    //用户定义的客户端媒体处理线程销毁函数。
    @Override public void UserClntMediaPocsThrdDstoy()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.MyClntMediaPocsThrdDstoy );
    }

    //用户定义的对讲客户端连接初始化函数。
    @Override public void UserTkbkClntCnctInit( int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.TkbkClntCnctInit );
    }

    //用户定义的对讲客户端连接销毁函数。
    @Override public void UserTkbkClntCnctDstoy()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, m_TkbkClntNum, "", null );
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.TkbkClntCnctDstoy );
        m_MainActPt.SendVdoInptOtptViewDstoyMsg( m_VdoInptPt.m_DvcPt.m_PrvwSurfaceViewPt );
    }

    //用户定义的对讲客户端连接状态函数。
    @Override public void UserTkbkClntCnctSts( int CurCnctSts )
    {
        if( CurCnctSts == ClntMediaPocsThrd.CnctSts.Wait )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, m_TkbkClntNum, "等待远端接受连接", null );
        }
        else if( CurCnctSts < ClntMediaPocsThrd.CnctSts.Wait )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, m_TkbkClntNum, "第" + -CurCnctSts + "次连接", null );
        }
        else if( CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, m_TkbkClntNum, "已连接", null );
        }
        else if( CurCnctSts == ClntMediaPocsThrd.CnctSts.Tmot )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, m_TkbkClntNum, "异常断开", null );
        }
        else if( CurCnctSts == ClntMediaPocsThrd.CnctSts.Dsct )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, m_TkbkClntNum, "已断开", null );
        }
    }

    //用户定义的对讲客户端我的对讲索引函数。
    @Override public void UserTkbkClntMyTkbkIdx( int MyTkbkIdx )
    {
        m_MainActPt.SendVdoInptOtptViewSetTitleMsg( m_VdoInptPt.m_DvcPt.m_PrvwSurfaceViewPt, "视频输入预览" + MyTkbkIdx );
    }

    //用户定义的对讲客户端本端对讲模式函数。
    @Override public void UserTkbkClntLclTkbkMode( int OldLclTkbkMode, int NewLclTkbkMode )
    {
        if( ( NewLclTkbkMode & TkbkMode.AdoInpt ) != 0 ) //如果新对讲模式有音频输入。
        {
            if( ( OldLclTkbkMode & TkbkMode.AdoInpt ) == 0 ) //如果旧对讲模式无音频输入。
            {
                if( m_AdoInptPt.m_IsInit == 0 ) //如果音频输入未初始化。
                {
                    SetToUseAdoInpt(); //设置要使用音频输入。
                }
            }
        }
        else //如果新对讲模式无音频输入。
        {
            if( ( OldLclTkbkMode & TkbkMode.AdoInpt ) != 0 ) //如果旧对讲模式有音频输入。
            {
                SetNotUseAdoInpt(); //设置不使用音频输入。
            }
        }

        if( ( NewLclTkbkMode & TkbkMode.AdoOtpt ) != 0 ) //如果新对讲模式有音频输出。
        {
            if( ( OldLclTkbkMode & TkbkMode.AdoOtpt ) == 0 ) //如果旧对讲模式无音频输出。
            {
                SetToUseAdoOtpt(); //设置要使用音频输出。
            }
        }
        else //如果新对讲模式无音频输出。
        {
            if( ( OldLclTkbkMode & TkbkMode.AdoOtpt ) != 0 ) //如果旧对讲模式有音频输出。
            {
                SetNotUseAdoOtpt(); //设置不使用音频输入。
            }
        }

        if( ( NewLclTkbkMode & TkbkMode.VdoInpt ) != 0 ) //如果新对讲模式有视频输入。
        {
            if( ( OldLclTkbkMode & TkbkMode.VdoInpt ) == 0 ) //如果旧对讲模式无视频输入。
            {
                SetToUseVdoInpt(); //设置要使用视频输入。
            }
        }
        else //如果新对讲模式无视频输入。
        {
            if( ( OldLclTkbkMode & TkbkMode.VdoInpt ) != 0 ) //如果旧对讲模式有视频输入。
            {
                SetNotUseVdoInpt(); //设置不使用视频输入。
            }
        }

        if( ( NewLclTkbkMode & TkbkMode.VdoOtpt ) != 0 ) //如果新对讲模式有视频输出。
        {
            if( ( OldLclTkbkMode & TkbkMode.VdoOtpt ) == 0 ) //如果旧对讲模式无视频输出。
            {
                SetToUseVdoOtpt(); //设置要使用视频输出。
            }
        }
        else //如果新对讲模式无视频输出。
        {
            if( ( OldLclTkbkMode & TkbkMode.VdoOtpt ) != 0 ) //如果旧对讲模式有视频输出。
            {
                SetNotUseVdoOtpt(); //设置不使用视频输出。
            }
        }

        //设置是否保存音视频输入输出到Avi文件。
        if( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoVdoInptOtptToAviFileCkBoxId ) ).isChecked() )
        {
            String p_FullPathStrPt;

            p_FullPathStrPt = ( ( EditText ) m_MainActPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileFullPathEdTxtId ) ).getText().toString();
            if( p_FullPathStrPt.charAt( 0 ) != '/' ) p_FullPathStrPt = m_MainActPt.m_ExternalDirFullAbsPathStrPt + "/" + p_FullPathStrPt;

            try
            {
                m_MainActPt.m_MyClntMediaPocsThrdPt.SetIsSaveAdoVdoInptOtptToAviFile( 0,
                                                                                      p_FullPathStrPt,
                                                                                      Integer.parseInt( ( ( TextView ) m_MainActPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileWrBufSzBytEdTxtId ) ).getText().toString() ),
                                                                                      Integer.parseInt( ( ( TextView ) m_MainActPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileMaxStrmNumEdTxtId ) ).getText().toString() ),
                                                                                      ( ( ( CheckBox ) m_MainActPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveAdoInptCkBoxId ) ).isChecked() ) ? 1 : 0,
                                                                                      ( ( ( CheckBox ) m_MainActPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveAdoOtptCkBoxId ) ).isChecked() ) ? 1 : 0,
                                                                                      ( ( ( CheckBox ) m_MainActPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveVdoInptCkBoxId ) ).isChecked() ) ? 1 : 0,
                                                                                      ( ( ( CheckBox ) m_MainActPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveVdoOtptCkBoxId ) ).isChecked() ) ? 1 : 0 );
            }
            catch( NumberFormatException e )
            {
                Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
            }
        }

        //设置是否保存状态到Txt文件。
        if( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsSaveStsToTxtFileCkBoxId ) ).isChecked() )
        {
            String p_FullPathStrPt;

            p_FullPathStrPt = ( ( EditText ) m_MainActPt.m_SaveStsToTxtFileStngLyotViewPt.findViewById( R.id.SaveStsToTxtFileFullPathEdTxtId ) ).getText().toString();
            if( p_FullPathStrPt.charAt( 0 ) != '/' ) p_FullPathStrPt = m_MainActPt.m_ExternalDirFullAbsPathStrPt + "/" + p_FullPathStrPt;

            m_MainActPt.m_MyClntMediaPocsThrdPt.SaveStsToTxtFile( 0, p_FullPathStrPt );
        }
    }

    //用户定义的对讲客户端对讲信息初始化函数。
    @Override public void UserTkbkClntTkbkInfoInit( TkbkClnt.TkbkInfo TkbkInfoPt )
    {

    }

    //用户定义的对讲客户端对讲信息销毁函数。
    @Override public void UserTkbkClntTkbkInfoDstoy( TkbkClnt.TkbkInfo TkbkInfoPt )
    {
        AdoOtptDelStrm( 1, 0, TkbkInfoPt.m_TkbkIdx ); //删除流操作需要立即执行，因为要防止中途出现其他消息导致重复删除流。

        for( VdoOtpt.Strm p_StrmPt : m_VdoOtptPt.m_StrmCntnrPt )
        {
            if( p_StrmPt.m_Idx == TkbkInfoPt.m_TkbkIdx )
            {
                m_MainActPt.SendVdoInptOtptViewDstoyMsg( p_StrmPt.m_DvcPt.m_DspySurfaceViewPt );
                break;
            }
        }
        VdoOtptDelStrm( 1, 0, TkbkInfoPt.m_TkbkIdx ); //删除流操作需要立即执行，因为要防止中途出现其他消息导致重复删除流。
    }

    //用户定义的对讲客户端对讲信息远端对讲模式函数。
    @Override public void UserTkbkClntTkbkInfoRmtTkbkMode( TkbkClnt.TkbkInfo TkbkInfoPt, int OldRmtTkbkMode, int NewRmtTkbkMode )
    {
        //设置音频输出流。
        if( ( NewRmtTkbkMode & TkbkMode.AdoInpt ) != 0 ) //如果新对讲模式有音频输入。
        {
            if( ( OldRmtTkbkMode & TkbkMode.AdoInpt ) == 0 ) //如果旧对讲模式无音频输入。
            {
                if( ( m_TkbkClntPt.m_LclTkbkMode & TkbkMode.AdoOtpt ) != 0 ) //如果本端对讲模式有音频输出。
                {
                    SetToUseAdoOtptStrm( TkbkInfoPt.m_TkbkIdx ); //设置要使用音频输出流。
                }
            }
        }
        else //如果新对讲模式无音频输出。
        {
            if( ( OldRmtTkbkMode & TkbkMode.AdoInpt ) != 0 ) //如果旧对讲模式有音频输入。
            {
                if( ( m_TkbkClntPt.m_LclTkbkMode & TkbkMode.AdoOtpt ) != 0 ) //如果本端对讲模式有音频输出。
                {
                    SetNotUseAdoOtptStrm( TkbkInfoPt.m_TkbkIdx ); //设置不使用音频输出流。
                }
            }
        }

        //设置视频输出流。
        if( ( NewRmtTkbkMode & TkbkMode.VdoInpt ) != 0 ) //如果新对讲模式有视频输入。
        {
            if( ( OldRmtTkbkMode & TkbkMode.VdoInpt ) == 0 ) //如果旧对讲模式无视频输入。
            {
                if( ( m_TkbkClntPt.m_LclTkbkMode & TkbkMode.VdoOtpt ) != 0 ) //如果本端对讲模式有视频输出。
                {
                    SetToUseVdoOtptStrm( TkbkInfoPt.m_TkbkIdx ); //设置要使用视频输出流。
                }
            }
        }
        else //如果新对讲模式无视频输入。
        {
            if( ( OldRmtTkbkMode & TkbkMode.VdoInpt ) != 0 ) //如果旧对讲模式有视频输入。
            {
                if( ( m_TkbkClntPt.m_LclTkbkMode & TkbkMode.VdoOtpt ) != 0 ) //如果本端对讲模式有视频输出。
                {
                    SetNotUseVdoOtptStrm( TkbkInfoPt.m_TkbkIdx ); //设置不使用视频输出流。
                }
            }
        }
    }

    //用户定义的广播客户端初始化函数。
    @Override public void UserBdctClntInit()
    {
        if( m_AdoInptPt.m_IsInit == 0 ) //如果音频输入未初始化。
        {
            SetToUseAdoInpt(); //设置要使用音频输入。
        }
    }

    //用户定义的广播客户端销毁函数。
    @Override public void UserBdctClntDstoy()
    {

    }

    //用户定义的广播客户端连接初始化函数。
    @Override public void UserBdctClntCnctInit( BdctClnt.CnctInfo CnctInfoPt, int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt )
    {

    }

    //用户定义的广播客户端连接销毁函数。
    @Override public void UserBdctClntCnctDstoy( BdctClnt.CnctInfo CnctInfoPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, CnctInfoPt.m_Num, null, "" );
    }

    //用户定义的广播客户端连接状态函数。
    @Override public void UserBdctClntCnctSts( BdctClnt.CnctInfo CnctInfoPt, int CurCnctSts )
    {
        if( CurCnctSts == ClntMediaPocsThrd.CnctSts.Wait )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, CnctInfoPt.m_Num, null, "等待远端接受连接" );
        }
        else if( CurCnctSts < ClntMediaPocsThrd.CnctSts.Wait )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, CnctInfoPt.m_Num, null, "第" + -CurCnctSts + "次连接" );
        }
        else if( CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, CnctInfoPt.m_Num, null, "已连接" );
        }
        else if( CurCnctSts == ClntMediaPocsThrd.CnctSts.Tmot )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, CnctInfoPt.m_Num, null, "异常断开" );
        }
        else if( CurCnctSts == ClntMediaPocsThrd.CnctSts.Dsct )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ClntLstModifyItem, CnctInfoPt.m_Num, null, "已断开" );
        }
    }

    //设置要使用音频输入。
    void SetToUseAdoInpt()
    {
        //设置音频输入。
        SetAdoInpt( 0,
                    ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate8000RdBtnId ) ).isChecked() ) ? 8000 :
                        ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).isChecked() ) ? 16000 :
                            ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate32000RdBtnId ) ).isChecked() ) ? 32000 :
                    ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate48000RdBtnId ) ).isChecked() ) ? 48000 : 0,
                        ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen10msRdBtnId ) ).isChecked() ) ? 10 :
                            ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).isChecked() ) ? 20 :
                                ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen30msRdBtnId ) ).isChecked() ) ? 30 : 0,
                    ( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsStartRecordingAfterReadCkBoxId ) ).isChecked() ) ? 1 : 0 );

        //设置音频输入是否使用系统自带声学回音消除器、噪音抑制器和自动增益控制器。
        AdoInptSetIsUseSystemAecNsAgc( 0,
                                       ( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).isChecked() ) ? 1 : 0 );

        if( m_TkbkClntPt.m_XfrMode == 0 ) //如果传输模式为实时半双工（一键通）。
        {
            AdoInptSetUseNoAec( 0 );
        }
        else //如果传输模式为实时全双工。
        {
            //设置音频输入是否不使用声学回音消除器。
            if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseNoAecRdBtnId ) ).isChecked() )
            {
                AdoInptSetUseNoAec( 0 );
            }

            //设置音频输入是否使用Speex声学回音消除器。
            if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexAecRdBtnId ) ).isChecked() )
            {
                try
                {
                    AdoInptSetUseSpeexAec( 0,
                                           Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenMsecEdTxtId ) ).getText().toString() ),
                                           ( ( ( CheckBox ) m_MainActPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).isChecked() ) ? 1 : 0,
                                           Float.parseFloat( ( ( TextView ) m_MainActPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).getText().toString() ),
                                           Float.parseFloat( ( ( TextView ) m_MainActPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).getText().toString() ),
                                           Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).getText().toString() ),
                                           Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).getText().toString() ),
                                           ( ( ( CheckBox ) m_MainActPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).isChecked() ) ? 1 : 0,
                                           m_MainActPt.m_ExternalDirFullAbsPathStrPt + "/SpeexAecMem" );
                }
                catch( NumberFormatException e )
                {
                    Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
                }
            }

            //设置音频输入是否使用WebRtc定点版声学回音消除器。
            if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecmRdBtnId ) ).isChecked() )
            {
                try
                {
                    AdoInptSetUseWebRtcAecm( 0,
                                             ( ( ( CheckBox ) m_MainActPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).isChecked() ) ? 1 : 0,
                                             Integer.parseInt( ( ( TextView ) m_MainActPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).getText().toString() ),
                                             Integer.parseInt( ( ( TextView ) m_MainActPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).getText().toString() ) );
                }
                catch( NumberFormatException e )
                {
                    Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
                }
            }

            //设置音频输入是否使用WebRtc浮点版声学回音消除器。
            if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecRdBtnId ) ).isChecked() )
            {
                try
                {
                    AdoInptSetUseWebRtcAec( 0,
                                            Integer.parseInt( ( ( TextView ) m_MainActPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).getText().toString() ),
                                            Integer.parseInt( ( ( TextView ) m_MainActPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).getText().toString() ),
                                            ( ( ( CheckBox ) m_MainActPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).isChecked() ) ? 1 : 0,
                                            ( ( ( CheckBox ) m_MainActPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).isChecked() ) ? 1 : 0,
                                            ( ( ( CheckBox ) m_MainActPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).isChecked() ) ? 1 : 0,
                                            ( ( ( CheckBox ) m_MainActPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).isChecked() ) ? 1 : 0,
                                            ( ( ( CheckBox ) m_MainActPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).isChecked() ) ? 1 : 0,
                                            m_MainActPt.m_ExternalDirFullAbsPathStrPt + "/WebRtcAecMem" );
                }
                catch( NumberFormatException e )
                {
                    Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
                }
            }

            //设置音频输入是否使用SpeexWebRtc三重声学回音消除器。
            if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexWebRtcAecRdBtnId ) ).isChecked() )
            {
                try
                {
                    AdoInptSetUseSpeexWebRtcAec( 0,
                                                 ( ( RadioButton ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmRdBtnId ) ).isChecked() ? 1 :
                                                    ( ( RadioButton ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeWebRtcAecmWebRtcAecRdBtnId ) ).isChecked() ? 2 :
                                                        ( ( RadioButton ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRdBtnId ) ).isChecked() ? 3 : 0,
                                                 Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenMsecEdTxtId ) ).getText().toString() ),
                                                 ( ( ( CheckBox ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).isChecked() ) ? 1 : 0,
                                                 Float.parseFloat( ( ( TextView ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).getText().toString() ),
                                                 Float.parseFloat( ( ( TextView ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).getText().toString() ),
                                                 Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).getText().toString() ),
                                                 Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).getText().toString() ),
                                                 ( ( ( CheckBox ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).isChecked() ) ? 1 : 0,
                                                 Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).getText().toString() ),
                                                 Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).getText().toString() ),
                                                 Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).getText().toString() ),
                                                 Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).getText().toString() ),
                                                 ( ( ( CheckBox ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).isChecked() ) ? 1 : 0,
                                                 ( ( ( CheckBox ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).isChecked() ) ? 1 : 0,
                                                 ( ( ( CheckBox ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).isChecked() ) ? 1 : 0,
                                                 ( ( ( CheckBox ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).isChecked() ) ? 1 : 0,
                                                 ( ( ( CheckBox ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).isChecked() ) ? 1 : 0,
                                                 Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).getText().toString() ) );
                }
                catch( NumberFormatException e )
                {
                    Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
                }
            }
        }

        //设置音频输入是否不使用噪音抑制器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseNoNsRdBtnId ) ).isChecked() )
        {
            AdoInptSetUseNoNs( 0 );
        }

        //设置音频输入是否使用Speex预处理器的噪音抑制。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexPrpocsNsRdBtnId ) ).isChecked() )
        {
            try
            {
                AdoInptSetUseSpeexPrpocsNs( 0,
                                            ( ( ( CheckBox ) m_MainActPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).isChecked() ) ? 1 : 0,
                                            Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).getText().toString() ),
                                            ( ( ( CheckBox ) m_MainActPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).isChecked() ) ? 1 : 0 );
            }
            catch( NumberFormatException e )
            {
                Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
            }
        }

        //设置音频输入是否使用WebRtc定点版噪音抑制器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsxRdBtnId ) ).isChecked() )
        {
            try
            {
                AdoInptSetUseWebRtcNsx( 0,
                                        Integer.parseInt( ( ( TextView ) m_MainActPt.m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).getText().toString() ) );
            }
            catch( NumberFormatException e )
            {
                Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
            }
        }

        //设置音频输入是否使用WebRtc浮点版噪音抑制器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsRdBtnId ) ).isChecked() )
        {
            try
            {
                AdoInptSetUseWebRtcNs( 0,
                                       Integer.parseInt( ( ( TextView ) m_MainActPt.m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).getText().toString() ) );
            }
            catch( NumberFormatException e )
            {
                Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
            }
        }

        //设置音频输入是否使用RNNoise噪音抑制器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseRNNoiseRdBtnId ) ).isChecked() )
        {
            try
            {
                AdoInptSetUseRNNoise( 0 );
            }
            catch( NumberFormatException e )
            {
                Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
            }
        }

        //设置音频输入是否使用Speex预处理器。
        try
        {
            AdoInptSetIsUseSpeexPrpocs( 0,
                                        ( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsCkBoxId ) ).isChecked() ) ? 1 : 0,
                                        ( ( ( CheckBox ) m_MainActPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).isChecked() ) ? 1 : 0,
                                        Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).getText().toString() ),
                                        Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).getText().toString() ),
                                        ( ( ( CheckBox ) m_MainActPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).isChecked() ) ? 1 : 0,
                                        Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).getText().toString() ),
                                        Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).getText().toString() ),
                                        Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).getText().toString() ),
                                        Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).getText().toString() ) );
        }
        catch( NumberFormatException e )
        {
            Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
        }

        //设置音频输入是否使用Pcm原始数据。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UsePcmRdBtnId ) ).isChecked() )
        {
            AdoInptSetUsePcm( 0 );
        }

        //设置音频输入是否使用Speex编码器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).isChecked() )
        {
            try
            {
                AdoInptSetUseSpeexEncd( 0,
                                        ( ( ( RadioButton ) m_MainActPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseCbrRdBtnId ) ).isChecked() ) ? 0 : 1,
                                        Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).getText().toString() ),
                                        Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).getText().toString() ),
                                        Integer.parseInt( ( ( TextView ) m_MainActPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).getText().toString() ) );
            }
            catch( NumberFormatException e )
            {
                Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
            }
        }

        //设置音频输入是否使用Opus编码器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseOpusCodecRdBtnId ) ).isChecked() )
        {
            AdoInptSetUseOpusEncd( 0 );
        }

        //设置音频输入是否保存音频到Wave文件。
        if( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).isChecked() &&
            ( ( CheckBox ) m_MainActPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileIsSaveAdoInptCkBoxId ) ).isChecked() )
        {
            String p_AdoInptSrcFullPathStrPt;
            String p_AdoInptRsltFullPathStrPt;

            p_AdoInptSrcFullPathStrPt = ( ( EditText ) m_MainActPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoInptSrcFullPathEdTxtId ) ).getText().toString();
            if( p_AdoInptSrcFullPathStrPt.charAt( 0 ) != '/' ) p_AdoInptSrcFullPathStrPt = m_MainActPt.m_ExternalDirFullAbsPathStrPt + "/" + p_AdoInptSrcFullPathStrPt;
            p_AdoInptRsltFullPathStrPt = ( ( EditText ) m_MainActPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoInptRsltFullPathEdTxtId ) ).getText().toString();
            if( p_AdoInptRsltFullPathStrPt.charAt( 0 ) != '/' ) p_AdoInptRsltFullPathStrPt = m_MainActPt.m_ExternalDirFullAbsPathStrPt + "/" + p_AdoInptRsltFullPathStrPt;

            try
            {
                AdoInptSetIsSaveAdoToWaveFile( 0,
                                               ( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).isChecked() ) ? 1 : 0,
                                               p_AdoInptSrcFullPathStrPt,
                                               p_AdoInptRsltFullPathStrPt,
                                               Integer.parseInt( ( ( TextView ) m_MainActPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileWrBufSzBytEdTxtId ) ).getText().toString() ) );
            }
            catch( NumberFormatException e )
            {
                Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
            }
        }

        //设置音频输入是否绘制音频波形到Surface。
        AdoInptSetIsDrawAdoWavfmToSurface( 0,
                                           ( ( ( CheckBox ) m_MainActPt.m_MainLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).isChecked() ) ? 1 : 0,
                                           ( ( SurfaceView ) m_MainActPt.findViewById( R.id.AdoInptWavfmSurfaceId ) ),
                                           ( ( SurfaceView ) m_MainActPt.findViewById( R.id.AdoRsltWavfmSurfaceId ) ) );

        //设置音频输入是否静音。
        AdoInptSetIsMute( 0,
                          ( ( ( CheckBox ) m_MainActPt.m_MainLyotViewPt.findViewById( R.id.AdoInptIsMuteCkBoxId ) ).isChecked() ) ? 1 : 0 );
    }

    //设置不使用音频输入。
    void SetNotUseAdoInpt()
    {

    }

    //设置要使用音频输出。
    void SetToUseAdoOtpt()
    {
        //设置音频输出。
        SetAdoOtpt( 0,
                    ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate8000RdBtnId ) ).isChecked() ) ? 8000 :
                        ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).isChecked() ) ? 16000 :
                            ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate32000RdBtnId ) ).isChecked() ) ? 32000 :
                                ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate48000RdBtnId ) ).isChecked() ) ? 48000 : 0,
                    ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen10msRdBtnId ) ).isChecked() ) ? 10 :
                        ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).isChecked() ) ? 20 :
                            ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen30msRdBtnId ) ).isChecked() ) ? 30 : 0 );

        //设置音频输出是否保存音频到Wave文件。
        if( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).isChecked() &&
            ( ( CheckBox ) m_MainActPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileIsSaveAdoOtptCkBoxId ) ).isChecked() )
        {
            String p_AdoOtptSrcFullPathStrPt;

            p_AdoOtptSrcFullPathStrPt = ( ( EditText ) m_MainActPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoOtptSrcFullPathEdTxtId ) ).getText().toString();
            if( p_AdoOtptSrcFullPathStrPt.charAt( 0 ) != '/' ) p_AdoOtptSrcFullPathStrPt = m_MainActPt.m_ExternalDirFullAbsPathStrPt + "/" + p_AdoOtptSrcFullPathStrPt;

            try
            {
                AdoOtptSetIsSaveAdoToWaveFile( 0,
                                               ( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).isChecked() ) ? 1 : 0,
                                               p_AdoOtptSrcFullPathStrPt,
                                               Integer.parseInt( ( ( TextView ) m_MainActPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileWrBufSzBytEdTxtId ) ).getText().toString() ) );
            }
            catch( NumberFormatException e )
            {
                Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
            }
        }

        //设置音频输出是否绘制音频波形到Surface。
        AdoOtptSetIsDrawAdoWavfmToSurface( 0,
                                           ( ( ( CheckBox ) m_MainActPt.m_MainLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).isChecked() ) ? 1 : 0,
                                           ( ( SurfaceView ) m_MainActPt.findViewById( R.id.AdoOtptWavfmSurfaceId ) ) );

        //设置音频输出使用的设备。
        AdoOtptSetUseDvc( 0,
                          ( ( ( RadioButton ) m_MainActPt.m_MainLyotViewPt.findViewById( R.id.UseSpeakerRdBtnId ) ).isChecked() ) ? 0 : 1,
                          0 );

        //设置音频输出是否静音。
        AdoOtptSetIsMute( 0,
                          ( ( ( CheckBox ) m_MainActPt.m_MainLyotViewPt.findViewById( R.id.AdoOtptIsMuteCkBoxId ) ).isChecked() ) ? 1 : 0 );

        for( TkbkClnt.TkbkInfo p_TkbkInfoTmpPt : m_TkbkClntPt.m_TkbkInfoCntnrPt )
        {
            if( ( p_TkbkInfoTmpPt.m_IsInit != 0 ) &&
                ( ( p_TkbkInfoTmpPt.m_RmtTkbkMode & TkbkMode.AdoInpt ) != 0 ) )
            {
                SetToUseAdoOtptStrm( p_TkbkInfoTmpPt.m_TkbkIdx );
            }
        }
    }

    //设置不使用音频输出。
    void SetNotUseAdoOtpt()
    {
        for( TkbkClnt.TkbkInfo p_TkbkInfoTmpPt : m_TkbkClntPt.m_TkbkInfoCntnrPt )
        {
            if( ( p_TkbkInfoTmpPt.m_IsInit != 0 ) &&
                ( ( p_TkbkInfoTmpPt.m_RmtTkbkMode & TkbkMode.AdoInpt ) != 0 ) )
            {
                SetNotUseAdoOtptStrm( p_TkbkInfoTmpPt.m_TkbkIdx );
            }
        }
    }

    //设置要使用音频输出流。
    void SetToUseAdoOtptStrm( int TkbkIdx )
    {
        //音频输出添加流。
        AdoOtptAddStrm( 0, TkbkIdx );

        //音频输出设置流要使用Pcm原始数据。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UsePcmRdBtnId ) ).isChecked() )
        {
            AdoOtptSetStrmUsePcm( 0, TkbkIdx );
        }

        //音频输出设置流要使用Speex解码器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).isChecked() )
        {
            try
            {
                AdoOtptSetStrmUseSpeexDecd( 0,
                                            TkbkIdx,
                                            ( ( ( CheckBox ) m_MainActPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).isChecked() ) ? 1 : 0 );
            }
            catch( NumberFormatException e )
            {
                Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
            }
        }

        //音频输出设置流要使用Opus解码器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseOpusCodecRdBtnId ) ).isChecked() )
        {
            AdoOtptSetStrmUseOpusDecd( 0, TkbkIdx );
        }

        //音频输出设置流是否要使用。
        AdoOtptSetStrmIsUse( 0, TkbkIdx, 1 );
    }

    //设置不使用音频输出流。
    void SetNotUseAdoOtptStrm( int TkbkIdx )
    {
        AdoOtptDelStrm( 1, 0, TkbkIdx ); //删除流操作需要立即执行，因为要防止中途出现其他消息导致重复删除流。
    }

    //设置要使用视频输入。
    void SetToUseVdoInpt()
    {
        //设置视频输入。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSzPrsetRdBtnId ) ).isChecked() ) //如果要使用预设的帧的大小。
        {
            SetVdoInpt( 0,
                        ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate12RdBtnId ) ).isChecked() ) ? 12 :
                            ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate15RdBtnId ) ).isChecked() ) ? 15 :
                                ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate24RdBtnId ) ).isChecked() ) ? 24 :
                                    ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate30RdBtnId ) ).isChecked() ) ? 30 : 0,
                        ( ( ( Spinner ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).getSelectedItemPosition() == 0 ) ? 120 :
                            ( ( ( Spinner ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).getSelectedItemPosition() == 1 ) ? 240 :
                                ( ( ( Spinner ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).getSelectedItemPosition() == 2 ) ? 480 :
                                    ( ( ( Spinner ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).getSelectedItemPosition() == 3 ) ? 960 : 0,
                        ( ( ( Spinner ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).getSelectedItemPosition() == 0 ) ? 160 :
                            ( ( ( Spinner ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).getSelectedItemPosition() == 1 ) ? 320 :
                                ( ( ( Spinner ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).getSelectedItemPosition() == 2 ) ? 640 :
                                    ( ( ( Spinner ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).getSelectedItemPosition() == 3 ) ? 1280 : 0,
                        0,
                        0,
                        m_MainActPt.getWindowManager().getDefaultDisplay().getRotation() * 90,
                        m_MainActPt.SendVdoInptOtptViewInitMsg( "视频输入预览" + m_TkbkClntPt.m_MyTkbkIdx ) );
        }
        else //如果要使用其他的帧的大小。
        {
            try
            {
                SetVdoInpt( 0,
                            ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate12RdBtnId ) ).isChecked() ) ? 12 :
                                ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate15RdBtnId ) ).isChecked() ) ? 15 :
                                    ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate24RdBtnId ) ).isChecked() ) ? 24 :
                            ( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate30RdBtnId ) ).isChecked() ) ? 30 : 0,
                            Integer.parseInt( ( ( TextView ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzOtherWidthEdTxtId ) ).getText().toString() ),
                            Integer.parseInt( ( ( TextView ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzOtherHeightEdTxtId ) ).getText().toString() ),
                            0,
                            0,
                            m_MainActPt.getWindowManager().getDefaultDisplay().getRotation() * 90,
                            m_MainActPt.SendVdoInptOtptViewInitMsg( "视频输入预览" + m_TkbkClntPt.m_MyTkbkIdx ) );
            }
            catch( NumberFormatException e )
            {
                Toast.makeText( m_MainActPt, "请输入数字", Toast.LENGTH_LONG ).show();
            }
        }

        //设置视频输入是否使用Yu12原始数据。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseYu12RdBtnId ) ).isChecked() )
        {
            VdoInptSetUseYu12( 0 );
        }

        //设置视频输入是否使用OpenH264编码器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).isChecked() )
        {
            VdoInptSetUseOpenH264Encd( 0,
                                       Integer.parseInt( ( ( TextView ) m_MainActPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).getText().toString() ),
                                       Integer.parseInt( ( ( TextView ) m_MainActPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).getText().toString() ) * 1024 * 8,
                                       Integer.parseInt( ( ( TextView ) m_MainActPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).getText().toString() ),
                                       Integer.parseInt( ( ( TextView ) m_MainActPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).getText().toString() ),
                                       Integer.parseInt( ( ( TextView ) m_MainActPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).getText().toString() ) );
        }

        //设置视频输入是否使用系统自带H264编码器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseSystemH264CodecRdBtnId ) ).isChecked() )
        {
            VdoInptSetUseSystemH264Encd( 0,
                                         Integer.parseInt( ( ( TextView ) m_MainActPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).getText().toString() ) * 1024 * 8,
                                         Integer.parseInt( ( ( TextView ) m_MainActPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).getText().toString() ),
                                         Integer.parseInt( ( ( TextView ) m_MainActPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).getText().toString() ),
                                         Integer.parseInt( ( ( TextView ) m_MainActPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).getText().toString() ) );
        }

        //设置视频输入使用的设备。
        VdoInptSetUseDvc( 0,
                          ( ( ( RadioButton ) m_MainActPt.m_MainLyotViewPt.findViewById( R.id.UseFrontCamereRdBtnId ) ).isChecked() ) ? 0 : 1,
                          -1,
                          -1 );

        //设置视频输入是否黑屏。
        VdoInptSetIsBlack( 0,
                           ( ( ( CheckBox ) m_MainActPt.m_MainLyotViewPt.findViewById( R.id.VdoInptIsBlackCkBoxId ) ).isChecked() ) ? 1 : 0 );
    }

    //设置不使用视频输入。
    void SetNotUseVdoInpt()
    {
        m_MainActPt.SendVdoInptOtptViewDstoyMsg( m_VdoInptPt.m_DvcPt.m_PrvwSurfaceViewPt );
    }

    //设置要使用视频输出。
    void SetToUseVdoOtpt()
    {
        for( TkbkClnt.TkbkInfo p_TkbkInfoTmpPt : m_TkbkClntPt.m_TkbkInfoCntnrPt )
        {
            if( ( p_TkbkInfoTmpPt.m_IsInit != 0 ) &&
                ( ( p_TkbkInfoTmpPt.m_RmtTkbkMode & TkbkMode.VdoInpt ) != 0 ) )
            {
                SetToUseVdoOtptStrm( p_TkbkInfoTmpPt.m_TkbkIdx );
            }
        }
    }

    //设置不使用视频输出。
    void SetNotUseVdoOtpt()
    {
        for( TkbkClnt.TkbkInfo p_TkbkInfoTmpPt : m_TkbkClntPt.m_TkbkInfoCntnrPt )
        {
            if( ( p_TkbkInfoTmpPt.m_IsInit != 0 ) &&
                ( ( p_TkbkInfoTmpPt.m_RmtTkbkMode & TkbkMode.VdoInpt ) != 0 ) )
            {
                SetNotUseVdoOtptStrm( p_TkbkInfoTmpPt.m_TkbkIdx );
            }
        }
    }

    //设置要使用视频输出流。
    void SetToUseVdoOtptStrm( int TkbkIdx )
    {
        //视频输出添加流。
        VdoOtptAddStrm( 0, TkbkIdx );

        //视频输出设置流。
        VdoOtptSetStrm( 0,
                        TkbkIdx,
                        m_MainActPt.SendVdoInptOtptViewInitMsg( "视频输出显示" + TkbkIdx ) );

        //视频输出设置流要使用Yu12原始数据。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseYu12RdBtnId ) ).isChecked() )
        {
            VdoOtptSetStrmUseYu12( 0, TkbkIdx );
        }

        //视频输出设置流要使用OpenH264解码器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).isChecked() )
        {
            VdoOtptSetStrmUseOpenH264Decd( 0, TkbkIdx, 0 );
        }

        //视频输出设置流要使用系统自带H264解码器。
        if( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseSystemH264CodecRdBtnId ) ).isChecked() )
        {
            VdoOtptSetStrmUseSystemH264Decd( 0, TkbkIdx );
        }

        //视频输出设置流是否黑屏。
        VdoOtptSetStrmIsBlack( 0,
                               TkbkIdx,
                               ( ( ( CheckBox ) m_MainActPt.m_MainLyotViewPt.findViewById( R.id.VdoOtptIsBlackCkBoxId ) ).isChecked() ) ? 1 : 0 );

        //视频输出设置流是否使用。
        VdoOtptSetStrmIsUse( 0, TkbkIdx, 1 );
    }

    //设置不使用视频输出流。
    void SetNotUseVdoOtptStrm( int TkbkIdx )
    {
        for( VdoOtpt.Strm p_StrmPt : m_VdoOtptPt.m_StrmCntnrPt )
        {
            if( p_StrmPt.m_Idx == TkbkIdx )
            {
                m_MainActPt.SendVdoInptOtptViewDstoyMsg( p_StrmPt.m_DvcPt.m_DspySurfaceViewPt );
                break;
            }
        }
        VdoOtptDelStrm( 1, 0, TkbkIdx ); //删除流操作需要立即执行，因为要防止中途出现其他消息导致重复删除流。
    }
}
