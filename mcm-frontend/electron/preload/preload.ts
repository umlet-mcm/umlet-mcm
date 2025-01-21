const { contextBridge, ipcRenderer } = require('electron');

/**
 * Safely expose APIs to the Renderer Process via contextBridge.
 */
contextBridge.exposeInMainWorld('electronAPI', {
    /**
     * Open a file dialog and return the selected file path.
     */
    openFile: async (): Promise<string | undefined> => {
        return await ipcRenderer.invoke('dialog:openFile');
    },

    /**
     * Listen for events from the main process.
     * @param eventName - The name of the event to listen for.
     * @param callback - The callback function to handle the event.
     */
    onAppEvent: (eventName: string, callback: (event: any, ...args: any[]) => void): void => {
        ipcRenderer.on(eventName, callback);
    },

    /**
     * Send data to the main process through a specific channel.
     * @param channel - The IPC channel.
     * @param data - The data to send.
     */
    sendToMain: (channel: string, data: any): void => {
        ipcRenderer.send(channel, data);
    }
});

/**
 * Replace version text in the DOM (optional, useful for debugging).
 */
window.addEventListener('DOMContentLoaded', () => {
    const replaceText = (selector: string, text: string): void => {
        const element = document.getElementById(selector);
        if (element) element.innerText = text;
    };

    ['chrome', 'node', 'electron'].forEach((dependency) => {
        replaceText(`${dependency}-version`, process.versions[dependency] || 'N/A');
    });
});
