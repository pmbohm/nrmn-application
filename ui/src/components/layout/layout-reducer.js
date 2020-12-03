import {
  createSlice
} from "@reduxjs/toolkit";


const toggleState = {
  leftSideMenuIsOpen: false,
  logoutMenuOpen: false,
  rightContextDrawerIsOpen: true // todo start closed
}

const toggleSlice = createSlice({
  name: "toggle",
  initialState: toggleState,
  reducers: {
    toggleLeftSideMenu: (state, action) => {
      state.leftSideMenuIsOpen = !state.leftSideMenuIsOpen;
    },
    toggleLogoutMenuOpen: (state, action) => {
      state.logoutMenuOpen = !state.logoutMenuOpen;
    },
    toggleRightContextDrawerOpen: (state, action) => {
      state.rightContextDrawerIsOpen = (action.payload);
    }
  },
});

export const toggleReducer = toggleSlice.reducer;
export const { toggleLeftSideMenu, toggleLogoutMenuOpen, toggleRightContextDrawerOpen } = toggleSlice.actions;

