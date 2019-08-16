package rabbitmq.comsumer;

import org.apache.log4j.Logger;


/**
 * 
 * @描述: 消费者监听
 * @版权: Copyright (c) 2017 
 * @公司: 思迪科技 
 * @作者: 林冬莲
 * @版本: 1.0 
 * @创建日期: 2017年2月15日 
 * @创建时间: 下午4:09:28
 */
public class ConsumerListener {
	private static Logger logger = Logger.getLogger(ConsumerListener.class);
	public synchronized static void register(String queueName)
    {
		logger.info("[注册监听队列"+queueName+"]");		
		ConsumerThread consumer = new ConsumerThread(queueName);
        Thread thread = new Thread(consumer);
        thread.start();
    }
}
