'use client';
import React, { useState, useRef, useEffect } from 'react';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { TabView, TabPanel } from 'primereact/tabview';
import { Toast } from 'primereact/toast';
import useAuthStore from '@/store/AuthStore';
import useAxiosInstance from '@/util/CustomAxios';
import { useRouter } from 'next/navigation';
import { Dialog } from 'primereact/dialog';

const Profile = () => {

  const { user, update } = useAuthStore();
  const axiosInstance = useAxiosInstance();
  const router = useRouter();

  const toast = useRef(null);

  const [accountInfo, setAccountInfo] = useState(null);
  const [newPassword, setNewPassword] = useState(null);

  const [loadingSaveDetails, setLoadingSaveDetails] = useState(false);
  const [loadingSavePassword, setLoadingSavePassword] = useState(false);

  const [visibleDialog, setVisibleDialog] = useState(false);

  useEffect(() => {
    setAccountInfo(user);
  }, [])

  const baseApi = () => {
    return "/api/user";
  }

  const updateDetails = () => {
    setLoadingSaveDetails(true);
    axiosInstance.post(baseApi() + "/update", accountInfo)
      .then((res) => {
        setLoadingSaveDetails(false);
        if (res.data.success) {
          update(accountInfo);
          toast.current.show({ severity: 'success', summary: res.data.message });
          if (res.data.data.needRelog) {
            setVisibleDialog(true);
          }
        } else {
          toast.current.show({ severity: 'error', summary: 'Error updating details', detail: res.data.message });
        }
      })
      .catch((error) => {
        setLoadingSaveDetails(false);
        toast.current.show({ severity: 'error', summary: 'Error updating details', detail: 'Please try again' });
      });
  }

  const updatePassword = () => {
    setLoadingSavePassword(true);
    axiosInstance.post(baseApi() + "/changePassword", { "id": accountInfo?.id, "password": newPassword })
      .then((res) => {
        setLoadingSavePassword(false);
        if (res.data.success) {
          setNewPassword("");
          toast.current.show({ severity: 'success', summary: res.data.message });
          setVisibleDialog(true);
        } else {
          toast.current.show({ severity: 'error', summary: 'Error updating password', detail: res.data.message });
        }
      })
      .catch((error) => {
        setLoadingSavePassword(false);
        toast.current.show({ severity: 'error', summary: 'Error updating password', detail: 'Please try again' });
      });
  }


  const footerDialog = (
    <div>
      <Button label="OK" icon="pi pi-check" onClick={() => router.push("/auth/login")} size="small" autoFocus />
    </div>
  );

  return (
    <div className="grid p-fluid">
      <div className="col-12 md:col-12">
        <div className="card">
          <Toast ref={toast} />
          <TabView>
            <TabPanel header="Details">
              <div className="grid flex justify-content-center">
                <div className="field col-7 md:col-7 mt-4">
                  <span className="p-float-label">
                    <InputText id="username" value={accountInfo?.username} className="w-full"
                      onChange={(e) => setAccountInfo({ ...accountInfo, username: e.target.value })} />
                    <label htmlFor="username">Username</label>
                  </span>
                </div>
                <div className="field col-7 md:col-7">
                  <span className="p-float-label">
                    <InputText id="email" value={accountInfo?.email} className="w-full"
                      onChange={(e) => setAccountInfo({ ...accountInfo, email: e.target.value })} />
                    <label htmlFor="email">Email</label>
                  </span>
                </div>
                <div className="field col-7 md:col-7">
                  <span className="p-float-label">
                    <InputText id="fullName" value={accountInfo?.fullName} className="w-full"
                      onChange={(e) => setAccountInfo({ ...accountInfo, fullName: e.target.value })} />
                    <label htmlFor="fullName">Full Name</label>
                  </span>
                </div>

                <div className="field col-7 md:col-7">
                  <Button label="Save" className="w-full" onClick={updateDetails} loading={loadingSaveDetails} text />
                </div>
              </div>
            </TabPanel>

            <TabPanel header="Change Password">
              <div className="grid flex justify-content-center mt-4">
                <div className="field col-7 md:col-7">
                  <span className="p-float-label">
                    <Password inputId="password" value={newPassword} className="w-full" inputClassName="w-full"
                      onChange={(e) => setNewPassword(e.target.value)} feedback={true} />
                    <label htmlFor="password">New Password</label>
                  </span>
                </div>

                <div className="field col-7 md:col-7">
                  <Button label="Save" className="w-full" onClick={updatePassword} loading={loadingSavePassword} text />
                </div>
              </div>
            </TabPanel>
          </TabView>
          <Dialog header="" visible={visibleDialog} footer={footerDialog}
            onHide={() => router.push("/auth/login")} >
            <p>Changes applied. You will be logged out.</p>
          </Dialog>
        </div>
      </div>
    </div>

  );
}
export default Profile;