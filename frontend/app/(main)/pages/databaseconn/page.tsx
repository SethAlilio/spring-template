'use client';
import { useRouter } from 'next/router';
import { Dropdown } from 'primereact/dropdown';
import { FloatLabel } from 'primereact/floatlabel';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Button } from 'primereact/button';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Toast } from 'primereact/toast';

import React, { useState, useRef, useEffect } from 'react';

import useAxiosInstance from '@/util/CustomAxios';
import { Paginator } from 'primereact/paginator';
import { Ripple } from 'primereact/ripple';
import { classNames } from 'primereact/utils';
import { Dialog } from 'primereact/dialog';


const DatabaseConnection = () => {
  const dataTable = useRef(null);
  const paginator = useRef(null);
  const toast = useRef(null);

  const axiosInstance = useAxiosInstance();
  //const router = useRouter();

  const [databaseConn, setDatabaseConn] = useState({
    id: null,
    type: "",
    name: "",
    host: "",
    port: "",
    schema: "",
    username: "",
    password: ""
  });

  const [databaseConnList, setDatabaseConnList] = useState([]);

  const [searchLoading, setSearchLoading] = useState(false);
  const [refreshLoading, setRefreshLoading] = useState(false);
  const [saveLoading, setSaveLoading] = useState(false);
  const [testLoading, setTestLoading] = useState(false);

  const [tableStart, setTableStart] = useState(0);
  const [tableLimit, setTableLimit] = useState(10);
  const [tableTotalCount, setTableTotalCount] = useState(0);
  const [tableLoading, setTableLoading] = useState(false);

  const [action, setAction] = useState(null);

  const [visibleAddDialog, setVisibleAddDialog] = useState(false);

  const [disableDialogAction, setDisableDialogAction] = useState({
    "save": false,
    "cancel": false,
    "test": false
  });

  /** CONSTANTS */

  const [databaseTypes, setDatabaseTypes] = useState([
    { name: "MySQL", value: "MY_SQL" },
    { name: "Oracle", value: "ORACLE" },
    { name: "MongoDB", value: "MONGO_DB" },
  ]);

  useEffect(() => {
    queryDatabaseConnectionList(tableStart, tableLimit, false, false);
  }, []);

  /** API */

  const baseApi = () => {
    return "/v1/api/dbconn";
  }

  const queryDatabaseConnectionList = (start, limit, refresh, search) => {
    setTableLoading(true);
    if (search) setSearchLoading(true);
    if (refresh) setRefreshLoading(true);
    var params = {
      "start": start,
      "limit": limit
    };
    axiosInstance.post(baseApi() + "/list", params)
      .then((res) => {
        setDatabaseConnList(res.data.data);
        setTableStart(start);
        setTableTotalCount(res.data.totalCount);
        setTableLoading(false);
        setSearchLoading(false);
        setRefreshLoading(false);
      })
      .catch((error) => {
        if (error.status === 401) {
          //router.push("/auth/login")
        } else {
          toast.current.show({ severity: 'error', detail: "Error getting database connections", life: 3000 });
          setTableLoading(false);
          setSearchLoading(false);
          setRefreshLoading(false);
        }
      });
  }

  const testDatabaseConnection = () => {
    setTestLoading(true);
    axiosInstance.post(baseApi() + "/test", databaseConn)
      .then((res) => {
        toast.current.show({
          severity: res.data.success ? 'success' : 'error',
          detail: res.data.message, life: 3000
        });
        setTestLoading(false);
      })
      .catch((error) => {
        toast.current.show({ severity: 'error', detail: "Test error", life: 3000 });
        setTestLoading(false);
      });
  }

  const saveDatabaseConnection = () => {
    setSaveLoading(true);
    const endPoint = action === "CREATE" ? "/create" : "/update";
    axiosInstance.post(baseApi() + endPoint, databaseConn)
      .then((res) => {
        toast.current.show({
          severity: res.data.success ? 'success' : 'error',
          detail: res.data.message, life: 3000
        });
        setSaveLoading(false);
        if (res.data.success) {
          if (action === "CREATE") {
            onClickRefresh();
          } else {
            queryDatabaseConnectionList(tableStart, tableLimit, true, false);
          }
          setVisibleAddDialog(false);
        }
      })
      .catch((error) => {
        toast.current.show({ severity: 'error', detail: "Saving error", life: 3000 });
        setSaveLoading(false);
      });
  }

  const getDatabaseConnectionDetails = (id) => {
    //setTestLoading(true);
    const params = new FormData();
    params.append("id", id);
    axiosInstance.post(baseApi() + "/details", params)
      .then((res) => {
        if (res.data.success) {
          setDatabaseConn(res.data.data);
        } else {
          toast.current.show({ severity: 'error', detail: res.data.message, life: 3000 });
        }
        //setTestLoading(false);
      })
      .catch((error) => {
        toast.current.show({ severity: 'error', detail: "Error getting connection details", life: 3000 });
        //setTestLoading(false);
      });
  }

  /** LISTENERS */

  const onPageChange = (e) => {
    setTableStart(e.first);
    setTableLimit(e.rows);
    queryDatabaseConnectionList(e.first, e.rows, false, false);
  }

  const onClickRefresh = () => {
    setTableStart(0);
    queryDatabaseConnectionList(0, tableLimit, true, false);
  }

  const onClickEdit = (id) => {
    setAction("UPDATE");
    getDatabaseConnectionDetails(id);
    setVisibleAddDialog(true);
  }

  /** HELPER */

  const clearDatabaseConn = () => {
    setDatabaseConn({
      id: null,
      type: "",
      name: "",
      host: "",
      port: "",
      schema: "",
      username: "",
      password: ""
    });
  }

  /** LAYOUT */

  const paginatorTemplate = {
    layout: 'PrevPageLink PageLinks NextPageLink RowsPerPageDropdown CurrentPageReport',
    'PrevPageLink': (options) => {
      return (
        <button type="button" className={options.className} onClick={options.onClick} disabled={options.disabled}>
          <span className="p-3">Previous</span>
          <Ripple />
        </button>
      )
    },
    'NextPageLink': (options) => {
      return (
        <button type="button" className={options.className} onClick={options.onClick} disabled={options.disabled}>
          <span className="p-3">Next</span>
          <Ripple />
        </button>
      )
    },
    'PageLinks': (options) => {
      if ((options.view.startPage === options.page && options.view.startPage !== 0) || (options.view.endPage === options.page && options.page + 1 !== options.totalPages)) {
        const className = classNames(options.className, { 'p-disabled': true });

        return <span className={className} style={{ userSelect: 'none' }}>...</span>;
      }

      return (
        <button type="button" className={options.className} onClick={options.onClick}>
          {options.page + 1}
          <Ripple />
        </button>
      )
    },
    'RowsPerPageDropdown': (options) => {
      const dropdownOptions = [
        { label: 10, value: 10 },
        { label: 25, value: 25 },
        { label: 50, value: 50 },
        { label: 100, value: 100 }
      ];

      return <Dropdown value={options.value} options={dropdownOptions} onChange={options.onChange} />;
    },
    'CurrentPageReport': (options) => {
      return (
        <span style={{ color: 'var(--text-color)', userSelect: 'none', width: '120px', textAlign: 'center' }}>
          {options.first} - {options.last} of {options.totalRecords}
        </span>
      )
    }
  };

  const headerTableDatabaseConnList = (
    <div>
      <div className="grid">
        <div className="col-6 md:col-6">
          <div className="flex flex-wrap align-items-center justify-content-start gap-2">
            <Button icon="pi pi-plus" label="Add" size="small" onClick={() => {
              setAction("CREATE");
              clearDatabaseConn();
              setVisibleAddDialog(true);
            }} />
          </div>
        </div>
        <div className="col-6 md:col-6">
          <div className="flex flex-wrap align-items-center justify-content-end gap-2">
            <Button icon="pi pi-refresh" onClick={onClickRefresh} size="small" loading={refreshLoading} text />
          </div>
        </div>
      </div>
    </div>
  );

  const actionTableDatabaseConnList = (rowData) => {
    return <div>
      <Button icon="pi pi-pen-to-square" size="small" rounded text aria-label="Edit" onClick={() => onClickEdit(rowData.id)} />
      <Button icon="pi pi-trash" size="small" rounded text severity="danger" aria-label="Delete" />
    </div>
  };

  const footerAddUpdateDialog = (
    <div className="flex justify-content-between">
      <Button label="Test Connection" onClick={() => testDatabaseConnection()}
        size="small" loading={testLoading} outlined disabled={disableDialogAction.test} />
      <div>
        <Button label="Cancel" onClick={() => setVisibleAddDialog(false)} size="small" text
          disabled={disableDialogAction.cancel} />
        <Button label="Save" loading={saveLoading} size="small" autoFocus
          disabled={disableDialogAction.save} onClick={() => saveDatabaseConnection()} />
      </div>
    </div>
  );

  return (
    <div className="grid">
      <Toast ref={toast} />
      <div className="col-12 md:col-12">
        <DataTable ref={dataTable} dataKey="id" value={databaseConnList} header={headerTableDatabaseConnList}
          tableStyle={{ minWidth: '60rem' }}>
          <Column field="name" header="Name"></Column>
          <Column field="typeName" header="Type"></Column>
          <Column header="Action" body={actionTableDatabaseConnList}></Column>
        </DataTable>
        <Paginator ref={paginator} template={paginatorTemplate} first={tableStart} rows={tableLimit} totalRecords={tableTotalCount}
          onPageChange={(e) => onPageChange(e)} />
      </div>

      <Dialog header={`${action === "CREATE" ? "Add" : "Update"} Connection`} visible={visibleAddDialog}
        onHide={() => setVisibleAddDialog(false)} footer={footerAddUpdateDialog} style={{ width: '50vw' }}>
        <div className="col-12 md:col-12">
          <div className="p-fluid mt-6">
            <div className="grid mt-2">

              <div className="field col-4 md:col-4">
                <FloatLabel>
                  <Dropdown
                    inputId="dbType"
                    value={databaseConn?.type}
                    onChange={(e) => setDatabaseConn({ ...databaseConn, "type": e.value })}
                    options={databaseTypes}
                    optionLabel="name"
                    optionValue="value"
                    placeholder="Select"
                    showClear />
                  <label htmlFor="dbType">Type</label>
                </FloatLabel>
              </div>

              <div className="field col-5 md:col-5">
                <FloatLabel>
                  <InputText
                    id="dbHost"
                    value={databaseConn?.host}
                    onChange={(e) => setDatabaseConn({ ...databaseConn, "host": e.target.value })} />
                  <label htmlFor="dbHost">Host</label>
                </FloatLabel>
              </div>

              <div className="field col-3 md:col-3">
                <FloatLabel>
                  <InputText
                    id="dbPort"
                    value={databaseConn?.port}
                    onChange={(e) => setDatabaseConn({ ...databaseConn, "port": e.target.value })} />
                  <label htmlFor="dbPort">Port</label>
                </FloatLabel>
              </div>

              <div className="field col-6 md:col-6">
                <FloatLabel>
                  <InputText
                    id="dbName"
                    value={databaseConn?.name}
                    onChange={(e) => setDatabaseConn({ ...databaseConn, "name": e.target.value })} />
                  <label htmlFor="dbName">Name</label>
                </FloatLabel>
              </div>

              <div className="field col-6 md:col-6">
                <FloatLabel>
                  <InputText
                    id="dbSchema"
                    value={databaseConn?.schema}
                    onChange={(e) => setDatabaseConn({ ...databaseConn, "schema": e.target.value })} />
                  <label htmlFor="dbSchema">Schema</label>
                </FloatLabel>
              </div>

              <div className="field col-6 md:col-6">
                <FloatLabel>
                  <InputText
                    id="dbUsername"
                    value={databaseConn?.username}
                    onChange={(e) => setDatabaseConn({ ...databaseConn, "username": e.target.value })} />
                  <label htmlFor="dbUsername">Username</label>
                </FloatLabel>
              </div>

              <div className="field col-6 md:col-6">
                <FloatLabel>
                  <Password
                    id="dbPassword" feedback={false}
                    value={databaseConn?.password}
                    onChange={(e) => setDatabaseConn({ ...databaseConn, "password": e.target.value })} />
                  <label htmlFor="dbPassword">Password</label>
                </FloatLabel>
              </div>
            </div>
          </div>
        </div>
      </Dialog>
    </div>
  );


};
export default DatabaseConnection;