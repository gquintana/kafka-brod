import axios from 'axios'

const AxiosService = axios.create({
  baseURL: '../api/',
  timeout: 5000
})

export default AxiosService
