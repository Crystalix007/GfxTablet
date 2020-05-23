package at.bitfire.gfxtablet;

import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class NetworkServer implements Runnable {
  static final int GFXTABLET_PORT = 40118;
  final SharedPreferences preferences;
  NetworkClient netClient;

  NetworkServer(SharedPreferences preferences) {
    this.preferences = preferences;
  }

  @Override
  public void run() {
    try {
      DatagramSocket socket = new DatagramSocket(GFXTABLET_PORT);

      SparseArray<byte[]> buffers = new SparseArray<>();
      // Init has to be done twice because the first call will be set on the server with 0.0.0.0
      // but we need the ip of the client.
      CanvasActivity.get().sendMotionStopSignal();
      CanvasActivity.get().sendMotionStopSignal();

      final int dataSize = 60000;
      final int suffixSize = 1;

      while (true) {
        byte[] buf = new byte[dataSize + suffixSize];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        final int packetStart = packet.getOffset();
        final int packetEnd = packetStart + packet.getLength();

        int n = buf[packetEnd - 1];
        Log.i("receive:", String.valueOf(n));
        if (n != 0) {
          buffers.put(n, Arrays.copyOfRange(buf, packetStart, packetEnd - suffixSize));
        } else if (buffers.size() > 0) {
          try {
            String path = "/storage/emulated/0/test.png"; // TODO set via options
            Log.i("buffer:", String.valueOf(buf[0]));
            boolean parts = buffers.size() == (int) buf[0];
            for (int i = 0; i < buf[0]; i++) {
              if (parts) {
                Log.i("keyAt " + i, String.valueOf(buffers.keyAt(i)));
                parts = buffers.keyAt(i) == i + 1;
              }
            }
            if (!parts) {
              buffers.clear();
              CanvasActivity.get().sendMotionStopSignal();
              Log.i("Image Problem", "tying to refetch the screenshot");
              continue;
            }
            Log.i("receive", "completed with " + buffers.size());

            FileOutputStream fos = new FileOutputStream(path);
            for (int i = 1; i <= buffers.size(); i++) {
              fos.write(buffers.get(i), 0, 60000);
            }
            fos.flush();
            fos.close();
            File file = new File(path);
            long size = file.length();
            Log.i("file-path", path);
            Log.i("file-size", String.valueOf(size));
            Log.i(
                "file-path-current",
                preferences.getString(SettingsActivity.KEY_TEMPLATE_IMAGE, "unset"));
            preferences.edit().putString(SettingsActivity.KEY_TEMPLATE_IMAGE, path).apply();
            CanvasActivity.get()
                .runOnUiThread(
                    new Runnable() {
                      @Override
                      public void run() {
                        CanvasActivity.get().showTemplateImage();
                      }
                    });
          } catch (IOException e) {
            e.printStackTrace();
          }
          // compile image and set it
          buffers.clear();
        } else {
          Log.i("receive", "Receive failure: unexpected packet with seq: " + n);
        }
      }
    } catch (Exception e) {
      Log.i("GfxTablet", "Screenshot server failed: " + e.getMessage());
    }
  }
}
