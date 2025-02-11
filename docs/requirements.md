# Requirements

## Development environment
### .env
.env file placed at the root of mcm-frontend directory, containing the following variables:
- VITE_PORT: The port the frontend will run on
- VITE_API_PORT: The port the backend will run on

**example:**
```
VITE_PORT=3000
VITE_API_PORT=8080
```

### JDK 21
JDK >= 21 is required to build the project <br>
It can be downloaded from [here](https://www.oracle.com/fr/java/technologies/downloads/#java21)

## Packaged application
### JRE 21
JRE >= 21 is required to run the packaged application <br>
It can be downloaded from [here](https://www.oracle.com/fr/java/technologies/downloads/#java21) <br>

#### How to Add Java 21 to the PATH
1. Open **Run** (`Win + R`), type `sysdm.cpl`, and press **Enter**.
2. Go to the **Advanced** tab and click **Environment Variables**.
3. Under **System Variables**, find `Path` and click **Edit**.
4. Click **New** and add the path to your Java 21 `bin` directory. Example: `C:\Program Files\Java\jdk-21\bin`.

