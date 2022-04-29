import storage from 'store'
import { login, logout, getUserInfo, register, perfectInfo } from '@/api/index'
import { ACCESS_TOKEN } from '@/store/mutation-types'
import { welcome } from '@/utils/util'

const user = {
  state: {
    token: '',
    name: '',
    welcome: '',
    avatar: '',
    roles: [],
    info: {}
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_NAME: (state, { name, welcome }) => {
      state.name = name
      state.welcome = welcome
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_INFO: (state, info) => {
      state.info = info
    }
  },

  actions: {
    // 登录
    Login({ commit }, userInfo) {
      return new Promise((resolve, reject) => {
        console.log('userInfo', userInfo)
        login(userInfo).then(response => {
          console.log('response', response)
          const { status, data } = response
          if (status === 1) {
            storage.set(ACCESS_TOKEN, data, 7 * 24 * 60 * 60 * 1000)
            commit('SET_TOKEN', data)
          }
          resolve(response)
        }).catch(error => {
          console.log('error', error)
          reject(error)
        })
      })
    },
    //注册
    Register({ commit }, userInfo) {
      console.log('Register')
      return new Promise((resolve, reject) => {
        register(userInfo).then(response => {
          const { status, data } = response
          if (status === 1) {
            storage.set(ACCESS_TOKEN, data, 7 * 24 * 60 * 60 * 1000)
            commit('SET_TOKEN', data)
          }
          resolve(response)
        }).catch(error => {
          reject(error)
        })
      })
    },
    // 获取用户信息
    GetInfo({ commit }) {
      return new Promise((resolve, reject) => {
        getUserInfo().then(({ status, data }) => {
          if (status === 1) {
            const { userName, headerImg } = data
            commit('SET_NAME', { name: userName || "游客", welcome: welcome() })
            commit('SET_AVATAR', headerImg || '')
            commit('SET_ROLES', ["admin"])
            commit('SET_INFO', data)
            return perfectInfo()
          } else {
            reject(new Error('网络请求错误！'))
          }
        }).then(res => {
          if (res.status === 1) {
            resolve(+res.data)
          }
        }).catch(error => {
          reject(error)
        })
      })
    },
    // user logout
    Logout({ commit, state, dispatch }) {
      return new Promise((resolve, reject) => {
        commit("SET_TOKEN", "");
        commit("SET_ROLES", []);
        storage.remove(ACCESS_TOKEN)
        // reset visited views and cached views
        // to fixed https://github.com/PanJiaChen/vue-element-admin/issues/2485
        resolve();
      });
    },
    // 登出
    // Logout({ commit, state }) {
    //   return new Promise((resolve) => {
    //     logout(state.token).then(() => {
    //       commit('SET_TOKEN', '')
    //       commit('SET_ROLES', [])
    //       storage.remove(ACCESS_TOKEN)
    //       resolve()
    //     }).catch((err) => {
    //       console.log('logout fail:', err)
    //       // resolve()
    //     }).finally(() => {
    //     })
    //   })
    // }

  }
}

export default user
