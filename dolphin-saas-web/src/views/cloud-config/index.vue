<template>
  <div>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper"></div>
      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="create" ghost>绑定云厂商</a-button>
      </div>
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
        <span slot="AccessKey" slot-scope="text,record">
          <ellipsis v-if="record.vid==view" :length="10" tooltip>{{ text }}</ellipsis>
          <span v-else>**********</span>
        </span>
        <span slot="AccessSecret" slot-scope="text,record">
          <ellipsis v-if="record.vid==view" :length="10" tooltip>{{ text }}</ellipsis>
          <span v-else>**********</span>
        </span>
        <span slot="vdefaults" slot-scope="text,record">
          <a-tag color="#108ee9" v-if="+text==1">默认密钥</a-tag>
          <a-tag v-else @click="setupDefault(record.vid,record.Status)">设为默认</a-tag>
          <!-- <a-badge :status="text | statusTypeFilter" :text="text | statusFilter" /> -->
        </span>
        <span slot="defaults">
          默认状态
          <a-popover placement="top">
            <template slot="content">
              <p>
                第一个有效秘钥会设定为
                <span class="text-blue">默认秘钥</span>，用于「创建集群」
              </p>
            </template>
            <template slot="title">
              <span>
                <span class="text-blue">默认秘钥</span>说明
              </span>
            </template>
            <a-icon type="info-circle" theme="twoTone" />
          </a-popover>
        </span>
        <span slot="status" slot-scope="text">
          <a-badge :status="text | statusTypeFilter" :text="text | statusFilter" />
        </span>
        <span slot="updateTime" slot-scope="text">
          <span>{{text||'无'}}</span>
        </span>

        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="goView(record.vid)">查看</a>
            <a-divider type="vertical" />
            <a @click="goDetails(record)">配置</a>
            <a-divider type="vertical" />
            <a @click="delCloud(record.vid)">删除</a>
          </template>
        </span>
      </a-table>
    </a-card>
    <a-drawer
      :title="isCreate?'创建配置':'更新配置'"
      :width="600"
      :visible.sync="drawerForm"
      :body-style="{ paddingBottom: '80px' }"
      @close="()=>{drawerForm=false,loading=false}"
    >
      <config-form ref="configForm" :isCreate="isCreate" :cloudList="cloudList" :key="drawerForm" />
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
// import Pagination from '@/components/Pagination'
import { Ellipsis } from '@/components'
import {
  getCloudlist,
  createCloud,
  updateCloud,
  judgmentCloud,
  getCloudLists,
  delCloud,
} from '@/api/index'
import ConfigForm from './components/configForm'
const columns = [
  {
    title: '云厂商名称',
    className: 'mw100',
    dataIndex: 'TypeName',
    // scopedSlots: { customRender: 'TypeName' }
  },
  {
    title: 'accessKey',
    dataIndex: 'AccessKey',
    scopedSlots: { customRender: 'AccessKey' },
  },
  {
    title: 'accessSecret',
    dataIndex: 'AccessSecret',
    scopedSlots: { customRender: 'AccessSecret' },
  },
  {
    title: '创建时间',
    className: 'mw100',
    dataIndex: 'CreateTime',
  },
  {
    title: '更新时间',
    className: 'mw100',
    dataIndex: 'UpdateTime',
    scopedSlots: { customRender: 'updateTime' },
  },
  // {
  //   title: '默认状态',
  //   className: 'mw90',
  //   dataIndex: 'Vdefaults',
  //   scopedSlots: { customRender: 'vdefaults' },
  // },
  {
    key: 'Vdefaults',
    slots: { title: 'defaults' },
    dataIndex: 'Vdefaults',
    scopedSlots: { customRender: 'vdefaults' },
  },
  {
    title: '状态',
    className: 'mw90',
    dataIndex: 'Status',
    scopedSlots: { customRender: 'status' },
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
  0: { status: 'default', text: '待检测' },
  1: { status: 'success', text: '启用' },
  2: { status: 'error', text: '无效' },
}
export default {
  components: { Ellipsis, ConfigForm },
  data() {
    return {
      /** 搜索 */
      searchForm: { instanceid: '', aliasname: '' },
      /**分页 */
      pagination: {
        current: 1, //当前页
        pageSize: 10, //每页条数
        total: 0, //总条数
        showQuickJumper: true,
        hideOnSinglePage: true,
        showTotal: (total) => `共 ${total} 条数据`,
      },
      list: [],
      columns,
      listLoading: false,
      loading: false,
      isCreate: true,
      drawerForm: false,
      cloudList: [],
      view: '',
      editVid: '',
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
  computed: {
    // rowSelection() {
    //   return {
    //     selectedRowKeys: this.selectedRowKeys,
    //     onChange: this.onSelectChange
    //   }
    // }
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
      getCloudlist({ ...{ page: current, pageSize }, ...this.searchForm })
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
    judgmentCloud() {
      let _this = this
      this.$destroyAll()
      judgmentCloud()
        .then(({ status, msg }) => {
          if (status != 1) {
            this.$confirm({
              title: '提示',
              content: '当前暂未绑定云厂商',
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
     * 创建云资产
     */
    create() {
      this.isCreate = true
      this.drawerForm = true
      this.getCloudList()
    },
    setupDefault(vid, status) {
      let _this = this
      this.$confirm({
        title: '提示',
        content: '是否设置此密钥为该厂商的默认密钥？',
        okText: '设置',
        onCancel: '取消',
        maskClosable: true,
        onOk() {
          updateCloud({ vid, status, Vdefaults: 1 })
            .then(({ status, data, msg }) => {
              if (status === 1) {
                _this.$message.success(msg)
                _this.pagination.current = 1
                _this.getList()
              } else {
                _this.$message.error(msg)
              }
            })
            .catch(() => {})
          return new Promise((resolve, reject) => {
            resolve()
          }).catch(() => console.log('Oops errors!'))
        },
        onCancel() {},
      })
    },
    /**
     * 查看Base详情
     */
    goDetails({ vid, AccessKey, AccessSecret, TypeName, Status }) {
      this.isCreate = false
      this.drawerForm = true
      this.editVid = vid
      this.getCloudList()
      this.$nextTick(function () {
        this.$refs.configForm.form.setFieldsValue({
          accessKey: AccessKey,
          accessSecret: AccessSecret,
          status: Status,
          typeName: TypeName,
        })
      })
    },
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
    goView(id) {
      console.log(id)
      if (this.view == id) {
        this.view = ''
      } else {
        this.view = id
      }
    },
    /**
     * 创建AK信息
     */
    submit() {
      this.loading = true
      let validateFields = this.$refs.configForm.form.validateFields
      const validateFieldsKey = this.isCreate
        ? ['accessKey', 'accessSecret', 'typeName']
        : ['accessKey', 'accessSecret']
      validateFields(validateFieldsKey, { force: true }, (err, values) => {
        if (!err) {
          let setConfig = this.isCreate
            ? createCloud({ ...values })
            : updateCloud({ ...values, ...{ vid: this.editVid } })
          console.log('values', values)
          setConfig
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
    /**
     * 删除云厂商信息
     */
    delCloud(vid) {
      const _this = this
      this.$confirm({
        title: '提示',
        content: '此删除操作不可逆，是否删除此云厂商？',
        okText: '删除',
        maskClosable: true,
        onOk() {
          delCloud({ vid })
            .then(({ status, data, msg }) => {
              if (status === 1) {
                _this.getList()
                _this.$message.success(msg)
              } else {
                _this.$message.error(msg)
              }
            })
            .catch(() => {})
          return new Promise((resolve, reject) => {
            resolve()
          }).catch(() => console.log('Oops errors!'))
        },
        onCancel() {},
      })
    },
  },
  created() {
    this.getList()
    this.judgmentCloud()
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