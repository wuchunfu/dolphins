<template>
  <div>
    <a-card :bordered="false">
      <a-form layout="inline" v-model="searchForm" v-show="showSearch" class="mb20">
        <a-form-item label="集群实例ID">
          <a-input
            v-model="searchForm.clusterInstanceId"
            placeholder="集群实例ID"
            @keyup.enter.native="search"
          />
        </a-form-item>
        <a-form-item label="归属云">
          <a-select
            v-model="searchForm.clusterCloudId"
            placeholder="请选择归属云"
            class="mw180"
            @change="search"
          >
            <a-select-option v-for="d in cloudList" :value="d.value" :key="d.value">{{ d.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="集群状态">
          <a-select
            v-model="searchForm.clusterServiceStatus"
            placeholder="请选择集群状态"
            class="mw180"
            @change="search"
          >
            <a-select-option v-for="d in statusMap" :value="d.value" :key="d.value">{{ d.text }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click.native.prevent="search">查询</a-button>
          <a-button style="margin-left: 8px" @click="() => searchForm = {}">重置</a-button>
        </a-form-item>
      </a-form>
      <a-row :gutter="10" type="flex" justify="space-between">
        <a-col :span="12">
          <a-button type="primary" icon="plus" @click="create" ghost>创建</a-button>
        </a-col>
        <a-col :span="2">
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </a-col>
      </a-row>
      <div class="table-operator"></div>
      <a-table
        ref="table"
        size="middle"
        :columns="columns"
        :dataSource="list"
        :pagination="pagination"
        @change="tableChange"
        :loading="listLoading"
        :rowKey="(record,index)=>{return index}"
      >
        <span slot="ID" slot-scope="text">{{text||'待初始化'}}</span>
        <span slot="status" slot-scope="text">
          <a-badge :status="text | statusTypeFilter" :text="text | statusFilter" />
        </span>
        <span
          slot="region"
          slot-scope="text,record"
        >{{record.clusterCloudId }}/{{record.clusterRegionId}}</span>

        <span slot="updateTime" slot-scope="text">
          <span>{{text||'无'}}</span>
        </span>

        <span slot="action" slot-scope="text, record">
          <template>
            <a-button
              type="link"
              v-if="record.clusterServiceStatus == 0||record.clusterServiceStatus == 7||record.clusterServiceStatus == 8"
              disabled
            >查看</a-button>
            <!-- <a v-if="record.clusterServiceStatus == 1" @click="goJumpDetails(record.clusterId)">查看</a> -->
            <a
              v-if="record.clusterServiceStatus> 1&&record.clusterServiceStatus<3||record.clusterServiceStatus ==4"
              @click="goDetaulsRead(record.clusterId)"
            >查看</a>
            <a v-if="record.clusterServiceStatus ==3" @click="goDetails(record.clusterId)">查看</a>
            <!-- <a-divider v-if="record.clusterServiceStatus ==3" type="vertical" />
            <a v-if="record.clusterServiceStatus ==3" @click="recycle(record.clusterId)">回收</a>-->
            <!-- <a v-if="record.clusterServiceStatus ==4" @click="hint">查看</a> -->
          </template>
        </span>
      </a-table>
    </a-card>
    <a-drawer
      title="创建集群"
      :width="700"
      :visible.sync="createDrawer"
      :body-style="{'padding-top': '18px', paddingBottom: '80px' }"
      @close="close"
    >
      <create-step ref="createForm" :cloudList="cloudList" v-if="createDrawer" @close="close" />
    </a-drawer>
    <a-drawer
      title="集群详情"
      :width="600"
      :visible.sync="detailDrawer"
      :body-style="{'padding-top': '0px'}"
      @close="()=>{detailDrawer=false,loading=false}"
    >
      <detail-from :clusterId="clusterId" v-if="detailDrawer" />
    </a-drawer>
    <!-- 待决策查看 -->
    <!-- <a-drawer
      title="待决策查看"
      :width="800"
      :visible.sync="jumpDrawer"
      :body-style="{'padding-top': '0px', paddingBottom: '80px' }"
      @close="()=>{jumpDrawer=false,loading=false}"
    >
      <jump-detail :clusterId="clusterId" v-if="jumpDrawer" />
      <div
        :style="{
          position: 'absolute',
          right: 0,
          bottom: 0,
          width: '100%',
          borderTop: '1px solid #e9e9e9',
          padding: '10px 16px',
          background: '#fff',
          textAlign: 'left',
          zIndex: 1,
        }"
      >
        <a-button :style="{ marginRight: '20px' }" @click="()=>{jumpDrawer=false,loading=false}">取消</a-button>
        <a-button
          :loading="loading"
          type="primary"
          @click.native.prevent="submitPurchase(clusterId)"
        >
          <span v-if="!loading">确认采购并部署</span>
          <span v-else>采购并部署中...</span>
        </a-button>
      </div>
    </a-drawer>-->
    <!-- 2-4状态内容 -->
    <a-drawer
      title="查看集群"
      :width="800"
      :visible.sync="readDrawer"
      :body-style="{'padding-top': '0px'}"
      @close="()=>{readDrawer=false,loading=false}"
    >
      <read-detail :clusterId="clusterId" v-if="readDrawer" />
    </a-drawer>
  </div>
</template>

<script>
import {
  clusterlists,
  judgmentCluster,
  getCloudLists,
  execClusterDeploy,
  recycleClusterDeploy,
} from '@/api/index'
import toolbar from '@/mixins/toolbar-mixin'
import CreateStep from './components/createStep'
import DetailFrom from './components/detail'
import JumpDetail from './components/jumpDetail'
import ReadDetail from './components/readDetail'

const columns = [
  {
    title: '实例ID',
    className: 'mw120',
    dataIndex: 'clusterInstanceId',
    scopedSlots: { customRender: 'ID' },
  },
  {
    title: '状态',
    className: 'mw90',
    dataIndex: 'clusterServiceStatus',
    scopedSlots: { customRender: 'status' },
  },
  {
    title: '归属',
    className: 'mw150',
    dataIndex: 'clusterRegionId',
    scopedSlots: { customRender: 'region' },
  },

  {
    title: '创建时间',
    className: 'mw90',
    dataIndex: 'clusterCreatetime',
  },
  {
    title: '更新时间',
    className: 'mw90',
    dataIndex: 'clusterUpdatetime',
    scopedSlots: { customRender: 'updateTime' },
  },
  {
    title: '付费类型',
    className: 'mw90',
    dataIndex: 'clusterType',
  },
  {
    title: '操作',
    dataIndex: 'action',
    align: 'center',
    width: '150px',
    scopedSlots: { customRender: 'action' },
  },
]
const statusMap = {
  0: { value: 0, status: 'processing', text: '初始化(搭建)' },
  1: { value: 1, status: 'processing', text: '待决策' },
  2: { value: 2, status: 'processing', text: '配置部署中' },
  3: { value: 3, status: 'success', text: '运行中' },
  4: { value: 4, status: 'error', text: '异常' },
  5: { value: 5, status: 'default', text: '停用' },
  6: { value: 6, status: 'warning', text: '回收中' },
}
export default {
  mixins: [toolbar],
  components: {
    CreateStep,
    DetailFrom,
    JumpDetail,
    ReadDetail,
  },
  data() {
    return {
      // 高级搜索 展开/关闭
      advanced: false,
      /** 搜索查询参数 */
      searchForm: { clusterInstanceId: undefined, clusterCloudId: undefined },
      /**分页 */
      pagination: {
        current: 1, //当前页
        pageSize: 10, //每页条数
        total: 0, //总条数
        showQuickJumper: true,
        hideOnSinglePage: true,
        showTotal: (total) => `共 ${total} 条数据`,
      },
      /**table数据 */
      list: [],
      statusMap,
      columns,
      //云厂商列表
      cloudList: [],
      //区域
      regionList: [],
      //表格Loading
      listLoading: false,
      //抽屉提交按钮loading
      loading: false,
      //抽屉显示隐藏
      createDrawer: false,
      detailDrawer: false,
      jumpDrawer: false,
      readDrawer: false,
      //详情id
      clusterId: '',
    }
  },
  filters: {
    statusFilter(type) {
      return statusMap[type].text
    },
    statusTypeFilter(type) {
      return statusMap[type].status
    },
  },
  methods: {
    /**
     * 搜索
     */
    search() {
      this.pagination.current = 1
      this.getList()
    },
    //分页
    tableChange(pagination) {
      this.pagination = pagination
      this.getList()
    },
    /**
     * 获取列表
     */
    getList() {
      this.listLoading = true
      const { current, pageSize } = this.pagination
      clusterlists({ ...{ page: current, pageSize }, ...this.searchForm })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            const { page, pageSize, total, list } = data
            this.list = list || []
            this.pagination = {
              current: page,
              pageSize,
              total,
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
    //查看当前是否创建集群
    judgmentCluster() {
      let _this = this
      this.$destroyAll()
      judgmentCluster()
        .then(({ status, data, msg }) => {
          if (status != 1) {
            this.$confirm({
              title: '提示',
              content: '当前暂未创建集群',
              okText: '去绑定',
              maskClosable: true,
              onOk() {
                _this.create()
                return new Promise((resolve, reject) => {
                  resolve()
                }).catch(() => console.log('Oops errors!'))
              },
              onCancel() {},
            })
          }
        })
        .catch((error) => {})
    },
    /**
     * 创建集群
     */
    create() {
      this.createDrawer = true
      this.getCloudList()
      // this.$nextTick(function () {
      //   this.$refs.createForm.form.resetFields()
      // })
    },
    //获取云厂商列表
    getCloudList() {
      getCloudLists()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.cloudList = data || []
          } else {
            this.$message.error(msg)
          }
        })
        .catch(() => {})
    },
    /**
     * 查看待决策
     */
    goJumpDetails(cid) {
      this.clusterId = cid
      this.jumpDrawer = true
    },
    /**
     * 决策选择
     */
    submitPurchase(cid) {
      execClusterDeploy({ cid }).then(({ status, data, msg }) => {
        if (status === 1) {
          this.jumpDrawer = false
          this.$message.success('提交后台部署成功，正在帮您创建！')
        } else {
          this.$message.error('执行部署有问题，请稍后再试!')
        }
      })
    },
    /**
     * 集群部署阶段2-4
     */
    goDetaulsRead(cid) {
      this.clusterId = cid
      this.readDrawer = true
    },
    /**
     * 查看详情
     */
    goDetails(id) {
      this.clusterId = id
      this.detailDrawer = true
    },
    //回收
    recycle(cid) {
      let _this = this
      this.$confirm({
        title: '提示',
        content: `此操作将执行集群回收, 是否继续?`,
        okText: '确定',
        maskClosable: true,
        onOk() {
          recycleClusterDeploy({ cid }).then(({ status, data, msg }) => {
            if (status === 1) {
              _this.$message.success(msg)
            } else {
              _this.$message.error(msg)
            }
          })
          return new Promise((resolve, reject) => {
            resolve()
          }).catch(() => console.log('Oops errors!'))
        },
        onCancel() {},
      })
    },
    hint() {
      this.$warning({
        title: '提示',
        content: '集群异常，请联系工作人员。',
        maskClosable: true,
      })
    },
    //搜素展开收起
    toggleAdvanced() {
      this.advanced = !this.advanced
    },
    close() {
      this.pagination.current = 1
      this.getList()
      this.createDrawer = false
    },
  },
  created() {
    this.getList()
    this.getCloudList()
    this.judgmentCluster()
  },
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
