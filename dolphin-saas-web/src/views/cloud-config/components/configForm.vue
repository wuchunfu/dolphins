<template>
  <a-form :form="form" :label-col="{ span: 5 }" layout="horizontal" :wrapper-col="{ span: 17 }">
    <a-form-item label="AccessKey">
      <a-input
        v-decorator="['accessKey', { rules: [{ required: true, message: 'AccessKey不能为空!' }] }]"
        placeholder="请输入AccessKey"
      />
    </a-form-item>
    <a-form-item label="AccessSecret">
      <a-input
        v-decorator="['accessSecret', { rules: [{ required: true, message: 'AccessSecret不能为空!' }] }]"
        placeholder="请输入AccessSecret"
      />
    </a-form-item>
    <a-form-item label="云厂商" :hasFeedback="isCreate">
      <a-select
        v-decorator="[
          'typeName',
          { rules: [isCreate?{ required: true, message: '请选择云厂商!' }:{}] },
        ]"
        placeholder="请选择云厂商"
        :disabled="!isCreate"
      >
        <a-select-option v-for="d in cloudList" :key="isCreate?d.value:d.label">{{ d.label }}</a-select-option>
      </a-select>
    </a-form-item>
    <!-- <a-form-item label="状态" v-if="!isCreate">
      <a-select
        v-decorator="[
          'status',
          { rules: [{ required: true, message: '请选择云厂商状态!' }] },
        ]"
        placeholder="请选择云厂商状态"
      >
        <a-select-option v-for="d in statusList" :key="d.value">{{ d.label }}</a-select-option>
      </a-select>
    </a-form-item>-->
  </a-form>
</template>
<script>
export default {
  props: {
    isCreate: {
      type: Boolean,
      default: true,
    },
    cloudList: {
      type: Array,
      default: true,
    },
  },
  data() {
    return {
      form: this.$form.createForm(this),
      // // 云厂商列表
      // statusList: [
      //   { value: 1, label: '启用' },
      //   { value: 2, label: '不启用' },
      // ],
    }
  },
  methods: {},
  created() {},
}
</script>