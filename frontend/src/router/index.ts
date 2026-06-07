import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/layout/index.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/layout/home.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('../views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'permission',
        name: 'Menu',
        component: () => import('../views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Lock' }
      },
      {
        path: 'organization',
        name: 'Organization',
        component: () => import('../views/system/organization/index.vue'),
        meta: { title: '组织管理', icon: 'OfficeBuilding' }
      },
      // 数据标准与模型中心
      {
        path: 'standard/category',
        name: 'StandardCategory',
        component: () => import('../views/standard/category/index.vue'),
        meta: { title: '字段分类管理' }
      },
      {
        path: 'standard/field',
        name: 'StandardField',
        component: () => import('../views/standard/field/index.vue'),
        meta: { title: '字段标准库' }
      },
      {
        path: 'standard/view',
        name: 'StandardView',
        component: () => import('../views/standard/view/index.vue'),
        meta: { title: '数据标准视图' }
      },
      {
        path: 'standard/view/edit',
        name: 'StandardViewEdit',
        component: () => import('../views/standard/view/edit.vue'),
        meta: { title: '新建视图' }
      },
      {
        path: 'standard/view/edit/:id',
        name: 'StandardViewEditId',
        component: () => import('../views/standard/view/edit.vue'),
        meta: { title: '编辑视图' }
      },
      {
        path: 'standard/view/design/:id',
        name: 'StandardViewDesign',
        component: () => import('../views/standard/view/design.vue'),
        meta: { title: '视图设计' }
      },
      {
        path: 'standard/encoding',
        name: 'StandardEncoding',
        component: () => import('../views/standard/encoding/index.vue'),
        meta: { title: '编码规则' }
      },
      {
        path: 'standard/domain',
        name: 'StandardDomain',
        component: () => import('../views/standard/domain/index.vue'),
        meta: { title: '值域管理' }
      },
      // 表单与视图设计中心
      {
        path: 'form/manage',
        name: 'FormManage',
        component: () => import('../views/form/manage/index.vue'),
        meta: { title: '表单管理' }
      },
      {
        path: 'form/design/:id',
        name: 'FormDesign',
        component: () => import('../views/form/design/index.vue'),
        meta: { title: '表单设计器' }
      },
      // 主数据管理
      {
        path: 'data/category',
        name: 'DataCategory',
        component: () => import('../views/data/category/index.vue'),
        meta: { title: '数据分类' }
      },
      {
        path: 'data/maintain',
        name: 'DataMaintain',
        component: () => import('../views/data/maintain/index.vue'),
        meta: { title: '数据维护' }
      },
      // 流程与任务管理中心
      {
        path: 'workflow/define',
        name: 'WorkflowDefine',
        component: () => import('../views/workflow/define/index.vue'),
        meta: { title: '流程定义' }
      },
      {
        path: 'workflow/todo',
        name: 'WorkflowTodo',
        component: () => import('../views/workflow/todo/index.vue'),
        meta: { title: '待办任务' }
      },
      {
        path: 'workflow/done',
        name: 'WorkflowDone',
        component: () => import('../views/workflow/done/index.vue'),
        meta: { title: '已办任务' }
      },
      {
        path: 'workflow/monitor',
        name: 'WorkflowMonitor',
        component: () => import('../views/workflow/monitor/index.vue'),
        meta: { title: '流程监控' }
      },
      // 主数据生命周期管理
      {
        path: 'masterdata/type',
        name: 'MasterdataType',
        component: () => import('../views/masterdata/type/index.vue'),
        meta: { title: '主数据类型' }
      },
      {
        path: 'masterdata/instance',
        name: 'MasterdataInstance',
        component: () => import('../views/masterdata/instance/index.vue'),
        meta: { title: '主数据实例' }
      },
      {
        path: 'masterdata/lifecycle',
        name: 'MasterdataLifecycle',
        component: () => import('../views/masterdata/lifecycle/index.vue'),
        meta: { title: '生命周期管理' }
      },
      {
        path: 'masterdata/relation',
        name: 'MasterdataRelation',
        component: () => import('../views/masterdata/relation/index.vue'),
        meta: { title: '关系管理' }
      },
      // 数据质量管理
      {
        path: 'quality/rule',
        name: 'QualityRule',
        component: () => import('../views/quality/rule/index.vue'),
        meta: { title: '质量规则' }
      },
      {
        path: 'quality/check',
        name: 'QualityCheck',
        component: () => import('../views/quality/check/index.vue'),
        meta: { title: '质量检测' }
      },
      {
        path: 'quality/issue',
        name: 'QualityIssue',
        component: () => import('../views/quality/issue/index.vue'),
        meta: { title: '问题管理' }
      },
      {
        path: 'quality/dashboard',
        name: 'QualityDashboard',
        component: () => import('../views/quality/dashboard/index.vue'),
        meta: { title: '质量看板' }
      },
      // 主数据分发管理中心
      {
        path: 'distribution/topic',
        name: 'DistributionTopic',
        component: () => import('../views/distribution/topic/index.vue'),
        meta: { title: '分发主题' }
      },
      {
        path: 'distribution/subscription',
        name: 'DistributionSubscription',
        component: () => import('../views/distribution/subscription/index.vue'),
        meta: { title: '订阅管理' }
      },
      {
        path: 'distribution/monitor',
        name: 'DistributionMonitor',
        component: () => import('../views/distribution/monitor/index.vue'),
        meta: { title: '分发监控' }
      },
      {
        path: 'distribution/trace',
        name: 'DistributionTrace',
        component: () => import('../views/distribution/trace/index.vue'),
        meta: { title: '数据溯源' }
      },
      // 版本与审计中心
      {
        path: 'version/manage',
        name: 'VersionManage',
        component: () => import('../views/version/manage/index.vue'),
        meta: { title: '版本管理' }
      },
      {
        path: 'version/compare',
        name: 'VersionCompare',
        component: () => import('../views/version/compare/index.vue'),
        meta: { title: '版本对比' }
      },
      {
        path: 'version/audit',
        name: 'VersionAudit',
        component: () => import('../views/version/audit/index.vue'),
        meta: { title: '审计日志' }
      },
      // 系统基础配置中心
      {
        path: 'system/data-permission',
        name: 'SystemDataPermission',
        component: () => import('../views/system/data-permission/index.vue'),
        meta: { title: '数据权限' }
      },
      {
        path: 'system/script',
        name: 'SystemScript',
        component: () => import('../views/system/script/index.vue'),
        meta: { title: '规则脚本' }
      },
      {
        path: 'system/schedule',
        name: 'SystemSchedule',
        component: () => import('../views/system/schedule/index.vue'),
        meta: { title: '定时任务' }
      },
      {
        path: 'system/notification',
        name: 'SystemNotification',
        component: () => import('../views/system/notification/index.vue'),
        meta: { title: '消息通知' }
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('../views/system/config/index.vue'),
        meta: { title: '系统配置' }
      },
      {
        path: 'system/integration',
        name: 'SystemIntegration',
        component: () => import('../views/system/integration/index.vue'),
        meta: { title: '外部集成' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.path === '/login') {
    next()
  } else {
    if (userStore.token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
