import axios from 'axios'

const AxiosService = axios.create({
  baseURL: '../api/',
  timeout: 1000
})

export default AxiosService
