import Vue from 'vue'
import Router from 'vue-router'
import { LoginLayout, DefaultLayout } from '@/layouts'
import { dashboard, asset, config, cluster, engineer, rocket } from '@/icons'
const RouteView = {
  name: 'RouteView',
  render: h => h('router-view')
}
export const asyncRouterMap = [
  {
    path: '/',
    component: DefaultLayout,
    meta: { title: '首页' },
    redirect: '/dashboard',
    children: [
      // 
      {
        path: '/beginnerTask',
        name: 'beginnerTask',
        component: RouteView,
        component: () => import('@/views/beginner-task/index'),
        meta: { title: '新手引导', icon: rocket, permission: ['admin'] }
      },
      // dashboard
      {
        path: '/dashboard',
        name: 'dashboard',
        component: RouteView,
        component: () => import('@/views/dashboard/index'),
        meta: { title: '数据助手', icon: dashboard, permission: ['admin'] }
      },
      // cloudConfig
      {
        path: '/cloudConfig',
        name: 'cloudConfig',
        component: RouteView,
        component: () => import('@/views/cloud-config/index'),
        meta: { title: '云厂商配置', icon: config, permission: ['admin'] },
      },
      // cloudAsset
      {
        path: '/cloudAsset',
        name: 'cloudAsset',
        component: RouteView,
        component: () => import('@/views/cloud-asset/index'),
        meta: { title: '云资产库', icon: asset, permission: ['admin'] },
      },
      // cluster
      {
        path: '/cluster',
        name: 'cluster',
        component: RouteView,
        component: () => import('@/views/cluster-manage/index'),
        meta: { title: '集群管理', icon: cluster, permission: ['admin'] },
      },
      {
        path: '/engineer',
        name: 'engineer',
        component: RouteView,
        redirect: "/strategy",
        meta: { title: '工程管理', icon: engineer, permission: ['admin'] },
        children: [
          {
            path: '/strategy',
            name: 'strategy',
            component: () => import('@/views/engineer/release-strategy'),
            meta: { title: '发布策略', permission: ['admin'] }
          },
          {
            path: '/project',
            name: 'project',
            component: () => import('@/views/engineer/project-manage'),
            meta: { title: '项目工程', permission: ['admin'] }
          },
          {
            path: '/release',
            name: 'release',
            component: () => import('@/views/engineer/release-manage'),
            meta: { title: '发布上线', permission: ['admin'] }
          },
        ]
      },
      // Exception
      {
        path: '/exception',
        name: 'exception',
        hidden: true,
        component: RouteView,
        redirect: '/exception/403',
        meta: { title: 'menu.exception', icon: 'warning', permission: ['exception'] },
        children: [
          {
            path: '/exception/403',
            name: 'Exception403',
            component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/403'),
            meta: { title: 'menu.exception.not-permission', permission: ['exception'] }
          },
          {
            path: '/exception/404',
            name: 'Exception404',
            component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404'),
            meta: { title: 'menu.exception.not-find', permission: ['exception'] }
          },
          {
            path: '/exception/500',
            name: 'Exception500',
            component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/500'),
            meta: { title: 'menu.exception.server-error', permission: ['exception'] }
          }
        ]
      },
    ]
  },
  {
    path: '/guide',
    name: 'guide',
    hidden: true,
    component: () => import('@/views/guide/index'),
    meta: { title: '新手引导', keepAlive: true, permission: ['admin'] },
  },
  {
    path: '*',
    redirect: '/404',
    hidden: true
  }
]

/**
 * 基础路由
 * @type { *[] }
 */
export const constantRouterMap = [
  {
    path: '/sign',
    component: LoginLayout,
    redirect: '/login',
    hidden: true,
    children: [
      {
        path: '/login',
        name: 'login',
        component: () => import(/* webpackChunkName: "user" */ '@/views/sign/Login')
      },
      {
        path: '/register',
        name: 'register',
        component: () => import(/* webpackChunkName: "user" */ '@/views/sign/Register')
      },
      {
        path: '/register-result',
        name: 'registerResult',
        component: () => import(/* webpackChunkName: "user" */ '@/views/sign/RegisterResult')
      },
      {
        path: '/recover',
        name: 'recover',
        component: undefined
      }
    ]
  },

  {
    path: '/404',
    component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404')
  }
]

// hack router push callback
const originalPush = Router.prototype.push
Router.prototype.push = function push(location, onResolve, onReject) {
  if (onResolve || onReject) return originalPush.call(this, location, onResolve, onReject)
  return originalPush.call(this, location).catch(err => err)
}

Vue.use(Router)

console.log('constantRouterMap.concat(asyncRouterMap)', constantRouterMap.concat(asyncRouterMap))

export default new Router({
  mode: 'history',
  scrollBehavior: () => ({ y: 0 }),
  base: process.env.BASE_URL,
  routes: constantRouterMap.concat(asyncRouterMap)
})
