package constants;

/**
 * 
 * @����: REDIS����(ʹ������Ŀ¼�� ActivtyConstantsConf �ļ�)
 * @��Ȩ: Copyright (c) 2016
 * @��˾: ˼�ϿƼ�
 * @����: ���Ѱ�
 * @�汾: 1.0
 * @��������: 2016��8��24��
 * @����ʱ��: ����1:27:29
 */
public final class BasicConstants {

    private BasicConstants() {

    }

    /**
     * oracle/mysql/reids Ĭ������IDS
     */
    public final static String act_defualt_dbname = "web";

    /**
     * ǰ׺
     */
    private static String redisPerKey = "";
    /**
     * Ӧ�ý���ƾ֤
     */
    public final static String app_access_token = redisPerKey + "act_app_access_token";// Ӧ�ý���ƾ֤
    
    
}
