import IntelliProveSDK

@objc(CDVIntelliProveWebView)
class CDVIntelliProveWebView: CDVPlugin {
    private var callbackId: String?

    @objc(presentWebView:)
    func presentWebView(command: CDVInvokedUrlCommand) {
        guard let urlString = command.arguments[0] as? String else {
            let result = CDVPluginResult(status: .error, messageAs: "Invalid URL")
            self.commandDelegate.send(result, callbackId: command.callbackId)
            return
        }

        callbackId = command.callbackId

        let keepAlive = CDVPluginResult(status: .noResult)
        keepAlive?.setKeepCallbackAs(true)
        self.commandDelegate.send(keepAlive, callbackId: command.callbackId)

        DispatchQueue.main.async { [weak self] in
            guard let self else { return }
            let webViewController = IntelliWebViewFactory.newWebView(urlString: urlString, delegate: self)
            webViewController.modalPresentationStyle = .fullScreen
            UIApplication.shared.keyWindow?.rootViewController?.present(webViewController, animated: true)
        }
    }
}

extension CDVIntelliProveWebView: IntelliWebViewDelegate {
    public func didReceive(postMessage: String) {
        guard let callbackId else { return }

        let result = CDVPluginResult(status: .ok, messageAs: postMessage)
        result?.setKeepCallbackAs(true)
        self.commandDelegate.send(result, callbackId: callbackId)
    }
}
