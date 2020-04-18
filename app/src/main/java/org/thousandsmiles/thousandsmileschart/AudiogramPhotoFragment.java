/*
 * (C) Copyright Syd Logan 2020
 * (C) Copyright Thousand Smiles Foundation 2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thousandsmiles.thousandsmileschart;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.Audiogram;
import org.thousandsmiles.tscharts_lib.AudiogramREST;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.ImageREST;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class AudiogramPhotoFragment extends Fragment {
    private Activity m_activity = null;
    private boolean m_init = true;
    private SessionSingleton m_sess = null;
    private boolean m_dirty = false;
    private boolean m_inCamera = false;
    private ImageView m_mainImageView = null;
    private ImageView m_buttonImageView = null;
    private String m_currentPhotoPath = "";
    private PhotoFile m_photo;
    private PhotoFile m_tmpPhoto;     // used to hold result of camera, copied on success to corresponding m_photo[123]
    static final int REQUEST_TAKE_PHOTO = 1;
    Audiogram m_audiogram = null;
    private View m_view = null;

    private class PhotoFile {
        private File m_file = null;
        private String m_path = "";
        int m_headshotImage = 0;

        public void onPhotoTaken() {
            ImageView v;
            setToCopyOfFile(m_tmpPhoto.getFile());
            v = (ImageView) m_activity.findViewById(m_headshotImage);
            if (v != null) {
                v.setClickable(true);
                setDirty();
                Picasso.with(getContext()).load(m_file).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(m_buttonImageView);
            }
        }

        public void setHeadshotImage(int id) {
            m_headshotImage = id;
        }

        public File getFile()
        {
            return m_file;
        }

        public void setToCopyOfFile(File file)
        {
            try {
                copyInputStreamToFile(new FileInputStream(file), m_file);
                m_file.setLastModified(file.lastModified());
            } catch (IOException e) {
            }
        }

        public void copyInputStreamToFile(final InputStream in, final File dest)
                throws IOException
        {
            copyInputStreamToOutputStream(in, new FileOutputStream(dest));
        }

        public void copyInputStreamToOutputStream(final InputStream in,
                                                  final OutputStream out) throws IOException {
            try {
                try {
                    final byte[] buffer = new byte[1024];
                    int n;
                    while ((n = in.read(buffer)) != -1)
                        out.write(buffer, 0, n);
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        }

        private void create()
        {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = m_activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                m_file = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
                );

                // Save a file: path for use with ACTION_VIEW intents
                m_path = m_file.getAbsolutePath();
            }
            catch (IOException e) {
                m_file = null;
                m_path = "";
            }
        }

        public void dispatchTakePictureIntent() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(m_activity.getPackageManager()) != null) {
                if (m_file != null) {
                    Uri photoURI = FileProvider.getUriForFile(m_activity,
                            "org.thousandsmiles.thousandsmileschart.android.fileprovider",
                        m_file);
                    m_inCamera = true;
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        }
    }

    private boolean validateFields()
    {
        boolean ret = true;
        return ret;
    }

    private void setDirty()
    {
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.VISIBLE);
        m_audiogram = copyAudiogramDataFromUI();
        button_bar_item.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                boolean valid = validateFields();
                if (valid == false) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle(m_activity.getString(R.string.title_missing_patient_data));
                    builder.setMessage(m_activity.getString(R.string.msg_please_enter_required_patient_data));

                    builder.setPositiveButton(m_activity.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    CreateAudiogramImageListener listener = new CreateAudiogramImageListener();
                    listener.setAudiogram(m_audiogram);
                    createAudiogramImage(listener);
                }
            }

        });
        m_dirty = true;
    }

    void updateAudiogram()
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                AudiogramREST rest = new AudiogramREST(m_sess.getContext());
                Object lock;
                int status;

                if (m_sess.getNewAudiogram() == true) {
                    lock = rest.createAudiogram(copyAudiogramDataFromUI());
                } else {
                    lock = rest.updateAudiogram(copyAudiogramDataFromUI());
                }

                synchronized (lock) {
                    // we loop here in case of race conditions or spurious interrupts
                    while (true) {
                        try {
                            lock.wait();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_save_audiogram), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            clearDirty();
                            m_audiogram = copyAudiogramDataFromUI();
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_saved_audiogram), Toast.LENGTH_LONG).show();
                            m_sess.setNewAudiogram(false);
                        }
                    });
                }
            }
        };
        thread.start();
    }

    private void clearDirty() {
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
        m_dirty = false;
    }

    public static AudiogramPhotoFragment newInstance() {
        return new AudiogramPhotoFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(Activity) context;
            m_sess = SessionSingleton.getInstance();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            m_photo.onPhotoTaken();
        }
        m_inCamera = false;
    }

    public void handleImageButtonPress(View v) {
        m_buttonImageView = (ImageView)  m_activity.findViewById(R.id.audiogram_image);
        m_tmpPhoto.dispatchTakePictureIntent();
    }

    public void createAudiogramImage(final RESTCompletionListener listener) {

        Thread thread = new Thread() {
            public void run() {
                // note we use session context because this may be called after onPause()
                ImageREST rest = new ImageREST(getContext());
                rest.addListener(listener);
                Object lock;
                int status;

                File file = m_tmpPhoto.m_file;

                lock = rest.createImage(file, m_sess.getClinicId(), m_sess.getDisplayPatientId(), "Audiogram");

                synchronized (lock) {
                    // we loop here in case of race conditions or spurious interrupts
                    while (true) {
                        try {
                            lock.wait();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            //Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_headshot_photo), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            //Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_headshot_photo), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public void getAudiogramImage(final int imageId, final GetAudiogramImageListener listener) {

        Thread thread = new Thread() {
            public void run() {
                // note we use session context because this may be called after onPause()

                File file = m_tmpPhoto.m_file;
                listener.setFile(file);
                listener.setContext(m_activity);
                listener.setImageView(m_mainImageView);

                ImageREST rest = new ImageREST(getContext());
                rest.addListener(listener);
                Object lock;
                int status;

                lock = rest.getImageData(imageId, file);

                synchronized (lock) {
                    // we loop here in case of race conditions or spurious interrupts
                    while (true) {
                        try {
                            lock.wait();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            //Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_headshot_photo), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            //Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_headshot_photo), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    private Audiogram copyAudiogramDataFromUI()
    {
        TextView tx;

        Audiogram audiogram = null;

        if (m_audiogram == null) {
            audiogram = new Audiogram();
        } else {
            audiogram = m_audiogram;      // copies over clinic, patient ID, etc..
        }

        audiogram.setPatient(m_sess.getDisplayPatientId());
        audiogram.setClinic(m_sess.getClinicId());

        EditText t = (EditText) m_view.findViewById(R.id.audiogram_comment);

        Editable text = t.getText();

        audiogram.setComment(text.toString());

        return audiogram;
    }

    private void copyAudiogramDataToUI() {
        TextView tx;

        if (m_audiogram != null) {

            String notes = m_audiogram.getComment();

            EditText t = (EditText) m_view.findViewById(R.id.audiogram_comment);

            t.setText(notes);

            GetAudiogramImageListener l = new GetAudiogramImageListener();
            l.setContext(m_activity);
            l.setImageView(m_mainImageView);
            getAudiogramImage(m_audiogram.getImage(), new GetAudiogramImageListener());
        }
        clearDirty();
    }

    private void setViewDirtyListeners()
    {
        EditText t = (EditText) m_view.findViewById(R.id.audiogram_comment);
        if (t != null) {
            t.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        m_photo = new PhotoFile();
        m_photo.create();
        m_photo.setHeadshotImage(R.id.audiogram_image);
        m_tmpPhoto = new PhotoFile();
        m_tmpPhoto.create();
        m_currentPhotoPath = m_tmpPhoto.m_file.getPath();
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        try {
            m_audiogram = (Audiogram) bundle.getSerializable("audiogram");
        } catch (Exception e ) {
            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_audiogram_data), Toast.LENGTH_SHORT).show();
        }
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (m_init == true) {
            m_mainImageView = (ImageView) m_activity.findViewById(R.id.audiogram_image);
            copyAudiogramDataToUI();
            setViewDirtyListeners();
            if (m_sess.getNewAudiogram() == true) {
                setDirty();
            } else {
                clearDirty();
                View v = (View) m_activity.findViewById(R.id.audiogram_image_button);
                if (v != null) {
                    v.setVisibility(View.GONE);
                }
            }
            m_init = false;
        }
    }

    class CreateAudiogramImageListener implements RESTCompletionListener {

        private Audiogram m_audiogram = null;

        public void setAudiogram(Audiogram audiogram) {
            m_audiogram = audiogram;

        }
        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {
            try {
                int id = m_audiogram.getId();
                m_audiogram.setImage(a.getInt("id"));
                updateAudiogram();
            } catch (Exception e) {
            }
        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {

        }
    }

    class GetAudiogramImageListener implements RESTCompletionListener {

        private File m_file = null;
        private Context m_context = null;
        private ImageView m_imageView = null;

        public void setFile(File file) {
            m_file = file;
        }

        public void setContext(Context context) {
            m_context = context;
        }

        public void setImageView(ImageView view) {
            m_imageView = view;
        }

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {
            try {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Picasso.with(m_context).load(m_file).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(m_imageView);
                     }
                });

            } catch (Exception e) {
            }
        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {
        }
    }

    @Override
    public void onPause() {
        Activity activity = getActivity();
        if (activity != null) {
            View button_bar_item = activity.findViewById(R.id.save_button);
            if (button_bar_item != null) {
                button_bar_item.setVisibility(View.GONE);
            }
        }

        super.onPause();

        final Audiogram mh = this.copyAudiogramDataFromUI();

        if (m_inCamera == false && (m_dirty || mh.equals(m_audiogram) == false)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_unsaved_audiogram));
            builder.setMessage(m_activity.getString(R.string.msg_save_audiogram));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    CreateAudiogramImageListener listener = new CreateAudiogramImageListener();
                    listener.setAudiogram(m_audiogram);
                    createAudiogramImage(listener);
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(m_activity.getString(R.string.button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }

        View button_bar_item = getActivity().findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        ImageView v = (ImageView) m_activity.findViewById(R.id.audiogram_image);
        if (v != null) {
            v.setClickable(false);
        }
        final View imageButton = (View) m_activity.findViewById(R.id.audiogram_image_button);
        if (imageButton != null) {
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleImageButtonPress(imageButton);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.audiogram_photo_fragment_layout, container, false);
        m_view  = view;
        return view;
    }
}