<template>
  <div id="app">
    <md-toolbar>
      <md-button class="md-icon-button" @click="$refs.sidenav.toggle()">
        <md-icon>menu</md-icon>
      </md-button>
      <h1 class="md-title">Kafka Brod</h1>
    </md-toolbar>
    <md-sidenav class="md-left" ref="sidenav">
      <md-toolbar>
        <md-button class="md-icon-button" @click="$refs.sidenav.toggle()">
          <md-icon>menu</md-icon>
        </md-button>
      </md-toolbar>
      <md-list>
        <router-link tag="li" to="brokers" class="md-list-item" >
          <div class="md-list-item-container md-button" @click="$refs.sidenav.toggle()">
            <md-icon>computer</md-icon><span>Brokers</span>
          </div>
        </router-link>
        <router-link tag="li" to="topics" class="md-list-item">
          <div class="md-list-item-container md-button" @click="$refs.sidenav.toggle()">
              <md-icon>book</md-icon><span>Topics</span>
          </div>
        </router-link>
        <router-link  tag="li" to="groups" class="md-list-item">
          <div class="md-list-item-container md-button" @click="$refs.sidenav.toggle()">
              <md-icon>group</md-icon><span>Consumer Groups</span>
          </div>
        </router-link>
      </md-list>
    </md-sidenav>
    <div class="main-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
export default {
  name: 'app'
}
</script>

<style>
#app {
}
</style>
