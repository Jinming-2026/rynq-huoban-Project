<script setup lang="ts">
import {ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showLoadingToast, showSuccessToast} from "vant";
import {useRouter} from "vue-router";
import {logoImg} from "../static/img/ossImgs.ts";

const router = useRouter();

const userAccount = ref('');
const userPassword = ref('');
const checkPassword = ref('');
const username = ref('');
const email = ref('');

/**
 * 实现用户注册
 */
const onSubmit = async () => {
  showLoadingToast({
    message: '注册中...',
    forbidClick: true,
  });
  const res = await myAxios.post('user/register',
      {
        username: username.value,
        userAccount: userAccount.value,
        userPassword: userPassword.value,
        checkPassword: checkPassword.value,
        email: email.value
      })
  if (res?.code === 200 && res.data) {
    // 跳转到登录页
    await router.push('/user/login');
    showSuccessToast("注册成功");
  } else {
    showFailToast('注册失败,' + res?.description);
  }
}

// 校验昵称：2-10个字符
const validateNickname = (value: string): boolean => {
  return value.length >= 2 && value.length <= 10;
};
// 校验账号：至少4位且无特殊字符
const validateAccount = (value: string): boolean => {
  const regex = /^[a-zA-Z0-9]{4,}$/;
  return regex.test(value);
};
// 校验密码：至少6位
const validatePassword = (value: string): boolean => {
  return value.length >= 6;
};
// 校验确认密码：与密码一致
const validateCheckPassword = (value: string): boolean => {
  return value === userPassword.value;
};

// 校验邮箱：符合标准的邮箱格式
const validateEmail = (value: string): boolean => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regex.test(value);
};

// 去登录页面
const toLogin = () => {
  router.push(
      '/user/login'
  )
}
</script>

<template>
  <div style="display: flex; flex-direction: column; align-items: center;">
    <van-image
        width="10rem"
        height="10rem"
        fit="cover"
        position="center"
        :src=logoImg
    />
    <p style="font-weight: bold; margin-top: 0.5rem; text-align: center;">小R同学伙伴匹配中心</p>
  </div>
  <van-form @submit="onSubmit">
    <van-cell-group inset>
        <van-field
            v-model="username"
            name="username"
            label="昵称"
            placeholder="请输入昵称"
            :rules="[{ validator: validateNickname, message: '昵称需为2-10个字符' }]"
        />
        <van-field
            v-model="userAccount"
            name="userAccount"
            label="账号"
            placeholder="请输入账号(用于登录)"
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
        <van-field
            v-model="checkPassword"
            type="password"
            name="checkPassword"
            label="确认密码"
            placeholder="请再次输入密码"
            :rules="[{ required: true, message: '请填写确认密码' }, { validator: validateCheckPassword, message: '两次密码输入不一致' }]"
        />
        <van-field
            v-model="email"
            name="email"
            label="邮箱"
            placeholder="请输入邮箱，用于找回密码"
            :rules="[{ validator: validateEmail, message: '请输入有效的邮箱地址' }]"
        />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        注册
      </van-button>
    </div>
    <div style="margin: 16px;">
      <van-button round block plain type="primary"  @click="toLogin">
        已有账号？去登录
      </van-button>
    </div>
  </van-form>

</template>

<style scoped>

</style>