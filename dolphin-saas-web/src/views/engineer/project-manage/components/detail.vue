<template>
  <div>
    <a-divider orientation="left">工程详情</a-divider>
    <a-descriptions size="small" :column="isMobile ? 1 : 3" layout="vertical" bordered>
      <a-descriptions-item label="工程名称">{{engineerInfo.engineerName}}</a-descriptions-item>
      <a-descriptions-item label="开发语言">{{engineerInfo.engineerLanguage}}</a-descriptions-item>
      <a-descriptions-item label="开发框架">{{engineerInfo.engineerFramework}}</a-descriptions-item>
      <a-descriptions-item label="业务发布策略组">{{engineerInfo.engineerReleaseRules}}</a-descriptions-item>
      <a-descriptions-item label="创建时间">{{engineerInfo.engineerCreatetime}}</a-descriptions-item>
      <a-descriptions-item label="更新时间">{{engineerInfo.engineerUpdatetime ||'无'}}</a-descriptions-item>

      <a-descriptions-item
        label="开发负责人"
        v-if="options.type=='enterprise'"
      >{{ engineerInfo.engineerCodeing||'未定义' }}</a-descriptions-item>
      <a-descriptions-item
        label="运维负责人"
        v-if="options.type=='enterprise'"
      >{{ engineerInfo.engineerDevops||'未定义' }}</a-descriptions-item>
      <a-descriptions-item
        label="安全负责人"
        v-if="options.type=='enterprise'"
      >{{ engineerInfo.engineerSecurity ||'未定义'}}</a-descriptions-item>
      <a-descriptions-item
        label="测试负责人"
        v-if="options.type=='enterprise'"
      >{{ engineerInfo.engineerTesting||'未定义' }}</a-descriptions-item>
      <a-descriptions-item
        label="业务负责人"
        v-if="options.type=='enterprise'"
      >{{ engineerInfo.engineerVocational||'未定义' }}</a-descriptions-item>

      <a-descriptions-item label="备注信息">{{ engineerInfo.engineerRemark||'无' }}</a-descriptions-item>
      <a-descriptions-item label="代码仓库地址">{{ engineerInfo.engineerGiturl }}</a-descriptions-item>
    </a-descriptions>
    <a-divider orientation="left">发布列表</a-divider>
    <a-table
      ref="table"
      size="middle"
      :columns="columns"
      :dataSource="releaseList"
      :loading="listLoading"
      :pagination="pagination"
      :rowKey="(record,index)=>{return index}"
      @change="tableChange"
    >
      <span slot="status" slot-scope="text">
        <a-badge :status="text | statusTypeFilter" :text="text | statusFilter" />
      </span>
      <span slot="ms" slot-scope="text">{{text|formatSeconds}}</span>
    </a-table>
  </div>
</template>
<script>
import { baseMixin } from '@/store/app-mixin'
import { getEngineerInfo, getEngineerReleaseList } from '@/api/index'
const columns = [
  {
    title: '工程ID',
    dataIndex: 'id',
  },
  {
    title: '创建时间',
    className: 'mw120',
    dataIndex: 'createtime',
  },
  {
    title: '云厂商',
    className: 'mw90',
    dataIndex: 'cloudId',
  },
  {
    title: '工程状态',
    className: 'mw90',
    dataIndex: 'status',
    scopedSlots: { customRender: 'status' },
  },

  {
    title: '发布耗时',
    className: 'mw90',
    dataIndex: 'ms',
    scopedSlots: { customRender: 'ms' },
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
  mixins: [baseMixin],
  props: {
    engineerId: {
      type: Number,
      required: true,
    },
    options: {
      type: Object,
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
    formatSeconds(value) {
      console.log('result', result)
      let result = parseInt(value)

      let h =
        Math.floor(result / 3600) < 10
          ? '0' + Math.floor(result / 3600)
          : Math.floor(result / 3600)
      let m =
        Math.floor((result / 60) % 60) < 10
          ? '0' + Math.floor((result / 60) % 60)
          : Math.floor((result / 60) % 60)
      let s =
        Math.floor(result % 60) < 10
          ? '0' + Math.floor(result % 60)
          : Math.floor(result % 60)

      let res = ''
      if (h !== '00') res += `${h}小时`
      if (m !== '00') res += `${m}分`
      res += `${s}秒`
      return res
    },
  },
  data() {
    this.columns = columns
    return {
      engineerInfo: {
        engineerId: '',
        engineerRemark: '',
        engineerDevops: '',
        engineerUpdatetime: '',
        engineerTesting: '',
        engineerGiturl: '',
        engineerReleaseRules: '',
        engineerFramework: '',
        engineerName: '',
        engineerSecurity: '',
        engineerStatus: '',
        engineerCodeing: '',
        engineerVocational: '',
        engineerDockerfile: '',
        engineerLanguage: '',
        engineerCreatetime: '',
      },
      releaseList: [],
      listLoading: false,
      /**分页 */
      pagination: {
        current: 1, //当前页
        pageSize: 10, //每页条数
        total: 0, //总条数
        showQuickJumper: true,
        hideOnSinglePage: true,
        showTotal: (total) => `共 ${total} 条数据`,
      },
    }
  },

  methods: {
    //分页
    tableChange(pagination) {
      this.pagination = pagination
      this.getList()
    },
    goDetails() {
      let engineerId = this.engineerId
      getEngineerInfo({ engineerId })
        .then(({ status, data, msg }) => {
          console.log('goDetails', engineerId)
          if (status === 1) {
            this.engineerInfo = { ...data }
          } else {
            this.$message.error(msg)
          }
        })
        .catch((err) => console.error(err))
      this.getList()
    },
    /**
     * 获取列表
     */
    getList() {
      this.listLoading = true
      console.log('getList')
      const { current, pageSize } = this.pagination
      getEngineerReleaseList({
        ...{ page: current, pageSize },
        ...{ engineerId: this.engineerId },
      })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            console.log('data', data)
            this.releaseList = data || []
            if (total) {
              this.pagination = {
                current: page,
                pageSize,
                total,
              }
            }
          } else {
            this.$message.error(msg)
          }
          this.listLoading = false
        })
        .catch((error) => {
          this.listLoading = false
        })
    },
  },
  created() {
    this.goDetails()
  },
}
</script>