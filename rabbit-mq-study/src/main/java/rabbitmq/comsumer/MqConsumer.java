package rabbitmq.comsumer;

import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Consumer;
import com.thinkive.base.jdbc.DataRow;
import com.thinkive.gateway.v2.result.Result;

/**
 * 
 * @����: �����ߴ���ӿ�
 * @��Ȩ: Copyright (c) 2017 
 * @��˾: ˼�ϿƼ� 
 * @����: �ֶ���
 * @�汾: 1.0 
 * @��������: 2017��2��16�� 
 * @����ʱ��: ����5:23:58
 */
public abstract class MqConsumer
{
    protected static Logger logger    = Logger.getLogger(Consumer.class);
    
    protected final String  option_no = UUID.randomUUID().toString();
    
    public static final String is_requeued_rather_key = "is_requeued_rather";
    
    protected DataRow dataRow = null;
    
    protected Result result = new Result();
    
    /**
     * 
     * @����������ǰ�ò���
     * @���ߣ�����
     * @ʱ�䣺2019��7��29�� ����10:45:40
     * @param param
     * @throws Exception �˷��������쳣��������ӡ�������Ҫ������ӡ��벶׽�����׳��������쳣
     */
    public abstract void before(Map<String, String> param) throws Exception;
    
    /**
     * 
     * @���������Ѻ��ò�������������������Ϣ�����쳣��Ҳ��ִ��after()�� error()��Ҳ����˵���������ѹ��̳����κ����⣬ʼ�ն�����finally��ִ��after()
     * @���ߣ�����
     * @ʱ�䣺2019��7��29�� ����10:46:08
     * @param param
     * @throws Exception �˷������쳣��Ҫ���������д����˴����쳣������ʹ����������С�����������У���Ҫ�ֶ�ִ����ӡ�
     */
    public abstract void after(Map<String, String> param) throws Exception;
    
    /**
     * 
     * @���������ѳɹ���ִ�еĲ�������������ִ������δ�׳��쳣��ִ�С�����˵��mq�ӻ��ֺ����û����ͻ��ֱ䶯֪ͨ
     * @���ߣ�����
     * @ʱ�䣺2019��7��29�� ����11:04:20
     * @param param ���Ѳ���
     * @param result ���ؽ��
     * @return
     * @throws Exception �˷������쳣��Ҫ���������д����˴����쳣������ʹ����������С�����������У���Ҫ�ֶ�ִ����ӡ�
     */
    public abstract void success(Map<String, String> param,Result result) throws Exception;
    
    //���б���ʱִ�еĲ��� ����ˣ����߰����������ƻػ�����е�
    /**
     * 
     * @���������ѳ����쳣��ִ�еĲ��������������˵����������Ҫ��׽���û��Զ����������ִ�й����е��쳣��������������һЩδ֪�쳣��������������л��߼�¼��redis�ȡ�
     * @���ߣ�����
     * @ʱ�䣺2019��7��29�� ����10:47:26
     * @param param
     * @return
     * @throws Exception �˷��������쳣��������ӡ�������Ҫ������ӡ��벶׽�����׳��������쳣
     */
    public abstract void error(Map<String, String> param) throws Exception;
    
    
    /**
     * 
     * @������������Ϣ
     * @���ߣ�������
     * @��������: 2016��6��4��
     * @����ʱ��: ����9:31:14
     * @param message
     * @return
     * @throws Exception 
     */
    public Result process(Map<String, String> param) throws Exception
    {
        before(param);
        boolean flag = false;
        try
        {
            result = pro(param);
            flag = true;
        }
        catch (Exception e)
        {
            flag = false;
            logger.error("�����ߴ��������Ϣʱ�����", e);
            dataRow = new DataRow();
            dataRow.set(is_requeued_rather_key, "true");//��Ϣ�������ԣ�true���������  false��������Ϣ
            result.setResult("ConsumerParams",dataRow);
            error(param);
            //��Ϣ�������ԣ�true���������  false��������Ϣ��������error������param.put("is_ requeued_rather", "true");��ȷ������������Ϣ�������������
        }
        if(flag)
        {
            try
            {
                success(param,result);
            }
            catch (Exception e)
            {
                logger.error("��success�������ߴ�����Ϣ���Ӻ���ִ�г����쳣", e);
            }
        }
        param.put("isSuccess", flag == true ? "yes" : "no");
        try
        {
            after(param);
        }
        catch (Exception e)
        {
            logger.error("��after�������ߴ�����Ϣ���Ӻ���ִ�г����쳣", e);
        }
        return result;
    };
    
    /**
     * 
     * @��������Ϣ�������
     * @���ߣ�����
     * @ʱ�䣺2019��7��29�� ����4:49:17
     */
    public void requeuedRather()
    {
        logger.info("��Ϣ�������");
        dataRow = new DataRow();
        dataRow.set(is_requeued_rather_key, "true");//��Ϣ�������ԣ�true���������  false��������Ϣ
        result.setResult("ConsumerParams",dataRow);
    }
    
    /**
     * 
     * @������������ʵ�ַ������˷������쳣��ʹ����Ӧȫ����׽������˷��������׳��쳣��������������ӡ�
     * @���ߣ�����
     * @ʱ�䣺2019��7��29�� ����11:08:21
     * @param param
     * @return
     * @throws Exception
     */
    public abstract Result pro(Map<String, String> param);
    
    
}
