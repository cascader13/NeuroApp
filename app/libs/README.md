# Capsule Android lib
## Dependencies 
1) Gradle 8.6
2) Android Studio Giraffe | 2022.3.1 Patch 1 or newer
3) openjdk 21.0.2 2024-01-16
4) ndkVersion '26.1.10909125'
5) Device with Android 10 or newer
___
## Get started

1) Link capsule lib into .gradle 

```
dependencies {
    implementation(name:'CapseuleService', ext:'aar')
}
```
2) Check ``AndroidManifest.xm``

<details>
  <summary> AndroidManifest.xml</summary>

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

    <uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="true" />

    <uses-permission
        android:name="android.permission.BLUETOOTH"
        android:maxSdkVersion="30" />
    <uses-permission
        android:name="android.permission.BLUETOOTH_ADMIN"
        android:maxSdkVersion="30" />
    <uses-permission
        android:name="android.permission.ACCESS_FINE_LOCATION"
        android:maxSdkVersion="30" />
    <uses-permission
        android:name="android.permission.ACCESS_COARSE_LOCATION"
        android:maxSdkVersion="30" />
    <uses-permission
        android:name="android.permission.BLUETOOTH_SCAN"
        android:usesPermissionFlags="neverForLocation" />
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />


</details>


3) Add native initialize code (kt)
```
import com.gelo.capsule.CapsuleNative
...
class MainActivity ...
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CapsuleNative.initCapsule(this)
    }
}
```
4) Now you can work with capsule android C library (see docs)
