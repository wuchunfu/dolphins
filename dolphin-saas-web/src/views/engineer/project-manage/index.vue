<template>
  <div>
    <a-card :bordered="false">
      <a-form layout="inline" v-model="searchForm" v-show="showSearch" class="mb20">
        <a-form-item label="工程名称">
          <a-input
            v-model="searchForm.engineerName"
            placeholder="请输入工程名称"
            @keyup.enter.native="search"
          />
        </a-form-item>

        <a-form-item label="工程语言">
          <a-select
            v-model="searchForm.engineerLanguageId"
            placeholder="请选择工程语言"
            class="mw180"
            @change="languageChange"
          >
            <a-select-option v-for="d in languageList" :value="d.value" :key="d.value">{{ d.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="工程框架">
          <a-select v-model="searchForm.engineerFrameworkId" placeholder="请选择工程框架" class="mw180">
            <a-select-option
              v-for="d in frameworkList"
              :value="d.value"
              :key="d.value"
            >{{ d.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="归属云">
          <a-select v-model="searchForm.engineerCloudId" placeholder="请选择归属云" class="mw180">
            <a-select-option v-for="d in cloudList" :value="d.value" :key="d.value">{{ d.label }}</a-select-option>
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
        :loading="listLoading"
        :pagination="pagination"
        @change="tableChange"
        :rowKey="(record,index)=>{return index}"
      >
        <span slot="engineerName" slot-scope="text,record">
          <a-tooltip
            :title="'仓库地址: ' + record.engineerGiturl"
            placement="top"
            arrow-point-at-center
          >
            <span>{{text}}</span>
          </a-tooltip>
        </span>
        <span slot="status" slot-scope="text">
          <a-badge :status="text | statusTypeFilter" :text="text | statusFilter" />
        </span>
        <span slot="languageAndFramework" slot-scope="text,record">
          {{text}}
          <br />
          {{record.engineerFrameworkId}}
        </span>
        <span slot="updateTime" slot-scope="text">
          <span>{{text||'无'}}</span>
        </span>
        <span slot="remark" slot-scope="text">
          <span>{{text||'无'}}</span>
        </span>
        <span slot="action" slot-scope="text, record">
          <a @click="goDetails(record.engineerId)">查看</a>
        </span>
      </a-table>
    </a-card>
    <a-drawer
      title="创建工程"
      :width="userOptions.type=='enterprise'?1200:700"
      :visible.sync="createDrawer"
      :body-style="{'padding-top': '18px', paddingBottom: '80px' }"
      @close="()=>{createDrawer=false,loading=false}"
    >
      <create-form ref="createForm" v-if="createDrawer" :options="userOptions" />
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
      :width="650"
      :visible.sync="detailDrawer"
      :body-style="{'padding-top': '0px' }"
      @close="()=>{detailDrawer=false,loading=false}"
    >
      <detail-form
        ref="detailForm"
        v-if="detailDrawer"
        :engineerId="engineerId"
        :options="userOptions"
      />
    </a-drawer>
  </div>
</template>

<script>
import {
  engineerList,
  judgmentEngineer,
  createEngineer,
  getDockerfileSelect,
  getRulesGroupSelect,
  getLanguageSelect,
  getFrameWorkSelect,
  getUserOptions,
  getCloudLists,
} from '@/api/index'
import toolbar from '@/mixins/toolbar-mixin'
import CreateForm from './components/create'
import DetailForm from './components/detail'
const columns = [
  {
    title: '工程名称',
    dataIndex: 'engineerName',
    scopedSlots: { customRender: 'engineerName' },
  },
  {
    title: '工程状态',
    dataIndex: 'engineerStatus',
    width: '80px',
    scopedSlots: { customRender: 'status' },
  },
  {
    title: '语言/框架',
    dataIndex: 'engineerLanguageId',
    scopedSlots: { customRender: 'languageAndFramework' },
  },
  {
    title: '业务发布策略组',
    className: 'mw120',
    dataIndex: 'engineerReleaseRulesId',
  },
  {
    title: '创建时间',
    className: 'mw120',
    dataIndex: 'engineerCreatetime',
  },
  {
    title: '更新时间',
    className: 'mw120',
    dataIndex: 'engineerUpdatetime',
    scopedSlots: { customRender: 'updateTime' },
  },
  {
    title: '备注',
    dataIndex: 'engineerRemark',
    scopedSlots: { customRender: 'remark' },
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
  0: { status: 'processing', text: '待创建' },
  1: { status: 'processing', text: '创建中' },
  2: { status: 'success', text: '已创建' },
  3: { status: 'error', text: '已失效' },
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
        engineerName: undefined,
        engineerLanguageId: undefined,
        engineerFrameworkId: undefined,
      },
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

      statusList: [
        { type: '', label: '待初始化' },
        { type: 'success', label: '启用' },
        { type: 'danger', label: '异常' },
      ],
      //表格Loading
      listLoading: false,
      //云厂商列表
      cloudList: [],
      //开发语言
      languageList: [],
      //开发框架
      frameworkList: [],
      //用户归属信息
      userOptions: {},
      loading: false,
      //抽屉显示隐藏
      createDrawer: false,
      detailDrawer: false,
      engineerId: 0,
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
      engineerList({ ...{ page: current, pageSize }, ...this.searchForm })
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
    judgmentEngineer() {
      let _this = this
      this.$destroyAll()
      judgmentEngineer()
        .then(({ status, data, msg }) => {
          if (status != 1) {
            this.$confirm({
              title: '提示',
              content: '当前暂未创建项目工程',
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
     * 详情
     */
    goDetails(id) {
      this.engineerId = id
      this.detailDrawer = true
    },
    create() {
      this.createDrawer = true
      getUserOptions()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.userOptions = data
            console.log('data', data)
          } else {
            this.$message.error(msg)
          }
        })
        .catch((err) => {})
      this.$nextTick(function () {
        this.$refs.createForm.form.resetFields()
      })
    },
    /**
     * 创建标准工程
     */
    submit() {
      this.loading = true
      let validateFields = this.$refs.createForm.form.validateFields
      const validateFieldsKey = [
        'engineerName',
        'engineerDockerfileId',
        'engineerFrameworkId',
        'engineerCloudId',
        'engineerCodeing',
        'engineerDevops',
        'engineerLanguageId',
        'engineerReleaseRulesId',
        'engineerGitGroupId',
        'engineerRemark',
        'engineerSecurity',
        'engineerVocational',
      ]
      validateFields(validateFieldsKey, { force: true }, (err, values) => {
        if (!err) {
          createEngineer({ ...values })
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
     * 获取dockerfile的下拉信息
     */
    getDockerfile() {
      getDockerfileSelect()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            console.log('getDockerfile', data)
            this.dockerFileList = Array.isArray(data) ? data : []
          } else {
            this.$message.error(msg)
          }
        })
        .catch((error) => {})
    },
    /**
     * 开发语言下拉
     */
    getLanguage() {
      getLanguageSelect()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.languageList = Array.isArray(data) ? data : []
          } else {
            this.$message.error(msg)
          }
        })
        .catch((error) => {})
    },
    /**
     * 开发框架下拉
     */
    languageChange() {
      getFrameWorkSelect({ LanguageId: this.searchForm.engineerLanguageId })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.frameworkList = Array.isArray(data) ? data : []
            this.searchForm.engineerFrameworkId = undefined
          } else {
            this.$message.error(msg)
          }
        })
        .catch((error) => {})
    },
  },
  created() {
    this.getCloudList()
    this.getList()
    this.getLanguage()
    this.judgmentEngineer()
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
