package com.example.todoapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.Adapter.ToDoAdapter;
import com.example.todoapp.model.ToDoModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.Value;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDiaLogCloseListner {

    private RecyclerView recyclerView;
    private FloatingActionButton mFab;
    private FirebaseFirestore firestore;
    private ToDoAdapter adapter;
    private List<ToDoModel> mList;
    private Query query;
    private ListenerRegistration listenerRegistration;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Uri imageUri;
    private String imageFileName = "my_image_" + System.currentTimeMillis() + ".jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rlv_dsach);
        mFab = findViewById(R.id.floatingActionButton);


        firestore = FirebaseFirestore.getInstance();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThemNhiemVu.newInstance().show(getSupportFragmentManager(), ThemNhiemVu.TAG);
            }
        });
        mList = new ArrayList<>();
        adapter = new ToDoAdapter(MainActivity.this, mList);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        showData();
        recyclerView.setAdapter(adapter);

        imageView = findViewById(R.id.imageView);
        chonHinh();
    }

    private void showData() {
        query = firestore.collection("task").orderBy("time", Query.Direction.DESCENDING);
        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }

                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        String id = documentChange.getDocument().getId();
                        ToDoModel toDoModel = documentChange.getDocument().toObject(ToDoModel.class).withId(id);

                        mList.add(toDoModel);
                        adapter.notifyItemInserted(mList.size() - 1);
                    }
                }
            }
        });
    }


    private void chonHinh() {
        Button button = findViewById(R.id.btnChonHinh);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moThuMucHinh();
            }
        });
    }

    private void moThuMucHinh() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn Hình"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);

                luuHinhAnhVaoMay(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể tải hình ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void luuHinhAnhVaoMay(Bitmap bitmap) {
        try {
            FileOutputStream fos = openFileOutput("imageFile", Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể lưu hình ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void taiHinhAnhLen() {
        try {
            FileInputStream fis = openFileInput("imageFile");
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            imageView.setImageBitmap(bitmap);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Không có hình ảnh để tải", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Bitmap savedBitmap = taiHinhAnhLen();
//        if (savedBitmap != null) {
//            imageView.setImageBitmap(savedBitmap);
//        }
//    }
    @Override
    protected void onStart() {
        super.onStart();
        taiHinhAnhLen();
    }

    @Override
    public void onDiaLogClose(DialogInterface dialogInterface) {
        mList.clear();
        showData();
        adapter.notifyDataSetChanged();
    }
}