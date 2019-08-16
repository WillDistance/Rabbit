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
 * @����: ����������Ϣ
 * @��Ȩ: Copyright (c) 2017
 * @��˾: ˼�ϿƼ�
 * @����: �ֶ���
 * @�汾: 1.0
 * @��������: 2017��2��15��
 * @����ʱ��: ����11:25:36
 */
public class MqQueueConfig {
	private static Logger logger = Logger.getLogger(MqQueueConfig.class);
	private static String CONFIG_FILE_NAME = "MqQueueConfig.xml";
	private static Map<String, HashMap<String, String>> QUEUE_CONFIG = new HashMap<String, HashMap<String, String>>();

	/**
	 * 
	 * @��������ȡ���ж���������Ϣ
	 * @���ߣ��ֶ���
	 * @ʱ�䣺2017��2��15�� ����2:36:20
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<String, HashMap<String, String>> getQueueInfos() {
		if(QUEUE_CONFIG==null || QUEUE_CONFIG.size()==0){
			try {
				Document document = XMLHelper.getDocument(MqQueueConfig.class,CONFIG_FILE_NAME);
				if (document == null) {
					logger.error("δ�ҵ������ļ�[" + CONFIG_FILE_NAME + "]");
					return QUEUE_CONFIG;
				}
				Element rootElement = document.getRootElement();
				List<Element> queueList = rootElement.elements("queue");
				// ���ض���������
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
	 * @���������ݶ���name��ȡ������Ϣ
	 * @���ߣ��ֶ���
	 * @ʱ�䣺2017��2��15�� ����2:36:40
	 * @param queueName
	 * @return
	 */
	public static HashMap<String, String> getQueueInfo(String queueName) {
		QUEUE_CONFIG = getQueueInfos();
		HashMap<String, String> queue=QUEUE_CONFIG.get(queueName);
		return queue;
	}

}
