package com.example.chatapplication.main;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.chatapplication.R;
import com.example.chatapplication.login.LoginActivity;
import com.example.chatapplication.main.settings.Settings_Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Set;

public class Fragment_Settings extends Fragment {
    private View view;
    Context context;

    SharedPreferences auto;

    Intent intent;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        context = getActivity().getApplicationContext();

        auto = new SharedPreferences() {        // SharedPreferences 객체 생성 (로그아웃 처리)
            @Override
            public Map<String, ?> getAll() {return null;}

            @Nullable
            @Override
            public String getString(String key, @Nullable String defValue) {return null;}

            @Nullable
            @Override
            public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {return null;}

            @Override
            public int getInt(String key, int defValue) {return 0;}

            @Override
            public long getLong(String key, long defValue) {return 0;}

            @Override
            public float getFloat(String key, float defValue) {return 0;}

            @Override
            public boolean getBoolean(String key, boolean defValue) {return false;}

            @Override
            public boolean contains(String key) {return false;}

            @Override
            public Editor edit() {return null;}

            @Override
            public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}

            @Override
            public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
        };
        auto = getActivity().getSharedPreferences("autoLogin", MODE_PRIVATE);

        // 프로필 변경 리스너
        TextView textView_profile = (TextView) view.findViewById(R.id.settings_profile);
        textView_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(context, Settings_Profile.class);
                startActivity(intent);
            }
        });

        // 이름 변경 리스너
        TextView textView_name = (TextView) view.findViewById(R.id.settings_name);
        textView_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
                EditText editText = new EditText(getActivity());
                editText.setHint("name");

                dlg.setTitle("변경하실 이름을 입력해주세요.");
                dlg.setView(editText);
                dlg.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String setName_str = editText.getText().toString();
                        if (setName_str.equals("")) {
                            Toast.makeText(context, "변경하실 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            myRef = database.getReference("member").child("UserAccount").child(firebaseUser.getUid());
                            myRef.child("name").setValue(setName_str);
                            Toast.makeText(context, "변경이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dlg.show();
            }
        });

        // 비밀번호 변경 리스너
        TextView textView_password = (TextView) view.findViewById(R.id.settings_password);
        textView_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //intent = new Intent(context, Settings_Password.class);
                //startActivity(intent);

                AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
                EditText editText = new EditText(getActivity());
                editText.setHint("password");
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                dlg.setTitle("본인 확인을 위해 기존 비밀번호를 입력해주세요.");
                dlg.setView(editText);
                dlg.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String myPwd_str = editText.getText().toString();
                        if (myPwd_str.equals("")) {
                            Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            myRef = database.getReference("member").child("UserAccount").child(firebaseUser.getUid()).child("password");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue(String.class).equals(myPwd_str)) {
                                        Toast.makeText(context, "확인되었습니다.", Toast.LENGTH_SHORT).show();
                                        AlertDialog.Builder dlg2 = new AlertDialog.Builder(getActivity());
                                        EditText editText = new EditText(getActivity());
                                        editText.setHint("password");
                                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                                        dlg2.setTitle("변경하실 비밀번호를 입력해주세요.");
                                        dlg2.setView(editText);
                                        dlg2.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Toast.makeText(context, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        dlg2.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String setPwd_str = editText.getText().toString();
                                                if (setPwd_str.equals("")) {
                                                    Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    // 새 비밀번호 DB에 저장 및 Auth 업데이트
                                                    myRef = database.getReference("member").child("UserAccount").child(firebaseUser.getUid()).child("password");
                                                    myRef.setValue(setPwd_str);
                                                    firebaseUser.updatePassword(setPwd_str);
                                                    // 새 비밀번호 자동 로그인 등록
                                                    SharedPreferences.Editor autoLogin = auto.edit();
                                                    autoLogin.putString("auto_Pwd", setPwd_str);
                                                    autoLogin.commit();

                                                    Toast.makeText(context, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        dlg2.show();
                                    }
                                    else {
                                        Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }
                    }
                });
                dlg.show();
            }
        });

        // 로그아웃 리스너
        TextView textView_logout = (TextView) view.findViewById(R.id.settings_logout);
        textView_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
                dlg.setTitle("로그아웃 확인");
                dlg.setMessage("이 기기에서 로그아웃 하시겠습니까?");
                dlg.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 자동 로그인 변수, 값 초기화
                        SharedPreferences.Editor autoLogin = auto.edit();
                        autoLogin.putString("auto_Id", "");
                        autoLogin.putString("auto_Pwd", "");
                        autoLogin.commit();

                        intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        Toast.makeText(context, "로그아웃이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.show();
            }
        });

        // 친구 추가 리스너
        TextView textView_addF = (TextView) view.findViewById(R.id.settings_addF);
        textView_addF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(context, Add_Friend.class);
                startActivity(intent);
            }
        });

        // 친구 삭제 리스너
        TextView textView_removeF = (TextView) view.findViewById(R.id.settings_removeF);
        textView_removeF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(context, Remove_Friend.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
