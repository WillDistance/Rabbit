package rabbitmq.comsumer;

import org.apache.log4j.Logger;


/**
 * 
 * @����: �����߼���
 * @��Ȩ: Copyright (c) 2017 
 * @��˾: ˼�ϿƼ� 
 * @����: �ֶ���
 * @�汾: 1.0 
 * @��������: 2017��2��15�� 
 * @����ʱ��: ����4:09:28
 */
public class ConsumerListener {
	private static Logger logger = Logger.getLogger(ConsumerListener.class);
	public synchronized static void register(String queueName)
    {
		logger.info("[ע���������"+queueName+"]");		
		ConsumerThread consumer = new ConsumerThread(queueName);
        Thread thread = new Thread(consumer);
        thread.start();
    }
}
