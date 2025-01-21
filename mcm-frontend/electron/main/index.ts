import { app, BrowserWindow, dialog } from 'electron';
import { createWindow } from './window.js';
import { startSpringBootServer, stopSpringBootServer } from './springBoot.js';
import { setupIpcHandlers } from './ipcHandlers.js';
import config from './config.js';
import log from 'electron-log/main';



/**
 * Entry point of the application
 */
async function initializeApp() {
    try {
        if (config.env.IS_DEV) {
            await createWindow(async () => { });
        }
        else {
            await createWindow(startSpringBootServer);
        }

        setupIpcHandlers();

    } catch (error: any) {
        log.error('Initialization failed:', error)
        dialog.showErrorBox('Initialization Error', error.message || 'Unknown error');
        cleanUpAndExit();
    }
}

/**
 * Handles cleanup tasks before the application exits.
 */
async function cleanUpAndExit() {
    try {
        if (!config.env.IS_DEV) {
            await stopSpringBootServer();
        }
    } catch (error) {
        dialog.showErrorBox('Initialization Error', error.message || 'Unknown error');
    } finally {
        app.quit();
    }
}

// Ensure that only one instance of the app is running
// Will have to be removed for multi-client support
const gotTheLock = app.requestSingleInstanceLock();

if (!gotTheLock) {
    log.error('Another instance of the application is already running. Exiting.');
    dialog.showErrorBox('Initialization Error', 'Another instance of the application is already running. Exiting.');
    app.quit();
} else {
    app.whenReady().then(initializeApp);

    app.on('window-all-closed', async () => {
        await cleanUpAndExit();
    });

    process.on('SIGINT', cleanUpAndExit); // Ctrl+C interruption
    process.on('SIGTERM', cleanUpAndExit); // Standard termination signal

    process.on('uncaughtException', (err) => {
        log.error('Uncaught Exception:', err);
        cleanUpAndExit();
    });
}
