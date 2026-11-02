<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {useRouter} from "vue-router";
import myAxios from "../plugins/myAxios.ts";

const router = useRouter();

// 选择标签
const activeIds = ref<Array<string | number>>([]);
const activeIndex = ref(0);
// 标签列表
const tagList = ref([]);

// 关闭所选择的标签
const doClose = (tag: string | number) => {
  activeIds.value = activeIds.value.filter(item => {
    return item !== tag;
  })
}

/**
 * 执行搜索
 */
const doSearchResult = () => {
  router.push({
    path: '/user/list',
    query: {
      tags: activeIds.value
    }
  })
}
/**
 * 获取数据库中所有的标签
 */
onMounted(async () => {
  const res = await myAxios.get('/tag/all');
  if (res.code === 200) {
    tagList.value = res.data;
  }
})

</script>

<template>
  <form action="/">
    <h4 style="text-align: center">选择要搜索伙伴的标签</h4>
  </form>
  <van-divider/>
  <van-divider content-position="left">已选标签</van-divider>
  <div v-if="activeIds.length === 0">请选择标签</div>
  <van-row gutter="20" style="padding: 0 16px">
    <van-col span="5" v-for="tag in activeIds">
      <van-tag :show="true"  closeable size="medium" type="primary" @close="doClose(tag)">
        {{tag}}
      </van-tag>
    </van-col>

  </van-row>


  <van-divider content-position="left">选择标签</van-divider>
  <van-tree-select
      v-model:active-id="activeIds"
      v-model:main-active-index="activeIndex"
      :items="tagList"
  />
  <div style="padding-left: 20px; padding-right: 20px">
    <van-button type="primary" size="large" @click="doSearchResult()">搜 索</van-button>
  </div>


</template>

<style scoped>

</style>