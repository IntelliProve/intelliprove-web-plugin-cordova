var exec = require('cordova/exec');

exports.presentWebView = function (urlString, success, error) {
    exec(success, error, "CDVIntelliProveWebView", "presentWebView", [urlString]);
};