// API Configuration
export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:3001/api';
export const API_TIMEOUT = 30000;

// HTTP Methods
export const HTTP_METHODS = {
  GET: 'GET',
  POST: 'POST',
  PUT: 'PUT',
  DELETE: 'DELETE',
  PATCH: 'PATCH',
};

// Status Codes
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500,
};

// Application States
export const APP_STATES = {
  LOADING: 'loading',
  SUCCESS: 'success',
  ERROR: 'error',
  IDLE: 'idle',
};

// Pagination
export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_LIMIT: 10,
  MAX_LIMIT: 100,
};

// Local Storage Keys
export const STORAGE_KEYS = {
  USER: 'user',
  TOKEN: 'authToken',
  THEME: 'theme',
  LANGUAGE: 'language',
};

// Validation Rules
export const VALIDATION = {
  MIN_PASSWORD_LENGTH: 8,
  MAX_USERNAME_LENGTH: 50,
  EMAIL_REGEX: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
};

// Error Messages
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network error. Please try again.',
  UNAUTHORIZED: 'Unauthorized. Please log in.',
  FORBIDDEN: 'Access denied.',
  NOT_FOUND: 'Resource not found.',
  SERVER_ERROR: 'Server error. Please try again later.',
  INVALID_INPUT: 'Invalid input provided.',
};

// Success Messages
export const SUCCESS_MESSAGES = {
  CREATED: 'Created successfully.',
  UPDATED: 'Updated successfully.',
  DELETED: 'Deleted successfully.',
  SAVED: 'Saved successfully.',
};

// Date/Time Formats
export const DATE_FORMATS = {
  SHORT: 'MM/DD/YYYY',
  LONG: 'MMMM DD, YYYY',
  TIME: 'HH:mm:ss',
  ISO: 'YYYY-MM-DDTHH:mm:ss.SSSZ',
};

// Theme
export const THEMES = {
  LIGHT: 'light',
  DARK: 'dark',
};

// Languages
export const LANGUAGES = {
  EN: 'en',
  ES: 'es',
  FR: 'fr',
};


