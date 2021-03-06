import React from 'react';
import {assert} from 'chai';
import {unmountComponentAtNode} from 'react-dom';
import {render} from '../../setupTests';
import {act} from '@testing-library/react';
import Login from './login';

let container;

jest.mock('react-router-dom', () => {
  const ActualReactRedux = require.requireActual('react-router-dom');
  return {
    ...ActualReactRedux,
    useLocation: jest.fn().mockImplementation(() => {
      return function () {
        return {};
      };
    })
  };
});

beforeEach(() => {
  container = document.createElement('div');
  document.body.appendChild(container);
});

afterEach(() => {
  unmountComponentAtNode(container);
  container.remove();
  container = null;
});

describe('login test', () => {
  it('renders form', () => {
    act(() => {
      render(<Login />, container);
    });

    const inputs = document.getElementsByTagName('input');
    assert.lengthOf(inputs, 2, 'Should be two input fields');
  });
});
