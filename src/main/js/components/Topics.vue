<template>
  <div>
    <h2>Topics</h2>
    <b-container v-if="topics && topics.length">
      <b-table striped hover :items="topics" @row-clicked="topicClicked" class="table-clickable"/>
    </b-container>

  </div>
</template>

<script>
  import axiosService from '../services/AxiosService'
  export default {
    data: function () {
      return {
        topics: []
      }
    },
    created: function () {
      axiosService.axios.get(`topics`)
        .then(response => {
          this.topics = response.data.map(t => { return { name: t } })
        })
        .catch(e => axiosService.helper.handleError(`Topics load failed`, e))
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
