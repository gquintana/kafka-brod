<template>
  <b-container fluid>
    <b-breadcrumb :items="breadcrumb" />
    <div v-if="topics && topics.length">
      <b-table striped hover :items="topics"
       :current-page="topicsPagination.currentPage" :per-page="topicsPagination.perPage"
        @row-clicked="topicClicked" class="table-clickable">
        <template slot="name" scope="data">
          <router-link :to="{ name: 'Topic', params: { name:  data.item.name } }"><a>{{ data.item.name }}</a></router-link>
        </template>
      </b-table>
      <div v-if="topics.length>topicsPagination.perPage">
        <b-pagination :total-rows="topics.length" :per-page="topicsPagination.perPage" v-model="topicsPagination.currentPage" />
      </div>
    </div>
  </b-container>
</template>

<script>
  import axiosService from '../services/AxiosService'
  export default {
    data: function () {
      return {
        topics: [],
        topicsPagination: {
          perPage: 10,
          currentPage: 1
        },
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
