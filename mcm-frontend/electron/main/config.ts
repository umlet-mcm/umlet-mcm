import { dirname, join } from 'path';
import { app, dialog } from 'electron';
import log from 'electron-log/main';
import dotenv from 'dotenv';

// Resolve file paths
const DIR_NAME = dirname(__filename);

// Environment management
const NODE_ENV = process.env.NODE_ENV || 'production';
const IS_DEV = NODE_ENV === 'development';

log.info('Running in', NODE_ENV, 'mode');

// Load .env file based on the environment
const ENV_PATH = app.isPackaged
    ? join(process.resourcesPath, '.env')
    : join(DIR_NAME, './../../.env');

dotenv.config({ path: ENV_PATH });

// Validate essential environment var
const VITE_PORT = process.env.VITE_PORT;
const VITE_API_PORT = process.env.VITE_API_PORT;

if (!VITE_PORT || !VITE_API_PORT) {
    dialog.showErrorBox(
        'Configuration Error',
        'The application cannot start: VITE_PORT and VITE_API_PORT environment variables are required. Set these variables in a .env file under mcm-frontend/ and rebuild the Electron app.'
    );
    app.exit(1);
}

// Manage paths
const BACKEND_DIR = app.isPackaged
    ? join(DIR_NAME, '../../../')
    : join(DIR_NAME, '../../../mcm-backend/mcm-server/build/libs');

const JAR_PATH = join(BACKEND_DIR, 'mcm-server.jar');
const SPLASH_PATH = join(DIR_NAME, '../../dist-electron/resources/splash.html');
const PRELOAD_PATH = join(DIR_NAME, '../../dist-electron/preload/preload.js');
const INDEX_PATH_PROD = join(DIR_NAME, '../../dist/index.html');


const config = {
    paths: {
        DIR_NAME,
        JAR_PATH,
        SPLASH_PATH,
        PRELOAD_PATH,
        INDEX_PATH_PROD,
    },
    env: {
        NODE_ENV,
        IS_DEV,
        VITE_PORT,
        VITE_API_PORT,
    },
};

export default config;
