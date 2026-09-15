// Serve the dev server over TLS so the app can be embedded in Storyblok's Visual Editor, which
// requires https even on localhost. Generate the certificate from the project root with:
//
//     mkcert -install
//     mkcert -cert-file localhost.pem -key-file localhost-key.pem localhost 127.0.0.1
//
// `__dirname` is the generated webpack package directory, four levels below the project root.
const path = require("path");
const projectRoot = path.resolve(__dirname, "../../../..");

config.devServer = config.devServer || {};

// The Visual Editor appends each story's real path to the preview URL, so serve index.html for
// any path rather than 404ing on anything but `/`.
config.devServer.historyApiFallback = true;

config.devServer.server = {
    type: "https",
    options: {
        key: path.join(projectRoot, "localhost-key.pem"),
        cert: path.join(projectRoot, "localhost.pem"),
    },
};
