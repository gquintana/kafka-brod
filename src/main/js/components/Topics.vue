<template>
  <b-container>
    <b-breadcrumb :items="breadcrumb" />
    <div v-if="topics && topics.length">
      <b-table striped hover :items="topics" @row-clicked="topicClicked" class="table-clickable"/>
    </div>
  </b-container>
</template>

<script>
  import axiosService from '../services/AxiosService'
  export default {
    data: function () {
      return {
        topics: [],
        breadcrumb: [
          {
            text: 'Topics',
            to: { name: 'Topics' }
          }
        ]
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
