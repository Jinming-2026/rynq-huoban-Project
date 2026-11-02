import {createStore} from 'vuex';

const store = createStore({
    state() {
        return {
            team: { // 当前查看的详情队伍
                id: 0,
                name: '',
                description: '',
                avatar: '',                 // 补：TeamCardList / TeamDetailsPage 会读
                expireTime: new Date(),
                maxNum: 0,
                status: 0,
                createTime: new Date(),
                updateTime: new Date(),
                createUser: {},             // 统一用 createUser
                hasJoin: false,
                hasJoinNum: 0
            },
            user: { // 当前查看的详情用户
                id: 0,
                username: '',
                userAccount: '',
                avatarUrl: '',
                description: '',
                gender: 0,
                phone: '',
                email: '',
                userStatus: 0,
                userRole: 0,
                tags: [],
                createTime: new Date()
            },
            loginUser: { // 当前登录用户
                id: 0,
                username: '',
                userAccount: '',
                avatarUrl: '',
                profile: '',
                gender: 0,
                phone: '',
                email: '',
                userStatus: 0,
                userRole: 0,
                tags: [],
                createTime: new Date()
            },
            chatUserId: { // 当前私聊用户 id
                id: 0
            },
            chatUserName: { // 当前私聊用户姓名
                name: ''
            }
        };
    },
    mutations: {
        // 更新队伍数据（兼容后端拼写：creatUser → createUser）
        updateTeam(state, teamData) {
            if (!teamData) return;
            const normalized: any = {...teamData};
            if (normalized.creatUser && !normalized.createUser) {
                normalized.createUser = normalized.creatUser;
            }
            state.team = {...state.team, ...normalized};
        },
        updateUser(state, userData) {
            state.user = {...state.user, ...userData};
        },
        updateLoginUser(state, userData) {
            state.loginUser = {...state.loginUser, ...userData};
        },
        updateChatUserId(state, userData) {
            state.chatUserId = {...state.chatUserId, ...userData};
        },
        updateChatUserName(state, name) {
            state.chatUserName = {...state.chatUserName, name};
        },
    }
});

export default store;