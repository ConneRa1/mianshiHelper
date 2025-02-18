import axios from 'axios';
import { message } from 'antd';

const BACKEND_URL = process.env.NODE_ENV === 'production' 
  ? 'http://mianshi.conner.asia/api'  
  : 'http://localhost:8101/api';

const instance = axios.create({
    baseURL: BACKEND_URL,
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true
});

// 请求拦截器
instance.interceptors.request.use(
    (config) => {
        // 对于 AI 相关的请求，设置更长的超时时间
        if (config.url?.includes('/chat/normal-response')) {
            config.timeout = 60000; // 60秒
        }
        // 移除开头的 /api（如果存在）
        if (config.url?.startsWith('/api')) {
            config.url = config.url.substring(4);
        }
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        
        // 从 cookie 中获取 satoken
        const getCookie = (name: string) => {
            const value = `; ${document.cookie}`;
            const parts = value.split(`; ${name}=`);
            if (parts.length === 2) return parts.pop()?.split(';').shift();
            return null;
        };

        // 尝试从不同的 cookie 名称获取 token
        const satoken = getCookie('satoken') || getCookie('mianshiya');
        if (satoken) {
            config.headers['satoken'] = satoken;
        }
        
        return config;
    },
    (error) => {
        console.error('Request error:', error);
        return Promise.reject(error);
    }
);

// 响应拦截器
instance.interceptors.response.use(
    (response) => {
        const { data } = response;
        // 这里直接返回整个响应数据，让业务代码处理具体的成功失败逻辑
        return data;
    },
    (error) => {
        let errorMsg = '请求失败';
        
        if (axios.isCancel(error)) {
            errorMsg = '请求已取消';
        } else if (error.code === 'ECONNABORTED') {
            if (error.config?.url?.includes('/chat/normal-response')) {
                errorMsg = 'AI响应超时，请重试';
            } else {
                errorMsg = '请求超时，请检查网络连接';
            }
        } else if (error.response) {
            switch (error.response.status) {
                case 401:
                    errorMsg = '未授权，请重新登录';
                    // 可以在这里处理登录跳转
                    window.location.href = '/login';
                    break;
                case 403:
                    errorMsg = '拒绝访问';
                    break;
                case 404:
                    errorMsg = '请求的资源不存在';
                    break;
                case 500:
                    errorMsg = '服务器错误，请稍后重试';
                    break;
                default:
                    errorMsg = `请求失败: ${error.message}`;
            }
        } else if (error.request) {
            errorMsg = '无法连接到服务器，请检查网络连接';
        }
        
        // 使用 antd 的 message 组件显示错误信息
        message.error(errorMsg);
        console.error('Response error:', error);
        return Promise.reject(new Error(errorMsg));
    }
);

export const request = {
    get: <T = any>(url: string, config?: any) => 
        instance.get<any, T>(url, config),
    
    post: <T = any>(url: string, data?: any, config?: any) =>
        instance.post<any, T>(url, data, config),
    
    put: <T = any>(url: string, data?: any, config?: any) =>
        instance.put<any, T>(url, data, config),
    
    delete: <T = any>(url: string, config?: any) =>
        instance.delete<any, T>(url, config),
};

export default request; 