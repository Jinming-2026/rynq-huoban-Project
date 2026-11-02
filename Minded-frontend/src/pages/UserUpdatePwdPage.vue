<script setup lang="ts">

import {ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast} from "vant";
import {useRouter} from "vue-router";
import {clearCurrentUserState} from "../states/user.ts";

const router = useRouter();
// 用户密码
const oldPassword = ref('');
// 用户修改密码
const newPassword = ref('');
// 效验用户修改的密码
const checkPassword = ref('');
// 效验密码（密码长度 >= 6 位）
const pattern = /^.{6,}$/;

// 执行修改密码
const onSubmit = async () => {
  const passwordDate = {
    oldPassword: oldPassword.value,
    newPassword: newPassword.value,
    checkPassword: checkPassword.value
  }
  const res = await myAxios.post('/user/update/password', passwordDate);
  if (res?.code === 200) {
    showSuccessToast("修改成功");
    clearCurrentUserState();
    // 修改成功回退到修改信息页面
    await router.push({
      path: '/user/update',
      replace: true,  // 提交后不能返回该页面
    })
  } else {
    showFailToast("修改失败！" + res?.description);
  }

}

</script>

<template>
  <div>
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <!-- 通过 pattern 进行正则校验 -->
        <van-field
            v-model="oldPassword"
            name="pattern"
            placeholder="请输入旧密码"
            :rules="[{ pattern, message: '密码长度不小于 6 位' }]"
        />
        <!-- 通过 validator 进行函数校验 -->
        <van-field
            v-model="newPassword"
            name="validator"
            placeholder="请输入新密码"
            :rules="[{ pattern, message: '密码长度不小于 6 位' }]"
        />
        <!-- 通过 validator 返回错误提示 -->
        <van-field
            v-model="checkPassword"
            name="validatorMessage"
            placeholder="请输入确认密码"
            :rules="[{ pattern, message: '密码长度不小于 6 位' }]"
        />
      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          提交
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<style scoped>

</style>