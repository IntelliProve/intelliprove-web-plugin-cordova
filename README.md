# cdv-intelliprovewebview

IntelliProve WebView for Cordova

## System
Install Cordova:
https://cordova.apache.org/docs/en/10.x/guide/cli/#installing-the-cordova-cli

Check Cordova requirements:
`cordova requirements`

## Package Cordova plugin
- Go to ./plugin folder
- Update version number in package.json
- `npm pack`

## Example Adding IntelliWebViewWSDK

### Installing the Cordova Plugin in your Cordova App project

* Download the `cdv-intelliprovewebview-x.x.x.tgz` tarball and unpack it into a folder.
* In your Cordova App root directory, execute `cordova plugin add /path/to/unpacked/tarball`.

### Using the IntelliProveWebView in your application

Here is a minimal example of how to use the IntelliProveWebView in the `index.html` file:

```
<html>
    ...
    <body>
        <div class="app">
            <h1>IntelliProveWebView Cordova Example</h1>
            <button id="openWebViewButton">Open WebView</button>
        </div>
        <script src="cordova.js"></script>
        <script src="js/index.js"></script>
    </body>
</html>
```

And the associated `index.js` file:

```
document.addEventListener('deviceready', onDeviceReady, false);

function onDeviceReady() {
    // Add Callback to IntelliProve Native Module for presenting the IntelliWebView
    document.getElementById('openWebViewButton').addEventListener('click', function () {
        const url = "https://plugin.intelliprove.com/?action_token=xxxxx";

        if (cordova.plugins && cordova.plugins.CDVIntelliProveWebView && cordova.plugins.CDVIntelliProveWebView.presentWebView) {
            cordova.plugins.CDVIntelliProveWebView.presentWebView(url,
                function postMessageCallback(postMessage) {
                    // Handle the received postMessage
                    console.log('CDV PostMessage Full Body:', postMessage);

                    try {
                        // The PostMessage is received as a JSON String, so we need to parse it
                        const parsedMessage = JSON.parse(postMessage);
                        if (parsedMessage && parsedMessage.stage) {
                            console.log('CDV PostMessage Stage:', parsedMessage.stage);
                        } else {
                            console.warn('CDV invalid PostMessage format:', parsedMessage);
                        }
                    } catch (error) {
                        console.error('CDV error parsing PostMessage:', error);
                    }
                },
                function errorCallback(err) {
                    console.error("Failed to open WebView:", err);
                }
            );
        } else {
            console.warn("CDVIntelliProveWebView.presentWebView is not available.");
        }
    });
}
```

### Running your application

Depending on how you build the application, you would run a different command.
For this example, let's assume cordova is installed as a ruby gem:

`cordova platforms add android` or `cordova platforms add ios`

`cordova clean android` or `cordova clean ios`
`cordova build android` or `cordova build ios`
`cordova run android` or `cordova run ios`

