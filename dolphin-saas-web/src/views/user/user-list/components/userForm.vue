<template>
  <a-form :form="form" :label-col="{ span: 5 }" layout="horizontal" :wrapper-col="{ span: 17 }">
      <a-form-item label="用户名">
      <a-input
        v-decorator="['userName',{rules: [{ required: true, message: '用户名不能为空!' },{ pattern: /^[0-9a-zA-Z_]{1,30}$/, message: '用户名只能输入字母、数字和_，且长度不超过20' }
            ],
            getValueFromEvent: getValueFromEvent
          }
        ]"
        placeholder="请输入用户名"
      />
    </a-form-item>
    <a-form-item label="手机号">
      <a-input
        v-decorator="[
          'phone',
          {
            rules: [
              { required: true, message: '手机号不能为空!' },
              { pattern: /^(1[2-9][0-9])\d{8}$/, message: '手机号码不正确' }
            ],
            getValueFromEvent: getValueFromEvent
          }
        ]"
        placeholder="请输入手机号"
      />
    </a-form-item>
    <a-form-item label="密码">
      <a-input-password
        v-decorator="[
          'password',
          {
            rules: [
              { required: true, message: '密码不能为空' },
              {
                pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/,
                message: '密码长度至少为6，至少含有一个字母和一个数字'
              }
            ],
            getValueFromEvent: getValueFromEvent
          }
        ]"
        placeholder="请输入密码"
      ></a-input-password>
    </a-form-item>
    <a-form-item label="昵称">
      <a-input
        v-decorator="[
          'commonName',
          { rules: [{ required: true, message: '昵称不能为空!' }], getValueFromEvent: getValueFromEvent }
        ]"
        placeholder="请输入昵称"
      />
    </a-form-item>

    <a-form-item label="状态">
      <a-select
        v-decorator="['loginStatus', { rules: [{ required: true, message: '状态不能为空!' }] }]"
        placeholder="请选择状态"
      >
        <a-select-option v-for="d in statusList" :key="d.value">{{ d.label }}</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="用户角色">
      <a-select
        v-decorator="['adminStatus', { rules: [{ required: true, message: '用户角色不能为空!' }] }]"
        placeholder="请选择角色"
      >
        <a-select-option v-for="d in roleList" :key="d.value">{{ d.label }}</a-select-option>
      </a-select>
    </a-form-item>
  </a-form>
</template>
<script>
export default {
  props: {
    isCreate: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      form: this.$form.createForm(this),
      statusList: [
        { label: '未启用', value: 0 },
        { label: '启用', value: 1 },
      ],
      roleList: [
        { label: '普通用户', value: 0 },
        { label: '管理员', value: 1 },
      ],
    }
  },
  methods: {
    getValueFromEvent(e) {
      return e.target.value.replace(/(^\s*)|(\s*$)/g, '')
    },
  },
  created() {},
}
</script>
