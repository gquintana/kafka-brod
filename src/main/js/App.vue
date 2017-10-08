<template>
  <div id="app">
    <b-navbar toggleable="md" type="primary" variant="light">
      <b-navbar-brand>Kafka Brod</b-navbar-brand>

      <b-nav>
        <b-nav-item to="/brokers" active-class="active" exact><octicon name="device-desktop"/> Brokers</b-nav-item>
        <b-nav-item to="/topics" active-class="active" exact><octicon name="file"/> Topics</b-nav-item>
        <b-nav-item to="/groups" active-class="active" exact><octicon name="organization"/> Consumer Groups</b-nav-item>
      </b-nav>
    </b-navbar>
    <div class="main-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
  import Octicon from 'vue-octicon/components/Octicon.vue'
  export default {
    name: 'app',
    components: { Octicon }
  }
</script>

<style>
#app {
}
</style>
