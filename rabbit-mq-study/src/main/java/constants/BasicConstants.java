package constants;

/**
 * 
 * @描述: REDIS常量(使用运行目录中 ActivtyConstantsConf 文件)
 * @版权: Copyright (c) 2016
 * @公司: 思迪科技
 * @作者: 曹友安
 * @版本: 1.0
 * @创建日期: 2016年8月24日
 * @创建时间: 下午1:27:29
 */
public final class BasicConstants {

    private BasicConstants() {

    }

    /**
     * oracle/mysql/reids 默认配置IDS
     */
    public final static String act_defualt_dbname = "web";

    /**
     * 前缀
     */
    private static String redisPerKey = "";
    /**
     * 应用接入凭证
     */
    public final static String app_access_token = redisPerKey + "act_app_access_token";// 应用接入凭证
    
    
}
