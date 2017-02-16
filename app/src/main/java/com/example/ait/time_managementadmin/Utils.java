package com.example.ait.time_managementadmin;

/**
 * Created by HP on 01.12.2016.
 */

public class Utils {



    public boolean validate(String text) {
        boolean valid = true;

        if (!text.equals("")) {
            valid = false;
        }

        return valid;
    }


}
