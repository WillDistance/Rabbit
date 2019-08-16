package exception;


/**
 * ����: 
 * ��Ȩ: Copyright (c) 2012 
 * ��˾: ˼�ϿƼ� 
 * ����: ���
 * �汾: 1.0 
 * ��������: May 11, 2013 
 * ����ʱ��: 11:07:35 AM
 */
public class InvokeException extends RuntimeException
{
    
    private static final long serialVersionUID = 1L;
    
    private int               errorCode;
    
    public int getErrorCode()
    {
        return errorCode;
    }
    
    private int errorType = -1;
    
    public int getErrorType()
    {
        return errorType;
    }
    
    @Deprecated
    public InvokeException(int errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
    }
    
    @Deprecated
    public InvokeException(int errorCode, String message, Throwable cause)
    {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    //���������������캯�������ڷ�function���ж����̡����������������ڼ������д��룬Ҫ�𲽷���
    /**
     * @param message ������Ϣ
     * @param errorType ��������(ֻ����0��99֮��)
     */
    public InvokeException(String message, int errorType)
    {
        super(message);
        this.errorType = getErrorType(errorType);
    }
    
    /**
     * @param message ������Ϣ
     * @param cause ����ԭ��
     * @param errorType ��������(ֻ����0��99֮��)
     */
    public InvokeException(String message, Throwable cause, int errorType)
    {
        super(message, cause);
        this.errorType = getErrorType(errorType);
    }
    
    private int getErrorType(int errorType)
    {
        if ( errorType > 100 )
        {
            return 99;
        }
        else if ( errorType < 0 )
        {
            return 0;
        }
        else
        {
            return errorType;
        }
    }
    
}
