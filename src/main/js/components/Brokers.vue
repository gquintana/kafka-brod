<template>
  <div>
    <h2>Brokers</h2>
    <ul v-if="brokers && brokers.length">
      <router-link tag="li" v-for="broker of brokers" key="broker" :to="{name:'Broker', params:{id: broker}}">{{broker}}</router-link>
    </ul>
  </div>
</template>

<script>
  import axios from '../services/AxiosService'
  export default {
    data: function () {
      return {
        brokers: [],
        errors: []
      }
    },
    created: function () {
      axios.get(`brokers`)
        .then(response => {
          this.brokers = response.data
        })
        .catch(e => {
          this.errors.push(e)
        })
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
