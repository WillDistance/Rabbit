package rabbitmq.conf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import com.thinkive.base.util.XMLHelper;

/**
 * 
 * @描述: 队列配置信息
 * @版权: Copyright (c) 2017
 * @公司: 思迪科技
 * @作者: 林冬莲
 * @版本: 1.0
 * @创建日期: 2017年2月15日
 * @创建时间: 上午11:25:36
 */
public class MqQueueConfig {
	private static Logger logger = Logger.getLogger(MqQueueConfig.class);
	private static String CONFIG_FILE_NAME = "MqQueueConfig.xml";
	private static Map<String, HashMap<String, String>> QUEUE_CONFIG = new HashMap<String, HashMap<String, String>>();

	/**
	 * 
	 * @描述：获取所有队列配置信息
	 * @作者：林冬莲
	 * @时间：2017年2月15日 下午2:36:20
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<String, HashMap<String, String>> getQueueInfos() {
		if(QUEUE_CONFIG==null || QUEUE_CONFIG.size()==0){
			try {
				Document document = XMLHelper.getDocument(MqQueueConfig.class,CONFIG_FILE_NAME);
				if (document == null) {
					logger.error("未找到配置文件[" + CONFIG_FILE_NAME + "]");
					return QUEUE_CONFIG;
				}
				Element rootElement = document.getRootElement();
				List<Element> queueList = rootElement.elements("queue");
				// 加载队列名配置
				for (Element e : queueList) {
					HashMap propMap = new HashMap();
					propMap.put("name", e.attributeValue("name"));
					propMap.put("value", e.attributeValue("value"));
					propMap.put("type", e.attributeValue("type"));
					propMap.put("exchange", e.attributeValue("exchange"));
					propMap.put("threadNum", e.attributeValue("threadNum"));
					QUEUE_CONFIG.put(e.attributeValue("name"), propMap);
				}
			} catch (Exception ex) {
				logger.error("", ex);
			}
		}		
		return QUEUE_CONFIG;
	}

	/**
	 * 
	 * @描述：根据队列name获取队列信息
	 * @作者：林冬莲
	 * @时间：2017年2月15日 下午2:36:40
	 * @param queueName
	 * @return
	 */
	public static HashMap<String, String> getQueueInfo(String queueName) {
		QUEUE_CONFIG = getQueueInfos();
		HashMap<String, String> queue=QUEUE_CONFIG.get(queueName);
		return queue;
	}

}
