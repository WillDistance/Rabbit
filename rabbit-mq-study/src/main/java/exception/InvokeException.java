package exception;


/**
 * 描述: 
 * 版权: Copyright (c) 2012 
 * 公司: 思迪科技 
 * 作者: 李炜
 * 版本: 1.0 
 * 创建日期: May 11, 2013 
 * 创建时间: 11:07:35 AM
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
    
    //新增下面两个构造函数，用于非function中中断流程。上面两个保留用于兼容现有代码，要逐步废弃
    /**
     * @param message 错误信息
     * @param errorType 错误类型(只能在0到99之间)
     */
    public InvokeException(String message, int errorType)
    {
        super(message);
        this.errorType = getErrorType(errorType);
    }
    
    /**
     * @param message 错误信息
     * @param cause 错误原因
     * @param errorType 错误类型(只能在0到99之间)
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
