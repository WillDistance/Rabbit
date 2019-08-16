package testDemo;

import java.util.HashMap;
import java.util.Map;

import rabbitmq.bean.MqRequestEntity;
import rabbitmq.bean.RabbitMqClient;
import rabbitmq.entity.ConsumerConstants;
import rabbitmq.entity.QueueContants;

public class TestPublish
{
    
    public static void main(String[] args)
    {
        Map<String, String> param = new HashMap<String, String>();
        for(int i=0;i<3;i++)
        {
            param.clear();
            param.put("id", i+"");
            System.out.println(messagePushQueue(param));
        }
    }
    
    /**
     * 
     * @�������û���ע��Ϣ���
     * @���ߣ�����
     * @ʱ�䣺2018��5��12�� ����7:06:11
     * @param param
     * @return
     */
    public static boolean messagePushQueue(Map<String, String> param)
    {
        boolean boo = false;
        try
        {
            //�������� 
            MqRequestEntity entity = new MqRequestEntity();
            entity.setQueueName(QueueContants.actQueueName);
            entity.setConsumerId(ConsumerConstants.TestConsumerId);
            entity.setParam(param);
            RabbitMqClient mqClient = new RabbitMqClient();
            boo = mqClient.publish(entity);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return boo;
    }
}
