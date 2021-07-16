package HeavenTao.Video;

import android.graphics.Bitmap;
import android.view.Surface;

import HeavenTao.Data.*;

//图片处理类。
public class LibYUV
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "LibYUV" ); //加载libLibYUV.so。
    }

    //构造函数。
    public LibYUV()
    {

    }

    //析构函数。
    public void finalize()
    {

    }

    //图片格式。
    public static final int PICTR_FMT_BT601F8_NV21 = 0;      //BT.601标准，Full Range，8位，内存排列：'YYYYYYYY……','VUVUVUVU……'
    public static final int PICTR_FMT_BT601F8_YV12 = 1;      //BT.601标准，Full Range，8位，内存排列：'YYYYYYYY……','VVVV……','UUUU……'
    public static final int PICTR_FMT_BT601F8_YU12 = 2;      //BT.601标准，Full Range，8位，内存排列：'YYYYYYYY……','UUUU……','VVVV……'
    public static final int PICTR_FMT_BT601F8_I420 = 3;      //BT.601标准，Full Range，8位，内存排列：'YYYYYYYY……','UUUU……','VVVV……'
    public static final int PICTR_FMT_SRGBF8_RGB555 = 4;     //sRGB标准，Full Range，8位，内存排列：'R','G','B'
    public static final int PICTR_FMT_SRGBF8_BGR555 = 5;     //sRGB标准，Full Range，8位，内存排列：'B','G','R'
    public static final int PICTR_FMT_SRGBF8_RGB565 = 6;     //sRGB标准，Full Range，8位，内存排列：'R','G','B'
    public static final int PICTR_FMT_SRGBF8_BGR565 = 7;     //sRGB标准，Full Range，8位，内存排列：'B','G','R'
    public static final int PICTR_FMT_SRGBF8_RGB888 = 8;     //sRGB标准，Full Range，8位，内存排列：'R','G','B'
    public static final int PICTR_FMT_SRGBF8_BGR888 = 9;     //sRGB标准，Full Range，8位，内存排列：'B','G','R'
    public static final int PICTR_FMT_SRGBF8_ARGB8888 = 10;  //sRGB标准，Full Range，8位，内存排列：'A','R','G','B'
    public static final int PICTR_FMT_SRGBF8_ABGR8888 = 11;  //sRGB标准，Full Range，8位，内存排列：'A','B','G','R'
    public static final int PICTR_FMT_SRGBF8_RGBA8888 = 12;  //sRGB标准，Full Range，8位，内存排列：'R','G','B','A'
    public static final int PICTR_FMT_SRGBF8_BGRA8888 = 13;  //sRGB标准，Full Range，8位，内存排列：'B','G','R','A'

    //旋转角度。
    public static final int ROTATE_DEGREE_0 = 0;      //0度。
    public static final int ROTATE_DEGREE_90 = 90;    //90度。
    public static final int ROTATE_DEGREE_180 = 180;  //180度。
    public static final int ROTATE_DEGREE_270 = 270;  //270度。

    public native static int PictrCrop( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
                                        int CropX, int CropY, int CropWidth, int CropHeight,
                                        byte DstPictrPt[], long DstPictrSz, HTLong DstPictrLenPt, HTInt DstPictrWidthPt, HTInt DstPictrHeightPt,
                                        VarStr ErrInfoVarStrPt );

    public native static int PictrRotate( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
                                          int RotateDegree,
                                          byte DstPictrPt[], long DstPictrSz, HTInt DstPictrWidthPt, HTInt DstPictrHeightPt,
                                          VarStr ErrInfoVarStrPt );

    public native static int PictrScale( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
                                         int Quality,
                                         byte DstPictrPt[], long DstPictrSz, HTLong DstPictrLenPt, int DstPictrWidth, int DstPictrHeight,
                                         VarStr ErrInfoVarStrPt );

    public native static int PictrFmtCnvrt( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
                                            byte DstPictrPt[], long DstPictrSz, HTLong DstPictrLenPt, int DstPictrFmt,
                                            VarStr ErrInfoVarStrPt );

    public native static int PictrDrawToBitmap( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
                                                Bitmap DstBitmapPt,
                                                VarStr ErrInfoVarStrPt );

    public native static int PictrDrawToSurface( byte SrcPictrPt[], long SrcPictrStart, int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
                                                 Surface DstSurfacePt,
                                                 VarStr ErrInfoVarStrPt );
}