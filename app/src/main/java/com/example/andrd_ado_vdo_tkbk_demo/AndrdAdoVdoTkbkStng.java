package com.example.andrd_ado_vdo_tkbk_demo;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import HeavenTao.Data.*;
import HeavenTao.Media.*;
import HeavenTao.TinyXml2.*;

//主界面。
public class AndrdAdoVdoTkbkStng
{
    //保存设置到Xml文件。
    public static void SaveStngToXmlFile( MainActivity MainActivityPt )
    {
        XMLDocument p_XMLDocumentPt = new XMLDocument();
        XMLElement p_StngXMLElementPt = new XMLElement();
        XMLElement p_TmpXMLElement1Pt = new XMLElement();
        XMLElement p_TmpXMLElement2Pt = new XMLElement();

        Out:
        {
            p_XMLDocumentPt.Init();

            p_XMLDocumentPt.NewElement( p_StngXMLElementPt, "Stng" );
            p_XMLDocumentPt.InsertEndChild( p_StngXMLElementPt );

            //主布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "Main" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "UseWhatXfrPrtcl" );
                p_TmpXMLElement2Pt.SetText( ( ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseTcpPrtclRdBtnId ) ).isChecked() ) ? "Tcp" : "Udp" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IPAddr" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.IPAddrEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "Port" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.PortEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "TkbkMode" );
                p_TmpXMLElement2Pt.SetText(
                        ( ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseAdoTkbkModeRdBtnId ) ).isChecked() ) ? "Ado" :
                            ( ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseVdoTkbkModeRdBtnId ) ).isChecked() ) ? "Vdo" : "AdoVdo" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AdoOtptDvc" );
                p_TmpXMLElement2Pt.SetText( ( ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseSpeakerRdBtnId ) ).isChecked() ) ? "Speaker" : "Headset" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VdoInptDvc" );
                p_TmpXMLElement2Pt.SetText( ( ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseFrontCamereRdBtnId ) ).isChecked() ) ? "FrontCamere" : "BackCamere" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AdoInptIsMute" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.AdoInptIsMuteCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AdoOtptIsMute" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.AdoOtptIsMuteCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VdoInptIsBlack" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.VdoInptIsBlackCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VdoOtptIsBlack" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.VdoOtptIsBlackCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsDrawAdoWavfm" ); //这里不加"ToSurface"是为了与Windows端的"ToWnd"保持一致。
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //传输协议设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "XfrPrtclStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "XfrMode" );
                p_TmpXMLElement2Pt.SetText( ( ( ( RadioButton ) MainActivityPt.m_XfrPrtclStngLyotViewPt.findViewById( R.id.UsePttRdBtnId ) ).isChecked() ) ? "Ptt" : "Rt" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "MaxCnctTimes" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_XfrPrtclStngLyotViewPt.findViewById( R.id.MaxCnctTimesEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsAutoAllowCnct" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_XfrPrtclStngLyotViewPt.findViewById( R.id.IsAutoAllowCnctCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "Stng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "UseWhatRecvOtptFrm" );
                p_TmpXMLElement2Pt.SetText( ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseCntnrRecvOtptFrmRdBtnId ) ).isChecked() ) ? "Cntnr" : "Ajb" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseFrgndSrvc" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseFrgndSrvcCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsSaveStsToTxtFile" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveStsToTxtFileCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsPrintLogShowToast" ); //这里不加"Logcat"是为了与Windows端的"Log"保持一致。
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsPrintLogcatShowToastCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseWakeLock" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseWakeLockCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsSaveAdoVdoInptOtptToAviFile" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoVdoInptOtptToAviFileCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "Effect" );
                p_TmpXMLElement2Pt.SetText(
                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectLowRdBtnId ) ).isChecked() ) ? "Low" :
                                ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectMidRdBtnId ) ).isChecked() ) ? "Mid" :
                                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectHighRdBtnId ) ).isChecked() ) ? "High" :
                                                ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectSuperRdBtnId ) ).isChecked() ) ? "Super" : "Premium" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AdoSmplRate" );
                p_TmpXMLElement2Pt.SetText(
                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate8000RdBtnId ) ).isChecked() ) ? "8000" :
                                ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).isChecked() ) ? "16000" :
                                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate32000RdBtnId ) ).isChecked() ) ? "32000" : "48000" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AdoFrmLen" );
                p_TmpXMLElement2Pt.SetText(
                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen10msRdBtnId ) ).isChecked() ) ? "10" :
                                ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).isChecked() ) ? "20" : "30" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseSystemAecNsAgc" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "UseWhatAec" );
                p_TmpXMLElement2Pt.SetText(
                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseNoAecRdBtnId ) ).isChecked() ) ? "NoAec" :
                                ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexAecRdBtnId ) ).isChecked() ) ? "SpeexAec" :
                                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecmRdBtnId ) ).isChecked() ) ? "WebRtcAecm" :
                                                ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecRdBtnId ) ).isChecked() ) ? "WebRtcAec" : "SpeexWebRtcAec" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "UseWhatNs" );
                p_TmpXMLElement2Pt.SetText(
                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseNoNsRdBtnId ) ).isChecked() ) ? "NoNs" :
                                ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexPrpocsNsRdBtnId ) ).isChecked() ) ? "SpeexPrpocsNs" :
                                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsxRdBtnId ) ).isChecked() ) ? "WebRtcNsx" :
                                                ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsRdBtnId ) ).isChecked() ) ? "WebRtcNs" : "RNNoise" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseSpeexPrpocs" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AdoUseWhatCodec" );
                p_TmpXMLElement2Pt.SetText(
                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UsePcmRdBtnId ) ).isChecked() ) ? "Pcm" :
                                ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).isChecked() ) ? "SpeexCodec" : "OpusCodec" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsSaveAdoInptOtptToWaveFile" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VdoSmplRate" );
                p_TmpXMLElement2Pt.SetText(
                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate12RdBtnId ) ).isChecked() ) ? "12" :
                                ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate15RdBtnId ) ).isChecked() ) ? "15" :
                                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate24RdBtnId ) ).isChecked() ) ? "24" : "30" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "UseWhatVdoFrmSz" );
                p_TmpXMLElement2Pt.SetText(
                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSzPrsetRdBtnId ) ).isChecked() ) ? "Prset" : "Other" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VdoFrmSzPrset" );
                p_TmpXMLElement2Pt.SetText( ( ( Spinner ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).getSelectedItem().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VdoFrmSzOtherWidth" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzOtherWidthEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VdoFrmSzOtherHeight" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzOtherHeightEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VdoUseWhatCodec" );
                p_TmpXMLElement2Pt.SetText(
                        ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseYu12RdBtnId ) ).isChecked() ) ? "Yu12" :
                                ( ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).isChecked() ) ? "OpenH264Codec" : "SystemH264Codec" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //自适应抖动缓冲器设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "AjbStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AAjbMinNeedBufFrmCnt" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbMinNeedBufFrmCntEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AAjbMaxNeedBufFrmCnt" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbMaxNeedBufFrmCntEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AAjbMaxCntuLostFrmCnt" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbMaxCntuLostFrmCntEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AAjbAdaptSensitivity" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbAdaptSensitivityEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VAjbMinNeedBufFrmCnt" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.VAjbMinNeedBufFrmCntEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VAjbMaxNeedBufFrmCnt" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.VAjbMaxNeedBufFrmCntEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VAjbAdaptSensitivity" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.VAjbAdaptSensitivityEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //保存状态到Txt文件设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "SaveStsToTxtFileStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "FullPath" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SaveStsToTxtFileStngLyotViewPt.findViewById( R.id.SaveStsToTxtFileFullPathEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //保存音视频输入输出到Avi文件设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "SaveAdoVdoInptOtptToAviFileStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "FullPath" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileFullPathEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WrBufSzByt" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileWrBufSzBytEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsSaveAdoInpt" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveAdoInptCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsSaveAdoOtpt" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveAdoOtptCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsSaveVdoInpt" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveVdoInptCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsSaveVdoOtpt" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveVdoOtptCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //Speex声学回音消除器设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "SpeexAecStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "FilterLenMsec" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenMsecEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseRec" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "EchoMutp" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "EchoCntu" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "EchoSupes" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "EchoSupesAct" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsSaveMemFile" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //WebRtc定点版声学回音消除器设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "WebRtcAecmStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseCNGMode" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "EchoMode" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "Delay" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //WebRtc浮点版声学回音消除器设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "WebRtcAecStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "EchoMode" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "Delay" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseDelayAgstcMode" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseExtdFilterMode" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseRefinedFilterAdaptAecMode" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseAdaptAdjDelay" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsSaveMemFile" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //SpeexWebRtc三重声学回音消除器设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "SpeexWebRtcAecStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WorkMode" );
                p_TmpXMLElement2Pt.SetText(
                        ( ( ( RadioButton ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmRdBtnId ) ).isChecked() ) ? "SpeexAecWebRtcAecm" :
                                ( ( ( RadioButton ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeWebRtcAecmWebRtcAecRdBtnId ) ).isChecked() ) ? "WebRtcAecmWebRtcAec" : "SpeexAecWebRtcAecmWebRtcAec" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SpeexAecFilterLenMsec" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenMsecEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SpeexAecIsUseRec" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SpeexAecEchoMutp" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SpeexAecEchoCntu" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SpeexAecEchoSupes" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SpeexAecEchoSupesAct" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WebRtcAecmIsUseCNGMode" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WebRtcAecmEchoMode" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WebRtcAecmDelay" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WebRtcAecEchoMode" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WebRtcAecDelay" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WebRtcAecIsUseDelayAgstcMode" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WebRtcAecIsUseExtdFilterMode" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WebRtcAecIsUseRefinedFilterAdaptAecMode" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WebRtcAecIsUseAdaptAdjDelay" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseSameRoomAec" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SameRoomEchoMinDelay" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //Speex预处理器的噪音抑制设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "SpeexPrpocsNsStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseNs" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "NoiseSupes" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseDereverb" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //WebRtc定点版噪音抑制器设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "WebRtcNsxStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "PolicyMode" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //WebRtc浮点版噪音抑制器设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "WebRtcNsStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "PolicyMode" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //Speex预处理器设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "SpeexPrpocsStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseVad" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VadProbStart" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "VadProbCntu" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsUseAgc" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AgcLevel" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AgcIncrement" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AgcDecrement" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AgcMaxGain" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //Speex编解码器设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "SpeexCodecStng" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SpeexEncdUseCbrOrVbr" );
                p_TmpXMLElement2Pt.SetText( ( ( ( RadioButton ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseCbrRdBtnId ) ).isChecked() ) ? "Cbr" : "Vbr" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SpeexEncdQualt" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SpeexEncdCmplxt" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SpeexEncdPlcExptLossRate" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SpeexDecdIsUsePrcplEnhsmt" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //保存音频到Wave文件设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "SaveAdoInptOtptToWaveFile" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsSaveAdoInpt" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileIsSaveAdoInptCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AdoInptSrcFullPath" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoInptSrcFullPathEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AdoInptRsltFullPath" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoInptRsltFullPathEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "IsSaveAdoOtpt" );
                p_TmpXMLElement2Pt.SetText( ( ( ( CheckBox ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileIsSaveAdoOtptCkBoxId ) ).isChecked() ) ? "1" : "0" );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "AdoOtptSrcFullPath" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoOtptSrcFullPathEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "WrBufSzByt" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileWrBufSzBytEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //OpenH264编解码器设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "OpenH264Codec" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "OpenH264EncdVdoType" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "OpenH264EncdEncdBitrate" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "OpenH264EncdBitrateCtrlMode" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "OpenH264EncdIDRFrmIntvl" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "OpenH264EncdCmplxt" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            //系统自带H264编解码器设置布局。
            {
                p_XMLDocumentPt.NewElement( p_TmpXMLElement1Pt, "SystemH264Codec" );
                p_StngXMLElementPt.InsertEndChild( p_TmpXMLElement1Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SystemH264EncdEncdBitrate" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SystemH264EncdBitrateCtrlMode" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SystemH264EncdIDRFrmIntvl" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );

                p_XMLDocumentPt.NewElement( p_TmpXMLElement2Pt, "SystemH264EncdCmplxt" );
                p_TmpXMLElement2Pt.SetText( ( ( EditText ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).getText().toString() );
                p_TmpXMLElement1Pt.InsertEndChild( p_TmpXMLElement2Pt );
            }

            if( p_XMLDocumentPt.SaveFile( MainActivityPt.m_ExternalDirFullAbsPathStrPt + "/Stng.xml" ) == 0 )
            {
                String p_InfoStrPt = "保存设置到Stng.xml文件成功。";
                Log.i( MainActivityPt.m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;MainActivityPt.m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                Toast.makeText( MainActivityPt, p_InfoStrPt, Toast.LENGTH_SHORT ).show();
            }
            else
            {
                String p_InfoStrPt = "保存设置到Stng.xml文件失败。";
                Log.e( MainActivityPt.m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;MainActivityPt.m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                Toast.makeText( MainActivityPt, p_InfoStrPt, Toast.LENGTH_SHORT ).show();
            }
        }

        p_XMLDocumentPt.Dstoy();
    }

    //从Xml文件读取设置。
    public static void ReadStngFromXmlFile( MainActivity MainActivityPt )
    {
        XMLDocument p_XMLDocumentPt = new XMLDocument();
        XMLElement p_StngXMLElementPt = new XMLElement();
        XMLElement p_TmpXMLElement1Pt = new XMLElement();
        XMLElement p_TmpXMLElement2Pt = new XMLElement();
        HTString p_HTStringPt = new HTString();

        Out:
        {
            p_XMLDocumentPt.Init();

            if( p_XMLDocumentPt.LoadFile( MainActivityPt.m_ExternalDirFullAbsPathStrPt + "/Stng.xml" ) == 0 )
            {
                String p_InfoStrPt = "从Stng.xml文件读取设置成功。";
                Log.i( MainActivityPt.m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;MainActivityPt.m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                Toast.makeText( MainActivityPt, p_InfoStrPt, Toast.LENGTH_SHORT ).show();
            }
            else
            {
                String p_InfoStrPt = "从Stng.xml文件读取设置失败。";
                Log.e( MainActivityPt.m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;MainActivityPt.m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                Toast.makeText( MainActivityPt, p_InfoStrPt, Toast.LENGTH_SHORT ).show();
                break Out;
            }

            for( p_XMLDocumentPt.FirstChildElement( p_StngXMLElementPt ); p_StngXMLElementPt.m_XMLElementPt != 0; p_StngXMLElementPt.NextSiblingElement( p_StngXMLElementPt ) )
            {
                p_StngXMLElementPt.Name( p_HTStringPt );
                if( p_HTStringPt.m_Val.equals( "Stng" ) )
                {
                    for( p_StngXMLElementPt.FirstChildElement( p_TmpXMLElement1Pt ); p_TmpXMLElement1Pt.m_XMLElementPt != 0; p_TmpXMLElement1Pt.NextSiblingElement( p_TmpXMLElement1Pt ) )
                    {
                        p_TmpXMLElement1Pt.Name( p_HTStringPt );
                        if( p_HTStringPt.m_Val.equals( "Main" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "UseWhatXfrPrtcl" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "Tcp" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseTcpPrtclRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseUdpPrtclRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IPAddr" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.IPAddrEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                    MainActivityPt.m_FirstIPaddrItemIsSelected = 1; //设置第一次IP地址已选择。
                                }
                                else if( p_HTStringPt.m_Val.equals( "Port" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.PortEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "TkbkMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "Ado" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseAdoTkbkModeRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "Vdo" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseVdoTkbkModeRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseAdoVdoTkbkModeRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "AdoOtptDvc" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "Speaker" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseSpeakerRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseHeadsetRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "VdoInptDvc" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "FrontCamere" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseFrontCamereRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseBackCamereRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "AdoInptIsMute" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.AdoInptIsMuteCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.AdoInptIsMuteCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "AdoOtptIsMute" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.AdoOtptIsMuteCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.AdoOtptIsMuteCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "VdoInptIsBlack" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.VdoInptIsBlackCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.VdoInptIsBlackCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "VdoOtptIsBlack" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.VdoOtptIsBlackCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.VdoOtptIsBlackCkBoxId ) ).setChecked( true );
                                    }
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "XfrPrtclStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "XfrMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "Ptt" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_XfrPrtclStngLyotViewPt.findViewById( R.id.UsePttRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_XfrPrtclStngLyotViewPt.findViewById( R.id.UseRtFdRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "MaxCnctTimes" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_XfrPrtclStngLyotViewPt.findViewById( R.id.MaxCnctTimesEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsAutoAllowCnct" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_XfrPrtclStngLyotViewPt.findViewById( R.id.IsAutoAllowCnctCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_XfrPrtclStngLyotViewPt.findViewById( R.id.IsAutoAllowCnctCkBoxId ) ).setChecked( true );
                                    }
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "Stng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "UseWhatRecvOtptFrm" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "Cntnr" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseCntnrRecvOtptFrmRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAjbRecvOtptFrmRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseFrgndSrvc" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseFrgndSrvcCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseFrgndSrvcCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsSaveStsToTxtFile" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveStsToTxtFileCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveStsToTxtFileCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsPrintLogShowToast" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsPrintLogcatShowToastCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsPrintLogcatShowToastCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseWakeLock" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseWakeLockCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseWakeLockCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsSaveAdoVdoInptOtptToAviFile" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoVdoInptOtptToAviFileCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoVdoInptOtptToAviFileCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "Effect" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "Low" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectLowRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "Mid" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectMidRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "High" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectHighRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "Super" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectSuperRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectPremiumRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "AdoSmplRate" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "8000" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate8000RdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "16000" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "32000" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate32000RdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate48000RdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "AdoFrmLen" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "10" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen10msRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "20" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen30msRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseSystemAecNsAgc" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "UseWhatAec" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "NoAec" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseNoAecRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "SpeexAec" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexAecRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "WebRtcAecm" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecmRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "WebRtcAec" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexWebRtcAecRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "UseWhatNs" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "NoNs" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseNoNsRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "SpeexPrpocsNs" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexPrpocsNsRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "WebRtcNsx" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsxRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "WebRtcNs" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseRNNoiseRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseSpeexPrpocs" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "AdoUseWhatCodec" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "Pcm" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UsePcmRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "SpeexCodec" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseOpusCodecRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsSaveAdoInptOtptToWaveFile" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "VdoSmplRate" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "12" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate12RdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "15" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate15RdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "24" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate24RdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate30RdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "UseWhatVdoFrmSz" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "Prset" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSzPrsetRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSzOtherRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "VdoFrmSzPrset" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "120×160" ) )
                                    {
                                        ( ( Spinner ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).setSelection( 0 );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "240×320" ) )
                                    {
                                        ( ( Spinner ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).setSelection( 1 );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "480×640" ) )
                                    {
                                        ( ( Spinner ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).setSelection( 2 );
                                    }
                                    else
                                    {
                                        ( ( Spinner ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).setSelection( 3 );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "VdoFrmSzOtherWidth" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzOtherWidthEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "VdoFrmSzOtherHeight" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzOtherHeightEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "VdoUseWhatCodec" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "Yu12" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseYu12RdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "OpenH264Codec" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSystemH264CodecRdBtnId ) ).setChecked( true );
                                    }
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "AjbStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "AAjbMinNeedBufFrmCnt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbMinNeedBufFrmCntEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "AAjbMaxNeedBufFrmCnt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbMaxNeedBufFrmCntEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "AAjbMaxCntuLostFrmCnt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbMaxCntuLostFrmCntEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "AAjbAdaptSensitivity" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbAdaptSensitivityEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "VAjbMinNeedBufFrmCnt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.VAjbMinNeedBufFrmCntEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "VAjbMaxNeedBufFrmCnt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.VAjbMaxNeedBufFrmCntEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "VAjbAdaptSensitivity" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.VAjbAdaptSensitivityEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "SaveStsToTxtFileStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "FullPath" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SaveStsToTxtFileStngLyotViewPt.findViewById( R.id.SaveStsToTxtFileFullPathEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "SaveAdoVdoInptOtptToAviFileStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "FullPath" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileFullPathEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "WrBufSzByt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileWrBufSzBytEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsSaveAdoInpt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveAdoInptCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveAdoInptCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsSaveAdoOtpt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveAdoOtptCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveAdoOtptCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsSaveVdoInpt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveVdoInptCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveVdoInptCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsSaveVdoOtpt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveVdoOtptCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveVdoOtptCkBoxId ) ).setChecked( true );
                                    }
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "SpeexAecStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "FilterLenMsec" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenMsecEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseRec" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "EchoMutp" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "EchoCntu" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "EchoSupes" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "EchoSupesAct" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsSaveMemFile" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( true );
                                    }
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "WebRtcAecmStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "IsUseCNGMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "EchoMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "Delay" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "WebRtcAecStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "EchoMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "Delay" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseDelayAgstcMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseExtdFilterMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseRefinedFilterAdaptAecMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseAdaptAdjDelay" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsSaveMemFile" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( true );
                                    }
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "SpeexWebRtcAecStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "WorkMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "SpeexAecWebRtcAecm" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmRdBtnId ) ).setChecked( true );
                                    }
                                    else if( p_HTStringPt.m_Val.equals( "WebRtcAecmWebRtcAec" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeWebRtcAecmWebRtcAecRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "SpeexAecFilterLenMsec" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenMsecEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "SpeexAecIsUseRec" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "SpeexAecEchoMutp" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "SpeexAecEchoCntu" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "SpeexAecEchoSupes" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "SpeexAecEchoSupesAct" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "WebRtcAecmIsUseCNGMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "WebRtcAecmEchoMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "WebRtcAecmDelay" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "WebRtcAecEchoMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "WebRtcAecDelay" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "WebRtcAecIsUseDelayAgstcMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "WebRtcAecIsUseExtdFilterMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "WebRtcAecIsUseRefinedFilterAdaptAecMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "WebRtcAecIsUseAdaptAdjDelay" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseSameRoomAec" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "SameRoomEchoMinDelay" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "SpeexPrpocsNsStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "IsUseNs" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "NoiseSupes" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseDereverb" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( true );
                                    }
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "WebRtcNsxStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "PolicyMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "WebRtcNsStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "PolicyMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "SpeexPrpocsStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "IsUseVad" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "VadProbStart" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "VadProbCntu" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsUseAgc" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "AgcLevel" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "AgcIncrement" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "AgcDecrement" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "AgcMaxGain" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "SpeexCodecStng" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "SpeexEncdUseCbrOrVbr" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "Cbr" ) )
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseCbrRdBtnId ) ).setChecked( true );
                                    }
                                    else
                                    {
                                        ( ( RadioButton ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseVbrRdBtnId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "SpeexEncdQualt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "SpeexEncdCmplxt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "SpeexEncdPlcExptLossRate" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "SpeexDecdIsUsePrcplEnhsmt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( true );
                                    }
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "SaveAdoInptOtptToWaveFile" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "IsSaveAdoInpt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileIsSaveAdoInptCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileIsSaveAdoInptCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "AdoInptSrcFullPath" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoInptSrcFullPathEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "AdoInptRsltFullPath" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoInptRsltFullPathEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "IsSaveAdoOtpt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    if( p_HTStringPt.m_Val.equals( "0" ) )
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileIsSaveAdoOtptCkBoxId ) ).setChecked( false );
                                    }
                                    else
                                    {
                                        ( ( CheckBox ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileIsSaveAdoOtptCkBoxId ) ).setChecked( true );
                                    }
                                }
                                else if( p_HTStringPt.m_Val.equals( "AdoOtptSrcFullPath" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoOtptSrcFullPathEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "WrBufSzByt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileWrBufSzBytEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "OpenH264Codec" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "OpenH264EncdVdoType" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "OpenH264EncdEncdBitrate" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "OpenH264EncdEncdBitrateCtrlMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "OpenH264EncdIDRFrmIntvl" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "OpenH264EncdCmplxt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                            }
                        }
                        else if( p_HTStringPt.m_Val.equals( "SystemH264Codec" ) )
                        {
                            for( p_TmpXMLElement1Pt.FirstChildElement( p_TmpXMLElement2Pt ); p_TmpXMLElement2Pt.m_XMLElementPt != 0; p_TmpXMLElement2Pt.NextSiblingElement( p_TmpXMLElement2Pt ) )
                            {
                                p_TmpXMLElement2Pt.Name( p_HTStringPt );
                                if( p_HTStringPt.m_Val.equals( "SystemH264EncdEncdBitrate" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "SystemH264EncdBitrateCtrlMode" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "SystemH264EncdIDRFrmIntvl" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                                else if( p_HTStringPt.m_Val.equals( "SystemH264EncdCmplxt" ) )
                                {
                                    p_TmpXMLElement2Pt.GetText( p_HTStringPt );
                                    ( ( EditText ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).setText( p_HTStringPt.m_Val );
                                }
                            }
                        }
                    }
                }
            }
        }

        p_XMLDocumentPt.Dstoy();
    }

    //删除设置Xml文件。
    public static void DelStngXmlFile( MainActivity MainActivityPt )
    {
        String p_StngXmlFileFullPathStrPt = MainActivityPt.m_ExternalDirFullAbsPathStrPt + "/Stng.xml";
        File file = new File( p_StngXmlFileFullPathStrPt );
        if( file.delete() )
        {
            String p_InfoStrPt = "删除设置文件Stng.xml成功。";
            Log.i( MainActivityPt.m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;MainActivityPt.m_MainActivityHandlerPt.sendMessage( p_MessagePt );
            Toast.makeText( MainActivityPt, p_InfoStrPt, Toast.LENGTH_SHORT ).show();
        }
        else
        {
            String p_InfoStrPt = "删除设置文件Stng.xml失败。";
            Log.e( MainActivityPt.m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;MainActivityPt.m_MainActivityHandlerPt.sendMessage( p_MessagePt );
            Toast.makeText( MainActivityPt, p_InfoStrPt, Toast.LENGTH_SHORT ).show();
        }
    }

    //重置设置。
    public static void ResetStng( MainActivity MainActivityPt )
    {
        //设置使用什么传输协议。
        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseUdpPrtclRdBtnId ) ).performClick();

        //设置传输协议。
        ( ( RadioButton ) MainActivityPt.m_XfrPrtclStngLyotViewPt.findViewById( R.id.UseRtFdRdBtnId ) ).performClick();
        ( ( EditText ) MainActivityPt.m_XfrPrtclStngLyotViewPt.findViewById( R.id.MaxCnctTimesEdTxtId ) ).setText( "5" );
        ( ( CheckBox ) MainActivityPt.m_XfrPrtclStngLyotViewPt.findViewById( R.id.IsAutoAllowCnctCkBoxId ) ).setChecked( true );

        //设置IP地址。
        try
        {
            ArrayList< String > p_IPAddrList = new ArrayList< String >();

            for( Enumeration<NetworkInterface> p_EnumNtwkIntfc = NetworkInterface.getNetworkInterfaces(); p_EnumNtwkIntfc.hasMoreElements(); ) //遍历所有的网络接口设备。
            {
                NetworkInterface clNetworkInterface = p_EnumNtwkIntfc.nextElement();
                for( Enumeration<InetAddress> p_EnumIpAddr = clNetworkInterface.getInetAddresses(); p_EnumIpAddr.hasMoreElements(); ) //遍历该网络接口设备所有的IP地址。
                {
                    InetAddress p_InetAddr = p_EnumIpAddr.nextElement();
                    if( ( !p_InetAddr.isLoopbackAddress() ) && ( p_InetAddr.getAddress().length == 4 ) ) //如果该IP地址不是回环地址，且是IPv4的。
                    {
                        p_IPAddrList.add( p_InetAddr.getHostAddress() );
                    }
                }
            }
            p_IPAddrList.add( "0.0.0.0" );
            p_IPAddrList.add( "127.0.0.1" );
            for( Enumeration<NetworkInterface> p_EnumNtwkIntfc = NetworkInterface.getNetworkInterfaces(); p_EnumNtwkIntfc.hasMoreElements(); ) //遍历所有的网络接口设备。
            {
                NetworkInterface clNetworkInterface = p_EnumNtwkIntfc.nextElement();
                for( Enumeration<InetAddress> p_EnumIpAddr = clNetworkInterface.getInetAddresses(); p_EnumIpAddr.hasMoreElements(); ) //遍历该网络接口设备所有的IP地址。
                {
                    InetAddress p_InetAddr = p_EnumIpAddr.nextElement();
                    if( ( !p_InetAddr.isLoopbackAddress() ) && ( p_InetAddr.getAddress().length != 4 ) ) //如果该IP地址不是回环地址，且是IPv6的。
                    {
                        p_IPAddrList.add( p_InetAddr.getHostAddress() );
                    }
                }
            }
            p_IPAddrList.add( "::" );
            p_IPAddrList.add( "::1" );

            ArrayAdapter< String > p_VdoFrmSzAdapter = new ArrayAdapter< String >( MainActivityPt, android.R.layout.simple_spinner_dropdown_item, p_IPAddrList );
            ( ( Spinner ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.IPAddrSpinnerId ) ).setAdapter( p_VdoFrmSzAdapter );
            ( ( Spinner ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.IPAddrSpinnerId ) ).setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() //设置IP地址Spinner控件的选择监听器。
            {
                @Override
                public void onItemSelected( AdapterView< ? > parent, View view, int position, long id )
                {
                    if( MainActivityPt.m_FirstIPaddrItemIsSelected == 0 ) //如果第一次IP地址未选择。
                        ( ( EditText ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.IPAddrEdTxtId ) ).setText( ( ( Spinner ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.IPAddrSpinnerId ) ).getSelectedItem().toString() );
                    else
                        MainActivityPt.m_FirstIPaddrItemIsSelected = 0; //设置第一次IP地址未选择。
                }

                @Override
                public void onNothingSelected( AdapterView< ? > parent )
                {

                }
            } );
        }
        catch( SocketException ignored )
        {
        }

        //设置端口。
        ( ( EditText ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.PortEdTxtId ) ).setText( "12345" );

        //设置使用什么对讲模式。
        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseAdoTkbkModeRdBtnId ) ).performClick();

        //设置音频输入设备、音频输出设备。
        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseSpeakerRdBtnId ) ).performClick();
        ( ( RadioButton ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.UseFrontCamereRdBtnId ) ).performClick();

        //设置音频输入出是否静音、视频输入输出是否黑屏。
        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.AdoInptIsMuteCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.AdoOtptIsMuteCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.VdoInptIsBlackCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.VdoOtptIsBlackCkBoxId ) ).setChecked( false );

        //设置是否绘制音频波形到Surface。
        ( ( CheckBox ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).setChecked( false );

        //设置系统音频输出音量拖动条。
        {
            SeekBar p_AdoOtptVolumePt = ( SeekBar ) MainActivityPt.m_MainLyotViewPt.findViewById( R.id.SystemAdoOtptVolmSkBarId ); //获取系统音频输出音量拖动条的指针。
            AudioManager p_AudioManagerPt = ( AudioManager ) MainActivityPt.getSystemService( Context.AUDIO_SERVICE ); //获取音频服务的指针。

            p_AdoOtptVolumePt.setMax( p_AudioManagerPt.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL ) ); //设置系统音频输出音量拖动条的最大值。
            p_AdoOtptVolumePt.setProgress( p_AudioManagerPt.getStreamVolume( AudioManager.STREAM_VOICE_CALL ) ); //设置系统音频输出音量拖动条的当前值。

            //设置系统音频输出音量拖动条变化消息监听器。
            p_AdoOtptVolumePt.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
            {
                @Override public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
                {
                    ( ( AudioManager ) MainActivityPt.getSystemService( Context.AUDIO_SERVICE ) ).setStreamVolume( AudioManager.STREAM_VOICE_CALL, progress, AudioManager.FLAG_PLAY_SOUND );
                    p_AdoOtptVolumePt.setProgress( p_AudioManagerPt.getStreamVolume( AudioManager.STREAM_VOICE_CALL ) );
                }

                @Override public void onStartTrackingTouch( SeekBar seekBar )
                {

                }

                @Override public void onStopTrackingTouch( SeekBar seekBar )
                {

                }
            } );

            //设置系统音量变化消息监听器。
            IntentFilter p_VolumeChangedActionIntentFilterPt = new IntentFilter();
            p_VolumeChangedActionIntentFilterPt.addAction( "android.media.VOLUME_CHANGED_ACTION" );
            MainActivityPt.registerReceiver(
                    new BroadcastReceiver()
                    {
                        @Override public void onReceive( Context context, Intent intent )
                        {
                            p_AdoOtptVolumePt.setProgress( p_AudioManagerPt.getStreamVolume( AudioManager.STREAM_VOICE_CALL ) );
                        }
                    },
                    p_VolumeChangedActionIntentFilterPt );
        }

        //设置设置。
        {
            ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAjbRecvOtptFrmRdBtnId ) ).performClick();
            ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseFrgndSrvcCkBoxId ) ).setChecked( true );
            ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveStsToTxtFileCkBoxId ) ).setChecked( true );
            ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsPrintLogcatShowToastCkBoxId ) ).setChecked( true );
            ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseWakeLockCkBoxId ) ).setChecked( true );
            ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoVdoInptOtptToAviFileCkBoxId ) ).setChecked( true );

            //设置视频帧的大小。
            {
                ArrayList< String > p_VdoFrmSzList = new ArrayList< String >();
                p_VdoFrmSzList.add( "120×160" );
                p_VdoFrmSzList.add( "240×320" );
                p_VdoFrmSzList.add( "480×640" );
                p_VdoFrmSzList.add( "960×1280" );
                ArrayAdapter< String > p_VdoFrmSzAdapter = new ArrayAdapter< String >( MainActivityPt, android.R.layout.simple_spinner_dropdown_item, p_VdoFrmSzList );
                ( ( Spinner ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).setAdapter( p_VdoFrmSzAdapter );

                ( ( EditText ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzOtherWidthEdTxtId ) ).setText( "640" );
                ( ( EditText ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzOtherHeightEdTxtId ) ).setText( "480" );
            }

            //设置使用什么效果等级。
            ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectSuperRdBtnId ) ).performClick();

            //设置自适应抖动缓冲器。
            ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbMinNeedBufFrmCntEdTxtId ) ).setText( "5" );
            ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbMaxNeedBufFrmCntEdTxtId ) ).setText( "20" );
            ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbMaxCntuLostFrmCntEdTxtId ) ).setText( "20" );
            ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.AAjbAdaptSensitivityEdTxtId ) ).setText( "1.0" );
            ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.VAjbMinNeedBufFrmCntEdTxtId ) ).setText( "3" );
            ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.VAjbMaxNeedBufFrmCntEdTxtId ) ).setText( "24" );
            ( ( EditText ) MainActivityPt.m_AjbStngLyotViewPt.findViewById( R.id.VAjbAdaptSensitivityEdTxtId ) ).setText( "1.0" );

            //设置保存状态到Txt文件。
            ( ( EditText ) MainActivityPt.m_SaveStsToTxtFileStngLyotViewPt.findViewById( R.id.SaveStsToTxtFileFullPathEdTxtId ) ).setText( "Sts.txt" );

            //设置保存音视频输入输出到Avi文件。
            ( ( EditText ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileFullPathEdTxtId ) ).setText( "AdoVdoInptOtpt.avi" );
            ( ( EditText ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileWrBufSzBytEdTxtId ) ).setText( "8192" );
            ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveAdoInptCkBoxId ) ).setChecked( true );
            ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveAdoOtptCkBoxId ) ).setChecked( true );
            ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveVdoInptCkBoxId ) ).setChecked( true );
            ( ( CheckBox ) MainActivityPt.m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt.findViewById( R.id.SaveAdoVdoInptOtptToAviFileIsSaveVdoOtptCkBoxId ) ).setChecked( true );

            //设置保存音频输入输出到Wave文件。
            ( ( CheckBox ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileIsSaveAdoInptCkBoxId ) ).setChecked( true );
            ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoInptSrcFullPathEdTxtId ) ).setText( "AdoInptSrc.wav" );
            ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoInptRsltFullPathEdTxtId ) ).setText( "AdoInptRslt.wav" );
            ( ( CheckBox ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileIsSaveAdoOtptCkBoxId ) ).setChecked( true );
            ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileAdoOtptSrcFullPathEdTxtId ) ).setText( "AdoOtptSrc.wav" );
            ( ( EditText ) MainActivityPt.m_SaveAdoInptOtptToWaveFileStngLyotViewPt.findViewById( R.id.SaveAdoInptOtptToWaveFileWrBufSzBytEdTxtId ) ).setText( "8192" );
        }
    }

    //效果等级：低。
    public static void EffectLow( MainActivity MainActivityPt )
    {
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectLowRdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate8000RdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecmRdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexPrpocsNsRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate12RdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSzPrsetRdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSzPrsetRdBtnId ) ).setChecked( true );
        ( ( Spinner ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).setSelection( 0 );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenMsecEdTxtId ) ).setText( "500" );
        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).setText( "3.0" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).setText( "0.65" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( false );

        ( ( CheckBox ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
        ( ( TextView ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).setText( "0" );

        ( ( TextView ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).setText( "2" );
        ( ( TextView ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).setText( "0" );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( false );

        ( ( RadioButton ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmRdBtnId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenMsecEdTxtId ) ).setText( "500" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).setText( "1.0" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).setText( "0.6" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).setText( "0" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).setText( "2" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).setText( "0" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).setText( "380" );

        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).setText( "3" );

        ( ( TextView ) MainActivityPt.m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).setText( "3" );

        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).setText( "95" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).setText( "95" );
        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).setText( "20000" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).setText( "10" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).setText( "-200" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).setText( "20" );

        ( ( RadioButton ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseCbrRdBtnId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).setText( "1" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).setText( "1" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).setText( "1" ); //影响比特率。
        ( ( CheckBox ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).setText( "0" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).setText( "10" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).setText( "3" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).setText( "12" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).setText( "0" );

        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).setText( "10" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).setText( "1" );
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).setText( "1" );
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).setText( "0" );
    }

    //效果等级：中。
    public static void EffectMid( MainActivity MainActivityPt )
    {
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectMidRdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecRdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsxRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate15RdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSzPrsetRdBtnId ) ).setChecked( true );
        ( ( Spinner ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).setSelection( 1 );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenMsecEdTxtId ) ).setText( "500" );
        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).setText( "3.0" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).setText( "0.65" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( false );

        ( ( CheckBox ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
        ( ( TextView ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).setText( "0" );

        ( ( TextView ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).setText( "2" );
        ( ( TextView ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).setText( "0" );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( false );

        ( ( RadioButton ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeWebRtcAecmWebRtcAecRdBtnId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenMsecEdTxtId ) ).setText( "500" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).setText( "1.0" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).setText( "0.6" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).setText( "0" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).setText( "2" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).setText( "0" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).setText( "380" );

        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).setText( "3" );

        ( ( TextView ) MainActivityPt.m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).setText( "3" );

        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).setText( "95" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).setText( "95" );
        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).setText( "20000" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).setText( "10" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).setText( "-200" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).setText( "20" );

        ( ( RadioButton ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseCbrRdBtnId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).setText( "4" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).setText( "4" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).setText( "40" ); //影响比特率。
        ( ( CheckBox ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).setText( "0" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).setText( "20" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).setText( "3" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).setText( "15" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).setText( "0" );

        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).setText( "20" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).setText( "1" );
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).setText( "1" );
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).setText( "1" );
    }

    //效果等级：高。
    public static void EffectHigh( MainActivity MainActivityPt )
    {
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectHighRdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexWebRtcAecRdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate15RdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSzPrsetRdBtnId ) ).setChecked( true );
        ( ( Spinner ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).setSelection( 2 );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenMsecEdTxtId ) ).setText( "500" );
        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).setText( "3.0" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).setText( "0.65" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( false );

        ( ( CheckBox ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
        ( ( TextView ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).setText( "0" );

        ( ( TextView ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).setText( "2" );
        ( ( TextView ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).setText( "0" );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( false );

        ( ( RadioButton ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRdBtnId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenMsecEdTxtId ) ).setText( "500" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).setText( "1.0" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).setText( "0.6" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).setText( "0" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).setText( "2" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).setText( "0" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).setText( "380" );

        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).setText( "3" );

        ( ( TextView ) MainActivityPt.m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).setText( "3" );

        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).setText( "95" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).setText( "95" );
        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).setText( "20000" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).setText( "10" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).setText( "-200" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).setText( "20" );

        ( ( RadioButton ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseVbrRdBtnId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).setText( "8" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).setText( "8" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).setText( "80" ); //影响比特率。
        ( ( CheckBox ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).setText( "0" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).setText( "40" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).setText( "3" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).setText( "15" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).setText( "0" );

        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).setText( "40" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).setText( "1" );
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).setText( "1" );
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).setText( "2" );
    }

    //效果等级：超。
    public static void EffectSuper( MainActivity MainActivityPt )
    {
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectSuperRdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexWebRtcAecRdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseRNNoiseRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate24RdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSzPrsetRdBtnId ) ).setChecked( true );
        ( ( Spinner ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).setSelection( 2 );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenMsecEdTxtId ) ).setText( "500" );
        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).setText( "3.0" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).setText( "0.65" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( false );

        ( ( CheckBox ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
        ( ( TextView ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).setText( "0" );

        ( ( TextView ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).setText( "2" );
        ( ( TextView ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).setText( "0" );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( false );

        ( ( RadioButton ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRdBtnId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenMsecEdTxtId ) ).setText( "500" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).setText( "1.0" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).setText( "0.6" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).setText( "0" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).setText( "2" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).setText( "0" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).setText( "380" );

        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).setText( "3" );

        ( ( TextView ) MainActivityPt.m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).setText( "3" );

        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).setText( "95" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).setText( "95" );
        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).setText( "20000" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).setText( "10" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).setText( "-200" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).setText( "20" );

        ( ( RadioButton ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseVbrRdBtnId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).setText( "10" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).setText( "10" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).setText( "100" ); //影响比特率。
        ( ( CheckBox ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).setText( "0" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).setText( "60" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).setText( "3" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).setText( "24" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).setText( "1" );

        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).setText( "60" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).setText( "1" );
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).setText( "1" );
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).setText( "2" );
    }

    //效果等级：特。
    public static void EffectPremium( MainActivity MainActivityPt )
    {
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseEffectPremiumRdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate32000RdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexWebRtcAecRdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseRNNoiseRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsSaveAdoInptOtptToWaveFileCkBoxId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate30RdBtnId ) ).setChecked( true );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSzPrsetRdBtnId ) ).setChecked( true );
        ( ( Spinner ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.VdoFrmSzPrsetSpinnerId ) ).setSelection( 3 );
        ( ( RadioButton ) MainActivityPt.m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenMsecEdTxtId ) ).setText( "500" );
        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).setText( "3.0" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).setText( "0.65" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
        ( ( TextView ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( false );

        ( ( CheckBox ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
        ( ( TextView ) MainActivityPt.m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).setText( "0" );

        ( ( TextView ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).setText( "2" );
        ( ( TextView ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).setText( "0" );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( false );

        ( ( RadioButton ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRdBtnId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenMsecEdTxtId ) ).setText( "500" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).setText( "1.0" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).setText( "0.6" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).setText( "0" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).setText( "2" );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).setText( "0" );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
        ( ( CheckBox ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).setText( "380" );

        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).setText( "-32768" );
        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).setText( "3" );

        ( ( TextView ) MainActivityPt.m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).setText( "3" );

        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).setText( "95" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).setText( "95" );
        ( ( CheckBox ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).setText( "20000" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).setText( "10" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).setText( "-200" );
        ( ( TextView ) MainActivityPt.m_SpeexPrpocsStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).setText( "20" );

        ( ( RadioButton ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseVbrRdBtnId ) ).setChecked( true );
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).setText( "10" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).setText( "10" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).setText( "100" ); //影响比特率。
        ( ( CheckBox ) MainActivityPt.m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( true );

        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).setText( "0" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).setText( "80" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).setText( "3" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).setText( "30" );
        ( ( TextView ) MainActivityPt.m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).setText( "2" );

        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).setText( "80" ); //影响比特率。
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).setText( "1" );
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).setText( "1" );
        ( ( TextView ) MainActivityPt.m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).setText( "2" );
    }
}