<!-- 用户信息编辑页 -->
<template>
  <van-form @submit="onSubmit">
    <van-field v-if="editUser.editKey !== 'profile'"
               v-model="editUser.currentValue"
               :name="editUser.editKey"
               :label="editUser.editName"
               :placeholder="`请输入${editUser.editName}`"
    />
    <van-field v-if="editUser.editKey === 'profile'"
               v-model="editUser.currentValue"
               rows="2"
               autosize
               :label="editUser.editName"
               type="textarea"
               maxlength="100"
               :placeholder="`${editUser.currentValue}`"
               show-word-limit
    />
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>

</template>

<script setup lang="ts">

import {useRouter, useRoute} from "vue-router";
import {ref} from 'vue';
import myAxios from "../plugins/myAxios.ts";
import {showSuccessToast, showFailToast} from 'vant';
import {getCurrentUser} from "../service/user.ts";
import {clearCurrentUserState} from "../states/user.ts";

const router = useRouter();
const route = useRoute();

const editUser = ref({
  editKey: route.query.editKey,
  currentValue: route.query.currentValue,
  editName: route.query.editName
})


const onSubmit = async () => {

  const currentUser = await getCurrentUser(); // 获取当前登录用户
  if (!currentUser) {
    showFailToast('用户未登录');
    return;
  }
  /**
   * 发送更新请求
   */
  const res = await myAxios.post('/user/update', {
    'id': currentUser.id,
    [editUser.value.editKey as String]: editUser.value.currentValue,
  })
  if (res.code === 200 && res.data > 0) {
    showSuccessToast('修改成功');
    clearCurrentUserState(); // 清空内存中的用户信息
    router.back(); // 返回上一页
  } else {
    showFailToast('修改错误' + res?.description)
  }
};

</script>

<style scoped>

</style>