import Vue from "vue";
import Router from "vue-router";
import Hello from "@/components/Hello";
import Login from "@/components/Login";
import Brokers from "@/components/Brokers";
import Broker from "@/components/Broker";
import Topics from "@/components/Topics";
import Topic from "@/components/Topic";
import ConsumerGroups from "@/components/ConsumerGroups";
import ConsumerGroup from "@/components/ConsumerGroup";
import Consumer from "@/components/Consumer";
import notificationService from "../services/NotificationService";

Vue.use(Router)

const router = new Router({
  routes: [
    {
      path: '/',
      name: 'Hello',
      component: Hello
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
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
    },
    {
      path: '/groups/:groupId/consumers/:id',
      name: 'Consumer',
      component: Consumer
    }
  ]
})
router.afterEach((to, from) => {
  notificationService.clearNotification()
})
notificationService.subscribeLogin(() => router.push({name: 'Login'}))

export default router
