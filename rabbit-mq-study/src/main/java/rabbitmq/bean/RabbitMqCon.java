package rabbitmq.bean;

import java.io.IOException;
import java.util.Date;

import manager.ChannelPool;

import com.rabbitmq.client.Connection;

/**
 * 
 * @����: ��rabbit mq���ӵķ�װ
 * @��Ȩ: Copyright (c) 2019 
 * @��˾: ˼�ϿƼ� 
 * @����: ����
 * @�汾: 1.0 
 * @��������: 2019��7��26�� 
 * @����ʱ��: ����9:12:01
 */
public class RabbitMqCon
{
    public static final String status_run  = "status_run";
    
    public static final String status_stop = "status_stop";
    
    /**
     * �����������ӵ�
     */
    protected Connection con;
    
    /**
     * �����������������
     */
    protected ChannelPool      channelPool;
    
    /**
     * ��ʼ��״̬Ϊ������
     */
    private String             status      = RabbitMqCon.status_run;
    
    //��ǰ�������ʹ��ʱ��ʱ��㣨ͨ�����ӻ�ȡ����ʱ����ʹ�ã�
    protected long                       IdleTime;
    
    /**
     * 
     * ������ ���췽��
     * @param con
     * @param maxSize
     * @throws Exception
     */
    public RabbitMqCon(Connection con, int maxSize) throws Exception
    {
        if ( con == null )
            throw new Exception("����con����Ϊ�գ��������");
        this.con = con;
        IdleTime = new Date().getTime();
        channelPool = new ChannelPool(con, maxSize);
    }
    
    /**
     * 
     * @��������ȡ���ӵ����ʹ��ʱ���
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
     * @���������������е�mq���Ӷ���Ϊstop״̬
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����6:34:26
     * @return
     */
    public synchronized boolean statusToClose()
    {
        if ( RabbitMqCon.status_run.equals(status) )
        {
            status = RabbitMqCon.status_stop;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * 
     * @�������ر����������ĳһ������
     * @���ߣ�����
     * @ʱ�䣺2019��7��26�� ����9:11:28
     * @param cha
     */
    public void closeUesdCha(RabbitMqCha cha)
    {
        channelPool.closeUesdCha(cha);
    }
    
    /**
     * 
     * @����������������������
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����8:35:59
     * @param cha
     */
    public void returnCha(RabbitMqCha cha)
    {
        channelPool.returnCha(cha);
    }
    
    
    /**
     * 
     * @�������ر�����
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����7:57:52
     * @throws IOException
     */
    public void closeCon() throws IOException
    {
        closeChannelPool();
        if ( this.con != null && this.con.isOpen() )
        {
            this.con.close();//���������ӹرգ�����amqp�е����ӵ�close����
        }
    }
    
    /**
     * 
     * @�������жϸ����ӵ��������Ƿ�æ
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����7:57:02
     * @return
     */
    public boolean isChannelPoolBusy()
    {
        return this.channelPool.isBusy();
    }
    
    /**
     * 
     * @��������ǰ�����Ƿ��Ǵ�״̬
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����7:53:47
     * @return
     */
    public boolean isOpen()
    {
        return this.con.isOpen();
    }
    
    /**
     * 
     * @�������رո������������������е�����
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����6:39:24
     */
    private void closeChannelPool()
    {
        this.channelPool.closePool();
    }
    
    /**
     * 
     * @����: ��ȡ���л��ߴ����µ�����
     * @����: libo
     * @��������: 2017��3��3�� ����2:37:05
     * @param queueName ��������
     * @param routingKey  ·��
     * @param exchange  ������
     * @return
     * @throws Exception 
     */
    public RabbitMqCha GetOrCreateChannel() throws Exception
    {
        RabbitMqCha cha = this.channelPool.GetOrCreateChannel();
        IdleTime = new Date().getTime();
        return cha;
    }
}
