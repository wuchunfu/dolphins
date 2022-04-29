<template>
  <div>
    <a-divider orientation="left">基础信息</a-divider>
    <a-descriptions size="small" :column="isMobile ? 1 : 2" layout="vertical" bordered>
      <a-descriptions-item label="实例ID">{{ assetInfo.cvmInstanceId||'无' }}</a-descriptions-item>
      <a-descriptions-item label="别名">{{ assetInfo.cvmTagName ||'无'}}</a-descriptions-item>
      <a-descriptions-item label="标签">
        <a-tag
          class="ml10"
          size="small"
          v-for="item in assetInfo.serviceLabel"
          :key="item"
          effect="plain"
        >{{ item }}</a-tag>
        <span v-if="!assetInfo.serviceLabel||assetInfo.serviceLabel.length==0">无</span>
      </a-descriptions-item>
      <a-descriptions-item label="所在云/归属地/归属区域">
        {{ assetInfo.regionSource ||'无'}} / {{ assetInfo.cvmRegionId ||'无'}} /
        {{ assetInfo.cvmRegionSource||'无' }}
      </a-descriptions-item>
      <a-descriptions-item label="状态">
        <span v-if="assetInfo.cvmStatus">
          <a-badge
            :status="assetInfo.cvmStatus | statusTypeFilter"
            :text="assetInfo.cvmStatus | statusFilter"
          />
        </span>
      </a-descriptions-item>
      <a-descriptions-item label="内网IP">{{ assetInfo.cvmClusterInSideIP||'无' }}</a-descriptions-item>
      <a-descriptions-item label="外网IP">{{ assetInfo.cvmClusterOutSideIP||'无' }}</a-descriptions-item>
      <a-descriptions-item label="创建时间">{{ assetInfo.cvmCreateTime||'无' }}</a-descriptions-item>
      <a-descriptions-item label="备注">{{ assetInfo.cvmRemark||'无' }}</a-descriptions-item>
      <a-descriptions-item label="更新时间">{{ assetInfo.cvmUpdateTime||'无' }}</a-descriptions-item>
    </a-descriptions>
    <a-divider orientation="left">基本资料</a-divider>
    <a-form :form="form" :label-col="{ span: 5 }" layout="horizontal" :wrapper-col="{ span: 17 }">
      <a-form-item label="绑定标签">
        <a-select
          v-decorator="[
          'tagId',
          { rules: [{ required: true, message: '云资产标签不能为空!' }] },
        ]"
          placeholder="请选择标签"
        >
          <a-select-option v-for="d in serverList" :key="d.value">{{ d.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="用户名">
        <a-input
          v-decorator="['serviceUsername', { rules: [{ required: true, message: '用户名不能为空!' }] }]"
          placeholder="请输入用户名"
        />
      </a-form-item>
      <a-form-item label="端口">
        <a-input
          v-decorator="['servicePort', { rules: [{ required: true, message: '端口不能为空!' }] }]"
          placeholder="请输入端口"
        />
      </a-form-item>
      <a-form-item label="密码">
        <a-input-password
          v-decorator="['servicePassword', { rules: [{ required: true, message: '密码不能为空!' }] }]"
          placeholder="请输入密码"
        />
      </a-form-item>
    </a-form>
  </div>
</template>
<script>
import { baseMixin } from '@/store/app-mixin'

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
  mixins: [baseMixin],
  props: {
    assetInfo: {
      type: Object,
      default: true,
    },
    serverList: {
      type: Array,
      default: true,
    },
  },
  data() {
    return {
      form: this.$form.createForm(this),
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
  methods: {},
  created() {},
}
</script>