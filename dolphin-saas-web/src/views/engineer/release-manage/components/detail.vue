<template>
  <div>
    <a-divider orientation="left">流程地图</a-divider>
    <div
      style="margin: 10px;min-height:137px;overflow-x: auto;overflow-y: hidden; display: flex;justify-content: center;align-items: center;"
    >
      <div id="efContainer" ref="efContainer" class="container" v-show="nodeList.length>0">
        <template v-for="(node,i) in nodeList">
          <flow-node :id="i+1" :key="i" :node="node" :activeElement="activeElement"></flow-node>
        </template>
      </div>
      <a-spin v-if="efLoading" />
      <a-empty description="暂无流程图数据" :image="simpleImage" v-if="nodeList.length==0&&!efLoading" />
    </div>
    <a-divider orientation="left">应用明细</a-divider>
    <div class="mt20">
      <a-descriptions size="small" :column="isMobile ? 1 : 3" layout="vertical" bordered>
        <a-descriptions-item label="工程名称">
          {{
          releaseInfo.engineerName
          }}
        </a-descriptions-item>
        <a-descriptions-item label="工程更新时间">
          {{
          releaseInfo.releaseJobUpdatetime?releaseInfo.releaseJobUpdatetime:'无'
          }}
        </a-descriptions-item>
        <a-descriptions-item label="工程创建时间">
          {{
          releaseInfo.releaseJobCreatetime
          }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-badge
            :status="releaseInfo.releaseJobStatus | statusTypeFilter"
            :text="releaseInfo.releaseJobStatus | statusFilter"
          />
        </a-descriptions-item>
        <a-descriptions-item label="归属云">
          {{
          releaseInfo.releaseCloudName
          }}
        </a-descriptions-item>
        <a-descriptions-item label="集群名称">
          {{
          releaseInfo.clusterName
          }}
        </a-descriptions-item>
        <a-descriptions-item label="命名空间">
          {{
          releaseInfo.releaseJobNamespace
          }}
        </a-descriptions-item>
        <a-descriptions-item label="发布版本号">
          {{
          releaseInfo.releaseVersion
          }}
        </a-descriptions-item>
        <a-descriptions-item label="发布分支">
          {{
          releaseInfo.releaseJobBranch
          }}
        </a-descriptions-item>
        <a-descriptions-item label="发布的CommitID">
          {{
          releaseInfo.releaseCommitId
          }}
        </a-descriptions-item>
        <a-descriptions-item label="发布的负责人">
          {{
          releaseInfo.releaseCommitAuthorName
          }}
        </a-descriptions-item>
        <a-descriptions-item label="发布时间">
          {{
          releaseInfo.releaseCommitAuthorCreatetime
          }}
        </a-descriptions-item>
      </a-descriptions>
    </div>
    <a-divider orientation="left" v-if="releaseInfo.releaseJobStatus==2">债务明细</a-divider>
    <div style="display: flex; margin-top: 20px;" v-if="releaseInfo.releaseJobStatus==2">
      <a-table
        ref="table"
        size="middle"
        style="width: 100%"
        :columns="columns"
        :dataSource="debtList"
        :loading="listLoading"
        :pagination="false"
        :rowKey="(record,index)=>{return index}"
        bordered
      >
        <span slot="star" slot-scope="text,record">
          <a-rate v-model="record.releaseDebtStar" disabled style="color: #ff9900" />
        </span>
      </a-table>
    </div>

    <!-- <el-card class="box-card" style="margin: 10px 10px 10px 10px">
      <el-breadcrumb separator-class="el-icon-arrow-right">
        <el-breadcrumb-item>发布信息</el-breadcrumb-item>
        <el-breadcrumb-item>决策建议</el-breadcrumb-item>
      </el-breadcrumb>
      <div style="margin-top: 20px">
        <el-alert title="智能AI发布系统提醒" type="warning" description="不建议发布该版本，因债务过多，有可能带来严重的体验损失，极大影响用户的使用。" :closable="false" show-icon>
        </el-alert>
      </div>
    </el-card>-->
  </div>
</template>

<script>
import '@/components/ef/jsplumb.js'
import { getReleaseInfo, getReleaseStages, getReleaseDetes } from '@/api/index'
import { easyFlowMixin } from '@/components/ef/mixins'
import { baseMixin } from '@/store/app-mixin'
import flowNode from '@/components/ef/node'
import { Empty } from 'ant-design-vue'
const columns = [
  {
    title: '债务编号',
    className: 'mw80',
    dataIndex: 'releaseDebtId',
  },
  {
    title: '债务名称',
    className: 'mw120',
    dataIndex: 'releaseDebtName',
  },
  {
    title: '债务星级',
    className: 'mw180',
    dataIndex: 'releaseDebtStar',
    scopedSlots: { customRender: 'star' },
  },
  {
    title: '债务说明/解决建议',
    className: 'mw220',
    dataIndex: 'releaseDebtInfo',
  },
]
const statusMap = {
  0: { status: 'default', text: '等待中' },
  1: { status: 'processing', text: '构建中' },
  2: { status: 'processing', text: '待发布' },
  3: { status: 'processing', text: '发布中' },
  4: { status: 'success', text: '已发布' },
  5: { status: 'warning', text: '回滚中' },
  6: { status: 'warning', text: '已回滚' },
  7: { status: 'error', text: '发布异常' },
}
export default {
  props: {
    releaseId: {
      type: Number,
      required: true,
    },
  },
  // 一些基础配置移动该文件中
  mixins: [easyFlowMixin, baseMixin],
  components: {
    flowNode,
  },
  filters: {
    statusFilter(type) {
      return statusMap[type].text
    },
    statusTypeFilter(type) {
      return statusMap[type].status
    },
  },

  data() {
    this.columns = columns
    return {
      simpleImage: Empty.PRESENTED_IMAGE_SIMPLE,
      // jsPlumb 实例
      jsPlumb: null,
      // 控制画布销毁
      easyFlowVisible: true,
      // 控制流程数据显示与隐藏
      flowInfoVisible: false,
      // 是否加载完毕标志位
      loadEasyFlowFinish: false,
      flowHelpVisible: false,
      // 数据
      data: {},
      // 激活的元素、可能是节点、可能是连线
      activeElement: {
        // 可选值 node 、line
        type: undefined,
        // 节点ID
        nodeId: undefined,
        // 连线ID
        sourceId: undefined,
        targetId: undefined,
      },
      zoom: 0.5,
      //债务列表
      debtList: [],
      //发布信息
      releaseInfo: {
        releaseId: '',
        engineerName: '',
        releaseCloudName: '',
        clusterName: '',
        releaseJobStatus: 0,
        releaseJobNamespace: '',
        releaseVersion: '',
        releaseCommitId: '',
        releaseJobBranch: '',
        releaseJobUpdatetime: '',
        releaseJobCreatetime: '',
        releaseCommitAuthorName: '',
        releaseCommitAuthorCreatetime: '',
        clusterInstanceId: '',
        clusterRegionId: '',
        clusterZoneId: '',
      },
      nodeList: [],
      efLoading: true,
      listLoading: false,
      loading1: false,
      loading2: false,
      timer: undefined,
    }
  },

  methods: {
    format(percentage) {
      return percentage === 100 ? '100%' : `${percentage}%`
    },
    /**
     * 初始化
     */
    initData() {
      this.loading1 = true
      this.loading2 = true
      this.getReleaseInfo()
      this.getReleaseDetes()
      this.getReleaseStages()
      //轮询查看进度
      this.timer = setInterval(() => {
        this.getReleaseStages()
      }, 5000)
    },
    /**
     * 获取基本信息
     */
    getReleaseInfo() {
      getReleaseInfo({ releaseId: this.releaseInfo.releaseId })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.releaseInfo = { ...data }
          } else {
            this.$message.error(msg)
          }
          this.loading2 = false
        })
        .catch((erro) => {})
    },
    /**
     * 获取流程信息
     */
    getReleaseStages() {
      getReleaseStages({ releaseId: this.releaseInfo.releaseId })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.nodeList = [...data]
            console.log('data', data)
            this.efLoading = false
            this.dataReload()
            if (
              !data.find((item) => item.stageStatus == 'standby') &&
              !data.find((item) => item.stageStatus == 'running') &&
              this.timer
            ) {
              clearInterval(this.timer)
            }
          } else {
            this.$message.error(msg)
          }
        })
        .catch((erro) => {})
    },
    /**
     * 获取债务信息
     */
    getReleaseDetes() {
      getReleaseDetes({ releaseId: this.releaseInfo.releaseId })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.debtList = [...data]
          } else {
            this.$message.error(msg)
          }
        })
        .catch((erro) => {})
    },
    jsPlumbInit() {
      this.jsPlumb.ready(() => {
        // 导入默认配置
        this.jsPlumb.importDefaults(this.jsplumbSetting)
        // 会使整个jsPlumb立即重绘。
        this.jsPlumb.setSuspendDrawing(false, true)
        // 初始化节点
        this.loadEasyFlow()
        // 连线
        // this.jsPlumb.bind("connection", evt => {

        // });
        this.jsPlumb.setContainer(this.$refs.efContainer)
        this.loading1 = false
      })
    },
    // 加载流程图
    loadEasyFlow() {
      // 初始化连线
      for (let i = 1; i <= this.nodeList.length; i++) {
        let connParam = {
          source: i.toString(),
          target: (i + 1).toString(),
          label: '',
          connector: 'Flowchart',
          anchors: undefined,
          paintStyle: { stroke: 'lightgray', strokeWidth: 3 },
        }
        this.jsPlumb.connect(connParam, this.jsplumbConnectOptions)
      }
    },
    // 加载流程图
    dataReload() {
      this.easyFlowVisible = true
      this.$nextTick(() => {
        this.easyFlowVisible = true
        this.$nextTick(() => {
          this.jsPlumb = jsPlumb.getInstance()
          this.$nextTick(() => {
            this.jsPlumbInit()
          })
        })
      })
    },
  },
  created() {
    this.releaseInfo.releaseId = this.releaseId
  },
  //生命周期 - 挂载完成（可以访问DOM元素）
  mounted() {
    this.jsPlumb = jsPlumb.getInstance()
    this.$nextTick(() => {
      // 默认加载流程A的数据、在这里可以根据具体的业务返回符合流程数据格式的数据即可
      this.initData()
    })
  },
  beforeCreate() {}, //生命周期 - 创建之前
  beforeMount() {}, //生命周期 - 挂载之前
  beforeUpdate() {}, //生命周期 - 更新之前
  updated() {}, //生命周期 - 更新之后
  beforeDestroy() {
    //销毁定时器
    if (this.timer) clearInterval(this.timer)
  }, //生命周期 - 销毁之前
}
</script>
