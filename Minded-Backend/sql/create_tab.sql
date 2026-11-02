/*用户表*/
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256)                       null comment '用户昵称',
    userAccount  varchar(256)                       null comment '账号',
    userPassword varchar(512)                       not null comment '用户密码',
    avatarUrl    varchar(1024)                      null comment '用户头像',
    gender       tinyint                            null comment '性别',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 not null comment '状态：0 - 正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    role         int      default 0                 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    tags         varchar(256)                       null comment '用户标签'
)
    comment '用户中心表';

/*标签表*/
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256)                       null comment '标签名称',
    userId     bigint                             null comment '用户 id',
    parentId   bigint                             null comment '父标签 id',
    isParent   tinyint                            null comment '0-父标签| 1-不是父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    constraint unildx_tagName
        unique (tagName)
)
    comment '标签表';

create index idx_userId
    on tag (userId);

/*队伍表*/
create table team
(
    id             bigint auto_increment comment 'id' primary key,
    name           varchar(256)                              not null comment '队伍名称',
    description    varchar(1024)                                 null comment '队伍描述',
    maxNum		   int			default 1					not null comment '最大人数',
    expireTime	   datetime                                      null comment '过期时间',
    userId		   bigint											comment '用户Id',
    createTime     datetime     default CURRENT_TIMESTAMP        null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP        null on update CURRENT_TIMESTAMP comment '更新时间',
    status         int          default 0                    not null comment '0-公开、1-加密',
    password       varchar(512)                                  null comment '密码',
    isDelete       tinyint      default 0                    not null comment '是否删除'
)
    comment '队伍表';

/*用户队伍关系表*/
create table user_team
(
    id             bigint auto_increment comment 'id' primary key,
    userId		     bigint											comment '用户Id',
    teamId		     bigint											comment '队伍Id',
    joinTime	     datetime                   null comment '加入时间',
    createTime     datetime     default CURRENT_TIMESTAMP        null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP        null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                    not null comment '是否删除'
)
    comment '用户队伍表';

/*创建粉丝关注表*/
create table follow_relationship
(
    id          bigint auto_increment primary key ,
    follower_id bigint                          not null comment '关注者 id',
    followed_id bigint                          not null comment '被关注者 id',
    create_time datetime default CURRENT_TIMESTAMP null comment '关注时间',
    is_delete   tinyint default 0               null comment '0 - 未删除，1 - 已删除'
)
    comment '粉丝关注表';

/*创建用户私聊消息表*/
create table chat_messages
(
    chat_id     bigint auto_increment primary key ,
    sender_id   bigint                          not null comment '发送者 id',
    receiver_id bigint                          not null comment '接收者 id',
    message     text                            null     comment '消息内容',
    timestamp   datetime default CURRENT_TIMESTAMP not null comment '发送时间',
    read_status tinyint default 0               null comment '0 - 未读，1 - 已读'
)
    comment '用户私聊消息表';






