<template>
  <div>
    <a-alert
      title="配置提醒"
      type="warning"
      description="请确认配置信息准确，不然可能会导致无法部署成功，影响后续的资源配置。确认后，您配置的信息将开始创建并部署，并且进行扣费，无法退回。"
      show-icon
      :closable="false"
      class="mb18"
    />

    <a-divider orientation="left">基础信息</a-divider>
    <a-descriptions size="small" :column="isMobile ? 1 : 3" layout="vertical" bordered>
      <a-descriptions-item label="归属云">{{assetData.cloud||'无'}}</a-descriptions-item>
      <a-descriptions-item
        label="总价格"
        class="text-red font-bold"
      >{{(Math.ceil(+assetData.totalPrice*100)/100).toFixed(2)||'无'}}</a-descriptions-item>
      <a-descriptions-item label="并发数">{{assetData.current||'无'}}</a-descriptions-item>
      <a-descriptions-item label="创建时间">{{assetData.creatatime||'无'}}</a-descriptions-item>
    </a-descriptions>
    <!-- <a-divider style=" font-weight: bold;font-size:24px;">资源清单</a-divider>
    <a-divider orientation="left">CFS信息</a-divider>
    <a-descriptions size="small" :column="2" layout="vertical" bordered>
      <a-descriptions-item label="资源名称">{{assetData.cfs.spec||'无'}}</a-descriptions-item>
      <a-descriptions-item label="资源单价">{{`${Math.ceil(+assetData.cfs.price).toFixed(2)}元`||'无'}}</a-descriptions-item>
      <a-descriptions-item label="资源备注">{{assetData.cfs.remark||'无'}}</a-descriptions-item>
    </a-descriptions>
    <a-divider orientation="left">TCR信息</a-divider>
    <a-descriptions size="small" :column="2" layout="vertical" bordered>
      <a-descriptions-item label="资源名称">{{assetData.tcr.spec||'无'}}</a-descriptions-item>
      <a-descriptions-item label="资源单价">{{`${Math.ceil(+assetData.tcr.price).toFixed(2)}元`||'无'}}</a-descriptions-item>
      <a-descriptions-item label="资源备注">{{assetData.tcr.remark||'无'}}</a-descriptions-item>
    </a-descriptions>
    <a-divider orientation="left">network信息</a-divider>
    <a-descriptions size="small" :column="2" layout="vertical" bordered>
      <a-descriptions-item label="资源名称">{{assetData.network.spec||'无'}}</a-descriptions-item>
      <a-descriptions-item
        label="资源单价"
      >{{`${Math.ceil(+assetData.network.price).toFixed(2)}元/GiB`||'无'}}</a-descriptions-item>
      <a-descriptions-item label="资源备注">{{assetData.network.remark||'无'}}</a-descriptions-item>
    </a-descriptions>-->

    <a-divider orientation="left">资源清单</a-divider>
    <a-table
      ref="table"
      size="middle"
      :columns="columns"
      :dataSource="[...[{spec:assetData.cfs.spec,price:assetData.cfs.price,remark:assetData.cfs.remark,type:'cfs'},{spec:'TCR',cvm:assetData.tcr.spec,price:assetData.tcr.price,remark:assetData.tcr.remark,type:'tcr'},{spec:assetData.network.spec,price:assetData.network.price,remark:assetData.network.remark,type:'network'}],...assetData.cvmLists]"
      :pagination="false"
      :rowKey="(record,index)=>{return index}"
    >
      <span slot="spec" slot-scope="text,{type}">
        <span v-if="type">{{text}}</span>
        <span v-else>云服务器</span>
      </span>
      <span slot="cvm" slot-scope="text">{{text||'无'}}</span>
      <span slot="price" slot-scope="text,{type}">
        <span v-if="type=='cfs'||type=='tcr'">{{`${(Math.ceil(+text*100)/100).toFixed(2)}元`||'无'}}</span>
        <span v-else-if="type=='network'">{{`${(Math.ceil(+text*100)/100).toFixed(2)}/元`||'无'}}</span>
        <span v-else>{{(Math.ceil(+text*100)/100).toFixed(2)}}元/小时</span>
      </span>
      <span slot="assetNumber" slot-scope="{type}">
        <span v-if="type">N</span>
        <span v-else>1/台</span>
      </span>
      <span slot="remark" slot-scope="text">{{text||"无"}}</span>
    </a-table>
    <a-divider orientation="left">实施成本</a-divider>
    <a-table
      ref="table"
      size="middle"
      :columns="columns1"
      :dataSource="[...assetData.services]"
      :pagination="false"
      :rowKey="(record,index)=>{return index}"
    >
      <span slot="orderPrice" slot-scope="text">{{text}}元</span>
    </a-table>
    <a-divider orientation="left">支付方式</a-divider>
    <div class="pay-mode">
      <div class="pay-mode-item" :class="sourceId==1?'pay-mode-active':''" @click="sourceId=1">
        <a-icon type="alipay" style="color:#1890ff" />支付宝支付
      </div>
      <div class="pay-mode-item" :class="sourceId==0?'pay-mode-active':''" @click="sourceId=0">
        <a-icon type="wechat" style="color: #04BE02" />微信支付
      </div>
    </div>

    <a-row type="flex" justify="center" class="mt20">
      <a-col>
        <a-button @click="prevStep" :disabled="loading">上一步</a-button>
        <a-button
          style="margin-left: 8px"
          :loading="loading"
          type="primary"
          @click.native.prevent="nextStep"
        >
          <span v-if="!loading">采买创建集群</span>
          <span v-else>采买创建中...</span>
        </a-button>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import { baseMixin } from '@/store/app-mixin'
import { createClusterOrders } from '@/api/index'
const columns = [
  {
    title: '资源名称',
    className: 'mw90',
    dataIndex: 'spec',
    scopedSlots: { customRender: 'spec' },
  },
  {
    title: '资源规格',
    className: 'mw120',
    dataIndex: 'cvm',
    scopedSlots: { customRender: 'cvm' },
  },
  {
    title: '资源单价',
    className: 'mw90',
    dataIndex: 'price',
    scopedSlots: { customRender: 'price' },
  },
  {
    title: '资源数量',
    className: 'mw90',
    scopedSlots: { customRender: 'assetNumber' },
  },
  {
    title: '资源备注',
    dataIndex: 'remark',
    scopedSlots: { customRender: 'remark' },
  },
]
const columns1 = [
  {
    title: '类目',
    className: 'mw90',
    dataIndex: 'typeName',
    scopedSlots: { customRender: 'typeName' },
  },
  {
    title: '单价',
    className: 'mw120',
    dataIndex: 'orderPrice',
    scopedSlots: { customRender: 'orderPrice' },
  },
  {
    title: '备注',
    className: 'mw90',
    dataIndex: 'orderInfo',
    scopedSlots: { customRender: 'orderInfo' },
  },
]
const statusMap = {
  0: { status: 'processing', text: '初始化(搭建)' },
  1: { status: 'processing', text: '待决策' },
  2: { status: 'processing', text: '配置部署中' },
  3: { status: 'success', text: '运行中' },
  4: { status: 'error', text: '异常' },
  5: { status: 'default', text: '停用' },
  6: { status: 'warning', text: '回收中' },
}
export default {
  mixins: [baseMixin],
  props: {
    form: {
      type: Object,
      required: true,
    },
    assetData: {
      type: Object,
      required: true,
    },
  },
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
      columns,
      columns1,
      sourceId: 0,
      loading: false,
    }
  },
  methods: {
    nextStep() {
      this.loading = true
      // this.$emit('nextStep', '')
      // return
      createClusterOrders({ ...this.form, ...{ sourceId: this.sourceId } })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.$emit('nextStep', data)
          } else {
            this.$message.error(msg)
          }
        })
        .catch((err) => {
          this.$message.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    prevStep() {
      this.$emit('prevStep')
    },
  },
}
</script>

<style lang="less" scoped>
.stepFormText {
  margin-bottom: 24px;

  .ant-form-item-label,
  .ant-form-item-control {
    line-height: 22px;
  }
}
.pay-mode {
  display: flex;
  &-item {
    padding: 20px;
    font-weight: bold;
    border: 1px solid #ccc;
    border-radius: 8px;
    font-size: 16px;
    line-height: 20px;

    cursor: pointer;
    i {
      margin-top: 2px;
      margin-right: 10px;
      font-size: 20px;
    }
  }
  &-item:not(:last-child) {
    margin-right: 20px;
  }
  &-active {
    border: 1px solid #2684ff;
  }
}
</style>
