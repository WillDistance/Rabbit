package rabbitmq;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import rabbitmq.comsumer.ConsumerListener;
import rabbitmq.conf.MqQueueConfig;

/**
 * 
 * @����: MQ���й���
 * @��Ȩ: Copyright (c) 2017 
 * @��˾: ˼�ϿƼ� 
 * @����: �ֶ���
 * @�汾: 1.0 
 * @��������: 2017��2��16�� 
 * @����ʱ��: ����5:24:48
 */
public class MqManager {
	private static Logger logger = Logger.getLogger(MqManager.class);

	//������ʱ�򣬵��ü����ߣ��ᴴ�����в��󶨵���������������֣������߽���Ϣ��������������û��ƥ��Ķ��У���Ϣ������
	public static void start() {
		logger.info("[MQ���п�ʼ����]");
		// ��ȡ�������ͼ������߳���
		Map<String, HashMap<String, String>> queueList =MqQueueConfig.getQueueInfos();
		for (Entry<String, HashMap<String, String>> entry : queueList
				.entrySet()) {
			HashMap<String, String> queueInfo = entry.getValue();
			String queueName = queueInfo.get("name");
			int threadNum = Integer.parseInt(queueInfo.get("threadNum").toString());
			for (int i = 0; i < threadNum; i++) {
				// ����MQ����
			    ConsumerListener.register(queueName);
			}
		}
		logger.info("[MQ������������]");
	}

	public static void stop() {

	}
}
