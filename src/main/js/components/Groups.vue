<template>
  <div>
    <h2>Groups</h2>
    <ul v-if="groups && groups.length">
      <li v-for="group of groups">{{group}}</li>
    </ul>
  </div>
</template>

<script>
  import axios from '../services/AxiosService'
  export default {
    data: function () {
      return {
        groups: [],
        errors: []
      }
    },
    created: function () {
      axios.get(`groups`)
        .then(response => {
          console.log(response.data)
          this.groups = response.data
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
