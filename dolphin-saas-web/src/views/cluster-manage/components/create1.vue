<template>
  <div>
    <a-alert
      title="配置提醒"
      type="warning"
      description="这里的配置一定要准确，不然可能会导致无法部署成功，影响后续的资源配置。"
      show-icon
      :closable="false"
      class="mb18"
    />
    <a-form
      :form="form"
      ref="form"
      layout="horizontal"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 17 }"
    >
      <a-form-item label="部署厂商">
        <a-select
          v-decorator="['cloudId', { rules: [{ required: true, message: '资源归属不可为空!' }] },]"
          placeholder="请选择资源归属"
          @change="cloudChange"
        >
          <a-select-option v-for="d in cloudList" :key="d.value">{{ d.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="部署区域">
        <a-select
          v-decorator="['regionId', { rules: [{ required: true, message: '部署区域不可为空!' }] },]"
          placeholder="请选择资源区域"
          @change="regionChange"
        >
          <a-select-option v-for="d in regionList" :key="d.value">{{ d.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="可用区域">
        <a-select
          v-decorator="['zoneId', { rules: [{ required: true, message: '可用区域不可为空!' }] },]"
          placeholder="请选择可用区域"
        >
          <a-select-option v-for="d in zoneList" :key="d.value">{{ d.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="业务并发数">
        <a-row :gutter="20" class="amount-list">
          <a-col :span="8" class="mb10">
            <a-card
              class="amount-item"
              :class="amountType==1?'active-item':''"
              @click="selectAmount(1,100)"
            >
              <div class="amount-item-title">电商型</div>
              <div class="amount-item-type">小型业务</div>
              <div>
                <span class="amount-item-label">同时在线：</span>
                <span class="amount-item-label1">100人</span>
              </div>
              <div>
                <span class="amount-item-label">并发数：</span>
                <span class="amount-item-label1">100</span>
              </div>
            </a-card>
          </a-col>
          <a-col :span="8" class="mb10">
            <a-card
              class="amount-item"
              :class="amountType==2?'active-item':''"
              @click="selectAmount(2,500)"
            >
              <div class="amount-item-title">医疗型</div>
              <div class="amount-item-type">中型业务</div>
              <div>
                <span class="amount-item-label">同时在线：</span>
                <span class="amount-item-label1">500人</span>
              </div>
              <div>
                <span class="amount-item-label">并发数：</span>
                <span class="amount-item-label1">500</span>
              </div>
            </a-card>
          </a-col>
          <a-col :span="8" class="mb10">
            <a-card
              class="amount-item"
              :class="amountType==3?'active-item':''"
              @click="selectAmount(3,300)"
            >
              <div class="amount-item-title">科技型</div>
              <div class="amount-item-type">中型业务</div>
              <div>
                <span class="amount-item-label">同时在线：</span>
                <span class="amount-item-label1">300人</span>
              </div>
              <div>
                <span class="amount-item-label">并发数：</span>
                <span class="amount-item-label1">300</span>
              </div>
            </a-card>
          </a-col>
          <a-col :span="24" class="amount-hint">
            <a-icon type="exclamation-circle" theme="twoTone" />上面套餐内并发数只做推荐参考使用，具体并发数以实际为准，可手动调整下面进度条选择您实际并发数！
          </a-col>
        </a-row>
        <a-slider
          v-decorator="['current',{ rules: [{ required: true, message: '业务规模不可为空!' }],initialValue:1 }]"
          :marks="{ 1: '1', 50: '50', 100: '100', 200: '200',300: '300', 500: '500及以上' }"
          :step="null"
          :max="500"
          :min="1"
        />
      </a-form-item>
      <a-form-item label="集群类型">
        <a-radio-group
          v-decorator="['clusterType',{ rules: [{ required: true, message: '集群类型不能为空!' }] },]"
        >
          <a-radio value="0">按需使用</a-radio>
          <a-radio value="1">月付使用</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="费用预计">189CNY/小时</a-form-item>
    </a-form>
  </div>
</template>
<script>
import { getCloudRegions, getCloudRegionZones } from '@/api/index'
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
      //部署区域
      regionList: [],
      zoneList: [],
      amountType: 0,
    }
  },
  methods: {
    cloudChange() {
      this.$nextTick(function () {
        getCloudRegions({ cloudId: this.form.getFieldsValue().cloudId })
          .then(({ status, data, msg }) => {
            if (status === 1) {
              this.regionList = data || []
              this.zoneList = []
              this.form.setFieldsValue({ regionId: undefined, zoneId: undefined })
            } else {
              this.$message.error(msg)
            }
          })
          .catch(() => {})
      })
    },
    regionChange() {
      this.$nextTick(function () {
        getCloudRegionZones({
          cloudId: this.form.getFieldsValue().cloudId,
          regionId: this.form.getFieldsValue().regionId,
        })
          .then(({ status, data, msg }) => {
            if (status === 1) {
              this.zoneList = data || []
              this.form.setFieldsValue({ zoneId: undefined })
            } else {
              this.$message.error(msg)
            }
          })
          .catch(() => {})
      })
    },
    selectAmount(type, number) {
      this.$nextTick(function () {
        this.amountType = type
        this.form.setFieldsValue({ current: number })
      })
    },
  },
  created() {},
}
</script>
<style lang="less" scoped>
.amount-item {
  background: #fff;
  border: 1px solid #eaebee;
  border-radius: 8px;
  text-align: left;
  cursor: pointer;
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
    color: #999;
  }
  &-label1 {
    font-weight: 600;
  }
}
.active-item {
  border: 1px solid #2684ff;
  color: #2684ff;
  background: rgba(57, 106, 255, 0.1);
  .amount-item-label {
    color: #2684ff;
  }
}
.amount-hint {
  padding-bottom: 10px;
  line-height: 1.2;
}
</style>