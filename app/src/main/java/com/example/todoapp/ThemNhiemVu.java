package com.example.todoapp;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ThemNhiemVu extends BottomSheetDialogFragment {

    public static final String TAG = "ThemNhiemVu";

    private TextView datNgayKetThuc;
    private EditText txtSua;
    private Button btnSave;
    private FirebaseFirestore firestore;
    private Context context;
    private String ngayKetThuc = "";
    private String id = "";
    private String capNhatNgay = "";
    private ImageButton btn_mic;
    private static final int RECOGNIZER_CODE = 1;
    public static ThemNhiemVu newInstance(){
        return new ThemNhiemVu();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.them_nhiem_vu , container , false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        datNgayKetThuc = view.findViewById(R.id.set_ngay_ket_thuc);
        txtSua = view.findViewById(R.id.task_edittext);
        btnSave = view.findViewById(R.id.btnSave);
        btn_mic = view.findViewById(R.id.btn_mic);

        btn_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ấn để nói");
                startActivityForResult(intent, RECOGNIZER_CODE);
            }
        });

        firestore = FirebaseFirestore.getInstance();

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            String task = bundle.getString("task");
            id = bundle.getString("id");
            capNhatNgay = bundle.getString("due");

            txtSua.setText(task);
            datNgayKetThuc.setText(capNhatNgay);

            if (task.length() > 0){
                btnSave.setEnabled(false);
                btnSave.setBackgroundColor(Color.GRAY);
            }
        }

        txtSua.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(s.toString().equals("")){
                    btnSave.setEnabled(false);
                    btnSave.setBackgroundColor(Color.GRAY);
                }else {
                    btnSave.setEnabled(true);
                    btnSave.setBackgroundColor(getResources().getColor(R.color.green_blue));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        datNgayKetThuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();

                int MONTH = calendar.get(Calendar.MONTH);
                int YEAR = calendar.get(calendar.YEAR);
                int DAY = calendar.get(calendar.DATE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1;
                        datNgayKetThuc.setText(dayOfMonth + "/" + month + "/" + year );
                        ngayKetThuc = dayOfMonth + "/" + month + "/" + year;

                    }
                }, YEAR, MONTH, DAY);
                datePickerDialog.show();
            }
        });
        boolean finalIsUpdate = isUpdate;
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nhiemVu = txtSua.getText().toString();

                if (finalIsUpdate){
                    firestore.collection("task").document(id).update("task", nhiemVu, "due" , ngayKetThuc);
                    Toast.makeText(context, "Cập nhật nhiệm vụ", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (nhiemVu.isEmpty()) {
                        Toast.makeText(context, "Nhiệm vụ trống không được phép sửa", Toast.LENGTH_SHORT).show();
                    } else {
                        Map<String, Object> taskMap = new HashMap<>();

                        taskMap.put("task", nhiemVu);
                        taskMap.put("due", ngayKetThuc);
                        taskMap.put("status", 0);
                        taskMap.put("time", FieldValue.serverTimestamp());

                        firestore.collection("task").add(taskMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Lưu thành công", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECOGNIZER_CODE && resultCode == RESULT_OK){
            ArrayList<String> taskText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            txtSua.setText(taskText.get(0).toString());
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof  OnDiaLogCloseListner){
            ((OnDiaLogCloseListner)activity).onDiaLogClose(dialog);
        }
    }
}
