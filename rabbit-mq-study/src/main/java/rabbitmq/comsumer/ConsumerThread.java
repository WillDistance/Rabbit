package rabbitmq.comsumer;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import rabbitmq.bean.MqRequestEntity;
import rabbitmq.conf.MqConsumerConfig;
import rabbitmq.conf.MqQueueConfig;
import rabbitmq.conf.MqServerConfig;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import com.thinkive.base.jdbc.DataRow;
import com.thinkive.base.util.StringHelper;
import com.thinkive.gateway.v2.result.Result;

/**
 * 
 * @����: �����߳�
 * @��Ȩ: Copyright (c) 2017
 * @��˾: ˼�ϿƼ�
 * @����: �ֶ���
 * @�汾: 1.0
 * @��������: 2017��2��15��
 * @����ʱ��: ����4:11:48
 */
@SuppressWarnings("deprecation")
public class ConsumerThread implements Runnable
{
    private Logger logger = Logger.getLogger(ConsumerThread.class);
    
    private String QUEUE_NAME;
    
    public ConsumerThread(String queueName)
    {
        this.QUEUE_NAME = queueName;
    }
    
    @SuppressWarnings({ "unused" })
    @Override
    public void run()
    {
        // ����queueName��ȡ������Ϣ
        Connection connection = MqServerConfig.getConnection();
        Channel channel = null;
        QueueingConsumer consumer = null;
        // ���ݶ�������ȡ������Ϣ
        HashMap<String, String> queueInfo = MqQueueConfig.getQueueInfo(QUEUE_NAME);
        String queueValue = queueInfo.get("value");
        String type = queueInfo.get("type");
        String exchange = queueInfo.get("exchange");
        try
        {
            channel = connection.createChannel();
            if ( type.equals("direct") )
            {
                channel.exchangeDeclare(exchange, type);
                if ( StringHelper.isBlank(queueValue) )
                {
                    queueValue = channel.queueDeclare().getQueue();
                }
                channel.queueDeclare(queueValue, true, false, false, null);
                channel.queueBind(queueValue, exchange, queueValue);
            }
            else if ( type.equals("fanout") )
            {
                channel.exchangeDeclare(exchange, type);
                if ( StringHelper.isBlank(queueValue) )
                {
                    queueValue = channel.queueDeclare().getQueue();
                }
                channel.queueDeclare(queueValue, true, false, false, null);
                channel.queueBind(queueValue, exchange, "");
            }
            else
            {
                channel.queueDeclare(queueValue, true, false, false, null);
            }
            
            /*
             * ����RabbitMQ�ͻ�ʹ��ÿ��Consumer��ͬһ��ʱ�����ദ��һ��Message��
             * ���仰˵���ڽ��յ���Consumer��ackǰ���������Ὣ�µ�Message�ַ�����
             */
            channel.basicQos(1);
            
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueValue, false, consumer);
            
            boolean isAck = true;
            while (true)
            {
                Result response = null;
                
                QueueingConsumer.Delivery delivery = null;
                BasicProperties props = null;
                try
                {
                    delivery = consumer.nextDelivery();//����һ�������ķ������Ӷ����ж�ȡ��Ϣ
                    props = delivery.getProperties();
                    String reqJson = new String(delivery.getBody(), "GBK");
                    response = invoke(reqJson);
                }
                catch (Exception e)
                {
                    logger.error("[x]���ѵ�mq��������", e);
                    //�о�������Բ��ó���������С����ǿ������棬�����ǽ������ߴ�����̵��쳣ȫ����׽���洢��redis�С� yl
                    //���⣬��finally�У�ִ���˷�����Ϣȷ�ϵ�ack����mqһ����ɾ��������Ϣ��
                    //��Ҫ���ԣ������catch��ִ��������к�����finally�з���ack���󣬻᲻�Ὣ������е���Ϣɾ����
                    //�����ͬһ����Ϣ����ʹ��basicReject������У���ʹ��basicAckȷ�Ͻ��գ���֮��ʹ��nextDelivery()��ȡ��Ϣ
                    //�ᱨ����com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED - unknown delivery tag 1, class-id=60, method-id=80)
                    /*
                     * channel.basicReject()������
                     * ��һ������deliveryTag��������ÿһ����Ϣ������һ��Ψһ��deliveryTag��deliveryTag��channel��Χ����Ψһ�� 
                     * �ڶ�������requeue����ʾ��δ���������Ϣ�����ֵΪtrue�������·���RabbitMQ�ķ��Ͷ��У����ֵΪfalse����֪ͨRabbitMQ����������Ϣ
                     * 
                     */
                    channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
                    isAck = false;
                }
                finally
                {
                    
                    if(response != null && response.getData("ConsumerParams") != null)
                    {
                        DataRow data = response.getData("ConsumerParams");
                        if("true".equals(data.getString(MqConsumer.is_requeued_rather_key)))
                        {
                            channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
                            isAck = false;
                        }  
                    }
                    
                    if(isAck)
                    {
                        /*  
                         * channel.basicAckfa()��������
                         * ��һ������deliveryTag��������ÿһ����Ϣ������һ��Ψһ��deliveryTag��(�κ�channel�Ϸ����ĵ�һ����Ϣ��deliveryTagΪ1���˺��ÿһ����Ϣ�����1)��deliveryTag��channel��Χ����Ψһ�� 
                         * �ڶ�������multiple������ȷ�ϱ�־�����ֵΪtrue����ִ������ȷ�ϣ���deliveryTag֮ǰ�յ�����Ϣȫ������ȷ��; ���ֵΪfalse����ֻ�Ե�ǰ�յ�����Ϣ����ȷ��
                         */
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }
                }
            }
        }
        catch (IOException e)
        {
            logger.error("������У�" + QUEUE_NAME, e);
        }
        finally
        {
            try
            {
                channel.close();
                connection.close();
            }
            catch (Exception ignore)
            {
                logger.error("rabbit mq���ӹرճ����쳣",ignore);
            }
        }
    }
    
    /**
     * 
     * @���������ݽ��յ���MqRequestEntity��ȡ������class·��������ִ��
     * @���ߣ��ֶ���
     * @ʱ�䣺2017��2��15�� ����5:43:46
     * @param reqJson
     * @return
     * @throws Exception
     */
    private Result invoke(String reqJson) throws Exception
    {
        MqRequestEntity entity = JSONObject.parseObject(reqJson, MqRequestEntity.class);
        Result result = null;
        HashMap<String, String> obj = MqConsumerConfig.getConsumerInfo(entity.getConsumerId());
        String clazz = (String) obj.get("class");
        MqConsumer consumer = (MqConsumer) ClassLoader.getSystemClassLoader().loadClass(clazz).newInstance();
        result = consumer.process(entity.getParam());
        return result;
    }
}
