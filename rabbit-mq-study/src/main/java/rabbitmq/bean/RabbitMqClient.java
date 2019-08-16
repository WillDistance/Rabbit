package rabbitmq.bean;

import org.apache.log4j.Logger;

import rabbitmq.conf.MqClientConfig;

import com.thinkive.base.util.StringHelper;

import exception.ChannelClosedException;


/**
 * 
 * @描述: 封装rabbit mq 服务端
 * @版权: Copyright (c) 2019 
 * @公司: 思迪科技 
 * @作者: 严磊
 * @版本: 1.0 
 * @创建日期: 2019年7月26日 
 * @创建时间: 上午9:13:15
 */
public class RabbitMqClient
{
    private static Logger logger = Logger.getLogger(RabbitMqClient.class);
    
    /**
     * 服务器配置信息 id
     */
    private String        id     = "";
    
    public RabbitMqClient()
    {
        
    }
    
    /**
     * 
     * 描述： 构造方法，根据服务端配置id，创建指定服务端对象（类似于数据库id）
     * @param id
     */
    public RabbitMqClient(String id)
    {
        this.id = id;
    }
    
    public RabbitMqCon getMqCon() throws Exception
    {
        if ( StringHelper.isEmpty(this.id) )
        {
            return MqClientConfig.getConnectionPool().getOrCreateConn();
        }else{
            return MqClientConfig.getConnectionPool(this.id).getOrCreateConn();
        }
    }
    
    public boolean publish(MqRequestEntity entity) throws Exception
    {
        RabbitMqCon con = null;
        RabbitMqCha cha = null;
        boolean success = true;
        try
        {
            con = this.getMqCon();
            if ( con != null )
            {
                cha = con.GetOrCreateChannel();
                cha.publish(entity);
            }
        }catch(ChannelClosedException ce){//渠道是关闭的错误
            success = false;
            publish(entity,1);
        }
        catch (Exception e)
        {
            success = false;
            logger.error("", e);
            throw e;
        }
        finally
        {
            if ( cha != null )
            {
                //如果报错就销毁渠道，没有报错就返回渠道
                if ( success )
                {
                    con.returnCha(cha);
                }
                else
                {
                    con.closeUesdCha(cha);
                }
            }
        }
        return true;
        
    }
    
    private  boolean publish(MqRequestEntity entity,int count) throws Exception
    {
        RabbitMqCon con = null;
        RabbitMqCha cha = null;
        boolean success = true;
        try
        {   
            con = this.getMqCon();
            if ( con != null )
            {
                cha = con.GetOrCreateChannel();
                cha.publish(entity);
            }
        }catch(ChannelClosedException ce){//渠道关闭的错误
            success = false;
            Thread.sleep(30);
            count++;
            if(count >= 15){
                logger.error("", ce);
                throw new ChannelClosedException("mq网络持续不通畅");
            }else{
                publish(entity,count);
            }
        }
        catch (Exception e)
        {
            success = false;
            logger.error("", e);
            throw e;
        }
        finally
        {
            if ( cha != null )
            {
                //如果报错就销毁渠道，没有报错就返回渠道
                if ( success )
                {
                    con.returnCha(cha);
                }
                else
                {
                    con.closeUesdCha(cha);
                }
            }
        }
        return true;
        
    }
}
