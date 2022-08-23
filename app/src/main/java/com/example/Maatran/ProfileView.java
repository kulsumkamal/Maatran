package com.example.Maatran;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileView extends AppCompatActivity {
    FirebaseFirestore db;
    FirebaseUser user;
    public static final String TAG="ProfileView";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile2);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching data..");
        progressDialog.show();
        user = FirebaseAuth.getInstance().getCurrentUser();
        TextView email = findViewById(R.id.emailId);
        email.setText(user.getEmail());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getUserDetails(user);
    }

    public void getUserDetails(FirebaseUser user)
    {

        db=FirebaseFirestore.getInstance();
        DocumentReference docRef=db.collection("UserDetails").document(user.getEmail());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    if(document.getData().get("isWorker").toString().equals("false")) {
                        findViewById(R.id.hospital_details).setVisibility(View.GONE);
                        findViewById(R.id.view_l7).setVisibility(View.GONE);
                        findViewById(R.id.employee_details).setVisibility(View.GONE);
                        findViewById(R.id.view_l8).setVisibility(View.GONE);
                    }
                    else
                    {
                        findViewById(R.id.age_details).setVisibility(View.GONE);
                        findViewById(R.id.view_l5).setVisibility(View.GONE);
                        TextView tv_hosp = findViewById(R.id.hospital_name);
                        tv_hosp.setText(document.getData().get("hospitalName").toString());
                        TextView tv_eid = findViewById(R.id.employee_id);
                        tv_eid.setText(document.getData().get("employeeId").toString());
                    }
                    TextView tv_name = findViewById(R.id.user_name);
                    tv_name.setText(document.getData().get("name").toString());
                    TextView tv_gender = findViewById(R.id.user_gender);
                    tv_gender.setText(document.getData().get("gender").toString());
                    TextView tv_adr = findViewById(R.id.user_adr);
                    tv_adr.setText(document.getData().get("address").toString());
                    TextView tv_mob = findViewById(R.id.user_mob);
                    tv_mob.setText(document.getData().get("mobile").toString());
                }
                else
                {
                    Log.d(TAG, "No such document");
                }
            }
            else
            {
                Log.d(TAG, "get failed with ", task.getException());
            }
            if(progressDialog.isShowing())
                progressDialog.dismiss();
        });
    }

    public void editProfile(View view)
    {
        db.collection("UserDetails")
                .document(FirebaseAuth.getInstance().getCurrentUser().getEmail()).get().addOnSuccessListener(documentSnapshot -> {
                    Intent intent = new Intent(getApplicationContext(), EditPatient.class);
                    intent.putExtra("isPatient", false);
                    intent.putExtra("user", documentSnapshot.toObject(User.class));
                    startActivity(intent);
                });
    }

    public void signOut(View view)
    {
        View popupConfirmSignOut = getLayoutInflater().inflate(R.layout.popupview_confirmation,null);
        ((TextView) popupConfirmSignOut.findViewById(R.id.text_dialog)).setText("Do you really want to sign out?");
        PopupWindow popupWindow = new PopupWindow(popupConfirmSignOut,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAtLocation(view,Gravity.CENTER,0,0);
        popupConfirmSignOut.findViewById(R.id.btn_yes).setOnClickListener(v->{
            FirebaseAuth.getInstance().signOut();
            Toast toast = Toast.makeText(getApplicationContext(),"You have successfully signed out, redirecting you to the log-in page",Toast.LENGTH_SHORT);
            toast.show();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        });
        popupConfirmSignOut.findViewById(R.id.btn_no).setOnClickListener(v->popupWindow.dismiss());
    }

    public void deleteProfile(View view)
    {
        LayoutInflater inflater = getLayoutInflater();
        View popupDeleteProfile = inflater.inflate(R.layout.popupview_confirmation,null);
        ((TextView)popupDeleteProfile.findViewById(R.id.text_dialog)).setText("Do you really want to delete your profile?");
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        // lets taps outside the popupWindow dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupDeleteProfile, width, height, true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        Button yes = popupDeleteProfile.findViewById(R.id.btn_yes);
        Button no = popupDeleteProfile.findViewById(R.id.btn_no);
        yes.setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(ProfileView.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Deleting profile..");
            progressDialog.show();
            deleteUserProfile(progressDialog,popupWindow);
        });
        no.setOnClickListener(v -> popupWindow.dismiss());
    }

    public void backToDashboard(View view)
    {
        super.finish();
    }

    public void changePassword(View view)
    {
        Intent intent = new Intent(getApplicationContext(),ChangePasswordActivity.class);
        intent.putExtra("UserName",((TextView) findViewById(R.id.user_name)).getText().toString());
        startActivity(intent);
    }

    public void deleteUserProfile(ProgressDialog progressDialog,PopupWindow popupWindow)
    {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                deleteUserData(user);
                Log.d(TAG, "User account deleted.");
                Toast toast = Toast.makeText(getApplicationContext(), "User account deleted.", Toast.LENGTH_SHORT);
                toast.show();
                progressDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Error deleting user account.", Toast.LENGTH_SHORT);
                toast.show();
                progressDialog.dismiss();
                popupWindow.dismiss();
                Log.d(TAG, "Error deleting document", task.getException());
            }
        });
    }

    public void deleteUserData(FirebaseUser user)
    {
        db=FirebaseFirestore.getInstance();
        DocumentReference docRef=db.collection("UserDetails").document(user.getEmail());
        CollectionReference colRef=db.collection("UserDetails").document(user.getEmail()).collection("Patients");
        colRef.get().addOnSuccessListener(value -> {
            for(DocumentSnapshot dc : value.getDocuments())
            {
                dc.getReference().delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    } else {
                        Log.d(TAG, "Error deleting document", task.getException());
                    }
                });
            }
        }).addOnFailureListener(aVoid->Log.d(TAG,"No such collection exists"));

        docRef.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "DocumentSnapshot successfully deleted!");
            } else {
                Log.d(TAG, "Error deleting document", task.getException());
            }
        });
    }
}