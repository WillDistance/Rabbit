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
 * @����: MQ������������Ϣ
 * @��Ȩ: Copyright (c) 2017 
 * @��˾: ˼�ϿƼ� 
 * @����: �ֶ���
 * @�汾: 1.0 
 * @��������: 2017��2��15�� 
 * @����ʱ��: ����2:38:36
 */
public class MqConsumerConfig {
	private static Logger logger = Logger.getLogger(MqConsumerConfig.class);
	private static final String CONFIG_FILE_NAME = "MqConsumerConfig.xml";
	private static Map<String, HashMap<String, String>> CONSUMER_CONFIG = new HashMap<String, HashMap<String, String>>();
	/**
	 * 
	 * @��������ȡ����������������Ϣ
	 * @���ߣ��ֶ���
	 * @ʱ�䣺2017��2��15�� ����3:40:05
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<String, HashMap<String, String>> getConsumerInfos() {
		logger.info("[��ȡ��������Ϣ��ʼ]");
		if(CONSUMER_CONFIG==null || CONSUMER_CONFIG.size()==0){
			try {
				Document document = XMLHelper.getDocument(MqConsumerConfig.class,CONFIG_FILE_NAME);
				
				if (document == null) {
					logger.error("δ�ҵ������ļ�[" + CONFIG_FILE_NAME + "]");
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
		logger.info("[��������Ϣ]:");
		logger.info(CONSUMER_CONFIG);
		logger.info("[��ȡ��������Ϣ����]");
		return CONSUMER_CONFIG;
	}
	/**
	 * 
	 * @����������������id��ȡ������������Ϣ
	 * @���ߣ��ֶ���
	 * @ʱ�䣺2017��2��15�� ����3:43:22
	 * @param consumerId
	 * @return
	 */
	public static HashMap<String, String> getConsumerInfo(String consumerId) {
		CONSUMER_CONFIG=getConsumerInfos();
		return CONSUMER_CONFIG.get(consumerId);
	}
}
