package com.share.debug;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView tvSharedContent;
    private Button btnCopy;
    private Button btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSharedContent = findViewById(R.id.tv_shared_content);
        btnCopy = findViewById(R.id.btn_copy);
        btnClear = findViewById(R.id.btn_clear);

        // 设置TextView可以滚动
        tvSharedContent.setMovementMethod(new ScrollingMovementMethod());

        // 设置复制按钮点击事件
        btnCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String content = tvSharedContent.getText().toString();
                    if (!TextUtils.isEmpty(content) && !content.equals("暂无分享数据")) {
                        ClipboardManager clipboard = 
                            (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = 
                            ClipData.newPlainText("shared_content", content);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "没有可复制的内容", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        // 设置清除按钮点击事件
        btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvSharedContent.setText("暂无分享数据");
                    Toast.makeText(MainActivity.this, "已清除", Toast.LENGTH_SHORT).show();
                }
            });

        // 处理分享进来的数据
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        String type = intent.getType();

        StringBuilder sharedInfo = new StringBuilder();

        // 添加基本信息
        sharedInfo.append("=== 分享信息 ===\n");
        sharedInfo.append("Action: ").append(action).append("\n");
        sharedInfo.append("Type: ").append(type).append("\n");
        sharedInfo.append("Time: ").append(System.currentTimeMillis()).append("\n\n");

        // 处理文本分享
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    sharedInfo.append("=== 文本内容 ===\n");
                    sharedInfo.append(sharedText);
                }

                String sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                if (sharedSubject != null) {
                    sharedInfo.append("\n\n=== 主题 ===\n");
                    sharedInfo.append(sharedSubject);
                }
            } else {
                sharedInfo.append("=== 数据类型 ===\n");
                sharedInfo.append("非文本类型: ").append(type);

                // 尝试获取URI
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null) {
                    sharedInfo.append("\n\n=== URI ===\n");
                    sharedInfo.append(uri.toString());
                }
            }
        } 
        // 处理多文件分享
        else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            sharedInfo.append("=== 多文件分享 ===\n");
            sharedInfo.append("类型: ").append(type).append("\n");

            java.util.ArrayList<Uri> uris = 
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uris != null) {
                sharedInfo.append("文件数量: ").append(uris.size()).append("\n\n");
                for (int i = 0; i < uris.size(); i++) {
                    sharedInfo.append("文件 ").append(i + 1).append(": ")
                        .append(uris.get(i).toString()).append("\n");
                }
            }
        }
        // 处理其他Intent
        else {
            sharedInfo.append("=== Intent 详情 ===\n");
            sharedInfo.append("数据: ").append(intent.getDataString()).append("\n");
            sharedInfo.append("Scheme: ").append(intent.getScheme()).append("\n");
            sharedInfo.append("Flags: 0x").append(Integer.toHexString(intent.getFlags()));
        }

        // 如果有额外数据，显示所有extras
        Bundle extras = intent.getExtras();
        if (extras != null && !extras.isEmpty()) {
            sharedInfo.append("\n\n=== Extras 详情 ===\n");
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                sharedInfo.append(key).append(": ");
                if (value instanceof String) {
                    sharedInfo.append("\"").append(value).append("\"");
                } else if (value instanceof String[]) {
                    String[] array = (String[]) value;
                    sharedInfo.append("[");
                    for (int i = 0; i < array.length; i++) {
                        if (i > 0) sharedInfo.append(", ");
                        sharedInfo.append("\"").append(array[i]).append("\"");
                    }
                    sharedInfo.append("]");
                } else {
                    sharedInfo.append(value);
                }
                sharedInfo.append("\n");
            }
        }

        tvSharedContent.setText(sharedInfo.toString());
    }
}
