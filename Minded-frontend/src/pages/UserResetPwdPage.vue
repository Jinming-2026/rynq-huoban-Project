<!-- 用户重置密码页 -->
<template>
  <van-form @submit="toResetPassword">
    <van-cell-group inset>
      <van-field
          v-model="userAccount"
          name="userAccount"
          label="账号"
          placeholder="请输入账号"
          :rules="[{ validator: validateAccount, message: '账号需至少4位且无特殊字符' }]"
      />
      <van-field
          v-model="email"
          name="email"
          label="邮箱"
          placeholder="请输入账号绑定的邮箱"
          :rules="[{ validator: validateEmail, message: '请输入有效的邮箱地址' }]"
      />
      <van-field
          v-model="captcha"
          center
          clearable
          label="邮箱验证码"
          placeholder="请输入邮箱验证码"
      >
        <template #button>
          <van-button size="small" type="primary" @click="getCaptcha">
            {{ isCountingDown ? `${count}s 后重新发送` : '发送验证码' }}
          </van-button>
        </template>
      </van-field>
      <van-field
          v-model="newPassword"
          label="新密码"
          placeholder="请输入新密码"
          :rules="[{ validator: validatePassword, message: '密码需至少6位' }]"
      />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>
</template>


<script setup lang="ts">

import {onBeforeUnmount, ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showLoadingToast, showSuccessToast} from "vant";
import {useRouter} from "vue-router";

const router = useRouter();

// 定义倒计时和状态
const count = ref(60); // 倒计时秒数
const isCountingDown = ref(false); // 是否正在倒计时
let timer = null; // 定时器

const userAccount = ref('');
const email = ref('');
const captcha = ref('');   // 邮箱验证码
const newPassword = ref('');

// 校验账号：至少4位且无特殊字符
const validateAccount = (value: string): boolean => {
  const regex = /^[a-zA-Z0-9]{4,}$/;
  return regex.test(value);
};
// 校验密码：至少6位
const validatePassword = (value: string): boolean => {
  return value.length >= 6;
};
// 校验邮箱：符合标准的邮箱格式
const validateEmail = (value: string): boolean => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regex.test(value);
};

// 获取邮箱验证码
const getCaptcha = async () => {

  if (!validateEmail(email.value)) {
      return;
  }

  showLoadingToast({
    message: '发送中...',
    forbidClick: true,
  });

  const requestDTO = {
    userAccount: userAccount.value,
    email: email.value,
  }

  const res = await myAxios.post('/user/captcha', requestDTO);
  if (res?.code === 200) {
    showSuccessToast("发生成功! 请前往邮箱获取验证码");
    isCountingDown.value = true;
    count.value = 60;

    // 启动倒计时
    timer = setInterval(() => {
      count.value--;
      if (count.value <= 0) {
        clearInterval(timer);
        isCountingDown.value = false; // 重置倒计时状态
      }
    }, 1000);
  } else {
    showFailToast("发送失败! " + res?.description);
  }
}

// 组件卸载时清除定时器
onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer);
  }
});

/**
 * 重设密码
 */
const toResetPassword = async () => {

  const requestDTO = {
    userAccount: userAccount.value,
    email: email.value,
    emailCode: captcha.value,
    userPassword: newPassword.value
  }

  const res = await myAxios.post('/user/reset', requestDTO);
  if (res?.code === 200) {
    showSuccessToast('修改成功，请前往登录');
    await router.push({ // 重设成功跳转到登录页
      path: '/user/login',
      replace: true, // 提交后不能回退到页面
    })
  } else {
    showFailToast('修改失败!     ' + res?.description)
  }
}

</script>

<style scoped>

</style>