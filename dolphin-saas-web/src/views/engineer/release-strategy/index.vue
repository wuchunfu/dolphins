<template>
  <div>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper"></div>
      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="create" ghost>添加策略组</a-button>
      </div>
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
        <span slot="action" slot-scope="text, record">
          <template>
            <a-switch
              checked-children="启用"
              un-checked-children="禁用"
              default-checked
              :checked="record.RulesStatus==1"
              @change="editRulesGroup({status: record.RulesStatus == 1 ? 0 : 1,rid: record.rid})"
              :loading="record.rid==rid"
            />
            <a-divider type="vertical" />
            <a @click="delRulesGroup(record.rid)">回收</a>
          </template>
        </span>
      </a-table>
    </a-card>
    <a-drawer
      title="添加策略组"
      :width="600"
      :visible.sync="drawerForm"
      :body-style="{'padding-top': '18px', paddingBottom: '80px' }"
      @close="()=>{drawerForm=false,loading=false}"
    >
      <create-form ref="createForm" v-if="drawerForm" :rulesList="rulesList" />
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
          <span v-if="!loading">添 加</span>
          <span v-else>添 加 中</span>
        </a-button>
      </div>
    </a-drawer>
  </div>
</template>
<script>
import {
  rulesGroupList,
  judgmentRulesGroup,
  editRulesGroup,
  createRulesGroup,
  deleteRulesGroup,
  rulesList,
} from '@/api/index'
import CreateForm from './components/create'
const columns = [
  {
    title: '发布组ID',
    dataIndex: 'rid',
  },
  {
    title: '发布组名称',
    className: 'mw100',
    dataIndex: 'RulesName',
  },
  {
    title: '发布策略',
    className: 'mw100',
    dataIndex: 'RulesType',
  },
  {
    title: '操作',
    dataIndex: 'action',
    align: 'center',
    width: '150px',
    scopedSlots: { customRender: 'action' },
  },
]
export default {
  components: { CreateForm },
  data() {
    return {
      /** 搜索 */
      searchForm: { id: '', name: '' },
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
      //发布策略可选集合
      rulesList: [],
      //发布策略必选数组
      defaultRules: [],
      //发布策略默认集合
      defaultRulesList: [],
      columns,
      //表格Loading
      listLoading: false,
      loading: false,
      drawerForm: false,
      rid: '',
    }
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
      rulesGroupList({ ...{ page: current, pageSize }, ...this.searchForm })
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
    //获取发布策略集合
    getRulesList() {
      rulesList()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.rulesList =
              data.filter((item) => item.rulesInfoMaster == 0) || []
            this.defaultRules =
              data
                .filter((item) => item.rulesInfoMaster == 1)
                .map((item) => item.rulesInfoSort) || []
            this.defaultRulesList = data || []
          } else {
            this.$message.error(msg)
          }
        })
        .catch((err) => {})
    },
    /**
     * 判断是否有发布策略存在
     */
    judgmentRulesGroup() {
      let _this = this
      this.$destroyAll()
      judgmentRulesGroup()
        .then(({ status, data, msg }) => {
          if (status != 1) {
            this.$confirm({
              title: '提示',
              content: '当前暂无发布策略',
              okText: '去添加',
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
    //发布规则修改
    editRulesGroup(params) {
      let _this = this
      this.$confirm({
        title: '提示',
        content: '是否切换发布策略状态',
        okText: '确认',
        maskClosable: true,
        onOk() {
          editRulesGroup({ ...params }).then(({ status, data, msg }) => {
            if (status === 1) {
              _this.$message.success(msg)
            } else {
              _this.$message.error(msg)
            }
            _this.pagination.current = 1
            _this.getList()
          })
          return new Promise((resolve, reject) => {
            resolve()
          }).catch(() => console.log('Oops errors!'))
        },
        onCancel() {},
      })
    },
    /**添加发布策略组 */
    create() {
      this.getRulesList()
      this.drawerForm = true
    },
    /**
     * 创建策略组信息
     */
    submit() {
      this.loading = true
      let validateFields = this.$refs.createForm.form.validateFields
      const validateFieldsKey = ['rulesName', 'rulesType', 'ruleInfos']
      validateFields(validateFieldsKey, { force: true }, (err, values) => {
        if (!err) {
          //rulesInfoSort集合
          let ruleInfosList = [...this.defaultRules, ...values.ruleInfos]
          //排完序的rulesInfoSort集合
          ruleInfosList.sort((a, b) => a - b)
          //筛选后的rulesInfoId集合
          values.ruleInfos = ruleInfosList
            .map((item) => {
              return this.defaultRulesList.find((i) => i.rulesInfoSort == item)
                .rulesInfoId
            })
            .join(',')

          console.log('  values.ruleInfos', values.ruleInfos)
          createRulesGroup({ ...values })
            .then(({ status, data, msg }) => {
              if (status === 1) {
                this.$message.success(msg)
                this.drawerForm = false
              } else {
                this.$message.error(msg)
              }
              this.pagination.current = 1
              this.getList()
              this.loading = false
            })
            .catch((err) => {
              this.loading = false
            })
        } else {
          this.loading = false
          return false
        }
      })
    },
    delRulesGroup(rid) {
      let _this = this
      this.$confirm({
        title: '提示',
        content: `此操作将执行发布策略删除, 是否继续?`,
        okText: '确定',
        maskClosable: true,
        onOk() {
          deleteRulesGroup({ rid }).then(({ status, data, msg }) => {
            if (status === 1) {
              _this.$message.success(msg)
            } else {
              _this.$message.error(msg)
            }
            _this.pagination.current = 1
            _this.getList()
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
    this.judgmentRulesGroup()
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