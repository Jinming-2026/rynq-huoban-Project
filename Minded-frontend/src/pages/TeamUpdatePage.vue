<template>
  <div id="teamAddPage">
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
            v-model="addTeamData.name"
            name="name"
            label="队伍名"
            placeholder="请输入队伍名"
            :rules="[{ required: true, message: '请填写队伍名' }]"
        />

        <van-field
            v-model="addTeamData.description"
            rows="4"
            autosize
            label="队伍描述"
            type="textarea"
            placeholder="请输入队伍描述"
        />

        <van-field
            is-link
            readonly
            name="datePicker"
            label="过期时间"
            :placeholder="expireTimeDisplay || '点击选择过期时间'"
            @click="showPicker = true"
        />
        <van-popup v-model:show="showPicker" position="bottom">
          <van-date-picker
              v-model="expireTimeArray"
              @confirm="onConfirm"
              @cancel="showPicker = false"
              title="请选择过期时间"
              :min-date="minDate"
          />
        </van-popup>

        <van-field name="radio" label="队伍状态">
          <template #input>
            <van-radio-group v-model="addTeamData.status" direction="horizontal">
              <van-radio name="0">公开</van-radio>
              <van-radio name="1">加密</van-radio>
              <van-radio name="2">私有</van-radio>
            </van-radio-group>
          </template>
        </van-field>

        <van-field
            v-if="Number(addTeamData.status) === 1"
            v-model="addTeamData.password"
            :type="passwordType"
            name="password"
            label="密码"
            placeholder="请输入队伍密码"
            :rules="[{ required: true, message: '请填写队伍密码' }]"
            clearable
            :right-icon="passwordIcon"
            @click-right-icon="switchPasswordType"
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

<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {computed, onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast} from "vant";

const router = useRouter();
const route = useRoute();

const teamId = route.query.id;

const initFormData = {
  description: "",
  expireTime: "",
  name: "",
  password: "",
  status: 0,
};

const addTeamData = ref<any>({...initFormData});

const showPicker = ref(false);
const minDate = new Date();
const expireTimeArray = ref<string[]>([]);

// 把后端返回的 expireTime（可能是 Date 对象 / 字符串）转成 YYYY-MM-DD 用于展示
const expireTimeDisplay = computed(() => {
  const t = addTeamData.value.expireTime;
  if (!t) return "";
  if (typeof t === "string" && /^\d{4}-\d{2}-\d{2}$/.test(t)) return t;
  const d = new Date(t);
  if (isNaN(d.getTime())) return "";
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
});

onMounted(async () => {
  if (!teamId || Number(teamId) <= 0) {
    showFailToast("队伍加载失败");
    return;
  }
  const res = await myAxios.get("/team/get", {params: {id: teamId}});
  if (res?.code === 200) {
    addTeamData.value = res.data;
    // 回显日期选择器
    const display = expireTimeDisplay.value;
    if (display) {
      const [y, m, d] = display.split("-");
      expireTimeArray.value = [y, m, d];
    }
  } else {
    showFailToast("获取队伍信息失败!" + res?.description);
  }
});

const onConfirm = ({selectedValues}: { selectedValues: string[] }) => {
  if (selectedValues && selectedValues.length === 3) {
    addTeamData.value.expireTime = selectedValues.join("-");
    showPicker.value = false;
  }
};

const showPassword = ref(false);
const passwordType = computed(() => (showPassword.value ? "text" : "password"));
const passwordIcon = computed(() => (showPassword.value ? "closed-eye" : "eye-o"));
const switchPasswordType = () => {
  showPassword.value = !showPassword.value;
};

const onSubmit = async () => {
  const postData = {
    ...addTeamData.value,
    status: Number(addTeamData.value.status),
  };
  const res = await myAxios.post("team/update", postData);
  if (res.code === 200) {
    showSuccessToast("修改成功");
    await router.push({path: "/team", replace: true});
  } else {
    showFailToast("修改失败! " + res?.description);
  }
};
</script>