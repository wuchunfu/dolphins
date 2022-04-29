<template>
  <div>
    <a-form
      :form="form"
      ref="form"
      :label-col="{ span: 5 }"
      layout="horizontal"
      :wrapper-col="{ span: 17 }"
    >
      <!-- <a-alert title="配置提醒" type="warning" description="若无'归属业务组'下拉选项则需先创建发布策略组后，再进行创建工程。" show-icon :closable="false" /> -->
      <a-row :gutter="32" type="flex" justify="center">
        <a-col :xs="24" :sm="12" :lg="options.type=='enterprise'?12:24">
          <a-form-item label="工程名称" help="(格式XXX-XXX-XXXX)">
            <a-input
              v-decorator="['engineerName', { rules: [{ required: true, message: '工程名称不能为空!' }] }]"
              placeholder="请输入工程名称"
            />
          </a-form-item>
          <a-form-item label="归属云">
            <a-select
              v-decorator="['engineerCloudId', { rules: [{ required: true, message: '归属云不能为空!' }] }]"
              placeholder="请输入归属云"
              @change="getGitNamespaceSelect"
            >
              <a-select-option v-for="d in cloudList" :value="d.value" :key="d.value">{{ d.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="开发语言">
            <a-select
              v-decorator="['engineerLanguageId', { rules: [{ required: true, message: '开发语言不可为空!' }] },]"
              placeholder="请选择开发语言"
              @change="languageChange"
            >
              <a-select-option v-for="d in languageList" :key="d.value">{{ d.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="开发框架">
            <a-select
              v-decorator="['engineerFrameworkId']"
              placeholder="请选择开发框架"
              @change="frameworkChange"
            >
              <a-select-option v-for="d in frameworkList" :key="d.value">{{ d.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="DockerFile模板">
            <a-select
              v-decorator="['engineerDockerfileId', { rules: [{ required: true, message: '开发框架不可为空!' }] },]"
              placeholder="请选择DockerFile模板"
            >
              <a-select-option v-for="d in dockerFileList" :key="d.value">{{ d.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="代码分组">
            <a-select
              v-decorator="['engineerGitGroupId', { rules: [{ required: true, message: '代码分组不可为空!' }] },]"
              placeholder="请选择代码分组"
            >
              <a-select-option v-for="d in nameSpacelist" :key="d.value">{{ d.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item
            label="发布策略组"
            validate-status="warning"
            help="若无'发布策略组'下拉选项，则需先创建发布策略组后，再进行创建工程"
          >
            <a-select
              v-decorator="['engineerReleaseRulesId', { rules: [{ required: true, message: '开发框架不可为空!' }] },]"
              placeholder="请选择归属的业务组"
            >
              <a-select-option v-for="d in busGrouplist" :key="d.value">{{ d.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="工程备注" v-if="options.type!='enterprise'">
            <a-textarea
              v-decorator="['engineerRemark']"
              placeholder="请输入工程备注"
              :auto-size="{ minRows: 3, maxRows: 6 }"
            />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="24" :lg="11" v-if="options.type=='enterprise'">
          <a-form-item label="开发负责人">
            <!-- <a-input
              v-decorator="['engineerCodeing', { rules: [{ required: true, message: '开发负责人不能为空!' }] }]"
              placeholder="请输入开发负责人"
            />-->
            <a-select
              v-decorator="['engineerCodeing', { rules: [{ required: true, message: '开发负责人不可为空!' }] },]"
              placeholder="请选择开发负责人"
            >
              <a-select-option v-for="d in options.orgUsers" :key="d.value">{{ d.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="安全负责人">
            <!-- <a-input
              v-decorator="['engineerSecurity', { rules: [{ required: true, message: '安全负责人不能为空!' }] }]"
              placeholder="请输入安全负责人"
            />-->
            <a-select
              v-decorator="['engineerSecurity', { rules: [{ required: true, message: '安全负责人不能为空!' }] },]"
              placeholder="请选择安全负责人"
            >
              <a-select-option v-for="d in options.orgUsers" :key="d.value">{{ d.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="运维负责人">
            <!-- <a-input
              v-decorator="['engineerDevops', { rules: [{ required: true, message: '运维负责人不能为空!' }] }]"
              placeholder="请输入运维负责人"
            />-->
            <a-select
              v-decorator="['engineerDevops', { rules: [{ required: true, message: '运维负责人不能为空!' }] },]"
              placeholder="请选择运维负责人"
            >
              <a-select-option v-for="d in options.orgUsers" :key="d.value">{{ d.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="测试负责人">
            <!-- <a-input
              v-decorator="['engineerTesting', { rules: [{ required: true, message: '测试负责人不能为空!' }] }]"
              placeholder="请输入测试负责人"
            />-->
            <a-select
              v-decorator="['engineerTesting', { rules: [{ required: true, message: '测试负责人不能为空!' }] },]"
              placeholder="请选择测试负责人"
            >
              <a-select-option v-for="d in options.orgUsers" :key="d.value">{{ d.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="业务负责人">
            <!-- <a-input
              v-decorator="['engineerVocational', { rules: [{ required: true, message: '业务负责人不能为空!' }] }]"
              placeholder="请输入业务负责人"
            />-->
            <a-select
              v-decorator="['engineerVocational', { rules: [{ required: true, message: '业务负责人不能为空!' }] },]"
              placeholder="请选择业务负责人"
            >
              <a-select-option v-for="d in options.orgUsers" :key="d.value">{{ d.label }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="工程备注">
            <a-textarea
              v-decorator="['engineerRemark']"
              placeholder="请输入工程备注"
              :auto-size="{ minRows: 3, maxRows: 6 }"
            />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </div>
</template>
<script>
import {
  getDockerfileSelect,
  getRulesGroupSelect,
  getFrameWorkSelect,
  getLanguageSelect,
  getEngineerCloudLists,
  getGitNamespaceSelect,
} from '@/api/index'
export default {
  name: 'create',
  props: {
    options: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      form: this.$form.createForm(this),
      //云厂商集合
      cloudList: [],
      //开发语言
      languageList: [],
      //开发框架
      frameworkList: [],
      //dockerfile列表
      dockerFileList: [],
      // 策略组列表
      busGrouplist: [],
      //gitlab的NameSpace
      nameSpacelist: [],
    }
  },
  methods: {
    /**初始化 */
    init() {
      this.getCloudList()
      this.getLanguage()
      this.getStrategyGroup()
    },
    cloudChange() {
      this.$nextTick(function () {
        getCloudClusterList({ cloudTypeId: this.form.getFieldsValue().releaseJobCloudId })
          .then(({ status, data, msg }) => {
            if (status === 1) {
              this.cloudClusterList = data || []
              this.form.setFieldsValue({ releaseJobClusterId: undefined })
            } else {
              this.$message.error(msg)
            }
          })
          .catch(() => {})
      })
    },
    //获取云厂商列表
    getCloudList() {
      getEngineerCloudLists()
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
     * 获取dockerfile的下拉信息
     */
    frameworkChange() {
      this.$nextTick(function () {
        getDockerfileSelect({
          engineerLanguageId: this.form.getFieldsValue().engineerLanguageId,
          engineerFrameworkId: this.form.getFieldsValue().engineerFrameworkId,
        })
          .then(({ status, data, msg }) => {
            if (status === 1) {
              this.dockerFileList = Array.isArray(data) ? data : []
            } else {
              this.$message.error(msg)
            }
          })
          .catch((error) => {})
      })
    },
    getGitNamespaceSelect() {
      this.$nextTick(function () {
        console.log('this.form.getFieldsValue().engineerCloudId', this.form.getFieldsValue().engineerCloudId)
        getGitNamespaceSelect({
          ventorId: this.form.getFieldsValue().engineerCloudId,
        })
          .then(({ status, data, msg }) => {
            if (status === 1) {
              this.nameSpacelist = Array.isArray(data) ? data : []
              this.form.setFieldsValue({ engineerGitGroupId: undefined })
            } else {
              this.$message.error(msg)
            }
          })
          .catch((error) => {})
      })
    },
    /**
     * 发布策略组下拉
     */
    getStrategyGroup() {
      getRulesGroupSelect()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.busGrouplist = Array.isArray(data) ? data : []
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
      this.$nextTick(function () {
        getFrameWorkSelect({ LanguageId: this.form.getFieldsValue().engineerLanguageId })
          .then(({ status, data, msg }) => {
            if (status === 1) {
              this.frameworkList = Array.isArray(data) ? data : []
              this.form.setFieldsValue({ engineerFrameworkId: undefined })
            } else {
              this.$message.error(msg)
            }
          })
          .catch((error) => {})
      })
    },
  },
  created() {
    this.init()
  },
}
</script>