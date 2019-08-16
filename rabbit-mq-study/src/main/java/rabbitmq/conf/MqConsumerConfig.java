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
 * @描述: MQ消费者配置信息
 * @版权: Copyright (c) 2017 
 * @公司: 思迪科技 
 * @作者: 林冬莲
 * @版本: 1.0 
 * @创建日期: 2017年2月15日 
 * @创建时间: 下午2:38:36
 */
public class MqConsumerConfig {
	private static Logger logger = Logger.getLogger(MqConsumerConfig.class);
	private static final String CONFIG_FILE_NAME = "MqConsumerConfig.xml";
	private static Map<String, HashMap<String, String>> CONSUMER_CONFIG = new HashMap<String, HashMap<String, String>>();
	/**
	 * 
	 * @描述：获取所有消费者配置信息
	 * @作者：林冬莲
	 * @时间：2017年2月15日 下午3:40:05
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<String, HashMap<String, String>> getConsumerInfos() {
		logger.info("[读取消费者信息开始]");
		if(CONSUMER_CONFIG==null || CONSUMER_CONFIG.size()==0){
			try {
				Document document = XMLHelper.getDocument(MqConsumerConfig.class,CONFIG_FILE_NAME);
				
				if (document == null) {
					logger.error("未找到配置文件[" + CONFIG_FILE_NAME + "]");
					return null;
				}
				Element rootElement = document.getRootElement();
				List<Element> consumerList = rootElement.elements("consumer");
				for (Element e : consumerList)
		        {
					HashMap propMap = new HashMap();
					String id=e.attributeValue("id");
					propMap.put("id", id);
					propMap.put("class", e.element("class").getText());
					CONSUMER_CONFIG.put(id, propMap);
		        }
			}catch (Exception ex) {
				logger.error("", ex);
			}
		}
		logger.info("[消费者信息]:");
		logger.info(CONSUMER_CONFIG);
		logger.info("[读取消费者信息结束]");
		return CONSUMER_CONFIG;
	}
	/**
	 * 
	 * @描述：根据消费者id获取消费者配置信息
	 * @作者：林冬莲
	 * @时间：2017年2月15日 下午3:43:22
	 * @param consumerId
	 * @return
	 */
	public static HashMap<String, String> getConsumerInfo(String consumerId) {
		CONSUMER_CONFIG=getConsumerInfos();
		return CONSUMER_CONFIG.get(consumerId);
	}
}
