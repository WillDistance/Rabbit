package exception;

/**
 * 
 * @����: �����ر��˵Ĵ���
 * @��Ȩ: Copyright (c) 2017 
 * @��˾: Thinkive 
 * @����: libo
 * @��������: 2017��8��11�� ����3:54:13
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
