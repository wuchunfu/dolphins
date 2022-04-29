import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import i18n from './locales'

import '@/assets/style/global.less' // global style
import '@/utils/lazy_use' // use lazy load components
import './permission' // permission control
import '@/utils/filter'

Vue.config.productionTip = false

new Vue({
  router,
  store,
  i18n,
  render: h => h(App)
}).$mount('#app')
