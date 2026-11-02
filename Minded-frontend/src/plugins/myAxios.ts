// 创建一个 axios
import axios from "axios";

const myAxios = axios.create({
    headers: {
        'X-Custom-Header': 'foobar'
    },
    baseURL: 'http://localhost:8080/api',
    // baseURL: 'http://120.55.73.95:9090/api',
    timeout: 50000,
});

myAxios.defaults.withCredentials = true;  // 向后端发送请求携带上 cookie


// 添加请求拦截器
myAxios.interceptors.request.use(function (config) {
    // 在发送请求之前做些什么
    return config;
}, function (error) {
    // 对请求错误做些什么
    console.log("请求错误+" + error);
    return Promise.reject(error);
});

// 添加响应拦截器
myAxios.interceptors.response.use((response => {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么
    return response.data;
}), error => {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    console.log("响应错误+" + error);
    return Promise.reject(error);
});

export default myAxios;
