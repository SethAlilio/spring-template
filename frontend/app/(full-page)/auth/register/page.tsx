/* eslint-disable @next/next/no-img-element */
'use client';
import { useRouter } from 'next/navigation';
import React, { useContext, useState, useEffect, useRef } from 'react';
import { Button } from 'primereact/button';
import { Password } from 'primereact/password';
import { LayoutContext } from '../../../../layout/context/layoutcontext';
import { InputText } from 'primereact/inputtext';
import { ProgressSpinner } from 'primereact/progressspinner';
import { Toast } from 'primereact/toast';
import { classNames } from 'primereact/utils';
import AuthService from '../../../../service/AuthService';
import useAuthStore from '@/store/AuthStore';
import { Dialog } from 'primereact/dialog';

const RegisterPage = () => {
    const toast = useRef(null);
    const [account, setAccount] = useState({
        "username": "",
        "password": "",
        "fullName": "",
        "email": "",
        "role": "ROLE_ADMIN"
    });

    const [loadingPage, setLoadingPage] = useState(true);
    const [loadingSignUp, setLoadingSignUp] = useState(false);
    const { layoutConfig } = useContext(LayoutContext);
    const [visibleLoginNotice, setVisibleLoginNotice] = useState(false);

    const service = new AuthService();

    const router = useRouter();
    const { user } = useAuthStore();
    const containerClassName = classNames('surface-ground flex align-items-center justify-content-center min-h-screen min-w-screen overflow-hidden', { 'p-input-filled': layoutConfig.inputStyle === 'filled' });
    const containerStyle = {
        background: 'linear-gradient(0deg, rgba(0,0,0,1) 0%, rgba(34,135,195,1) 100%)'
    };
    
    useEffect(() => {
        setLoadingPage(false);
    }, [user]); 

    const signUp = () => {
        setLoadingSignUp(true);
        service.signUp(account)
            .then(res => {
                setLoadingSignUp(false);
                if (res.status === 200) {
                    setVisibleLoginNotice(true);
                } else {
                    // @ts-ignore
                    toast.current.show({severity:'error', detail: res.message, life: 3000});
                }
            })
            .catch((error) => {
                setLoadingSignUp(false);
                // @ts-ignore
                toast.current.show({severity:'error', detail: error?.response?.data?.message || "Register error", life: 3000});
            })
    }

    const onChangeForm = (e) => {
        setAccount({...account, [e.target.id]: e.target.value});
    }

    
    const footerDialog = (
        <div>
            <Button label="OK" icon="pi pi-check" onClick={() => router.push("/auth/login")} size="small" autoFocus />
        </div>
    );

    return (!loadingPage ? 
        <div className={containerClassName} style={containerStyle}>
            <Toast ref={toast} />
            <div className="flex flex-column align-items-center justify-content-center">
                <div
                    style={{
                        borderRadius: '56px',
                        padding: '0.3rem',
                        background: 'linear-gradient(180deg, rgba(34,135,195,1) 10%, rgba(33, 150, 243, 0) 30%)'
                    }}
                >
                    <div className="w-full surface-card py-8 px-5 sm:px-8" style={{ borderRadius: '53px' }}>
                        <div className="text-center mb-5">
                            <div className="text-900 text-3xl font-medium mb-3">Raffle System Admin Register</div>
                        </div>

                        <div>
                            <label htmlFor="username" className="block text-900 text-xl font-medium mb-2">
                                Username<span className="text-red-500">*</span>
                            </label>
                            <InputText id="username" type="text" value={account?.username} onChange={(e) => onChangeForm(e)} 
                                placeholder="Username" className="w-full md:w-30rem mb-5" style={{ padding: '1rem' }} />

                            <label htmlFor="password" className="block text-900 font-medium text-xl mb-2">
                                Password<span className="text-red-500">*</span>
                            </label>
                            <Password inputId="password" value={account?.password} onChange={(e) => onChangeForm(e)} placeholder="Password" toggleMask 
                                className="w-full mb-5" inputClassName="w-full p-3 md:w-30rem"  feedback={false}></Password>

                            <label htmlFor="fullName" className="block text-900 text-xl font-medium mb-2">
                                Full Name<span className="text-red-500">*</span>
                            </label>
                            <InputText id="fullName" type="text" value={account?.fullName} onChange={(e) => onChangeForm(e)} 
                                placeholder="Full Name" className="w-full md:w-30rem mb-5" style={{ padding: '1rem' }} />

                            <label htmlFor="email" className="block text-900 text-xl font-medium mb-2">
                                Email
                            </label>
                            <InputText id="email" type="text" value={account?.email} onChange={(e) => onChangeForm(e)} 
                                placeholder="Email" className="w-full md:w-30rem mb-5" style={{ padding: '1rem' }} />

                            <div className="flex align-items-center justify-content-between mb-2 gap-5">
                            </div>
                            
                            <Button label="Sign Up" className="w-full p-3 text-xl" onClick={() => signUp()} loading={loadingSignUp}></Button>
                            <Button label="Already have account" className="w-full p-3 text-xl" text onClick={() => router.push('/auth/login')}></Button>
                        </div>
                    </div>
                </div>
            </div>
            <Dialog header="" visible={visibleLoginNotice} footer={footerDialog}
                onHide={() => router.push("/auth/login")} >
                <p>Registered successfully. You can now login.</p>
            </Dialog>
        </div>
        : 
        <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100vh' // Full viewport height
        }}>
            <ProgressSpinner />
        </div>
    );
};

export default RegisterPage;
