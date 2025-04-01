/* eslint-disable @next/next/no-img-element */

import React, { useContext } from 'react';
import AppMenuitem from './AppMenuitem';
import { LayoutContext } from './context/layoutcontext';
import { MenuProvider } from './context/menucontext';
import Link from 'next/link';
import { AppMenuItem } from '@/types';

const CustomAppMenu = () => {
    const { layoutConfig } = useContext(LayoutContext);

    const model: AppMenuItem[] = [
        {
            label: 'Home',
            items: [{ label: 'Dashboard', icon: 'pi pi-fw pi-home', to: '/' }]
        },
        {
            label: 'Modules',
            items: [
                { label: 'Database Connection', icon: 'pi pi-fw pi-database', to: '/pages/databaseconn' },
                { label: 'Database Backup', icon: 'pi pi-fw pi-cloud-download', to: '/pages/databasebackup' },
                { label: 'Input', icon: 'pi pi-fw pi-check-square', to: '/uikit/input' },
                { label: 'Panel', icon: 'pi pi-fw pi-check-square', to: '/uikit/panel' },
                { label: 'Menu', icon: 'pi pi-fw pi-check-square', to: '/uikit/menu' },
            ]
        }
    ];

    return (
        <MenuProvider>
            <ul className="layout-menu">
                {model.map((item, i) => {
                    return !item?.seperator ? <AppMenuitem item={item} root={true} index={i} key={item.label} /> : <li className="menu-separator"></li>;
                })}
            </ul>
        </MenuProvider>
    );
};

export default CustomAppMenu;
