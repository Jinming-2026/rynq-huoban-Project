<script setup lang="ts">
import {ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showLoadingToast, showSuccessToast} from "vant";
import {useRouter} from "vue-router";
import {setCurrentUserState} from "../states/user.ts";
import {logoImg} from "../static/img/ossImgs.ts";

const router = useRouter();

const userAccount = ref('');
const userPassword = ref('');

/**
 * 实现用户登录
 */
const onSubmit = async () => {
  showLoadingToast({
    message: '登录中...',
    forbidClick: true,
  });
  const res = await myAxios.post('user/login',
      {
        userAccount: userAccount.value,
        userPassword: userPassword.value
      })
    if (res.code === 200 && res.data) {
      showSuccessToast("登录成功");
      setCurrentUserState(res.data);  // 缓存当前登录用户
      // 跳转到主页面，并且不能回退
      await router.push({
        path: '/',
        replace: true, // 提交后不能回退到页面
      })
    } else {
      showFailToast('登录失败,' + res?.description);
    }
}

// 校验账号：至少4位且无特殊字符
const validateAccount = (value: string): boolean => {
  const regex = /^[a-zA-Z0-9]{4,}$/;
  return regex.test(value);
};
// 校验密码：至少6位
const validatePassword = (value: string): boolean => {
  return value.length >= 6;
};

// 去注册页面
const toRegister = () => {
  router.push(
      '/user/register'
  )
}
// 跳转到找回密码页面
const moveToResetPassword = () => {
  router.push('/user/reset');
}
</script>

<template>
  <div style="display: flex; flex-direction: column; align-items: center;">
    <van-image
        width="10rem"
        height="10rem"
        fit="cover"
        position="center"
        :src = logoImg
    />
    <p style="font-weight: bold; margin-top: 0.5rem; text-align: center;">小R同学伙伴匹配中心</p>
  </div>
  <van-form @submit="onSubmit">
    <van-cell-group inset>
      <van-field
          v-model="userAccount"
          name="userAccount"
          label="账号"
          placeholder="请输入账号"
          :rules="[{ validator: validateAccount, message: '账号需至少4位且无特殊字符' }]"

      />
      <van-field
          v-model="userPassword"
          type="password"
          name="userPassword"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ validator: validatePassword, message: '密码需至少6位' }]"
      />
      <div style="text-align: right; margin-top: 4px; margin-bottom: 1px;">
        <span @click="moveToResetPassword" style="color: red; cursor: pointer; font-size: 0.8rem;">忘记密码？</span>
      </div>
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        登录
      </van-button>
    </div>
    <div style="margin: 16px;">
      <van-button round block plain type="primary"  @click="toRegister">
        没有账号？去注册
      </van-button>
    </div>
  </van-form>

</template>

<style scoped>

</style>