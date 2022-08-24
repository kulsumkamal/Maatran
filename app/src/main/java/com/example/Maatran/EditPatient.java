package com.example.Maatran;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class EditPatient extends AppCompatActivity {

    User user;
    String id;
    boolean newDetails, isPatient;
    private EditText name, age, gender, mobile, emergency, address;
    DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_details);
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore.getInstance()
                .collection("UserDetails")
                .document(mUser.getEmail()).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                if (document.exists()) {
                                    if (document.getData().get("isWorker").toString().equals("false")) {
                                        findViewById(R.id.hospital_details).setVisibility(View.GONE);
                                        findViewById(R.id.view_l7).setVisibility(View.GONE);
                                        findViewById(R.id.employee_details).setVisibility(View.GONE);
                                        findViewById(R.id.view_l8).setVisibility(View.GONE);
                                    }
                                    else
                                    {
                                        EditText hospital_name = findViewById(R.id.hospital_name);
                                        hospital_name.setText(document.getData().get("hospitalName").toString());
                                        EditText employee_id = findViewById(R.id.employee_id);
                                        hospital_name.setText(document.getData().get("employeeId").toString());
                                    }
                                }
                            }
                        });

        newDetails = getIntent().getBooleanExtra("newDetails", false);
        isPatient = getIntent().getBooleanExtra("isPatient", true);

        name = findViewById(R.id.patient_name);
        age = findViewById(R.id.patient_age);
        gender = findViewById(R.id.patient_gender);
        mobile = findViewById(R.id.patient_mobile);
        emergency = findViewById(R.id.emergency_no);
        address = findViewById(R.id.patient_address);

        if(isPatient == false)
        {
            findViewById(R.id.age_details).setVisibility(View.GONE);
            findViewById(R.id.view_l5).setVisibility(View.GONE);
            findViewById(R.id.emergency).setVisibility(View.GONE);
            findViewById(R.id.view_20).setVisibility(View.GONE);
        }

            if (newDetails == false) {
                user = getIntent().getParcelableExtra("user");
                name.setText(user.getName());
                gender.setText(user.getGender());
                mobile.setText(user.getMobile());
                address.setText(user.getAddress());

                if (isPatient) {
                    age.setText(Long.toString(user.getAge()));
                    emergency.setText(user.getEmergency());
                    id = getIntent().getStringExtra("id");
                    docRef = FirebaseFirestore.getInstance()
                            .collection("UserDetails")
                            .document(mUser.getEmail())
                            .collection("Patients")
                            .document(id);
                } else {
                    docRef = FirebaseFirestore.getInstance()
                            .collection("UserDetails")
                            .document(mUser.getEmail());
                }
            } else {
                TextView head_text = findViewById(R.id.edit_details);
                head_text.setText("ADD DETAILS");
                if (isPatient) {
                    Toast.makeText(EditPatient.this, "Enter new patient details.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditPatient.this, "Enter user details.",
                            Toast.LENGTH_SHORT).show();
                }
                docRef = FirebaseFirestore.getInstance()
                        .collection("UserDetails")
                        .document(mUser.getEmail());
            }

    }

    public void saveDetails(View view)
    {
        if(newDetails)
        {
            user = new User();
        }
        user.setName(name.getText().toString());
        user.setGender(gender.getText().toString());
        user.setAddress(address.getText().toString());
        user.setMobile(mobile.getText().toString());
        if(isPatient) {
            user.setEmergency(emergency.getText().toString());
            user.setAge(Long.parseLong(age.getText().toString()));
        }

        if(checkDetails())
        {
            if(newDetails == false || (newDetails && isPatient == false))
            {
                docRef.set(user, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> Log.d("TAG", "DocumentSnapshot successfully written!"))
                        .addOnFailureListener(e -> Log.w("TAG", "Error writing document", e));
            }
            else
            {
                docRef.collection("Patients").add(user)
                        .addOnSuccessListener(aVoid -> Log.d("TAG", "DocumentSnapshot successfully written!"))
                        .addOnFailureListener(e -> Log.w("TAG", "Error writing document", e));
            }
            if(newDetails)
            {
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(intent);
            }
            else
            {
                super.finish();
            }
        }
    }

    public boolean checkDetails()
    {
        boolean flag=true;
        //check if all fields are filled and if all fields are valid
        if(user.getMobile().length()!=10)
        {
            Toast.makeText(getApplicationContext(),"Enter the correct mobile number",Toast.LENGTH_SHORT).show();
            flag=false;
        }
        if(isPatient && user.getEmergency().length()!=10)
        {
            Toast.makeText(getApplicationContext(), "Enter the correct emergency mobile number", Toast.LENGTH_SHORT).show();
            flag=false;
        }
        if(isPatient && user.getAge()<18||user.getAge()>90)
        {
            Toast.makeText(getApplicationContext(), "Only users above or 18 and below 90 are allowed to register", Toast.LENGTH_SHORT).show();
            flag=false;
        }

        return flag;
    }

    public void cancelChanges(View view)
    {
        if(newDetails) {
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(intent);
        }
        else {
            super.finish();
        }
    }
}
