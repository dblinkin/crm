package com.ftd.system;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ftd.servlet.Handler;
import com.ftd.util.dbclient.DBClient;

public class SysMgr {

	private DBClient dbClient;

	private Map<String, Handler> handlerMap = new HashMap<String, Handler>();

	private static Logger logger = LoggerFactory.getLogger(SysMgr.class);

	private SysMgr() {
	}

	private static SysMgr instance = new SysMgr();

	public static SysMgr getInstance() {
		return instance;
	}

	public DBClient getDbClient() {
		return dbClient;
	}

	public Handler getHandler(String cmd) {
		return handlerMap.get(cmd);
	}

	public boolean init(String filePath) {
		SAXReader reader = new SAXReader();
		Document document = null;

		try {
			document = reader.read(filePath);
		} catch (DocumentException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		if (document == null) {
			return false;
		}

		Element root = document.getRootElement();
		Element dbconfigElement = root.element("Database");
		HashMap<String, String> dbkv = getAttribute(dbconfigElement);

		String jdbcUrl = dbkv.get("JdbcUrl");
		String user = dbkv.get("User");
		String password = dbkv.get("Password");

		try {
			this.dbClient = new DBClient(jdbcUrl, user, password);
		} catch (ClassNotFoundException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			return false;
		} catch (SQLException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			return false;
		}

		List<Element> handlerElements = root.element("Handlers").elements();
		for (Element handlerElement : handlerElements) {
			HashMap<String, String> hkv = getAttribute(handlerElement);

			Handler h = null;
			try {
				h = (Handler) Class.forName(hkv.get("Class")).newInstance();
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				logger.error(ExceptionUtils.getStackTrace(e));
			}
			if (h != null)
				this.handlerMap.put(hkv.get("Req"), h);
		}

		return true;
	}

	public static HashMap<String, String> getAttribute(Element element) {
		HashMap<String, String> kv = new HashMap<String, String>();
		Iterator<Attribute> it = element.attributeIterator();
		while (it.hasNext()) {
			Attribute at = it.next();
			kv.put(at.getName(), at.getValue());
		}
		return kv;
	}

}
