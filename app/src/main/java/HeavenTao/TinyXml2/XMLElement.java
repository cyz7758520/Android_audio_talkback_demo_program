package HeavenTao.TinyXml2;

import HeavenTao.Data.*;

public class XMLElement
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "MediaFile" ); //加载libMediaFile.so。
    }

    public long m_XMLElementPt; //存放XMLElement指针。

    //构造函数。
    public XMLElement()
    {
        m_XMLElementPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        m_XMLElementPt = 0;
    }

    public int Name( HTString NamePt )
    {
        return XMLElementName( m_XMLElementPt, NamePt );
    }

    public int SetText( String TextPt )
    {
        return XMLElementSetText( m_XMLElementPt, TextPt );
    }

    public int GetText( HTString TextPt )
    {
        return XMLElementGetText( m_XMLElementPt, TextPt );
    }

    public int InsertEndChild( XMLElement XMLChildElementPt )
    {
        return XMLElementInsertEndChild( m_XMLElementPt, ( XMLChildElementPt != null ) ? XMLChildElementPt.m_XMLElementPt : 0 );
    }

    public int NextSiblingElement( XMLElement XMLNextSiblingElementPt )
    {
        return XMLElementNextSiblingElement( m_XMLElementPt, XMLNextSiblingElementPt );
    }

    public int FirstChildElement( XMLElement XMLFirstChildElementPt )
    {
        return XMLElementFirstChildElement( m_XMLElementPt, XMLFirstChildElementPt );
    }

    private native int XMLElementName( long XMLElementPt, HTString NamePt );

    private native int XMLElementSetText( long XMLElementPt, String TextPt );

    private native int XMLElementGetText( long XMLElementPt, HTString TextPt );

    private native int XMLElementInsertEndChild( long XMLElementPt, long XMLChildElementPt );

    private native int XMLElementNextSiblingElement( long XMLElementPt, XMLElement XMLNextSiblingElementPt );

    private native int XMLElementFirstChildElement( long XMLElementPt, XMLElement XMLFirstChildElementPt );
}