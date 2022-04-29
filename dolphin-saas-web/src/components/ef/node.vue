<template>
  <div ref="node" @click="clickNode" @mouseup="changeNodeSite" :class="nodeContainerClass">
    <!-- 节点名称 -->
    <div class="ef-node-text" :show-overflow-tooltip="true">
      <div>{{node.stageName}}</div>
      <!-- 最后备注内容 -->
      <div class="ef-node-info">
        <span v-if="node.releaseStagesUpdatetime">{{node.releaseStagesUpdatetime}}</span>
      </div>
    </div>
    <!-- 节点状态图标 -->
    <div class="ef-node-right-ico">
      <a-icon type="clock-circle-o" style="color:#2684ff" v-show="node.stageStatus === 'standby'"></a-icon>
      <a-icon type="check-circle" style="color:green" v-show="node.stageStatus === 'success'"></a-icon>
      <a-icon type="close-circle" style="color:red" v-show="node.stageStatus === 'error'"></a-icon>
      <a-icon type="warning" style="color:orange" v-show="node.stageStatus === 'warning'"></a-icon>
      <a-icon type="sync" style="color:blue" spin v-show="node.stageStatus === 'running'"></a-icon>
    </div>
  </div>
</template>

<style scoped>
@import './index.css';
.box-card {
  width: 300px;
}
.it {
  height: 300px;
}
</style>

<script>
export default {
  props: {
    node: Object,
    activeElement: Object,
  },
  data() {
    return {}
  },
  computed: {
    nodeContainerClass() {
      return {
        'ef-node-container': true,
        'ef-node-active': this.activeElement.type == 'node' ? true : false,
      }
    },
    nodeIcoClass() {
      var nodeIcoClass = {}
      // nodeIcoClass[this.node.ico] = true
      // 添加该class可以推拽连线出来，viewOnly 可以控制节点是否运行编辑
      nodeIcoClass['flow-node-drag'] = false
      return nodeIcoClass
    },
  },
  methods: {
    // 点击节点
    clickNode() {
      this.$emit('clickNode', this.node.id)
    },
    // 鼠标移动后抬起
    changeNodeSite() {
      // 避免抖动
      if (
        this.node.left == this.$refs.node.style.left &&
        this.node.top == this.$refs.node.style.top
      ) {
        return
      }
      this.$emit('changeNodeSite', {
        nodeId: this.node.id,
        left: this.$refs.node.style.left,
        top: this.$refs.node.style.top,
      })
    },
  },
}
</script>
