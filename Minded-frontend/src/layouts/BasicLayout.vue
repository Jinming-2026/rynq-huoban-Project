<template>
  <!-- 顶部栏（登录/注册/重置密码等页面隐藏） -->
  <van-nav-bar
      v-if="showLayout"
      :title="title"
      left-arrow
      @click-left="onClickLeft"
      @click-right="onClickRight"
      class="fixed-top-bar">
    <template #right>
      <van-icon name="search" size="18"/>
    </template>
  </van-nav-bar>

  <!-- 内容 -->
  <div id="content" :class="{ 'no-top-bar': !showLayout }">
    <router-view/>
  </div>

  <!-- 底部菜单栏 -->
  <van-tabbar v-if="showLayout" v-model="active" active-color="#4169E1">
    <van-tabbar-item to="/" name="index" icon="home-o">主页</van-tabbar-item>
    <van-tabbar-item to="/team" name="team" icon="search">队伍</van-tabbar-item>
    <van-tabbar-item to="/chat" name="chat" icon="friends-o">消息</van-tabbar-item>
    <van-tabbar-item to="/user" name="user" icon="setting-o">个人</van-tabbar-item>
  </van-tabbar>

</template>

<script setup lang="ts">
import {computed, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import store from "../store";

const router = useRouter();
const route = useRoute();
const DEFAULT_TITLE = '小R同学伙伴匹配中心';

// 是否展示导航栏与底部 Tabbar（登录/注册/重置密码等页面隐藏）
const showLayout = computed(() => !route.meta.hideLayout);

// 页面标题：私聊页显示聊天对象姓名，其余显示路由 meta 标题
const title = computed(() => {
  if (route.path === '/chat/private') {
    return store.state.chatUserName.name || DEFAULT_TITLE;
  }
  return (route.meta.title as string) || DEFAULT_TITLE;
});

const active = ref('index');

// 根据路由切换底部导航栏高亮
watch(() => route.path, (toPath) => {
  if (toPath === '/') {
    active.value = 'index';
  } else if (toPath === '/team') {
    active.value = 'team';
  } else if (toPath === '/chat') {
    active.value = 'chat';
  } else if (toPath === '/user') {
    active.value = 'user';
  }
}, { immediate: true });

const onClickLeft = () => {
  // 当处于主页四个主页面时不允许返回上一页
  const nowPath = route.path;
  if (nowPath == '/' || nowPath == '/team' || nowPath == '/user' || nowPath == '/chat') {
    return;
  } else {
    if (nowPath == '/user/interest') {
      router.push({path: '/user'})
      return;
    }
    // 路由跳转回到上一页
    router.back();
  }
};
const onClickRight = () => {
  // 路由跳转到 search 页面
  router.push('/search')
};

</script>

<style scoped>
#content {
  padding-top: 46px; /* 确保内容不会被固定的顶部栏遮挡 */
  padding-bottom: 50px;
}

/* 隐藏顶部栏时取消顶部内边距 */
#content.no-top-bar {
  padding-top: 0;
}
</style>
