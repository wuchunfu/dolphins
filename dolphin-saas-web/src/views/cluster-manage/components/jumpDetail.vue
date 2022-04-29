<template>
  <div>
    <a-divider orientation="left">基础信息</a-divider>
    <a-descriptions size="small" :column="isMobile ? 1 : 3" layout="vertical" bordered>
      <a-descriptions-item label="集群规模">{{clusterInfo.clusterAmounts||'无'}}</a-descriptions-item>
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
    <a-divider orientation="left">资源计算</a-divider>
    <a-table
      ref="table"
      size="middle"
      :columns="columns"
      :dataSource="Assetslist"
      :loading="listLoading"
      :pagination="false"
      :rowKey="(record,index)=>{return index}"
    >
      <span slot="clusterAssetsMoney" slot-scope="text">{{text}}/元</span>
      <span slot="clusterAssetsQuantity" slot-scope="text">{{text}}/个</span>
      <span slot="clusterAssetsRemark" slot-scope="text">{{text||"无"}}</span>
    </a-table>
  </div>
</template>
<script>
import { baseMixin } from '@/store/app-mixin'
import { getClusterInfo, getClusterAssets } from '@/api/index'
const columns = [
  {
    title: '资源名称',
    className: 'mw120',
    dataIndex: 'clusterAssetsName',
  },
  {
    title: '资源规格',
    className: 'mw120',
    dataIndex: 'clusterAssetsSpecification',
  },
  {
    title: '资源单价',
    className: 'mw90',
    dataIndex: 'clusterAssetsMoney',
    scopedSlots: { customRender: 'clusterAssetsMoney' },
  },
  {
    title: '资源数量',
    className: 'mw90',
    dataIndex: 'clusterAssetsQuantity',
    scopedSlots: { customRender: 'clusterAssetsQuantity' },
  },

  {
    title: '资源备注',
    className: 'mw90',
    dataIndex: 'clusterAssetsRemark',
    scopedSlots: { customRender: 'clusterAssetsRemark' },
  },
]
const statusMap = {
  0: { status: 'processing', text: '初始化(搭建)' },
  1: { status: 'processing', text: '待决策' },
  2: { status: 'processing', text: '配置部署中' },
  3: { status: 'success', text: '运行中' },
  4: { status: 'error', text: '异常' },
  5: { status: 'default', text: '停用' },
  6: { status: 'warning', text: '回收中' },
  // 0: { status: "processing", text: "初始化(搭建)" },
  // 1: { status: "processing", text: "待决策" },
  // 2: { status: "processing", text: "买资源中" },
  // 3: { status: "processing", text: "部署服务" },
  // 4: { status: "processing", text: "配置服务" },
  // 5: { status: "success", text: "运行中" },
  // 6: { status: "error", text: "异常" },
  // 7: { status: "default", text: "停用" },
  // 8: { status: "warning", text: "回收中" }
}
export default {
  mixins: [baseMixin],
  props: {
    clusterId: {
      type: Number,
      required: true,
    },
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
      clusterInfo: {},
      Assetslist: [],
      listLoading: false,
    }
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
          return getClusterAssets({ clusterId: this.clusterId })
        })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.Assetslist = data || []
            this.listLoading = false
          } else {
            this.listLoading = false
            this.$message.error(msg)
          }
        })
        .catch((error) => {
          this.listLoading = false
        })
    },
  },
  created() {
    this.getClusterInfo()
  },
}
</script>