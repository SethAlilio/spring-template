'use client';
import React, { useState, useRef, useEffect } from 'react';
import { useRouter } from 'next/navigation';

import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Divider } from 'primereact/divider';
import { Dropdown } from 'primereact/dropdown';
import { FloatLabel } from 'primereact/floatlabel';
import { RadioButton } from 'primereact/radiobutton';
import { Steps } from 'primereact/steps';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

import useAxiosInstance from '@/util/CustomAxios'
import useAuthStore from '@/store/AuthStore';
import { Panel } from 'primereact/panel';

const DatabaseBackup = () => {
  const dataTable = useRef(null);
  const paginator = useRef(null);
  const toast = useRef(null);
  const stepperRef = useRef(null);

  const [radioValue, setRadioValue] = useState(null);
  const axiosInstance = useAxiosInstance();
  const { user } = useAuthStore();
  const router = useRouter();

  const [stepsActiveIndex, setStepsActiveIndex] = useState(0);
  const stepsItems = [
    { label: 'Database', command: () => { } },
    { label: 'Configuration', command: () => { } },
    { label: 'Process', command: () => { } }
  ];

  const [databaseSelection, setDatabaseSelection] = useState([]);

  const [backupTypeSelection, setBackupTypeSelection] = useState([
    { name: "Structure Only", value: "STRUCTURE_ONLY" },
    { name: "Data Only", value: "DATA_ONLY" },
    { name: "Structure and Data", value: "STRUCTURE_AND_DATA" }
  ]);

  const scheduleSelection = [{ label: "Now", value: "NOW" }, { label: "Custom", value: "CUSTOM" }];

  const [advanceSettings, setAdvanceSettings] = useState([]);

  const [backupConfig, setBackupConfig] = useState({
    "sourceDatabase": { id: null, name: "" },
    "destinationDatabase": { id: null, name: "" },
    "type": null,
    "backupTableList": [],
    "schedule": null,
    "scheduleTime": null,
    "advanceSettingStructure": {key: null, type: null, desc: null},
    "advanceSettingData": {key: null, type: null, desc: null}
  });

  const [sourceDatabaseTables, setSourceDatabaseTables] = useState([]);

  const [backupStatusList, setBackupStatusList] = useState([]);

  const [testConnectionLoading, setTestConnectionLoading] = useState({
    source: false,
    destination: false
  });

  const [testConnectionStatus, setTestConnectionStatus] = useState({
    source: "init",
    destination: "init"
  });

  useEffect(() => {
    queryAllDatabaseConnectionList();
    queryAdvanceSettings();
  }, []);

  /** API */


  const baseApi = () => {
    return "/v1/api/dbconn";
  }

  const queryAllDatabaseConnectionList = () => {
    axiosInstance.post(baseApi() + "/listAll")
      .then((res) => {
        setDatabaseSelection(res.data.data);
      })
      .catch((error) => {
        if (error.status === 401) {
          //router.push("/auth/login")
        } else {
          toast.current.show({ severity: 'error', detail: "Error getting database connections", life: 3000 });
        }
      });
  }

  const testDatabaseConnection = (direction) => {
    setTestConnectionLoading({ ...testConnectionLoading, [direction]: true });
    const params = new FormData();
    const id = direction === "source" ? backupConfig.sourceDatabase.id : backupConfig.destinationDatabase.id;
    params.append("id", id.toString());
    setTestConnectionStatus({...testConnectionStatus, [direction]: "init"});
    axiosInstance.post(baseApi() + "/testId", params)
      .then((res) => {
        /* toast.current.show({
          severity: res.data.success ? 'success' : 'error',
          detail: res.data.message, life: 3000
        }); */
        setTestConnectionLoading({ ...testConnectionLoading, [direction]: false });
        setTestConnectionStatus({ ...testConnectionStatus, [direction]: res.data.success ? "success" : "failed" });
      })
      .catch((error) => {
        setTestConnectionLoading({ ...testConnectionLoading, [direction]: false });
        setTestConnectionStatus({ ...testConnectionStatus, [direction]: "failed" });
      });
  }

  const querySourcDbTables = () => {
    const params = new FormData();
    params.append("id", backupConfig?.sourceDatabase?.id?.toString());
    axiosInstance.post(baseApi() + "/tables", params)
      .then((res) => {
        const tables = formatDbTables(res.data.data);
        console.log(`tables: ${JSON.stringify(tables)}`);
        setSourceDatabaseTables(tables);
      })
      .catch((error) => {
      });
  }

  const queryAdvanceSettings = () => {
    axiosInstance.post(baseApi() + "/advanceSettings")
      .then((res) => {
        setAdvanceSettings(res.data.data);
      })
      .catch((error) => {
        if (error.status === 401) {
          //router.push("/auth/login")
          setAdvanceSettings
        } else {
          toast.current.show({ severity: 'error', detail: "Error getting advance settings", life: 3000 });
        }
      });
  }

  const formatDbTables = (tables) => {
    console.log(`formatDbTables: ${JSON.stringify(tables)}`);
    let id = 0;
    return tables.map(x => {
      id++;
      return ({ id: id, name: x});
    });
  }

  /** LISTENERS */

  const onChangeBackupTables = (event) => {
    console.log(`onChangeBackupTables | source: ${JSON.stringify(event.source)}`);
    setSourceDatabaseTables(event.source);
    setBackupConfig({ ...backupConfig, backupTableList: event.target });
  };

  const onClickNextStep = (currentStep) => {
    switch(currentStep) {
      case 1:
        if (!backupConfig.sourceDatabase.id) {
          toast.current.show({ severity: 'error', detail: "Please select source database", life: 3000 });
          return;
        }
        if (!backupConfig.destinationDatabase.id) {
          toast.current.show({ severity: 'error', detail: "Please select destination database", life: 3000 });
          return;
        }
        if (testConnectionStatus.source === "init") {
          toast.current.show({ severity: 'warning', detail: "Please test source database connection", life: 3000 });
          return;
        }
        if (testConnectionStatus.destination === "init") {
          toast.current.show({ severity: 'warning', detail: "Please test destination database connection", life: 3000 });
          return;
        }
        if (testConnectionStatus.source === "failed") {
          toast.current.show({ severity: 'error', detail: "Source database connection failed", life: 3000 });
          return;
        }
        if (testConnectionStatus.destination === "failed") {
          toast.current.show({ severity: 'error', detail: "Destination database connection failed", life: 3000 });
          return;
        }
        querySourcDbTables();
        setStepsActiveIndex(1);
        break;
      case 2:
        if (!backupConfig.type || backupConfig.type === "") {
          toast.current.show({ severity: 'error', detail: "Please select export type", life: 3000 });
          return;
        }
        if (backupConfig.backupTableList.length === 0 ) {
          toast.current.show({ severity: 'error', detail: "Select tables to backup", life: 3000 });
          return;
        }
        console.log(`backupConfig: ${JSON.stringify(backupConfig)}`);
        setStepsActiveIndex(2);
        setBackupStatusList(formatDbTablesToProcessList);
        break;
    }

  }

  const formatDbTablesToProcessList = () => {
    let id = 0;
    return backupConfig.backupTableList.map(x => {
      id++;
      return ({ table: x.name, status: "NOT STARTED", "action":""});
    }); 
  }

  const onClickStartBackup = () => {
    console.log(`onClickStartBackup | backupConfig: ${JSON.stringify(backupConfig)}`);
    if (!backupConfig.schedule || backupConfig.schedule === "") {
      toast.current.show({ severity: 'error', detail: "Please select schedule", life: 3000 });
      return;
    }
    if (backupConfig.schedule === "CUSTOM" && !backupConfig.scheduleTime) {
      toast.current.show({ severity: 'error', detail: "Please select schedule time", life: 3000 });
      return;
    }
  }

  /** LAYOUT */

  const itemTemplate = (item) => {
    return <span>{item}</span>
  }

  const statusBodyTemplate = (item) => {
    return <Tag value={item.status} severity={getSeverity(item)}></Tag>;
  };

  const getSeverity = (item) => {
    switch (item.status) {
      case 'DONE':
        return 'success';

      case 'ON GOING':
        return 'info';

      case 'ERROR':
        return 'danger';

      default:
        return null;
    }
  };

  return (
    <div className="grid">
      <Toast ref={toast} />
      <div className="col-12 md:col-12">
        <Steps model={stepsItems} activeIndex={stepsActiveIndex} onSelect={(e) => { /* setStepsActiveIndex(e.index); */  }} readOnly={false} />
        {/* Step 1: Database */}
        {
          stepsActiveIndex === 0 ?
            <div className="p-fluid mt-6">
              <div className="grid mt-2">
                <div className="field col-8 md:col-8">
                  <FloatLabel>
                    <Dropdown
                      inputId="sourceDb"
                      value={backupConfig?.sourceDatabase}
                      onChange={(e) => {
                        setBackupConfig({ ...backupConfig, "sourceDatabase": e.value });
                        setTestConnectionStatus({ ...testConnectionStatus, "source": "init" });
                      }}
                      options={databaseSelection}
                      optionLabel="name"
                      placeholder="Select"
                      showClear />
                    <label htmlFor="sourceDb">Source Database</label>
                  </FloatLabel>
                  {
                    testConnectionStatus.source !== "init" ?
                      <small id="username-help" className="mt-4" style={{ color: `${testConnectionStatus.source === "success" ? "green" : "red"}` }}>
                        <i className={`pi ${testConnectionStatus.source === "success" ? "pi-check-circle" : "pi-times-circle"}`}
                          style={{ fontSize: '.65rem' }} ></i> {`Connection ${testConnectionStatus.source}`}
                      </small> : null
                  }
                </div>
                <div className="field col-4 md:col-4">
                  <Button label="Test Connection" loading={testConnectionLoading.source} onClick={() => testDatabaseConnection("source")} />
                </div>
                <div className="field col-12 md:col-12">
                  <Divider align="center">
                    <div className="inline-flex align-items-center">
                      <i className="pi pi-arrow-down "></i>
                      {/* <b>Text</b> */}
                    </div>
                  </Divider>
                </div>
                <div className="field col-8 md:col-8">
                  <FloatLabel>
                    <Dropdown
                      inputId="destDb"
                      value={backupConfig?.destinationDatabase}
                      onChange={(e) => {
                        setBackupConfig({ ...backupConfig, "destinationDatabase": e.value });
                        setTestConnectionStatus({ ...testConnectionStatus, "destination": "init" });
                      }}
                      options={databaseSelection}
                      optionLabel="name"
                      placeholder="Select"
                      showClear />
                    <label htmlFor="destDb">Destination Database</label>
                  </FloatLabel>
                  {
                    testConnectionStatus.destination !== "init" ?
                      <small id="username-help" className="mt-4" style={{ color: `${testConnectionStatus.destination === "success" ? "green" : "red"}` }}>
                        <i className={`pi ${testConnectionStatus.destination === "success" ? "pi-check-circle" : "pi-times-circle"}`}
                          style={{ fontSize: '.65rem' }} ></i> {`Connection ${testConnectionStatus.destination}`}
                      </small> : null
                  }
                </div>
                <div className="field col-4 md:col-4">
                  <Button label="Test Connection" loading={testConnectionLoading.destination} onClick={() => testDatabaseConnection("destination")} />
                </div>
                <div id="next-container" className="flex justify-content-end w-full">
                  <div className="flex justify-content-end">
                      <Button label="Next" onClick={() => onClickNextStep(1)} icon="pi pi-angle-right" iconPos="right" />
                  </div>
                </div>
              </div>
            </div> : null
        }

        {/* Step 2: Configuration */}
        {
          stepsActiveIndex === 1 ?
            <div className="p-fluid mt-6">
              <div className="grid mt-2">
                <div className="field col-6 md:col-6">
                  <FloatLabel>
                    <Dropdown
                      inputId="type"
                      value={backupConfig?.type}
                      onChange={(e) => setBackupConfig({ ...backupConfig, "type": e.value })}
                      options={backupTypeSelection}
                      optionLabel="name"
                      optionValue="value"
                      placeholder="Select"
                      showClear />
                    <label htmlFor="type">Export Type</label>
                  </FloatLabel>
                </div>
                <div className="field col-12 md:col-12">
                  {/* <PickList dataKey="id" source={sourceDatabaseTables} target={backupConfig?.backupTableList}
                    onChange={onChangeBackupTables} itemTemplate={itemTemplate} filter filterBy="name" breakpoint="1280px"
                    sourceHeader={`Source [${backupConfig.sourceDatabase?.name}]`} targetHeader="For Backup" sourceStyle={{ height: '24rem' }} targetStyle={{ height: '24rem' }}
                    sourceFilterPlaceholder="Search by name" targetFilterPlaceholder="Search by name" /> */}

                  <DataTable value={sourceDatabaseTables} selection={backupConfig?.backupTableList} 
                    selectionMode={"multiple"} 
                    onSelectionChange={(e) => setBackupConfig({ ...backupConfig, backupTableList: e.value })}
                    dataKey="id" tableStyle={{ minWidth: '50rem' }}>
                      {/* <Column selectionMode="multiple" headerStyle={{ width: '3rem' }}></Column> */}
                      <Column field="name" header="Name"></Column>
                  </DataTable>


                </div>

                <div className="field col-12 md:col-12">
                <Panel header="Advance Settings" toggleable>
                  <div className="grid">
                    <div className="grid">
                      <small>Structure</small>
                      <div className="col-12 md:col-12">
                      {advanceSettings.filter((settings) => settings.type === "STRUCTURE_ONLY").map((settings) => {
                          return (
                            
                              <div className="field-radiobutton">
                                  <RadioButton inputId={settings.key} name={settings.key} value={settings} onChange={(e) => setBackupConfig({...backupConfig, advanceSettingStructure: e.value})} 
                                    checked={backupConfig?.advanceSettingStructure.key === settings.key} />
                                  <label htmlFor={settings.key} className="ml-2">{settings.desc}</label>
                              </div>
                          );
                      })}
                            </div>
                    </div>
                    <div className="mt-4">
                      <small>Data</small>
                      {advanceSettings.filter((settings) => settings.type === "DATA_ONLY").map((settings) => {
                          return (
                              <div key={settings.key} >
                                  <RadioButton inputId={settings.key} name="settings" value={settings} onChange={(e) => setBackupConfig({...backupConfig, advanceSettingStructure: e.value})} 
                                    checked={backupConfig?.advanceSettingStructure.key === settings.key} />
                                  <label htmlFor={settings.key} className="ml-2">{settings.desc}</label>
                              </div>
                          );
                      })}
                    </div>
                  </div>
                </Panel>
                </div>

                <div className="field col-6 md:col-6">
                  <div id="next-container" className="flex justify-content-start w-full">
                    <div className="flex justify-between">
                      <Button label="Previous" onClick={() => setStepsActiveIndex(0)} icon="pi pi-angle-left" />
                    </div>
                  </div>
                </div>
                <div className="field col-6 md:col-6">
                  <div id="next-container" className="flex justify-content-end w-full">
                    <div className="flex justify-content-end">
                      <Button label="Next" onClick={() => onClickNextStep(2)} icon="pi pi-angle-right" iconPos="right" />
                    </div>
                  </div>
                </div>
              </div>
            </div> : null
        }

        {/* Step 3: Process */}
        {
          stepsActiveIndex === 2 ?
            <div className="p-fluid mt-6">
              <div className="grid mt-2">
                <div className="field col-6 md:col-6">
                  <FloatLabel>
                    <Dropdown
                      inputId="sourceDb"
                      value={backupConfig?.schedule}
                      onChange={(e) => setBackupConfig({ ...backupConfig, "schedule": e.value })}
                      options={scheduleSelection}
                      optionLabel="label"
                      optionValue="value"
                      placeholder="Select"
                      showClear />
                    <label htmlFor="sourceDb">Schedule</label>
                  </FloatLabel>
                </div>
                <div className="field col-4 md:col-4">
                  {
                    backupConfig?.schedule === "CUSTOM" ?
                    <FloatLabel>
                      <Calendar
                        value={backupConfig.scheduleTime} showButtonBar showTime hourFormat="24"
                        onChange={(e) => setBackupConfig({ ...backupConfig, scheduleTime: e.value ?? null })} />
                      <label htmlFor="sourceDb">Time</label>
                    </FloatLabel> : null
                  }
                </div>
                {/* {
                  backupConfig?.schedule === "CUSTOM" ?
                    <div className="field col-6 md:col-6">
                      <FloatLabel>
                        <Calendar
                          value={backupConfig.scheduleTime} showButtonBar showTime hourFormat="24"
                          onChange={(e) => setBackupConfig({ ...backupConfig, scheduleTime: e.value ?? null })} />
                        <label htmlFor="sourceDb">Time</label>
                      </FloatLabel>
                    </div> : null
                } */}
                <Divider align="left">
                  <div className="inline-flex align-items-center">
                    <i className="pi pi-check-square mr-2"></i>
                    <b>Summary</b>
                  </div>
                </Divider>

                <div className="field col-12 md:col-12">
                  <DataTable value={backupStatusList} tableStyle={{ minWidth: '60rem' }}>
                    <Column field="table" header="Table"></Column>
                    <Column field="action" header="Action"></Column>
                    <Column header="Status" body={statusBodyTemplate}></Column>
                  </DataTable>
                </div>

                <div className="field col-6 md:col-6">
                  <div id="next-container" className="flex justify-content-start w-full">
                    <div className="flex justify-between">
                      <Button label="Previous" onClick={() => setStepsActiveIndex(1)} icon="pi pi-angle-left" />
                    </div>
                  </div>
                </div>
                <div className="field col-6 md:col-6">
                  <div id="next-container" className="flex justify-content-end w-full">
                    <div className="flex justify-content-end">
                      <Button label="Start" onClick={() => onClickStartBackup()} />
                    </div>
                  </div>
                </div>


              </div>
            </div>
            : null
        }
      </div>
    </div>
  );
};

export default DatabaseBackup;
