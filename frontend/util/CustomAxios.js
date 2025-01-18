import { useEffect } from 'react';
import axios from "axios";
import useAuthStore from '@/store/AuthStore';

const useAxiosInstance = () => {
    const { user, auth } = useAuthStore();

    const axiosInstance = axios.create({
        baseURL: window.location.protocol.concat("//").concat(window.location.hostname).concat(":8082"),
        withCredentials: true, // Enables sending cookies with requests
        headers: {'Authorization': `Bearer=${user?.auth}`}
    });

    // Set up request interceptor
    /* useEffect(() => {
        const requestInterceptor = axiosInstance.interceptors.request.use(config => {
            // Set the Cookie header
            config.headers['Cookie'] = `Bearer=${user?.auth}`; // Adjust as necessary
            return config;
        });

        // Cleanup interceptor on unmount
        return () => {
            axiosInstance.interceptors.request.eject(requestInterceptor);
        };
    }, [user]); */

    return axiosInstance;
};

export default useAxiosInstance;