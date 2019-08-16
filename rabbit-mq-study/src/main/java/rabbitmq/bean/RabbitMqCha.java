package rabbitmq.bean;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import rabbitmq.conf.MqQueueConfig;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import exception.ChannelClosedException;

/**
 * 
 * @描述: 对rabbit mq 渠道的封装
 * @版权: Copyright (c) 2019 
 * @公司: 思迪科技 
 * @作者: 严磊
 * @版本: 1.0 
 * @创建日期: 2019年7月26日 
 * @创建时间: 上午9:12:34
 */
public class RabbitMqCha
{
    /**
     * 使用中
     */
    public static final String status_used    = "status_used";
    
    /**
     * 空闲，初始化的状态
     */
    public static final String status_idel    = "status_idel";
    
    /**
     * 空闲
     */
    public static final String status_invalid = "status_invalid";
    
    protected static Logger    logger         = Logger.getLogger(RabbitMqCha.class);
    
    protected Channel          cha;
    
    protected String           status         = RabbitMqCha.status_idel;
    
    //状态改变锁，通过对该对象加锁，来限制，不能异步（同时去调用）调用同一个对象的多个改变状态的方法。
    protected Object           statusLock     = new Object();
    
    //当前渠道最后被使用时的时间点（通过连接获取渠道时代表被使用）
    protected volatile long              IdleTime;
    
    /**
     * 当前渠道的状态
     */
    protected boolean                    isClose        = false;
    
    /**
     * 
     * @描述：获取渠道的最后使用时间点
     * @作者：严磊
     * @时间：2019年7月25日 下午7:51:40
     * @return
     */
    public long getIdleTime()
    {
        return IdleTime;
    }
    
    /**
     * 
     * @描述：修改渠道状态由使用中变为***状态
     * @作者：严磊
     * @时间：2019年7月25日 下午7:46:39
     * @return
     */
    public boolean toBeIdel()
    {
        synchronized (statusLock)
        {
            if ( RabbitMqCha.status_used.equals(this.status) )
            {
                this.status = RabbitMqCha.status_idel;
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
    /**
     * 
     * @描述：修改渠道状态由***变为使用中状态，如果渠道不为空闲，返回false
     * @作者：严磊
     * @时间：2019年7月25日 下午7:46:39
     * @return
     */
    public boolean toBeUsed()
    {
        synchronized (statusLock)
        {
            if ( RabbitMqCha.status_idel.equals(this.status) )
            {
                this.status = RabbitMqCha.status_used;
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
    /**
     * 
     * @描述：修改渠道状态由***变为空闲状态
     * @作者：严磊
     * @时间：2019年7月25日 下午7:46:39
     * @return
     */
    public boolean idelToInvalid()
    {
        synchronized (statusLock)
        {
            if ( RabbitMqCha.status_idel.equals(this.status) )
            {
                this.status = RabbitMqCha.status_invalid;
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
    /**
     * 
     * @描述：修改渠道状态由使用中变为空闲状态
     * @作者：严磊
     * @时间：2019年7月25日 下午7:46:39
     * @return
     */
    public boolean usedToInvalid()
    {
        synchronized (statusLock)
        {
            if ( RabbitMqCha.status_used.equals(this.status) )
            {
                this.status = RabbitMqCha.status_invalid;
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
    /**
     * 
     * 描述： 构造方法，RabbitMqCha是对Channel对象的封装
     * @param cha
     * @throws Exception
     */
    public RabbitMqCha(Channel cha) throws Exception
    {
        if ( cha == null )
        {
            throw new Exception("cha不能为空");
        }
        this.cha = cha;
    }
    
    /**
     * 
     * @描述：关闭渠道
     * @作者：严磊
     * @时间：2019年7月25日 下午6:45:03
     */
    public void closeCha()
    {
        try
        {
            if ( cha != null && cha.isOpen() )
            {
                cha.close();
                cha = null;
            }
        }
        catch (Exception e)
        {
            logger.error("关闭渠道出错", e);
        }
    }
    
    //把渠道还到渠道池
    /**
     * 
     * @描述：重设渠道的最后使用时间
     * @作者：严磊
     * @时间：2019年7月25日 下午7:59:01
     */
    public void reloadIdleTime()
    {
        IdleTime = new Date().getTime();
    }
    
    /**
     * 
     * @描述：检查渠道是否是打开状态
     * @作者：严磊
     * @时间：2019年7月26日 上午8:59:05
     * @return
     */
    public boolean isOpen()
    {
        return this.cha.isOpen();
    }
    
    //发布消息
    /**
     * 
     * @描述：发布消息到交换机
     * @作者：严磊
     * @时间：2019年7月26日 上午9:07:30
     * @param entity
     * @throws Exception
     */
    public void publish(MqRequestEntity entity) throws Exception
    {
        String queueName = entity.getQueueName();
        // 获取队列信息
        HashMap<String, String> queueInfo = MqQueueConfig.getQueueInfo(queueName);
        String queueValue = queueInfo.get("value");
        String type = queueInfo.get("type");
        String exchange = queueInfo.get("exchange");
        if ( !isOpen() )
        {
            throw new ChannelClosedException("渠道关闭了");
        }
        if ( type.equals("fanout") )
        {
            cha.basicPublish(exchange, "", MessageProperties.PERSISTENT_TEXT_PLAIN, JSONObject.toJSONString(entity)
                    .getBytes("GBK"));
        }
        else if ( type.equals("direct") )
        {
            cha.basicPublish(exchange, queueValue, null, JSONObject.toJSONString(entity).getBytes("GBK"));
        }
        else
        {
            cha.basicPublish("", queueValue, MessageProperties.PERSISTENT_TEXT_PLAIN, JSONObject.toJSONString(entity)
                    .getBytes("GBK"));
        }
    }
}
