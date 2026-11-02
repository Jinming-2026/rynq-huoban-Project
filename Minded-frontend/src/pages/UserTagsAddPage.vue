<!--用户创建标签-->
<script setup lang="ts">

import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast} from "vant";
import {useRouter} from "vue-router";

const result = ref('');
const showPicker = ref(false);
const parentTags = ref([]);

const childrenTagName = ref(''); // 要创建的子标签名称
const createParent = ref(false); // 是否创建父标签
const parentTagName = ref(''); // 自定义的父标签名称
const parentId = ref(0); // 要创建的标签的父标签id

const router = useRouter();

/**
 * 选择父标签点击确定时触发
 * @param selectedOptions
 */
const onConfirm = ({selectedOptions}: { selectedOptions: Array<{ text?: string; value?: number | string }> }) => {
  createParent.value = selectedOptions[selectedOptions.length - 1].value === 99999;
  result.value = selectedOptions[0]?.text;
  parentId.value = selectedOptions[0]?.value;
  showPicker.value = false;
};

/**
 * 获取父标签数据
 */
onMounted(async () => {
  const res = await myAxios.get('/tag/all/parent');
  if (res.code === 200 && res.data) {
    parentTags.value = res.data;
    // 最后一条数据为自定义，用户自行编辑
    parentTags.value.push({text: '自定义', value: 99999})
  }
})

/**
 * 发送请求创建标签
 */
const addTag = async () => {
  let res;
  if (createParent.value) { // 创建父子标签
    res = await myAxios.post('/tag/add', {
      tagName: childrenTagName.value,
      parentName: parentTagName.value
    })
  } else { // 只创建子标签
    res = await myAxios.post('/tag/add', {
      tagName: childrenTagName.value,
      parentId: parentId.value
    });
  }
  if (res?.code === 200) {
    showSuccessToast('创建成功!');
    setTimeout(() => {
      window.location = '/user/interest';
    }, 500); // 2000 毫秒 = 2 秒
  } else {
    showFailToast('添加失败!' + res?.description);
    setTimeout(() => {
      window.location = '/user/interest';
    }, 500); // 2000 毫秒 = 2 秒
  }
}
</script>

<template>
  <van-field
      v-model="result"
      is-link
      required
      readonly
      name="picker"
      label="选择父标签"
      placeholder="点击选择标签"
      @click="showPicker = true"
  />
  <van-popup v-model:show="showPicker" position="bottom">
    <van-picker
        :columns="parentTags"
        @confirm="onConfirm"
        @cancel="showPicker = false"
    />
  </van-popup>

  <van-field
      :disabled="!createParent"
      v-model="parentTagName"
      required
      :rules="[{ required: true }]"
      label="创建父标签"
      placeholder="请输入父标签名"
  />

  <van-field
      v-model="childrenTagName"
      required
      :rules="[{ required: true }]"
      label="子标签"
      placeholder="请输入子标签名"
  />
  <div style="padding: 12px">
    <van-button class="add" type="primary" block @click="addTag">提交</van-button>
  </div>
</template>

<style scoped>

</style>