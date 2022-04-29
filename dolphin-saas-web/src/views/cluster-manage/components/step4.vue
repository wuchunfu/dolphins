<template>
  <div>
    <a-form>
      <a-result
        status="success"
        :is-success="true"
        title="集群创建成功"
        sub-title="预计三十分钟内，部署完毕"
        style="max-width: 560px; margin: 40px auto 0;"
      >
        <!-- <div class="information">
          <a-row>
            <a-col :sm="8" :xs="24">操作账户：</a-col>
            <a-col :sm="16" :xs="24">ant-design@alipay.com</a-col>
          </a-row>
          <a-row>
            <a-col :sm="8" :xs="24">操作人：</a-col>
            <a-col :sm="16" :xs="24">辉夜</a-col>
          </a-row>
          <a-row>
            <a-col :sm="8" :xs="24">花费金额：</a-col>
            <a-col :sm="16" :xs="24">
              <span class="money">500</span> 元
            </a-col>
          </a-row>
        </div>-->
        <template #extra>
          <a-button type="primary" @click="finish">确认</a-button>
        </template>
      </a-result>
    </a-form>
  </div>
</template>

<script>
export default {
  name: 'Step3',
  data() {
    return {
      loading: false,
    }
  },
  methods: {
    finish() {
      this.$emit('finish')
    },
  },
}
</script>
<style lang="less" scoped>
.information {
  line-height: 22px;

  .ant-row:not(:last-child) {
    margin-bottom: 24px;
  }
}
.money {
  font-family: 'Helvetica Neue', sans-serif;
  font-weight: 500;
  font-size: 20px;
  line-height: 14px;
}
</style>
