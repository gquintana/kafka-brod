<template>
  <b-container>
    <b-breadcrumb :items="breadcrumb" />
    <div v-if="groups && groups.length">
      <b-table striped hover :items="groups" @row-clicked="groupClicked" class="table-clickable"/>
    </div>
  </b-container>
</template>

<script>
  import axiosService from '../services/AxiosService'
  export default {
    data: function () {
      return {
        groups: [],
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
