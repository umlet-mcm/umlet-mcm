# MCM Frontend

## Requirements
- `.env` file placed at the root of `mcm-frontend` directory, containing the following variables:
    - `VITE_PORT`: The port the frontend will run on
    - `VITE_API_PORT`: The port the backend will run on
    - Initially, copy the file `mcm-frontend/.env.example` & adjust it to your needs
- `npm`  needs to be installed (version 20 was used for development)

## Setup
- Before the first execution, `npm install` needs to be executed
- The frontend can be started by running `npm run dev`

## Project scripts
- `npm run dev` - Start development server
- `npm run build` - Build project
- `npm run electron:dev` - Start electron app in development mode. The frontend is served by the vite server. The
  backend has to be started manually.
- `npm run electron:preview` - Start electron app in preview mode. The frontend is built but not the electron app. The
  backend is started automatically.
- `npm run electron:build` - Build electron app. The frontend is built but not the electron app. The backend is started
  automatically.