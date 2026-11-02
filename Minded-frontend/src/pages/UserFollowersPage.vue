<template>
  <van-pull-refresh v-model="isLoadingDog" :head-height="80" @refresh="loadData">
    <!-- 下拉提示，通过 scale 实现一个缩放效果 -->
    <template #pulling="props">
      <img
          class="doge"
          :src="loadingDog"
          :style="{ transform: `scale(${props.distance / 80})` }"
          alt="图片加载失败"
      />
    </template>
    <!--    释放提示-->
    <template #loosing>
      <img
          class="doge"
          :src="loadingDog"
          alt="图片加载失败"
      />
    </template>
    <!-- 加载提示 -->
    <template #loading>
      <img
          class="doge"
          :src="loadingDog"
          alt="加载图片失败"
      />
    </template>
    <div>
      <!-- 用户卡片列表 -->
      <user-card-list :user-list="userListPage" :loading="loading"/>
      <!-- 分页 -->
      <van-divider />
      <van-pagination
          v-model="currentPage"
          :total-items="totalItem"
          show-page-size="3"
          :items-per-page="itemsPerPage"
          force-ellipses
      />
    </div>
    <van-empty v-if="!userList || userList.length < 1" description="数据为空" />
  </van-pull-refresh>

</template>

<script setup lang="ts">
import {onMounted, ref, watch} from 'vue';
import myAxios from '../plugins/myAxios.ts';
import UserCardList from "../components/UserCardList.vue";
import {showFailToast} from "vant";
import {loadingDog} from "../static/img/ossImgs.ts";

const userList = ref([]); // 用户集合

const loading = ref(true); // 骨架屏加载效果

const currentPage = ref(1); // 当前页码
const totalItem = ref(8); // 总记录数量
const itemsPerPage = ref(8); // 每页展示的记录数

const userListPage = ref();

const isLoadingDog = ref(false);  // 下拉刷新图标

// 组件加载完成后查询用户的关注
const loadData = async () => {
  loading.value = true;
  isLoadingDog.value = true;
  const res = await myAxios.get('/friends/myFollower',{
    params: {
      pageNum: currentPage.value,
      pageSize: itemsPerPage.value,
    }
  })
  if (res.data?.records) {
    res.data?.records.forEach(user => {
      if (user.tags) {
        user.tags = JSON.parse(user.tags); // 解析后端返回的json字符串
      }
    });
    userList.value = res.data?.records;
    userListPage.value = res.data?.records;
    totalItem.value = res.data.total;
  } else {
    showFailToast("请求失败! " + res?.description);
  }
  loading.value = false;
  isLoadingDog.value = false;
  window.scrollTo({top: 0})
};

// 首次加载
onMounted(() => {
  loadData();
});

// 页码变化时重新加载
watch(currentPage, () => {
  loadData();
});
</script>

<style scoped>

</style>
