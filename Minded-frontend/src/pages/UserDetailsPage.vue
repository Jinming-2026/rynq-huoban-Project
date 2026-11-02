<!--用户详情页-->
<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, computed, ref} from "vue";
import {getCurrentUser} from "../service/user.ts";
import myAxios from "../plugins/myAxios.ts";
import {useStore} from "vuex";
import {userImg} from "../static/img/ossImgs.ts";
import {showFailToast, showLoadingToast, showSuccessToast} from "vant";

const router = useRouter();

const currentUser = ref();

const store = useStore();
// 获取点击对应用户的信息，使用 computed 保持与 store 状态同步
const user = computed(() => store.state.user);
// 用户粉丝数
const userFansNum = ref(0);
// 用户的关注数
const userFollowNum = ref(0);
// 是否关注
const isFollowUser = ref(false);


onMounted(async () => {
  // 获取当前登录用户信息
  currentUser.value = await getCurrentUser();
  // 获取用户粉丝数
  await getFansNum(user.value.id);
  // 获取用户的关注数
  await getFollowNum(user.value.id);
  await isFollow(user.value.id);
})

/**
 * 获取用户的粉丝数
 */
const getFansNum = async (userId: number) => {
  const res = await myAxios.get('/friends/fansNum',{
    params: {
      id: userId,
    }
  });
  if (res?.code === 200) {
    userFansNum.value = res?.data;
  } else {
    userFansNum.value = -1;
    showFailToast("获取用户粉丝数失败！" + res?.description);
  }
}

/**
 * 获取用户的关注数
 */
const getFollowNum = async (userId: number) => {
  const res = await myAxios.get('/friends/followNum',{
    params: {
      id: userId,
    }
  });
  if (res?.code === 200) {
    userFollowNum.value = res?.data;
  } else {
    userFollowNum.value = -1;
    showFailToast("获取用户关注数失败！" + res?.description);
  }
}

/**
 * 判断当前用户是否关注该用户
 */
const isFollow = async (userId: number) => {
  const res = await myAxios.get('/friends/isFollow',{
    params: {
      id: userId,
    }
  })
  if (res?.code === 200) {
    isFollowUser.value = res.data === true;
  } else {
    isFollowUser.value = false;
    showFailToast("判断是否关注用户失败！" + res?.description);
  }
}

/**
 * 关注用户
 */
const doFollowUser = async (userId: number) => {
  showLoadingToast({
    message: '操作中...',
    forbidClick: true,
  });
  const res = await myAxios.post((`/friends/dofollow/${userId}`));
  if (res?.code === 200) {
    isFollowUser.value = true;
    userFansNum.value += 1;
    showSuccessToast("关注成功");
  } else {
    showFailToast("关注失败！" + res?.description);
  }
}
/**
 * 取消关注
 */
const unFollowUser = async (userId: number) => {
  showLoadingToast({
    message: '操作中...',
    forbidClick: true,
  });
  const res = await myAxios.post(`/friends/unfollow/${userId}`)
  if (res?.code === 200) {
    isFollowUser.value = false;
    userFansNum.value -= 1;
    showSuccessToast("取关成功");
  } else {
    showFailToast("取关失败！" + res?.description);
  }
}

/**
 * 进行私聊
 */
const toUserChatPage = async () => {
  store.commit('updateChatUserId', user.value);
  store.commit('updateChatUserName', user.value.username);  // 更新当前私聊用户姓名
  await router.push('/chat/private');
}

</script>

<template>
  <template v-if="user.id">
    <div style="display: flex; flex-direction: column; align-items: center;">
    <van-image
        width="15rem"
        height="15rem"
        fit="cover"
        position="center"
        radius="10px"
        :src="user.avatarUrl == null ? userImg:user.avatarUrl"
    />
    <p style="font-weight: bold; margin-top: 0.5rem; text-align: center;">{{ user.username }}</p>
  </div>
  <div class="centered-container">
    <p class="small-text">
      {{ userFollowNum }} 关注 &nbsp;&nbsp;&nbsp; {{ userFansNum }} 粉丝
    </p>
  </div>

  <van-cell-group inset>
    <van-cell title="个人简介:  " :label="user.profile" size="large"/>
    <div class="tag-container" v-if="user.tags">
      <van-tag title="标签"
               :key="tag"
               plain
               round
               type="primary"
               size="large"
               color="#00BFFF"
               class="tag-inline"
               v-for="tag in user.tags"
      >
        {{tag}}
      </van-tag>
    </div>
    <van-cell v-if="user.email" title="邮箱" :value="user.email" />
    <van-cell v-if="user.gender" title="性别" :value="user.gender === 0 ? '女' : '男'"/>
    <van-cell title="创建时间" :value="user.createTime"/>
  </van-cell-group>

  <div style="margin: 16px;">
    <van-button round block type="primary" native-type="submit"
                v-if="(!isFollowUser)"
                @click="doFollowUser(user.id)">
      关注
    </van-button>
  </div>
  <div style="margin: 16px;">
    <van-button round plain block type="primary" native-type="submit"
                v-if="(isFollowUser)"
                @click="toUserChatPage()">
      私聊
    </van-button>
  </div>
    <div style="margin: 16px;">
      <van-button round plain block type="danger" native-type="submit"
                  v-if="(isFollowUser)"
                  @click="unFollowUser(user.id)">
        取消关注
      </van-button>
    </div>
  </template>
</template>

<style scoped>
.tag-container {
  /* 使用 Flexbox 布局 */
  display: flex;
  /* 允许内容换行 */
  flex-wrap: wrap;
  /* 如果需要水平居中，可以添加以下属性 */
  justify-content: center;
  /* 如果需要垂直居中（在容器高度确定的情况下），可以添加以下属性 */
  align-items: center;
}

.tag-inline {
  /* 确保标签之间有一定的间距 */
  margin-right: 5px;
  margin-bottom: 5px; /* 如果需要更清晰的换行间距 */
}

.centered-container {
  display: flex;
  justify-content: center; /* 水平居中 */
}

.small-text {
  font-size: 15px; /* 设置字体大小 */
  /* 可以添加其他样式，如字体颜色、行高等 */
  color: #333;
  line-height: 0;
}
</style>