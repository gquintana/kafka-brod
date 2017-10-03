import Vue from 'vue'
import Router from 'vue-router'
import Hello from '@/components/Hello'
import Brokers from '@/components/Brokers'
import Topics from '@/components/Topics'
import Groups from '@/components/Groups'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Hello',
      component: Hello
    },
    {
      path: '/brokers',
      name: 'Brokers',
      component: Brokers
    },
    {
      path: '/topics',
      name: 'Topics',
      component: Topics
    },
    {
      path: '/groups',
      name: 'Consumer Groups',
      component: Groups
    }
  ]
})
