/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// Wait for the deviceready event before using any of Cordova's device APIs.
// See https://cordova.apache.org/docs/en/latest/cordova/events/events.html#deviceready
document.addEventListener('deviceready', onDeviceReady, false);

function onDeviceReady() {
    // Cordova is now initialized. Have fun!

    console.log('Running cordova-' + cordova.platformId + '@' + cordova.version);
    document.getElementById('deviceready').classList.add('ready');

    // Add Callback to IntelliProve Native Module for presenting the IntelliWebView
    document.getElementById('openWebViewButton').addEventListener('click', function () {
        const url = "https://plugin.intelliprove.com/?action_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjp7ImVtYWlsIjoiIiwiY3VzdG9tZXIiOiJ0ZXN0aW5nLWFwaS1rZXkiLCJncm91cCI6ImFkbWluIiwibWF4X21lYXN1cmVtZW50X2NvdW50IjotMSwidXNlcl9pZCI6bnVsbCwiYXV0aDBfdXNlcl9pZCI6bnVsbH0sIm1ldGEiOnt9LCJleHAiOjE3NjcyMjU2MDB9.3aW8GlcctgPzryNmnuSRxzf4QyD6RXC_W9ng8tHFyyE&patient=Dries&performer=Dries";

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
