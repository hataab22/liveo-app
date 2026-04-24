# 🎬 Liveo - الدليل الشامل الكامل

---

## 📚 محتويات الدليل

1. رفع Backend على Render
2. تعديل Admin Panel
3. بناء APK
4. التثبيت والاستخدام

---

# القسم 1: رفع Backend على Render

## الخطوات:

### 1. تحضير الملفات

في مجلد المشروع، ادخل:
```
LiveoApp/backend/
```

ستجد:
- app.py ✅
- requirements.txt ✅

---

### 2. إنشاء حساب GitHub (إذا ما عندك)

1. روح [github.com](https://github.com)
2. **Sign up** (سجل حساب جديد)
3. فعّل الحساب من بريدك

---

### 3. رفع الملفات على GitHub

#### أ) إنشاء Repository:

1. في GitHub، اضغط **+** (أعلى اليمين)
2. اختر **New repository**
3. اكتب:
   ```
   Repository name: liveo-backend
   Public ✅
   ```
4. اضغط **Create repository**

#### ب) رفع الملفات:

1. اضغط **uploading an existing file**
2. اسحب:
   - app.py
   - requirements.txt
3. اضغط **Commit changes**

✅ تم!

---

### 4. ربط Render بـ GitHub

1. في [render.com](https://render.com)
2. اضغط **New +**
3. اختر **Web Service**
4. اضغط **Connect GitHub**
5. سجل دخول GitHub
6. اضغط **Authorize Render**

---

### 5. اختيار المشروع

1. ابحث عن **liveo-backend**
2. اضغط **Connect**

---

### 6. الإعدادات

```
Name: liveo-backend
Region: Singapore (أو أي منطقة قريبة)
Branch: main
Runtime: Python 3
Build Command: pip install -r requirements.txt
Start Command: python app.py
Instance Type: Free ✅
```

7. اضغط **Create Web Service**

---

### 7. انتظر البناء (2-5 دقائق)

سيظهر:
```
Building...
Installing dependencies...
Starting...
✅ Live
```

---

### 8. احصل على الرابط

أعلى الصفحة:
```
https://liveo-backend-xxxx.onrender.com
```

**انسخه! مهم جداً!**

---

### 9. اختبار

افتح المتصفح:
```
https://liveo-backend-xxxx.onrender.com
```

سيظهر:
```json
{
  "status": "running",
  "app": "Liveo Backend"
}
```

✅ **Backend شغال!**

---

# القسم 2: تعديل Admin Panel

## الخطوة 1: فتح الملف

افتح:
```
LiveoApp/admin-panel/index.html
```

---

## الخطوة 2: التعديل

ابحث عن السطر (~280):
```javascript
const API_URL = 'PUT_YOUR_RENDER_URL_HERE';
```

غيّره:
```javascript
const API_URL = 'https://liveo-backend-xxxx.onrender.com';
```
(ضع رابطك!)

احفظ (Ctrl + S)

---

## الخطوة 3: التجربة

1. افتح `index.html` في المتصفح
2. سجل دخول:
   ```
   admin / admin123
   ```

3. أنشئ كود تجريبي:
   ```
   M3U URL: http://example.com/test.m3u
   Duration: 30
   ```

4. اضغط **إنشاء كود**

✅ **Admin Panel يشتغل!**

---

# القسم 3: بناء APK

## الخطوة 1: فتح المشروع

1. افتح **Android Studio**
2. **Open** → اختر مجلد `LiveoApp`
3. انتظر Gradle Sync

---

## الخطوة 2: تعديل رابط API

في Android Studio:

1. افتح:
   ```
   app/java/com.liveo.app/ApiClient
   ```

2. السطر 12:
   ```kotlin
   private const val BASE_URL = "PUT_YOUR_RENDER_URL_HERE"
   ```

3. غيّره:
   ```kotlin
   private const val BASE_URL = "https://liveo-backend-xxxx.onrender.com"
   ```

4. احفظ (Ctrl + S)

---

## الخطوة 3: بناء APK

1. **Build** (القائمة العلوية)
2. **Build Bundle(s) / APK(s)**
3. **Build APK(s)**

انتظر...

```
✅ APK(s) generated successfully
```

4. اضغط **locate**

---

## الخطوة 4: أخذ APK

الملف في:
```
LiveoApp/app/build/outputs/apk/debug/app-debug.apk
```

✅ **APK جاهز!**

---

# القسم 4: التثبيت والاستخدام

## تثبيت على التلفزيون

### الطريقة 1: USB

1. احفظ `app-debug.apk` على فلاش
2. شبك الفلاش بالتلفزيون
3. Settings → Security → فعّل Unknown Sources
4. File Manager → الفلاش → اضغط على APK
5. Install

---

### الطريقة 2: Google Drive

1. ارفع APK على Drive
2. في التلفزيون، حمّله من Drive
3. ثبته

---

## الاستخدام

### 1. افتح Liveo على التلفزيون

### 2. سيطلب كود

### 3. افتح Admin Panel على الكمبيوتر

افتح المتصفح:
```
https://liveo-backend-xxxx.onrender.com
```

**⚠️ مهم:** أضف في آخر الرابط:
```
/admin-panel/index.html
```

**أو** افتح الملف المحلي `admin-panel/index.html`

### 4. أنشئ كود

```
M3U URL: ضع رابط قنوات حقيقي
Duration: 30
Customer: تجربة
```

اضغط **إنشاء كود**

### 5. انسخ الكود (مثلاً: ABC12345)

### 6. في التلفزيون، أدخل الكود

### 7. اضغط **تفعيل**

✅ **يشتغل!**

---

# 🎯 ملخص سريع

```
□ 1. سجل في Render.com
□ 2. سجل في GitHub.com
□ 3. ارفع backend على GitHub
□ 4. اربط Render بـ GitHub
□ 5. انشر Web Service
□ 6. انسخ رابط Render
□ 7. عدّل admin-panel/index.html (ضع الرابط)
□ 8. عدّل ApiClient.kt (ضع الرابط)
□ 9. Build APK
□ 10. ثبت على التلفزيون
□ 11. أنشئ كود من Admin Panel
□ 12. فعّل في التطبيق
□ 13. استمتع! 🎉
```

---

# 🐛 حل المشاكل

## "خطأ في الاتصال"

✅ تأكد Backend شغال على Render
✅ تأكد الرابط صحيح في ApiClient.kt
✅ تأكد التلفزيون متصل بالنت

---

## "الكود غير صحيح"

✅ أنشئ كود جديد من Admin Panel
✅ تأكد من كتابة الكود صح

---

## "APK ما يثبت"

✅ فعّل Unknown Sources
✅ ابن APK موقّع (release)

---

# 📞 ملاحظات مهمة

1. **Backend على Render مجاني** لكن يتوقف بعد 15 دقيقة بدون استخدام
2. **أول طلب بعد توقف** ياخذ 30-60 ثانية (عادي)
3. **إذا تبي أداء أفضل** استخدم خطة مدفوعة (7$/شهر)

---

# 🎉 مبروك!

الآن عندك:
✅ نظام IPTV كامل
✅ يشتغل من أي مكان
✅ Admin Panel احترافي
✅ تطبيق Android جاهز

**أي سؤال، أنا هنا! 😊**
