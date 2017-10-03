<template>
  <div>
    <h2>Topics</h2>
    <ul v-if="topics && topics.length">
      <li v-for="topic of topics">{{topic}}</li>
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
          console.log(response.data)
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
