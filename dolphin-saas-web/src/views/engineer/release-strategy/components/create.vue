<template>
  <a-form :form="form" :label-col="{ span: 5 }" layout="horizontal" :wrapper-col="{ span: 17 }">
    <a-form-item label="策略组名">
      <a-input
        v-decorator="['rulesName', { rules: [{ required: true, message: '发布组合名称不能为空!' }] }]"
        placeholder="请输入发布组合名称"
      />
    </a-form-item>
    <a-form-item label="发布策略">
      <a-select
        v-decorator="[
          'rulesType',
          { rules: [{ required: true, message: '发布策略不能为空!' }] },
        ]"
        placeholder="请选择发布策略"
      >
        <a-select-option v-for="d in strategyRules" :key="d.value">{{ d.label }}</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="策略模式">
      <a-select mode="multiple" v-decorator="['ruleInfos']" placeholder="请选择策略模式">
        <a-select-option v-for="d in rulesList" :key="d.rulesInfoSort">{{ d.rulesInfoTitle }}</a-select-option>
      </a-select>
    </a-form-item>
  </a-form>
</template>
<script>
export default {
  props: {
    rulesList: {
      type: Array,
      default: () => {
        return []
      },
    },
  },
  data() {
    return {
      form: this.$form.createForm(this),
      //发布策略
      strategyRules: [
        { value: 0, label: '谨慎策略' },
        { value: 1, label: '放松策略' },
      ],
    }
  },
  methods: {},
  created() {},
}
</script>