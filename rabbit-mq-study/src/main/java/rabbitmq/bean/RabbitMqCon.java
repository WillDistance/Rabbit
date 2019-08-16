package rabbitmq.bean;

import java.io.IOException;
import java.util.Date;

import manager.ChannelPool;

import com.rabbitmq.client.Connection;

/**
 * 
 * @描述: 对rabbit mq连接的封装
 * @版权: Copyright (c) 2019 
 * @公司: 思迪科技 
 * @作者: 严磊
 * @版本: 1.0 
 * @创建日期: 2019年7月26日 
 * @创建时间: 上午9:12:01
 */
public class RabbitMqCon
{
    public static final String status_run  = "status_run";
    
    public static final String status_stop = "status_stop";
    
    /**
     * 用于真正链接的
     */
    protected Connection con;
    
    /**
     * 该链接下面的渠道池
     */
    protected ChannelPool      channelPool;
    
    /**
     * 初始化状态为运行中
     */
    private String             status      = RabbitMqCon.status_run;
    
    //当前连接最后被使用时的时间点（通过连接获取渠道时代表被使用）
    protected long                       IdleTime;
    
    /**
     * 
     * 描述： 构造方法
     * @param con
     * @param maxSize
     * @throws Exception
     */
    public RabbitMqCon(Connection con, int maxSize) throws Exception
    {
        if ( con == null )
            throw new Exception("参数con不能为空，请检查参数");
        this.con = con;
        IdleTime = new Date().getTime();
        channelPool = new ChannelPool(con, maxSize);
    }
    
    /**
     * 
     * @描述：获取连接的最后使用时间点
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
     * @描述：设置运行中的mq连接对象为stop状态
     * @作者：严磊
     * @时间：2019年7月25日 下午6:34:26
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
     * @描述：关闭链接里面的某一个渠道
     * @作者：严磊
     * @时间：2019年7月26日 上午9:11:28
     * @param cha
     */
    public void closeUesdCha(RabbitMqCha cha)
    {
        channelPool.closeUesdCha(cha);
    }
    
    /**
     * 
     * @描述：返回渠道到渠道池
     * @作者：严磊
     * @时间：2019年7月25日 下午8:35:59
     * @param cha
     */
    public void returnCha(RabbitMqCha cha)
    {
        channelPool.returnCha(cha);
    }
    
    
    /**
     * 
     * @描述：关闭连接
     * @作者：严磊
     * @时间：2019年7月25日 下午7:57:52
     * @throws IOException
     */
    public void closeCon() throws IOException
    {
        closeChannelPool();
        if ( this.con != null && this.con.isOpen() )
        {
            this.con.close();//真正的连接关闭，调用amqp中的连接的close方法
        }
    }
    
    /**
     * 
     * @描述：判断该连接的渠道池是否繁忙
     * @作者：严磊
     * @时间：2019年7月25日 下午7:57:02
     * @return
     */
    public boolean isChannelPoolBusy()
    {
        return this.channelPool.isBusy();
    }
    
    /**
     * 
     * @描述：当前连接是否是打开状态
     * @作者：严磊
     * @时间：2019年7月25日 下午7:53:47
     * @return
     */
    public boolean isOpen()
    {
        return this.con.isOpen();
    }
    
    /**
     * 
     * @描述：关闭该连接中渠道池中所有的渠道
     * @作者：严磊
     * @时间：2019年7月25日 下午6:39:24
     */
    private void closeChannelPool()
    {
        this.channelPool.closePool();
    }
    
    /**
     * 
     * @描述: 获取已有或者创建新的渠道
     * @作者: libo
     * @创建日期: 2017年3月3日 下午2:37:05
     * @param queueName 队列名字
     * @param routingKey  路由
     * @param exchange  交换机
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
