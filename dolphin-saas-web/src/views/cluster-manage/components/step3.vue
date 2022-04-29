<template>
  <div>
    <!-- <a-form>
      <a-result
        status="success"
        :is-success="true"
        title="集群创建成功"
        sub-title="预计三十分钟内，部署完毕"
        style="max-width: 560px; margin: 40px auto 0;"
      >
        <template #extra>
          <a-button type="primary" @click="finish">确认</a-button>
        </template>
      </a-result>
    </a-form>-->
    <div id="qrcode" ref="qrcode"></div>
    <a-row type="flex" justify="center" class="mt20">
      <a-col>
        <a-button @click="prevStep" :disabled="loading">取消</a-button>
        <a-button
          style="margin-left: 8px"
          :loading="loading"
          type="primary"
          @click.native.prevent="nextStep"
        >
          <span>支付成功</span>
        </a-button>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import QRCode from 'qrcodejs2'
import { payCallback } from '@/api/index'
export default {
  name: 'Step3',
  props: {
    payInfo: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      loading: false,
      expireTime: 0,
    }
  },
  mounted() {
    this.qrcodeScan() // 注：需在mounted里触发qrcodeScan函数
    this.payCallback()
  },
  methods: {
    nextStep() {
      this.loading = true
      payCallback({ orderId: this.payInfo.orderId })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            clearInterval(this.timer)
            this.$message.success('支付成功')
            this.$emit('nextStep', data)
          } else {
            this.$message.warning('订单正在处理中')
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
    payCallback() {
      this.expireTime = new Date().getTime() + 900000
      this.timer = setInterval(() => {
        console.log('this.expireTime', this.expireTime)
        if (new Date().getTime() > this.expireTime) {
          clearInterval(this.timer)
          this.$message.error('支付有效时间为15分钟，当前支付超时，请重新下单')
          this.$emit('finish')
        }
        payCallback({ orderId: this.payInfo.orderId })
          .then(({ status, data, msg }) => {
            if (status === 1) {
              clearInterval(this.timer)
              this.$message.success('支付成功')
              this.$emit('nextStep', data)
            }
          })
          .catch((err) => {
            this.$message.error(err)
          })
      }, 5000)
    },
    qrcodeScan() {
      //生成二维码
      const qrcode = new QRCode('qrcode', {
        width: 200, // 二维码宽度
        height: 200, // 二维码高度
        text: this.payInfo.codeUrl,
      })
    },
  },
}
</script>
<style lang="less" scoped>
#qrcode {
  margin: 40px auto;
  display: flex;
  justify-content: center;
}
</style>
