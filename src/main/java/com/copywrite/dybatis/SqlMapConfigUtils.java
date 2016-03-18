package com.copywrite.dybatis;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SqlMapConfigUtils {

    @SuppressWarnings("unchecked")
    public static List<FileDesc> readSqlMapFileMapping(InputStream sqlMapConfig, String resouceProjectPath) {
        try {
            InputStream is = sqlMapConfig;
            InputStreamReader isr = new InputStreamReader(is, "utf-8");

            SAXBuilder builder = new SAXBuilder();
            builder.setEntityResolver(new NoOpEntityResolver());
            Document doc = builder.build(isr);
            Element root = doc.getRootElement();
            List list = root.getChildren("sqlMap");
            List<FileDesc> retList = new ArrayList<FileDesc>();
            for (Iterator it = list.listIterator(); it.hasNext(); ) {
                Element e = (Element) it.next();
                String loc = e.getAttribute("resource").getValue();
                URL url = SqlMapConfigUtils.class.getClassLoader().getResource(loc);
                File file = new File(url.getFile());
                long lastTm = file.lastModified();
                String name = file.getName();

                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet("http://127.0.0.1:63342" + resouceProjectPath + name);
                CloseableHttpResponse response = httpclient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream remoteIs = entity.getContent();
                String md5 = DigestUtils.md5Hex(remoteIs);
                FileDesc fd = new FileDesc(name, lastTm, md5);
                retList.add(fd);

                response.close();
            }
            return retList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<FileDesc>();
    }

    public static List<String> readSqlMap(FileDesc fd, String resouceProjectPath) {
        try {
            String path = fd.getPath();
            String name = new File(path).getName();
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet("http://127.0.0.1:63342" + resouceProjectPath + name);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            SAXBuilder builder = new SAXBuilder();
            builder.setEntityResolver(new NoOpEntityResolver());
            Document doc = builder.build(isr);
            Element root = doc.getRootElement();
            String namespace = null;
            if (root.getAttribute("namespace") != null) {
                namespace = root.getAttribute("namespace").getValue();
            }
            List list = root.getChildren();
            List<String> retList = new ArrayList<String>();
            for (Iterator it = list.listIterator(); it.hasNext(); ) {
                Element e = (Element) it.next();
                String tagName = e.getName();
                if ("statement".equals(tagName) || "insert".equals(tagName)
                        || "update".equals(tagName) || "delete".equals(tagName)
                        || "select".equals(tagName)
                        || "procedure".equals(tagName)) {
                    String id = e.getAttribute("id").getValue();
                    if (namespace != null) {
                        if (id.indexOf(".") == -1) {
                            id = namespace + "." + id;
                        }
                    }
                    retList.add(id);
                }
            }

            return retList;
        } catch (UnsupportedEncodingException e) {
        } catch (JDOMException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<String>();
    }
}
