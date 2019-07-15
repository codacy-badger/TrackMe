package com.triple.trackme;

import android.app.Application;
import android.content.Context;


import com.triple.trackme.Data.Storage.User;
import com.triple.trackme.Data.Work.UserJson;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static androidx.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.*;

public class UserJson_Test {

    @Test
    public void Writing_and_reading_of_User_isCorrect() {

        //String filename = "UserData";

        User user = new User();
        ArrayList<String> array = new ArrayList<>();
        Context c = getContext();
        File file = c.getFilesDir();

        //init user
        user.login = "log";
        user.name = "Nikolay";
        user.photoFilePath = "photo/";
        user.surname = "Ivanov";
        array.add("trackDir1/");
        array.add("trackDir2/");
        array.add("trackDir2/");
        user.trackFilePaths = array;

        //write user and read user
        UserJson.WriteUserToJsonFile(file,user);
        User user2;
        user2 = UserJson.ReadUserFromJsonFile(file);

        //check
        boolean check = false;
        if      ((user.login.equals(user2.login)))
                //(user.name.equals(user2.name))&&
                //(user.photoFilePath.equals(user2.photoFilePath))&&
                //(user.surname.equals(user2.surname)))
                //(user.trackFilePaths.equals(user2.trackFilePaths)))
        {check = true;}
        else{
            System.out.println("ERROR: Users not equaled!");
            String a = user2.login;
            String b = user.login;
            System.out.println(a + a + "12" + b + b);
        }
        assertEquals(true,check);
    }

}
