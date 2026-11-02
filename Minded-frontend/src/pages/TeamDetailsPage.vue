<!--队伍详情页-->
<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref, computed} from "vue";
import {getCurrentUser} from "../service/user.ts";
import {TeamType} from "../models/team";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast, showLoadingToast, showConfirmDialog, closeDialog} from "vant";
import {useStore} from "vuex";
import {teamImg} from "../static/img/ossImgs.ts";

const router = useRouter();

const currentUser = ref();

const showPasswordDialog = ref(false);
const password = ref('');
const joinTeamId = ref();

const store = useStore();
// 使用 computed 保持与 store 状态的同步
const team = computed(() => store.state.team);
const createUser = computed(() => team.value.createUser);

/**
 * 跳转到队伍成员页
 */
const moveToMemberPage = () => {
  router.push('/team/member')
}

onMounted(async () => {
  currentUser.value = await getCurrentUser();
})

/**
 * 重置数据
 */
const doJoinClear = () => {
  joinTeamId.value = 0;
  password.value = '';
}

/**
 * 私有队伍弹出密码框
 * @param team
 */
const preJoinTeam = (team: TeamType) => {
  joinTeamId.value = team.id;
  if (team.status === 0) {
    doJoinTeam()
  } else {
    showPasswordDialog.value = true; // 显示密码输入框
  }
}

/**
 * 加入队伍
 */
const doJoinTeam = async () => {
  showLoadingToast({
    message: '正在加入...',
    forbidClick: true,
  })
  if (!joinTeamId.value) return;
  const res = await myAxios.post('/team/join', {
    teamId: joinTeamId.value,
    password: password.value,
  });
  if (res?.code === 200) {
    showSuccessToast('加入成功');
    team.value.hasJoin = true;
    team.value.hasJoinNum = (team.value.hasJoinNum ?? 0) + 1;
  } else {
    showFailToast('加入失败' + (res.description ? `，${res.description}` : ''));
  }
  doJoinClear();
}

/**
 * 跳转到修改队伍页面
 * @param id 队伍 id
 */
const doUpdateTeam = (id: number) => {
  router.push({
    path: '/team/update',
    query: {
      id,
    }
  })
}
/**
 * 退出队伍提示
 */
const doQuitTeamConfirm = (team: TeamType) => showConfirmDialog({
  title: '是否退出队伍',
      message:
  '退出队伍后，将清除您在该队伍的所有信息。',
}).then(() => {
  // 执行退出
  doQuitTeam(team.id);
}).catch(() => {
  // 关闭弹窗
  closeDialog();
});

/**
 * 退出队伍
 * @param id 队伍 id
 */
const doQuitTeam = async (id: number) => {
  showLoadingToast({
    message: '正在退出...',
    forbidClick: true,
  })
  const res = await myAxios.post('/team/quit', {
    teamId: id
  });
  if (res?.code === 200) {
    showSuccessToast('已退出');
    await router.push('/team')
  } else {
    showFailToast('退出失败!' + (res.description ? `，${res.description}}` : ''));
  }
}
/**
 * 解散队伍提示
 */
const doDeleteTeamConfirm = (team: TeamType) => showConfirmDialog({
  title: '是否解散队伍',
  message:
      '解散队伍后，将清楚该队伍的所有信息。',
}).then(() => {
  // 执行解散
  doDeleteTeam(team.id);
}).catch(() => {
  // 关闭弹窗
  closeDialog();
});
/**
 * 解散队伍
 * @param id 队伍 id
 */
const doDeleteTeam = async (id: number) => {
  const res = await myAxios.post('/team/disband', {
    id: id,
  });
  if (res?.code === 200) {
    showSuccessToast('操作成功');
    router.go(-1);  // 刷新页面
  } else {
    showFailToast('操作失败' + (res.description ? `，${res.description}}` : ''));
  }
}
</script>

<template>
  <template v-if="team.id">
    <div style="display: flex; flex-direction: column; align-items: center;">
      <van-image
          width="10rem"
          height="10rem"
          fit="cover"
          position="center"
          :src="team.avatar==null ? teamImg:team.avatar"
      />
      <p style="font-weight: bold; margin-top: 0.5rem; text-align: center;">{{ team.name }}</p>
    </div>
    <div class="centered-container">
      <p class="small-text">
        {{ team.hasJoinNum }} 加入人数 &nbsp;&nbsp;&nbsp; {{ team.maxNum }} 最大人数
      </p>
    </div>

    <van-cell-group inset>
      <van-cell title="创建人" :value="team.createUser?.username"/>
      <van-cell title="队伍描述" :label="team.description" size="large"/>
      <van-cell title="成员列表" @click="moveToMemberPage" is-link/>
      <van-cell title="队伍状态" :value="team.status === 0 ? '公开' : '加密'"/>
      <van-cell title="过期时间" :value="team.expireTime"/>
      <van-cell title="创建时间" :value="team.createTime"/>
    </van-cell-group>

    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit"
                  v-if="(createUser.id !== currentUser?.id) && !team.hasJoin && (team.maxNum > team.hasJoinNum)"
                  @click="preJoinTeam(team)">
        加入队伍
      </van-button>
    </div>
    <div style="margin: 16px;">
      <van-button round plain block type="primary" native-type="submit"
                  v-if="createUser.id === currentUser?.id"
                  @click="doUpdateTeam(team.id)">
        修改队伍
      </van-button>
    </div>
    <div style="margin: 16px;">
      <van-button round block type="warning" native-type="submit"
                  v-if="((createUser.id !== currentUser?.id) && team.hasJoin)"
                  @click="doQuitTeamConfirm(team)">
        退出队伍
      </van-button>
    </div>
    <div style="margin: 16px;">
      <van-button round plain block type="danger" native-type="submit"
                  v-if="createUser.id === currentUser?.id"
                  @click="doDeleteTeamConfirm(team)">
        解散队伍
      </van-button>
    </div>
    <van-dialog v-model:show="showPasswordDialog" title="填写密码" show-cancel-button @confirm="doJoinTeam"
                @cancel="doJoinClear">
      <van-field v-model="password" placeholder="请输入房间密码"/>
    </van-dialog>
  </template>
</template>

<style scoped>
.centered-container {
  display: flex;
  justify-content: center; /* 水平居中 */
}
</style>
