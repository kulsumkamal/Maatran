package com.example.Maatran;

import android.util.Log;

import com.algorithmia.algo.AlgoFailure;
import com.algorithmia.algo.AlgoResponse;
import com.algorithmia.algo.AlgoSuccess;

import java.util.ArrayList;

public class InputHandler {
    //output stores prediction
    String output;
    void putInput(ArrayList<String> inputList)
    {
        String input = inputList.toString();
        //Inputs to be sent as comma separated string
        //To be handled by python script in algorithmia
        input = input.replace("[", "")
                .replace("]", "")
                .replace(" ", "");

        new AlgorithmiaTask<String>() {
            @Override
            protected void onPostExecute(AlgoResponse response) {
                if(response.isSuccess()) {
                    AlgoSuccess success = (AlgoSuccess) response;
                    output = success.asJsonString();
                } else {
                    AlgoFailure failure = (AlgoFailure) response;
                    Log.e("InputHandler", "Algorithm Error", failure.error);
                }
            }
        }.execute(input);
    }

}
