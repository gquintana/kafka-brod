import Vue from 'vue'
import Router from 'vue-router'
import Hello from '@/components/Hello'
import Brokers from '@/components/Brokers'
import Broker from '@/components/Broker'
import Topics from '@/components/Topics'
import Topic from '@/components/Topic'
import ConsumerGroups from '@/components/ConsumerGroups'
import ConsumerGroup from '@/components/ConsumerGroup'

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
      path: '/brokers/:id',
      name: 'Broker',
      component: Broker
    },
    {
      path: '/topics',
      name: 'Topics',
      component: Topics
    },
    {
      path: '/topics/:name',
      name: 'Topic',
      component: Topic
    },
    {
      path: '/groups',
      name: 'ConsumerGroups',
      component: ConsumerGroups
    },
    {
      path: '/groups/:id',
      name: 'ConsumerGroup',
      component: ConsumerGroup
    }
  ]
})
