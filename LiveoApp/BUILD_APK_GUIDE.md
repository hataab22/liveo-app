# 📱 بناء APK - دليل مفصل جداً

## 🎯 قبل البناء: تعديل رابط API

### الخطوة 1: فتح المشروع في Android Studio

1. افتح **Android Studio**
2. اختر **Open**
3. اختر مجلد **LiveoApp** (المشروع الكامل)
4. اضغط **OK**
5. **انتظر Gradle Sync** (شريط أسفل اليمين سيظهر "Syncing...")

---

### الخطوة 2: تعديل رابط API

**في Android Studio:**

1. في الجانب الأيسر، افتح:
   ```
   app
   └── java
       └── com.liveo.app
           └── ApiClient
   ```

2. **انقر مرتين** على `ApiClient`

3. ستجد في **السطر 12**:
   ```kotlin
   private const val BASE_URL = "PUT_YOUR_RENDER_URL_HERE"
   ```

4. **غيّره** لرابط Render:
   ```kotlin
   private const val BASE_URL = "https://liveo-backend.onrender.com"
   ```
   (استخدم رابطك الخاص من Render!)

5. اضغط **Ctrl + S** (حفظ)

✅ تم!

---

## 🏗️ بناء APK

### الطريقة السهلة (APK للتجربة)

#### الخطوة 1: بناء APK

في Android Studio:

1. من القائمة العلوية، اضغط **Build**
2. اختر **Build Bundle(s) / APK(s)**
3. اختر **Build APK(s)**

**سيظهر شريط تقدم أسفل اليمين:**
```
Building...
Running Gradle tasks...
✅ APK(s) generated successfully.
```

---

#### الخطوة 2: تحديد موقع APK

عندما يظهر الإشعار:

1. اضغط **locate** في الإشعار

**أو يدوياً:**

2. روح لمجلد المشروع:
   ```
   LiveoApp\app\build\outputs\apk\debug\
   ```

3. ستجد ملف:
   ```
   app-debug.apk
   ```

✅ **هذا هو APK!**

---

### الطريقة الاحترافية (APK موقّع)

**إذا تبي APK احترافي:**

#### الخطوة 1: إنشاء مفتاح توقيع

1. **Build** → **Generate Signed Bundle / APK**
2. اختر **APK**
3. اضغط **Next**
4. اضغط **Create new...**

---

#### الخطوة 2: ملء معلومات المفتاح

```
Key store path: 
  اضغط 📁 واختر مكان واسم للمفتاح
  مثلاً: Desktop\liveo-key.jks

Key store password: liveo2024
  (احفظها!)

Key alias: liveo

Key password: liveo2024
  (نفس كلمة المرور)

Validity (years): 25

Certificate:
  First and Last Name: اسمك
  Organizational Unit: (اتركه فاضي أو اكتب "Developer")
  Organization: (اتركه فاضي أو اكتب "Liveo")
  City or Locality: (مدينتك أو اتركه فاضي)
  State or Province: (اتركه فاضي)
  Country Code (XX): SA
```

5. اضغط **OK**

---

#### الخطوة 3: بناء APK الموقّع

6. تأكد كلمة المرور صحيحة
7. اضغط **Next**
8. اختر **release**
9. **Signature Versions:**
   - ✅ V1 (Jar Signature)
   - ✅ V2 (Full APK Signature)
10. اضغط **Finish**

**سيبدأ البناء...**

---

#### الخطوة 4: تحديد موقع APK الموقّع

بعد ما يخلص البناء:

```
LiveoApp\app\release\app-release.apk
```

✅ **APK جاهز للنشر!**

---

## 📦 الفرق بين APK Debug و Release

| النوع | الاستخدام | الحجم | الأمان |
|-------|-----------|-------|--------|
| **debug** | تجربة شخصية | أكبر | عادي |
| **release** | نشر للعملاء | أصغر | موقّع |

**للتلفزيون:** استخدم أي واحد، لكن **release** أفضل.

---

## 🎮 نقل APK للتلفزيون

### الطريقة 1: USB (الأسهل)

1. احفظ APK على **فلاش USB**
2. شبك الفلاش بالتلفزيون
3. في التلفزيون:
   - Settings → Security
   - فعّل **Unknown Sources**
4. افتح **File Manager**
5. اختر الفلاش
6. اضغط على `app-debug.apk` أو `app-release.apk`
7. اضغط **Install**

✅ **مثبّت!**

---

### الطريقة 2: Google Drive

1. ارفع APK على Google Drive من الكمبيوتر
2. في التلفزيون، افتح Google Drive
3. حمّل APK
4. ثبته

---

### الطريقة 3: Send Anywhere

1. حمّل تطبيق **Send Anywhere** على الموبايل والتلفزيون
2. أرسل APK من الكمبيوتر للتلفزيون
3. ثبته

---

## 🎯 بعد التثبيت

### 1. افتح Liveo على التلفزيون

### 2. سيطلب كود التفعيل

### 3. افتح Admin Panel على الكمبيوتر:
   ```
   https://YOUR_RENDER_URL.onrender.com
   ```
   (ضع رابطك في المتصفح + أضف `/admin-panel/index.html` آخره)
   
   **أو** افتح الملف المحلي `admin-panel/index.html`

### 4. سجل دخول (admin / admin123)

### 5. أنشئ كود جديد:
   - رابط M3U: ضع أي رابط قنوات
   - المدة: 30 يوم
   - اضغط **إنشاء كود**

### 6. انسخ الكود (مثلاً: ABC12345)

### 7. في التلفزيون، أدخل الكود واضغط **تفعيل**

✅ **يشتغل!**

---

## 🐛 مشاكل محتملة

### المشكلة: "خطأ في الاتصال"

**الحل:**
1. تأكد Backend شغال على Render
2. افتح رابط Render في المتصفح للتأكد
3. تأكد من رابط API في `ApiClient.kt` صحيح
4. تأكد التلفزيون متصل بالإنترنت

---

### المشكلة: "التطبيق ما يثبت"

**الحل:**
1. Settings → Security → فعّل "Unknown Sources"
2. أو ابن APK موقّع (release)

---

### المشكلة: "Gradle Sync Failed"

**الحل:**
1. File → Invalidate Caches → Invalidate and Restart
2. انتظر إعادة التشغيل

---

## 📊 ملخص الخطوات

```
✅ 1. عدّل ApiClient.kt (ضع رابط Render)
✅ 2. Build → Build APK(s)
✅ 3. خذ app-debug.apk من:
      LiveoApp\app\build\outputs\apk\debug\
✅ 4. انقله للتلفزيون (USB/Drive)
✅ 5. ثبته
✅ 6. أنشئ كود من Admin Panel
✅ 7. أدخل الكود في التطبيق
✅ 8. استمتع! 🎉
```

---

## 🎉 تم!

الآن عندك تطبيق Liveo الكامل يشتغل من أي مكان!

لا تحتاج الكمبيوتر يكون شغال!

Backend على Render يعمل 24/7! 🚀
