package rabbitmq.bean;

import java.util.Map;

/**
 * 
 * @描述: MQ请求参数实体
 * @版权: Copyright (c) 2017
 * @公司: 思迪科技
 * @作者: 林冬莲
 * @版本: 1.0
 * @创建日期: 2017年2月15日
 * @创建时间: 上午10:42:33
 */
public class MqRequestEntity {
	private String consumerId;// 消费者
	private String queueName;// 队列
	private Map<String, String> param;// 消费者参数

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
