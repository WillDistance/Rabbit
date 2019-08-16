package rabbitmq.conf;

import java.util.HashMap;

import manager.ConnectionPool;
import rabbitmq.bean.RabbitMqCon;

import com.rabbitmq.client.ConnectionFactory;

public class MqClientConfig
{
    private static String DEFAULT_SERVER_ID = "default";

    private static ConnectionFactory factory = null;//连接工厂

    private static ConnectionPool connectionPool = null;//连接池
    
    public static synchronized ConnectionPool getConnectionPool() throws Exception{
        if(factory==null){
            //根据id读取配置文件，创建连接工厂
            factory=new ConnectionFactory();
            HashMap<String, String> serverInfo=null;
            serverInfo=MqServerConfig.getServerInfo(DEFAULT_SERVER_ID);
            
            String url=serverInfo.get("url");
            int port=Integer.parseInt(serverInfo.get("port"));
            String username=serverInfo.get("username");
            String password=serverInfo.get("password");
            String virtualHost=serverInfo.get("virtualHost");
            
            factory.setHost(url);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setVirtualHost(virtualHost);
            //链接时间设置为永久
            factory.setConnectionTimeout(0);
            //断了自动链接
            factory.setAutomaticRecoveryEnabled(true);
            
        }
        if(connectionPool == null){
            HashMap<String, String> serverInfo=null;
            serverInfo=MqServerConfig.getServerInfo(DEFAULT_SERVER_ID);
            int minPoolSize=Integer.parseInt(serverInfo.get("minPoolSize"));
            int maxPoolSize=Integer.parseInt(serverInfo.get("maxPoolSize"));
            int channelSize=Integer.parseInt(serverInfo.get("channelSize"));
            connectionPool = new ConnectionPool(factory,minPoolSize,maxPoolSize,channelSize);
        }
        return connectionPool;
    }
    public static synchronized ConnectionPool getConnectionPool(String id) throws Exception{
        if(factory==null){
            //根据id读取配置文件，创建连接工厂
            factory=new ConnectionFactory();
            HashMap<String, String> serverInfo=null;
            serverInfo=MqServerConfig.getServerInfo(id);
            
            String url=serverInfo.get("url");
            int port=Integer.parseInt(serverInfo.get("port"));
            String username=serverInfo.get("username");
            String password=serverInfo.get("password");
            String virtualHost=serverInfo.get("virtualHost");
            
            factory.setHost(url);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setVirtualHost(virtualHost);
            //链接时间设置为永久
            factory.setConnectionTimeout(0);
            //断了自动链接
            factory.setAutomaticRecoveryEnabled(true);
            
        }
        if(connectionPool == null){
            //创建mq连接池
            HashMap<String, String> serverInfo=null;
            serverInfo=MqServerConfig.getServerInfo(id);
            int minPoolSize=Integer.parseInt(serverInfo.get("minPoolSize"));
            int maxPoolSize=Integer.parseInt(serverInfo.get("maxPoolSize"));
            int channelSize=Integer.parseInt(serverInfo.get("channelSize"));
            connectionPool = new ConnectionPool(factory,minPoolSize,maxPoolSize,channelSize);
        }
        return connectionPool;
    }
    
    /**
     * 
     * @描述：从连接工厂中获取mq连接
     * @作者：严磊
     * @时间：2019年7月25日 下午6:09:39
     * @return
     * @throws Exception
     */
    public static RabbitMqCon getConnection() throws Exception{
        connectionPool = MqClientConfig.getConnectionPool();
        return connectionPool.getOrCreateConn();
        
    }
}
