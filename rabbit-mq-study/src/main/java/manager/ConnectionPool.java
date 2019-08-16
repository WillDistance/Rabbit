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
    
    //用于该链接创建
    protected ConnectionFactory        conFactory;
    
    //轮值任务间隔时间
    private int                        ScanvageInterval = 120 * 1000;
    
    private Timer                      ScanTimer;
    
    //闲置多长时间就回收
    private long                       idelTime         = 60 * 1000;
    
    public ConnectionPool(ConnectionFactory conFactory) throws Exception
    {
        this(conFactory, DECORESIZE, DEMAXSIZE, DECHANNELSIZE);
    }
    
    public ConnectionPool(ConnectionFactory conFactory, int coreSize, int maxSize, int channelSize) throws Exception
    {
        if ( conFactory == null )
        {
            throw new Exception("conFactory不能为空");
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
        //创建定时器对象
        ScanTimer = new Timer();
        /**
         * 理解schedule和scheduleAtFixedRate的区别：
         * 暑假到了老师给schedule和scheduleAtFixedRate两个同学布置作业。
         * 老师要求学生暑假每天写2页，30天后完成作业。这两个学生每天按时完成作业，直到第10天，出了意外，两个学生出去旅游花了5天时间，这5天时间里两个人都没有做作业。任务被拖延了。这时候两个学生采取的策略就不同了：schedule重新安排了任务时间，旅游回来的第一天做第11天的任务，第二天做第12天的任务，最后完成任务花了35天。scheduleAtFixedRate是个守时的学生，她总想按时完成老师的任务，于是在旅游回来的第一天把之前5天欠下的任务以及第16天当天的任务全部完成了，之后还是按照老师的原安排完成作业，最后完成任务花了30天。(不一定是第一天就完成之前5天的任务,因任务执行时间而定,但会连续执行任务,不间隔,直到赶上之前时间节点的任务安排为止.)
         * 
         */
        ScanTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                logger.info("--开始执行连接池" + this + "的定时任务");
                scanTask();
            }
        }, ScanvageInterval, ScanvageInterval);
    }
    
    private void scanTask()
    {
        if ( list.size() > coreSize )
        {
            //保留核心数目
            for (int i = 0; i < list.size() && list.size() > coreSize; i++)
            {
                RabbitMqCon con = list.get(i);
                long temp_time = new Date().getTime() - con.getIdleTime();
                logger.info("当前链接为" + con + "，已经闲置了" + temp_time);
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
        list.remove(con);//从连接对象列表中删除连接
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
//        //等比负载均衡 
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
