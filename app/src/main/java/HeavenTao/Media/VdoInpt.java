package HeavenTao.Media;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import HeavenTao.Vdo.*;
import HeavenTao.Data.*;

public class VdoInpt //视频输入。
{
	MediaPocsThrd m_MediaPocsThrdPt; //存放媒体处理线程的指针。

	public int m_IsUse; //存放是否使用视频输入，为0表示不使用，为非0表示要使用。
	public int m_IsInit; //存放是否初始化视频输入，为0表示未初始化，为非0表示已初始化。

	public int m_MaxSmplRate; //存放最大采样频率，取值范围为[1,60]，实际帧率和图像的亮度有关，亮度较高时采样频率可以达到最大值，亮度较低时系统就自动降低采样频率来提升亮度。
	public int m_FrmWidth; //存放屏幕旋转0度时，帧的宽度，单位为像素，只能为偶数。
	public int m_FrmHeight; //存放屏幕旋转0度时，帧的高度，单位为像素，只能为偶数。
	public long m_Yu12FrmLenByt; //存放Yu12格式帧的长度，单位为字节，为m_FrmWidth * m_FrmHeight * 3 / 2。
	public int m_SrcFrmWidth; //存放屏幕旋转0度时，原始帧的宽度，单位为像素，只能为偶数，为0表示自动选择。
	public int m_SrcFrmHeight; //存放屏幕旋转0度时，原始帧的高度，单位为像素，只能为偶数，为0表示自动选择。
	public int m_ScreenRotate; //存放屏幕旋转的角度，只能为0、90、180、270，0度表示竖屏，其他表示顺时针旋转。

	public int m_UseWhatEncd; //存放使用什么编码器，为0表示Yu12原始数据，为1表示OpenH264编码器，为2表示系统自带H264编码器。

	class OpenH264Encd //存放OpenH264编码器。
	{
		HeavenTao.Vdo.OpenH264Encd m_Pt; //存放指针。
		int m_VdoType; //存放视频类型，为0表示实时摄像头视频，为1表示实时屏幕内容视频，为2表示非实时摄像头视频，为3表示非实时屏幕内容视频，为4表示其他视频。
		int m_EncdBitrate; //存放编码后比特率，单位为bps。
		int m_BitrateCtrlMode; //存放比特率控制模式，为0表示质量优先模式，为1表示比特率优先模式，为2表示缓冲区优先模式，为3表示时间戳优先模式。
		int m_IDRFrmIntvl; //存放IDR帧间隔帧数，单位为个帧，为0表示仅第一帧为IDR帧，为大于0表示每隔这么帧就至少有一个IDR帧。
		int m_Cmplxt; //存放复杂度，复杂度越高压缩率不变、CPU使用率越高、画质越好，取值区间为[0,2]。
	}
	OpenH264Encd m_OpenH264EncdPt = new OpenH264Encd();

	class SystemH264Encd //存放系统自带H264编码器。
	{
		HeavenTao.Vdo.SystemH264Encd m_Pt; //存放指针。
		int m_EncdBitrate; //存放编码后比特率，单位为bps。
		int m_BitrateCtrlMode; //存放比特率控制模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ(0x00)表示质量模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR(0x01)表示动态比特率模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR(0x02)表示固定比特率模式。
		int m_IDRFrmIntvlTimeSec; //存放IDR帧间隔时间，单位为秒，为负数表示仅第一帧为IDR帧，为0表示每一帧都为IDR帧，为大于0表示每这么多秒就有一个IDR帧。
		int m_Cmplxt; //存放复杂度，复杂度越高压缩率不变、CPU使用率越高、画质越好，取值区间为[0,2]。
	}
	SystemH264Encd m_SystemH264EncdPt = new SystemH264Encd();

	public class Dvc //存放设备。
	{
		Camera m_Pt; //存放指针。
		int m_UseWhatDvc; //存放使用什么设备，为-1表示自动查找，为0表示前置摄像头，为1表示后置摄像头。
		int m_FrontCameraId; //存放前置摄像头的标识符，为-1表示自动查找。
		int m_BackCameraId; //存放后置摄像头的标识符，为-1表示自动查找。
		public HTSurfaceView m_PrvwSurfaceViewPt; //存放预览Surface视图的指针。
		SurfaceHolder.Callback m_PrvwSurfaceClbkPt; //存放预览Surface回调函数的指针。
		byte m_PrvwClbkBufPtPt[][]; //存放预览回调函数缓冲区的指针。
		VodInptPrvwClbk m_PrvwClbkPt; //存放预览回调函数的指针。
		ConcurrentLinkedQueue< byte[] > m_Nv21SrcFrmCntnrPt; //存放Nv21格式原始帧容器的指针。
		int m_Nv21SrcFrmWidth; //存放Nv21格式原始帧的宽度，单位为像素。
		int m_Nv21SrcFrmHeight; //存放Nv21格式原始帧的高度，单位为像素。
		long m_Nv21SrcFrmLenByt; //存放Nv21格式原始帧的长度，单位为字节，为m_Nv21SrcFrmWidth * m_Nv21SrcFrmHeight * 3 / 2。
		int m_Nv21SrcFrmIsCrop; //存放Nv21格式原始帧是否裁剪，为0表示不裁剪，为非0表示要裁剪。
		int m_Nv21SrcFrmCropX; //存放Nv21格式原始帧裁剪区域左上角的横坐标，单位像素。
		int m_Nv21SrcFrmCropY; //存放Nv21格式原始帧裁剪区域左上角的纵坐标，单位像素。
		int m_Nv21SrcFrmCropWidth; //存放Nv21格式原始帧裁剪区域的宽度，单位像素。
		int m_Nv21SrcFrmCropHeight; //存放Nv21格式原始帧裁剪区域的高度，单位像素。
		int m_Yu12SrcFrmRotate; //存放Yu12格式原始帧旋转的角度，只能为0、90、180、270，0度表示横屏，其他表示顺时针旋转。
		int m_Yu12SrcFrmRotateWidth; //存放Yu12格式原始帧旋转后的宽度，单位为像素。
		int m_Yu12SrcFrmRotateHeight; //存放Yu12格式原始帧旋转后的高度，单位为像素。
		int m_Yu12SrcFrmIsScale; //存放Yu12格式原始帧是否缩放，为0表示不缩放，为非0表示要缩放。
		int m_Yu12SrcFrmScaleWidth; //存放Yu12格式原始帧缩放后的宽度，单位为像素，为m_FrmWidth或m_FrmHeight。
		int m_Yu12SrcFrmScaleHeight; //存放Yu12格式原始帧缩放后的高度，单位为像素，为m_FrmWidth或m_FrmHeight。
		long m_Yu12SrcFrmScaleLenByt; //存放Yu12格式原始帧缩放后的长度，单位为字节，为m_Yu12SrcFrmScaleWidth * m_Yu12SrcFrmScaleHeight * 3 / 2。
		int m_IsBlack; //存放是否黑屏，为0表示有图像，为非0表示黑屏。
	}
	public Dvc m_DvcPt = new Dvc();

	public class Frm //存放帧。
	{
		byte m_Nv21SrcFrmPt[]; //存放Nv21格式原始帧的指针，宽度为m_DvcPt.m_Nv21SrcFrmWidth，高度为m_DvcPt.m_Nv21SrcFrmHeight，大小为m_DvcPt.m_Nv21SrcFrmLenByt。
		byte m_Yu12RsltFrmPt[]; //存放Yu12格式结果帧的指针，宽度为m_DvcPt.m_Yu12SrcFrmScaleWidth，高度为m_DvcPt.m_Yu12SrcFrmScaleHeight，大小为m_DvcPt.m_Yu12SrcFrmScaleLenByt。
		byte m_EncdRsltFrmPt[]; //存放已编码格式结果帧的指针，大小为m_DvcPt.m_Yu12SrcFrmScaleLenByt。
		HTLong m_EncdRsltFrmLenBytPt; //存放已编码格式结果帧的长度，单位为字节。
		long m_TimeStampMsec; //存放时间戳，单位为毫秒。
	}
	public ConcurrentLinkedQueue< Frm > m_FrmCntnrPt; //存放帧容器的指针。
	public ConcurrentLinkedQueue< Frm > m_IdleFrmCntnrPt; //存放空闲帧容器的指针。

	class Thrd //存放线程。
	{
		int m_IsInitThrdTmpVar; //存放是否初始化线程的临时变量。
		double m_LastFrmTickMsec; //存放上一个帧的嘀嗒钟，单位为毫秒。
		double m_FrmTimeStepMsec; //存放帧的时间步进，单位为毫秒。
		byte m_Nv21SrcFrmPt[]; //存放Nv21格式原始帧的指针。
		byte m_TmpFrm1Pt[]; //存放临时帧的指针。
		byte m_TmpFrm2Pt[]; //存放临时帧的指针。
		long m_TmpFrmSzByt; //存放临时帧的大小，单位为字节。
		HTLong m_TmpFrmLenBytPt; //存放临时帧的长度，单位为字节。
		Frm m_FrmPt; //存放帧的指针。
		int m_ElmTotal; //存放元素总数。
		long m_LastTickMsec; //存放上次的嘀嗒钟，单位为毫秒。
		long m_NowTickMsec; //存放本次的嘀嗒钟，单位为毫秒。

		VdoInptThrd m_ThrdPt; //存放线程的指针。
		int m_ExitFlag; //存放退出标记，为0表示保持运行，为1表示请求退出。
	}
	Thrd m_ThrdPt = new Thrd();

	//初始化视频输入。
	int Init()
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。
		long p_LastMsec = 0;
		long p_NowMsec = 0;

		Out:
		{
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastMsec = SystemClock.uptimeMillis(); //记录初始化开始的时间。

			//初始化设备。
			{
				int p_CameraId = -1;
				Camera.CameraInfo p_CameraInfoPt = new Camera.CameraInfo();

				//打开设备。
				{
					//查找设备对应的标识符。
					if( m_DvcPt.m_UseWhatDvc == -1 ) //如果自动查找摄像头。
					{
						p_CameraId = -1;
					}
					else if( m_DvcPt.m_UseWhatDvc == 0 ) //如果要使用前置摄像头。
					{
						p_CameraId = m_DvcPt.m_FrontCameraId;
					}
					else if( m_DvcPt.m_UseWhatDvc == 1 ) //如果要使用后置摄像头。
					{
						p_CameraId = m_DvcPt.m_BackCameraId;
					}
					if( p_CameraId == -1 ) //如果需要自动查找设备ID。
					{
						for( p_CameraId = 0; p_CameraId < Camera.getNumberOfCameras(); p_CameraId++ )
						{
							try
							{
								Camera.getCameraInfo( p_CameraId, p_CameraInfoPt );
							}
							catch( Exception e )
							{
								String p_InfoStrPt = "媒体处理线程：视频输入：获取设备 " + p_CameraId + " 的信息失败。原因：" + e.getMessage();
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, p_InfoStrPt );
								if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
								break Out;
							}
							if( p_CameraInfoPt.facing == Camera.CameraInfo.CAMERA_FACING_FRONT )
							{
								if( ( m_DvcPt.m_UseWhatDvc == -1 ) || ( m_DvcPt.m_UseWhatDvc == 0 ) )
								{
									if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：查找到前置摄像头设备对应的标识符为" + p_CameraId + "。" );
									break;
								}
							}
							else if( p_CameraInfoPt.facing == Camera.CameraInfo.CAMERA_FACING_BACK )
							{
								if( ( m_DvcPt.m_UseWhatDvc == -1 ) || ( m_DvcPt.m_UseWhatDvc == 1 ) )
								{
									if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：查找到后置摄像头设备对应的标识符为" + p_CameraId + "。" );
									break;
								}
							}
						}
						if( p_CameraId == Camera.getNumberOfCameras() )
						{
							String p_InfoStrPt = "媒体处理线程：视频输入：查找设备对应的标识符失败。原因：没有" + ( ( m_DvcPt.m_UseWhatDvc == -1 ) ? "前置或后置摄像头。" : ( m_DvcPt.m_UseWhatDvc == 0 ) ? "前置摄像头。" : "后置摄像头。" );
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, p_InfoStrPt );
							if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
							break Out;
						}
					}

					//打开设备。
					try
					{
						m_DvcPt.m_Pt = Camera.open( p_CameraId );
					}
					catch( RuntimeException e )
					{
						String p_InfoStrPt = "媒体处理线程：视频输入：打开设备失败。原因：" + e.getMessage();
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, p_InfoStrPt );
						if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
						break Out;
					}
				}

				Camera.Parameters p_CameraParaPt = m_DvcPt.m_Pt.getParameters(); //获取设备的参数。

				p_CameraParaPt.setPreviewFormat( ImageFormat.NV21 ); //设置预览帧的格式。

				p_CameraParaPt.setPreviewFrameRate( m_MaxSmplRate ); //设置最大采样频率。

				//遍历设备支持的Nv21格式原始帧的大小，并智能选择满足目标Nv21格式原始帧的大小。
				int p_TgtNv21SrcFrmWidth = m_FrmHeight; //存放目标Nv21格式原始帧的宽度，单位为像素。因为帧要旋转，所以设置为帧的高度。
				int p_TgtNv21SrcFrmHeight = m_FrmWidth; //存放目标Nv21格式原始帧的高度，单位为像素。因为帧要旋转，所以设置为帧的宽度。
				double p_TgtNv21SrcFrmAspectRatio = ( double )p_TgtNv21SrcFrmWidth / ( double )p_TgtNv21SrcFrmHeight; //存放目标Nv21格式原始帧的宽高比。
				List< Camera.Size > p_SupportedPrvwSizesListPt = p_CameraParaPt.getSupportedPreviewSizes(); //获取设备支持预览的Nv21格式原始帧的大小的列表。
				double p_CurNv21SrcFrmAspectRatio; //存放本次Nv21格式原始帧的宽高比。
				int p_CurNv21SrcFrmCropX; //存放本次Nv21格式原始帧裁剪区域左上角的横坐标，单位像素。
				int p_CurNv21SrcFrmCropY; //存放本次Nv21格式原始帧裁剪区域左上角的纵坐标，单位像素。
				int p_CurNv21SrcFrmCropWidth; //存放本次Nv21格式原始帧裁剪区域的宽度，单位像素。
				int p_CurNv21SrcFrmCropHeight; //存放本次Nv21格式原始帧裁剪区域的高度，单位像素。
				int p_IsSetSelCur; //存放是否设置选择的为本次的，为0表示不设置，为非0表示要设置。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：目标的Nv21格式原始帧的大小：宽度：" + p_TgtNv21SrcFrmWidth + "，高度：" + p_TgtNv21SrcFrmHeight + "。" );
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：指定的Nv21格式原始帧的大小：宽度：" + m_SrcFrmWidth + "，高度：" + m_SrcFrmHeight + "。" );
				for( Camera.Size p_CurNv21SrcFrmSz : p_SupportedPrvwSizesListPt )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：设备支持的Nv21格式原始帧的大小：宽度：" + p_CurNv21SrcFrmSz.width + "，高度：" + p_CurNv21SrcFrmSz.height + "。" );

					//设置本次Nv21格式原始帧的宽高比、裁剪宽度、裁剪高度、裁剪区域左上角的坐标。
					p_CurNv21SrcFrmAspectRatio = ( double )p_CurNv21SrcFrmSz.width / ( double )p_CurNv21SrcFrmSz.height;
					if( p_CurNv21SrcFrmAspectRatio >= p_TgtNv21SrcFrmAspectRatio ) //如果本次Nv21格式原始帧的宽高比比目标的大，就表示需要裁剪宽度。
					{
						p_CurNv21SrcFrmCropWidth = ( int )( ( double )p_CurNv21SrcFrmSz.height * p_TgtNv21SrcFrmAspectRatio ); //设置本次Nv21格式原始帧裁剪区域左上角的宽度，使裁剪区域居中。
						p_CurNv21SrcFrmCropWidth -= p_CurNv21SrcFrmCropWidth % 2;
						p_CurNv21SrcFrmCropHeight = p_CurNv21SrcFrmSz.height; //设置本次Nv21格式原始帧裁剪区域左上角的高度，使裁剪区域居中。

						p_CurNv21SrcFrmCropX = ( p_CurNv21SrcFrmSz.width - p_CurNv21SrcFrmCropWidth ) / 2; //设置本次Nv21格式原始帧裁剪区域左上角的横坐标，使裁剪区域居中。
						p_CurNv21SrcFrmCropX -= p_CurNv21SrcFrmCropX % 2;
						p_CurNv21SrcFrmCropY = 0; //设置本次Nv21格式原始帧裁剪区域左上角的纵坐标。
					}
					else //如果本次Nv21格式原始帧的宽高比比目标的小，就表示需要裁剪高度。
					{
						p_CurNv21SrcFrmCropWidth = p_CurNv21SrcFrmSz.width; //设置本次Nv21格式原始帧裁剪区域左上角的宽度，使裁剪区域居中。
						p_CurNv21SrcFrmCropHeight = ( int )( ( double )p_CurNv21SrcFrmSz.width / p_TgtNv21SrcFrmAspectRatio ); //设置本次Nv21格式原始帧裁剪区域左上角的高度，使裁剪区域居中。
						p_CurNv21SrcFrmCropHeight -= p_CurNv21SrcFrmCropHeight % 2;

						p_CurNv21SrcFrmCropX = 0; //设置本次Nv21格式原始帧裁剪区域左上角的横坐标。
						p_CurNv21SrcFrmCropY = ( p_CurNv21SrcFrmSz.height - p_CurNv21SrcFrmCropHeight ) / 2; //设置本次Nv21格式原始帧裁剪区域左上角的纵坐标，使裁剪区域居中。
						p_CurNv21SrcFrmCropY -= p_CurNv21SrcFrmCropY % 2;
					}

					//如果本次的帧的大小符合原始帧的大小，就设置选择的为本次的。
					//如果要自动选择原始帧的大小，或选择的帧的大小不符合原始帧的大小，就判断本次的和目标的。
					p_IsSetSelCur = 0;
					if( ( p_CurNv21SrcFrmSz.width == m_SrcFrmWidth ) && ( p_CurNv21SrcFrmSz.height == m_SrcFrmHeight ) )
					{
						p_IsSetSelCur = 1;
					}
					else if( ( ( m_SrcFrmWidth == 0 ) && ( m_SrcFrmHeight == 0 ) ) ||
							 ( m_DvcPt.m_Nv21SrcFrmWidth != m_SrcFrmWidth ) || ( m_DvcPt.m_Nv21SrcFrmHeight != m_SrcFrmHeight ) )
					{
						//如果选择的帧裁剪区域不满足目标的（包括选择的帧裁剪区域为0），则只要本次的帧裁剪区域比选择的大，就设置选择的为本次的。
						//如果选择的帧裁剪区域满足目标的，就只要本次的帧裁剪区域满足目标的，且本次的帧裁剪区域比选择的小、或本次的帧裁剪区域相同但裁剪量比选择的小，就设置选择的为本次的。
						if( ( m_DvcPt.m_Nv21SrcFrmCropWidth < p_TgtNv21SrcFrmWidth ) && ( m_DvcPt.m_Nv21SrcFrmCropHeight < p_TgtNv21SrcFrmHeight ) )
						{
							if( ( p_CurNv21SrcFrmCropWidth > m_DvcPt.m_Nv21SrcFrmCropWidth ) && ( p_CurNv21SrcFrmCropHeight > m_DvcPt.m_Nv21SrcFrmCropHeight ) )
							{
								p_IsSetSelCur = 1;
							}
						}
						else
						{
							if(
								( ( p_CurNv21SrcFrmCropWidth >= p_TgtNv21SrcFrmWidth ) && ( p_CurNv21SrcFrmCropHeight >= p_TgtNv21SrcFrmHeight ) )
								&&
								(
									( ( p_CurNv21SrcFrmCropWidth < m_DvcPt.m_Nv21SrcFrmCropWidth ) || ( p_CurNv21SrcFrmCropHeight < m_DvcPt.m_Nv21SrcFrmCropHeight ) )
									||
									( ( p_CurNv21SrcFrmCropWidth == m_DvcPt.m_Nv21SrcFrmCropWidth ) && ( p_CurNv21SrcFrmCropHeight == m_DvcPt.m_Nv21SrcFrmCropHeight ) && ( p_CurNv21SrcFrmCropX + p_CurNv21SrcFrmCropY < m_DvcPt.m_Nv21SrcFrmCropX + m_DvcPt.m_Nv21SrcFrmCropY ) )
								)
							)
							{
								p_IsSetSelCur = 1;
							}
						}
					}
					if( p_IsSetSelCur != 0 ) //如果要设置选择的为本次的。
					{
						m_DvcPt.m_Nv21SrcFrmWidth = p_CurNv21SrcFrmSz.width;
						m_DvcPt.m_Nv21SrcFrmHeight = p_CurNv21SrcFrmSz.height;
						m_DvcPt.m_Nv21SrcFrmLenByt = m_DvcPt.m_Nv21SrcFrmWidth * m_DvcPt.m_Nv21SrcFrmHeight * 3 / 2;

						m_DvcPt.m_Nv21SrcFrmCropX = p_CurNv21SrcFrmCropX;
						m_DvcPt.m_Nv21SrcFrmCropY = p_CurNv21SrcFrmCropY;
						m_DvcPt.m_Nv21SrcFrmCropWidth = p_CurNv21SrcFrmCropWidth;
						m_DvcPt.m_Nv21SrcFrmCropHeight = p_CurNv21SrcFrmCropHeight;
					}
				}
				p_CameraParaPt.setPreviewSize( m_DvcPt.m_Nv21SrcFrmWidth, m_DvcPt.m_Nv21SrcFrmHeight ); //设置预览的帧的大小为选择的帧的大小。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：选择设备的Nv21格式原始帧的大小：宽度：" + m_DvcPt.m_Nv21SrcFrmWidth + "，高度：" + m_DvcPt.m_Nv21SrcFrmHeight + "。" );

				//判断Nv21格式原始帧是否裁剪。
				if( ( m_DvcPt.m_Nv21SrcFrmWidth > m_DvcPt.m_Nv21SrcFrmCropWidth ) || //如果Nv21格式原始帧的宽度比裁剪区域的宽度大，就表示需要裁剪宽度。
					( m_DvcPt.m_Nv21SrcFrmHeight > m_DvcPt.m_Nv21SrcFrmCropHeight ) ) //如果Nv21格式原始帧的高度比裁剪区域的高度大，就表示需要裁剪高度。
				{
					m_DvcPt.m_Nv21SrcFrmIsCrop = 1; //设置Nv21格式原始帧要裁剪。
				}
				else //如果Nv21格式原始帧的宽度和高度与裁剪区域的宽度和高度一致，就表示不需要裁剪。
				{
					m_DvcPt.m_Nv21SrcFrmIsCrop = 0; //设置Nv21格式原始帧不裁剪。
				}
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：Nv21格式原始帧是否裁剪：" + m_DvcPt.m_Nv21SrcFrmIsCrop + "，裁剪区域左上角的横坐标：" + m_DvcPt.m_Nv21SrcFrmCropX + "，纵坐标：" + m_DvcPt.m_Nv21SrcFrmCropY + "，裁剪区域的宽度：" + m_DvcPt.m_Nv21SrcFrmCropWidth + "，高度：" + m_DvcPt.m_Nv21SrcFrmCropHeight + "。" );

				//设置Yu12格式原始帧。
				if( p_CameraInfoPt.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ) //如果要使用前置摄像头。
				{
					m_DvcPt.m_Yu12SrcFrmRotate = ( 270 + m_ScreenRotate ) % 360; //设置Yu12格式原始帧的旋转角度。
				}
				else //如果要使用后置摄像头。
				{
					m_DvcPt.m_Yu12SrcFrmRotate = ( 450 - m_ScreenRotate ) % 360; //设置Yu12格式原始帧的旋转角度。
				}
				if( ( m_DvcPt.m_Yu12SrcFrmRotate == 0 ) || ( m_DvcPt.m_Yu12SrcFrmRotate == 180 ) ) //如果旋转后为横屏。
				{
					m_DvcPt.m_Yu12SrcFrmRotateWidth = m_DvcPt.m_Nv21SrcFrmCropWidth; //设置Yu12格式原始帧旋转后的宽度。
					m_DvcPt.m_Yu12SrcFrmRotateHeight = m_DvcPt.m_Nv21SrcFrmCropHeight; //设置Yu12格式原始帧旋转后的高度。
				}
				else //如果旋转后为竖屏。
				{
					m_DvcPt.m_Yu12SrcFrmRotateWidth = m_DvcPt.m_Nv21SrcFrmCropHeight; //设置Yu12格式原始帧旋转后的宽度。
					m_DvcPt.m_Yu12SrcFrmRotateHeight = m_DvcPt.m_Nv21SrcFrmCropWidth; //设置Yu12格式原始帧旋转后的高度。
				}
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：Yu12格式原始帧旋转后的宽度：" + m_DvcPt.m_Yu12SrcFrmRotateWidth + "，旋转后的高度：" + m_DvcPt.m_Yu12SrcFrmRotateHeight + "。" );

				//设置Yu12格式原始帧是否缩放。
				if( ( m_DvcPt.m_Nv21SrcFrmCropWidth != p_TgtNv21SrcFrmWidth ) || ( m_DvcPt.m_Nv21SrcFrmCropHeight != p_TgtNv21SrcFrmHeight ) )
				{
					m_DvcPt.m_Yu12SrcFrmIsScale = 1; //设置Yu12格式原始帧要缩放。
				}
				else
				{
					m_DvcPt.m_Yu12SrcFrmIsScale = 0; //设置Yu12格式原始帧不缩放。
				}
				if( ( m_DvcPt.m_Yu12SrcFrmRotate == 0 ) || ( m_DvcPt.m_Yu12SrcFrmRotate == 180 ) ) //如果旋转后为横屏。
				{
					m_DvcPt.m_Yu12SrcFrmScaleWidth = p_TgtNv21SrcFrmWidth; //设置Yu12格式原始帧缩放后的宽度。
					m_DvcPt.m_Yu12SrcFrmScaleHeight = p_TgtNv21SrcFrmHeight; //设置Yu12格式原始帧缩放后的高度。
				}
				else //如果旋转后为竖屏。
				{
					m_DvcPt.m_Yu12SrcFrmScaleWidth = p_TgtNv21SrcFrmHeight; //设置Yu12格式原始帧缩放后的宽度。
					m_DvcPt.m_Yu12SrcFrmScaleHeight = p_TgtNv21SrcFrmWidth; //设置Yu12格式原始帧缩放后的高度。
				}
				m_DvcPt.m_Yu12SrcFrmScaleLenByt = m_DvcPt.m_Yu12SrcFrmScaleWidth * m_DvcPt.m_Yu12SrcFrmScaleHeight * 3 / 2; //设置Yu12格式原始帧缩放后的长度。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：Yu12格式原始帧是否缩放：" + m_DvcPt.m_Yu12SrcFrmIsScale + "，缩放后的宽度：" + m_DvcPt.m_Yu12SrcFrmScaleWidth + "，缩放后的高度：" + m_DvcPt.m_Yu12SrcFrmScaleHeight + "。" );

				//设置对焦模式。
				List<String> p_FocusModesListPt = p_CameraParaPt.getSupportedFocusModes();
				String p_PrvwFocusModePt = "";
				for( int p_TmpInt = 0; p_TmpInt < p_FocusModesListPt.size(); p_TmpInt++ )
				{
					switch( p_FocusModesListPt.get( p_TmpInt ) )
					{
						case Camera.Parameters.FOCUS_MODE_AUTO: //自动对焦模式。应用程序应调用autoFocus（AutoFocusCallback）以此模式启动焦点。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：设备支持的对焦模式：FOCUS_MODE_AUTO，自动对焦模式。" );
							break;
						case Camera.Parameters.FOCUS_MODE_MACRO: //微距（特写）对焦模式。应用程序应调用autoFocus（AutoFocusCallback）以此模式开始聚焦。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：设备支持的对焦模式：FOCUS_MODE_MACRO，微距（特写）对焦模式。" );
							break;
						case Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO: //用于视频的连续自动对焦模式。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：设备支持的对焦模式：FOCUS_MODE_CONTINUOUS_VIDEO，用于视频的连续自动对焦模式。" );
							p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
							break;
						case Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE: //用于拍照的连续自动对焦模式，比视频的连续自动对焦模式对焦速度更快。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：设备支持的对焦模式：FOCUS_MODE_CONTINUOUS_PICTURE，用于拍照的连续自动对焦模式。" );
							if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) )
								p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
							break;
						case Camera.Parameters.FOCUS_MODE_EDOF: //扩展景深（EDOF）对焦模式，对焦以数字方式连续进行。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：设备支持的对焦模式：FOCUS_MODE_EDOF，扩展景深（EDOF）对焦模式。" );
							if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
								!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) )
								p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_EDOF;
							break;
						case Camera.Parameters.FOCUS_MODE_FIXED: //固定焦点对焦模式。如果焦点无法调节，则相机始终处于此模式。如果相机具有自动对焦，则此模式可以固定焦点，通常处于超焦距。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：设备支持的对焦模式：FOCUS_MODE_FIXED，固定焦点对焦模式。" );
							if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
								!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) &&
								!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_EDOF ) )
								p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_FIXED;
							break;
						case Camera.Parameters.FOCUS_MODE_INFINITY: //无限远焦点对焦模式。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：设备支持的对焦模式：FOCUS_MODE_INFINITY，无限远焦点对焦模式。" );
							if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
								!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) &&
								!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_EDOF ) &&
								!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_FIXED ) )
								p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_INFINITY;
							break;
					}
				}
				p_CameraParaPt.setFocusMode( p_PrvwFocusModePt ); //设置对焦模式。

				try
				{
					m_DvcPt.m_Pt.setParameters( p_CameraParaPt ); //设置参数到设备。
				}
				catch( RuntimeException e )
				{
					String p_InfoStrPt = "媒体处理线程：视频输入：设置参数到设备失败。原因：" + e.getMessage();
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, p_InfoStrPt );
					if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
					break Out;
				}

				if( m_DvcPt.m_PrvwSurfaceViewPt == null )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：预览Surface视图的指针为空。" );
					break Out;
				}
				m_DvcPt.m_PrvwSurfaceViewPt.getHolder().setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS ); //设置预览Surface视图的类型。老机型上必须要用。
				m_DvcPt.m_PrvwSurfaceClbkPt = new SurfaceHolder.Callback() //创建预览Surface的回调函数。
				{
					@Override public void surfaceCreated( SurfaceHolder holder )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：VdoInptPrvwSurface Created" );
						m_MediaPocsThrdPt.VdoInptSetUseDvc( 0, m_DvcPt.m_UseWhatDvc, m_DvcPt.m_FrontCameraId, m_DvcPt.m_BackCameraId ); //重启视频输入。
					}

					@Override public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：VdoInptPrvwSurface Changed" );
					}

					@Override public void surfaceDestroyed( SurfaceHolder holder )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：VdoInptPrvwSurface Destroyed" );
					}
				};
				m_DvcPt.m_PrvwSurfaceViewPt.getHolder().addCallback( m_DvcPt.m_PrvwSurfaceClbkPt ); //设置预览Surface视图的回调函数添加。
				try
				{
					m_DvcPt.m_Pt.setPreviewDisplay( m_DvcPt.m_PrvwSurfaceViewPt.getHolder() ); //设置预览Surface视图。
				}
				catch( Exception ignored )
				{
				}
				if( m_ScreenRotate == 0 || m_ScreenRotate == 180 ) //如果屏幕为竖屏。
				{
					m_DvcPt.m_PrvwSurfaceViewPt.SetWidthToHeightRatio( ( float )m_DvcPt.m_Nv21SrcFrmHeight / m_DvcPt.m_Nv21SrcFrmWidth ); //设置预览SurfaceView视图的宽高比。
				}
				else //如果屏幕为横屏。
				{
					m_DvcPt.m_PrvwSurfaceViewPt.SetWidthToHeightRatio( ( float )m_DvcPt.m_Nv21SrcFrmWidth / m_DvcPt.m_Nv21SrcFrmHeight ); //设置预览SurfaceView视图的宽高比。
				}
				m_DvcPt.m_Pt.setDisplayOrientation( ( 450 - m_ScreenRotate ) % 360 ); //设置预览显示的旋转角度。

				//设置预览回调函数缓冲区的指针。
				m_DvcPt.m_PrvwClbkBufPtPt = new byte[ m_MaxSmplRate ][ ( int ) m_DvcPt.m_Nv21SrcFrmLenByt ];
				for( int p_TmpInt = 0; p_TmpInt < m_MaxSmplRate; p_TmpInt++ )
					m_DvcPt.m_Pt.addCallbackBuffer( m_DvcPt.m_PrvwClbkBufPtPt[ p_TmpInt ] ); //放入Nv21格式原始帧到设备。

				//设置预览回调函数。
				m_DvcPt.m_PrvwClbkPt = new VodInptPrvwClbk();
				m_DvcPt.m_Pt.setPreviewCallbackWithBuffer( m_DvcPt.m_PrvwClbkPt );

				//初始化Nv21格式原始帧容器。
				m_DvcPt.m_Nv21SrcFrmCntnrPt = new ConcurrentLinkedQueue();

				//设置设备开始预览。
				try
				{
					m_DvcPt.m_Pt.startPreview();
				}
				catch( RuntimeException e )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：设置设备开始预览失败。原因：" + e.getMessage() );
					break Out;
				}

				//发送视频输入设备改变线程消息。
				m_MediaPocsThrdPt.m_ThrdMsgQueuePt.SendMsg( 0, 0, MediaPocsThrd.ThrdMsgTyp.ThrdMsgTypVdoInptDvcChg, p_CameraId );

				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：初始化设备成功。" );
			}

			//初始化编码器。
			switch( m_UseWhatEncd )
			{
				case 0: //如果要使用Yu12原始数据。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：初始化Yu12原始数据成功。" );
					break;
				}
				case 1: //如果要使用OpenH264编码器。
				{
					m_OpenH264EncdPt.m_Pt = new HeavenTao.Vdo.OpenH264Encd();
					if( m_OpenH264EncdPt.m_Pt.Init( m_MediaPocsThrdPt.m_LicnCodePt, m_DvcPt.m_Yu12SrcFrmScaleWidth, m_DvcPt.m_Yu12SrcFrmScaleHeight, m_OpenH264EncdPt.m_VdoType, m_OpenH264EncdPt.m_EncdBitrate, m_OpenH264EncdPt.m_BitrateCtrlMode, m_MaxSmplRate, m_OpenH264EncdPt.m_IDRFrmIntvl, m_OpenH264EncdPt.m_Cmplxt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：初始化OpenH264编码器成功。" );
					}
					else
					{
						m_OpenH264EncdPt.m_Pt = null;
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：初始化OpenH264编码器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						break Out;
					}
					break;
				}
				case 2: //如果要使用系统自带H264编码器。
				{
					m_SystemH264EncdPt.m_Pt = new HeavenTao.Vdo.SystemH264Encd();
					if( m_SystemH264EncdPt.m_Pt.Init( m_MediaPocsThrdPt.m_LicnCodePt, m_DvcPt.m_Yu12SrcFrmScaleWidth, m_DvcPt.m_Yu12SrcFrmScaleHeight, m_SystemH264EncdPt.m_EncdBitrate, m_SystemH264EncdPt.m_BitrateCtrlMode, m_MaxSmplRate, m_SystemH264EncdPt.m_IDRFrmIntvlTimeSec, m_SystemH264EncdPt.m_Cmplxt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：初始化系统自带H264编码器成功。" );
					}
					else
					{
						m_SystemH264EncdPt.m_Pt = null;
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：初始化系统自带H264编码器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						break Out;
					}
					break;
				}
			}

			//初始化线程的临时变量。
			{
				m_ThrdPt.m_IsInitThrdTmpVar = 1; //设置已初始化线程的临时变量。
				m_ThrdPt.m_LastFrmTickMsec = 0; //初始化上一个帧的嘀嗒钟。
				m_ThrdPt.m_FrmTimeStepMsec = 1000.0 / m_MaxSmplRate; //初始化帧的时间步进。
				m_ThrdPt.m_Nv21SrcFrmPt = null; //初始化Nv21格式原始帧的指针。
				if( m_DvcPt.m_Yu12SrcFrmScaleLenByt >= m_DvcPt.m_Nv21SrcFrmLenByt ) //如果Yu12格式原始帧缩放后的长度大于等于Nv21格式原始帧的长度。
				{
					m_ThrdPt.m_TmpFrmSzByt = m_DvcPt.m_Yu12SrcFrmScaleLenByt; //初始化临时帧的大小。
				}
				else //如果Yu12格式原始帧缩放后的长度小于Nv21格式原始帧的长度。
				{
					m_ThrdPt.m_TmpFrmSzByt = m_DvcPt.m_Nv21SrcFrmLenByt; //初始化临时帧的大小。
				}
				m_ThrdPt.m_TmpFrm1Pt = new byte[( int )m_ThrdPt.m_TmpFrmSzByt]; //初始化临时帧的指针。
				m_ThrdPt.m_TmpFrm2Pt = new byte[( int )m_ThrdPt.m_TmpFrmSzByt]; //初始化临时帧的指针。
				m_ThrdPt.m_TmpFrmLenBytPt = new HTLong(); //初始化临时帧的长度。
				m_ThrdPt.m_FrmPt = null; //初始化视频输入帧的指针。
				m_ThrdPt.m_ElmTotal = 0; //初始化元素总数。
				m_ThrdPt.m_LastTickMsec = 0; //初始化上次的嘀嗒钟。
				m_ThrdPt.m_NowTickMsec = 0; //初始化本次的嘀嗒钟。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：初始化线程的临时变量成功。" );
			}

			//初始化帧容器。
			m_FrmCntnrPt = new ConcurrentLinkedQueue<>();
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：初始化帧容器成功。" );

			//初始化空闲帧容器。
			m_IdleFrmCntnrPt = new ConcurrentLinkedQueue<>();
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：初始化空闲帧容器成功。" );

			//初始化线程。
			{
				m_ThrdPt.m_ExitFlag = 0; //设置线程退出标记为0表示保持运行。
				m_ThrdPt.m_ThrdPt = new VdoInptThrd();
				m_ThrdPt.m_ThrdPt.start(); //启动线程。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：创建线程成功。" );
			}

			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
			{
				p_NowMsec = SystemClock.uptimeMillis(); //记录初始化结束的时间。
				Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：初始化耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			Dstoy();
		}
		return p_Rslt;
	}

	//销毁视频输入。
	void Dstoy()
	{
		long p_LastMsec = 0;
		long p_NowMsec = 0;

		if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastMsec = SystemClock.uptimeMillis(); //记录销毁开始的时间。

		//销毁线程。
		if( m_ThrdPt.m_ThrdPt != null )
		{
			m_ThrdPt.m_ExitFlag = 1; //请求线程退出。
			try
			{
				m_ThrdPt.m_ThrdPt.join(); //等待线程退出。
			}
			catch( InterruptedException ignored )
			{
			}
			m_ThrdPt.m_ThrdPt = null;
			m_ThrdPt.m_ExitFlag = 0;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：销毁线程成功。" );
		}

		//销毁线程的临时变量。
		if( m_ThrdPt.m_IsInitThrdTmpVar != 0 )
		{
			m_ThrdPt.m_IsInitThrdTmpVar = 0; //设置未初始化线程的临时变量。
			m_ThrdPt.m_LastFrmTickMsec = 0; //销毁上一个帧的嘀嗒钟。
			m_ThrdPt.m_FrmTimeStepMsec = 0; //销毁帧的时间步进。
			m_ThrdPt.m_Nv21SrcFrmPt = null; //销毁Nv21格式原始帧的指针。
			m_ThrdPt.m_TmpFrm1Pt = null; //初始化临时帧的指针。
			m_ThrdPt.m_TmpFrm2Pt = null; //初始化临时帧的指针。
			m_ThrdPt.m_TmpFrmLenBytPt = null; //初始化结果帧的长度。
			m_ThrdPt.m_TmpFrmSzByt = 0; //销毁结果帧的大小。
			m_ThrdPt.m_FrmPt = null; //销毁帧的指针。
			m_ThrdPt.m_ElmTotal = 0; //销毁元素总数。
			m_ThrdPt.m_LastTickMsec = 0; //销毁上次的嘀嗒钟。
			m_ThrdPt.m_NowTickMsec = 0; //销毁本次的嘀嗒钟。
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：销毁线程的临时变量成功。" );
		}

		//销毁设备。
		if( m_DvcPt.m_Pt != null )
		{
			m_DvcPt.m_Pt.setPreviewCallback( null ); //设置预览回调函数为空，防止出现java.lang.RuntimeException: Method called after release()异常。
			m_DvcPt.m_Pt.stopPreview(); //停止预览。
			m_DvcPt.m_Pt.release(); //销毁摄像头。
			m_DvcPt.m_Pt = null;
			if( m_DvcPt.m_Nv21SrcFrmCntnrPt != null )
			{
				m_DvcPt.m_Nv21SrcFrmCntnrPt.clear();
				m_DvcPt.m_Nv21SrcFrmCntnrPt = null;
			}
			m_DvcPt.m_PrvwClbkPt = null;
			if( m_DvcPt.m_PrvwSurfaceViewPt != null )
			{
				m_DvcPt.m_PrvwSurfaceViewPt.getHolder().removeCallback( m_DvcPt.m_PrvwSurfaceClbkPt ); //设置预览Surface视图的回调函数移除。
				m_DvcPt.m_PrvwSurfaceViewPt.SetBlack(); //设置预览Surface视图为黑屏。
			}
			m_DvcPt.m_PrvwSurfaceClbkPt = null;
			m_DvcPt.m_PrvwClbkBufPtPt = null;
			m_DvcPt.m_Yu12SrcFrmRotate = 0;
			m_DvcPt.m_Nv21SrcFrmWidth = 0;
			m_DvcPt.m_Nv21SrcFrmHeight = 0;
			m_DvcPt.m_Nv21SrcFrmIsCrop = 0;
			m_DvcPt.m_Nv21SrcFrmCropX = 0;
			m_DvcPt.m_Nv21SrcFrmCropY = 0;
			m_DvcPt.m_Nv21SrcFrmCropWidth = 0;
			m_DvcPt.m_Nv21SrcFrmCropHeight = 0;
			m_DvcPt.m_Yu12SrcFrmIsScale = 0;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：销毁设备成功。" );
		}

		//销毁空闲帧容器。
		if( m_IdleFrmCntnrPt != null )
		{
			m_IdleFrmCntnrPt.clear();
			m_IdleFrmCntnrPt = null;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：销毁空闲帧容器成功。" );
		}

		//销毁帧容器。
		if( m_FrmCntnrPt != null )
		{
			m_FrmCntnrPt.clear();
			m_FrmCntnrPt = null;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：销毁帧容器成功。" );
		}

		//销毁编码器。
		switch( m_UseWhatEncd )
		{
			case 0: //如果要使用Yu12原始数据。
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：销毁Yu12原始数据成功。" );
				break;
			}
			case 1: //如果要使用OpenH264编码器。
			{
				if( m_OpenH264EncdPt.m_Pt != null )
				{
					if( m_OpenH264EncdPt.m_Pt.Dstoy( null ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：销毁OpenH264编码器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：销毁OpenH264编码器失败。" );
					}
					m_OpenH264EncdPt.m_Pt = null;
				}
				break;
			}
			case 2: //如果要使用系统自带H264编码器。
			{
				if( m_SystemH264EncdPt.m_Pt != null )
				{
					if( m_SystemH264EncdPt.m_Pt.Dstoy( null ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：销毁系统自带H264编码器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：销毁系统自带H264编码器失败。" );
					}
					m_SystemH264EncdPt.m_Pt = null;
				}
				break;
			}
		}

		if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
		{
			p_NowMsec = SystemClock.uptimeMillis(); //记录销毁结束的时间。
			Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入：销毁耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
		}
	}

	//视频输入预览回调函数。
	public class VodInptPrvwClbk implements Camera.PreviewCallback
	{
		//读取一个视频输入帧的预览回调函数，本函数是在主线程中运行的。
		@Override public void onPreviewFrame( byte[] data, Camera camera )
		{
			m_DvcPt.m_Nv21SrcFrmCntnrPt.offer( data ); //放入本次视频输入帧到视频输入帧容器。
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：读取一个Nv21格式原始帧。" );
		}
	}

	//视频输入线程。
	public class VdoInptThrd extends Thread
	{
		public void run()
		{
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：开始准备视频输入。" );

			//视频输入循环开始。
			while( true )
			{
				OutPocs:
				{
					m_ThrdPt.m_ElmTotal = m_DvcPt.m_Nv21SrcFrmCntnrPt.size(); //获取Nv21格式原始帧容器的元素总数。
					if( m_ThrdPt.m_ElmTotal > 0 ) //如果Nv21格式原始帧容器中有帧了。
					{
						m_ThrdPt.m_Nv21SrcFrmPt = ( byte[] ) m_DvcPt.m_Nv21SrcFrmCntnrPt.poll(); //从Nv21格式原始帧容器中取出并删除第一个帧。
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：从Nv21格式原始帧容器中取出并删除第一个帧，Nv21格式原始帧容器元素总数：" + m_ThrdPt.m_ElmTotal + "。" );
					}
					else //如果Nv21格式原始帧容器中没有帧。
					{
						break OutPocs;
					}

					//丢弃采样频率过快的Nv21格式原始帧。
					m_ThrdPt.m_LastTickMsec = SystemClock.uptimeMillis();
					if( m_ThrdPt.m_LastFrmTickMsec != 0 ) //如果已经设置过上一个帧的嘀嗒钟。
					{
						if( m_ThrdPt.m_LastTickMsec - m_ThrdPt.m_LastFrmTickMsec >= m_ThrdPt.m_FrmTimeStepMsec )
						{
							m_ThrdPt.m_LastFrmTickMsec += m_ThrdPt.m_FrmTimeStepMsec;
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：采样频率过快，本次帧丢弃。" );
							break OutPocs;
						}
					}
					else //如果没有设置过上一个帧的嘀嗒钟。
					{
						m_ThrdPt.m_LastFrmTickMsec = m_ThrdPt.m_LastTickMsec;
					}

					//获取一个空闲帧。
					m_ThrdPt.m_ElmTotal = m_IdleFrmCntnrPt.size(); //获取空闲帧容器的元素总数。
					if( m_ThrdPt.m_ElmTotal > 0 ) //如果空闲帧容器中有帧。
					{
						m_ThrdPt.m_FrmPt = m_IdleFrmCntnrPt.poll(); //从空闲帧容器中取出并删除第一个帧。
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：从空闲帧容器中取出并删除第一个帧，空闲帧容器元素总数：" + m_ThrdPt.m_ElmTotal + "。" );
					}
					else //如果空闲帧容器中没有帧。
					{
						m_ThrdPt.m_ElmTotal = m_FrmCntnrPt.size(); //获取帧容器的元素总数。
						if( m_ThrdPt.m_ElmTotal <= 20 )
						{
							m_ThrdPt.m_FrmPt = new Frm(); //创建一个空闲帧。
							m_ThrdPt.m_FrmPt.m_Nv21SrcFrmPt = new byte[ ( int ) m_DvcPt.m_Nv21SrcFrmLenByt ];
							m_ThrdPt.m_FrmPt.m_Yu12RsltFrmPt = new byte[ ( int ) m_DvcPt.m_Yu12SrcFrmScaleLenByt ];
							if( m_UseWhatEncd != 0 )
							{
								m_ThrdPt.m_FrmPt.m_EncdRsltFrmPt = new byte[ ( int ) m_DvcPt.m_Yu12SrcFrmScaleLenByt ];
							}
							else
							{
								m_ThrdPt.m_FrmPt.m_EncdRsltFrmPt = null;
							}
							m_ThrdPt.m_FrmPt.m_EncdRsltFrmLenBytPt = new HTLong( 0 );
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：空闲帧容器中没有帧，创建一个空闲帧。" );
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：帧容器中帧数量为" + m_ThrdPt.m_ElmTotal + "已经超过上限20，不再创建一个空闲帧，本次帧丢弃。" );
							break OutPocs;
						}
					}

					{
						System.arraycopy( m_ThrdPt.m_Nv21SrcFrmPt, 0, m_ThrdPt.m_FrmPt.m_Nv21SrcFrmPt, 0, ( int )m_DvcPt.m_Nv21SrcFrmLenByt ); //复制Nv21格式原始帧。
						System.arraycopy( m_ThrdPt.m_Nv21SrcFrmPt, 0, m_ThrdPt.m_TmpFrm1Pt, 0, ( int )m_DvcPt.m_Nv21SrcFrmLenByt );  //将Nv21格式原始帧复制到临时帧，方便处理。
					}

					//裁剪Nv21格式原始帧。
					if( m_DvcPt.m_Nv21SrcFrmIsCrop != 0 )
					{
						if( LibYUV.PictrCrop( m_ThrdPt.m_TmpFrm1Pt, LibYUV.PICTR_FMT_BT601F8_NV21, m_DvcPt.m_Nv21SrcFrmWidth, m_DvcPt.m_Nv21SrcFrmHeight,
											  m_DvcPt.m_Nv21SrcFrmCropX, m_DvcPt.m_Nv21SrcFrmCropY, m_DvcPt.m_Nv21SrcFrmCropWidth, m_DvcPt.m_Nv21SrcFrmCropHeight,
											  m_ThrdPt.m_TmpFrm2Pt, m_ThrdPt.m_TmpFrmSzByt, m_ThrdPt.m_TmpFrmLenBytPt, null, null,
											  null ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：裁剪Nv21格式原始帧成功。" );
							byte p_SwapFrmPt[] = m_ThrdPt.m_TmpFrm1Pt; m_ThrdPt.m_TmpFrm1Pt = m_ThrdPt.m_TmpFrm2Pt; m_ThrdPt.m_TmpFrm2Pt = p_SwapFrmPt; //交换临时帧的指针。
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：裁剪Nv21格式原始帧失败，本次帧丢弃。" );
							break OutPocs;
						}
					}

					//Nv21格式原始帧旋转为Yu12格式原始帧。
					if( LibYUV.PictrRotate( m_ThrdPt.m_TmpFrm1Pt, LibYUV.PICTR_FMT_BT601F8_NV21, m_DvcPt.m_Nv21SrcFrmCropWidth, m_DvcPt.m_Nv21SrcFrmCropHeight,
											m_DvcPt.m_Yu12SrcFrmRotate,
											m_ThrdPt.m_TmpFrm2Pt, m_ThrdPt.m_TmpFrmSzByt, null, null,
											null ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：Nv21格式原始帧旋转为Yu12格式原始帧成功。" );
						byte p_SwapFrmPt[] = m_ThrdPt.m_TmpFrm1Pt; m_ThrdPt.m_TmpFrm1Pt = m_ThrdPt.m_TmpFrm2Pt; m_ThrdPt.m_TmpFrm2Pt = p_SwapFrmPt; //交换临时帧的指针。
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：Nv21格式原始帧旋转为Yu12格式原始帧失败，本次帧丢弃。" );
						break OutPocs;
					}

					//缩放Yu12格式原始帧。
					if( m_DvcPt.m_Yu12SrcFrmIsScale != 0 )
					{
						if( LibYUV.PictrScale( m_ThrdPt.m_TmpFrm1Pt, LibYUV.PICTR_FMT_BT601F8_Yu12_I420, m_DvcPt.m_Yu12SrcFrmRotateWidth, m_DvcPt.m_Yu12SrcFrmRotateHeight,
											   3,
											   m_ThrdPt.m_TmpFrm2Pt, m_ThrdPt.m_TmpFrmSzByt, m_ThrdPt.m_TmpFrmLenBytPt, m_DvcPt.m_Yu12SrcFrmScaleWidth, m_DvcPt.m_Yu12SrcFrmScaleHeight,
											   null ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：缩放Yu12格式原始帧成功。" );
							byte p_SwapFrmPt[] = m_ThrdPt.m_TmpFrm1Pt; m_ThrdPt.m_TmpFrm1Pt = m_ThrdPt.m_TmpFrm2Pt; m_ThrdPt.m_TmpFrm2Pt = p_SwapFrmPt; //交换临时帧的指针。
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：缩放Yu12格式原始帧失败，本次帧丢弃。" );
							break OutPocs;
						}
					}

					//将Yu12格式原始帧复制到Yu12格式结果帧。
					System.arraycopy( m_ThrdPt.m_TmpFrm1Pt, 0, m_ThrdPt.m_FrmPt.m_Yu12RsltFrmPt, 0, ( int )m_DvcPt.m_Yu12SrcFrmScaleLenByt );

					//判断设备是否黑屏。在视频输入处理完后再设置黑屏，这样可以保证视频输入处理器的连续性。
					if( m_DvcPt.m_IsBlack != 0 )
					{
						int p_TmpLenByt = m_DvcPt.m_Yu12SrcFrmScaleWidth * m_DvcPt.m_Yu12SrcFrmScaleHeight;
						Arrays.fill( m_ThrdPt.m_FrmPt.m_Yu12RsltFrmPt, 0, p_TmpLenByt, ( byte ) 0 );
						Arrays.fill( m_ThrdPt.m_FrmPt.m_Yu12RsltFrmPt, p_TmpLenByt, m_ThrdPt.m_FrmPt.m_Yu12RsltFrmPt.length, ( byte ) 128 );
					}

					//使用编码器。
					switch( m_UseWhatEncd )
					{
						case 0: //如果要使用Yu12原始数据。
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：使用Yu12原始数据。" );
							break;
						}
						case 1: //如果要使用OpenH264编码器。
						{
							if( m_OpenH264EncdPt.m_Pt.Pocs( m_ThrdPt.m_FrmPt.m_Yu12RsltFrmPt, m_DvcPt.m_Yu12SrcFrmScaleWidth, m_DvcPt.m_Yu12SrcFrmScaleHeight, m_ThrdPt.m_LastTickMsec,
															m_ThrdPt.m_FrmPt.m_EncdRsltFrmPt, m_ThrdPt.m_FrmPt.m_EncdRsltFrmPt.length, m_ThrdPt.m_FrmPt.m_EncdRsltFrmLenBytPt,
															null ) == 0 )
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：使用OpenH264编码器成功。H264格式结果帧的长度：" + m_ThrdPt.m_FrmPt.m_EncdRsltFrmLenBytPt.m_Val + "，时间戳：" + m_ThrdPt.m_LastTickMsec + "，类型：" + ( m_ThrdPt.m_FrmPt.m_EncdRsltFrmPt[4] & 0xff ) + "。" );
							}
							else
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：使用OpenH264编码器失败，本次帧丢弃。" );
								break OutPocs;
							}
							break;
						}
						case 2: //如果要使用系统自带H264编码器。
						{
							if( m_SystemH264EncdPt.m_Pt.Pocs( m_ThrdPt.m_FrmPt.m_Yu12RsltFrmPt, m_ThrdPt.m_LastTickMsec,
															  m_ThrdPt.m_FrmPt.m_EncdRsltFrmPt, ( long ) m_ThrdPt.m_FrmPt.m_EncdRsltFrmPt.length, m_ThrdPt.m_FrmPt.m_EncdRsltFrmLenBytPt,
															  1000 / m_MaxSmplRate * 2 / 3, null ) == 0 )
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：使用系统自带H264编码器成功。H264格式结果帧的长度：" + m_ThrdPt.m_FrmPt.m_EncdRsltFrmLenBytPt.m_Val + "，时间戳：" + m_ThrdPt.m_LastTickMsec + "，类型：" + ( m_ThrdPt.m_FrmPt.m_EncdRsltFrmPt[4] & 0xff ) + "。" );
							}
							else
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：使用系统自带H264编码器失败，本次帧丢弃。" );
								break OutPocs;
							}
							break;
						}
					}

					m_ThrdPt.m_FrmPt.m_TimeStampMsec = m_ThrdPt.m_LastTickMsec; //设置时间戳。

					//放入本次帧到帧容器。注意：从取出到放入过程中可以跳出，跳出后会重新放入到空闲帧容器。
					m_FrmCntnrPt.offer( m_ThrdPt.m_FrmPt );
					m_ThrdPt.m_FrmPt = null;

					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
					{
						m_ThrdPt.m_NowTickMsec = SystemClock.uptimeMillis();
						Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：本次帧处理完毕，耗时 " + ( m_ThrdPt.m_NowTickMsec - m_ThrdPt.m_LastTickMsec ) + " 毫秒。" );
					}
				}

				if( m_ThrdPt.m_FrmPt != null ) //如果获取的空闲帧没有放入到帧容器。
				{
					m_IdleFrmCntnrPt.offer( m_ThrdPt.m_FrmPt );
					m_ThrdPt.m_FrmPt = null;
				}

				//放入本次Nv21格式原始帧到设备。
				if( m_ThrdPt.m_Nv21SrcFrmPt != null )
				{
					m_DvcPt.m_Pt.addCallbackBuffer( m_ThrdPt.m_Nv21SrcFrmPt );
					m_ThrdPt.m_Nv21SrcFrmPt = null;
				}

				if( m_ThrdPt.m_ExitFlag == 1 ) //如果退出标记为请求退出。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：接收退出请求。" );
					break;
				}

				SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
			} //视频输入循环结束。

			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：本线程已退出。" );
		}
	}
}
