package rabbitmq.conf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.thinkive.base.util.StringHelper;
import com.thinkive.base.util.XMLHelper;

/**
 * 
 * @����: ��ȡ��Ϣ���з������Ϣ
 * @��Ȩ: Copyright (c) 2017
 * @��˾: ˼�ϿƼ�
 * @����: �ֶ���
 * @�汾: 1.0
 * @��������: 2017��2��15��
 * @����ʱ��: ����2:02:18
 */
public class MqServerConfig {
	private static Logger logger = Logger.getLogger(MqServerConfig.class);

	private static final String CONFIG_FILE_NAME = "MqServerConfig.xml";

	private static Map<String, HashMap<String, String>> SERVER_CONFIG = new HashMap<String, HashMap<String, String>>();

	private static String DEFAULT_SERVER_ID = "default";

	private static ConnectionFactory factory = null;

	/**
	 * 
	 * @��������ȡ����MQ�����������Ϣ
	 * @���ߣ��ֶ���
	 * @ʱ�䣺2017��2��15�� ����2:02:48
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map<String, HashMap<String, String>> getServerInfos() {
		logger.info("[��ȡMQ�������Ϣ��ʼ]");
		if(SERVER_CONFIG==null || SERVER_CONFIG.size()==0){
			try {
				Document document = XMLHelper.getDocument(MqServerConfig.class,CONFIG_FILE_NAME);
				if (document == null) {
					logger.error("δ�ҵ������ļ�[" + CONFIG_FILE_NAME + "]");
					return null;
				}
				Element rootElement = document.getRootElement();

				String defaultServerId = rootElement.attributeValue("default", "");// Ĭ��ִ�еķ�������Ϣ
				String firstServerId = "";// ServerConfig.xml�����õĵ�һ���ڵ���Ϣ
				int i = 0;
				List serverList = rootElement.elements("server");

				for (Iterator serverIter = serverList.iterator(); serverIter
						.hasNext();) {
					Element serverElement = (Element) serverIter.next();
					String serverId = serverElement.attributeValue("id");
					if (StringHelper.isEmpty(serverId)) {
						continue;
					}
					if (i == 0) {
						firstServerId = serverId;
						i++;
					}
					// ����MQ����������
					HashMap propMap = new HashMap();
					List itemList = serverElement.elements("property");
					for (Iterator itemIter = itemList.iterator(); itemIter
							.hasNext();) {
						Element itemElement = (Element) itemIter.next();
						String itemName = itemElement.attributeValue("name");
						String value = itemElement.attributeValue("value");
						if (!StringHelper.isEmpty(itemName)) {
							propMap.put(itemName, value);
						}
					}
					SERVER_CONFIG.put(serverId, propMap);
				}

				if (StringHelper.isEmpty(defaultServerId)
						|| !SERVER_CONFIG.containsKey(defaultServerId)) {
					defaultServerId = firstServerId;
				}
				DEFAULT_SERVER_ID = defaultServerId;
			} catch (Exception ex) {
				logger.error("", ex);
			}
		}	
		logger.info("[MQ�������Ϣ]:");
		logger.info(SERVER_CONFIG);
		logger.info("[��ȡMQ�������Ϣ����]");
		return SERVER_CONFIG;
	}

	/**
	 * 
	 * @����������id��ȡMQ�����������Ϣ
	 * @���ߣ��ֶ���
	 * @ʱ�䣺2017��2��15�� ����2:03:17
	 * @param serverId
	 * @return
	 */
	public static HashMap<String, String> getServerInfo(String serverId) {
		SERVER_CONFIG = getServerInfos();
		return SERVER_CONFIG.get(serverId);
	}

	public static synchronized Connection getConnection(){
		if(factory==null){
			factory=new ConnectionFactory();
			HashMap<String, String> serverInfo=null;
			serverInfo=getServerInfo(DEFAULT_SERVER_ID);
			
			String url=serverInfo.get("url");
			int port=Integer.parseInt(serverInfo.get("port"));
			String username=serverInfo.get("username");
			String password=serverInfo.get("password");
			String virtualHost=serverInfo.get("virtualHost");
			
			factory.setHost(url);
			factory.setPort(port);
			factory.setUsername(username);
			factory.setPassword(password);
			factory.setVirtualHost(virtualHost);
			
			factory.setConnectionTimeout(0);
			factory.setAutomaticRecoveryEnabled(true);
			
		}
        Connection connection = null;
        try
        {
            connection = factory.newConnection();
        }
        catch (IOException e)
        {
            logger.error("",e);
        }
        catch (TimeoutException e)
        {
            logger.error("",e);
        }
		return connection;
	}
}
