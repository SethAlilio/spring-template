import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useAuthStore = create(persist((set) => ({
    user: null, // Initial state for userd
    auth: null,
    login: (userData) => {set({ user: userData }); set({ auth: userData.auth })}, // Method to log in
    logout: () => {set({ user: null }); set({ auth: null });}, // Method to log out
    update: (userData) => {set({ user: userData });}
  }), 
  {
    name: 'fhrf-auth-storage', // Name of the storage key
    getStorage: () => localStorage, // Use localStorage for persistence
  }));

export default useAuthStore;