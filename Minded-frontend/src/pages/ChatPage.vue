<!--好友列表页-->
<script setup lang="ts">
import {onMounted, ref} from 'vue';
import myAxios from "../plugins/myAxios.ts";
import {showFailToast} from "vant";
import {userImg} from "../static/img/ossImgs.ts";
import {useRouter} from "vue-router";
import {useStore} from "vuex";

const router = useRouter();
const store = useStore();

const isLoadingDog = ref(false);  // 下拉刷新图标
const loading = ref(true); // 骨架屏加载效果

interface Friend {
  id: number,
  name: string,
  timestamp: number,     // 最后一条消息的时间
  avatar: string,
  hasUnRead: boolean,  // 是否有未读消息
  oneNewMessage: string   // 用户之间最新的一条消息
}

const friends = ref<Friend[]>([]); // 好友集合

onMounted(async () => {
  await loadData();
})
/**
 * 获取用户的所有好友（关注 + 粉丝）
 */
const loadData = async () => {
  loading.value = true;
  isLoadingDog.value = true;
  const res = await myAxios.get('/friends/all');
  if (res?.code === 200) {
    friends.value = res.data;
  } else {
    showFailToast("加载好友列表失败！" + res?.description);
  }
  loading.value = false;
  isLoadingDog.value = false;
}

/**
 * 格式化最后一条消息时间
 * @param timestamp 时间戳（秒或毫秒）
 */
const formatTime = (timestamp: number): string => {
  if (!timestamp) return '';
  const date = new Date(timestamp < 1e12 ? timestamp * 1000 : timestamp);
  const pad = (n: number) => n.toString().padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

/**
 * 进入私聊
 */
const openChat = (friend: Friend) => {
  store.commit('updateChatUserId', friend);   // 更新当前私聊用户id
  store.commit('updateChatUserName',friend.name);  // 更新当前私聊用户姓名
  router.push('/chat/private');
}
</script>

<template>
  <div class="friend-list-container" >
    <van-cell-group v-if="friends.length > 0"  >
      <van-cell
          v-for="(friend, index) in friends"
          :key="index"
          :title="friend.name"
          :label="friend.oneNewMessage == '' ? '' : friend.oneNewMessage"
          clickable
          @click="openChat(friend)"
      >
        <template #icon>
          <img :src="friend.avatar == null ? userImg:friend.avatar" class="avatar"  alt=""/>
          <div class="timeClass">{{ formatTime(friend.timestamp) }}</div>
          <!-- 显示未读消息红点 -->
          <div v-if="friend.hasUnRead" class="unread-dot"></div>
        </template>
      </van-cell>
    </van-cell-group>
    <van-empty v-else description="暂无关注用户和粉丝" />
  </div>
</template>

<style scoped>
.friend-list-container {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  overflow: auto;
  background-color: #ffffff;
  padding-bottom: 12px; /* 确保底部空间足够，防止被底部布局遮住 */
}

/* 深度选择 van-cell 组件内的 label */
:deep(.van-cell__label) {
  max-width: 30ch;           /* 设置 label 的最大宽度 */
  white-space: nowrap;        /* 禁止换行 */
  overflow: hidden;           /* 隐藏超出部分 */
  text-overflow: ellipsis;    /* 超出内容显示省略号 */
  color: #666;                /* 可选，设置文字颜色 */
  font-size: 14px;            /* 可选，设置文字大小 */
}

.avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  position: relative; /* 相对定位，用于未读消息提示 */
  left: 5px;
  margin-right: 15px;
}

.unread-dot {
  position: absolute;
  top: 30%;
  right: 10px; /* 调整位置确保圆点完全显示在右侧 */
  transform: translateY(-50%);
  background-color: red;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.timeClass {
  position: absolute;
  top: 60%;
  right: 5px; /* 调整位置确保完全显示在右侧 */
  transform: translateY(-50%);
  width: 115px;
  height: 10px;
  border-radius: 50%;
  font-weight: normal;
  font-size: 12px;
}

/* 修改 title 的字体大小并加粗 */
:deep(.van-cell__title) {
  font-size: 15px; /* 调整字体大小 */
  font-weight: bold; /* 加粗 */
}
:deep(.van-cell__label) {
  font-size: 12px; /* 确保 label 的字体大小不变 */
  font-weight: normal; /* 确保 label 的字体粗细不变 */
}
</style>