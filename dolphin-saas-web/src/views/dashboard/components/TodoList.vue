<template>
  <div class="todo-box">
    <a-table
      ref="table"
      size="middle"
      :columns="columns"
      :dataSource="list"
      :pagination="false"
      :scroll="{ y: 0 }"
      :rowKey="(record,index)=>{return index}"
    >
      <span slot="status" slot-scope="text">
        <a-badge :status="text | statusTypeFilter" :text="text | statusFilter" />
      </span>
    </a-table>
  </div>
</template>

<script>
const columns = [
  {
    title: '应用名',
    dataIndex: 'app',
  },
  {
    title: '版本号',
    dataIndex: 'version',
  },
  {
    title: '创建时间',
    dataIndex: 'createtime',
  },
  {
    title: '状态',
    dataIndex: 'status',
    scopedSlots: { customRender: 'status' },
  },
]
const statusMap = {
  0: { status: 'default', text: '待检查' },
  1: { status: 'processing', text: '检查中' },
  2: { status: 'processing', text: '待发布' },
  3: { status: 'processing', text: '发布中' },
  4: { status: 'success', text: '已发布' },
  5: { status: 'warning', text: '回滚中' },
  6: { status: 'warning', text: '已回滚' },
  7: { status: 'default', text: '发布异常' },
  8: { status: 'error', text: '项目异常' },
}
export default {
  filters: {
    statusFilter(type) {
      return statusMap[type].text
    },
    statusTypeFilter(type) {
      return statusMap[type].status
    },
  },
  props: {
    list: {
      type: Array,
      required: true,
    },
  },
  data() {
    return {
      columns,
    }
  },
  methods: {},
  created() {},
  //生命周期 - 挂载完成（可以访问DOM元素）
  mounted() {},
  beforeCreate() {}, //生命周期 - 创建之前
  beforeMount() {}, //生命周期 - 挂载之前
  beforeUpdate() {}, //生命周期 - 更新之前
  updated() {}, //生命周期 - 更新之后
  beforeDestroy() {}, //生命周期 - 销毁之前
  destroyed() {}, //生命周期 - 销毁完成
  activated() {}, //如果页面有keep-alive缓存功能，这个函数会触发
}
</script>
<style lang="less" scoped>
.todo-box {
  min-height: 300px;
  max-height: 300px;
  overflow: auto;
}
</style>