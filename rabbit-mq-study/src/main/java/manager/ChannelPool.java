package manager;

import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import rabbitmq.bean.RabbitMqCha;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import exception.ChannelClosedException;

public class ChannelPool
{
    protected static Logger                  logger           = Logger.getLogger(ChannelPool.class);
    
    //创建该渠道池的链接
    protected Connection                     conn;
    
    //使用原子操作类，计数渠道池的大小
    private AtomicLong                       poolSize         = new AtomicLong(0);
    
    private int                              maxSize;
    
    //使用可阻塞的链表队列，存储mq连接下的渠道
    private LinkedBlockingQueue<RabbitMqCha> queue            = new LinkedBlockingQueue<RabbitMqCha>();
    
    //轮值任务间隔时间
    private int                              ScanvageInterval = 60 * 1000;
    
    private Timer                            ScanTimer;
    
    private volatile boolean                 isClose          = false;
    
    //闲置多长时间就回收
    private volatile long                    idelTime         = 30 * 1000;
    
    //无界的
    //    public ChannelPool(Connection conn) throws Exception{
    //        if (conn == null)
    //            throw new Exception("参数conn不能为空，请检查参数");
    //        queue = new LinkedBlockingQueue<RabbitMqCha>();
    //        this.conn = conn;
    //        ScanTimer = new Timer();  
    //        long delay = 0;  
    //        ScanTimer.scheduleAtFixedRate(new TimerTask() {  
    //            @Override  
    //            public void run() {  
    //                scanTask(); 
    //            }  
    //        } , delay, ScanvageInterval);  
    //    }
    
    
    //有界的
    public ChannelPool(Connection conn, int maxSize) throws Exception
    {
        if ( conn == null )
            throw new Exception("参数conn不能为空，请检查参数");
        if ( maxSize <= 0 )
        {
            this.maxSize = 1;
        }
        else
        {
            this.maxSize = maxSize;
        }
        this.conn = conn;
        ScanTimer = new Timer();
        ScanTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                logger.info("--开始执行渠道池" + this + "的定时任务");
                scanTask();
            }
        }, ScanvageInterval, ScanvageInterval);
    }
    
    //对池子里面的空闲对象进行查看
    /**
     * 
     * @描述：遍历渠道池中的渠道空闲时间是否超过了允许的最大空闲时间。并对空闲过长的渠道尝试关闭
     * @作者：严磊
     * @时间：2019年7月25日 下午8:13:09
     */
    private void scanTask()
    {
        logger.info("开始循环便利渠道池" + this + "的定时任务");
        //没有空闲渠道
        if ( queue.size() == 0 )
        {
            logger.info(this + "队列长度为0返回");
            return;
        }
        else
        {
            Iterator<RabbitMqCha> ite = queue.iterator();
            while (ite.hasNext())
            {
                RabbitMqCha cha = (RabbitMqCha) ite.next();
                long temp_time = new Date().getTime() - cha.getIdleTime();
                logger.info(this + "渠道池里面的 渠道" + cha + "已经闲置了" + temp_time);
                if ( temp_time > idelTime )
                {
                    //查看是否为空闲状态的
                    if ( cha.idelToInvalid() )
                    {
                        removeItem(cha);
                    }
                }
            }
        }
    }
    
    /**
     * 
     * @描述：清空定时器的任务队列
     * @作者：严磊
     * @时间：2019年7月25日 下午8:06:30
     */
    public void closeTimer()
    {
        if ( ScanTimer != null )
        {
            //TimerTask类中的cancel()方法侧重的是将自身从任务队列中清除，其他任务不受影响.
            //Timer类中的cancel()方法则是将任务队列中全部的任务清空
            ScanTimer.cancel();
        }
    }
    

    
    /**
     * 
     * @描述：查看池子是否繁忙，如果渠道池中的渠道数大于等于设定的最大数的70%，则判断为繁忙
     * @作者：严磊
     * @时间：2019年7月25日 下午7:54:47
     * @return
     */
    public boolean isBusy()
    {
        if ( maxSize * 0.7 <= poolSize.get() )
        {
            return true;
        }
        return false;
    }
    
    //关闭本连接池
    /**
     * 
     * @描述：关闭渠道池
     * @作者：严磊
     * @时间：2019年7月25日 下午8:03:04
     */
    public void closePool()
    {
        if ( isClose )
        {
            return;
        }
        isClose = true;
        closeTimer();
        Iterator<RabbitMqCha> ite = queue.iterator();
        queue.clear();
        while (ite.hasNext())
        {
            RabbitMqCha cha = (RabbitMqCha) ite.next();
            if ( cha.idelToInvalid() )
            {
                removeItem(cha);
            }
        }
    }
    
    /**
     * 
     * @描述：关闭渠道
     * @作者：严磊
     * @时间：2019年7月25日 下午8:08:12
     * @param cha
     */
    private void removeItem(RabbitMqCha cha)
    {
        queue.remove(cha);//将渠道移除出连接的渠道池
        closeIdelCha(cha);
    }
    
    
    /**
     * 
     * @描述：渠道池数量减1，然后关闭渠道
     * @作者：严磊
     * @时间：2019年7月25日 下午7:33:11
     * @param cha
     */
    public void closeIdelCha(RabbitMqCha cha)
    {
        poolSize.decrementAndGet();
        if ( cha.isOpen() )
        {
            cha.closeCha();
        }
    }
    
    /**
     * 
     * @描述：关闭使用状态的渠道
     * @作者：严磊
     * @时间：2019年7月25日 下午6:49:22
     * @param cha
     */
    public void closeUesdCha(RabbitMqCha cha)
    {
        if ( cha.usedToInvalid() )
        {
            poolSize.decrementAndGet();//渠道池大小减1
            if ( isClose )
            {
                return;
            }
            if ( cha.isOpen() )
            {
                cha.closeCha();
            }
        }
    }
    
    /**
     * 
     * @描述：返回渠道到渠道池，将渠道的状态由使用中改为初始化状态。若渠道池状态是关闭状态，则渠道池大小减1，该渠道丢弃。
     * @作者：严磊
     * @时间：2019年7月25日 下午8:18:30
     * @param cha
     */
    public void returnCha(RabbitMqCha cha)
    {
        cha.reloadIdleTime();
        if ( cha.toBeIdel() )
        {
            if ( isClose )
            {
                poolSize.decrementAndGet();
                removeItem(cha);//这里最好还是关闭下渠道  yl
                return;
            }
            queue.offer(cha);
        }
    }
    
    //会阻塞线程的获取渠道
    /**
     * 
     * @描述：使用take()阻塞，获取渠道（若渠道队列中为空，则阻塞等待）
     * @作者：严磊
     * @时间：2019年7月25日 下午8:26:53
     * @return
     * @throws Exception
     */
    public RabbitMqCha GetOrCreateChannel() throws Exception
    {
        if ( isClose )
        {
            throw new ChannelClosedException("渠道关闭");
        }
        //立即获取
        RabbitMqCha cha = null;
        while (cha == null)
        {
            cha = queue.poll();
            //空闲队列里面没有
            if ( cha == null )
            {
                if ( poolSize.get() < maxSize )
                {
                    cha = addRabbiMq();
                }
                else
                {
                    //阻塞获取
                    cha = queue.take();
                }
            }
            //将渠道置为使用中，如果渠道已经是使用中，则重新获取
            if ( !cha.toBeUsed() )
            {
                cha = null;
            }
        }
        return cha;
    }
    
    //创建新的渠道
    /**
     * 
     * @描述：创建新的渠道，之所以在创建的时候不需要将渠道放入渠道池，是因为每次使用渠道后，会调用returnCha()方法将渠道返回给渠道池，并offer进queue
     * @作者：严磊
     * @时间：2019年7月25日 下午8:29:35
     * @return
     * @throws Exception
     */
    public RabbitMqCha addRabbiMq() throws Exception
    {
        Channel cha = conn.createChannel();
        RabbitMqCha rmc = new RabbitMqCha(cha);
        poolSize.incrementAndGet();
        return rmc;
    }
}
