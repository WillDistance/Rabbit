package rabbitmq.bean;

import org.apache.log4j.Logger;

import rabbitmq.conf.MqClientConfig;

import com.thinkive.base.util.StringHelper;

import exception.ChannelClosedException;


/**
 * 
 * @����: ��װrabbit mq �����
 * @��Ȩ: Copyright (c) 2019 
 * @��˾: ˼�ϿƼ� 
 * @����: ����
 * @�汾: 1.0 
 * @��������: 2019��7��26�� 
 * @����ʱ��: ����9:13:15
 */
public class RabbitMqClient
{
    private static Logger logger = Logger.getLogger(RabbitMqClient.class);
    
    /**
     * ������������Ϣ id
     */
    private String        id     = "";
    
    public RabbitMqClient()
    {
        
    }
    
    /**
     * 
     * ������ ���췽�������ݷ��������id������ָ������˶������������ݿ�id��
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
        }catch(ChannelClosedException ce){//�����ǹرյĴ���
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
                //������������������û�б���ͷ�������
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
        }catch(ChannelClosedException ce){//�����رյĴ���
            success = false;
            Thread.sleep(30);
            count++;
            if(count >= 15){
                logger.error("", ce);
                throw new ChannelClosedException("mq���������ͨ��");
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
                //������������������û�б���ͷ�������
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
