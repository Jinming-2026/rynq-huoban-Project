import {getChatUserState, getCurrentUserState, setCurrentUserState} from "../states/user.ts";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast} from "vant";

/**
 * 获取当前登录用户
 */
export const getCurrentUser = async () => {
    const currentUser = getCurrentUserState(); // 获取内存中的当前登录用户
    if (currentUser) {
        return currentUser;
    }
    // 内存中没有则从远程获取
    const res = await myAxios.get('/user/current');
    if (res?.code === 200) {
        setCurrentUserState(res.data); // 将当前登录用户存到内存中
        return res.data;
    }
    return null;
}

/**
 * 获取当前聊天用户
 */
export const getChatUser = async () => {
    const chatUser = getChatUserState();
    if (chatUser) {
        return chatUser;
    } else {
        showFailToast("获取聊天用户失败");
    }
}