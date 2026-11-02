<!--用户加入的队伍列表-->
<template>
  <van-pull-refresh v-model="isLoadingDog" :head-height="80" @refresh="listTeam">
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
    <div id="teamPage">
      <van-tabs v-model:active="active" @click="changeTeamType(active)">
        <van-tab title="当前队伍" name="current"></van-tab>
        <van-tab title="历史队伍" name="history"></van-tab>
      </van-tabs>
      <div style="margin-bottom: 16px" />
      <van-empty v-if="!teamList || teamList.length < 1" description="暂无加入队伍"/>
      <!--    队伍卡片展示-->
      <team-card-list :team-list="teamList"/>
      <van-divider />
      <van-pagination
          v-model="currentPage"
          :total-items="totalItem"
          :show-page-size="showPageSize"
          :items-per-page="itemsPerPage"
          force-ellipses
          @change="changeTeamType(active)"
      />
    </div>
  </van-pull-refresh>



</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast} from "vant";
import TeamCardList from "../components/TeamCardList.vue";
import {useRouter} from "vue-router";
import {loadingDog} from "../static/img/ossImgs.ts";

const router = useRouter();

const teamList = ref([]);
// 当前页码
const currentPage = ref(1);
// 总记录数
const totalItem = ref(8);
// 展示的页码
const showPageSize = ref(3);
// 每页展示的记录数
const itemsPerPage = ref(8);
// 用于切换查找公开队伍还是加密队伍
const active = ref('current');

const isLoadingDog = ref(false);

// 切换上方状态栏切换
const changeTeamType = (name: string) => {
  // 将分页调整回第一页
  currentPage.value = 1;
  active.value = name;
  listTeam();
}

onMounted(() => {
  // 按照类型搜索队伍
  listTeam();
})

const listTeam = async (val = '') => {
  isLoadingDog.value = true;
  let res;
  if (active.value === 'current') {
    res = await  myAxios.get('/team/list/join', {
      params: {
        searchText: val,
        pageNum: currentPage.value,
        pageSize: itemsPerPage.value,
      }
    });
  } else {
    res = await myAxios.get('/team/list/join',{
      params: {
        searchText: val,
        timeOut: true,
        pageNum: currentPage.value,
        pageSize: itemsPerPage.value,
      }
    });
  }
  if (res.code === 200) {
    teamList.value = res.data?.records;
    totalItem.value = res?.data?.total;
    itemsPerPage.value = res?.data?.size;
    currentPage.value = res?.data?.current;
  } else {
    showFailToast("加载失败, " + res?.description);
  }
  isLoadingDog.value = false;
}

</script>

<style scoped>

</style>