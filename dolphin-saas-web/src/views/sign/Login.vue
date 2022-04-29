<template>
  <div class="main">
    <a-form
      id="formLogin"
      class="user-layout-login"
      ref="formLogin"
      :form="form"
      @submit="handleSubmit"
    >
      <a-tabs
        :activeKey="customActiveKey"
        :tabBarStyle="{ textAlign: 'center', borderBottom: 'unset' }"
        @change="handleTabClick"
      >
        <!-- 账号登录 -->
        <a-tab-pane key="account" :tab="$t('user.login.tab-login-credentials')">
          <a-alert
            v-if="isLoginError"
            type="error"
            showIcon
            style="margin-bottom: 24px;"
            :message="errMessage||$t('user.login.message-invalid-credentials')"
          />
          <a-form-item>
            <a-input
              size="large"
              type="text"
              :placeholder="$t('user.login.username.placeholder')"
              v-decorator="[
                'userName',
                {rules: [{ required: true, message: $t('user.userName.required') }, { validator: handleUsernameOrEmail }], validateTrigger: 'change'}
              ]"
            >
              <a-icon slot="prefix" type="user" :style="{ color: 'rgba(0,0,0,.25)' }" />
            </a-input>
          </a-form-item>

          <a-form-item>
            <a-input-password
              size="large"
              :placeholder="$t('user.login.password.placeholder')"
              v-decorator="[
                'passWord',
                {rules: [{ required: true, message: $t('user.password.required') }], validateTrigger: 'change'}
              ]"
            >
              <a-icon slot="prefix" type="lock" :style="{ color: 'rgba(0,0,0,.25)' }" />
            </a-input-password>
          </a-form-item>
        </a-tab-pane>
        <!-- 手机号登录 -->
        <a-tab-pane key="phone" :tab="$t('user.login.tab-login-mobile')">
          <a-alert
            v-if="isLoginError"
            type="error"
            showIcon
            style="margin-bottom: 24px;"
            :message="$t('user.login.message-invalid-credentials')"
          />
          <a-form-item>
            <a-input
              size="large"
              type="text"
              :placeholder="$t('user.login.mobile.placeholder')"
              v-decorator="['phone', {rules: [{ required: true, pattern: /^1[345789]\d{9}$/, message: $t('user.login.mobile.placeholder') }], validateTrigger: 'change'}]"
            >
              <a-icon slot="prefix" type="mobile" :style="{ color: 'rgba(0,0,0,.25)' }" />
            </a-input>
          </a-form-item>
          <a-form-item>
            <a-input-password
              size="large"
              :placeholder="$t('user.login.password.placeholder')"
              v-decorator="[
                'passWord',
                {rules: [{ required: true, message: $t('user.password.required') }], validateTrigger: 'change'}
              ]"
            >
              <a-icon slot="prefix" type="lock" :style="{ color: 'rgba(0,0,0,.25)' }" />
            </a-input-password>
          </a-form-item>
          <!-- <a-row :gutter="16">
            <a-col class="gutter-row" :span="16">
              <a-form-item>
                <a-input size="large" type="text" :placeholder="$t('user.login.mobile.verification-code.placeholder')" v-decorator="['captcha', {rules: [{ required: true, message: $t('user.verification-code.required') }], validateTrigger: 'blur'}]">
                  <a-icon slot="prefix" type="mail" :style="{ color: 'rgba(0,0,0,.25)' }" />
                </a-input>
              </a-form-item>
            </a-col>
            <a-col class="gutter-row" :span="8">
              <a-button class="getCaptcha" tabindex="-1" :disabled="state.smsSendBtn" @click.stop.prevent="getCaptcha" v-text="!state.smsSendBtn && $t('user.register.get-verification-code') || (state.time+' s')"></a-button>
            </a-col>
          </a-row>-->
        </a-tab-pane>
      </a-tabs>

      <a-form-item>
        <a-checkbox
          v-decorator="['rememberMe', { valuePropName: 'checked' }]"
        >{{ $t('user.login.remember-me') }}</a-checkbox>
        <!-- <router-link :to="{ name: 'recover', params: { user: 'aaa'} }" class="forge-password" style="float: right;">{{ $t('user.login.forgot-password') }}</router-link> -->
      </a-form-item>

      <a-form-item style="margin-top:24px">
        <a-button
          size="large"
          type="primary"
          htmlType="submit"
          class="login-button"
          :loading="state.loginBtn"
          :disabled="state.loginBtn"
        >{{ $t('user.login.login') }}</a-button>
      </a-form-item>

      <div class="user-login-other">
        <!-- <span>{{ $t('user.login.sign-in-with') }}</span>
        <a>
          <a-icon class="item-icon" type="alipay-circle"></a-icon>
        </a>
        <a>
          <a-icon class="item-icon" type="taobao-circle"></a-icon>
        </a>
        <a>
          <a-icon class="item-icon" type="weibo-circle"></a-icon>
        </a>-->
        <router-link class="register" :to="{ name: 'register' }">{{ $t('user.login.signup') }}</router-link>
      </div>
    </a-form>

    <two-step-captcha
      v-if="requiredTwoStepCaptcha"
      :visible="stepCaptchaVisible"
      @success="stepCaptchaSuccess"
      @cancel="stepCaptchaCancel"
    ></two-step-captcha>
  </div>
</template>

<script>
import TwoStepCaptcha from '@/components/tools/TwoStepCaptcha'
import { mapActions } from 'vuex'
import { timeFix } from '@/utils/util'
import Cookies from 'js-cookie'
import { encrypt, decrypt } from '@/utils/jsencrypt'
// import { getSmsCaptcha, get2step } from '@/api/login'

export default {
  components: {
    TwoStepCaptcha,
  },
  data() {
    return {
      customActiveKey: 'account',
      loginBtn: false,
      // login type: 0 email, 1 userName, 2 telephone
      loginType: 0,
      isLoginError: false,
      requiredTwoStepCaptcha: false,
      stepCaptchaVisible: false,
      form: this.$form.createForm(this),
      state: {
        time: 60,
        loginBtn: false,
        // login type: 0 email, 1 userName, 2 telephone
        loginType: 0,
        smsSendBtn: false,
      },
      errMessage: '',
      redirect: undefined,
    }
  },
  watch: {
    $route: {
      handler: function (route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true,
    },
  },
  created() {
    // get2step({})
    //   .then(res => {
    //     this.requiredTwoStepCaptcha = res.result.stepCode
    //   })
    //   .catch(() => {
    //     this.requiredTwoStepCaptcha = false
    //   })
    // this.requiredTwoStepCaptcha = true
    this.getCookie()
  },
  methods: {
    ...mapActions(['Login', 'Logout']),
    //记住密码
    getCookie() {
      const userName = Cookies.get('userName') || ''
      const passWord = decrypt(Cookies.get('passWord')) || ''
      const rememberMe = Cookies.get('rememberMe') || false
      console.log('rememberMe', rememberMe)
      const customActiveKey = Cookies.get('customActiveKey') || 'account'
      if (rememberMe) {
        this.customActiveKey = customActiveKey
        setTimeout(() => {
          this.form.setFieldsValue(
            customActiveKey === 'account'
              ? { userName, passWord, rememberMe: true }
              : { phone: userName, passWord, rememberMe: true }
          )
        }, 0)
      }
    },
    // handler
    // 判断是否是邮箱
    handleUsernameOrEmail(rule, value, callback) {
      const { state } = this
      const regex = /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\.[a-zA-Z0-9_-]{2,3}){1,2})$/
      if (regex.test(value)) {
        state.loginType = 0
      } else {
        state.loginType = 1
      }
      callback()
    },
    //切换登录方式
    async handleTabClick(key) {
      this.customActiveKey = key
      this.isLoginError = false
      let rememberMe = await Boolean(this.form.getFieldValue('rememberMe'))
      await this.form.resetFields()
      await this.form.setFieldsValue({ rememberMe })
      // this.form.resetFields()
    },
    //登录事件
    handleSubmit(e) {
      e.preventDefault()
      const {
        form: { validateFields },
        state,
        customActiveKey,
        Login,
      } = this
      state.loginBtn = true
      const validateFieldsKey =
        customActiveKey === 'account' ? ['userName', 'passWord', 'rememberMe'] : ['phone', 'passWord', 'rememberMe']
      validateFields(validateFieldsKey, { force: true }, (err, values) => {
        if (!err) {
          //是否记住密码
          if (values.rememberMe) {
            Cookies.set('userName', customActiveKey === 'account' ? values.userName : values.phone), { expires: 30 }
            Cookies.set('passWord', encrypt(values.passWord), { expires: 30 })
            Cookies.set('rememberMe', values.rememberMe, { expires: 30 })
            Cookies.set('customActiveKey', customActiveKey, { expires: 30 })
          } else {
            Cookies.remove('userName')
            Cookies.remove('passWord')
            Cookies.remove('rememberMe')
            Cookies.remove('customActiveKey')
          }
          //判断是否是手机号
          const regex = /^1[345789][0-9]{9}$/
          console.log('regex', regex.test(values.userName))
          if (regex.test(values.userName)) {
            values.phone = values.userName
            values.userName = undefined
          }
          const loginParams = { ...values }
          //判断是否是邮箱
          // delete loginParams.userName
          // loginParams[!state.loginType ? 'email' : 'userName'] = values.userName
          // loginParams.password = md5(values.password)
          // console.log('loginParams',loginParams)
          Login(loginParams)
            .then(({ status, msg }) => {
              if (status === 1) {
                this.loginSuccess(msg)
              } else {
                this.errMessage = msg
                this.requestFailed(msg)
              }
            })
            .catch((err) => this.requestFailed(err))
            .finally(() => {
              state.loginBtn = false
            })
        } else {
          setTimeout(() => {
            state.loginBtn = false
          }, 600)
        }
      })
    },
    //获取验证码
    // getCaptcha(e) {
    //   e.preventDefault()
    //   const { form: { validateFields }, state } = this

    //   validateFields(['mobile'], { force: true }, (err, values) => {
    //     if (!err) {
    //       state.smsSendBtn = true
    //       const interval = window.setInterval(() => {
    //         if (state.time-- <= 0) {
    //           state.time = 60
    //           state.smsSendBtn = false
    //           window.clearInterval(interval)
    //         }
    //       }, 1000)

    //       const hide = this.$message.loading('验证码发送中..', 0)
    //       getSmsCaptcha({ mobile: values.mobile }).then(res => {
    //         setTimeout(hide, 2500)
    //         this.$notification['success']({
    //           message: '提示',
    //           description: '验证码获取成功，您的验证码为：' + res.result.captcha,
    //           duration: 8
    //         })
    //       }).catch(err => {
    //         setTimeout(hide, 1)
    //         clearInterval(interval)
    //         state.time = 60
    //         state.smsSendBtn = false
    //         this.requestFailed(err)
    //       })
    //     }
    //   })
    // },
    stepCaptchaSuccess() {
      this.loginSuccess()
    },
    stepCaptchaCancel() {
      this.Logout().then(() => {
        this.loginBtn = false
        this.stepCaptchaVisible = false
      })
    },
    loginSuccess(res) {
      console.log(res)
      // check res.homePage define, set $router.push name res.homePage
      // Why not enter onComplete
      /*
      this.$router.push({ name: 'analysis' }, () => {
        console.log('onComplete')
        this.$notification.success({
          message: '欢迎',
          description: `${timeFix()}，欢迎回来`
        })
      })
      */
      this.$router.push({ path: this.redirect || '/' })
      // 延迟 1 秒显示欢迎信息
      setTimeout(() => {
        this.$notification.success({
          message: '欢迎',
          description: `${timeFix()}，欢迎回来`,
        })
      }, 1000)
      this.isLoginError = false
    },
    requestFailed(err) {
      this.isLoginError = true
      // this.$notification['error']({
      //   message: '错误',
      //   description: ((err.response || {}).data || {}).message || '请求出现错误，请稍后再试',
      //   duration: 4
      // })
    },
  },
}
</script>

<style lang="less" scoped>
.user-layout-login {
  label {
    font-size: 14px;
  }

  .getCaptcha {
    display: block;
    width: 100%;
    height: 40px;
  }

  .forge-password {
    font-size: 14px;
  }

  button.login-button {
    padding: 0 15px;
    font-size: 16px;
    height: 40px;
    width: 100%;
  }

  .user-login-other {
    text-align: left;
    margin-top: 24px;
    line-height: 22px;

    .item-icon {
      font-size: 24px;
      color: rgba(0, 0, 0, 0.2);
      margin-left: 16px;
      vertical-align: middle;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: #1890ff;
      }
    }

    .register {
      float: right;
    }
  }
}
</style>
