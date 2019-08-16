package rabbitmq.conf;

import java.util.HashMap;

import manager.ConnectionPool;
import rabbitmq.bean.RabbitMqCon;

import com.rabbitmq.client.ConnectionFactory;

public class MqClientConfig
{
    private static String DEFAULT_SERVER_ID = "default";

    private static ConnectionFactory factory = null;//���ӹ���

    private static ConnectionPool connectionPool = null;//���ӳ�
    
    public static synchronized ConnectionPool getConnectionPool() throws Exception{
        if(factory==null){
            //����id��ȡ�����ļ����������ӹ���
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
            //����ʱ������Ϊ����
            factory.setConnectionTimeout(0);
            //�����Զ�����
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
            //����id��ȡ�����ļ����������ӹ���
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
            //����ʱ������Ϊ����
            factory.setConnectionTimeout(0);
            //�����Զ�����
            factory.setAutomaticRecoveryEnabled(true);
            
        }
        if(connectionPool == null){
            //����mq���ӳ�
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
     * @�����������ӹ����л�ȡmq����
     * @���ߣ�����
     * @ʱ�䣺2019��7��25�� ����6:09:39
     * @return
     * @throws Exception
     */
    public static RabbitMqCon getConnection() throws Exception{
        connectionPool = MqClientConfig.getConnectionPool();
        return connectionPool.getOrCreateConn();
        
    }
}
