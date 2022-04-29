<template>
  <div>
    <a-divider orientation="left">集群信息</a-divider>
    <a-descriptions size="small" :column="isMobile ? 1 : 3" bordered>
      <a-descriptions-item label="集群规模">{{`≈${clusterInfo.clusterCurrent}人同时在线`||'无'}}</a-descriptions-item>
      <a-descriptions-item label="状态">
        <span v-if="clusterInfo.clusterServiceStatus">
          <a-badge
            :status="clusterInfo.clusterServiceStatus | statusTypeFilter"
            :text="clusterInfo.clusterServiceStatus | statusFilter"
          />
        </span>
      </a-descriptions-item>
      <a-descriptions-item label="归属云">{{clusterInfo.clusterCloudId||'无'}}</a-descriptions-item>
      <a-descriptions-item label="更新时间">{{clusterInfo.clusterUpdatetime||'无'}}</a-descriptions-item>
      <a-descriptions-item label="创建时间">{{clusterInfo.clusterCreatetime||'无'}}</a-descriptions-item>
    </a-descriptions>
    <a-divider orientation="left">部署进度</a-divider>
    <div v-if="progressLStatus">
      <!-- 部署中 -->
      <a-progress
        v-if="progress.length!=progress.reduce((sum,e)=>sum+Number(e.stageStatus>1?1:0),0)"
        :percent="(progress.reduce((sum,e)=>sum+Number(e.stageStatus>1?1:0),0)/progress.length).toFixed(4)*100"
        status="active"
      />
      <!-- 部署成功 -->
      <a-progress v-if="progress.every(({ stageStatus }) => stageStatus == 2)" :percent="100" />
      <!-- 部署失败 -->
      <a-progress
        v-if="progress.every(({ stageStatus }) => stageStatus > 1)&&progress.reduce((sum,e)=>sum+Number(e.stageStatus>2?1:0),0)>1"
        :percent="(progress.reduce((sum,e)=>sum+Number(e.stageStatus>1?1:0),0)/progress.length).toFixed(4)*100"
        status="exception"
      />
    </div>
    <div v-else>
      <a-progress :percent="0" status="active" />
    </div>

    <a-divider orientation="left">部署阶段</a-divider>
    <div class="example" v-if="!progressLStatus">
      <a-spin tip="部署阶段正在加载中..." />
    </div>
    <a-timeline mode="left" v-else>
      <a-timeline-item
        v-for="(item,index) in progress"
        :key="index"
        :color="progressStatusList[item.stageStatus].color"
      >
        <a-icon
          slot="dot"
          :type="progressStatusList[item.stageStatus].icon"
          style="font-size: 16px;"
          :spin="progressStatusList[item.stageStatus].icon=='sync'"
        />
        <h4>
          {{item.stageName}}
          <a-tag
            size="small"
            :color="progressStatusList[item.stageStatus].color"
          >{{ progressStatusList[item.stageStatus].label }}</a-tag>
        </h4>

        <p class="timeline-item-createtime">{{item.stageCreatetime}}</p>
        <p
          v-if="item.stageStatus>1&&item.stageCreatetime"
        >耗时 {{(new Date(item.stageUpdatetime).getTime()/1000)-(new Date(item.stageCreatetime).getTime()/1000)||1|formatSeconds}}</p>
      </a-timeline-item>
    </a-timeline>
  </div>
</template>
<script>
import { baseMixin } from '@/store/app-mixin'
import { getClusterInfo, getDeployStages } from '@/api/index'
const statusMap = {
  0: { status: 'processing', text: '初始化(搭建)' },
  1: { status: 'processing', text: '待决策' },
  2: { status: 'processing', text: '配置部署中' },
  3: { status: 'success', text: '运行中' },
  4: { status: 'error', text: '异常' },
  5: { status: 'default', text: '停用' },
  6: { status: 'warning', text: '回收中' },
}
export default {
  mixins: [baseMixin],
  props: {
    clusterId: {
      type: Number,
      required: true,
    },
  },
  name: 'detail',
  data() {
    return {
      clusterInfo: {},
      progress: [],
      progressStatusList: [
        { color: 'blue', label: '待执行', icon: 'clock-circle' },
        { color: 'blue', label: '执行中', icon: 'sync' },
        { color: 'green', label: '成功', icon: 'check-circle' },
        { color: 'red', label: '失败', icon: 'close-circle' },
        { color: 'green', label: '完成', icon: 'check-circle' },
        { color: 'orange', label: '异常', icon: 'warning' },
        { color: 'cyan', label: '跳过', icon: 'issues-close' },
      ],
      progressLStatus: false,
      timer: null,
    }
  },
  filters: {
    statusFilter(type) {
      return statusMap[type].text
    },
    statusTypeFilter(type) {
      return statusMap[type].status
    },
    formatSeconds(value) {
      let result = parseInt(value)
      let h = Math.floor(result / 3600) < 10 ? '0' + Math.floor(result / 3600) : Math.floor(result / 3600)
      let m =
        Math.floor((result / 60) % 60) < 10 ? '0' + Math.floor((result / 60) % 60) : Math.floor((result / 60) % 60)
      let s = Math.floor(result % 60) < 10 ? '0' + Math.floor(result % 60) : Math.floor(result % 60)

      let res = ''
      if (h !== '00') res += `${h}小时`
      if (m !== '00') res += `${m}分`
      res += `${s}秒`
      return res
    },
  },
  methods: {
    getClusterInfo() {
      this.listLoading = true
      getClusterInfo({ cid: this.clusterId })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.clusterInfo = { ...data }
          } else {
            this.$message.error(msg)
          }
        })
        .catch((error) => {})
    },
    getDeployStages() {
      getDeployStages({ cid: this.clusterId })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            if (data.clusterStages.length > 0) {
              this.progressLStatus = true
            }
            if (data.clusterStages.every(({ stageStatus }) => stageStatus > 1) && this.timer) {
              clearInterval(this.timer)
              this.timer = null
            }
            this.progress = data.clusterStages || []
            if (!data.find((item) => item.stageStatus < 2) && this.timer) {
              clearInterval(this.timer)
            }
          } else {
            this.$message.error(msg)
          }
        })
        .catch((error) => {})
    },
  },
  created() {
    this.getClusterInfo()
  },
  //生命周期 - 挂载完成（可以访问DOM元素）
  mounted() {
    //轮询查看进度
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
    this.timer = setInterval(() => {
      this.getDeployStages()
    }, 1500)
  },
  beforeCreate() {}, //生命周期 - 创建之前
  beforeMount() {}, //生命周期 - 挂载之前
  beforeUpdate() {}, //生命周期 - 更新之前
  updated() {}, //生命周期 - 更新之后
  beforeDestroy() {
    //销毁定时器
    if (this.timer != null) {
      clearInterval(this.timer)
      this.timer = null
    }
  }, //生命周期 - 销毁之前
  destroyed() {}, //生命周期 - 销毁完成
}
</script>
<style lang="less" scoped>
.timeline-item-createtime {
  position: absolute;
  right: calc(100% + 30px);
  top: 0;
  width: 180px;
  text-align: right;
}
::v-deep .ant-timeline {
  margin-left: 180px;
}
.example {
  width: 100%;
  text-align: center;
  border-radius: 4px;
  margin-bottom: 20px;
  padding: 30px 50px;
  margin: 20px 0;
}
</style>