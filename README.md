# Get user position from Wi-Fi infrastructures on Android

This provider allows you to retrieve IndoorLocations from a server using web socket. In particular, it's perfect to retrieve locations from Wi-Fi infrastructures.

This provider needs to be connected to a SocketIndoorLocationEmitter server. Multiple emitters are available:

- [Cisco CMX](https://github.com/IndoorLocation/cmx-socket-indoor-location-emitter)
- [Cisco Meraki](https://github.com/IndoorLocation/meraki-socket-indoor-location-emitter)
- [Test (for debug)](https://github.com/IndoorLocation/test-socket-indoor-location-emitter)

## Use

Instanciate the provider with your emitter URL:

```
socketIndoorLocationProvider = new SocketIndoorLocationProvider(this, "YOUR_SERVER_SOCKET_ADDRESS");
```

Set the provider in your Mapwize SDK:

```
mapwizePlugin.setLocationProvider(socketIndoorLocationProvider);     
```

## Demo app

A simple demo application to test the provider is available in the /app directory.

You will need to set your credentials in SocketIndoorLocationProviderDemoApp and MapActivity.

Sample keys are given for Mapwize and Mapbox. Please note that those keys can only be used for testing purposes, with very limited traffic, and cannot be used in production. Get your own keys from [mapwize.io](https://www.mapwize.io) and [mapbox.com](https://www.mapbox.com). Free accounts are available.

## Contribute

Contributions are welcome. We will be happy to review your PR.

## Support

For any support with this provider, please do not hesitate to contact [support@mapwize.io](mailto:support@mapwize.io)

## License

MIT