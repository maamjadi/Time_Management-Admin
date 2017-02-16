package com.example.ait.time_managementadmin;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest  {



    @Test
    public void ValidateEmptyFields() throws Exception {

        String emptyTestText = "";

        Utils utils = new Utils();

        assertTrue(utils.validate(emptyTestText));
    }
}
