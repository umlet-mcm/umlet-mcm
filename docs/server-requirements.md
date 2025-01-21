# Server Requirements Document

## 1. Notes

### 1.1 General Observations

This document provides suggestions regarding the server minimum requirements for running the **umletino-mcm** project.  
These suggestions are based on observations of the resources consumed when running the project on personal computers.  
They should be considered for **informational purposes only**, as deployment on a server intended for public exposure  
would require extensive testing to determine the optimal configuration.

### 1.2 User Management Limitations

The project does not implement specific user management functionality:

- No authentication system is provided.
- No user-specific workspaces are created.

Deploying a single instance exposed to public users would result in:

- **Shared Git Directory**: All users sharing the same Git directory.
- **Potential Risks**: Users would have access to modify and delete all configurations within the workspace.

---

## 2. Hardware Requirements

These estimations are based on the project's consumption when run locally on the following machine configuration:

- **OS**: Microsoft Windows 11 Professional
- **CPU**: 11th Gen Intel(R) Core(TM) i7-11850H @ 2.50GHz, 2496 MHz, 8 cores, 16 logical processors
- **RAM**: 32 GB

The project has also been tested on Linux-based operating systems and performs flawlessly.

### 2.1 Backend

| Resource     | Minimum Requirement                                                                                                                                                                                                                                       |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **RAM**      | 4 GB per instance                                                                                                                                                                                                                                         |
| **CPU**      | 4 cores per instance                                                                                                                                                                                                                                      |
| **Database** | Embedded within the app                                                                                                                                                                                                                                   |
| **Storage**  | - Configurations are stored and versioned via Git repositories on the server. <br/>- Storage requirements depend on the usage and size of models. <br/> - Initial recommended storage: **30 GB per instance**, including space for the embedded database. |

### 2.2 Frontend

The frontend is served statically after build, resulting in minimal resource requirements:

- **Disk Space**: ~1 GB for static files
- **RAM**: Negligible (as files are served statically)
- **CPU**: Minimal impact

---

## 3. Software Requirements

### 3.1 Operating System

- **Preferred OS**: Linux-based OS

### 3.2 Java Runtime Environment (JRE)

- **Version**: JRE 21 (e.g., Amazon Corretto)

### 3.3 Neo4J Database

- **Version**: Embedded Neo4J (bundled with the application)

### 3.4 Embedded Git

- **Version**: Embedded Git (bundled with the application)
- **Configuration Path**: The path where configurations are stored needs to be specified.
    - Default path: `/tmp`

## 4. Reverse Proxy Requirements

To manage multiple users, each with their own frontend and backend servers, a reverse proxy could be used.
It would be in charge of request routing and secure connections.

### 4.1 Suggested Tools

- **Nginx**: A lightweight and widely-used proxy.

### 4.2 Configuration

The reverse proxy should:

- Route requests to the appropriate backend and frontend instances based on user context.
- Support SSL/TLS termination to secure communication.
- Enable configuration for adding new users & servers.
