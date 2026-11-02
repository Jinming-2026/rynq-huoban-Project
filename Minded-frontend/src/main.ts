import { createApp } from 'vue'
import App from './App.vue'
import Vant from 'vant';
import 'vant/lib/index.css';
import * as VueRouter from 'vue-router'
import routes from "./config/router";
import store from './store'
import '../global.css'; // 引入全局样式表

const app = createApp(App)

app.use(Vant);

const router = VueRouter.createRouter({
    history: VueRouter.createWebHistory(),
    routes,
})

// 根据路由切换浏览器标签页标题
router.afterEach((to) => {
    document.title = (to.meta.title as string) || '小R同学伙伴匹配中心';
});

// 3. 注册你需要的组件
app.use(router);
app.use(store);
// 4.将组件挂载在 app 上
app.mount('#app')
