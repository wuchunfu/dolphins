<template>
  <page-header-wrapper title="用户管理" :breadcrumb="false">
    <a-card :bordered="false">
      <div class="table-page-search-wrapper"></div>
      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="create" ghost>创建用户</a-button>
      </div>
      <a-table
        ref="table"
        size="middle"
        :columns="columns"
        :dataSource="list"
        :loading="listLoading"
        :pagination="pagination"
        @change="tableChange"
        :rowKey="
          (record, index) => {
            return index
          }
        "
      >
        <span slot="status" slot-scope="text">
          <a-badge :status="text | statusTypeFilter" :text="text | statusFilter" />
        </span>
        <span slot="role" slot-scope="text">
          <span v-if="text==0">普通用户</span>
          <span v-if="text==1">管理员</span>
        </span>
        <span slot="updateTime" slot-scope="text">
          <span>{{ text || '无' }}</span>
        </span>

        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="edit(record)">编辑</a>
            <a-divider type="vertical" />
            <a @click="remove(record.uuid)">删除</a>
          </template>
        </span>
      </a-table>
    </a-card>
    <a-drawer
      :title="isCreate ? '创建配置' : '更新配置'"
      :width="600"
      :visible.sync="drawerForm"
      :body-style="{ paddingBottom: '80px' }"
      @close="
        () => {
          ;(drawerForm = false), (loading = false)
        }
      "
    >
      <user-form ref="userForm" :isCreate="isCreate" :key="drawerForm" />
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
          zIndex: 1
        }"
      >
        <a-button
          :style="{ marginRight: '20px' }"
          @click="
            () => {
              ;(drawerForm = false), (loading = false)
            }
          "
        >取消</a-button>
        <a-button :loading="loading" type="primary" @click.native.prevent="submit">
          <span v-if="!loading">保 存</span>
          <span v-else>保 存 中</span>
        </a-button>
      </div>
    </a-drawer>
  </page-header-wrapper>
</template>
<script>
import { getUserList, createUser, updateUser, deleteUser } from '@/api/index'
import UserForm from './components/userForm'
const columns = [
  {
    title: '用户名',
    dataIndex: 'userName',
    // scopedSlots: { customRender: 'TypeName' }
  },
  {
    title: '昵称',
    dataIndex: 'commonName',
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    scopedSlots: { customRender: 'phone' },
  },
  {
    title: '角色',
    className: 'mw100',
    dataIndex: 'admin',
    scopedSlots: { customRender: 'role' },
  },
  {
    title: '创建时间',
    className: 'mw100',
    dataIndex: 'createTime',
  },
  {
    title: '状态',
    className: 'mw90',
    dataIndex: 'status',
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
  0: { status: 'default', text: '未启用' },
  1: { status: 'success', text: '启用' },
}
export default {
  components: { UserForm },
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
      uuid: '',
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
  computed: {},
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
      getUserList({ ...{ page: current, pageSize }, ...this.searchForm })
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
     * 创建用户
     */
    create() {
      this.isCreate = true
      this.drawerForm = true
    },
    /**
     * 编辑用户详情
     */
    edit({ admin, commonName, status, phone, userName, uuid }) {
      this.isCreate = false
      this.drawerForm = true
      this.uuid = uuid
      this.$nextTick(function () {
        this.$refs.userForm.form.setFieldsValue({
          adminStatus: admin,
          commonName,
          loginStatus: status,
          password: '',
          phone: phone || '',
          userName,
        })
      })
    },
    /**
     * 创建&更新用户信息
     */
    submit() {
      this.loading = true
      let validateFields = this.$refs.userForm.form.validateFields
      console.log('validateFields', validateFields)
      const validateFieldsKey = ['adminStatus', 'commonName', 'loginStatus', 'password', 'phone', 'userName']
      validateFields(validateFieldsKey, { force: true }, (err, values) => {
        if (!err) {
          let setUser = this.isCreate ? createUser({ ...values }) : updateUser({ ...values }, { uuid: this.uuid })
          setUser
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
    /**删除用户 */
    remove(uuid) {
      let _this = this
      this.$confirm({
        title: '提示',
        content: '是否删除此用户，删除后不可恢复',
        okText: '确定',
        maskClosable: true,
        onOk() {
          deleteUser({ uuid }).then(({ status, data, msg }) => {
            if (status === 1) {
              _this.$message.success(msg)
              _this.pagination.current = 1
              _this.getList()
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
  },
  created() {
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
