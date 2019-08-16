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
 * @����: ��rabbit mq �����ķ�װ
 * @��Ȩ: Copyright (c) 2019 
 * @��˾: ˼�ϿƼ� 
 * @����: ����
 * @�汾: 1.0 
 * @��������: 2019��7��26�� 
 * @����ʱ��: ����9:12:34
 */
public class RabbitMqCha
{
    /**
     * ʹ����
     */
    public static final String status_used    = "status_used";
    
    /**
     * ���У���ʼ����״̬
     */
    public static final String status_idel    = "status_idel";
    
    /**
     * ����
     */
    public static final String status_invalid = "status_invalid";
    
    protected static Logger    logger         = Logger.getLogger(RabbitMqCha.class);
    
    protected Channel          cha;
    
    protected String           status         = RabbitMqCha.status_idel;
    
    //״̬�ı�����ͨ���Ըö�������������ƣ������첽��ͬʱȥ���ã�����ͬһ������Ķ���ı�״̬�ķ�����
    protected Object           statusLock     = new Object();
    
    //��ǰ�������ʹ��ʱ��ʱ��㣨ͨ�����ӻ�ȡ����ʱ����ʹ�ã�
    protected volatile long              IdleTime;
    
    /**
     * ��ǰ������״̬
     */
    protected boolean                    isClose        = false;
    
    /**
     * 
     * @��������ȡ���������ʹ��ʱ���
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����7:51:40
     * @return
     */
    public long getIdleTime()
    {
        return IdleTime;
    }
    
    /**
     * 
     * @�������޸�����״̬��ʹ���б�Ϊ***״̬
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����7:46:39
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
     * @�������޸�����״̬��***��Ϊʹ����״̬�����������Ϊ���У�����false
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����7:46:39
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
     * @�������޸�����״̬��***��Ϊ����״̬
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����7:46:39
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
     * @�������޸�����״̬��ʹ���б�Ϊ����״̬
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����7:46:39
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
     * ������ ���췽����RabbitMqCha�Ƕ�Channel����ķ�װ
     * @param cha
     * @throws Exception
     */
    public RabbitMqCha(Channel cha) throws Exception
    {
        if ( cha == null )
        {
            throw new Exception("cha����Ϊ��");
        }
        this.cha = cha;
    }
    
    /**
     * 
     * @�������ر�����
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����6:45:03
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
            logger.error("�ر���������", e);
        }
    }
    
    //����������������
    /**
     * 
     * @�������������������ʹ��ʱ��
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����7:59:01
     */
    public void reloadIdleTime()
    {
        IdleTime = new Date().getTime();
    }
    
    /**
     * 
     * @��������������Ƿ��Ǵ�״̬
     * @���ߣ�����
     * @ʱ�䣺2019��7��26�� ����8:59:05
     * @return
     */
    public boolean isOpen()
    {
        return this.cha.isOpen();
    }
    
    //������Ϣ
    /**
     * 
     * @������������Ϣ��������
     * @���ߣ�����
     * @ʱ�䣺2019��7��26�� ����9:07:30
     * @param entity
     * @throws Exception
     */
    public void publish(MqRequestEntity entity) throws Exception
    {
        String queueName = entity.getQueueName();
        // ��ȡ������Ϣ
        HashMap<String, String> queueInfo = MqQueueConfig.getQueueInfo(queueName);
        String queueValue = queueInfo.get("value");
        String type = queueInfo.get("type");
        String exchange = queueInfo.get("exchange");
        if ( !isOpen() )
        {
            throw new ChannelClosedException("�����ر���");
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
