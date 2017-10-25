import axios from 'axios'
import notificationService from '../services/NotificationService'

const axiosInstance = axios.create({
  baseURL: '../api/',
  timeout: 1000
})

class AxiosHelper {
  constructor (axios) {
    this.axios = axios
  }

  handleError (message, error) {
    let errorMessage = message
    if (error.response && error.response.data && error.response.data.message) {
      errorMessage = errorMessage + ': ' + error.response.data.message
    } else if (error.message) {
      errorMessage = errorMessage + ': ' + error.message
    }
    notificationService.notifyError(errorMessage)
    if (error.response.status === 403 || error.response.status === 401) {
      notificationService.login()
    }
  }
  handleAuth (response) {
    const authorizationHeader = response.headers['authorization'] || response.headers['Authorization']
    console.log(authorizationHeader)
    const user = response.data
    if (authorizationHeader) {
      this.axios.defaults.headers.common['Authorization'] = authorizationHeader
    }
    notificationService.notify('success', `User ${user.name} login success`)
  }
}

export default {
  axios: axiosInstance,
  helper: new AxiosHelper(axiosInstance)
}
