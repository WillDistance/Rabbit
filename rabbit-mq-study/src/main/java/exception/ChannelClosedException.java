package exception;

/**
 * 
 * @描述: 渠道关闭了的错误
 * @版权: Copyright (c) 2017 
 * @公司: Thinkive 
 * @作者: libo
 * @创建日期: 2017年8月11日 下午3:54:13
 */
public class ChannelClosedException extends Exception
{
    
    private static final long serialVersionUID = 1L;
    
    public ChannelClosedException()
    {
        super();
    }
    
    public ChannelClosedException(String message)
    {
        super(message);
    }
    
    public ChannelClosedException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public ChannelClosedException(Throwable cause)
    {
        super(cause);
    }
}
