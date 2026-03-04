# Production Comments (March 5, 2026)

```text
3 actions recommended
Edge-to-edge may not display for all users

User experience
Release name: 1.0.3 (production)
Your app uses deprecated APIs or parameters for edge-to-edge
One or more of the APIs you use or parameters that you set for edge-to-edge and window display have been deprecated in Android 15. Your app uses the following deprecated APIs or parameters:

LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
These start in the following places:

androidx.activity.EdgeToEdgeApi28.adjustLayoutInDisplayCutoutMode
To fix this, migrate away from these APIs or parameters.

User experience
Release name: 1.0.3 (production)
Is this useful?
Remove resizability and orientation restrictions in your app to support large screen devices
From Android 16, Android will ignore resizability and orientation restrictions for large screen devices, such as foldables and tablets. This may lead to layout and usability issues for your users.

We detected the following resizability and orientation restrictions in your app:

<activity android:name="com.hawkfranklin.aura.MainActivity" android:screenOrientation="PORTRAIT" />
To improve the user experience for your app, remove these restrictions and check that your app layouts work on various screen sizes and orientations by testing on Android 16 and below.
User experience
Release name: 1.0.3 (production)
```
