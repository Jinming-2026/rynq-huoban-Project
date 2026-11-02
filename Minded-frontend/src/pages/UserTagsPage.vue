<!--用户标签页-->
<script setup lang="ts">

import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showLoadingToast, showSuccessToast} from "vant";
import {getCurrentUser} from "../service/user.ts";
import {useRouter} from "vue-router";
import {clearCurrentUserState} from "../states/user.ts";
import {loadingDog} from "../static/img/ossImgs.ts";

const active = ref('allTag'); // 模块
const manage = ref(0);

const isLoadingDog = ref(false); // 下拉刷新图像

const interestTags = ref([]); // 用户感兴趣的标签
const tagList = ref([]); // 标签列表
const activeIndex = ref(0);

const user = ref(); // 当前登录用户信息

const router = useRouter();

// 加载提示图标
const showLoading = () => {
  showLoadingToast({
    message: '更新中...',
    forbidClick: true,
  })
}

/**
 * 移除用户标签
 * @param tag 要移除的用户标签
 */
const doClose = (tag: string) => {
  showLoading();
  interestTags.value = interestTags.value.filter(item => {
    // 保留没有选择移除的标签
    return item != tag;
  })
  // 向后端发送请求更新用户标签
  updateTags();
}

/**
 * 修改用户标签
 */
const updateTags = async () => {
  showLoading(); // 加载提示
  const tags = JSON.stringify(interestTags.value)
  const res = await myAxios.post('/tag/update', {
    tags: tags
  });
  if (res?.code === 200) {
    showSuccessToast('修改成功!');
    clearCurrentUserState();
  } else {
    showFailToast('修改失败!' + res?.description);
    active.value = 'myTag'; // 返回用户已选择的页面
  }
  await onTabChange(active.value)
}

/**
 * 向后端发送请求，刷新数据
 */
const onTabChange = async (name: string) => {
  isLoadingDog.value = true;
  let res = '';
  if (name === 'myTag') {
    await getCurrentTags(); // 查询用户已选择的标签
  } else if (name === 'allTag' || name === 'manageTag') { // 查询所有标签
    res = await myAxios.get('/tag/all');
    tagList.value = res?.data;
  }
  isLoadingDog.value = false;
}

onMounted(async () => {
  await onTabChange(active.value);
  await getCurrentTags();
})

/**
 * 获取当前登录用户的标签
 */
const getCurrentTags = async () => {
  user.value = await getCurrentUser();
  if (user.value?.tags) {
    interestTags.value = JSON.parse(user.value.tags); // 解析后端返回的json字符串为js数组
  }
  manage.value = user.value?.userRole ?? 0;
  console.log(tagList.value)
}

/**
 * 跳转到添加标签页
 */
const toAddTag = () => {
  router.push('/tag/add');
}
</script>

<template>
  <div>
    <van-pull-refresh v-model="isLoadingDog" :head-height="80" @refresh="onTabChange(active)">
      <!-- 下拉提示，通过 scale 实现一个缩放效果 -->
      <template #pulling="props">
        <img
            class="doge"
            :src="loadingDog"
            alt="图片加载失败"
        />
      </template>

      <!-- 释放提示 -->
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
            alt="图片加载失败"
        />
      </template>

      <div id="teamPage" style="min-height: 76vh">
        <van-tabs v-model:active="active" @change="onTabChange">
          <van-tab title="我的标签" name="myTag"/>
          <van-tab title="全部标签" name="allTag"/>
        </van-tabs>
        <div style="margin-bottom: 16px"/>

        <van-button v-if="active === 'allTag'" class="add-button"
                    type="primary" icon="plus" @click="toAddTag"/>

        <van-row v-if="active === 'myTag'" gutter="16" style="padding: 0 16px">
          <van-col v-for="tag in interestTags">
            <van-tag closeable size="large" type="primary" style="margin-right: 10px; margin-top: 10px"
                     @close="doClose(tag)">
              {{ tag }}
            </van-tag>
          </van-col>
        </van-row>

        <van-button v-if="active === 'manageTag'" class="add-button"
                    type="primary" icon="plus" @click="toAddTag"/>

        <!-- 全部标签       -->
        <van-tree-select
            v-if="active === 'allTag'"
            v-model:active-id="interestTags"
            v-model:main-active-index="activeIndex"
            :items="tagList"
            @click-item="updateTags"
            style="min-height: 76vh"
        />

        <!-- 未加载到数据 -->
        <van-empty v-if="!interestTags || interestTags.length < 1" description="暂无数据"/>
      </div>
    </van-pull-refresh>
  </div>
</template>

<style scoped>

.delButton{
  position: absolute;
  top: 30%;
  right: 10px; /* 调整位置确保圆点完全显示在右侧 */
  transform: translateY(-50%);
  background-color: red;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

</style>