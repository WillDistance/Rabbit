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
 * @描述: 消费线程
 * @版权: Copyright (c) 2017
 * @公司: 思迪科技
 * @作者: 林冬莲
 * @版本: 1.0
 * @创建日期: 2017年2月15日
 * @创建时间: 下午4:11:48
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
        // 根据queueName获取队列信息
        Connection connection = MqServerConfig.getConnection();
        Channel channel = null;
        QueueingConsumer consumer = null;
        // 根据队列名获取队列信息
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
             * 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。
             * 换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它
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
                    delivery = consumer.nextDelivery();//这是一个阻塞的方法，从队列中读取消息
                    props = delivery.getProperties();
                    String reqJson = new String(delivery.getBody(), "GBK");
                    response = invoke(reqJson);
                }
                catch (Exception e)
                {
                    logger.error("[x]消费的mq出了问题", e);
                    //感觉这里可以采用出错重入队列。但是看到后面，发现是将消费者处理过程的异常全部捕捉，存储在redis中。 yl
                    //另外，在finally中，执行了发送消息确认的ack请求，mq一定会删除这条消息。
                    //需要测试，如果在catch中执行重入队列后，再在finally中发送ack请求，会不会将重入队列的消息删除。
                    //如果对同一个消息，既使用basicReject重入队列，又使用basicAck确认接收，在之后使用nextDelivery()读取消息
                    //会报出：com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED - unknown delivery tag 1, class-id=60, method-id=80)
                    /*
                     * channel.basicReject()方法：
                     * 第一个参数deliveryTag：发布的每一条消息都会获得一个唯一的deliveryTag，deliveryTag在channel范围内是唯一的 
                     * 第二个参数requeue：表示如何处理这条消息，如果值为true，则重新放入RabbitMQ的发送队列，如果值为false，则通知RabbitMQ销毁这条消息
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
                         * channel.basicAckfa()方法参数
                         * 第一个参数deliveryTag：发布的每一条消息都会获得一个唯一的deliveryTag，(任何channel上发布的第一条消息的deliveryTag为1，此后的每一条消息都会加1)，deliveryTag在channel范围内是唯一的 
                         * 第二个参数multiple：批量确认标志。如果值为true，则执行批量确认，此deliveryTag之前收到的消息全部进行确认; 如果值为false，则只对当前收到的消息进行确认
                         */
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }
                }
            }
        }
        catch (IOException e)
        {
            logger.error("报错队列：" + QUEUE_NAME, e);
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
                logger.error("rabbit mq连接关闭出现异常",ignore);
            }
        }
    }
    
    /**
     * 
     * @描述：根据接收到的MqRequestEntity获取消费者class路径，反射执行
     * @作者：林冬莲
     * @时间：2017年2月15日 下午5:43:46
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
