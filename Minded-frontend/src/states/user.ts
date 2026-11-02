import {UserType} from "../models/user";

let currentUser: UserType | null = null;
let chatUser: UserType | null = null;

const setCurrentUserState = (user: UserType) => {
    currentUser = user;
}

const getCurrentUserState = (): UserType | null => {
    return currentUser;
}

const clearCurrentUserState = () => {
    currentUser = null;
}

const setChatUserState = (user: UserType) => {
    chatUser = user
}

const getChatUserState = (): UserType | null => {
    return chatUser;
}

export {
    setCurrentUserState,
    getCurrentUserState,
    clearCurrentUserState,
    setChatUserState,
    getChatUserState,
}
