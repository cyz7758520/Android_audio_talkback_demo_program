package HeavenTao.Data;

//动态字符串类。
public class VarStr
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
    }

    public long m_VarStrPt; //存放动态字符串的指针。

    //构造函数。
    public VarStr()
    {
        m_VarStrPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Dstoy();
    }

    //创建并初始化动态字符串。
    public int Init()
    {
        if( m_VarStrPt == 0 )
        {
            HTLong p_VarStrPt = new HTLong();
            if( VarStrInit( p_VarStrPt ) == 0 )
            {
                m_VarStrPt = p_VarStrPt.m_Val;
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            return 0;
        }
    }

    //复制字符串到动态字符串。
    public int Cpy( String StrPt )
    {
        return VarStrCpy( m_VarStrPt, StrPt );
    }

    //插入字符串到动态字符串的指定位置。
    public int Ins( long Pos, String StrPt )
    {
        return VarStrIns( m_VarStrPt, Pos, StrPt );
    }

    //追加字符串到动态字符串的末尾。
    public int Cat( String StrPt )
    {
        return VarStrCat( m_VarStrPt, StrPt );
    }

    //清空动态字符串的字符串。
    public int SetEmpty()
    {
        return VarStrSetEmpty( m_VarStrPt );
    }

    //设置动态字符串的字符串内存大小。
    public int SetSz( long StrSz )
    {
        return VarStrSetSz( m_VarStrPt, StrSz );
    }

    //获取动态字符串的字符串内存大小。
    public int GetSz( HTLong StrSzPt )
    {
        return VarStrGetSz( m_VarStrPt, StrSzPt );
    }

    //获取动态字符串的字符串。
    public String GetStr()
    {
        return VarStrGetStr( m_VarStrPt );
    }

    //销毁动态字符串。
    public int Dstoy()
    {
        if( m_VarStrPt != 0 )
        {
            if( VarStrDstoy( m_VarStrPt ) == 0 )
            {
                m_VarStrPt = 0;
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            return 0;
        }
    }

    //创建并初始化动态字符串。
    public native int VarStrInit( HTLong VarStrPt );

    //复制字符串到动态字符串。
    public native int VarStrCpy( long VarStrPt, String StrPt );

    //插入字符串到动态字符串的指定位置。
    public native int VarStrIns( long VarStrPt, long Pos, String StrPt );

    //追加字符串到动态字符串的末尾。
    public native int VarStrCat( long VarStrPt, String StrPt );

    //清空动态字符串的字符串。
    public native int VarStrSetEmpty( long VarStrPt );

    //设置动态字符串的字符串内存大小。
    public native int VarStrSetSz( long VarStrPt, long StrSz );

    //获取动态字符串的字符串内存大小。
    public native int VarStrGetSz( long VarStrPt, HTLong StrSzPt );

    //获取动态字符串的字符串。
    public native String VarStrGetStr( long VarStrPt );

    //销毁动态字符串。
    public native int VarStrDstoy( long VarStrPt );
}