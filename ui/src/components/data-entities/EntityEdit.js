import React, {useEffect} from 'react';
import {useDispatch, useSelector} from 'react-redux';
import {useParams, NavLink, Redirect} from 'react-router-dom';
import config from 'react-global-configuration';
import Form from '@rjsf/material-ui';
import {Box, Button, CircularProgress, Grid, Typography} from '@material-ui/core';
import {Save} from '@material-ui/icons';
import Alert from '@material-ui/lab/Alert';
import PropTypes from 'prop-types';

import {createEntityRequested, itemRequested, updateEntityRequested} from './middleware/entities';

import LoadingBanner from '../layout/loadingBanner';
import EntityContainer from '../containers/EntityContainer';

import DropDownInput from './customWidgetFields/DropDownInput';
import TextInput from './customWidgetFields/TextInput';
import NumberInput from './customWidgetFields/NumberInput';
import CheckboxInput from './customWidgetFields/CheckboxInput';
import AutoCompleteInput from './customWidgetFields/AutoCompleteInput';
import SearchInput from './customWidgetFields/SearchInput';

import SpeciesSearch from '../search/SpeciesSearch';

const EntityEdit = ({entity, template, clone}) => {
  const params = useParams();

  const schemaDefinition = config.get('api') || {};
  const formData = useSelector((state) => state.form.data);
  const saved = useSelector((state) => state.form.saved);
  const errors = useSelector((state) => state.form.errors);
  const dispatch = useDispatch();

  const edit = !clone && typeof params.id !== 'undefined';

  useEffect(() => {
    if (params.id !== undefined) {
      dispatch(itemRequested(`${entity.endpoint}/${params.id}`));
    }
  }, [saved]);

  const handleSubmit = (e) => {
    if (edit) {
      const data = {path: `${entity.endpoint}/${params.id}`, data: e.formData};
      dispatch(updateEntityRequested(data));
    } else {
      const data = {path: entity.endpoint, data: e.formData};
      dispatch(createEntityRequested(data));
    }
  };

  const title = (edit === true ? 'Edit ' : clone === true ? 'Clone ' : 'New ') + entity.name;
  const entityDef = {...schemaDefinition[edit ? entity.schemaKey.edit : entity.schemaKey.add]};
  const entitySchema = {title: title, ...entityDef};
  const JSSchema = {components: {schemas: schemaDefinition}, ...entitySchema};

  const uiSchema = {};
  for (const key in entitySchema.properties) {
    const item = entitySchema.properties[key];
    // HACK: to just get this working
    if (key === 'mpa') {
      uiSchema[key] = {'ui:field': 'autostring', route: 'marineProtectedAreas'};
    } else if (entity.name === 'Site' && key === 'protectionStatus') {
      uiSchema[key] = {'ui:field': 'autostring', route: 'protectionStatuses'};
    } else if (key === 'obsItemTypeId') {
      uiSchema[key] = {
        'ui:field': 'dropdown',
        default: 'Species',
        route: 'obsItemTypes',
        entity: 'obsItemTypeId',
        entityList: 'obsItemTypes',
        idKey: 'obsItemTypeId',
        valueKey: 'obsItemTypeName'
      };
    } else if (key === 'locationId') {
      uiSchema[key] = {
        'ui:field': 'dropdown',
        route: 'locations?projection=selection',
        entity: 'location',
        entityList: 'locations',
        idKey: 'locationId',
        valueKey: 'locationName'
      };
    } else if (key === 'reportGroup') {
      uiSchema[key] = {
        'ui:field': 'autostring',
        route: 'reportGroups'
      };
    } else if (key === 'habitatGroups') {
      uiSchema[key] = {
        'ui:field': 'autostring',
        route: 'habitatGroups'
      };
      // HACK: just to get these fields working on the Edit page
    } else if (key === 'relief' || key === 'slope' || key === 'waveExposure' || key === 'currents') {
      uiSchema[key] = {
        'ui:field': 'dropdown',
        entity: key,
        optional: true,
        values: [
          {id: 1, label: '1'},
          {id: 2, label: '2'},
          {id: 3, label: '3'},
          {id: 4, label: '4'}
        ]
      };
    } else if (key === 'insideMarinePark') {
      uiSchema[key] = {
        'ui:field': 'dropdown',
        entity: key,
        optional: true,
        values: [
          {id: 'Yes', label: 'Yes'},
          {id: 'No', label: 'No'},
          {id: 'Unsure', label: 'Unsure'}
        ]
      };
    } else if (key === 'supersededBy') {
      uiSchema[key] = {'ui:field': 'searchInput', exclude: 'observableItemName'};
    } else if (item.format === 'double') {
      uiSchema[key] = {'ui:field': 'double'};
    } else if (item.type === 'boolean') {
      uiSchema[key] = {'ui:field': 'boolean'};
    } else if (key === 'oldSiteCodes') {
      uiSchema[key] = {'ui:field': 'array'};
    } else {
      uiSchema[key] = {'ui:field': 'string', 'ui:readonly': item.readOnly ?? false};
    }
  }

  const fields = {
    dropdown: DropDownInput,
    string: TextInput,
    double: NumberInput,
    boolean: CheckboxInput,
    autostring: AutoCompleteInput,
    searchInput: SearchInput
  };

  if (saved) {
    const id = saved[entity.idKey];
    return <Redirect to={`${entity.route.base}/${id}/${edit ? 'saved' : 'new'}`} />;
  }

  return params.id && Object.keys(formData).length === 0 ? (
    <Grid container direction="row" justify="flex-start" alignItems="center">
      <LoadingBanner variant={'h5'} msg={`Loading ${entity.name}`} />
    </Grid>
  ) : (
    <EntityContainer name={entity.list.name} goBackTo={entity.list.route} header={entity.showSpeciesSearch && !edit && <SpeciesSearch />}>
      <Grid container alignItems="flex-start" direction="row">
        <Grid item xs={10}>
          <Box fontWeight="fontWeightBold">
            <Typography variant="h4">{title}</Typography>
          </Box>
        </Grid>
      </Grid>
      <Grid container direction="column" justify="flex-start" alignItems="center">
        {params.loading ? (
          <CircularProgress size={20} />
        ) : (
          <Box pt={2} pb={6} padding={2} width="90%">
            {errors.length > 0 ? (
              <Box py={2}>
                <Alert severity="error" variant="filled">
                  Please review this form for errors and try again.
                </Alert>
              </Box>
            ) : null}
            <Form
              onError={params.onError}
              errors={errors}
              schema={JSSchema}
              uiSchema={uiSchema}
              onSubmit={handleSubmit}
              showErrorList={false}
              fields={fields}
              noValidate
              formData={formData}
              ObjectFieldTemplate={template}
            >
              <Box display="flex" justifyContent="center" mt={5}>
                <Button variant="contained" disabled={params.loading} component={NavLink} to={entity.list.route}>
                  Cancel
                </Button>
                <Button
                  style={{width: '50%', marginLeft: '5%', marginRight: '20%'}}
                  type="submit"
                  variant="contained"
                  color="secondary"
                  startIcon={<Save></Save>}
                  disabled={params.loading}
                >
                  Save {entity.name}
                </Button>
              </Box>
            </Form>
          </Box>
        )}
      </Grid>
    </EntityContainer>
  );
};

EntityEdit.propTypes = {
  entity: PropTypes.object,
  template: PropTypes.func,
  clone: PropTypes.bool
};

export default EntityEdit;
