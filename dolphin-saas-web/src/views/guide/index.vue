<template>
  <div class="guide-box">
    <div class="logo-svg">
      <logo-svg />
      <span>海豚工程</span>
    </div>
    <div class="guide-progress">
      <a-progress :percent="(progress * 100) / 2 - 1" :show-info="false" :strokeWidth="5" />
    </div>
    <a-row>
      <a-col :span="10" class="guide-left">
        <div class="guide-left-img">
          <img
            style="display: inline-block; width: 100%; max-width: 100%; height: auto;"
            :src="guideimg"
          />
        </div>
      </a-col>

      <a-col :span="14" class="guide-right">
        <!-- <transition name="fade"> -->
        <div class="guide-content" v-if="progress == 1">
          <div class="content-title">请选择您的场景身份</div>
          <div class="content-text">您已经了解海豚的部分功能，请选择一个适合您的场景身份，开启海豚之旅。</div>
          <div class="scene-select">
            <div class="scene-item">
              <div class="scene-item-icon">
                <img :src="firm" style="width:75px;height:45px;" />
                <div>企业用户</div>
              </div>
              <div class="scene-materials">
                <div>需提供以下材料</div>
                <div>企业名称</div>
                <div>企业规模</div>
                <div>联系人姓名</div>
              </div>
              <a-button type="primary" class="select-btn" @click="selectScene(1)">选择</a-button>
            </div>
            <div class="scene-item">
              <div class="scene-item-icon">
                <img :src="individual" style="width:45px;height:45px;" />
                <div>个人用户</div>
              </div>
              <div class="scene-materials">
                <div>需提供以下材料</div>
                <div>申请者个人信息</div>
              </div>
              <a-button type="primary" class="select-btn" @click="selectScene(2)">选择</a-button>
            </div>
          </div>
        </div>
        <div class="last-step" v-if="progress == 2" @click="lastStep">
          <span class="last-step-icon">
            <a-icon type="arrow-left" class />
          </span>返回场景选择
        </div>
        <div class="guide-content" v-if="progress == 2">
          <div class="content-form" v-if="scene == 1">
            <div class="content-title">下面请创建或加入企业</div>
            <a-form :form="form" layout="vertical" class="mt30">
              <a-form-item>
                <span slot="label">
                  企业名称&nbsp;
                  <a-tooltip title="若是首次注册企业名则默认为管理员账户">
                    <a-icon type="question-circle-o" />
                  </a-tooltip>
                </span>
                <a-auto-complete
                  v-decorator="['companyName', { rules: [{ required: true, message: '请输入企业名称!' },{ pattern:/^[\u4e00-\u9fa5]+$/, message: '企业名称只能输入中文' }] }]"
                  placeholder="请输入企业名称"
                  @change="corporationChange"
                >
                  <template slot="dataSource">
                    <a-select-option
                      v-for="d in corporationList"
                      :key="d.label+d.value"
                      @click="() => (corporationSelect = d)"
                    >{{ d.label }}</a-select-option>
                  </template>
                  <a-input />
                </a-auto-complete>
              </a-form-item>
              <a-form-item label="企业规模">
                <a-select
                  v-decorator="['companyType',{ rules: [{ required: true, message: '企业规模不可为空!' }] },]"
                  placeholder="请选择企业规模"
                  :disabled="scaleDisabled"
                >
                  <a-select-option v-for="d in scaleList" :key="d.value">{{d.label}}</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="姓名">
                <a-input
                  v-decorator="[
                    'commonName',
                    { rules: [{ required: true, message: '姓名不能为空!' }] }
                  ]"
                  placeholder="请输入姓名"
                />
              </a-form-item>
            </a-form>
            <a-button
              type="primary"
              class="next-step"
              @click="submitCompany"
              :loading="submitBtn"
            >开启海豚之旅</a-button>
          </div>
          <div class="content-form flex" v-if="scene == 2">
            <div class="content-title">下面请填写个人资料</div>
            <a-form :form="form" layout="vertical" class="mt30">
              <a-form-item label="姓名">
                <a-input
                  v-decorator="[
                    'commonName',
                    { rules: [{ required: true, message: '姓名不能为空!' }]}
                  ]"
                  placeholder="请输入姓名"
                />
              </a-form-item>
            </a-form>
            <a-button
              type="primary"
              class="next-step"
              @click="submitUser"
              :loading="submitBtn"
            >开启海豚之旅</a-button>
          </div>
        </div>
        <!-- </transition> -->
      </a-col>
    </a-row>
  </div>
</template>

<script>
import LogoSvg from '@/assets/logo.svg?inline'
import guideimg from '@/assets/images/guide.png'
import firm from '@/assets/images/firm.png'
import individual from '@/assets/images/individual.png'
import {
  perfectInfo,
  guideInfoUsers,
  guideInfoCompany,
  getMerchantName,
} from '@/api/index'
export default {
  components: {
    LogoSvg,
  },
  data() {
    return {
      firm,
      guideimg,
      individual,
      progress: 1, //进度
      scene: 1, //用户场景
      form: this.$form.createForm(this),
      scaleList: [
        { value: 0, label: '10人以下' },
        { value: 1, label: '10人至300人' },
        { value: 2, label: '300人至1000人' },
        { value: 3, label: '1000人以上' },
      ],
      scaleDisabled: false,
      corporationList: [],
      corporationSelect: {},
      submitBtn: false,
    }
  },
  mounted() {
    perfectInfo().then(({ status, data, msg }) => {
      if (status === 1) {
        if (data == '1') this.$router.push({ path: '/' })
      } else {
        this.$message.error(msg)
      }
    })
  },
  methods: {
    lastStep() {
      this.progress = 1
    },
    selectScene(scene) {
      this.scene = scene
      this.nextStep()
    },
    nextStep() {
      this.progress = 2
    },
    submitUser() {
      const {
        form: { validateFields },
      } = this
      this.submitBtn = true
      const validateFieldsKey = ['commonName']
      validateFields(validateFieldsKey, { force: true }, (err, values) => {
        if (!err) {
          guideInfoUsers({ ...values }).then(({ status, data, msg }) => {
            if (status == 1) {
              this.$message.success(msg)
              this.$router.push({ name: 'beginnerTask' })
            } else {
              this.$message.error(msg)
            }
          })
        } else {
          setTimeout(() => {
            this.submitBtn = false
          }, 600)
        }
      })
    },
    //企业提交
    submitCompany() {
      const {
        form: { validateFields },
      } = this
      this.submitBtn = true
      const validateFieldsKey = ['companyName', 'companyType', 'commonName']
      validateFields(validateFieldsKey, { force: true }, (err, values) => {
        if (!err) {
          let merchantId = this.corporationSelect.value || undefined
          guideInfoCompany({ ...values, ...{ merchantId: merchantId } }).then(
            ({ status, data, msg }) => {
              if (status === 1) {
                this.$message.success(msg)
                this.$router.push({ name: 'beginnerTask' })
              } else {
                this.$message.error(msg)
              }
            }
          )
        } else {
          this.submitBtn = false
        }
      })
    },
    //联想企业名称
    async corporationChange(value) {
      value = value.replace(/[^\u4E00-\u9FA5]/g, '')
      if (!value) {
        this.corporationList = []
      } else {
        await getMerchantName({ MerchantName: value })
          .then(({ status, data, msg }) => {
            if (status == 1) {
              this.corporationList = data || []
            }
          })
          .catch((err) => {
            this.corporationList = []
          })
      }
      //回显企业规模
      this.$nextTick(() => {
        let types = undefined
        let corporationSelect = this.corporationSelect
        let corporationList = this.corporationList
        //判断是否有选择企业
        if (
          Object.keys(corporationSelect).length == 0 &&
          corporationList.length > 0
        ) {
          corporationSelect =
            corporationList.find((item) => item.label === value) || {}
          this.corporationSelect = corporationSelect
        }
        if (corporationSelect.label == value) {
          types = corporationSelect.types
          this.scaleDisabled = true
        } else {
          this.scaleDisabled = false
        }
        this.form.setFieldsValue({ companyName: value, companyType: types })
      })
    },
  },
}
</script>

<style lang="less" scope>
.fade-enter-active {
  transition: all 0.6s linear;
}
.fade-leave-active {
  transition: all 0s linear;
}
.fade-enter {
  opacity: 0;
}
.fade-leave-to {
  opacity: 0;
}
.fade-enter.fade-enter-active,
.fade-appear.fade-appear-active {
  -webkit-animation-name: none;
  animation-name: none;
}

.fade-leave.fade-leave-active {
  -webkit-animation-name: none;
  animation-name: none;
}
.mt30 {
  margin-top: 30px;
}
.guide-box {
  width: 100vw;
  height: 100vh;
  min-width: 800px;
  .guide-progress {
    width: 300px;
    height: 0;
    position: fixed;
    z-index: 1;
    background: #f5f6f7;
    position: fixed;
    top: 50%;
    right: -130px;
    transform: translateY(-50%);
    transform: rotate(90deg);
  }
  .logo-svg {
    width: 100vw;
    height: 54px;
    margin-top: 10px;
    font-size: 20px;
    font-weight: bold;
    position: fixed;
    z-index: 1;
    left: 0;
    top: 0;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    padding: 0 20px;
    svg {
      height: 42px;
      width: 42px;
    }
    span {
      margin-left: 10px;
    }
  }
  .guide-left,
  .guide-right {
    background: #fafbff;
    height: 100vh;
  }
  .guide-left {
    display: flex;
    justify-content: center;
    align-items: center;
    &-img {
      max-width: 552px;
      max-height: 583px;
      min-width: 276px;
      min-height: 292px;
      margin-right: 20px;
    }
  }
  .guide-right {
    background: #fff;
    position: relative !important;
    .guide-content {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      min-width: 410px;
      max-width: 500px;
      height: 458px;
      padding: 20px;
      text-align: center;
      .next-step {
        cursor: pointer;
        width: 210px;
        height: 46px;
        border-radius: 4px;
        background: #1890ff;
        margin-right: auto;
        margin-left: auto;
        // margin-bottom: 32px;
      }
      .content-form {
        height: 265px;
      }
      .flex {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
      }
      .content-title {
        font-size: 24px;
        font-weight: bold;
        color: #000000;
        line-height: 40px;
        margin-bottom: 10px;
      }
      .content-text {
        font-size: 14px;
        color: #999999;
        line-height: 16px;
        margin-bottom: 20px;
      }
      .content-logo {
        padding-bottom: 30px;
        padding-right: 90px;
        display: flex;
        align-items: center;
        justify-content: center;
        svg {
          height: 150px;
          width: 150px;
        }
      }
      .scene-select {
        display: flex;
        justify-content: space-between;
        .scene-item {
          background: #fff;
          border: 1px solid #cfd5de;
          border-radius: 6px;
          padding: 15px 20px;
          width: 195px;
          &:first-child {
            margin-right: 30px;
          }
          &-icon {
            text-align: center;
            width: 150px;
            line-height: 44px;
            font-size: 16px;
            font-weight: bold;
            text-align: center;
            border-bottom: 1px solid #ebebeb;
          }
          .scene-materials {
            height: 151px;
            font-size: 14px;
            text-align: left;
            padding: 20px 0;
            color: #666;
            & div:nth-child(1) {
              font-weight: bold;
              color: #000000;
            }
            & div:not(:first-child)::before {
              content: '·';
              font-size: 20px;
              font-weight: bold;
              padding-right: 5px;
            }
          }
          .select-btn {
            width: 75px;
            height: 30px;
            background: #1890ff;
            border-radius: 4px;
          }
        }
      }
    }
    .last-step {
      position: absolute;
      top: 20px;
      left: 15px;
      font-size: 14px;
      height: 21px;
      line-height: 21px;
      font-weight: 500;
      color: #010101;
      cursor: pointer;
      z-index: 2;
      &-icon {
        padding: 4px 6px;
        margin-right: 6px;
        background: #ffffff;
        box-shadow: 0px 0px 8px 0px rgba(0, 0, 0, 0.15);
        border-radius: 50%;
        font-weight: bold;
      }
      &:hover {
        color: #7188ff;
        .last-step-icon {
          color: #7188ff;
        }
      }
    }
  }
}
::v-deep .ant-form-item {
  margin-bottom: 10px;
}
</style>
