import TeamPage from "../pages/TeamPage.vue";
import Index from "../pages/Index.vue";
import UserPage from "../pages/UserPage.vue";
import SearchPage from "../pages/SearchPage.vue";
import UserEditPage from "../pages/UserEditPage.vue";
import SearchResultPage from "../pages/SearchResultPage.vue";
import UserLoginPage from "../pages/UserLoginPage.vue";
import UserRegisterPage from "../pages/UserRegisterPage.vue";
import UserUpdatePage from "../pages/UserUpdatePage.vue";
import TeamAddPage from "../pages/TeamAddPage.vue";
import UserCreateTeamPage from "../pages/UserCreateTeamPage.vue";
import UserUpdatePwdPage from "../pages/UserUpdatePwdPage.vue";
import UserJoinTeamPage from "../pages/UserJoinTeamPage.vue";
import TeamDetailsPage from "../pages/TeamDetailsPage.vue";
import TeamMemberPage from "../pages/TeamMemberPage.vue";
import TeamUpdatePage from "../pages/TeamUpdatePage.vue";
import UserDetailsPage from "../pages/UserDetailsPage.vue";
import UserFansPage from "../pages/UserFansPage.vue";
import UserFollowersPage from "../pages/UserFollowersPage.vue";
import UserTagsPage from "../pages/UserTagsPage.vue";
import UserTagsAddPage from "../pages/UserTagsAddPage.vue";
import ChatPage from "../pages/ChatPage.vue";
import UserChatPage from "../pages/UserChatPage.vue";
import UserResetPwdPage from "../pages/UserResetPwdPage.vue";

const routes = [
    { path: '/', meta: {title: '小R同学伙伴匹配中心'}, component: Index },
    { path: '/team', meta: {title: '找队伍'}, component: TeamPage },
    { path: '/team/add', meta: {title: '创建队伍'}, component: TeamAddPage },
    { path: '/team/details', meta: {title: '队伍详情'}, component: TeamDetailsPage },
    { path: '/team/member', meta: {title: '成员列表'}, component: TeamMemberPage },
    { path: '/team/update', meta: {title: '修改队伍'}, component: TeamUpdatePage },
    { path: '/user', meta: {title: '个人信息'}, component: UserPage },
    { path: '/search', meta: {title: '找伙伴'}, component: SearchPage },
    { path: '/user/edit', meta: {title: '修改信息'}, component: UserEditPage },
    { path: '/user/edit/password', meta: {title: '修改密码'}, component: UserUpdatePwdPage },
    { path: '/user/list', meta: {title: '伙伴列表'}, component: SearchResultPage },
    { path: '/user/login', meta: {title: '登录', hideLayout: true}, component: UserLoginPage },
    { path: '/user/register', meta: {title: '注册', hideLayout: true}, component: UserRegisterPage },
    { path: '/user/update', meta: {title: '修改信息'}, component: UserUpdatePage },
    { path: '/user/details', meta: {title: '用户详情'}, component: UserDetailsPage },
    { path: '/user/create/team', meta: {title: '创建的队伍'}, component: UserCreateTeamPage },
    { path: '/user/join/team', meta: {title: '加入的队伍'}, component: UserJoinTeamPage },
    { path: '/user/fans', meta: {title: '粉丝列表'}, component: UserFansPage },
    { path: '/user/followers', meta: {title: '关注列表'}, component: UserFollowersPage },
    { path: '/user/interest', meta: {title: '标签管理'}, component: UserTagsPage },
    { path: '/user/reset', meta: {title: '忘记密码', hideLayout: true}, component: UserResetPwdPage },
    { path: '/tag/add', meta: {title: '创建标签'}, component: UserTagsAddPage },
    { path: '/chat', meta: {title: '消息'}, component: ChatPage },
    { path: '/chat/private', meta: {title: '私聊'}, component: UserChatPage },
]

export default routes;