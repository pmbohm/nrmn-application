import React from 'react';
import Box from '@material-ui/core/Box';
import Alert from '@material-ui/lab/Alert';
import Grid from '@material-ui/core/Grid';
import Form from '@rjsf/material-ui';
import makeStyles from '@material-ui/core/styles/makeStyles';
import CircularProgress from '@material-ui/core/CircularProgress';
import Button from '@material-ui/core/Button';
import {NavLink} from 'react-router-dom';
import green from '@material-ui/core/colors/green';
import {PropTypes} from 'prop-types';

const useStyles = makeStyles((theme) => ({
  root: {
    '& > * + *': {
      marginTop: theme.spacing(2)
    }
  },
  rootFlex: {
    display: 'flex',
    alignItems: 'center'
  },
  button: {
    marginRight: '10px',
    marginBottom: '5px'
  },
  wrapper: {
    marginTop: theme.spacing(3),
    position: 'relative'
  },
  buttonSuccess: {
    backgroundColor: green[500],
    '&:hover': {
      backgroundColor: green[700]
    }
  },
  buttonProgress: {
    position: 'absolute',
    top: '50%',
    left: '50%',
    marginTop: -12,
    marginLeft: -12
  }
}));

const BaseForm = (params) => {
  const classes = useStyles();

  const loading = params.loading;

  function submitForm(formData) {
    params.onSubmit(formData);
  }

  function getErrors(errors) {
    return errors.map((item, key) => {
      return <div key={key}>{item}</div>;
    });
  }

  let errorAlert =
    params.errors && params.errors.length > 0 ? (
      <Alert severity="error" variant="filled">
        {getErrors(params.errors)}
      </Alert>
    ) : (
      ''
    );

  return (
    <>
      <Grid container spacing={0} alignItems="center" justify="center" style={{minHeight: '70vh'}}>
        <Box pt={4} px={6} pb={6} className={classes.root}>
          {errorAlert}
          <Form
            onError={params.onError}
            schema={params.schema}
            uiSchema={params.uiSchema}
            onSubmit={submitForm}
            gutterBottom={true}
            showErrorList={true}
            fields={params.fields}
            formData={params.formData}
          >
            <div className={classes.rootFlex}>
              <div className={classes.wrapper}>
                {params.submitButton ? (
                  params.submitButton
                ) : (
                  <>
                    <Button type="submit" className={classes.button} variant="contained" color="secondary" disabled={loading}>
                      {params.submitLabel ?? 'Save'}
                    </Button>
                    {!params.hideCancel && params.onCancel ? (
                      <Button component={NavLink} className={classes.button} variant="contained" to={params.onCancel}>
                        Cancel
                      </Button>
                    ) : null}
                  </>
                )}
                {loading && <CircularProgress size={20} className={classes.buttonProgress} />}
              </div>
            </div>
          </Form>
        </Box>
      </Grid>
    </>
  );
};

BaseForm.propTypes = {
  submitLabel: PropTypes.string
};

export default BaseForm;
