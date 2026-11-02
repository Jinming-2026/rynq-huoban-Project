<!-- 用户卡片列表公共组件 -->
<template>
  <!-- 加载中：展示骨架屏 -->
  <template v-if="props.loading">
    <van-skeleton v-for="n in 3" :key="n" title avatar :row="3"/>
  </template>
  <!-- 加载完成：展示用户卡片 -->
  <template v-else>
    <van-card
        v-for="user in props.userList"
        :key="user.id"
        :desc="user.profile"
        :title="user.username == null ? '新用户' : user.username"
        :thumb="user.avatarUrl == null ? userImg:user.avatarUrl"
        @click="userDetails(user)"
        class="custom-card"
    >
      <!-- 用户标签 -->
      <template #tags>
        <van-tag
            style="margin-right: 8px; margin-top: 8px"
            plain type="primary"
            v-for="tag in user.tags"
            >
          {{ tag }}
        </van-tag>
      </template>
      <template #footer>
        <van-button size="mini">联系我</van-button>
      </template>
    </van-card>
  </template>
</template>

<script setup lang="ts">
import {UserType} from "../models/user";
import {useRouter} from "vue-router";
import {useStore} from "vuex";
import {userImg} from "../static/img/ossImgs.ts";

const router = useRouter();

const store = useStore();

// 定义组件属性
interface UserCardListProps {
  loading: boolean;
  userList: UserType[];
}

const props = withDefaults(defineProps<UserCardListProps>(), {
  loading: true, // 默认显示加载中（骨架屏）
  userList: () => [],
});

/**
 * 查看用户详情
 */
const userDetails = (user: UserType) => {
  store.commit('updateUser', user); // 更新存储的当前用户数据
  router.push('/user/details')
}

</script>

<style scoped>
.custom-card {
  border: 1px solid #e0e0e0; /* 轻微的灰色边框 */
  background-color: white; /* 白色背景 */
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); /* 轻微的阴影 */
  transition: box-shadow 0.3s ease-in-out; /* 阴影过渡效果 */
  cursor: pointer;
  color: #333; /* 文字颜色 */
  margin-bottom: 10px; /* 增加卡片底部的外边距 */
}

.custom-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15); /* 鼠标悬停时更深的阴影 */
}
</style>
