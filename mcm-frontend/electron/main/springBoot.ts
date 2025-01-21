import { join } from 'path';
import { spawn, exec, execSync } from 'child_process';
import config from './config.js';
import log from 'electron-log/main';
import axios from 'axios';

const MINIMUM_JAVA_VERSION = 21.0;
const DOWNLOAD_URL = 'https://www.oracle.com/fr/java/technologies/downloads/#java21';

let springBootProcess: any = null;

/**
 * Check if Java is installed and available in PATH
 */
async function isJavaInstalled(): Promise<void> {
    return new Promise((resolve, reject) => {
        exec('java -version', (error, _stdout, stderr) => {
            if (error) {
                log.error('Java is not installed or not in PATH:', error);
                return reject(new Error(
                    `Java is not installed or not in your PATH.\n\n` +
                    `Please install the latest version from ${DOWNLOAD_URL} or add Java (>= ${MINIMUM_JAVA_VERSION}) to your PATH.`
                ));
            }

            const versionOutput = stderr.toString();
            const versionMatch = versionOutput.match(/version "(\d+\.\d+).*"/);

            if (!versionMatch) {
                log.error('Could not parse Java version:', versionOutput);
                return reject(new Error('Could not parse Java version.'));
            }
            else {
                const [major, minor] = versionMatch[1].split('.').map(Number);
                const version = major + '.' + minor;

                if (major < MINIMUM_JAVA_VERSION) {
                    log.error('Java version is too old:', version);
                    return reject(new Error(
                        `Java version ${version} is too old. Version ${MINIMUM_JAVA_VERSION} or higher is required.\n\n` +
                        `Please install the latest version from ${DOWNLOAD_URL} and add it to your PATH.`
                    ));
                }

                log.info('Java version:', version);
                resolve();
            }
        });
    });
}

/**
 * Wait the Spring Boot server to be ready by checking the health endpoint
 */
async function waitForServerReady(): Promise<void> {
    const maxRetries = 50;
    let retries = 0;

    return new Promise((resolve, reject) => {
        // Ping the health endpoint every second until the server is ready or max retries reached
        const interval = setInterval(async () => {
            try {
                const response = await axios.get(`http://localhost:${config.env.VITE_API_PORT}/actuator/health`);
                if (response.status === 200 && response.data.status === 'UP') {
                    clearInterval(interval);
                    log.info('Spring Boot server is ready!');
                    resolve();
                }
            } catch (err) {
                retries++;
                if (retries >= maxRetries) {
                    clearInterval(interval);
                    reject(new Error('Spring Boot server failed to start.'));
                }
            }
        }, 1000);
    });
}

/**
 * Start the Spring Boot server on a separate process and block main process until the server is ready
 */
async function startSpringBootServer(): Promise<void> {
    log.info('Starting Spring Boot server...');

    await isJavaInstalled();

    // Start the Spring Boot server on a separate process
    springBootProcess = spawn('java', ['-jar', config.paths.JAR_PATH], {
        stdio: ['ignore', 'pipe', 'pipe'],
        windowsHide: true,
    });

    log.info('Starting Spring Boot server with PID:', springBootProcess.pid);

    return new Promise((resolve, reject) => {
        // handle server output and errors
        const onData = (data: any) => {
            const message = data.toString();
            log.info(`[Spring Boot] ${message}`);
        };

        const onError = (data: any) => {
            const error = data.toString();
            log.error(`[Spring Boot Error] ${error}`);
            if (error.includes('SEVERE') || error.includes('Exception') || error.includes('Error')) {
                return reject(new Error(`Spring Boot server error:\n${error}`));
            }
        };

        const onClose = (code: number) => {
            log.error(`Spring Boot server exited with code ${code}`);
            if (code !== 0) {
                return reject(new Error(`Spring Boot server exited with code ${code}`));
            }
        };


        springBootProcess.stdout?.on('data', onData);
        springBootProcess.stderr?.on('data', onError);
        springBootProcess.on('close', onClose);

        // Block main process until the server is ready
        waitForServerReady()
            .then(() => {
                log.info('Spring Boot server started successfully.');
                resolve();
            })
            .catch((err) => {
                log.error('Failed to detect Spring Boot server readiness:', err);
                reject(new Error('Failed to detect Spring Boot server readiness.'));
            })
            .finally(() => {
                // Cleanup listeners
                springBootProcess.stdout?.off('data', onData);
                springBootProcess.stderr?.off('data', onError);
                springBootProcess.off('close', onClose);
            });
    });
}

/**
 * Stop the Spring Boot server by killing the process
 */
async function stopSpringBootServer() {
    log.info('Stopping Spring Boot server');

    if (springBootProcess && springBootProcess.pid) {
        log.info('Killing Spring Boot process with pid:', springBootProcess.pid);

        killAll(springBootProcess.pid);

        springBootProcess = null;
    } else {
        log.warn('Spring Boot process is not running.');
    }
}

/**
 * Kill all child processes of a given process id (including the process itself)
 * @param pid server process id
 * @param signal signal to send to the process
 */
function killAll(pid: number, signal: string | number = 'SIGTERM') {
    try {
        if (process.platform === "win32") {
            // Windows does not automatically kill child processes when the parent is killed
            // Need to do this manually
            execSync(`taskkill /PID ${pid} /T /F`);
        } else {
            springBootProcess.kill()
        }
    } catch (err) {
        log.error('Failed to kill process:', err);
    }
}

export { startSpringBootServer, stopSpringBootServer };
