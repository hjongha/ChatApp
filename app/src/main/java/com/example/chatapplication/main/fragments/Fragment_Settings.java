package com.example.chatapplication.main.fragments;

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
import com.example.chatapplication.main.Add_Friend;
import com.example.chatapplication.main.Remove_Friend;
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

        auto = new SharedPreferences() {        // SharedPreferences ?????? ?????? (???????????? ??????)
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

        // ????????? ?????? ?????????
        TextView textView_profile = (TextView) view.findViewById(R.id.settings_profile);
        textView_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(context, Settings_Profile.class);
                startActivity(intent);
            }
        });

        // ?????? ?????? ?????????
        TextView textView_name = (TextView) view.findViewById(R.id.settings_name);
        textView_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
                EditText editText = new EditText(getActivity());
                editText.setHint("name");

                dlg.setTitle("???????????? ????????? ??????????????????.");
                dlg.setView(editText);
                dlg.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "?????????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String setName_str = editText.getText().toString();
                        if (setName_str.equals("")) {
                            Toast.makeText(context, "???????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                        } else {
                            myRef = database.getReference("member").child("UserAccount").child(firebaseUser.getUid());
                            myRef.child("name").setValue(setName_str);
                            Toast.makeText(context, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dlg.show();
            }
        });

        // ???????????? ?????? ?????????
        TextView textView_password = (TextView) view.findViewById(R.id.settings_password);
        textView_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
                EditText editText = new EditText(getActivity());
                editText.setHint("password");
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                dlg.setTitle("?????? ????????? ?????? ?????? ??????????????? ??????????????????.");
                dlg.setView(editText);
                dlg.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "?????????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String myPwd_str = editText.getText().toString();
                        if (myPwd_str.equals("")) {
                            Toast.makeText(context, "??????????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                        } else {
                            myRef = database.getReference("member").child("UserAccount").child(firebaseUser.getUid()).child("password");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue(String.class).equals(myPwd_str)) {
                                        Toast.makeText(context, "?????????????????????.", Toast.LENGTH_SHORT).show();
                                        AlertDialog.Builder dlg2 = new AlertDialog.Builder(getActivity());
                                        EditText editText = new EditText(getActivity());
                                        editText.setHint("password");
                                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                                        dlg2.setTitle("???????????? ??????????????? ??????????????????.");
                                        dlg2.setView(editText);
                                        dlg2.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Toast.makeText(context, "?????????????????????.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        dlg2.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String setPwd_str = editText.getText().toString();
                                                if (setPwd_str.equals("")) {
                                                    Toast.makeText(context, "??????????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    // ??? ???????????? DB??? ?????? ??? Auth ????????????
                                                    myRef = database.getReference("member").child("UserAccount").child(firebaseUser.getUid()).child("password");
                                                    myRef.setValue(setPwd_str);
                                                    firebaseUser.updatePassword(setPwd_str);
                                                    // ??? ???????????? ?????? ????????? ??????
                                                    SharedPreferences.Editor autoLogin = auto.edit();
                                                    autoLogin.putString("auto_Pwd", setPwd_str);
                                                    autoLogin.commit();

                                                    Toast.makeText(context, "??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        dlg2.show();
                                    }
                                    else {
                                        Toast.makeText(context, "??????????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
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

        // ???????????? ?????????
        TextView textView_logout = (TextView) view.findViewById(R.id.settings_logout);
        textView_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
                dlg.setTitle("???????????? ??????");
                dlg.setMessage("??? ???????????? ???????????? ???????????????????");
                dlg.setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "?????????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("???", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // ?????? ????????? ??????, ??? ?????????
                        SharedPreferences.Editor autoLogin = auto.edit();
                        autoLogin.putString("auto_Id", "");
                        autoLogin.putString("auto_Pwd", "");
                        autoLogin.commit();

                        intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        Toast.makeText(context, "??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.show();
            }
        });

        // ?????? ?????? ?????????
        TextView textView_addF = (TextView) view.findViewById(R.id.settings_addF);
        textView_addF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(context, Add_Friend.class);
                startActivity(intent);
            }
        });

        // ?????? ?????? ?????????
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
