package com.copywrite.dybatis;


import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;
import com.ibatis.sqlmap.engine.scope.SessionScope;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class DySqlMapExecutorDelegate extends SqlMapExecutorDelegate {
	private SqlMapExecutorDelegate delegate = null;
	private DySqlMapClient client = null;
	private String resouceProjectPath;
	
	public void setClient(DySqlMapClient client) {
		this.client = client;
	}

	public DySqlMapExecutorDelegate(SqlMapExecutorDelegate delegate, String resouceProjectPath) {
		super();
		this.delegate = delegate;
		this.resouceProjectPath = resouceProjectPath;
	}

	protected void checkAndRefreshSqlMap(String id) {
        try {
            FileDesc fd = client.getFileDescUseId(id);
            if ( fd == null ) {
                return;
            }
            String path = fd.getPath();
            File file = new File(path);
            String name = file.getName();

            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet("http://127.0.0.1:63342" + resouceProjectPath + name);
            CloseableHttpResponse response = httpclient.execute(httpGet);

            try {
                HttpEntity entity = response.getEntity();
                InputStream remoteIs = entity.getContent();
                byte[] bytes = IOUtils.toByteArray(remoteIs);
                String md5 = DigestUtils.md5Hex(bytes);
                ByteArrayInputStream is = new ByteArrayInputStream(bytes);

                if (!fd.getMd5().equals(md5)) {
                    try {
                        client.reParseSqlMap(is);
                        client.refreshFileDesc(id, file.lastModified(), md5);

                        System.out.println(file.getName() + " has been reloaded");
                    } catch (Exception e) {
                        System.out.println(file.getName() + " reload failed! " + e.getMessage());
                    }
                }

            }catch (Exception e) {

            }finally {
                response.close();
            }
        }catch (Exception e){

        }
	}
	
	@Override
	public void addMappedStatement(MappedStatement ms) {		
		try {
			this.delegate.getMappedStatement(ms.getId());
			Field f = this.delegate.getClass().getDeclaredField("mappedStatements");
			f.setAccessible(true);
			Map map = (Map)f.get(this.delegate);
			if ( map.containsKey(ms.getId())) {
				map.remove(ms.getId());
			}
		} catch (SqlMapException ignore) {
			
		} catch (Exception e) {}
		this.delegate.addMappedStatement(ms);
	}

	@Override
	public List queryForList(SessionScope sessionScope, String id,
			Object paramObject, int skip, int max) throws SQLException {
		checkAndRefreshSqlMap(id);
		return this.delegate.queryForList(sessionScope, id, paramObject, skip, max);
	}

	@Override
	public List queryForList(SessionScope sessionScope, String id,
			Object paramObject) throws SQLException {
		checkAndRefreshSqlMap(id);
		return this.delegate.queryForList(sessionScope, id, paramObject);
	}

	@Override
	public Map queryForMap(SessionScope sessionScope, String id,
			Object paramObject, String keyProp, String valueProp)
			throws SQLException {
		checkAndRefreshSqlMap(id);
		return this.delegate.queryForMap(sessionScope, id, paramObject, keyProp, valueProp);
	}

	@Override
	public Map queryForMap(SessionScope sessionScope, String id,
			Object paramObject, String keyProp) throws SQLException {
		checkAndRefreshSqlMap(id);
		return this.delegate.queryForMap(sessionScope, id, paramObject, keyProp);
	}

	@Override
	public Object queryForObject(SessionScope sessionScope, String id,
			Object paramObject, Object resultObject) throws SQLException {
		checkAndRefreshSqlMap(id);
		return this.delegate.queryForObject(sessionScope, id, paramObject, resultObject);
	}

	@Override
	public Object queryForObject(SessionScope sessionScope, String id,
			Object paramObject) throws SQLException {
		checkAndRefreshSqlMap(id);
		return this.delegate.queryForObject(sessionScope, id, paramObject);
	}

	@Override
	public PaginatedList queryForPaginatedList(SessionScope sessionScope,
			String id, Object paramObject, int pageSize) throws SQLException {
		checkAndRefreshSqlMap(id);
		return this.delegate.queryForPaginatedList(sessionScope, id, paramObject, pageSize);
	}

	@Override
	public void queryWithRowHandler(SessionScope sessionScope, String id,
			Object paramObject, RowHandler rowHandler) throws SQLException {
		checkAndRefreshSqlMap(id);
		this.delegate.queryWithRowHandler(sessionScope, id, paramObject, rowHandler);
	}

	@Override
	public int update(SessionScope sessionScope, String id, Object param)
			throws SQLException {
		checkAndRefreshSqlMap(id);
		return this.delegate.update(sessionScope, id, param);
	}

    @Override
    public Object insert(SessionScope sessionScope, String id, Object param)
            throws SQLException {
        checkAndRefreshSqlMap(id);
        return this.delegate.insert(sessionScope, id, param);
    }
}
