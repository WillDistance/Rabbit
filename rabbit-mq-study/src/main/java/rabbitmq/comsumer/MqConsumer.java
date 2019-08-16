package rabbitmq.comsumer;

import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Consumer;
import com.thinkive.base.jdbc.DataRow;
import com.thinkive.gateway.v2.result.Result;

/**
 * 
 * @描述: 消费者处理接口
 * @版权: Copyright (c) 2017 
 * @公司: 思迪科技 
 * @作者: 林冬莲
 * @版本: 1.0 
 * @创建日期: 2017年2月16日 
 * @创建时间: 下午5:23:58
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
     * @描述：消费前置操作
     * @作者：严磊
     * @时间：2019年7月29日 上午10:45:40
     * @param param
     * @throws Exception 此方法出现异常，重新入队。若不需要重新入队。请捕捉方法抛出的所有异常
     */
    public abstract void before(Map<String, String> param) throws Exception;
    
    /**
     * 
     * @描述：消费后置操作，若消费者消费消息出现异常，也会执行after()和 error()，也就是说，不管消费过程出现任何问题，始终都会在finally中执行after()
     * @作者：严磊
     * @时间：2019年7月29日 上午10:46:08
     * @param param
     * @throws Exception 此方法的异常需要开发者自行处理，此处的异常，不会使数据重入队列。若想重入队列，需要手动执行入队。
     */
    public abstract void after(Map<String, String> param) throws Exception;
    
    /**
     * 
     * @描述：消费成功后执行的操作，在消费者执行消费未抛出异常后执行。比如说用mq加积分后，向用户推送积分变动通知
     * @作者：严磊
     * @时间：2019年7月29日 上午11:04:20
     * @param param 消费参数
     * @param result 返回结果
     * @return
     * @throws Exception 此方法的异常需要开发者自行处理，此处的异常，不会使数据重入队列。若想重入队列，需要手动执行入队。
     */
    public abstract void success(Map<String, String> param,Result result) throws Exception;
    
    //队列报错时执行的操作 如回退，或者把数据重新推回缓存队列等
    /**
     * 
     * @描述：消费出现异常后执行的操作，从设计上来说，开发者需要捕捉在用户自定义的消费者执行过程中的异常，仅在这里做另一些未知异常，让数据重入队列或者记录到redis等。
     * @作者：严磊
     * @时间：2019年7月29日 上午10:47:26
     * @param param
     * @return
     * @throws Exception 此方法出现异常，重新入队。若不需要重新入队。请捕捉方法抛出的所有异常
     */
    public abstract void error(Map<String, String> param) throws Exception;
    
    
    /**
     * 
     * @描述：处理消息
     * @作者：邱育武
     * @创建日期: 2016年6月4日
     * @创建时间: 下午9:31:14
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
            logger.error("消费者处理队列消息时候出错", e);
            dataRow = new DataRow();
            dataRow.set(is_requeued_rather_key, "true");//消息丢弃策略，true：重入队列  false：丢弃消息
            result.setResult("ConsumerParams",dataRow);
            error(param);
            //消息丢弃策略，true：重入队列  false：丢弃消息。可以再error中设置param.put("is_ requeued_rather", "true");来确定是抛弃该消息，还是重入队列
        }
        if(flag)
        {
            try
            {
                success(param,result);
            }
            catch (Exception e)
            {
                logger.error("【success】消费者处理消息钩子函数执行出现异常", e);
            }
        }
        param.put("isSuccess", flag == true ? "yes" : "no");
        try
        {
            after(param);
        }
        catch (Exception e)
        {
            logger.error("【after】消费者处理消息钩子函数执行出现异常", e);
        }
        return result;
    };
    
    /**
     * 
     * @描述：消息重新入队
     * @作者：严磊
     * @时间：2019年7月29日 下午4:49:17
     */
    public void requeuedRather()
    {
        logger.info("消息重入队列");
        dataRow = new DataRow();
        dataRow.set(is_requeued_rather_key, "true");//消息丢弃策略，true：重入队列  false：丢弃消息
        result.setResult("ConsumerParams",dataRow);
    }
    
    /**
     * 
     * @描述：消费者实现方法，此方法的异常，使用者应全部捕捉。如果此方法向上抛出异常，则数据重新入队。
     * @作者：严磊
     * @时间：2019年7月29日 上午11:08:21
     * @param param
     * @return
     * @throws Exception
     */
    public abstract Result pro(Map<String, String> param);
    
    
}
