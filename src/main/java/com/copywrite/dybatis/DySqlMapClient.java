package com.copywrite.dybatis;


import com.ibatis.common.xml.NodeletException;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.engine.builder.xml.SqlMapConfigParser;
import com.ibatis.sqlmap.engine.builder.xml.SqlMapParser;
import com.ibatis.sqlmap.engine.builder.xml.XmlParserState;
import com.ibatis.sqlmap.engine.config.SqlMapConfiguration;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * copied and wrote from someone's gitHub
 * Repo: https://github.com/copywrite/dybatis
 */

public class DySqlMapClient extends SqlMapClientImpl {
	private SqlMapClient client = null;
	private SqlMapParser mapParser = null;
	private SqlMapConfigParser configParser = null;
	private Resource[] configLocations = null;
	private List <FileDesc> descList = null;
	private DySqlMapExecutorDelegate myDelegate = null;
	private String resouceProjectPath;
	
	private Map<String, FileDesc> statementMap = null;
	private Map<FileDesc, List<String>> fileStatementMap = null;
	
	private DySqlMapClient(SqlMapExecutorDelegate delegate) {
		super(delegate);
	}

	public DySqlMapClient(SqlMapClient client,
						  SqlMapParser mapParser,
						  SqlMapConfigParser configParser,
						  Resource[] configLocations,
						  String resouceProjectPath) {
		super(new DySqlMapExecutorDelegate(((ExtendedSqlMapClient)client).getDelegate(), resouceProjectPath));
		this.myDelegate = (DySqlMapExecutorDelegate) this.delegate;
		this.myDelegate.setClient(this);
		this.client = client;
		this.mapParser = mapParser;
		this.configParser = configParser;
		this.configLocations = configLocations;
		this.resouceProjectPath = resouceProjectPath;
		initFileDescList();
		initStatementMap();
		initState();
	}
	
	public FileDesc getFileDescUseId(String id) {
		FileDesc fd = statementMap.get(id);
		return fd;
	}
	public void refreshFileDesc(String id, long tm, String md5) {
		FileDesc fd = getFileDescUseId(id);
		fd.setTm(tm);
		fd.setMd5(md5);
	}
	public void reParseSqlMap(InputStream is) {
		try {
			this.mapParser.parse(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NodeletException e) {
			e.printStackTrace();
		}
	}
	
	public void initState() {
		try {
			Field f = this.configParser.getClass().getDeclaredField("state");
			f.setAccessible(true);
			XmlParserState state = (XmlParserState) f.get(this.configParser);
			Field configFiled = state.getClass().getDeclaredField("config");
			configFiled.setAccessible(true);
			SqlMapConfiguration impl = (SqlMapConfiguration) configFiled.get(state);
			Field clientField = impl.getClass().getDeclaredField("client");
			clientField.setAccessible(true);
			clientField.set(impl, this);
			Field delegateField = impl.getClass().getDeclaredField("delegate");
			delegateField.setAccessible(true);
			delegateField.set(impl, this.myDelegate);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initFileDescList() {
		descList = new ArrayList<FileDesc>();
		for ( int i = 0; i < configLocations.length; i++) {
			try {
				InputStream path = configLocations[i].getInputStream();
				descList.addAll(SqlMapConfigUtils.readSqlMapFileMapping(path, resouceProjectPath));
			} catch (IOException e) {}
		}
	}
	
	private void initStatementMap() {
		this.statementMap = new HashMap<String, FileDesc>();
		this.fileStatementMap = new HashMap<FileDesc, List<String>>();
		for ( Iterator <FileDesc> it = descList.listIterator(); it.hasNext(); ) {
			FileDesc desc = it.next();
			List <String> list = SqlMapConfigUtils.readSqlMap(desc, resouceProjectPath);
			for ( Iterator <String> i = list.listIterator(); i.hasNext(); ) {
				String id = i.next();
				statementMap.put(id, desc);
			}
			this.fileStatementMap.put(desc, list);
		}
	}
}
