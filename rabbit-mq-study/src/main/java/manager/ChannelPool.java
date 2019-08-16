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
    
    //�����������ص�����
    protected Connection                     conn;
    
    //ʹ��ԭ�Ӳ����࣬���������صĴ�С
    private AtomicLong                       poolSize         = new AtomicLong(0);
    
    private int                              maxSize;
    
    //ʹ�ÿ�������������У��洢mq�����µ�����
    private LinkedBlockingQueue<RabbitMqCha> queue            = new LinkedBlockingQueue<RabbitMqCha>();
    
    //��ֵ������ʱ��
    private int                              ScanvageInterval = 60 * 1000;
    
    private Timer                            ScanTimer;
    
    private volatile boolean                 isClose          = false;
    
    //���ö೤ʱ��ͻ���
    private volatile long                    idelTime         = 30 * 1000;
    
    //�޽��
    //    public ChannelPool(Connection conn) throws Exception{
    //        if (conn == null)
    //            throw new Exception("����conn����Ϊ�գ��������");
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
    
    
    //�н��
    public ChannelPool(Connection conn, int maxSize) throws Exception
    {
        if ( conn == null )
            throw new Exception("����conn����Ϊ�գ��������");
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
                logger.info("--��ʼִ��������" + this + "�Ķ�ʱ����");
                scanTask();
            }
        }, ScanvageInterval, ScanvageInterval);
    }
    
    //�Գ�������Ŀ��ж�����в鿴
    /**
     * 
     * @�����������������е���������ʱ���Ƿ񳬹��������������ʱ�䡣���Կ��й������������Թر�
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����8:13:09
     */
    private void scanTask()
    {
        logger.info("��ʼѭ������������" + this + "�Ķ�ʱ����");
        //û�п�������
        if ( queue.size() == 0 )
        {
            logger.info(this + "���г���Ϊ0����");
            return;
        }
        else
        {
            Iterator<RabbitMqCha> ite = queue.iterator();
            while (ite.hasNext())
            {
                RabbitMqCha cha = (RabbitMqCha) ite.next();
                long temp_time = new Date().getTime() - cha.getIdleTime();
                logger.info(this + "����������� ����" + cha + "�Ѿ�������" + temp_time);
                if ( temp_time > idelTime )
                {
                    //�鿴�Ƿ�Ϊ����״̬��
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
     * @��������ն�ʱ�����������
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����8:06:30
     */
    public void closeTimer()
    {
        if ( ScanTimer != null )
        {
            //TimerTask���е�cancel()�������ص��ǽ������������������������������Ӱ��.
            //Timer���е�cancel()�������ǽ����������ȫ�����������
            ScanTimer.cancel();
        }
    }
    

    
    /**
     * 
     * @�������鿴�����Ƿ�æ������������е����������ڵ����趨���������70%�����ж�Ϊ��æ
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����7:54:47
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
    
    //�رձ����ӳ�
    /**
     * 
     * @�������ر�������
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����8:03:04
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
     * @�������ر�����
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����8:08:12
     * @param cha
     */
    private void removeItem(RabbitMqCha cha)
    {
        queue.remove(cha);//�������Ƴ������ӵ�������
        closeIdelCha(cha);
    }
    
    
    /**
     * 
     * @������������������1��Ȼ��ر�����
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����7:33:11
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
     * @�������ر�ʹ��״̬������
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����6:49:22
     * @param cha
     */
    public void closeUesdCha(RabbitMqCha cha)
    {
        if ( cha.usedToInvalid() )
        {
            poolSize.decrementAndGet();//�����ش�С��1
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
     * @���������������������أ���������״̬��ʹ���и�Ϊ��ʼ��״̬����������״̬�ǹر�״̬���������ش�С��1��������������
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����8:18:30
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
                removeItem(cha);//������û��ǹر�������  yl
                return;
            }
            queue.offer(cha);
        }
    }
    
    //�������̵߳Ļ�ȡ����
    /**
     * 
     * @������ʹ��take()��������ȡ������������������Ϊ�գ��������ȴ���
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����8:26:53
     * @return
     * @throws Exception
     */
    public RabbitMqCha GetOrCreateChannel() throws Exception
    {
        if ( isClose )
        {
            throw new ChannelClosedException("�����ر�");
        }
        //������ȡ
        RabbitMqCha cha = null;
        while (cha == null)
        {
            cha = queue.poll();
            //���ж�������û��
            if ( cha == null )
            {
                if ( poolSize.get() < maxSize )
                {
                    cha = addRabbiMq();
                }
                else
                {
                    //������ȡ
                    cha = queue.take();
                }
            }
            //��������Ϊʹ���У���������Ѿ���ʹ���У������»�ȡ
            if ( !cha.toBeUsed() )
            {
                cha = null;
            }
        }
        return cha;
    }
    
    //�����µ�����
    /**
     * 
     * @�����������µ�������֮�����ڴ�����ʱ����Ҫ���������������أ�����Ϊÿ��ʹ�������󣬻����returnCha()�������������ظ������أ���offer��queue
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����8:29:35
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
