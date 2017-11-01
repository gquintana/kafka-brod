<template>
  <div>
    <h2>Login</h2>
    <b-container>
      <b-form @submit="onSubmit">
        <b-form-group label="User name:" label-for="userName">
          <b-form-input id="userName" type="text" v-model="form.userName" required
                        placeholder="Enter user"
          ></b-form-input>
        </b-form-group>
        <b-form-group label="Password:" label-for="password">
          <b-form-input id="password" type="password" v-model="form.password" required
                        placeholder="Enter password"
          ></b-form-input>
        </b-form-group>
        <b-button type="submit" variant="primary">Submit</b-button>
        <b-button type="reset" variant="secondary">Reset</b-button>
      </b-form>
    </b-container>
  </div>
</template>
<script>
  import axiosService from '../services/AxiosService'
  export default {
    data: function () {
      return {
        form: {
          userName: null,
          password: null
        }
      }
    },
    methods: {
      onSubmit (event) {
        event.preventDefault()
        const userName = this.form.userName
        const axiosConfig = {
          headers: {
            'accept': 'application/json',
            'content-type': 'text/plain'
          }
        }
        axiosService.axios.post(`users/${userName}/_auth`, this.form.password, axiosConfig)
          .then(response => axiosService.helper.handleAuth(response))
          .catch(e => axiosService.helper.handleError(`User ${userName} login failed`, e))
      }
    }
  }
</script>
