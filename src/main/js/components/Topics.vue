<template>
  <div>
    <h2>Topics</h2>
    <b-container v-if="topics && topics.length">
      <b-table striped hover :items="topics" @row-clicked="topicClicked" class="table-clickable"/>
    </b-container>

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
          this.topics = response.data.map(t => { return { name: t } })
        })
        .catch(e => {
          this.errors.push(e)
        })
    },
    methods: {
      topicClicked: function (topic) {
        this.$router.push({ name: 'Topic', params: { name: topic.name } })
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
