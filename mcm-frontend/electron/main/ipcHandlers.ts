import { ipcMain, dialog } from 'electron';
import log from 'electron-log/main';

async function handleFileOpen() {
    try {
        const { canceled, filePaths } = await dialog.showOpenDialog({ title: 'Open File' });
        console.log('filePaths:', filePaths);
        console.log('canceled:', canceled);
        return !canceled && filePaths.length > 0 ? filePaths[0] : undefined;
    } catch (error) {
        log.error('Failed to open file dialog:', error)
    }
}

function setupIpcHandlers() {
    ipcMain.handle('dialog:openFile', handleFileOpen);
}

export { setupIpcHandlers };
