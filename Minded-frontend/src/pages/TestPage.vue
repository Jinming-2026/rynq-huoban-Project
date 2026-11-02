<template>
  <div>
    <van-tree-select
        v-model="mainActiveIndex"
        :items="items"
        :main-active-index="mainActiveIndex"
        :active-id="activeId"
        height="400"
    >
      <!-- 自定义子项内容 -->
      <template #content="props">
        <div v-if="props.items && props.items.length">
          <!-- 遍历子项 -->
          <div
              v-for="item in props.items"
              :key="item.id"
              class="custom-item"
          >
            <div class="item-content">
              <span>{{ item.text }}</span>
              <!-- 按钮1 -->
              <van-button
                  size="mini"
                  type="primary"
                  @click="onEdit(item)"
              >
                修改
              </van-button>
              <!-- 按钮2 -->
              <van-button
                  size="mini"
                  type="danger"
                  @click="onDelete(item)"
              >
                删除
              </van-button>
            </div>
          </div>
        </div>
      </template>
    </van-tree-select>
  </div>
</template>

<script>
export default {
  data() {
    return {
      mainActiveIndex: 0, // 当前选中主选项的索引
      activeId: [], // 当前选中的子项 ID
      items: [
        {
          text: "分类1",
          children: [
            { text: "选项1-1", id: 1 },
            { text: "选项1-2", id: 2 },
          ],
        },
        {
          text: "分类2",
          children: [
            { text: "选项2-1", id: 3 },
            { text: "选项2-2", id: 4 },
          ],
        },
      ],
    };
  },
  methods: {
    onEdit(item) {
      console.log("点击修改：", item);
      // 在此添加修改逻辑
    },
    onDelete(item) {
      console.log("点击删除：", item);
      // 删除逻辑
      this.items.forEach((mainItem) => {
        mainItem.children = mainItem.children.filter(
            (child) => child.id !== item.id
        );
      });
    },
  },
};
</script>

<style scoped>
.custom-item {
  padding: 10px 16px;
  border-bottom: 1px solid #ebedf0;
}

.item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-content span {
  flex: 1;
}
</style>
