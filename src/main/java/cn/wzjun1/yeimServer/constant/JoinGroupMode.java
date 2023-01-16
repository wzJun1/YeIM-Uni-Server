package cn.wzjun1.yeimServer.constant;

/**
 * 群组的加群处理方式
 */
public class JoinGroupMode {

    //自有加入，不需要申请和审核，不需要被邀请人允许。
    public static final Integer FREE = 1;
    //验证加入，需要申请，以及群主或管理员的同意才能入群
    public static final Integer CHECK = 2;
    //禁止加入
    public static final Integer FORBIDDEN = 3;

}
