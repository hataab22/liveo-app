# 🚀 رفع Backend على Render - خطوات مفصلة

## الخطوة 1: تحضير الملفات

1. حمّل ملف `backend.zip` (سأعطيك إياه)
2. فك الضغط
3. ستجد مجلد `backend` فيه:
   - app.py
   - requirements.txt

---

## الخطوة 2: إنشاء حساب GitHub (إذا ما عندك)

Render يحتاج GitHub لرفع الملفات.

### إذا ما عندك حساب:

1. روح [github.com](https://github.com)
2. اضغط **Sign up**
3. سجل حساب (بريد + كلمة مرور)
4. فعّل الحساب من البريد

✅ جاهز!

---

## الخطوة 3: رفع الملفات على GitHub

### أ) إنشاء Repository جديد

1. بعد تسجيل الدخول في GitHub
2. اضغط زر **+** (أعلى اليمين)
3. اختر **New repository**

### ب) الإعدادات:

```
Repository name: liveo-backend
Description: Liveo IPTV Backend
Public ✅ (اختار Public)
Add a README: لا تحدده
```

4. اضغط **Create repository**

---

### ج) رفع الملفات

**طريقة سهلة (من المتصفح):**

1. في صفحة الـ Repository الجديد
2. اضغط **uploading an existing file**
3. اسحب ملفات backend:
   - app.py
   - requirements.txt
4. اضغط **Commit changes**

✅ الملفات على GitHub!

---

## الخطوة 4: ربط Render بـ GitHub

1. ارجع لـ [render.com](https://render.com)
2. اضغط **Dashboard** (إذا مو فيه)
3. اضغط **New +** (أعلى اليمين)
4. اختر **Web Service**

### سيطلب ربط GitHub:

5. اضغط **Connect account** أو **GitHub**
6. سجل دخول GitHub
7. اضغط **Authorize Render**

✅ تم الربط!

---

## الخطوة 5: اختيار المشروع

1. سيظهر قائمة بمشاريعك
2. ابحث عن **liveo-backend**
3. اضغط **Connect**

---

## الخطوة 6: الإعدادات

### املأ النموذج:

```
Name: liveo-backend
(يمكنك تغييره لأي اسم تحبه)

Region: اختر أقرب منطقة (مثلاً: Frankfurt أو Singapore)

Branch: main

Root Directory: (اتركه فاضي)

Runtime: Python 3

Build Command: pip install -r requirements.txt

Start Command: python app.py
```

### تحت في الصفحة:

```
Instance Type: Free

Environment Variables: (اتركها فاضية)
```

7. اضغط **Create Web Service**

---

## الخطوة 7: الانتظار

سيبدأ Render في بناء المشروع:

```
Building...
==> Downloading buildpack...
==> Installing dependencies...
==> Starting application...
✅ Live
```

**يأخذ 2-5 دقائق**

---

## الخطوة 8: الحصول على الرابط

بعد ما يكتمل:

1. أعلى الصفحة ستجد رابط مثل:
   ```
   https://liveo-backend.onrender.com
   ```

2. **انسخ هذا الرابط!** (مهم جداً)

---

## الخطوة 9: اختبار Backend

1. افتح المتصفح
2. روح للرابط:
   ```
   https://liveo-backend.onrender.com
   ```

**سيظهر:**
```json
{
  "status": "running",
  "app": "Liveo Backend",
  "version": "1.0"
}
```

✅ **Backend شغال!**

---

## 🎉 تم!

الآن عندك:
- ✅ Backend شغال على Render
- ✅ متاح من أي مكان
- ✅ شغال 24/7

**الرابط:**
```
https://liveo-backend.onrender.com
```

---

## الخطوة القادمة

استخدم هذا الرابط في التطبيق!

(سأعطيك الخطوات التالية)
