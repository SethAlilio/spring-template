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
                { label: 'Participant', icon: 'pi pi-fw pi-users', to: '/pages/participant' },
                { label: 'Prize', icon: 'pi pi-fw pi-gift', to: '/pages/prize' },
                { label: 'Raffle Event', icon: 'pi pi-fw pi-star', to: '/pages/raffleevent' },
                { label: 'Batch Raffle', icon: 'pi pi-fw pi-star', to: '/pages/batchraffle' }
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
