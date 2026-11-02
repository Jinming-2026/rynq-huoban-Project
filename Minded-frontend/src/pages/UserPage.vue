<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import {closeDialog, showConfirmDialog, showFailToast, showSuccessToast} from "vant";
import {getCurrentUser} from "../service/user.ts";
import myAxios from "../plugins/myAxios.ts";
import {clearCurrentUserState} from "../states/user.ts";
import {userImg} from "../static/img/ossImgs.ts";

const router = useRouter();

// 用户粉丝数量
const fansNum = ref(0);
// 用户关注数量
const followNum = ref(0);

const user = ref('');

onMounted( async () => {
  const res = await getCurrentUser();
  if (res) {
//    showSuccessToast("获取用户信息成功");
    user.value = res;
    await getFansNum();
    await getFollowNum();
  } else {
    await router.push(
        '/user/login'
    )
    showFailToast("获取信息失败，请先登录");
  }
})

// 退出登录确认
const doLogoutConfirm = () => showConfirmDialog({
  title: '退出登录',
  message:
      '退出登录不会删除你的数据，确认退出吗',
}).then(() => {
      doUserLogout();
      router.push('/user/login'); // 跳转到登录页
    }).catch(() => {
      closeDialog()
    });

// 执行退出登录
const doUserLogout = async () => {
  clearCurrentUserState();
  const res = await myAxios.post('/user/logout');
  if (res.code === 200) {
    showSuccessToast("退出成功");
  } else {
    showFailToast("退出失败，" + res.description);
  }
}

// 跳转到登录页
const moveToLogin = () => {
  router.push('/user/login')
}

// 获取登录用户粉丝数量
const getFansNum = async () => {
  const res = await myAxios.get('/friends/fansNum', {
    params : {
      id: user.value.id,
    }
  });
  if (res?.code === 200) {
    fansNum.value = res.data;
  } else {
    showFailToast("获取用户粉丝数量失败！" + res?.description);
  }
}
// 获取用户的关注数量
const getFollowNum = async () => {
  const res = await myAxios.get('/friends/followNum', {
    params: {
      id: user.value.id,
    }
  });
  if (res?.code === 200) {
    followNum.value = res.data;
  } else {
    showFailToast("获取用户关注数量失败！" + res?.description);
  }
}
</script>

<template>

  <template v-if="user">
  <div style="display: flex; flex-direction: column; align-items: center;">
    <van-image
        round
        width="15rem"
        height="15rem"
        :src="user.avatarUrl == null ? userImg:user.avatarUrl"
    />
    <p style="font-weight: bold; margin-top: 0.5rem; text-align: center;">{{ user?.username }}</p>
  </div>
    <van-cell-group inset>
      <van-cell title="粉丝列表" is-link to="/user/fans" :value="fansNum" />
      <van-cell title="关注列表" is-link to="/user/followers" :value="followNum" />
      <van-cell title="个人详情" is-link to="/user/update"/>
      <van-cell title="标签管理" is-link to="/user/interest"/>
      <van-cell title="我创建的队伍" is-link to="/user/create/team"/>
      <van-cell title="我加入的队伍" is-link to="/user/join/team"/>
    </van-cell-group>
  <div style="margin: 16px;">
    <van-button round plain block type="primary" native-type="submit" @click="doLogoutConfirm()">
      退出登录
    </van-button>
  </div>
  </template>
  <template v-else>
    <van-empty description="未登录">
      <van-button round type="primary" class="bottom-button" @click="moveToLogin">登录｜注册</van-button>
    </van-empty>
  </template>
</template>

<style scoped>
.bottom-button {
  width: 160px;
  height: 40px;
}
</style>