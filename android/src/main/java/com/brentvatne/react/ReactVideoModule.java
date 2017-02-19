package com.brentvatne.react;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.os.Environment;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.utils.AnimatedGifEncoder;
import com.utils.ImageUtil;
import android.net.Uri;

import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;
import android.provider.MediaStore;
import android.media.MediaScannerConnection;

public class ReactVideoModule extends ReactContextBaseJavaModule {
    private static final String TAG = "ReactVideoModule";
    private static ReactApplicationContext _reactContext;

    public static final int CAPTURE_MODE_STILL = 0;
    public static final int CAPTURE_MODE_ANIMATION = 1;

    public static final int CAPTURE_TARGET_MEMORY = 0;
    public static final int CAPTURE_TARGET_DISK = 1;
    public static final int CAPTURE_TARGET_CAMERA_ROLL = 2;

    public ReactVideoModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this._reactContext = reactContext;
    }

    @Override
    public String getName() {
        return TAG;
    }
    public static ReactApplicationContext getReactContextSingleton() {
        return _reactContext;
    }
    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("CaptureMode", getCaptureModeConstants());
                put("CaptureTarget", getCaptureTargetConstants());
            }
        });
    }
    private Map<String, Object> getCaptureModeConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("still", CAPTURE_MODE_STILL);
                put("animation", CAPTURE_MODE_ANIMATION);
            }
        });
    }
    private Map<String, Object> getCaptureTargetConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("memory", CAPTURE_TARGET_MEMORY);
                put("disk", CAPTURE_TARGET_DISK);
                put("cameraRoll", CAPTURE_TARGET_CAMERA_ROLL);
            }
        });
    }

    @ReactMethod
    public void capture(final ReadableMap options, final Promise promise) {
        try{
            WritableMap response = new WritableNativeMap();
            File pictureFile;

            if(options.getInt("mode") == CAPTURE_MODE_STILL) {
                switch (options.getInt("target")) {
                    case CAPTURE_TARGET_DISK:
                        pictureFile = getOutputFile(
                                options.getInt("mode"),
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        );
                        Throwable error = writeDataToFile(ImageUtil.getByteArray(ReactVideoView.captureSnapShot()), pictureFile);
                        if (error != null) {
                            promise.reject(error);
                            return;
                        }
                        response.putString("path", Uri.fromFile(pictureFile).toString());
                        promise.resolve(response);
                        break;
                    case CAPTURE_TARGET_CAMERA_ROLL:
                        ContentValues values = new ContentValues();
                        pictureFile = getOutputFile(
                                options.getInt("mode"),
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        );
                        values.put(MediaStore.Video.Media.DATA, pictureFile.getPath());
                        values.put(MediaStore.Video.Media.TITLE, "image");
                        values.put(MediaStore.Video.Media.MIME_TYPE, "image/png");
                        Throwable err = writeDataToFile( ImageUtil.getByteArray(ReactVideoView.captureSnapShot()) ,pictureFile);
                        if (err != null) {
                            promise.reject(err);
                            return;
                        }
                        _reactContext.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                        addToMediaStore(pictureFile.getAbsolutePath());
                        response.putString("path", Uri.fromFile(pictureFile).toString());
                        promise.resolve(response);
                        break;
                    default:
                    case CAPTURE_TARGET_MEMORY:
                        response.putString("data", ImageUtil.convert(ReactVideoView.captureSnapShot()));
                        promise.resolve(response);
                        break;
                }
            } else if(options.getInt("mode") == CAPTURE_MODE_ANIMATION) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                AnimatedGifEncoder encoder = new AnimatedGifEncoder();
                encoder.start(bos);
                encoder.setDelay(250);
                encoder.setRepeat(0);
                int seconds = 10;
                int counter = 0;
                int millisecondsByFrame = 250;
                while (counter < ((1000 *seconds)/millisecondsByFrame)) {
                    encoder.addFrame(ReactVideoView.captureSnapShot());
                    Thread.sleep(millisecondsByFrame);
                    counter++;
                }
                encoder.finish();

                switch (options.getInt("target")) {
                    case CAPTURE_TARGET_DISK:
                        pictureFile = getOutputFile(
                                options.getInt("mode"),
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        );
                        Throwable error = writeDataToFile(bos.toByteArray() , pictureFile);
                        if (error != null) {
                            promise.reject(error);
                            return;
                        }
                        response.putString("path", Uri.fromFile(pictureFile).toString());
                        promise.resolve(response);
                        break;
                    case CAPTURE_TARGET_CAMERA_ROLL:
                        ContentValues values = new ContentValues();
                        pictureFile = getOutputFile(
                                options.getInt("mode"),
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        );
                        values.put(MediaStore.Video.Media.DATA, pictureFile.getPath());
                        values.put(MediaStore.Video.Media.TITLE, "animation");
                        values.put(MediaStore.Video.Media.MIME_TYPE, "image/gif");
                        Throwable err = writeDataToFile( bos.toByteArray()  ,pictureFile);
                        if (err != null) {
                            promise.reject(err);
                            return;
                        }
                        _reactContext.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                        addToMediaStore(pictureFile.getAbsolutePath());
                        response.putString("path", Uri.fromFile(pictureFile).toString());
                        promise.resolve(response);
                        break;
                    case CAPTURE_TARGET_MEMORY:
                        response.putString("data", ImageUtil.convert(bos.toByteArray()));
                        promise.resolve(response);
                        break;
                }

            }


        }catch(Exception ex) {
            promise.reject(ex);
        }
    }
    private void addToMediaStore(String path) {
        MediaScannerConnection.scanFile(_reactContext, new String[] { path }, null, null);
    }
    private File getOutputFile(int mode, File storageDir) {
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String fileName = String.format("%s", new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));

        if (mode == CAPTURE_MODE_STILL) {
            fileName = String.format("IMG_%s.jpg", fileName);
        } else if (mode == CAPTURE_MODE_ANIMATION) {
            fileName = String.format("GIF_%s.gif", fileName);
        } else {
            return null;
        }

        return new File(String.format("%s%s%s", storageDir.getPath(), File.separator, fileName));
    }
    private Throwable writeDataToFile(byte[] data, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            return e;
        } catch (IOException e) {
            return e;
        }

        return null;
    }

}
