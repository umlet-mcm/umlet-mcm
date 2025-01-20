const NODE_ENV = import.meta.env.VITE_NODE_ENV;
const IS_DEV = NODE_ENV === 'development';
const API_PORT = import.meta.env.VITE_API_PORT;

console.log('VITE_NODE_ENV:', NODE_ENV);

if (!NODE_ENV || !API_PORT) {
    throw new Error('Environment variables VITE_NODE_ENV and VITE_API_PORT are required.');
}

export const AppConfig = {
    projectName: "UMLetino MCM",
    version: "v1.0.0",
    // When the server is started through electron app (not in dev mode), 
    // base url needs to be specified explicitly (otherwise it uses file:// as a base url). In other cases, it is empty.
    apiBaseUrl: IS_DEV ? '' : `http://localhost:${API_PORT}`,
}