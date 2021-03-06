/**
 * ZxFrame Java Library
 * https://github.com/zhouxuanGithub/zxframe
 *
 * Copyright (c) 2019 zhouxuan
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package zxframe.data.mgr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import zxframe.data.model.ZXData;
import zxframe.jpa.dao.MysqlTemplate;
import zxframe.jpa.ex.DataExpiredException;
import zxframe.jpa.util.SQLParsing;

/**
 * 使用ZXData，支持100亿的键值对数据快速存取（KEY,VALUE）。可用Hbase替代，本功能只为降低成本，使用mysql存储。
 * 用/demo/webproject/file/db-zxdata.sql创建存储过程，可自定义mark，创建需要的数据库
 * 支持将数据分片存储到多个DB服务，并每个都支持读写分离，支持乐观锁。
 * @author zx
 */
@Service
public class ZXDataTemplate{

	@Resource
	private MysqlTemplate mysqlTemplate;
	
	public void put(String key, String value) {
		put("zxdata",key,value);
	}
	public void put(String mark,String key, String value) {
		String tc = getTableCode(key);
		String sql="insert  into "+mark+tc+".data (`key`,`value`,`version`) values(?,?,0)";
		mysqlTemplate.executeBySql(mark+tc, sql, key,value);
	}
	/**
	 * 批量替换插入
	 * @param list
	 */
	public void putReplaceBatch(List<ZXData> list) {
		putReplaceBatch("zxdata",list);
	}
	/**
	 * 批量替换插入
	 * @param list
	 */
	public void putReplaceBatch(String mark,List<ZXData> list) {
		execPutBatch("replace into ",mark,list);
	}
	/**
	 * 批量替换插入
	 * @param list
	 */
	public void putIgnoreBatch(List<ZXData> list) {
		putIgnoreBatch("zxdata",list);
	}
	/**
	 * 批量替换插入
	 * @param list
	 */
	public void putIgnoreBatch(String mark,List<ZXData> list) {
		execPutBatch("insert ignore into ",mark,list);
	}
	private void execPutBatch(String fsql,String mark,List<ZXData> list) {
		//一条SQL版
		Map<String,StringBuffer> als=new HashMap<>(); 
		for (int i = 0; i < list.size(); i++) {
			ZXData zxData = list.get(i);
			String tc = getTableCode(zxData.getKey());
			String dsname=mark+tc;
			StringBuffer sb = als.get(dsname);
			if(sb==null) {
				sb=new StringBuffer();
				als.put(dsname, sb);
				sb.append(fsql+" "+dsname+".data (`key`,`value`,`version`) values ");
			}else {
				sb.append(",");
			}
			sb.append("("+SQLParsing.escapeSQLString(zxData.getKey())+","+SQLParsing.escapeSQLString(zxData.getValue())+",0)");
		}
		Iterator<String> iterator = als.keySet().iterator();
		while(iterator.hasNext()) {
			String dsname = iterator.next();
			StringBuffer sb = als.get(dsname);
			mysqlTemplate.executeBySql(dsname, sb.toString());
		}
	}
	public int update(String key, String value) {
		return update("zxdata",key,value);
	}
	public int update(String mark,String key, String value) {
		return update(mark,key,value,null);
	}
	public int update(String key, String value,Integer version) {
		return update("zxdata",key,value,version);
	}
	public int update(String mark,String key, String value,Integer version) {
		int rcount= 0;
		String tc = getTableCode(key);
		if(version==null) {
			String sql="update "+mark+tc+".data set `value`=? where `key`=?";
			rcount= (int)mysqlTemplate.executeBySql(mark+tc, sql, value,key);
		}else {
			String sql="update "+mark+tc+".data set `value`=?, `version`=`version`+1 where `key`=? and `version`=?";
			rcount= (int)mysqlTemplate.executeBySql(mark+tc, sql, value,key,version);
			if(rcount<1) {
				//版本控制出现问题
				throw new DataExpiredException("数据 version 已过期。");
			}
		}
		return rcount;
	}
	public void remove(String key) {
		remove("zxdata",key);
	}
	public void remove(String mark,String key) {
		String tc = getTableCode(key);
		String sql="delete from "+mark+tc+".data where `key`=?";
		mysqlTemplate.executeBySql(mark+tc, sql, key);
	}
	public String get(String key) {
		return get("zxdata",key);
	}
	public String get(String mark,String key) {
		ZXData zxData = getZXData(mark,key);
		if(zxData==null) {
			return null;
		}
		return zxData.getValue();
	}
	public ZXData getZXData(String key) {
		return getZXData("zxdata",key);
	}
	public ZXData getZXData(String mark,String key) {
		String tc = getTableCode(key);
		String sql="select * from "+mark+tc+".data where `key`=? ";
		List<ZXData> zxdatas = mysqlTemplate.getListBySql(mark+tc, ZXData.class, sql, key);
		if(zxdatas==null||zxdatas.size()==0) {
			return null;
		}
		return zxdatas.get(0);
	}
	public String getTableCode(String key) {
		String hashCode = String.valueOf(key.hashCode());
		int length = hashCode.length();
		return hashCode.substring(length-1,length);
	}
}