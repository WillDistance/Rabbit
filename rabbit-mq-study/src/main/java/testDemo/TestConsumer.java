package testDemo;

import java.util.Map;

import org.apache.log4j.Logger;

import rabbitmq.comsumer.MqConsumer;

import com.thinkive.gateway.v2.result.Result;

/**
 * 
 * @描述: 华泰期货合约产品竞猜处理类
 * @版权: Copyright (c) 2018 
 * @公司: 思迪科技 
 * @作者: 韩典宗
 * @版本: 1.0 
 * @创建日期: 2018年5月3日 
 * @创建时间: 下午4:56:30
 */
public class TestConsumer extends MqConsumer
{
    private Logger         logger = Logger.getLogger(TestConsumer.class);

    @Override
    public Result pro(Map<String, String> param)
    {
        Result result = new Result();
        logger.info("***********************处理投注记录队列处理开始!************************");
        
        /**************消费处理**************/
        logger.info("处理参数为:" + param.toString());
        System.out.println("执行消费："+ param.toString());
        if(Integer.parseInt(param.get("id")) == 2)
        {
            if((int)(Math.random()*10)>5)
            {
                throw new RuntimeException("主动抛出异常，在error方法中实现消息重入队");
            }
        }
        return result;
        
    }

    @Override
    public void before(Map<String, String> param) throws Exception
    {
        System.out.println("消费前置操作");
    }

    @Override
    public void after(Map<String, String> param) throws Exception
    {
        System.out.println("消费后置操作");
    }

    @Override
    public void success(Map<String, String> param, Result result) throws Exception
    {
        System.out.println("消费成功后执行的操作");
    }

    @Override
    public void error(Map<String, String> param) throws Exception
    {
        System.out.println("消费出现异常后执行的操作");
        requeuedRather();//重入队列
    }
}
