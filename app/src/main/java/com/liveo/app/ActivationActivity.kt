<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="24dp"
    android:background="#FFFFFF">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Liveo IPTV"
        android:textSize="32sp"
        android:textColor="#000000"
        android:textStyle="bold"
        android:layout_marginBottom="48dp" />

    <EditText
        android:id="@+id/codeInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="أدخل كود التفعيل"
        android:inputType="textCapCharacters"
        android:textSize="18sp"
        android:padding="16dp"
        android:gravity="center"
        android:layout_marginBottom="24dp"
        android:background="#F0F0F0" />

    <Button
        android:id="@+id/activateButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="تفعيل"
        android:textSize="18sp"
        android:padding="16dp" />

</LinearLayout>
