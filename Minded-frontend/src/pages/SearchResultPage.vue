<!-- 搜索结果页 -->
<template>
  <van-empty v-if="!userList || userList.length < 1" description="搜索结果为空"/>
  <div v-else>
    <!-- 用户卡片列表 -->
    <user-card-list :user-list="userListPage" :loading="loading"/>
    <!-- 分页 -->
    <van-divider />
    <van-pagination
        v-model="currentPage"
        :total-items="totalItem"
        show-page-size="3"
        force-ellipses
        @change="changePage"
    />
  </div>

</template>

<script setup>
import {onMounted, ref} from 'vue';
import {useRoute} from "vue-router";
import myAxios from '../plugins/myAxios.ts';
import qs from 'qs';
import UserCardList from "../components/UserCardList.vue";
import {showFailToast, showSuccessToast} from "vant";

const route = useRoute(); // 获取查询参数

const userList = ref([]); // 用户集合

const {tags} = route.query;
// 单个标签时 route.query.tags 为字符串而非数组，统一转为数组
const tagList = Array.isArray(tags) ? tags : (tags ? [tags] : []);

const loading = ref(true); // 骨架屏加载效果

const currentPage = ref(1); // 当前页码
const totalItem = ref(userList.value.length); // 总记录数量
const itemsPerPage = ref(10); // 每页展示的记录数

const userListPage = ref();

// 组件加载完成后查找标签符合的用户
onMounted(async () => {
  const response = await myAxios.get('/user/search/tags', {
    params: {
      tagList
    },
    // 解析参数使得后端集合能够接收到 ?hobbies=["coding","reading","music"]
    paramsSerializer: params => qs.stringify(params, {indices: false})
  });
  if (response?.data) {
    showSuccessToast("请求成功")
    response.data.forEach(user => {
      if (user.tags) {
        user.tags = JSON.parse(user.tags); // 解析后端返回的json字符串
      }
    });
    userList.value = response.data;
  } else {
    showFailToast("请求失败")
  }
  loading.value = false;

  // 更新页码
  changePage();
});

// 改变页码时触发
const changePage = () => {
  const start = itemsPerPage.value * (currentPage.value - 1);
  let end = itemsPerPage.value * currentPage.value;
  if (userList.value.length > itemsPerPage.value * currentPage.value) {
    userListPage.value = userList.value.slice(start, end);
  } else {
    end = userList.value.length;
    userListPage.value = userList.value.slice(start, end);
  }
  totalItem.value = userList.value.length;
}

</script>

<style scoped>

</style>