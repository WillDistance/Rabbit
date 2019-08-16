package rabbitmq.bean;

import java.util.Map;

/**
 * 
 * @����: MQ�������ʵ��
 * @��Ȩ: Copyright (c) 2017
 * @��˾: ˼�ϿƼ�
 * @����: �ֶ���
 * @�汾: 1.0
 * @��������: 2017��2��15��
 * @����ʱ��: ����10:42:33
 */
public class MqRequestEntity {
	private String consumerId;// ������
	private String queueName;// ����
	private Map<String, String> param;// �����߲���

	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Map<String, String> getParam() {
		return param;
	}

	public void setParam(Map<String, String> param) {
		this.param = param;
	}

}
