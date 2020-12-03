import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import Slide from "@material-ui/core/Slide";
import store from "../store";
import {toggleRightContextDrawerOpen} from "./layout-reducer";
import IconButton from "@material-ui/core/IconButton";
import KeyboardArrowRightIcon from '@material-ui/icons/KeyboardArrowRight';


const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: '#eee',
    height: '100%',
    padding: theme.spacing(2),
  },
  drawerContainer: {
    padding: theme.spacing(3),
    overflow: 'auto',
  },
  content: {
  },
}));

const handleCloseThis = () => {
  store.dispatch(toggleRightContextDrawerOpen(false));
};

export default function RightContextDrawer(props) {
  const classes = useStyles();

  return (
      <Slide direction="left" in={props.open} mountOnEnter unmountOnExit>
      <div className={classes.root}>
        <IconButton aria-label="delete" onClick={handleCloseThis}  size="large">
          <KeyboardArrowRightIcon fontSize="inherit" />
        </IconButton>
        <Divider />
        <Typography variant={'h5'}>props.title goes here</Typography>

          <div className={classes.drawerContainer}>
            <Typography paragraph> content to replace this</Typography>
            {props.content}
          </div>
      </div>
     </Slide>
  );
}