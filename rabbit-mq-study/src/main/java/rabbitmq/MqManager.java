package rabbitmq;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import rabbitmq.comsumer.ConsumerListener;
import rabbitmq.conf.MqQueueConfig;

/**
 * 
 * @描述: MQ队列管理
 * @版权: Copyright (c) 2017 
 * @公司: 思迪科技 
 * @作者: 林冬莲
 * @版本: 1.0 
 * @创建日期: 2017年2月16日 
 * @创建时间: 下午5:24:48
 */
public class MqManager {
	private static Logger logger = Logger.getLogger(MqManager.class);

	//启动的时候，调用监听者，会创建队列并绑定到交换机，避免出现，生产者将消息发布到交换机，没有匹配的队列，消息被丢弃
	public static void start() {
		logger.info("[MQ队列开始启动]");
		// 获取队列名和监听的线程数
		Map<String, HashMap<String, String>> queueList =MqQueueConfig.getQueueInfos();
		for (Entry<String, HashMap<String, String>> entry : queueList
				.entrySet()) {
			HashMap<String, String> queueInfo = entry.getValue();
			String queueName = queueInfo.get("name");
			int threadNum = Integer.parseInt(queueInfo.get("threadNum").toString());
			for (int i = 0; i < threadNum; i++) {
				// 启动MQ监听
			    ConsumerListener.register(queueName);
			}
		}
		logger.info("[MQ队列启动结束]");
	}

	public static void stop() {

	}
}
