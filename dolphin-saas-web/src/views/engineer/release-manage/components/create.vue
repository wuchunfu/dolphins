<template>
  <div>
    <a-alert
      title="配置提醒"
      type="warning"
      description="这里的配置一定要准确，不然可能会导致无法部署成功，影响后续的资源配置。"
      show-icon
      :closable="false"
    />
    <a-form
      :form="form"
      ref="form"
      :label-col="{ span: 5 }"
      layout="horizontal"
      :wrapper-col="{ span: 17 }"
    >
      <a-form-item label="工程名称" help="(格式XXX-XXX-XXXX)">
        <a-select
          v-decorator="['engineerId', { rules: [{ required: true, message: '工程名称不能为空!' }] },]"
          placeholder="请选择工程名称"
          @change="branchChange"
        >
          <a-select-option v-for="d in jobList" :key="d.value">{{ d.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="分支名">
        <a-select
          v-decorator="['branchName', { rules: [{ required: true, message: '分支名不能为空!' }] },]"
          placeholder="请选择分支名"
        >
          <a-select-option
            v-for="d in jobBranchList"
            :key="d.value"
            @click="branchSelect(d)"
          >{{ d.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item v-show="false">
        <a-input v-decorator="['commitId']" />
      </a-form-item>
      <a-form-item v-show="false">
        <a-input v-decorator="['authorName']" />
      </a-form-item>
      <a-form-item v-show="false">
        <a-input v-decorator="['commitTime']" />
      </a-form-item>
      <a-form-item label="资源归属">
        <a-select
          v-decorator="['jobCloudId', { rules: [{ required: true, message: '资源归属不可为空!' }] },]"
          placeholder="请选择资源归属"
          @change="cloudChange"
        >
          <a-select-option v-for="d in cloudList" :key="d.value">{{ d.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="所属集群">
        <a-select
          v-decorator="['clusterId', { rules: [{ required: true, message: '所属集群不可为空!' }] },]"
          placeholder="请选择所属集群"
          @change="clusterChange"
        >
          <a-select-option v-for="d in cloudClusterList" :key="d.value">{{ d.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="命名空间">
        <a-select
          v-decorator="['nameSpace', { rules: [{ required: true, message: '命名空间不可为空!' }] },]"
          placeholder="请选择命名空间"
        >
          <a-select-option v-for="d in namespaceList" :key="d.value">{{ d.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="模块名称">
        <a-input v-decorator="['javaMoule']" placeholder="请输入模块名称" />
      </a-form-item>
    </a-form>
    <div class="mt100" v-if="branchInfo.commitId">
      <a-divider />
      <a-card class="amount-item">
        <div class="amount-item-title">选择分支详情</div>
        <div>
          <span class="amount-item-label">commitID：</span>
          <span class="amount-item-label1">{{branchInfo.commitId}}</span>
        </div>
        <div>
          <span class="amount-item-label">提交人：</span>
          <span class="amount-item-label1">{{branchInfo.authorName}}</span>
        </div>
        <div>
          <span class="amount-item-label">创建时间：</span>
          <span class="amount-item-label1">{{branchInfo.createTime}}</span>
        </div>
        <div>
          <span class="amount-item-label">message：</span>
          <span class="amount-item-label1">{{branchInfo.message}}</span>
        </div>
      </a-card>
    </div>
  </div>
</template>
<script>
import {
  getCloudClusterList,
  getClusterNameSpaceList,
  getEngineerSelect,
  getEngineerBranch,
} from '@/api/index'
export default {
  props: {
    cloudList: {
      type: Array,
      default: true,
    },
  },
  name: 'create',
  data() {
    return {
      form: this.$form.createForm(this),
      jobList: [],
      jobBranchList: [],
      // 所属集群
      cloudClusterList: [],
      // 云厂商集群列表
      cloudClusterList: [],
      // namespace列表
      namespaceList: [],
      branchInfo: {
        commitId: undefined,
        authorName: undefined,
        createTime: undefined,
      },
    }
  },
  methods: {
    getEngineerSelect() {
      getEngineerSelect()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.jobList = data || []
          } else {
            this.$message.error(msg)
          }
        })
        .catch(() => {})
    },
    branchChange() {
      this.$nextTick(function () {
        getEngineerBranch({
          engineerId: this.form.getFieldsValue().engineerId,
        })
          .then(({ status, data, msg }) => {
            if (status === 1) {
              this.jobBranchList = data || []
              this.form.setFieldsValue({ branchName: undefined })
              this.form.setFieldsValue({ commitId: undefined })
              this.form.setFieldsValue({ authorName: undefined })
              this.form.setFieldsValue({ commitTime: undefined })
              this.branchInfo = {}
              if (data.length < 1) {
                this.$message.error('代码工程需要先推送代码才能发布!')
              }
            } else {
              this.$message.error(msg)
            }
          })
          .catch(() => {})
      })
    },
    branchSelect(info) {
      console.log('info', info)
      this.form.setFieldsValue({ commitId: info.commitId })
      this.form.setFieldsValue({ authorName: info.authorName })
      this.form.setFieldsValue({ commitTime: info.createTime })
      this.branchInfo = { ...info }
      console.log('info', info)
    },
    cloudChange() {
      this.$nextTick(function () {
        getCloudClusterList({
          cloudTypeId: this.form.getFieldsValue().jobCloudId,
        })
          .then(({ status, data, msg }) => {
            if (status === 1) {
              this.cloudClusterList = data || []
              this.form.setFieldsValue({ clusterId: undefined })
              this.form.setFieldsValue({ nameSpace: undefined })
            } else {
              this.$message.error(msg)
            }
          })
          .catch(() => {})
      })
    },
    clusterChange() {
      this.$nextTick(function () {
        getClusterNameSpaceList({
          clusterId: this.form.getFieldsValue().clusterId,
        })
          .then(({ status, data, msg }) => {
            if (status === 1) {
              this.namespaceList = data
              this.form.setFieldsValue({ nameSpace: undefined })
            } else {
              this.$message.error(msg)
            }
          })
          .catch(() => {})
      })
    },
  },
  created() {
    this.getEngineerSelect()
  },
}
</script>
<style lang="less" scoped>
.mt100 {
  margin-top: 100px;
}
.amount-item {
  background: rgba(57, 106, 255, 0.1);
  border: 1px solid #eaebee;
  border-radius: 8px;
  text-align: left;
  border: 1px solid #2684ff;
  color: #2684ff;
  width: 100%;
  transition: all 0.2s;
  ::v-deep .ant-card-body {
    padding: 10px;
    margin-bottom: 10px;
  }
  &-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 10px;
  }
  &-type {
    font-weight: bold;
    margin-bottom: 6px;
  }
  &-label {
    color: #2684ff;
  }
  &-label1 {
    font-weight: 600;
  }
}
</style>