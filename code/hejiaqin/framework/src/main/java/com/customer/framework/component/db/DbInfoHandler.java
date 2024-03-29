package com.customer.framework.component.db;

import com.customer.framework.utils.LogUtil;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Administrator on 2016/3/3.
 */
public class DbInfoHandler extends DefaultHandler {
    //    private String strXmlTag = null;

    private DatabaseInfo databaseInfo;

    private List<DatabaseInfo.Table> tableList;

    private List<DatabaseInfo.Field> fileList;

    private DatabaseInfo.Table table;

    private DatabaseInfo.Field field;

    /***/
    public void doParse(DatabaseInfo inDatabaseInfo, String inStrToParse)
            throws ParserConfigurationException, SAXException, IOException {

        LogUtil.i("DbInfoHandler", "DbInfoHandler : \n" + inStrToParse);
        this.databaseInfo = inDatabaseInfo;
        StringReader read = new StringReader(inStrToParse);
        InputSource source = new InputSource(read);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        xr.setContentHandler(this);
        xr.parse(source);
    }

    /**
     * [XML开始]<BR>
     * [功能详细描述]
     *
     * @throws SAXException XML异常
     * @see DefaultHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        tableList = new ArrayList<DatabaseInfo.Table>();
        databaseInfo.setListTable(tableList);
        super.startDocument();
    }

    /**
     * [XML结束]<BR>
     * [功能详细描述]
     *
     * @throws SAXException XML异常
     * @see DefaultHandler#endDocument()
     */
    public void endDocument() throws SAXException {
    }

    /**
     * [XML节点开始]<BR>
     * [功能详细描述]
     *
     * @param namespaceURI 名空间
     * @param localName    名
     * @param qName        名
     * @param atts         属性
     * @throws SAXException XML异常
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        //        strXmlTag = localName;
        if ("table".equals(localName)) {
            table = new DatabaseInfo.Table();
            tableList.add(table);
            fileList = new ArrayList<DatabaseInfo.Field>();
            table.setListFiled(fileList);
            table.setName(atts.getValue("name"));
        }
        if ("field".equals(localName)) {
            field = new DatabaseInfo.Field();
            field.setName(atts.getValue("name"));
            field.setType(atts.getValue("type"));
            field.setObligatory(atts.getValue("obligatory"));
            fileList.add(field);
        }
    }

    /**
     * [XML节点结束]<BR>
     * [功能详细描述]
     *
     * @param namespaceURI 名空间
     * @param localName    名
     * @param qName        名
     * @throws SAXException XML异常
     */
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        //        strXmlTag = null;
    }

    /**
     * [XML值]<BR>
     * [功能详细描述]
     *
     * @param ch     字符
     * @param start  起始
     * @param length 长度
     * @see DefaultHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) {
        //        if (strXmlTag != null) {
        //            String data = new String(ch, start, length);
        //            LogUtil.i("DbInfoHandler", "characters is: " + data);
        //
        //            if (strXmlTag.equals("version")) {
        //                this.databaseInfo.setVersion(data);
        //            } else if (strXmlTag.equals("database")) {
        //                this.databaseInfo.setName(data);
        //            } else if (strXmlTag.equals("table")) {
        //                this.table.setName(data);
        //            } else if (strXmlTag.equals("field")) {
        //                this.field.setName(data);
        //            } else if (strXmlTag.equals("type")) {
        //                this.field.setType(data);
        //            } else if (strXmlTag.equals("obligatory")) {
        //                this.field.setObligatory(data);
        //            }
        //        }
    }
}
