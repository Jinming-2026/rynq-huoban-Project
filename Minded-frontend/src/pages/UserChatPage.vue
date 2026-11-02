<!--用户私聊类-->
<script setup lang="ts">

import {useRouter} from "vue-router";
import {useStore} from "vuex";
import {nextTick, onMounted, onUnmounted, ref} from "vue";
import {getCurrentUser} from "../service/user.ts";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast} from "vant";
import {userImg} from "../static/img/ossImgs.ts";
import {UserType} from "../models/user";


const store = useStore();
const router = useRouter();

const chatUser = ref();
// 聊天对象 id
const chatUserId = ref(store.state.chatUserId.id);
const loginUser = ref<UserType | null>(null);
// WebSocket 连接
const ws = ref<WebSocket | null>(null);
// WebSocket 连接状态
const isWsConnected = ref(false);

onMounted(async () => {
  await getChatUser(); // 获取当前聊天用户对象
  loginUser.value = await getCurrentUser();
  // 加载历史聊天消息
  await getHistoryMessages();
  // 滚动到底部
  scrollToBottom();
  // 连接 WebSocket TODO
  ws.value = new WebSocket(`ws://localhost:8080/api/chat/${chatUserId.value}`);
  // ws.value = new WebSocket(`ws://120.55.73.95:9090/api/chat/${chatUserId.value}`);
  ws.value.onopen = () => {
    isWsConnected.value = true;   // WebSocket 建立连接成功
  }
  // 发送消息
  ws.value.onmessage = (msg) => {
    const onMessage = JSON.parse(msg.data);   // 解析 JSON 字符串
    // 效验发送对象
    if ((onMessage.senderId === loginUser.value?.id && onMessage.receiverId === chatUserId.value) ||
        (onMessage.receiverId === loginUser.value?.id && onMessage.senderId === chatUserId.value)) {
      messages.value.push(onMessage);
    }
    scrollToBottom();
  }
  // 关闭连接
  ws.value.onclose = () => {
    isWsConnected.value = false;
  }
})

// 当组件摧毁时，自动执行该回调函数
onUnmounted(() => {
  if (ws.value) {
    ws.value.close();
  }
})

interface Message {
  senderId: number;   // 发送者id
  receiverId: number; // 接收者id
  message: string;    // 发送消息内容
  timestamp: string;  // 发送时间
  avatar: string;     // 头像url
}
const messages = ref<Message[]>([]);
const newMessage = ref('');
// HTMLElement（用于存储一个 HTML 元素的引用）或 null（初始化时值为空）
const messageList = ref<HTMLElement | null>(null);

// 发送消息
const sendMessage = () => {
  // 效验参数
  if (newMessage.value && isWsConnected.value && ws.value && loginUser.value) {
    const message = {
      senderId: loginUser.value.id,
      receiverId: chatUserId.value,
      message: newMessage.value,
      timestamp: new Date().toISOString(),  // 使用 ISO 格式的时间戳 例：YYYY-MM-DDTHH:mm:ss.sssZ
      avatar: loginUser.value.avatarUrl
    }
    ws.value.send(JSON.stringify(message));
    messages.value.push(message);
    newMessage.value = '';
    scrollToBottom();
  }
}

/**
 * 日期格式转换
 * @param timestamp
 */
const formatTime = (timestamp: string): string => {
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  const hour = date.getHours().toString().padStart(2, '0');
  const minute = date.getMinutes().toString().padStart(2, '0');
  const second = date.getSeconds().toString().padStart(2, '0');
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
};

/**
 * 获取当前私聊用户对象
 */
const getChatUser = async () => {
  const id = store.state.chatUserId.id;
  const res = await myAxios.get(`/user/${id}`);
  if (res?.code === 200) {
    if (res?.data) {
      res.data.tags = JSON.parse(res.data.tags); // 解析后端返回的json字符串
    }
    chatUser.value = res.data;
  } else {
    showFailToast('加载用户信息失败' + res?.description)
  }
}

/**
 * 获取历史聊天消息
 */
const getHistoryMessages = async () => {
  const res = await myAxios.get('/chat/history',{
    params: {
      receiverId: chatUserId.value,
    }
  });
  if (res?.code === 200 ){
    messages.value = res?.data;
  } else {
    showFailToast("获取历史消息失败");
  }
}

/**
 * 点击用户头像进入用户详情页
 */
const toUserDetail = (user: UserType) => {
  store.commit('updateUser', user);
  router.push('/user/details')
}

/**
 * 滚动到底部
 */
const scrollToBottom = () => {
  nextTick(() => {
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight;
    }
  });
};


</script>

<template>
  <div class="chat-container" v-if="loginUser">
    <div class="chat-messages" ref="messageList">
      <van-cell-group>
        <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message', { 'message--self': message.senderId === loginUser.id }]"
        >
          <div
              v-if="message.senderId !== loginUser.id"
              class="avatar-container"
              @click="toUserDetail(chatUser)"
          >
            <img class="avatar" :src="message.avatar == '' ? userImg:message.avatar" alt="avatar"/>
          </div>
          <div
              v-else
              class="avatar-container"
          >
            <img class="avatar" :src="loginUser.avatarUrl == null ? userImg:loginUser.avatarUrl" alt="avatar"/>
          </div>
          <div class="message-content-container">
            <div class="message-content">
              <div class="message-text">{{ message.message }}</div>
            </div>
            <!-- 仅在最后一条消息显示时间戳 -->
            <div v-if="index === messages.length - 1" class="message-time">
              {{ formatTime(message.timestamp) }}
            </div>
          </div>
        </div>
      </van-cell-group>
    </div>
    <div class="chat-input">
      <textarea class="auto-wrap-textarea" rows="3" cols="40"
        v-model="newMessage"></textarea>
      <van-button type="primary" @click="sendMessage">↗</van-button>
    </div>
  </div>
</template>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 85vh; /* 将高度改为 85vh */
  background-color: #ffffff;
}

.auto-wrap-textarea {
  width: 100%;          /* 设置宽度 */
  max-width: 400px;     /* 最大宽度 */
  height: auto;         /* 高度自适应 */
  overflow-wrap: break-word; /* 自动换行 */
  white-space: pre-wrap; /* 保留空格和换行 */
  word-wrap: break-word; /* 允许长单词换行 */
  resize: vertical;     /* 允许用户垂直调整大小 */
  padding: 10px;
  font-size: 16px;
  line-height: 1.5;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background-color: #ffffff;
}

.message {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
}

.message--self {
  flex-direction: row-reverse;
}

.avatar-container {
  position: relative;
  cursor: pointer;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin: 0 10px;
}

.message-content-container {
  display: flex;
  flex-direction: column; /* 确保消息内容和时间戳垂直排列 */
  align-items: flex-start;
  max-width: 70%;
}

.message--self .message-content-container {
  align-items: flex-end; /* 当前用户发送的消息对齐右边 */
}

.message-content {
  /*padding: 10px;*/
  max-width: 100%;             /* 气泡最大宽度 */
  padding: 10px 10px;           /* 内边距 */
  background-color: #e0f7fa;    /* 气泡背景颜色 */
  color: #333;                  /* 文字颜色 */
  border-radius: 15px;          /* 圆角 */
  font-size: 16px;
  line-height: 1.5;
  word-wrap: break-word;        /* 自动换行 */
  overflow-wrap: break-word;    /* 长单词自动换行 */
  white-space: pre-wrap;        /* 保留用户输入的空格和换行 */
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.1); /* 阴影效果 */
  position: relative;
  margin: 5px 0;                /* 外边距 */
}

.message--self .message-content {
  background-color: #b0e57c; /* 将当前用户消息背景颜色设置为绿色 */
}

.message-text {
  margin-bottom: 0;
}

.message-time {
  font-size: 0.75rem;
  color: #888;
  align-self: flex-end; /* 使时间戳在消息框的外部，并右对齐 */
}

.chat-input {
  display: flex;
  height: 45px;
  padding: 10px;
  background-color: #f7f7f7;
  position: sticky;
}

.input-field {
  flex: 1;
  margin-right: 10px;
  background-color: #ffffff;
  border-radius: 20px;
  padding-left: 10px;
}

.van-field__control {
  background-color: transparent !important;
}
</style>