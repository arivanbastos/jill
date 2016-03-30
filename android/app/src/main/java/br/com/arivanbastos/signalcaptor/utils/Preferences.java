/***
 *
 * SharedPreferences
 * 	 - http://developer.android.com/guide/topics/data/data-storage.html#pref
 * 	 - http://developer.android.com/reference/android/content/SharedPreferences.html
 *
 **/
package br.com.arivanbastos.signalcaptor.utils;

import android.content.Context;
import android.content.SharedPreferences;

import br.com.arivanbastos.signalcaptor.BaseActivity;
import br.com.arivanbastos.signalcaptor.CustomApplication;

public class Preferences {

    /***
     * Retorna uma preferencia salva.
     *
     * @param key
     * @return
     */
    public static String readString(String name, String key, String defaultValue)
    {
        SharedPreferences preferences = BaseActivity.currentActivity.getSharedPreferences(
                name,
                Context.MODE_PRIVATE
        );

        return preferences.getString(key, defaultValue);
    }

    public static Boolean readBoolean(String name, String key, Boolean defaultValue)
    {
        SharedPreferences preferences = BaseActivity.currentActivity.getSharedPreferences(
                name,
                Context.MODE_PRIVATE
        );

        System.err.println("readBoolean "+name+"|"+key+"|"+preferences.getBoolean(key, defaultValue));
        return preferences.getBoolean(key, defaultValue);
    }

    /***
     * Salva uma preferencia.
     *
     * @param key
     * @param data
     */
    public static void writeString(String name, String key, String data)
    {
        SharedPreferences preferences = BaseActivity.currentActivity.getSharedPreferences(
                name,
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, data);
        editor.commit();
    }

    public static void writeBoolean(String name, String key, Boolean data)
    {
        SharedPreferences preferences = BaseActivity.currentActivity.getSharedPreferences(
                name,
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, data);
        editor.commit();

        System.err.println("writeBoolean "+name+"|"+key+"|"+data);
    }

    public static void remove(String name, String key)
    {
        SharedPreferences preferences = BaseActivity.currentActivity.getSharedPreferences(
                name,
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
    }

    /****
     * Limpa todas preferencias salvas.
     */
    public static void clear(String name)
    {
        SharedPreferences preferences = BaseActivity.currentActivity.getSharedPreferences(
                name,
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}