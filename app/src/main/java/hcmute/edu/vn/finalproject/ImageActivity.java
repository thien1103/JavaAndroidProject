package hcmute.edu.vn.finalproject;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class ImageActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private ImageView resultImageView;
    private Button actionButton;

    private  Button translateBtn;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagetranslate);

        // Ánh xạ các thành phần giao diện từ layout
        resultImageView = findViewById(R.id.resultImageView);
        actionButton = findViewById(R.id.actionButton);
        resultTextView = findViewById(R.id.resultTextView);
        translateBtn = findViewById(R.id.translateBtn);

        // Kiểm tra và yêu cầu quyền truy cập bộ nhớ ngoại nếu cần
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }

        // Cài đặt hành động khi button được nhấn
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchGalleryIntent();
            }
        });

        // Cài đặt sự kiện click cho ImageView
        resultImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchGalleryIntent();
            }
        });
        // tạo sự kiện cho nút dịch sẽ chuyển dữ liệu vừa detect qua cho trang main dịch
        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lấy thông tin detect từ TextView
                String detectedText = resultTextView.getText().toString();

                // Tạo Intent để gửi dữ liệu detect về MainActivity
                Intent intent = new Intent(ImageActivity.this, MainActivity.class);
                intent.putExtra("DETECTED_TEXT", detectedText);
                startActivity(intent);

                // Đánh dấu kết thúc Activity hiện tại và trả về dữ liệu detect
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    // Mở thư viện ảnh để chọn hình ảnh
    private void dispatchGalleryIntent() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    // Xử lý kết quả trả về từ thư viện ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                    resultImageView.setImageBitmap(bitmap);
                    detectText(bitmap);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                }
            }
        }
    }

    // Phát hiện văn bản trên ảnh và hiển thị kết quả
    private void detectText(Bitmap bitmap) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.e("ERROR", "lỗi hệ thống");
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                TextBlock item = items.valueAt(i);
                stringBuilder.append(item.getValue());
                stringBuilder.append("\n");
            }
            resultTextView.setText(stringBuilder.toString());
        }
    }

    // Xử lý kết quả yêu cầu cấp quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền truy cập bộ nhớ được cấp", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền truy cập bộ nhớ để chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
