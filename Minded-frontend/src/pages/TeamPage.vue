<template>
  <van-pull-refresh v-model="isLoadingDog" :head-height="80" @refresh="onTabChange(active)">
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
      <!--  搜索框-->
      <van-search v-model="searchText" @search="onSearch" placeholder="搜索队伍" />
      <!--  标签分割-->
      <van-tabs v-model:active="active" @click="changeTeamType(active)">
        <van-tab title="公开" name="public"></van-tab>
        <van-tab title="加密" name="private"></van-tab>
      </van-tabs>
      <div style="margin-bottom: 16px" />
      <van-button class="add-button" type="primary" icon="plus" @click="toAddTeam" />
      <van-empty v-if="!teamList || teamList.length < 1" description="暂无更多队伍"/>
      <!--    队伍卡片展示-->
      <team-card-list :team-list="teamList"/>
      <van-divider />
      <van-pagination
          v-model="currentPage"
          :total-items="totalItem"
          :show-page-size="showPageSize"
          :items-per-page="itemsPerPage"
          force-ellipses
          @change="onTabChange(active)"
      />
    </div>
  </van-pull-refresh>



</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast} from "vant";
import {loadingDog} from "../static/img/ossImgs.ts";
import TeamCardList from "../components/TeamCardList.vue";
import {useRouter} from "vue-router";

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
const active = ref('public');
// 搜索关键词
const searchText = ref('');

const isLoadingDog = ref(false);

const changeTeamType = (name: string) => {
  // 将分页调整回第一页
  currentPage.value = 1;
  onTabChange(name);
}

/**
 * 根据状态动态查询数据
 */
const onTabChange = (name: string) => {
  if (name === 'public') { // 查公开
    listTeam(searchText.value, 0);
  } else { // 查加密
    listTeam(searchText.value, 1);
  }
}

onMounted(() => {
  // 按照类型搜索队伍
  listTeam();
})

// 搜索功能（根据条件搜索队伍）
const onSearch = (val: string) => {
  if(active.value === 'public') {
    // 查询公开的队伍
    listTeam(val, 0);
  } else {
    // 查询加密的队伍
    listTeam(val,1);
  }
}

const listTeam = async (val = '', status = 0) => {
  isLoadingDog.value = true;
  const res = await myAxios.get('/team/list',{
    params: {
      searchText: val,
      status,
      pageNum: currentPage.value,
      pageSize: itemsPerPage.value,
    }
  });
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

// 跳转到创建队伍页
const toAddTeam = () => {
  router.push({
    path: '/team/add'
  })
}


</script>

<style scoped>

</style>