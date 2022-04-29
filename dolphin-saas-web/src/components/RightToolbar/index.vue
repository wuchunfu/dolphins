<template>
  <div class="top-right-btn">
    <a-row type="flex" justify="end">
      <a-button-group>
        <a-tooltip class="item" :title="showSearch ? '隐藏搜索' : '显示搜索'" placement="top">
          <a-button shape="circle" icon="search" @click="toggleSearch()" />
        </a-tooltip>
        <a-tooltip class="item" title="刷新" placement="top">
          <a-button shape="circle" icon="sync" @click="refresh()" />
        </a-tooltip>
      </a-button-group>
      <!-- <a-tooltip class="item" title="显隐列" placement="top" v-if="columns">
        <a-button size="mini" shape="circle" icon="appstore" @click="showColumn()" />
      </a-tooltip>-->
    </a-row>
    <!-- <a-dialog :title="title" :visible.sync="open" append-to-body>
      <a-transfer :titles="['显示', '隐藏']" v-model="value" :data="columns" @change="dataChange"></a-transfer>
    </a-dialog>-->
  </div>
</template>
<script>
export default {
  name: 'RightToolbar',
  data() {
    return {
      // 显隐数据
      value: [],
      // 弹出层标题
      title: '显示/隐藏',
      // 是否显示弹出层
      open: false,
      //刷新按钮是否旋转
      spin: false,
    }
  },
  props: {
    showSearch: {
      type: Boolean,
      default: true,
    },
    columns: {
      type: Array,
    },
  },
  created() {
    // 显隐列初始默认隐藏列
    for (let item in this.columns) {
      if (this.columns[item].visible === false) {
        this.value.push(parseInt(item))
      }
    }
  },
  methods: {
    // 搜索
    toggleSearch() {
      this.$emit('update:showSearch', !this.showSearch)
    },
    // 刷新
    refresh() {
      this.$emit('queryTable')
    },
    // 右侧列表元素变化
    dataChange(data) {
      for (var item in this.columns) {
        const key = this.columns[item].key
        this.columns[item].visible = !data.includes(key)
      }
    },
    // 打开显隐列dialog
    showColumn() {
      this.open = true
    },
  },
}
</script>
<style lang="less" scoped>
// ::v-deep .el-transfer__button {
//   border-radius: 50%;
//   padding: 12px;
//   display: block;
//   margin-left: 0px;
// }
// ::v-deep .el-transfer__button:first-child {
//   margin-bottom: 10px;
// }
// .top-right-btn {
//   position: relative;
//   float: right;
// }
</style>
