import { BrowserWindow, screen } from 'electron';
import waitOn from 'wait-on';
import config from './config.js';
import log from 'electron-log/main';


/**
 * Execute the async task and show splash screen while it's running. Switch to main window when done.
 * @param serverTask Async task to execute
 */
async function createWindow(serverTask: () => Promise<void>) {
    let mainWindow: BrowserWindow | null = null;
    let splash: BrowserWindow | null = null;

    try {
        splash = await createSplashScreen();
        mainWindow = await createMainWindow();

        await serverTask();

        splash.destroy();
        mainWindow.show();

        mainWindow.reload();
    }
    catch (error) {
        log.error('Failed to start the app:', error);
        if (splash) {
            splash.destroy();
        }
        if (mainWindow) {
            mainWindow.destroy();
        }

        throw error;
    }
}


/**
 * Create a new window containing splash screen
 * @returns Splash screen window
 */
async function createSplashScreen() {
    let splash = new BrowserWindow({
        width: 300,
        height: 300,
        frame: false,
        transparent: true,
        alwaysOnTop: true,
    });

    await splash.loadFile(config.paths.SPLASH_PATH);

    return splash;
}

/**
 * Create the main window. Load either from Vite or bundled resources.
 * @returns Main window
 */
async function createMainWindow() {
    const { width, height } = screen.getPrimaryDisplay().workAreaSize

    let mainWindow = new BrowserWindow({
        show: false,
        width: width,
        height: height,
        webPreferences: {
            preload: config.paths.PRELOAD_PATH,
        },
    });

    try {
        // In dev mode Vite serves the frontend on localhost
        if (config.env.IS_DEV) {
            log.info('Waiting for Vite to start on port', config.env.VITE_PORT);

            const viteUrl = `http://localhost:${config.env.VITE_PORT}`;
            await waitOn({ resources: [viteUrl] });

            await mainWindow.loadURL(viteUrl);

            log.info('Vite started');

            mainWindow.webContents.openDevTools();
        }
        // In production mode the frontend is bundled
        else {
            await mainWindow.loadFile(config.paths.INDEX_PATH_PROD);
        }
    } catch (error) {
        log.error('Failed to load main window:', error);
    }

    return mainWindow;
}

export { createWindow };
