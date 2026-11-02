import {UserType} from "./user";

/**
 * 队伍类别
 */
export type TeamType = {
    id: number;
    name: string;
    description: string;
    avatar?: string;       // 队伍头像（TeamCardList / TeamDetailsPage 使用）
    expireTime?: Date;
    maxNum: number;
    password?: string;
    status: number;
    createTime: Date;
    updateTime: Date;
    // 注意：后端字段名如果仍是 creatUser（拼写错误），保留这一行兼容
    creatUser?: UserType;
    createUser?: UserType;
    hasJoin: boolean;
    hasJoinNum?: number;
};