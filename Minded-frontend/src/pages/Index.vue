<template>
  <van-cell center title="❤️️心动模式❤️">
    <template #right-icon>
      <van-switch v-model="isMatchMode"/>
    </template>
  </van-cell>
  <van-pull-refresh v-model="isLoadingDog" :head-height="80" @refresh="loadData">
    <template #pulling="props">
      <img class="doge" :src="loadingDog"
           :style="{ transform: `scale(${props.distance / 80})` }" alt="图片加载失败"/>
    </template>
    <template #loosing>
      <img class="doge" :src="loadingDog" alt="图片加载失败"/>
    </template>
    <template #loading>
      <img class="doge" :src="loadingDog" alt="加载图片失败"/>
    </template>
    <div>
      <user-card-list :user-list="userListPage" :loading="loading"/>
      <van-divider/>
      <van-pagination
          v-model="currentPage"
          :total-items="totalItem"
          show-page-size="3"
          :items-per-page="itemsPerPage"
          force-ellipses
      />
    </div>
    <van-empty v-if="!userList || userList.length < 1" description="数据为空"/>
  </van-pull-refresh>
</template>

<script setup lang="ts">
import {onMounted, ref, watch} from "vue";
import myAxios from "../plugins/myAxios.ts";
import UserCardList from "../components/UserCardList.vue";
import {showFailToast} from "vant";
import {useRouter} from "vue-router";
import {loadingDog} from "../static/img/ossImgs.ts";

const router = useRouter();

const userList = ref<any[]>([]);
const isMatchMode = ref<boolean>(false);
const loading = ref(true);
const currentPage = ref(1);
const totalItem = ref(8);
const itemsPerPage = ref(8);
const userListPage = ref<any[]>([]);
const isLoadingDog = ref(false);

const loadData = async () => {
  loading.value = true;
  isLoadingDog.value = true;
  if (isMatchMode.value) {
    const num = 8;
    const userListData = await myAxios.get("/user/match", {
      params: {
        num,
        pageSize: itemsPerPage.value,
        pageNum: currentPage.value,
      },
    });
    if (userListData?.data?.records) {
      userListData.data.records.forEach((user: any) => {
        if (user.tags) user.tags = JSON.parse(user.tags);
      });
      userList.value = userListData.data.records;
      userListPage.value = userListData.data.records;
      totalItem.value = userListData.data.total;
    } else {
      showFailToast("暂无与你匹配的伙伴");
      isMatchMode.value = false;
    }
  } else {
    const response = await myAxios.get("/user/recommend", {
      params: {
        pageNum: currentPage.value,
        pageSize: itemsPerPage.value,
      },
    });
    if (response.data?.records) {
      response.data.records.forEach((user: any) => {
        if (user.tags) user.tags = JSON.parse(user.tags);
      });
      userList.value = response.data.records;
      userListPage.value = response.data.records;
      totalItem.value = response.data.total;
    } else {
      showFailToast("请求失败");
      await router.push("/user/login");
    }
  }
  loading.value = false;
  isLoadingDog.value = false;
  window.scrollTo({top: 0});
};

onMounted(() => {
  loadData();
});

// 切换心动模式：
// - 不在第 1 页时，只改 currentPage，让 watch(currentPage) 去 loadData（一次）
// - 已在第 1 页时，直接 loadData（一次）
// 两条路径互斥，不会出现并发请求
watch(isMatchMode, () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1;   // 由 watch(currentPage) 触发 loadData
  } else {
    loadData();
  }
});

// 翻页（含心动模式重置页码）→ 唯一的 loadData 触发点
watch(currentPage, () => {
  loadData();
});
</script>