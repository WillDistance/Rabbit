package testDemo;

import java.util.Map;

import org.apache.log4j.Logger;

import rabbitmq.comsumer.MqConsumer;

import com.thinkive.gateway.v2.result.Result;

/**
 * 
 * @����: ��̩�ڻ���Լ��Ʒ���´�����
 * @��Ȩ: Copyright (c) 2018 
 * @��˾: ˼�ϿƼ� 
 * @����: ������
 * @�汾: 1.0 
 * @��������: 2018��5��3�� 
 * @����ʱ��: ����4:56:30
 */
public class TestConsumer extends MqConsumer
{
    private Logger         logger = Logger.getLogger(TestConsumer.class);

    @Override
    public Result pro(Map<String, String> param)
    {
        Result result = new Result();
        logger.info("***********************����Ͷע��¼���д���ʼ!************************");
        
        /**************���Ѵ���**************/
        logger.info("�������Ϊ:" + param.toString());
        System.out.println("ִ�����ѣ�"+ param.toString());
        if(Integer.parseInt(param.get("id")) == 2)
        {
            if((int)(Math.random()*10)>5)
            {
                throw new RuntimeException("�����׳��쳣����error������ʵ����Ϣ�����");
            }
        }
        return result;
        
    }

    @Override
    public void before(Map<String, String> param) throws Exception
    {
        System.out.println("����ǰ�ò���");
    }

    @Override
    public void after(Map<String, String> param) throws Exception
    {
        System.out.println("���Ѻ��ò���");
    }

    @Override
    public void success(Map<String, String> param, Result result) throws Exception
    {
        System.out.println("���ѳɹ���ִ�еĲ���");
    }

    @Override
    public void error(Map<String, String> param) throws Exception
    {
        System.out.println("���ѳ����쳣��ִ�еĲ���");
        requeuedRather();//�������
    }
}
