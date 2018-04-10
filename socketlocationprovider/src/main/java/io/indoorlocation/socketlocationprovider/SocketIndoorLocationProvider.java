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
import java.util.Timer;
import java.util.TimerTask;

import io.indoorlocation.core.IndoorLocation;
import io.indoorlocation.core.IndoorLocationProvider;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;

import static android.content.Context.WIFI_SERVICE;


public class SocketIndoorLocationProvider extends IndoorLocationProvider {

    private String mServerUrl;
    private Context mContext;
    private Socket socket;
    private boolean started = false;
    private String clientIp;
    private Timer refreshIpTimer;
    private TimerTask refreshIpTimerTask;

    public SocketIndoorLocationProvider(Context context, String url) {
        super();
        this.mContext = context;
        this.mServerUrl = url;
        refreshIpTimerTask = new TimerTask() {
            @Override
            public void run() {
                refreshIp();
            }
        };
    }

    private String wifiIpAddress() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString = null;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = null;
        }

        return ipAddressString;
    }

    private void refreshIp() {
        String newIp = this.wifiIpAddress();
        if (newIp != null && socket == null) {
            clientIp = newIp;
            this.destroySocket();
            this.initSocket();
        }
        if (newIp == null) {
            destroySocket();
        }
    }

    private void initSocket() {
        if (mServerUrl != null && clientIp != null) {
            final IO.Options options = new IO.Options();
            options.query = "userId="+clientIp;

            try {
                this.socket = IO.socket(mServerUrl, options);

                this.socket.on("connect", new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        started = true;
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
                        started = false;

                    }

                }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        EngineIOException json = (EngineIOException) args[0];
                        dispatchOnProviderError(new Error(json.getMessage()));
                        started = false;

                    }

                });

                socket.connect();

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private void destroySocket() {
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
    }

    @Override
    public boolean supportsFloor() {
        return true;
    }

    @Override
    public void start() {
        if(started) {
            return;
        }
        started = true;
        refreshIp();
        if (refreshIpTimer == null) {
            refreshIpTimer = new Timer();
            refreshIpTimer.scheduleAtFixedRate(refreshIpTimerTask, 0, 10000);
        }
    }

    @Override
    public void stop() {
        if (started) {
            refreshIpTimer.cancel();
            refreshIpTimer.purge();
            destroySocket();
            started = false;
        }
    }

    @Override
    public boolean isStarted() {
        return started;
    }

}
