package hcmute.edu.vn.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner,toSpinner;
    private TextInputEditText sourceEdt;
    private ImageView micIV, imageIV;
    private MaterialButton translateBtn;
    private TextView translatedTV;
    String[] fromLanguages={"From","English","Afrikaans","Arabic","Belarusian","Bulgarian","Bengali","Catalan","Japan","Welsh","Hindi","Vietnamese"};
    String[] toLanguages={"To","English","Afrikaans","Arabic","Belarusian","Bulgarian","Bengali","Catalan","Japan","Welsh","Hindi","Vietnamese"};
private static final int REQUEST_PERMISSION_CODE=1;
int languageCode,fromLanguagesCode,toLanguagesCode =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner=findViewById(R.id.idFromSpinner);
        toSpinner=findViewById(R.id.idToSpinner);
        sourceEdt=findViewById(R.id.idEdtSource);
        micIV=findViewById(R.id.idIVMic);
        imageIV=findViewById(R.id.idIVImg);
        translateBtn=findViewById(R.id.idBtnTranslate);
        translatedTV=findViewById(R.id.idTVTranslate);
//        // Kiểm tra xem có dữ liệu detect được gửi đến hay không
//        if (getIntent() != null && getIntent().hasExtra("DETECTED_TEXT")) {
//            Toast.makeText(MainActivity.this, "yes", Toast.LENGTH_SHORT).show();
//            // Lấy dữ liệu detect từ Intent
//            String detectedText = getIntent().getStringExtra("DETECTED_TEXT");
//
//            // Hiển thị dữ liệu detect trong ô dịch để dịch
//            sourceEdt.setText(detectedText);
//
////            // Xóa dữ liệu detect từ Intent để tránh hiển thị lại khi quay lại MainActivity
////            getIntent().removeExtra("DETECTED_TEXT");
//        }else {
//            Toast.makeText(MainActivity.this, "No detected text available", Toast.LENGTH_SHORT).show();
//        }
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // nhận ngôn ngữ đầu vào và sử dụng hàm getLanguageCode để lấy code tương ứng
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFromLanguage = fromLanguages[position];
                fromLanguagesCode = getLanguageCode(selectedFromLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter formAdapter=new ArrayAdapter(this,R.layout.spinner_item,fromLanguages);
        formAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(formAdapter);


        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedToLanguage = toLanguages[position];
                toLanguagesCode = getLanguageCode(selectedToLanguage);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*
         Thiết lập sự kiện khi click button, những thông báo lỗi tương ứng sẽ hiển thị ví dụ như:
        "làm ơn hãy nhập dữ liệu để dịch ", nếu không có lỗi sẽ gọi hàm translateText để dịch văn bản
        */

        ArrayAdapter toAdapter=new ArrayAdapter(this,R.layout.spinner_item,toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);
        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTV.setText(""); //xóa văn bản hiện tại để chuẩn bị cho lần dịch tới
                Log.e("TAG","to code = "+toLanguagesCode+"from code = "+fromLanguagesCode);
                if (sourceEdt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please to enter your text to tramslate", Toast.LENGTH_SHORT).show();
                } else if (fromLanguagesCode == 0) {
                    Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT).show();
                } else if (toLanguagesCode == 0) {
                    Toast.makeText(MainActivity.this, "Please select the language to make translate", Toast.LENGTH_SHORT).show();
                } else {
                    translateText(fromLanguagesCode,toLanguagesCode,sourceEdt.getText().toString()); // gọi hàm translateText
                }
            }


        });
        /*gắn sự kiện click cho icon voice để kích hoạt nhận diện giọng nói của hệ thống*/
        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); //nhận diện ngôn ngữ tự do, không giới hạn cấu trúc nhất định
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());// chỉ định ngôn ngữ mặc định là ngôn ngữ ban đầu của thiết bị
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text"); // hiện thị thông điệp cho người dùng trên ui
                try{
                    startActivityForResult(i,REQUEST_PERMISSION_CODE); // chạy hàm startActivityForResult
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }

            }
        });

        imageIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null && getIntent().hasExtra("DETECTED_TEXT")) {
            String detectedText = getIntent().getStringExtra("DETECTED_TEXT");
            sourceEdt.setText(detectedText);
            Log.d("DetectedText", "Detected text: " + detectedText); // Thêm log để kiểm tra xem detectedText có giá trị hay không
        }
    }

    // xử lý kết quả trả về từ nhận diện giọng nói sau đó đưa text đó hiển thị trên sourceEdt
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQUEST_PERMISSION_CODE){
            if(resultCode==RESULT_OK && data!=null){
                ArrayList<String>result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdt.setText(result.get(0));
            }
        }
    }

    // hàm để dịch văn bản đã nhận dạng nhận vào 3 tham số là "formLanguageCode, toLanguageCode, soure"
    // trong đó formLanguageCode là ngôn ngữ nguồn, toLanguageCode là ngôn ngữ đích và soure là văn bản cần dịch
    private  void translateText(int fromLanguagesCode,int toLanguagesCode,String source){
        translatedTV.setText("Downloanding Modal..."); // thông báo người dùng modal đang được tải
        FirebaseTranslatorOptions options=new FirebaseTranslatorOptions.Builder() //Tạo đối tượng FirebaseTranslatorOptions
                .setSourceLanguage(fromLanguagesCode)
                .setTargetLanguage(toLanguagesCode)
                .build();
        FirebaseTranslator translator= FirebaseNaturalLanguage.getInstance().getTranslator(options);

       FirebaseModelDownloadConditions conditions=new FirebaseModelDownloadConditions.Builder().build();

       translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
           @Override
           public void onSuccess(Void unused) {
               translatedTV.setText("Translating..."); //thông báo cho người dùng là đang dịch
               translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                   @Override
                   public void onSuccess(String s) {
                       translatedTV.setText(s);
                       Log.d("trans", s);
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(MainActivity.this,"Fail to translate: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                   }
               });
           }

       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {
               Toast.makeText(MainActivity.this,"Fail to download Modal "+e.getMessage(),Toast.LENGTH_SHORT).show();
           }
       });
    }

    // trả về giá trị tham số khi chọn
    public int getLanguageCode(String language){
        int languageCode=0;
        switch (language){
            case"English":
                languageCode= FirebaseTranslateLanguage.EN;
                break;
            case"Afrikaans":
                languageCode= FirebaseTranslateLanguage.AF;
                break;
            case"Arabic":
                languageCode= FirebaseTranslateLanguage.AR;
                break;
            case"Belarusian":
                languageCode= FirebaseTranslateLanguage.BE;
                break;
            case"Bulgarian":
                languageCode= FirebaseTranslateLanguage.BG;
                break;
            case"Bengali":
                languageCode= FirebaseTranslateLanguage.BN;
                break;
            case"Catalan":
                languageCode= FirebaseTranslateLanguage.CA;
                break;
            case"Japan":
                languageCode= FirebaseTranslateLanguage.JA;
                break;
            case"Vietnamese":
                languageCode= FirebaseTranslateLanguage.VI;
                break;



        }
        return languageCode;
    }

}