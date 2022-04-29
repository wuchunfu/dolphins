<template>
  <div>
    <a-steps class="steps" :current="currentTab">
      <a-step title="集群信息" />
      <a-step title="规划方案" />
      <a-step title="确认支付" />
      <a-step title="完成" />
    </a-steps>
    <div class="content">
      <!-- step1 v-show 缓存填写的集群信息 -->
      <step1 v-show="currentTab === 0" ref="createForm" v-bind="$attrs" @nextStep="nextStep" />
      <step2
        v-if="currentTab === 1"
        :form="form"
        :assetData="assetData"
        @nextStep="nextStep"
        @prevStep="prevStep"
      />
      <step3 v-if="currentTab === 2" :payInfo="payInfo" @nextStep="nextStep" @prevStep="prevStep" />
      <step4 v-if="currentTab === 3" @prevStep="prevStep" @finish="finish" />
    </div>
  </div>
</template>

<script>
import Step1 from './step1'
import Step2 from './step2'
import Step3 from './step3'
import Step4 from './step4'
export default {
  name: 'StepForm',
  components: {
    Step1,
    Step2,
    Step3,
    Step4,
  },
  data() {
    return {
      currentTab: 0,
      // form
      form: {},
      assetData: {},
      payInfo: {},
    }
  },
  methods: {
    // handler
    nextStep(params) {
      switch (this.currentTab) {
        case 0:
          console.log('params', params)
          this.form = params.values
          this.assetData = params.data
          this.currentTab = 1
          break
        case 1:
          console.log('params', params)
          this.payInfo = params
          console.log('params', params)
          // this.assetData = params.data
          this.currentTab = 2
          break
        default:
          if (this.currentTab < 3) {
            this.currentTab += 1
          }
      }
    },
    prevStep() {
      if (this.currentTab > 0) {
        this.currentTab -= 1
      }
    },
    finish() {
      this.$emit('close')
    },
  },
}
</script>

<style lang="less" scoped>
.steps {
  max-width: 100%;
  padding: 16px;
  margin-bottom: 8px;
  box-sizing: border-box;
}
</style>
