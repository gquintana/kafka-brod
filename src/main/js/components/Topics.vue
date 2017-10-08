<template>
  <div>
    <h2>Topics</h2>
    <ul v-if="topics && topics.length">
      <router-link tag="li" v-for="topic of topics" key="topic" :to="{name:'Topic', params:{name: topic}}"><a>{{topic}}</a></router-link>
    </ul>

  </div>
</template>

<script>
  import axios from '../services/AxiosService'
  export default {
    data: function () {
      return {
        topics: [],
        errors: []
      }
    },
    created: function () {
      axios.get(`topics`)
        .then(response => {
          this.topics = response.data
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
