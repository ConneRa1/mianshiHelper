import { useEffect } from 'react';
import { useRouter } from 'next/router';

export const AuthCheck = ({ children }: { children: React.ReactNode }) => {
    const router = useRouter();
    
    useEffect(() => {
        // 检查 cookie 中的 satoken
        const getCookie = (name: string) => {
            const value = `; ${document.cookie}`;
            const parts = value.split(`; ${name}=`);
            if (parts.length === 2) return parts.pop()?.split(';').shift();
            return null;
        };

        const token = localStorage.getItem('token') || getCookie('satoken');
        
        if (!token && !router.pathname.startsWith('/login')) {
            const currentPath = encodeURIComponent(router.asPath);
            router.push(`/login?redirect=${currentPath}`);
        }
    }, [router.pathname]);

    return <>{children}</>;
}; 