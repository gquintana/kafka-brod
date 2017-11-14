<template>
  <b-container fluid>
    <b-breadcrumb :items="breadcrumb" />
    <div v-if="groups && groups.length">
      <b-table striped hover :items="groups"
       :current-page="groupsPagination.currentPage" :per-page="groupsPagination.perPage"
        @row-clicked="groupClicked" class="table-clickable">
        <template slot="id" scope="data">
          <router-link :to="{ name: 'ConsumerGroup', params: { id:  data.item.id } }"><a>{{ data.item.id }}</a></router-link>
        </template>
      </b-table>
      <div v-if="groups.length>groupsPagination.perPage">
        <b-pagination :total-rows="groups.length" :per-page="groupsPagination.perPage" v-model="groupsPagination.currentPage" />
      </div>
    </div>
  </b-container>
</template>

<script>
  import axiosService from '../services/AxiosService'
  export default {
    data: function () {
      return {
        groups: [],
        groupsPagination: {
          perPage: 10,
          currentPage: 1
        },
        breadcrumb: [
          {
            text: 'Consumer Groups',
            to: { name: 'ConsumerGroups' }
          }
        ]
      }
    },
    created: function () {
      axiosService.axios.get(`groups`)
        .then(response => {
          this.groups = response.data.map(g => { return { id: g } })
        })
        .catch(e => axiosService.helper.handleError(`Consumer Groups load failed`, e))
    },
    methods: {
      groupClicked: function (group) {
        this.$router.push({ name: 'ConsumerGroup', params: { id: group.id } })
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
