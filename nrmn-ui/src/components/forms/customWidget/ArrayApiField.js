import React, { useState } from "react";

import { useDispatch, useSelector } from "react-redux";
import { useEffect } from 'react';
import { selectRequested } from "../redux-form";
import Autocomplete from '@material-ui/lab/Autocomplete';
import TextField from '@material-ui/core/TextField';
import pluralize from 'pluralize';



const handleMultiChanges = (event, props) => {
  
  console.log(event);
}


const ArrayApiField = (props) => {

  const items = useSelector(state => state.form.entities)
  const dispatch = useDispatch();
  const entity = props.schema.items.$ref.split("/").pop() ;
  const pluralEntity = pluralize(entity);
  const entities =  pluralEntity.charAt(0).toLowerCase() + pluralEntity.slice(1)

  useEffect(() => {
    console.log("entity:", entities);
    if (entities != undefined)
      dispatch(selectRequested(entities))
  }, [])

  return (items) ? (
     <Autocomplete
     multiple
      id={"select-auto-"+ entity}
      options={items.map(it => it.name)}
      getOptionLabel={(option) => option}
      defaultValue={[]}
      onChange={(event) => handleMultiChanges(event, props)}
      renderInput={(params) => <TextField {...params} label={"enter " + props.name} variant="outlined" />}
    />) : (<></>)
}

export default ArrayApiField;
