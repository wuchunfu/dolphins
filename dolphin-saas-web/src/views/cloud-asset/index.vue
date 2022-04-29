<template>
  <div>
    <a-card :bordered="false">
      <a-form layout="inline" v-model="searchForm" v-show="showSearch" class="mb20">
        <a-form-item label="IP地址">
          <a-input v-model="searchForm.assetIp" placeholder="请输入IP地址" @keyup.enter.native="search" />
        </a-form-item>
        <a-form-item label="归属云">
          <a-select v-model="searchForm.cloudId" placeholder="请选择归属云" class="mw180">
            <a-select-option v-for="d in cloudList" :value="d.value" :key="d.value">{{ d.label }}</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="服务标签">
          <a-select v-model="searchForm.tagId" placeholder="请选择服务标签" class="mw180">
            <a-select-option v-for="d in serverList" :value="d.value" :key="d.value">{{ d.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="实例名称">
          <a-input
            v-model="searchForm.cvmTagName"
            placeholder="请输入实例名称"
            @keyup.enter.native="search"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click.native.prevent="search">查询</a-button>
          <a-button style="margin-left: 8px" @click="() => searchForm = {}">重置</a-button>
        </a-form-item>
      </a-form>
      <a-row :gutter="10" type="flex" justify="space-between">
        <a-col :span="12">
          <a-button type="primary" icon="sync" @click="updateAsset" ghost :loading="updateLoading">
            <span v-if="!updateLoading">加载云资产数据</span>
            <span v-else>加载云资产数据中...</span>
          </a-button>
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
        <span slot="cvmID" slot-scope="text,record">
          {{record.cvmInstanceId}}
          <br />
          {{record.cvmTagName}}
        </span>
        <span slot="IP" slot-scope="text,record">
          <span class="text-success">
            <span v-if="record.cvmClusterOutSideIP">{{record.cvmClusterOutSideIP}}(公)</span>
            <br />
            <span v-if="record.cvmClusterInSideIP">{{record.cvmClusterInSideIP}}(内)</span>
          </span>
        </span>
        <span slot="cvmCost" slot-scope="text">{{text }}天</span>
        <span slot="region" slot-scope="text,record">
          {{record.regionSource }}
          <br />
          {{record.cvmRegionId}}
        </span>
        <span slot="serviceLabel" slot-scope="text">
          <a-tag
            class="ml5 mb5"
            size="small"
            v-for="item in text"
            :key="item"
            effect="plain"
          >{{item }}</a-tag>
          <span v-if="text.length==0">无服务</span>
        </span>
        <span slot="status" slot-scope="text">
          <a-badge :status="text | statusTypeFilter" :text="text | statusFilter" />
        </span>
        <span slot="updateTime" slot-scope="text">
          <span>{{text||'无'}}</span>
        </span>
        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="goDetails(record)">查看</a>
          </template>
        </span>
      </a-table>
    </a-card>
    <a-drawer
      title="云资产详情"
      :width="isMobile?300:600"
      :visible.sync="drawerForm"
      :body-style="{'padding-top': '0px', paddingBottom: '80px' }"
      @close="()=>{drawerForm=false,loading=false}"
    >
      <asset-form
        ref="assetForm"
        :key="drawerForm"
        :assetInfo="assetInfo"
        :serverList="serverList"
      />
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
        <a-button :style="{ marginRight: '20px' }" @click="()=>{drawerForm=false,loading=false}">取消</a-button>
        <a-button :loading="loading" type="primary" @click.native.prevent="submit">
          <span v-if="!loading">保 存</span>
          <span v-else>保 存 中</span>
        </a-button>
      </div>
    </a-drawer>
  </div>
</template>

<script>
import {
  assetlists,
  updateAsset,
  judgmentAsset,
  checkAsset,
  getAssetInfo,
  buildTag,
  getTagSelect,
  getCloudLists,
} from '@/api/index'
import { baseMixin } from '@/store/app-mixin'
import toolbar from '@/mixins/toolbar-mixin'
import AssetForm from './components/assetForm'

const columns = [
  {
    title: 'ID/名称',
    className: 'mw100',
    scopedSlots: { customRender: 'cvmID' },
  },
  {
    title: '状态',
    dataIndex: 'cvmStatus',
    className: 'mw80',
    scopedSlots: { customRender: 'status' },
  },
  {
    title: '主IPV4地址',
    className: 'mw120',
    scopedSlots: { customRender: 'IP' },
  },
  {
    title: '预计到期时间',
    dataIndex: 'cvmCost',
    className: 'mw100',
    scopedSlots: { customRender: 'cvmCost' },
  },
  {
    title: '内存/硬盘/cpu',
    className: 'mw120',
    dataIndex: 'cvmConfig',
  },
  {
    title: '归属',
    className: 'mw70',
    scopedSlots: { customRender: 'region' },
  },
  {
    title: '标签',
    className: 'mw70',
    dataIndex: 'serviceLabel',
    scopedSlots: { customRender: 'serviceLabel' },
  },
  // {
  //   title: '创建时间',
  //   className: 'mw100',
  //   dataIndex: 'cvmCreateTime',
  // },
  {
    title: '更新时间',
    className: 'mw100',
    dataIndex: 'cvmUpdateTime',
    scopedSlots: { customRender: 'updateTime' },
  },

  {
    title: '操作',
    dataIndex: 'action',
    align: 'center',
    className: 'mw80',
    scopedSlots: { customRender: 'action' },
  },
]
const statusMap = {
  0: {
    status: 'default',
    text: '待初始化',
  },
  1: {
    status: 'success',
    text: '运行中',
  },
  2: {
    status: 'warning',
    text: '更新中',
  },
  3: {
    status: 'error',
    text: '停用',
  },
}
export default {
  components: { AssetForm },
  mixins: [baseMixin, toolbar],
  data() {
    return {
      /** 搜索 */
      searchForm: {
        IPv4: undefined,
        cvmRegionSource: undefined,
        tagId: undefined,
        cvmTagName: undefined,
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
      //抽屉提交按钮
      loading: false,
      //表格Loading
      listLoading: false,
      //加载按钮Loading
      updateLoading: false,
      assetInfo: {
        cid: '',
        cvmClusterOutSideIP: '',
        cvmTag: [],
        cvmCvmCost: '',
        cvmRemark: '',
        cvmassetConfig: '',
        serviceLists: {},
        cvmInstanceId: '',
        cvmRegionId: '',
        cvmRegionSource: '',
        cvmClusterInSideIP: '',
        cvmUpdateTime: '',
        cvmDelete: 0,
        cvmStatus: 0,
        cvmTagName: '',
        cvmCreateTime: '',
      },
      //云厂商列表
      cloudList: [],
      // 服务列表
      serverList: [],
      drawerForm: false,
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
     * 获取更新CVM列表
     */
    getList() {
      this.listLoading = true
      const { current, pageSize } = this.pagination
      assetlists({ ...{ page: current, pageSize }, ...this.searchForm })
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

    /**
     * 判断是否有资产存在
     */
    judgmentAsset() {
      let _this = this
      this.$destroyAll()
      judgmentAsset()
        .then(({ status, msg }) => {
          if (status != 1) {
            this.$confirm({
              title: '提示',
              content: '当前暂无资产',
              okText: '去加载',
              maskClosable: true,
              onOk() {
                _this.updateAsset()
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
     * 判断是否在采集任务中
     */
    checkAsset() {
      checkAsset()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.updateLoading = false
          } else {
            this.updateLoading = true
            // this.$message.error(msg)
          }
        })
        .catch((error) => {})
    },
    /**
     * 加载CVM数据
     */
    updateAsset() {
      this.updateLoading = true
      updateAsset()
        .then(({ status, msg }) => {
          if (status === 1) {
            this.pagination.current = 1
            this.getList()
            this.checkAsset()
            this.$message.success(msg)
          } else {
            this.$message.error(msg)
          }
          this.updateLoading = false
        })
        .catch((err) => {
          this.updateLoading = false
        })
    },
    /**
     * 查看资产详情
     */
    goDetails({ cid }) {
      this.drawerForm = true
      getAssetInfo({ cid })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.assetInfo = { ...data }
          } else {
            this.$message.error(msg)
          }
        })
        .catch((erro) => {})
      getTagSelect()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.serverList = [...data]
          } else {
            this.$message.error(msg)
          }
        })
        .catch((erro) => {})
    },
    submit() {
      this.loading = true
      let validateFields = this.$refs.assetForm.form.validateFields
      const validateFieldsKey = [
        'tagId',
        'servicePort',
        'serviceUsername',
        'servicePassword',
      ]
      validateFields(validateFieldsKey, { force: true }, (err, values) => {
        if (!err) {
          buildTag({ ...values, ...{ cid: this.assetInfo.cid } })
            .then(({ status, data, msg }) => {
              if (status === 1) {
                this.$message.success(msg)
                this.pagination.current = 1
                this.getList()
                this.drawerForm = false
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
      getTagSelect()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.serverList = [...data]
          } else {
            this.$message.error(msg)
          }
        })
        .catch((erro) => {})
    },
  },
  created() {
    this.judgmentAsset()
    this.checkAsset()
    this.getCloudList()
    this.getList()
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