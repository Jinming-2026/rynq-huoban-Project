<!-- 用户个人信息页 -->
<template>
  <template v-if="user">
    <van-cell title="昵称" is-link to="/user/edit" :value="user.username"
              @click="toEdit('username', '昵称', user.username)"/>
    <van-cell title="账号" :value="user.userAccount"/>
    <van-cell title="上传头像" class="custom-cell">
      <van-uploader
          :after-read="afterRead"
          v-model="avatarUrlFile"
          multiple
          :max-count="1"
          preview-size="60"
          max-size="2097152"
          @oversize="onOversize"
      />
    </van-cell>
    <van-field
        v-model="showGender"
        is-link
        label="性别"
        @click="showGenderPicker = true"
    />
    <van-popup v-model:show="showGenderPicker" round position="bottom">
      <van-picker
          title="选择性别"
          :loading="loadingUpdateGender"
          :columns="genderColumns"
          @cancel="showGenderPicker = false"
          @confirm="modifyUserGender"
      />
    </van-popup>
    <van-cell title="电话" :value="user.phone" is-link to="/user/edit" @click="toEdit('phone', '电话', user.phone)"/>
    <van-cell title="邮箱" is-link to="/user/edit" :value="user.email" @click="toEdit('email', '邮箱', user.email)"/>
    <van-cell title="注册时间" :value="user.createTime"/>
    <van-cell title="修改密码" is-link to="/user/edit/password"/>
    <van-cell center title="个人简介" :label="user.profile" is-link to="/user/edit"
              @click="toEdit('profile', '个人简介', user.profile)"/>
  </template>
</template>

<script setup lang="ts">
import {useRouter} from 'vue-router';
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../service/user.ts";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast} from "vant";
import {clearCurrentUserState} from "../states/user.ts";

const router = useRouter();

const user = ref();

const avatarUrlFile = ref();
const showUpload = ref(false);

const showGenderPicker = ref(false); // 是否显示性别修改框
const showGender = ref('');
const loadingUpdateGender = ref(false); // 加载样式

// 用户性别选择
const genderColumns = [
  {text: '男', value: 1},
  {text: '女', value: 0},
]

onMounted(async () => {
  user.value = await getCurrentUser(); // 获取当前登录用户
  showGender.value = user.value?.gender === 0 ? '女':'男';
  if (!user.value.avatarUrl) {
    showUpload.value = true;
  }
})

/**
 * 修改用户性别
 */
const modifyUserGender = async ({selectedValues}: { selectedValues: number[] }) => {
  loadingUpdateGender.value = true;
  user.value.gender = selectedValues[0]; // 修改性别
  const res = await myAxios.post('/user/update', user.value)
  if (res.code === 200 && res.data > 0) {
    showSuccessToast('修改成功');
  } else {
    showFailToast('修改失败' + res?.description)
  }
  showGenderPicker.value = false;
  loadingUpdateGender.value = false;
  showGender.value = user.value?.gender === 0 ? '女' : '男';
}

/**
 * 上传文件超出限制
 */
const onOversize = () => {
  showFailToast("图片大小不能大于5M!");
}

/**
 * 更新头像
 * @param avatarFile
 */
const afterRead = async (avatarFile: File) => {
  const res = await myAxios.post('/user/upload', avatarFile, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  if (res?.code === 200) {
    clearCurrentUserState(); // 清除缓存中的登录用户
    showSuccessToast('上传成功');
    await router.push('/user') // 跳转回用户主页
  } else {
    showFailToast("上传失败" + res?.description)
  }
}

/**
 * 发送请求修改用户信息
 * @param editKey
 * @param editName
 * @param currentValue
 */
const toEdit = (editKey: string, editName: string, currentValue: string) => {
  router.push({
    path: '/user/edit',
    query: {
      editKey,
      editName,
      currentValue // 要更新的值
    }
  })
}

</script>

<style scoped>
.custom-cell {
  display: flex;
  align-items: center;
  height: 100%;
}
</style>