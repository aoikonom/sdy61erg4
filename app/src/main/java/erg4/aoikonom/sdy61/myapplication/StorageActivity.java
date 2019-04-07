package erg4.aoikonom.sdy61.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

public class StorageActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 2;

    private static final String TAG = "StorageActivity";

    private GoogleDriveHelper mDriveServiceHelper;
    private String mDriveFileId;
    private String mSavedFileName;

    private EditText titleEditText;
    private EditText contentsEditText;
    private ViewGroup rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        rootView = findViewById(R.id.rootView);
        findViewById(R.id.internet).setOnClickListener(this::onBrowseEap);
        findViewById(R.id.storage_open).setOnClickListener(this::onStorageOpen);
        findViewById(R.id.storage_save).setOnClickListener(this::onStorageCreateOrSave);

        titleEditText = findViewById(R.id.title);
        contentsEditText = findViewById(R.id.contents);

        requestSignIn();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_storage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_continue:
                Intent intent = new Intent(this, TabbedActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    Drive googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Drive API Migration")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new GoogleDriveHelper(googleDriveService);
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

    void onBrowseEap(View view) {

        if (!isNetworkConnectionAvailable()) {
            Snackbar snackbar = Snackbar.make(rootView, R.string.no_internet, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.refresh, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBrowseEap(null);
                }
            });
            snackbar.show();
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.eap.gr"));
            startActivity(browserIntent);
        }
    }

    boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                }
                break;

            case REQUEST_CODE_OPEN_DOCUMENT:
                if (resultCode == Activity.RESULT_OK && resultData != null) {

                    Uri uri = resultData.getData();
                    if (uri != null) {
                        openFileFromFilePicker(uri);
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }
    /**
     * Opens the Storage Access Framework file picker using {@link #REQUEST_CODE_OPEN_DOCUMENT}.
     */
    private void onStorageOpen(View view) {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Opening file picker.");

            Intent pickerIntent = mDriveServiceHelper.createFilePickerIntent();

            // The result of the SAF Intent is handled in onActivityResult.
            startActivityForResult(pickerIntent, REQUEST_CODE_OPEN_DOCUMENT);
        }
    }


    /**
     * Opens a file from its {@code uri} returned from the Storage Access Framework file picker
     * initiated by {@link #onStorageOpen(View view)}.
     */
    private void openFileFromFilePicker(Uri uri) {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Opening " + uri.getPath());

            mDriveServiceHelper.openFileUsingStorageAccessFramework(getContentResolver(), uri)
                    .addOnSuccessListener(driveFile -> {
                        mDriveFileId = null; //driveFile.fileId;
                        String name = driveFile.name;
                        String content = driveFile.contents;

                        titleEditText.setText(name);
                        contentsEditText.setText(content);
                        mSavedFileName = driveFile.name;

                        // Files opened through SAF cannot be modified.
                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Unable to open file from picker.", exception));
        }
    }


    /**
     * Creates a new file via the Drive REST API.
     */
    private void onStorageCreate(View view) {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Creating a file.");


            String fileName = titleEditText.getText().toString();
            String fileContents = contentsEditText.getText().toString();

            if (TextUtils.isEmpty(fileName))
                titleEditText.setError(getString(R.string.no_file_title));
            else
                mDriveServiceHelper.createFile(fileName, fileContents)
                        .addOnSuccessListener(fileId -> readFile(fileId))
                        .addOnFailureListener(exception ->
                                Log.e(TAG, "Couldn't create file.", exception));
        }
    }


    /**
     * Retrieves the title and content of a file identified by {@code fileId} and populates the UI.
     */
    private void readFile(String fileId) {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Reading file " + fileId);

            mDriveServiceHelper.readFile(fileId)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;

                        titleEditText.setText(name);
                        contentsEditText.setText(content);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't read file.", exception));
        }
    }

    private void onStorageCreateOrSave(View view) {
        String fileName = titleEditText.getText().toString();

        boolean shouldCreateFirst = mDriveFileId == null || !fileName.equals(mSavedFileName);
        if (shouldCreateFirst)
            onStorageCreate(view);
        else
            onStorageSave(view);

    }

    /**
     * Saves the currently opened file created via {@link #onStorageSave(View view)} if one exists.
     */
    private void onStorageSave(View view) {
        if (mDriveServiceHelper != null && mDriveFileId != null) {
            Log.d(TAG, "Saving " + mDriveFileId);

            String fileName = titleEditText.getText().toString();
            String fileContent = contentsEditText.getText().toString();

            mDriveServiceHelper.saveFile(mDriveFileId, fileName, fileContent)
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Unable to save file via REST.", exception));
        }
    }



}
