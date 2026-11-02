<!--队伍成员页面-->
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
      <user-card-list :user-list="memberList" :loading="loading"/>
    </div>
    <van-empty v-if="!memberList || memberList.length < 1" description="数据为空" />
  </van-pull-refresh>

</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import UserCardList from "../components/UserCardList.vue";
import myAxios from "../plugins/myAxios.ts";
import {loadingDog} from "../static/img/ossImgs.ts";
import {useStore} from "vuex";
import {showFailToast} from "vant";


const memberList = ref([]); // 用户集合

const loading = ref(true); // 骨架屏加载效果

const isLoadingDog = ref(false);  // 下拉刷新图标
// 获取队伍 id
const store = useStore();
const teamId = store.state.team.id;

const loadData = () => {
  listTeamMember();
}

// 获取队伍成员列表
const listTeamMember = async () => {
  isLoadingDog.value = true;
  const res = await myAxios.get('/team/member',{
    params: {
      teamId: teamId,
    }
  })
  if (res?.code === 200) {
    // 解析后端返回来的 JSON 字符串
    res.data.forEach(member => {
      if (member.tags) {
        member.tags = JSON.parse(member.tags);
      }
    })
    memberList.value = res?.data;
  } else {
    showFailToast("加载失败！" + res?.description);
  }
  isLoadingDog.value = false;
  loading.value = false;
}

onMounted(() => {
  loadData();
})
</script>

<style scoped>

</style>