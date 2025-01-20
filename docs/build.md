# Build

## Electron

- When running `npm run electron:build` will package the electron app and place an installer in the `release` folder.

- The installer created depends on the OS the build is run on.

### Steps

1. Build the backend using `gradlew bootjar`
2. Build the electron app using `npm run electron:build`
3. The installer will be placed in the `release` folder
