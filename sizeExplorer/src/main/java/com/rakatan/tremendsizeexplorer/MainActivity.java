package com.rakatan.tremendsizeexplorer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rakatan.tremendsizeexplorer.adapters.FileViewAdapter;
import com.rakatan.tremendsizeexplorer.events.FileClickedEvent;
import com.rakatan.tremendsizeexplorer.models.FileModel;
import com.rakatan.tremendsizeexplorer.utils.MyFileUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    public static final int PERMISSION_CODE = 9001;
    @BindView(R.id.toolbar_main)
    Toolbar toolbarMain;
    @BindView(R.id.recyclerView_main)
    RecyclerView recyclerViewMain;
    @BindView(R.id.button_up)
    Button buttonUp;
    @BindView(R.id.linearLayout_progress)
    LinearLayout linearLayoutProgress;

    FileViewAdapter fileViewAdapter;
    Stack<File> fileStack;
    boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbarMain);
        getSupportActionBar().setTitle(R.string.toolbar_title);

        fileStack = new Stack<>();
        requestFilesPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (fileStack.size() > 1)
            goUp();
        else { // Credits http://stackoverflow.com/a/13578600/3738533
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press Back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }

    }

    /**
     * Called whenever an item is clicked in the recyclerview.
     * Retains the clicked item in a stack (for back navigation) and updates the recyclerView
     *
     * @param event The event that serves as a message carrier between the adapter and activity
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void fileClicked(FileClickedEvent event) {
        fileStack.push(event.getFileModel().getFile());
        updateRecyclerView(event.getFileModel().getFile());
    }

    /**
     * Standard file read access permission request
     */
    private void requestFilesPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Setup the initial UI

                    setupRecyclerView();

                    // Pops the current folder that is in view and loads the previous one if it is not Root.
                    buttonUp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goUp();
                        }
                    });
                } else {
                    Toast.makeText(this, "You must grant the required permission!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Navigate up in the folder structure.
     */
    private void goUp() {
        if (fileStack.size() > 1) {
            fileStack.pop();
            updateRecyclerView(fileStack.peek());
        } else {
            Toast.makeText(MainActivity.this, "No where to go up to!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initial RecyclerView setup
     * Spawns a new thread to avoid file IO lockup
     * Creates the layout manager and populates the recyclerView with the Root folder's data.
     */
    private void setupRecyclerView() {
        linearLayoutProgress.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<FileModel> initialList = MyFileUtils.getRootFiles();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileStack.push(MyFileUtils.getSystemRoot());

                        fileViewAdapter = new FileViewAdapter(initialList);

                        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                        recyclerViewMain.setLayoutManager(layoutManager);

                        recyclerViewMain.setAdapter(fileViewAdapter);

                        linearLayoutProgress.setVisibility(View.GONE);
                    }
                });

            }
        }).start();
    }

    /**
     * Updates the contents of the recycler view to show the children of the selected folder.
     * Spawns a new thread to avoid file IO lockup
     *
     * @param file Source file for the new data
     */
    private void updateRecyclerView(final File file) {
        linearLayoutProgress.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<FileModel> newList = MyFileUtils.getFilesFromDirectory(file);

                if (newList != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileViewAdapter.updateFiles(newList);
                            linearLayoutProgress.setVisibility(View.GONE);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Folder is empty", Toast.LENGTH_SHORT).show();
                            fileStack.pop();
                            linearLayoutProgress.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }
}
