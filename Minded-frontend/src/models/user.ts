/**
 * 用户类别
 */
export type UserType = {
    id: number;
    username: string;       // 用户名
    userAccount: string;    // 账号
    avatarUrl?: string;     // 头像地址
    profile: string;       // 个人简介
    gender:number;          // 性别
    phone: string;          // 电话
    email: string;          // 邮箱
    userStatus: number;     // 用户状态
    userRole: number;       // 用户角色
    tags: string[];         // 标签
    createTime: Date;       // 创建日期
};