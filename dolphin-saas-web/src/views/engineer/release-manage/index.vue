<template>
  <div>
    <a-card :bordered="false">
      <a-form layout="inline" v-model="searchForm" v-show="showSearch" class="mb20">
        <a-form-item label="工程名称">
          <a-input
            v-model="searchForm.releaseJobName"
            placeholder="请输入工程名称"
            @keyup.enter.native="search"
          />
        </a-form-item>

        <a-form-item label="版本号">
          <a-input
            v-model="searchForm.releaseVersion"
            placeholder="请输入版本号"
            @keyup.enter.native="search"
          />
        </a-form-item>
        <a-form-item>
          <a-range-picker v-model="dateRange" @change="onChange" format="YYYY-MM-DD">
            <a-icon slot="suffixIcon" type="calendar" />
          </a-range-picker>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click.native.prevent="search">查询</a-button>
          <a-button style="margin-left: 8px" @click="() => {searchForm = {};dateRange=[]}">重置</a-button>
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
        :loading="listLoading"
        :pagination="pagination"
        @change="tableChange"
        :rowKey="(record,index)=>{return index}"
      >
        <span slot="releaseJob" slot-scope="text,record">
          {{ record.releaseJobCloudId }}
          <br />
          {{record.releaseJobNamespaceId}}
        </span>
        <span slot="releaseJobBranch" slot-scope="text">
          <a-tag size="small" color="blue">{{ text}}</a-tag>
        </span>
        <span slot="status" slot-scope="text">
          <a-badge :status="text | statusTypeFilter" :text="text | statusFilter" />
        </span>
        <span slot="action" slot-scope="text, record">
          <template>
            <!-- <a v-if="record.releaseJobStatus == 2" @click="executeRollback(record.releaseId,2)">执行发布</a>
            <a-popover placement="topLeft" width="500" trigger="click">
              <template slot="content">
                <a-table
                  :dataSource="rollbackList"
                  :columns="rollbackColumns"
                  showPagination="auto"
                  :pagination="false"
                  :rowKey="(record,index)=>{return index}"
                >
                  <span slot="action" slot-scope="record1">
                    <a @click="executeRollback(record.releaseId,3,record1.version)">回滚此版本</a>
                  </span>
                </a-table>
              </template>
              <a v-if="record.releaseJobStatus ==4" @click="getRollback(record.releaseId)">执行回滚</a>
            </a-popover>
            <a-divider
              v-if="record.releaseJobStatus ==4||record.releaseJobStatus == 2"
              type="vertical"
            />-->
            <a @click="goDetails(record.releaseId,record.releaseJobStatus)">查看</a>
            <!-- <a @click="executeRollback(record.releaseId,1)" v-if="record.releaseJobStatus==0">构建工程</a> -->
          </template>
        </span>
      </a-table>
    </a-card>
    <a-drawer
      title="创建发布"
      :width="600"
      :visible.sync="createDrawer"
      :body-style="{'padding-top': '5px', paddingBottom: '80px' }"
      @close="()=>{createDrawer=false,loading=false}"
    >
      <create-form ref="createForm" :cloudList="cloudList" v-if="createDrawer" />
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
        <a-button
          :style="{ marginRight: '20px' }"
          @click="()=>{createDrawer=false,loading=false}"
        >取消</a-button>
        <a-button :loading="loading" type="primary" @click.native.prevent="submit">
          <span v-if="!loading">添 加</span>
          <span v-else>添 加 中</span>
        </a-button>
      </div>
    </a-drawer>
    <a-drawer
      title="发布详情"
      :width="800"
      :visible.sync="detailDrawer"
      :body-style="{'padding-top': '0px' , paddingBottom: '80px' }"
      @close="()=>{detailDrawer=false,loading=false}"
    >
      <detail-form ref="detailForm" v-if="detailDrawer" :releaseId="releaseId" />
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
        <a-button
          :style="{ marginRight: '20px' }"
          @click="()=>{detailDrawer=false,loading=false}"
        >返回</a-button>
        <a-button
          :loading="loading"
          v-if="JobStatus==0"
          type="primary"
          @click="executeRollback(releaseId,1)"
        >
          <span v-if="!loading">构建工程</span>
          <span v-else>构建工程中</span>
        </a-button>
        <a-button
          :loading="loading"
          v-if="JobStatus==2"
          type="primary"
          @click="executeRollback(releaseId,2)"
        >
          <span v-if="!loading">执行发布</span>
          <span v-else>发布中</span>
        </a-button>

        <a-popover placement="topLeft" width="500" trigger="click">
          <template slot="content">
            <a-table
              :dataSource="rollbackList"
              :columns="rollbackColumns"
              showPagination="auto"
              :pagination="false"
              :rowKey="(record,index)=>{return index}"
            >
              <span slot="action" slot-scope="record1">
                <a @click="executeRollback(releaseId,3,record1.version)">回滚此版本</a>
              </span>
            </a-table>
          </template>
          <a-button
            :loading="loading"
            v-if="JobStatus==4"
            type="primary"
            @click="getRollback(releaseId)"
          >
            <span v-if="!loading">执行回滚</span>
            <span v-else>回滚中</span>
          </a-button>
        </a-popover>
      </div>
    </a-drawer>
  </div>
</template>

<script>
import {
  judgmentRelease,
  releaseList,
  releaseExecute,
  getRollbackList,
  createRelease,
  getCloudLists,
} from '@/api/index'
import toolbar from '@/mixins/toolbar-mixin'
import CreateForm from './components/create'
import DetailForm from './components/detail'
import moment from 'moment'
const columns = [
  {
    title: '工程名称',
    dataIndex: 'releaseJobName',
  },
  {
    title: '版本号',
    dataIndex: 'releaseVersion',
  },
  {
    title: '发布环境',
    dataIndex: 'releaseJobCloudId',
    scopedSlots: { customRender: 'releaseJob' },
  },
  {
    title: '发布分支',
    dataIndex: 'releaseJobBranch',
    scopedSlots: { customRender: 'releaseJobBranch' },
  },
  {
    title: '工程创建时间',
    dataIndex: 'releaseJobCreatetime',
    scopedSlots: { customRender: 'createtime' },
  },
  {
    title: '发布状态',
    dataIndex: 'releaseJobStatus',
    className: 'mw80',
    scopedSlots: { customRender: 'status' },
  },
  {
    title: '操作',
    align: 'center',
    width: '150px',
    scopedSlots: { customRender: 'action' },
  },
]
const rollbackColumns = [
  {
    title: '命名空间',
    dataIndex: 'namespace',
  },
  {
    title: '发布时间',
    className: 'mw100',
    dataIndex: 'updatetime',
  },
  {
    title: '版本',
    className: 'mw100',
    dataIndex: 'version',
  },
  {
    title: '操作',
    align: 'center',
    className: 'mw90',
    scopedSlots: { customRender: 'action' },
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
  mixins: [toolbar],
  components: { CreateForm, DetailForm },
  filters: {
    statusFilter(type) {
      return statusMap[type].text
    },
    statusTypeFilter(type) {
      return statusMap[type].status
    },
  },
  data() {
    return {
      /** 搜索 */
      searchForm: {
        releaseJobName: undefined,
        releaseVersion: undefined,
        releaseJobCreatetime: undefined,
        releaseJobUpdatetime: undefined,
      },
      dateRange: [],
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
      columns,
      rollbackColumns,
      //表格Loading
      listLoading: false,
      //回滚列表
      rollbackList: [],
      cloudList: [],
      loading: false,
      //抽屉显示隐藏
      createDrawer: false,
      detailDrawer: false,
      releaseId: 0,
      JobStatus: 0,
    }
  },
  methods: {
    /**
     * 搜索
     */
    search() {
      console.log('searchForm', this.searchForm)
      this.pagination.current = 1
      this.getList()
    },
    onChange(date, dateString) {
      this.searchForm.releaseJobCreatetime = dateString[0]
      this.searchForm.releaseJobUpdatetime = dateString[1]
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
      releaseList({ ...{ page: current, pageSize }, ...this.searchForm })
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
    judgmentRelease() {
      let _this = this
      this.$destroyAll()
      judgmentRelease()
        .then(({ status, data, msg }) => {
          if (status != 1) {
            this.$confirm({
              title: '提示',
              content: '当前暂未创建发布',
              okText: '去创建',
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
     * 创建发布
     */
    create() {
      this.createDrawer = true
      this.getCloudList()
      this.$nextTick(function () {
        this.$refs.createForm.form.resetFields()
      })
    },
    //获取云厂商列表
    getCloudList() {
      getCloudLists()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            console.log('cloudList', data)
            this.cloudList = data || []
          } else {
            this.$message.error(msg)
          }
        })
        .catch(() => {})
    },
    /**
     * 创建标准发布
     */
    submit() {
      this.loading = true
      let validateFields = this.$refs.createForm.form.validateFields
      const validateFieldsKey = [
        'engineerId',
        'branchName',
        'jobCloudId',
        'clusterId',
        'nameSpace',
        'commitId',
        'authorName',
        'commitTime',
        'javaMoule',
      ]
      validateFields(validateFieldsKey, { force: true }, (err, values) => {
        if (!err) {
          console.log('values', values)
          createRelease({ ...values, ...{ jobCloudId: undefined } })
            .then(({ status, data, msg }) => {
              if (status === 1) {
                this.$message.success(msg)
                this.pagination.current = 1
                this.getList()
                this.createDrawer = false
              } else {
                this.$message.error(msg)
              }
              this.loading = false
            })
            .catch(() => {
              this.loading = false
            })
        } else {
          this.loading = false
          return false
        }
      })
    },
    /**
     * 查看k8s详情
     */
    goDetails(id, JobStatus) {
      this.releaseId = id
      this.JobStatus = JobStatus
      this.detailDrawer = true
    },
    /**
     * 执行发布和回滚
     */
    executeRollback(releaseId, status, version) {
      let _this = this
      let text = { 1: '构建', 2: '发布', 3: '回滚' }
      this.$confirm({
        title: '提示',
        content: `此操作将执行${text[status]}, 是否继续?`,
        okText: '确定',
        maskClosable: true,
        onOk() {
          _this.loading = true
          releaseExecute(
            status == 3 ? { releaseId, status, version } : { releaseId, status }
          )
            .then(({ status, data, msg }) => {
              if (status === 1) {
                _this.$message.success(msg)
                _this.pagination.current = 1
                _this.getList()
                _this.detailDrawer = false
              } else {
                _this.$message.error(msg)
              }
            })
            .catch((error) => {})
            .finally(() => {
              _this.loading = false
            })
          return new Promise((resolve, reject) => {
            resolve()
          }).catch(() => console.log('Oops errors!'))
        },
        onCancel() {},
      })
    },
    getRollback(releaseId) {
      getRollbackList({ releaseId })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.rollbackList = [...data]
          } else {
            this.$message.error(msg)
          }
        })
        .catch((error) => {})
    },
  },
  created() {
    this.getList()
    this.judgmentRelease()
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
