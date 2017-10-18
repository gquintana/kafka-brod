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
  import notificationService from '../services/NotificationService'
  export default {
    data: function () {
      return {
        topics: []
      }
    },
    created: function () {
      axios.get(`topics`)
        .then(response => {
          this.topics = response.data.map(t => { return { name: t } })
        })
        .catch(e => notificationService.notifyError(`Topics load failed: ${e.message}`))
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
