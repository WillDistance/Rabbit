package manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import rabbitmq.bean.RabbitMqCon;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ConnectionPool
{
    protected static Logger            logger           = Logger.getLogger(ConnectionPool.class);
    
    private volatile List<RabbitMqCon> list             = new ArrayList<RabbitMqCon>();
    
    private Object                     lock             = new Object();
    
    private static final int           DEMAXSIZE        = 8;
    
    private static final int           DECORESIZE       = 2;
    
    private static final int           DECHANNELSIZE    = 100;
    
    private int                        channelSize;
    
    private int                        maxSize;
    
    private int                        coreSize;
    
    //���ڸ����Ӵ���
    protected ConnectionFactory        conFactory;
    
    //��ֵ������ʱ��
    private int                        ScanvageInterval = 120 * 1000;
    
    private Timer                      ScanTimer;
    
    //���ö೤ʱ��ͻ���
    private long                       idelTime         = 60 * 1000;
    
    public ConnectionPool(ConnectionFactory conFactory) throws Exception
    {
        this(conFactory, DECORESIZE, DEMAXSIZE, DECHANNELSIZE);
    }
    
    public ConnectionPool(ConnectionFactory conFactory, int coreSize, int maxSize, int channelSize) throws Exception
    {
        if ( conFactory == null )
        {
            throw new Exception("conFactory����Ϊ��");
        }
        if ( maxSize <= 0 )
        {
            this.maxSize = 1;
        }
        else
        {
            this.maxSize = maxSize;
        }
        if ( coreSize <= 0 )
        {
            this.coreSize = 1;
        }
        else
        {
            this.coreSize = coreSize;
        }
        if ( channelSize <= 0 )
        {
            this.channelSize = 1;
        }
        else
        {
            this.channelSize = channelSize;
        }
        this.conFactory = conFactory;
        for (int i = 0; i < this.coreSize; i++)
        {
            addConn();
        }
        //������ʱ������
        ScanTimer = new Timer();
        /**
         * ���schedule��scheduleAtFixedRate������
         * ��ٵ�����ʦ��schedule��scheduleAtFixedRate����ͬѧ������ҵ��
         * ��ʦҪ��ѧ�����ÿ��д2ҳ��30��������ҵ��������ѧ��ÿ�찴ʱ�����ҵ��ֱ����10�죬�������⣬����ѧ����ȥ���λ���5��ʱ�䣬��5��ʱ���������˶�û������ҵ�����������ˡ���ʱ������ѧ����ȡ�Ĳ��ԾͲ�ͬ�ˣ�schedule���°���������ʱ�䣬���λ����ĵ�һ������11������񣬵ڶ�������12�������������������35�졣scheduleAtFixedRate�Ǹ���ʱ��ѧ���������밴ʱ�����ʦ���������������λ����ĵ�һ���֮ǰ5��Ƿ�µ������Լ���16�쵱�������ȫ������ˣ�֮���ǰ�����ʦ��ԭ���������ҵ��������������30�졣(��һ���ǵ�һ������֮ǰ5�������,������ִ��ʱ�����,��������ִ������,�����,ֱ������֮ǰʱ��ڵ��������Ϊֹ.)
         * 
         */
        ScanTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                logger.info("--��ʼִ�����ӳ�" + this + "�Ķ�ʱ����");
                scanTask();
            }
        }, ScanvageInterval, ScanvageInterval);
    }
    
    private void scanTask()
    {
        if ( list.size() > coreSize )
        {
            //����������Ŀ
            for (int i = 0; i < list.size() && list.size() > coreSize; i++)
            {
                RabbitMqCon con = list.get(i);
                long temp_time = new Date().getTime() - con.getIdleTime();
                logger.info("��ǰ����Ϊ" + con + "���Ѿ�������" + temp_time);
                if ( temp_time > idelTime )
                {
                    try
                    {
                        if ( con.statusToClose() )
                        {
                            removeItem(con);
                        }
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
        }
    }
    
    //
    private void removeItem(RabbitMqCon con) throws IOException
    {
        list.remove(con);//�����Ӷ����б���ɾ������
        con.closeCon();
    }
    
    public RabbitMqCon getOrCreateConn() throws Exception
    {
        
            for (RabbitMqCon con : list)
            {
                if ( !con.isChannelPoolBusy() )
                {
                    boolean b = con.isOpen();
                    if ( b )
                    {
                        return con;
                    }
//                    else
//                    {
//                        if ( con.statusToClose() )
//                        {
//                            removeItem(con);
//                        }
//                    }
                }
            }
            synchronized (lock)
            {
                if ( list.size() < maxSize )
                {
                    RabbitMqCon con = addConn();
                    return con;
                }
            }
            return getOrCreateConn();
    }
    
//    private RabbitMqCon fzjhGetCon()
//    {
//        //�ȱȸ��ؾ��� 
//        int tempM = list.size();
//        int index = (int) (Math.random() * tempM);
//        return list.get(index);
//    }
    
    private RabbitMqCon addConn() throws Exception
    {
        Connection con = conFactory.newConnection();
        RabbitMqCon rmc = new RabbitMqCon(con, channelSize);
        list.add(rmc);
        return rmc;
    }
}
