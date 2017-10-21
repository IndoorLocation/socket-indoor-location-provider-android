package io.indoorlocation.socketlocationprovider;

import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import io.indoorlocation.core.IndoorLocation;
import io.indoorlocation.core.IndoorLocationProvider;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.SocketIOException;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;

import static android.content.Context.WIFI_SERVICE;


public class SocketIndoorLocationProvider extends IndoorLocationProvider {

    private String mURL;
    private Context mContext;
    private Socket socket;
    private boolean connected = false;

    public SocketIndoorLocationProvider(Context context, String url) {
        super();
        this.mContext = context;
        this.mURL = url;

        if (this.mURL == null) {
            return;
        }

        final IO.Options options = new IO.Options();
        options.query = "userId="+wifiIpAddress();

        try {
            this.socket = IO.socket(mURL, options);

            this.socket.on("connect", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    connected = true;
                    dispatchOnProviderStarted();
                }

            }).on("indoorLocationChange", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    JSONObject object = (JSONObject) args[0];
                    try {
                        JSONObject position = object.getJSONObject("indoorLocation");
                        double latitude = position.getDouble("latitude");
                        double longitude = position.getDouble("longitude");
                        Double floor = position.optDouble("floor");
                        Integer accuracy = position.optInt("accuracy");

                        Location location = new Location(getName());
                        location.setLatitude(latitude);
                        location.setLongitude(longitude);
                        location.setAccuracy(accuracy);
                        location.setTime(System.currentTimeMillis());
                        IndoorLocation indoorLocation = new IndoorLocation(location, floor);

                        dispatchIndoorLocationChange(indoorLocation);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    dispatchOnProviderStopped();
                    connected = false;

                }

            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    EngineIOException json = (EngineIOException) args[0];
                    dispatchOnProviderError(new Error(json.getMessage()));
                    connected = false;

                }

            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private String wifiIpAddress() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = null;
        }

        return ipAddressString;
    }

    @Override
    public boolean supportsFloor() {
        return true;
    }

    @Override
    public void start() {
        if (this.socket != null) {
            this.socket.open();
        }
    }

    @Override
    public void stop() {
        if (this.socket != null) {
            this.socket.close();
        }
    }

    @Override
    public boolean isStarted() {
        return connected;
    }

}
